package me.qigan.abse.fr.exc;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.KeybindManager;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.EDLogic;
import me.qigan.abse.crp.Module;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.mapping.routing.RouteUpdater;
import me.qigan.abse.sync.CommandRoute;
import me.qigan.abse.sync.GenCommandDispatcher;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@DangerousModule
public class Blink extends Module implements EDLogic {
    public static boolean S08RecievedRecently = false;
    public static long skipped = 0;
    public static BlinkRoute recorded = new BlinkRoute();
    public static boolean recordState = false;
    public static boolean playingRoute = false;


    @SubscribeEvent
    public void s08got(PacketEvent.ReceiveEvent e) {
        if (e.packet instanceof S08PacketPlayerPosLook) {
            S08RecievedRecently = true;
            TickTasks.call(() -> S08RecievedRecently = false, 1);
        }
    }

    @SubscribeEvent
    public void collector(PacketEvent.SendEvent e) {
        if (!isEnabled() || !Index.KEY_MANAGER.get("blink_charge").isDown() || playingRoute) return;

        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        if (!(e.packet instanceof C03PacketPlayer)) return;
        if (e.packet instanceof C03PacketPlayer.C05PacketPlayerLook || e.packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) return;

        if (Sync.ticksStill == 0) return;
        if (!Sync.player().onGround) return;

        e.setCanceled(true);
    }

    @SubscribeEvent
    public void lowest(PacketEvent.SendEvent e) {
        if (!isEnabled()) return;
        TickTasks.call(() -> {if ((e.packet instanceof C03PacketPlayer) && e.isCanceled()) skipped++;}, 1);
    }

    public static double[] prevPacket = {0, 0, 0};

    @SubscribeEvent
    public void recorder(PacketEvent.SendEvent e) {
        if (!isEnabled() || !recordState || playingRoute) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        if (!(e.packet instanceof C03PacketPlayer)) return;

        switch (e.packet.getClass().getSimpleName()) {
            case "C05PacketPlayerLook":
            case "C03PacketPlayer":
                return;
            default:
            {
                C03PacketPlayer packet = (C03PacketPlayer) e.packet;
                double[] pack = {packet.getPositionX(), packet.getPositionY(), packet.getPositionZ()};
                if (!(pack[0] == prevPacket[0] && pack[1] == prevPacket[1] && pack[2] == prevPacket[2])) {
                    recorded.add(packet);
                }
            }
            break;
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        if (Index.KEY_MANAGER.get("blink_rtr").isPressed()) {
            if (recordState) {

            } else {
                recorded = new BlinkRoute();
            }

            recordState=!recordState;
        }
    }

    @SubscribeEvent
    public void renderInterface(RenderGameOverlayEvent.Text e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        Esp.drawCenteredString(Long.toString(skipped), e.resolution.getScaledWidth()/2, e.resolution.getScaledHeight()/2+10, 0xFFFFFF, S2Dtype.DEFAULT);
    }

    @SubscribeEvent
    public void renderWorldLast(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        recorded.render(e);
    }

    public static void clearQueue() {
        skipped = 0;
    }

    @Override
    public String id() {
        return "blink";
    }

    @Override
    public Specification category() {
        return Specification.QOL;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("blink_charge", "Charge", ValType.KEYBINDING, Keyboard.KEY_P));
        list.add(new SetsData<>("blink_clear", "Clear skipped", ValType.BUTTON, (Runnable) Blink::clearQueue));
        list.add(new SetsData<>("blink_rtr", "Runtime route record", ValType.KEYBINDING, Keyboard.KEY_RCONTROL));

        return list;
    }

    @Override
    public String description() {
        return "An interesting shit that might ban you ;)";
    }

    @Override
    public void onEnable() {
        clearQueue();
        recorded = new BlinkRoute();
    }

    @Override
    public void onDisable() {
        clearQueue();
        recorded = new BlinkRoute();
        recordState = false;
        playingRoute = false;
    }

    public static class BlinkRoute {
        public List<AddressedData<Vec3, Boolean>> packets = new ArrayList<>();

        public void add(C03PacketPlayer playerState) {
            this.packets.add(new AddressedData<>(new Vec3(playerState.getPositionX(), playerState.getPositionY(), playerState.getPositionZ()), playerState.isOnGround()));
        }

        public void execute() {
            if (packets.isEmpty()) return;
            if (skipped < packets.size()) return;
            playingRoute = true;
            Vec3 last = null;
            for (AddressedData<Vec3, Boolean> pack : packets) {
                if (skipped > 0) {
                    Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C03PacketPlayer.
                            C04PacketPlayerPosition(pack.getNamespace().xCoord, pack.getNamespace().yCoord, pack.getNamespace().zCoord, pack.getObject()));
                    last = pack.getNamespace();
                    skipped--;
                }
            }
            Sync.player().setPosition(last.xCoord, last.yCoord, last.zCoord);
            TickTasks.call(() -> playingRoute = false, 1);
        }

        public void render(RenderWorldLastEvent e) {
            if (packets.size() < 2) return;
            Vec3 p;
            p = packets.get(0).getNamespace();
            Esp.drawPointInWorldCircle(new Vec3(p.xCoord, p.yCoord, p.zCoord), 1.3, 16, 2.3f, Color.magenta);
            List<Vec3> path = new ArrayList<>();
            for (AddressedData<Vec3, Boolean> pat : packets)
                path.add(new Vec3(pat.getNamespace().xCoord, pat.getNamespace().yCoord, pat.getNamespace().zCoord));
            RouteUpdater.drawPath(path, 1.4f, Color.magenta);
            p = packets.get(packets.size()-1).getNamespace();
            Esp.drawPointInWorldCircle(new Vec3(p.xCoord, p.yCoord, p.zCoord), 1.3, 16, 2.3f, Color.magenta);
        }
    }

    public static class BlinkCmd extends GenCommandDispatcher {
        public BlinkCmd() {
            super("blink");
        }

        @CommandRoute(route = "/+")
        public void execCurrentRoute(String[] args) {
            recorded.execute();
        }

        @CommandRoute(route = "/testspawn")
        public void test(String[] args) {
            skipped = 1000;
        }

        @Override
        protected Method[] methods() {
            return this.getClass().getMethods();
        }
    }
}
