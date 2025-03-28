package me.qigan.abse.fr.exc;

import me.qigan.abse.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InvWalk {
    public static boolean invWalk = false;
    public static boolean shadowRotation = false;

    public static void reset() {
        invWalk = false;
        shadowRotation = false;
    }

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        reset();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    void packetSent(PacketEvent.SendEvent e) {
        if (e.packet instanceof C0DPacketCloseWindow) {
            reset();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    void packetReceived(PacketEvent.ReceiveEvent e) {
        if (e.packet instanceof S2EPacketCloseWindow) {
            reset();
        }
    }
}
