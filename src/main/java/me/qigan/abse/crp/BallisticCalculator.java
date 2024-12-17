package me.qigan.abse.crp;

import me.qigan.abse.config.AddressedData;
import me.qigan.abse.vp.Vec3List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class BallisticCalculator {
    public static final double ka = 0.99;
    public static final double ga = 0.05;
    public static final double v0a = 3;

    public static double[] calcRelPos(float angle, final double v0, final double k, final double g, double t) {
        angle = (float) (angle * Math.PI / 180);

        double v0x = v0 * Math.cos(angle);
        double v0y = v0 * Math.sin(angle);

        double logK = Math.log(k);
        double ktm1 = Math.pow(k, t)-1;

        double x = v0x*(ktm1/logK);
        double y = (
                (v0y*(ktm1/logK)) - (g*ktm1)/(k*Math.pow(logK, 2)) + (g*t)/(logK) + Minecraft.getMinecraft().thePlayer.getEyeHeight()
        );

        return new double[]{x, y};
    }

    public static Vec3 splitToVec3(double[] vec2, float yaw) {
        yaw = (float) Math.toRadians(yaw);
        return new Vec3(-vec2[0]*Math.sin(yaw), vec2[1], vec2[0]*Math.cos(yaw));
    }

    public static double[] calcRelPosArrow(float angle, double t) {
        return calcRelPos(angle, v0a, ka, ga, t);
    }

    /**
     * Return angle in radians despite balistic calculator uses degrees
     */
    public static AddressedData<Float, Double> solveForAngle(double v0, double k, double g, double x, double y, double precision, int lim) {
        double logK = Math.log(k);

        float angle = 0;
        for (int i = 1; i <= lim; i++) {
            double v0x = v0 * Math.cos(angle);
            double v0y = v0 * Math.sin(angle);
            double s = x*logK/v0x + 1;
            double t = Math.log(s)/logK;
            double ktm1 = Math.pow(k, t)-1;

            double ny = (v0y*(ktm1/logK)) - (g*ktm1)/(k*Math.pow(logK, 2)) + (g*t)/(logK) + Minecraft.getMinecraft().thePlayer.getEyeHeight();

            double dist = ny-y;
            if (Math.abs(dist) <= precision) return new AddressedData<>(angle, t);
            else {
                if (dist == 0) return new AddressedData<>(angle, t);
                else {
                    angle += (float) (Math.PI/Math.pow(2, i+1)) * (dist < 0 ? 1 : -1);
                }
            }
        }
        return new AddressedData<>(0f, -25d);
    }

    public static AddressedData<Float, Double> solveForArrowAngle(double x, double y, double precision, int lim) {
        return solveForAngle(v0a, ka, ga, x, y, precision, lim);
    }

    public static Double solveForTWPDC(AddressedData<Entity, Vec3List> pt, double roughness) {
        Double result = null;
        double mnd = Double.MAX_VALUE;
        final double k = ka;
        final double v0 = v0a;
        final double g = ga;
        double ax = Math.sqrt(Math.pow(pt.getObject().vecSum.x, 2) + Math.pow(pt.getObject().vecSum.z, 2));
        double ay = pt.getObject().vecSum.y;

        double d1x = Math.sqrt(Math.pow(pt.getNamespace().posX-Minecraft.getMinecraft().thePlayer.posX, 2) +
                Math.pow(pt.getNamespace().posZ-Minecraft.getMinecraft().thePlayer.posZ, 2));
        double d2x = Math.sqrt(Math.pow(pt.getNamespace().posX+pt.getObject().vecSum.x-Minecraft.getMinecraft().thePlayer.posX, 2) +
                Math.pow(pt.getNamespace().posZ+pt.getObject().vecSum.z-Minecraft.getMinecraft().thePlayer.posZ, 2));

        double d1y = pt.getNamespace().posY-Minecraft.getMinecraft().thePlayer.posY;
        double d2y = pt.getNamespace().posY+pt.getObject().vecSum.y-Minecraft.getMinecraft().thePlayer.posY;

        double cosx = Math.cos(Math.PI-Math.acos(-(Math.pow(d1x, 2)-Math.pow(d2x, 2)-Math.pow(ax, 2))/(2*d2x*ax))),
                cosy = Math.cos(Math.PI-Math.acos(-(Math.pow(d1y, 2)-Math.pow(d2y, 2)-Math.pow(ay, 2))/(2*d2y*ay)));

        double logK = Math.log(k);

        for (double t = 0; t < 3.2*20d; t+=roughness) {
            double ktm1 = Math.pow(k, t)-1;
            double tm1sq = Math.pow(t-1, 2);
            double sx = Math.pow(logK, 2)*((Math.pow(d2x, 2)+Math.pow(ax/20, 2)*tm1sq-0.1*d2x*ax*(t-1)*cosx)/(v0*ktm1));
            double sy = v0*ktm1-logK*Math.sqrt(
                    Math.pow(d2y, 2)+Math.pow(ay/20, 2)*tm1sq-0.1*d2y*ay*(t-1)*cosy
            ) + (g*ktm1)/(k*logK) - g*t;

            double diff = Math.abs(sx-sy);
            if (diff < mnd) {
                mnd = diff;
                result = t;
            }
        }

        return result;
    }
}
