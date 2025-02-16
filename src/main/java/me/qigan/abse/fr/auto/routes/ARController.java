package me.qigan.abse.fr.auto.routes;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;

public class ARController {
    public boolean inRoute = false;
    public static String URL = "abse/routes";

    public ARoute currentARoute = null;

    public ARController() {
        URL = Loader.instance().getConfigDir() + "/" + URL;
        int sc = 0;
        File file = new File(URL);
        if (!file.exists()) file.mkdirs();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File mf : files) {
                if (!mf.getName().endsWith(".json")) continue;

            }
        }
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || e.phase == TickEvent.Phase.END) return;
        if (!inRoute) return;

        if (currentARoute != null) {
            if (Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(currentARoute.stepElement().pos) > 80*80) {
                interrupt(currentARoute);
            }
            if (currentARoute.update(e)) finish(currentARoute);
        }
    }

    public void pause() {
        this.inRoute = false;
    }

    public void resume() {
        this.inRoute = this.currentARoute != null;
    }

    public void interrupt(ARoute route) {
        stop();
        //Route interruption logic
    }

    public void finish(ARoute route) {
        stop();
        //Some finish route logic
    }

    public void stop() {
        this.pause();
        this.currentARoute = null;
    }

    public void enterRoute(ARoute route) {
        this.currentARoute = route;
        resume();
    }
}
