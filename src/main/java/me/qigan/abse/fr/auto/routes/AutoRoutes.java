package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@DangerousModule
public class AutoRoutes extends Module {

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || !isEnabled() || e.phase == TickEvent.Phase.END) return;
        if (Index.KEY_MANAGER.get("ar_mod_kb").isPressed() && !Index.AR_CONTROLLER.inRoute) {
            //TODO: Enter ar logic
        }
        if (Index.KEY_MANAGER.get("global_space").isPressed() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            Index.AR_CONTROLLER.stop();
            return;
        }
        if (Index.KEY_MANAGER.get("global_space").isPressed()) {
            if (Index.AR_CONTROLLER.inRoute) Index.AR_CONTROLLER.pause();
            else Index.AR_CONTROLLER.resume();
        }
    }

    @Override
    public String id() {
        return "ar_mod";
    }

    @Override
    public String fname() {
        return "Auto routes";
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("ar_mod_kb", "Route enter keybind", ValType.KEYBINDING, -100));
        list.add(new SetsData<>("ar_wait", "Force wait between actions", ValType.NUMBER, "300"));
        list.add(new SetsData<>("ar_phantom", "Use phantom rotation[for legit]", ValType.BOOLEAN, "false"));
        return list;
    }

    @Override
    public String description() {
        return "Dungeon auto routes... Maybe one day we will get full auto dungeon!\n Who knows, who knows...";
    }
}
