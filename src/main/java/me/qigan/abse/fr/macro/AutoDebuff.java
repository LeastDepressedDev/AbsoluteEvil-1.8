package me.qigan.abse.fr.macro;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.config.WKeybind;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.crp.Module;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@Macro
public class AutoDebuff extends Module {

    private boolean use = false;
    private int SPRAY_SLOT = -1, SW_SLOT = -1;

    private static int findSlot(String str) {
        for (int i = 0; i < 9; i++) {
            if (Utils.getSbData(Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i))
                    .getString("id").equalsIgnoreCase(str)) return i;
        }
        return -1;
    }



    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled()) return;
        WKeybind bind = Index.KEY_MANAGER.get("debuffKey");
        if (bind.isPressed()) {
            SPRAY_SLOT = findSlot("ICE_SPRAY_WAND");
            SW_SLOT = findSlot("SOUL_WHIP");
            if (SPRAY_SLOT == -1 || SW_SLOT == -1) return;
            new Thread(() -> {
                try {
                    Utils.selectHotbarSlot(SPRAY_SLOT);
                    Thread.sleep(80);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), true);
                    use = true;
                    Thread.sleep(150);
                    Utils.selectHotbarSlot(SW_SLOT);
                    Thread.sleep(70);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        }
        if (!bind.isDown() && use) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
            SPRAY_SLOT = -1;
            SW_SLOT = -1;
            use = false;
        }
    }

    @Override
    public String id() {
        return "atm7db";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("debuffKey", "Debuff key", ValType.KEYBINDING, Keyboard.KEY_NONE));
        return super.sets();
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Auto debuff [M7]";
    }

    @Override
    public String description() {
        return "Automatically debuff dragons on key";
    }
}
