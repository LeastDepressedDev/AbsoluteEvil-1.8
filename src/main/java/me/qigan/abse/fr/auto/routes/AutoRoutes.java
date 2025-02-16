package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class AutoRoutes extends Module {

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || !isEnabled() || e.phase == TickEvent.Phase.END) return;
        if (Index.KEY_MANAGER.get("ar_mod_kb").isPressed()) {
            //TODO: Enter ar logic
        }
    }

    @Override
    public String id() {
        return "ar_mod";
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("ar_mod_kb", "Route enter keybind", ValType.KEYBINDING, -100));
        return list;
    }

    @Override
    public String description() {
        return "Dungeon auto routes... Maybe one day we will get full auto dungeon!\n Who knows, who knows...";
    }
}
