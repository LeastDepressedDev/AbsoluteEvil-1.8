package me.qigan.abse.fr.dungons.m7p3;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.dungons.InvTracker;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.fr.exc.TickTasks;
import me.qigan.abse.fr.macro.LeapShortcut;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoM7P4 extends Module {

    public static final Vec3 startingPos = new Vec3(68, 130, 50);

    public static final BlockPos[] DIM = {new BlockPos(62, 126, 34), new BlockPos(64, 129, 36)};

    /**
     * ANGLE_MATRIX[row][line][axis]
     */
    public static final Float[][][] ANGLES_MATRIX = {
            {         {-15.3f, -10.1f},           {-7.6f, -10.1f}       },
            {         {-15.3f,  -2.5f},           {-7.6f,  -2.9f}       },
            {         {-15.3f,   4.3f},           {-8.1f,   4.7f}       }
    };

    public static int count = 0;
    public static boolean exec = true;
    public static boolean swapping = false;
    private static boolean leap = false;

    public static int line = 0;
    public static int row = 0;

    public static BlockPos last = null;

    private static int find() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) continue;
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.fishing_rod) return i;
        }
        return -1;
    }

    public static void warpAway() {
        leap = true;
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
        LeapShortcut.call(false);
        TickTasks.call(() -> ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1), 3);
        TickTasks.call(() -> {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
                GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
                ContainerChest c1 = (ContainerChest) chest.inventorySlots;
                for (int i = 9; i < 18; i++) {
                    if (c1.getInventory().get(i) == null) continue;
                    if (c1.getInventory().get(i).getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                        Minecraft.getMinecraft().currentScreen
                                .mc.playerController.windowClick(c1.windowId, i, 0, 0,
                                        Minecraft.getMinecraft().currentScreen.mc.thePlayer);
                        return;
                    }
                }
                //Minecraft.getMinecraft().thePlayer.closeScreen();
            }
        }, 7);
    }

    @SubscribeEvent
    void chat(ClientChatReceivedEvent e) {
        if (!Index.MAIN_CFG.getBoolVal("am7p4_warp") || !Utils.posInDim(Sync.playerPosAsBlockPos(), DIM, false)) return;
        if (Utils.cleanSB(e.message.getFormattedText())
                .startsWith(Minecraft.getMinecraft().thePlayer.getName()+" completed a device!")) warpAway();
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null || e.phase == TickEvent.Phase.END) return;
        if (!Sync.inDungeon) return;

        if (!Utils.posInDim(Sync.playerPosAsBlockPos(), DIM, false)) {
            last = null;
            count = 0;
            exec = true;
            leap = false;
            swapping = false;
            return;
        }

        if (leap) return;

        if (Index.MAIN_CFG.getBoolVal("am7p4_warp")) {
            for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if (Minecraft.getMinecraft().thePlayer.getDistanceToEntity(ent) < 4 &&
                        Utils.cleanSB(ent.getName()).equalsIgnoreCase("Active")) {
                    warpAway();
                    return;
                }
            }
        }

        if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null ||
                Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() != Items.bow) return;

        if (Index.MAIN_CFG.getBoolVal("am7p4_inv")) {
            long t = InvTracker.lost();
            if (exec && t > 0 && t < Index.MAIN_CFG.getIntVal("am7p4_inv_t")) {
                swapping = true;
                exec = false;
                new Thread(() -> {
                    try {
                        int slot = find();
                        if (slot == -1) {
                            swapping = false;
                            return;
                        }
                        int pre = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
                        long del = Index.MAIN_CFG.getIntVal("am7p4_inv_s");
                        Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;
                        Thread.sleep(del);
                        ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1);
                        Thread.sleep(del);
                        Minecraft.getMinecraft().thePlayer.inventory.currentItem = pre;
                        swapping = false;
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
            }
        }

        if (swapping) return;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                BlockPos pos = new BlockPos(startingPos).add(-x*2, -y*2, 0);
                if (Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock() == Blocks.emerald_block) {
                    if (!Utils.compare(pos, last)) {
                        count++;
                        last = pos;


                        if (x != 1) {
                            line = x == 0 ? 0 : 1;
                        }
                        row = y;
                        TickTasks.call(() -> ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1),
                                Index.MAIN_CFG.getIntVal("am7p4_d"));
                    }
                }
            }
        }
        Float[] angles = Utils.getRotationsTo(Sync.player().getPositionVector().addVector(0, 1,0), startingPos.addVector(-1-2*line, -2*row, 0),
                new float[]{Minecraft.getMinecraft().thePlayer.rotationYaw, Minecraft.getMinecraft().thePlayer.rotationPitch});
        angles[1]-=1.5f;
        angles[0]-=0.5f;

        if (last != null) SmoothAimControl.set(angles, 3, 23, Index.MAIN_CFG.getDoubleVal("am7p4_s"));
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled() || !Utils.posInDim(Sync.playerPosAsBlockPos(), DIM)) return;
        Esp.renderTextInWorld("\u00A76PHASE", startingPos.xCoord+4, startingPos.yCoord+2, startingPos.zCoord, 0xFFFFFF, 1.6d, e.partialTicks);
        Esp.renderTextInWorld("\u00A7a"+count, startingPos.xCoord+4, startingPos.yCoord, startingPos.zCoord, 0xFFFFFF, 2d, e.partialTicks);
    }


    @Override
    public String id() {
        return "am7p4";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Auto P4 dev";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("am7p4_d", "Delay ticks", ValType.NUMBER, "8"));
        list.add(new SetsData<>("am7p4_s", "Speed", ValType.DOUBLE_NUMBER, "14"));
        list.add(new SetsData<>("am7p4_warp", "Warp after", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("am7p4_warp_d", "Warp after delay[ticks]", ValType.NUMBER, "18"));
        list.add(new SetsData<>("am7p4_inv", "Inv control", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("am7p4_inv_s", "Inv control del[ms]", ValType.NUMBER, "80"));
        list.add(new SetsData<>("am7p4_inv_t", "Rod swap time[ms]", ValType.NUMBER, "700"));
        return list;
    }

    @Override
    public String description() {
        return "Auto p4";
    }
}
