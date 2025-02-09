package me.qigan.abse.fr.exc;

import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.events.PostMotionEvent;
import me.qigan.abse.events.PreMotionEvent;
import me.qigan.abse.fr.cbh.CombatHelperAimRandomize;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class PhantomAim {

    private static double speed = 3.5d;

    private static double devideCF = 5;

    public static boolean enabled = false;
    public static Float[] currentAngles = null;
    public static Float[] aimPoint = null;

    private static float sPreYaw = 0f;
    private static float sPrePitch = 0f;

    private static long lTime = 0L;
    private static long activeTime = 0L;

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    void tick(PacketEvent.SendEvent e) {
//        if (!enabled || currentAngles == null || currentAngles[0] == null || currentAngles[1] == null) return;
//        if (System.currentTimeMillis()-lTime>activeTime) return;
//        Minecraft.getMinecraft().getRenderViewEntity()
//        if (e.packet instanceof C03PacketPlayer) {
//            ReflectionHelper.setPrivateValue(C03PacketPlayer.class, (C03PacketPlayer) e.packet, currentAngles[0], "yaw", "field_149476_e");
//            ReflectionHelper.setPrivateValue(C03PacketPlayer.class, (C03PacketPlayer) e.packet, currentAngles[1], "pitch", "field_149473_f");
//        }
//    }

    @SubscribeEvent
    void postUpdate(PostMotionEvent e) {
        if (!enabled) return;
        if (currentAngles == null) return;
        if (System.currentTimeMillis()-lTime<activeTime) {
            if (currentAngles[0] != null) Minecraft.getMinecraft().thePlayer.rotationYaw = sPreYaw;
            if (currentAngles[1] != null) Minecraft.getMinecraft().thePlayer.rotationPitch = sPrePitch;
        } else {
            enabled = false;
            currentAngles = null;
        }
    }

    @SubscribeEvent
    void preUpdate(PreMotionEvent e) {
        if (!enabled) return;
        if (currentAngles == null) return;
        if (System.currentTimeMillis()-lTime<activeTime) {
            sPreYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
            sPrePitch = Minecraft.getMinecraft().thePlayer.rotationPitch;

            if (currentAngles[0] != null) Minecraft.getMinecraft().thePlayer.rotationYaw = currentAngles[0];
            if (currentAngles[1] != null) Minecraft.getMinecraft().thePlayer.rotationPitch = currentAngles[1];
        } else {
            enabled = false;
            currentAngles = null;
        }
    }

    @SubscribeEvent
    void clientTick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        if (e.phase == TickEvent.Phase.END && aimPoint != null) {
            if (System.currentTimeMillis()-lTime<activeTime) {
                if (aimPoint.length > 1) {
                    double s = speed + CombatHelperAimRandomize.createRandomDouble();

                    if (aimPoint[0] != null) currentAngles[0] += (float) ((aimPoint[0] - currentAngles[0]) * (s / devideCF));
                    if (aimPoint[1] != null) currentAngles[1] += (float) ((aimPoint[1] - currentAngles[1]) * (s / devideCF));
                }
            }
        }
    }

    @SubscribeEvent
    void renderOve(RenderGameOverlayEvent.Text e) {
        if (!enabled) return;
        Esp.drawCenteredString(currentAngles[0]+" "+currentAngles[1], 500, 200, 0xFFFFFF, S2Dtype.DEFAULT);
    }

    public static void resetSmoothParam() {
        speed = 3.5d;
        devideCF = 5;
    }

    public static void setupSmoothParam(double speed, double devideCF) {
        PhantomAim.speed = speed;
        PhantomAim.devideCF = devideCF;
    }

    public static void set(Float[] angles) {
        currentAngles = angles;
    }

    public static void call(Float[] angles, long time, boolean smooth) {
        lTime = System.currentTimeMillis();
        if (smooth) {
            aimPoint = angles.clone();
            if (currentAngles == null) currentAngles = new Float[]{Minecraft.getMinecraft().thePlayer.rotationYaw, Minecraft.getMinecraft().thePlayer.rotationPitch};
        }
        else {
            currentAngles = angles.clone();
        }
        activeTime = time;
        enabled = true;
    }

    public static void setEnabled(boolean enabled) {
        PhantomAim.enabled = enabled;
    }

    public static void on() {
        setEnabled(true);
    }

    public static void off() {
        setEnabled(false);
    }
}
