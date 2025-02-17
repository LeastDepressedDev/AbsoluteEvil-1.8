package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.fr.auto.routes.elems.ARWait;
import me.qigan.abse.fr.auto.routes.elems.ARWalk;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.mapping.Room;
import me.qigan.abse.sync.Sync;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ARController {
    public boolean inRoute = false;
    public static String URL = "abse/routes";

    public ARoute currentARoute = null;
    public List<ARoute> loadedRoutes = new ArrayList<>();

    public List<ARoute> existingRoutes = new ArrayList<>();

    public ARController() {
        URL = Loader.instance().getConfigDir() + "/" + URL;
        File file = new File(URL);
        if (!file.exists()) file.mkdirs();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File mf : files) {
                if (!mf.getName().endsWith(".json")) continue;
                try {
                    //Loading
                    JSONObject obj = new JSONObject(new Scanner(mf).useDelimiter("\\Z").next());
                    JSONObject pos = obj.getJSONObject("start");
                    ARoute route = new ARoute(ARoute.Referer.valueOf(obj.getString("ref")), obj.getInt("ref_id"),
                            new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
                    route.name = obj.getString("name");
                    route.author = obj.getString("author");
                    int length = obj.getInt("length");
                    obj = obj.getJSONObject("script");
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonEle = obj.getJSONObject(Integer.toString(i));
                        JSONObject part = jsonEle.getJSONObject("pos");
                        BlockPos semiPos = new BlockPos(part.getInt("x"), part.getInt("y"), part.getInt("z"));
                        switch (jsonEle.getString("type")) {
                            case "wait":
                                {
                                    route.add(new ARWait(semiPos, jsonEle.getLong("time")));
                                }
                                break;
                            case "walk":
                                {
                                    JSONObject to = jsonEle.getJSONObject("to");
                                    route.add(new ARWalk(semiPos, new Vec3(
                                            to.getDouble("x"),
                                            Sync.player().posY,
                                            to.getDouble("z")
                                        ), jsonEle.getBoolean("jump"), jsonEle.getBoolean("sprint"),
                                            jsonEle.getBoolean("instant") ? 0 : jsonEle.getDouble("rot_speed"))
                                    );
                                }
                                break;
                        }
                    }
                    existingRoutes.add(route);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || e.phase == TickEvent.Phase.END) return;

        if (currentARoute != null) {
            Index.PLAYER_CONTROLLER.globalToggle = inRoute;

            if (!inRoute) return;
            if (Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(currentARoute.stepElement().pos) > 80*80) {
                interrupt(currentARoute);
            }

            if (currentARoute.update(e)) {
                finish(currentARoute);
            }
        }
    }

    public void rotate(Float[] target, double speed) {
        if (speed <= 0) {
            //TODO: add instant rotation proc
        } else {
            if (Index.MAIN_CFG.getBoolVal("ar_phantom")) {
                //TODO: Add phantom rotation processor
            } else {
                SmoothAimControl.set(target, 2, 20, speed);
            }
        }
    }

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        loadedRoutes.clear();
        Room droom = Index.MAPPING_CONTROLLER.getPlayerRoom();

        for (ARoute route : existingRoutes) {
            if (route.referer == ARoute.Referer.GENERAL) {
                loadedRoutes.add(route);
            }
            if (route.referer == ARoute.Referer.DUNGEON && droom != null) {
                if (droom.id == route.ref_id) loadedRoutes.add(route);
            }
            if (route.referer == ARoute.Referer.FLOOR) {
                //TODO: Add floor implementation
            }
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
        this.currentARoute.reset();
        resume();
    }
}
