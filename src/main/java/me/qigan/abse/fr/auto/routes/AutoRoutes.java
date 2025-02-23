package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.auto.routes.elems.ARElement;
import me.qigan.abse.mapping.routing.RouteUpdater;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@DangerousModule
public class AutoRoutes extends Module {

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || !isEnabled() || e.phase == TickEvent.Phase.END) return;
        boolean pressedState = Index.KEY_MANAGER.get("global_space").isPressed();
        if (Index.KEY_MANAGER.get("ar_mod_kb").isPressed() && !Index.AR_CONTROLLER.inRoute) {
            for (ARoute route : Index.AR_CONTROLLER.loadedRoutes) {
                if (Utils.compare(Sync.playerPosAsBlockPos(), route.startingPos)) {
                    Index.AR_CONTROLLER.enterRoute(route);
                    return;
                }
            }
        }
        if (pressedState && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            Index.AR_CONTROLLER.stop();
            return;
        }
        if (pressedState) {
            if (Index.AR_CONTROLLER.inRoute) Index.AR_CONTROLLER.pause();
            else Index.AR_CONTROLLER.resume();
        }
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        if (Index.AR_CONTROLLER == null || Index.AR_CONTROLLER.loadedRoutes == null) return;
        List<ARoute> routes = new ArrayList<>(Index.AR_CONTROLLER.loadedRoutes);
        for (ARoute route : routes) {
            List<BlockPos> path = new ArrayList<>();
            for (ARElement ele : route.elems) {
                path.add(ele.pos);
            }
            Esp.autoFilledBox3D(route.startingPos, Color.green, 1.3f, true);
            RouteUpdater.drawPath(path, 1.4f, Color.cyan);
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

    public static void reloadRoads() {
        MinecraftForge.EVENT_BUS.unregister(Index.AR_CONTROLLER);
        Index.AR_CONTROLLER = new ARController();
        MinecraftForge.EVENT_BUS.register(Index.AR_CONTROLLER);
        Sync.player().addChatMessage(new ChatComponentText(String.format("\u00A7aLoaded %d routes", Index.AR_CONTROLLER.existingRoutes.size())));
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("ar_mod_kb", "Route enter keybind", ValType.KEYBINDING, -100));
        list.add(new SetsData<>("ar_wait", "Force wait between actions", ValType.NUMBER, "300"));
        list.add(new SetsData<>("ar_phantom", "Use phantom rotation[for legit]", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("ar_reload", "Reload routes", ValType.BUTTON, (Runnable) AutoRoutes::reloadRoads));
        list.add(new SetsData<>("ar_recall", "Recall routes", ValType.BUTTON, (Runnable) () -> Index.AR_CONTROLLER.recallRoutes()));
        return list;
    }

    @Override
    public String description() {
        return "Dungeon auto routes... Maybe one day we will get full auto dungeon!\n Who knows, who knows...";
    }
}
