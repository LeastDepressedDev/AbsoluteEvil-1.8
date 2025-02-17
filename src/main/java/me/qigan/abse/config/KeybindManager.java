package me.qigan.abse.config;

import me.qigan.abse.Holder;
import me.qigan.abse.Index;
import me.qigan.abse.crp.AutoDisable;
import me.qigan.abse.crp.EDLogic;
import me.qigan.abse.crp.EnabledByDefault;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;

public class KeybindManager {

    public Map<String, WKeybind> binds = new HashMap<>();

    public static final List<WKeybind> unsortedBinds = new ArrayList<>(Arrays.asList(
        new WKeybind("global_space", Keyboard.KEY_SPACE)
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
            for (SetsData<?> dat : mdl.sets()) {
                if (dat.dataType == ValType.KEYBINDING) {
                    if (!writer.contains(dat.setId)) {
                        this.writer.set(dat.setId, ((Integer) dat.defVal).toString());
                    }
                }
            }
        }

        for (WKeybind wkb : unsortedBinds) {
            if (!writer.contains(wkb.kbName)) {
                this.writer.set(wkb.kbName, Integer.toString(wkb.keyCode));
            }
        }

        for(AddressedData<String, String> w: writer.get()) {
            WKeybind bind = new WKeybind(w.getNamespace(), Integer.parseInt(w.getObject()));
            binds.put(w.getNamespace(), bind);
            sortedBinds.add(bind);
        }
    }

    public final void after() {
        for (Module mdl: Holder.MRL) {
            mdl.moduleBind().setExecutor(() -> Index.MAIN_CFG.toggle(mdl.id()));
        }
    }

    @SubscribeEvent
    void handleKeys(TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) return;
        for (WKeybind key : sortedBinds) {
            if (key.keyCode > 0) {
                key.update(Minecraft.getMinecraft().currentScreen == null && Keyboard.isKeyDown(key.keyCode));
            } else {
                key.update(Minecraft.getMinecraft().currentScreen == null && Mouse.isButtonDown(key.keyCode+100));
            }
        }
    }

//    public void update() {
//        writer.set();
//    }

    public WKeybind get(String id) {
        return binds.get(id);
    }

    public void set(String id, int code) {
        this.binds.get(id).keyCode = code;
        this.writer.set(id, Integer.toString(code));
    }
}
