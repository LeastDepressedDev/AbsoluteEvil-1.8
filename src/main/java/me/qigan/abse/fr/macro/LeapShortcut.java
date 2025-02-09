package me.qigan.abse.fr.macro;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.config.WKeybind;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@Macro
public class LeapShortcut extends Module {

    private static int find() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) continue;
            NBTTagCompound tag = Utils.getSbData(Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i));
            if (tag != null && (tag.getString("id").equalsIgnoreCase("SPIRIT_LEAP") ||
                    tag.getString("id").equalsIgnoreCase("INFINITE_SPIRIT_LEAP"))) return i;
        }
        return -1;
    }

    public static void call(boolean ret) {
        double factor = Index.MAIN_CFG.getDoubleVal("leapSC_sf");
        new Thread(() -> {
            try {
                int slot = find();
                if (slot == -1) return;
                int pre = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
                Utils.selectHotbarSlot(slot);
                Thread.sleep((int) (120*factor));
                ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1);
                if (ret) {
                    Thread.sleep((int) (80*factor));
                    Utils.selectHotbarSlot(pre);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }).start();
    }

    @SubscribeEvent
    void rendTick(RenderWorldLastEvent e) {
        if (!isEnabled() || !Sync.inDungeon) return;
        WKeybind bind = Index.KEY_MANAGER.get("leapShortcut");
        if (bind.isPressed()) call(Index.MAIN_CFG.getBoolVal("leapSC_back"));
    }

    @Override
    public String id() {
        return "leapSC";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Leap shortcut";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("leapShortcut", "Leap key", ValType.KEYBINDING, Keyboard.KEY_Y));
        list.add(new SetsData<>("leapSC_back", "Switch item back", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("leapSC_sf", "Actions speed factor", ValType.DOUBLE_NUMBER, "1"));
        return list;
    }

    @Override
    public String description() {
        return "Leaps with one button on keyboard!";
    }
}
