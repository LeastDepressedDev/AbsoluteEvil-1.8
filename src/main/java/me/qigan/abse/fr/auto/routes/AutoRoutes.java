package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.auto.routes.elems.ARClick;
import me.qigan.abse.fr.auto.routes.elems.ARENull;
import me.qigan.abse.fr.auto.routes.elems.ARElement;
import me.qigan.abse.mapping.routing.RouteUpdater;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DangerousModule
public class AutoRoutes extends Module {

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || !isEnabled() || e.phase == TickEvent.Phase.END) return;
        boolean pressedState = Index.KEY_MANAGER.get("global_space").isPressed();
        if (Index.KEY_MANAGER.get("ar_mod_kb").isPressed() && !Index.AR_CONTROLLER.inRoute) {
            List<ARoute> routes = new ArrayList<>(Index.AR_CONTROLLER.loadedRoutes);
            if (ARRCmd.route != null) routes.add(ARRCmd.route);
            for (ARoute route : routes) {
                if (Sync.player().getPositionVector().distanceTo(route.startingPos) <= 0.6) {
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
        if (ARRCmd.route != null) routes.add(ARRCmd.route);
        for (ARoute route : routes) {
            List<Vec3> path = new ArrayList<>();
            for (ARElement ele : route.elems) {
                if (ele instanceof ARENull) continue;
                if (ele instanceof ARClick) {
                    Esp.autoBox3D(((ARClick) ele).clickPos, Color.yellow, 3f, true);
                    continue;
                }
                Esp.drawPointInWorldCircle(ele.startPos, 0.7, 16, 1.7f, Color.cyan);
                path.add(ele.startPos);
                if (!Utils.compare(ele.startPos, ele.endPos)) {
                    Esp.drawPointInWorldCircle(ele.endPos, 0.7, 16, 1.7f, Color.cyan);
                    path.add(ele.endPos);
                }
            }
            Esp.autoFilledBox3D(route.startingPos, new Color(0, 255, 0, 85), 1.6f, true);
            RouteUpdater.drawPath(path, 1.4f, Color.cyan);
        }
    }

    @SubscribeEvent
    void renderGameOverlay(RenderGameOverlayEvent.Text e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        if (Index.AR_CONTROLLER.inRoute && Index.AR_CONTROLLER.currentARoute != null) {

            GlStateManager.pushMatrix();
            GL11.glScaled(2, 2, 2);
            Esp.drawCenteredString("In route", e.resolution.getScaledWidth()/4, e.resolution.getScaledHeight()/2-150, Color.green, S2Dtype.CORNERED);
            GlStateManager.popMatrix();

            ARoute ctr = Index.AR_CONTROLLER.currentARoute;
            Point pt = Index.POS_CFG.calc("ar_loc");
            List<String> texts = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                texts.add(0, ctr.step+i<ctr.elems.size() ? ctr.elems.get(ctr.step+i).elementString() : "");
            }
            texts.add(0, "\u00A7aRoute: ");
            Esp.drawAllignedTextList(texts, pt.x, pt.y, false, e.resolution, S2Dtype.CORNERED);
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
        list.add(new SetsData<>("ar_rotspeed", "Rotation speed[<=0->instnat]", ValType.DOUBLE_NUMBER, "16.3"));
        list.add(new SetsData<>("ar_reload", "Reload routes", ValType.BUTTON, (Runnable) AutoRoutes::reloadRoads));
        list.add(new SetsData<>("ar_recall", "Recall routes", ValType.BUTTON, (Runnable) () -> Index.AR_CONTROLLER.recallRoutes()));
        return list;
    }

    @Override
    public String description() {
        return "Dungeon auto routes... Maybe one day we will get full auto dungeon!\n Who knows, who knows...";
    }
}
