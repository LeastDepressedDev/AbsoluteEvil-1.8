package me.qigan.abse.fr.macro;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.ClickSimTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Macro
public class LegitGhostBlocksMacro extends Module {

    public static boolean cdm = true;

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
        if (cdm && (b || Index.MAIN_CFG.getBoolVal("lgmacro_ovr") &&
                Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown())) {
            int i = findPicake();
            if (i == -1) return;
            int p = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
            cdm = false;
            new Thread(() -> {
                try {
                    Random rand = new Random();
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode(), true);
                    KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode());
                    Thread.sleep(5+Math.abs(rand.nextInt())%9);
                    if (b) {
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), true);
                        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode());
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode(), false);
                    }
                    Thread.sleep(15);
                    if (b) {
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
                    }
                    Thread.sleep(70);
                    Minecraft.getMinecraft().thePlayer.inventory.currentItem = p;
                    cdm=true;
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        }
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
        list.add(new SetsData<>("lgmKey", "Activate on key", ValType.KEYBINDING, Keyboard.KEY_F));
        list.add(new SetsData<>("lgmacro_ovr", "Override non pickaxe lcm", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("lgmacro_fix", "Fix", ValType.BUTTON, (Runnable) ()->{cdm=true;}));
        return list;
    }

    @Override
    public String description() {
        return "One key legit ghost blocks placement";
    }
}
