package me.qigan.abse.fr.macro;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.fr.exc.Alert;
import me.qigan.abse.fr.exc.ClickSimTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Macro
public class LegitGhostBlocksMacro extends Module {

    public static boolean cdm = true;
    // Fuck the developer who forced me to do this very VERY shitty move!
    public static final int MOVE_SLOTS_CONST = +36;

    private int findPicake() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) continue;
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.diamond_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.golden_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.iron_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.stone_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.wooden_pickaxe) return i;
        }
        return -1;
    }


    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        boolean b = Index.KEY_MANAGER.get("lgmKey").isPressed();
        boolean x = Index.KEY_MANAGER.get("lgmGpKey").isPressed();
        int i = findPicake();
        if (i == -1) return;
        int p = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
        if (cdm && b) {
            cdm = false;
            new Thread(() -> {
                try {
                    Random rand = new Random();
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode(), true);
                    KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode());
                    Thread.sleep(5+Math.abs(rand.nextInt())%9);
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), true);
                        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode());
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode(), false);
                    Thread.sleep(15);
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
                    Thread.sleep(70);
                    Minecraft.getMinecraft().thePlayer.inventory.currentItem = p;
                    cdm=true;
                } catch (InterruptedException ex) {
                    reportBroken();
                    throw new RuntimeException(ex);
                }
            }).start();
        }
        if (cdm && x) {
            cdm = false;
            new Thread(() -> {
                try {
                    Random rand = new Random();
                    long time = System.currentTimeMillis();
                    if (Minecraft.getMinecraft().currentScreen == null) {
                        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode());
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode(), true);
                        Thread.sleep(50);
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode(), false);
                    }
                    while (Minecraft.getMinecraft().currentScreen == null) {
                        if (System.currentTimeMillis()-time>=3000) {
                            cdm = true;
                            return;
                        }
                    }
                    double a = Index.MAIN_CFG.getDoubleVal("lgmacro_speed");
                    Thread.sleep((int) (90/a));
                    if (!(Minecraft.getMinecraft().currentScreen instanceof GuiInventory)) {
                        cdm = true;
                        System.out.println("Suck some dicks!!!");
                        return;
                    }
                    ContainerPlayer cont = (ContainerPlayer) ((GuiInventory) Minecraft.getMinecraft().currentScreen).inventorySlots;

                    Thread.sleep(60);
                    Minecraft.getMinecraft().currentScreen.mc.playerController.windowClick(cont.windowId, p+MOVE_SLOTS_CONST,
                            i, 2, Minecraft.getMinecraft().thePlayer);
                    Thread.sleep((int) (120/a));
                    Minecraft.getMinecraft().currentScreen.mc.playerController.windowClick(cont.windowId, p+MOVE_SLOTS_CONST,
                            p, 2, Minecraft.getMinecraft().thePlayer);
                    Thread.sleep((int) (120/a));
                    Minecraft.getMinecraft().currentScreen.mc.playerController.windowClick(cont.windowId, p+MOVE_SLOTS_CONST,
                            i, 2, Minecraft.getMinecraft().thePlayer);
                    Thread.sleep((int) (160/a));
                    Minecraft.getMinecraft().thePlayer.closeScreen();
                    cdm=true;
                } catch (Exception ex) {
                    reportBroken();
                    throw new RuntimeException(ex);
                }
            }).start();
        }
    }

//    @SubscribeEvent
//    void packet(PacketEvent.SendEvent e) {
//        if (e.packet instanceof C0EPacketClickWindow) {
//            C0EPacketClickWindow act = (C0EPacketClickWindow) e.packet;
//            System.out.println(String.format("%s,%s,%s,%s", act.getWindowId(), act.getSlotId(), act.getUsedButton(), act.getMode()));
//        }
//    }

    public static void reportBroken() {
        Alert.call("Oopsie!", 30, 1);
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00A7a Something broken while trying to simulate legit actions. " +
                "If you want, if can send me log. To fix function -> press \"fix\" button in setting of module."));
    }

    @Override
    public String id() {
        return "lgbmacro";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("lgmKey", "Simulate swap", ValType.KEYBINDING, Keyboard.KEY_F));
        list.add(new SetsData<>("lgmGpKey", "Create ghost pick", ValType.KEYBINDING, Keyboard.KEY_B));
        list.add(new SetsData<>("lgmacro_speed", "Speed modifier[The higher the faster]", ValType.DOUBLE_NUMBER, "1"));
        list.add(new SetsData<>("lgmacro_fix", "Fix", ValType.BUTTON, (Runnable) ()->{cdm=true;}));
        return list;
    }

    @Override
    public String fname() {
        return "Legit ghost blocks";
    }

    @Override
    public String description() {
        return "Auto pick swapping and ghost pick. GL admins to detect!";
    }
}
