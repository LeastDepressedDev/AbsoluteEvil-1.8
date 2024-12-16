package me.qigan.abse.crp;

import me.qigan.abse.config.AddressedData;
import net.minecraft.client.Minecraft;
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
}
