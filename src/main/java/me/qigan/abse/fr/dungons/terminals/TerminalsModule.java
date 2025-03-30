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
import net.minecraft.item.ItemStack;
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

    public static boolean upt = false;
    public static boolean upt_used = true;

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        if (Minecraft.getMinecraft().currentScreen == null && (inTerminal != null || !fc)) {
            resetDefault();
        }
        if (inTerminal instanceof MelodyTerminal) return;
        if (Index.MAIN_CFG.getBoolVal("terms_ld") && inTerminal!=null && upt && !upt_used
                && lastTime != -1 && System.currentTimeMillis()-lastTime>Index.MAIN_CFG.getIntVal("terms_ld_t")) {
            upt = false;
            upt_used = true;
            fc = Index.MAIN_CFG.getBoolVal("terms_ld_fc");
            ContainerChest c = TerminalUtils.getOpenedChestContainer();
            if (c==null) return;
            matchTerminal(c.getLowerChestInventory().getName());
            inTerminal.build(c.getInventory().stream().toArray(ItemStack[]::new));
            Terminal.ClickInfo info = inTerminal.next(c.windowId);
            callPreClicK(info);
        }
    }

    @SubscribeEvent
    void melodyTick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        if (inTerminal instanceof MelodyTerminal) {
            if (System.currentTimeMillis() - lastTime > Index.MAIN_CFG.getIntVal("terms_fc") && !called) {
                ContainerChest c = TerminalUtils.getOpenedChestContainer();
                if (c==null) return;
                int currentSlot = 0;
                for (int i = 1; i <= 5; i++) {
                    if (c.getInventory().get(i).getMetadata() == 2) {
                        currentSlot = i;
                        break;
                    }
                }
                int[] cord = null;
                if (currentSlot == 0) return;
                for (int x = 1; x <= 5; x++) {
                    for (int y = 1; y <= 4; y++) {
                        int[] pc = {x, y};
                        if (c.getInventory().get(TerminalUtils.cordToSlot(pc)).getMetadata() == 5) {
                            cord = pc;
                            break;
                        }
                    }
                }
                if (cord == null) return;
                if (cord[0] == currentSlot) {
                    dispatchDelayedClick(new Terminal.ClickInfo(TerminalUtils.cordToSlot(new int[]{7, cord[1]}), 0, c.windowId), 20);
                }
            }
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
        upt = false;
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

    public static void matchTerminal(String guiName) {
        for (Terminal.Linker link : Terminal.Linker.values()) {
            Matcher matcher = link.regex.matcher(guiName);
            if (matcher.matches()) {
                try {
                    inTerminal = link.instance(matcher);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                fc = true;
                if (inTerminal instanceof MelodyTerminal) lastTime = System.currentTimeMillis();
                return;
            }
        }
    }

    @SubscribeEvent
    void guiPacket(PacketEvent.ReceiveEvent e) {
        if (!isEnabled()) return;
        if (e.packet instanceof S2DPacketOpenWindow) {
            upt = false;
            upt_used = false;
            ready = true;
            if (inTerminal == null) {
                String title = ((S2DPacketOpenWindow) e.packet).getWindowTitle().getUnformattedText();

                matchTerminal(title);

                TimeoutTasks.addTimeout(() -> {
                    if (inTerminal == null) {
                        ContainerChest c = TerminalUtils.getOpenedChestContainer();
                        if (c==null) return;
                        String sub = c.getLowerChestInventory().getName();

                        matchTerminal(sub);

                        if (inTerminal != null) {
                            if (inTerminal instanceof MelodyTerminal) return;
                            inTerminal.build(c.getInventory().stream().toArray(ItemStack[]::new));
                            Terminal.ClickInfo info = inTerminal.next(c.windowId);
                            callPreClicK(info);
                        }
                    }
                }, Index.MAIN_CFG.getIntVal("terms_fc"));
            }
            if (fc) return;
            //Idk why I placed this check but let it be!
            if (inTerminal == null) return;
            if (inTerminal instanceof MelodyTerminal) return;
            Terminal.ClickInfo info = inTerminal.next(((S2DPacketOpenWindow) e.packet).getWindowId());
            callPreClicK(info);
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
            if (inTerminal instanceof MelodyTerminal) return;
            inTerminal.build(((S30PacketWindowItems) e.packet).getItemStacks());
            Terminal.ClickInfo info = inTerminal.next(c.windowId);
            callPreClicK(info);
        }
    }

    public static void callPreClicK(Terminal.ClickInfo info) {
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

    public static void dispatchDelayedClick(Terminal.ClickInfo info, long delay) {
        TimeoutTasks.addTimeout(() -> Minecraft.getMinecraft().addScheduledTask(() -> delayedClick(info)), delay);
        System.out.println(String.format("Called click %d:%d", info.slot, info.type));
        called = true;
        ready = false;
        fc = false;
    }

    public static void delayedClick(Terminal.ClickInfo info) {
        ContainerChest current = TerminalUtils.getOpenedChestContainer();
        upt = true;
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
        list.add(new SetsData<>("terms_cd", "Click delay[after gui update]", ValType.NUMBER, "80"));
        list.add(new SetsData<>("terms_cdr", "Delay randomization amount", ValType.NUMBER, "0"));
        list.add(new SetsData<>("terms_com1", "Inv walking: ", ValType.COMMENT, null));
        list.add(new SetsData<>("terms_walk", "Inventory walk", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("terms_rot", "Allow rotation in terminal", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("terms_com2", "Advanced terminal settings: ", ValType.COMMENT, null));
        list.add(new SetsData<>("terms_ld", "Sync prediction[WIP]", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("terms_com_ld", "^^^ Recommended for sub 50ms main delay", ValType.COMMENT, null));
        list.add(new SetsData<>("terms_ld_fc", "First click on sync", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("terms_ld_t", "Sync time", ValType.NUMBER, "700"));
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
