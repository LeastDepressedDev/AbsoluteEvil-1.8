package me.qigan.abse.fr.exc;

import me.qigan.abse.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class PhantomAim {

    public static boolean enabled = false;
    public static Float[] currentAngles = null;
    public static Float[] preAngles = new Float[]{0f, 0f};
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

//    @SubscribeEvent
//    void rend( e) {
//
//    }

//    public static void set()

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
