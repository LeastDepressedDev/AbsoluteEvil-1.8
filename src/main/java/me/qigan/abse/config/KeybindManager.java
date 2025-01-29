package me.qigan.abse.config;

import me.qigan.abse.Holder;
import me.qigan.abse.crp.AutoDisable;
import me.qigan.abse.crp.EDLogic;
import me.qigan.abse.crp.EnabledByDefault;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class KeybindManager {

    public Map<String, WKeybind> binds = new HashMap<>();

    public static final List<WKeybind> unsortedBinds = new ArrayList<>(Arrays.asList(
            new WKeybind("unlimitedRange", Keyboard.KEY_V).setDisplayName("Unlimited render range."),
            new WKeybind("ghostBlocks", Keyboard.KEY_F).setDisplayName("Ghost block kaybind."),
            new WKeybind("ghostBlocksReset", Keyboard.KEY_Z).setDisplayName("Ghost block reset kaybind."),
            new WKeybind("legGhostBlocks", Keyboard.KEY_C).setDisplayName("Legacy ghost block"),
            new WKeybind("tempGhostBlocks", Keyboard.KEY_NONE).setDisplayName("Temporary ghost block."),
            new WKeybind("ghostChest", Keyboard.KEY_NONE).setDisplayName("Ghost chest."),
            new WKeybind("aimBreak", Keyboard.KEY_NONE).setDisplayName("Aim break button."),
            new WKeybind("aimLock", Keyboard.KEY_G).setDisplayName("Aim lock."),
            new WKeybind("airStrafe", Keyboard.KEY_NONE).setDisplayName("Air strafe"),
            new WKeybind("debuffKey", Keyboard.KEY_NONE).setDisplayName("Debuff key"),
            new WKeybind("ssKey", Keyboard.KEY_NONE).setDisplayName("Auto SS"),
            new WKeybind("leapShortcut", Keyboard.KEY_Y).setDisplayName("Leap shortcut")
    ));

    public final List<WKeybind> sortedBinds = new ArrayList<>();

    public final AddressedWriter writer;

    public KeybindManager() {
        //init writer class
        this.writer = new AddressedWriter(Loader.instance().getConfigDir() + "/keys_abse.cfg");

        //clr_mem
        binds.clear();
        sortedBinds.clear();

        //Not a very optimised shit, but hopefully it works. At least...
        for (Module mdl: Holder.MRL) {
            if (!writer.contains(mdl.id())) {
                this.writer.set(mdl.id(), Integer.toString(Keyboard.KEY_NONE));
            }
        }

        for(AddressedData<String, String> w: writer.get()) {
            WKeybind bind = new WKeybind(w.getNamespace(), Integer.parseInt(w.getObject())).setDisplayName(w.getNamespace());
            binds.put(w.getNamespace(), bind);
            sortedBinds.add(bind);
        }

        for (WKeybind keys : unsortedBinds) {
            if (!binds.containsKey(keys.kbName)) {
                binds.put(keys.kbName, keys);
                sortedBinds.add(keys);
            }
        }
    }

    @SubscribeEvent
    void handleKeys(TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) return;
        for (WKeybind key : sortedBinds) {
            key.update(Keyboard.isKeyDown(key.keyCode));
        }
    }

    public WKeybind get(String id) {
        return binds.get(id);
    }
}
