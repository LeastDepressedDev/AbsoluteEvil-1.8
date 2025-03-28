package me.qigan.abse.fr.dungons.terminals;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.EDLogic;
import me.qigan.abse.crp.Module;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.fr.exc.InvWalk;
import me.qigan.abse.fr.exc.TimeoutTasks;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalsModule extends Module implements EDLogic {

    public static boolean fc = true;
    public static Terminal inTerminal = null;
    public static long lastTime = -1;
    public static boolean called = false;
    public static boolean ready = false;

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        if (Minecraft.getMinecraft().currentScreen == null && (inTerminal != null || !fc)) {
            resetDefault();
        }
    }

    @SubscribeEvent
    void overlay(RenderGameOverlayEvent.Text e) {
        if (!isEnabled()) return;
        if (true) {
            Esp.drawAllignedTextList(Arrays.asList(
                    "fc: "+Boolean.toString(fc),
                    "inTerminal: " + Boolean.toString(inTerminal!=null),
                    "lastClick: " + (lastTime == -1 ? "None" : Long.toString(System.currentTimeMillis()-lastTime))
                    ), e.resolution.getScaledWidth()/4, e.resolution.getScaledHeight()/4*3, false, e.resolution, S2Dtype.DEFAULT);
        }
    }

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        resetDefault();
    }

    @SubscribeEvent
    void packetSent(PacketEvent.SendEvent e) {
        if (!isEnabled()) return;
        if (e.packet instanceof C0DPacketCloseWindow) {
            resetDefault();
        }
    }

    private void resetDefault() {
        fc = true;
        called = false;
        ready = true;
        inTerminal = null;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    void packetLow(PacketEvent.ReceiveEvent e) {
        if (e.packet instanceof S2DPacketOpenWindow) {
            //if (inTerminal!=null) {
                InvWalk.invWalk = Index.MAIN_CFG.getBoolVal("terms_walk");
                InvWalk.shadowRotation = Index.MAIN_CFG.getBoolVal("terms_rot");
            //}
        }
    }

    @SubscribeEvent
    void guiPacket(PacketEvent.ReceiveEvent e) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!isEnabled()) return;
        if (e.packet instanceof S2DPacketOpenWindow) {
            ready = true;
            if (inTerminal == null) {
                String title = ((S2DPacketOpenWindow) e.packet).getWindowTitle().getUnformattedText();

                for (Terminal.Linker link : Terminal.Linker.values()) {
                    Matcher matcher = link.regex.matcher(title);
                    if (matcher.matches()) {
                        inTerminal = link.instance(matcher);
                        fc = true;
                        break;
                    }
                }
            }
            if (fc) return;
            //Idk why I placed this check but let it be!
            if (inTerminal == null) return;
            Terminal.ClickInfo info = inTerminal.next(((S2DPacketOpenWindow) e.packet).getWindowId());
            if (info == null) {
                System.out.println("Click Info returned null -> terminal should be finished!");
                return;
            }
            long cd = Index.MAIN_CFG.getIntVal("terms_cd");
            int rd = Index.MAIN_CFG.getIntVal("terms_cdr");
            if (rd > 0) {
                Random rand = new Random();
                cd += rand.nextInt() % (rd+1);
            }
            dispatchDelayedClick(info, cd);
        }
        if (e.packet instanceof S2EPacketCloseWindow) {
            resetDefault();
            return;
        }
        if (e.packet instanceof S30PacketWindowItems) {
            if (inTerminal == null) return;
            if (!ready || called) return;
            ContainerChest c = TerminalUtils.getOpenedChestContainer();
            if (c==null) return;
            inTerminal.build(((S30PacketWindowItems) e.packet).getItemStacks());
            Terminal.ClickInfo info = inTerminal.next(c.windowId);
            if (info == null) {
                System.out.println("Click Info returned null -> terminal should be finished!");
                return;
            }
            long cd = Index.MAIN_CFG.getIntVal(fc ? "terms_fc" : "terms_cd");
            int rd = Index.MAIN_CFG.getIntVal("terms_cdr");
            if (rd > 0) {
                Random rand = new Random();
                cd += rand.nextInt() % (rd+1);
            }
            dispatchDelayedClick(info, cd);
        }
    }

    public static void dispatchDelayedClick(Terminal.ClickInfo info, long delay) {
        TimeoutTasks.addTimeout(() -> delayedClick(info), delay);
        System.out.println(String.format("Called click %d:%d", info.slot, info.type));
        called = true;
        ready = false;
        fc = false;
    }

    public static void delayedClick(Terminal.ClickInfo info) {
        ContainerChest current = TerminalUtils.getOpenedChestContainer();
        if (current == null) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A76[ABSE terminals] \u00A7cCancelled click due to null was returned for current inventory."));
            return;
        }
        if (current.windowId != info.windowId) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A76[ABSE terminals] \u00A7cCancelled click due to unproved window id."));
            return;
        }
        if (Minecraft.getMinecraft().currentScreen != null) {
            Minecraft.getMinecraft().playerController.windowClick(info.windowId, info.slot, info.type, 0, Sync.player());
            lastTime = System.currentTimeMillis();
            System.out.println(String.format("Clicked %d:%d", info.slot, info.type));
        }
        called = false;
    }

    @Override
    public String id() {
        return "terms";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Terminals Issue";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("terms_fc", "First click delay", ValType.NUMBER, "350"));
        list.add(new SetsData<>("terms_cd", "Click delay[after gui update]", ValType.NUMBER, "100"));
        list.add(new SetsData<>("terms_cdr", "Delay randomization amount", ValType.NUMBER, "0"));
        list.add(new SetsData<>("terms_walk", "Inventory walk", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("terms_rot", "Allow rotation in terminal", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("terms_clust", "Use clusterization methods", ValType.BOOLEAN, "false"));
        return list;
    }

    @Override
    public String description() {
        return "I just don't the way it is implemented in ct modules...\nSo now ABSE have it!";
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        resetDefault();
    }
}
