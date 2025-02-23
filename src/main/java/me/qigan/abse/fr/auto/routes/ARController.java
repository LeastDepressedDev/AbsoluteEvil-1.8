package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.events.RoomChangedEvent;
import me.qigan.abse.fr.auto.routes.elems.ARWait;
import me.qigan.abse.fr.auto.routes.elems.ARWalk;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.gui.overlay.GuiNotifier;
import me.qigan.abse.mapping.MappingUtils;
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
    public static String URL;

    public ARoute currentARoute = null;
    public List<ARoute> loadedRoutes = new ArrayList<>();

    //Not the most optimized shit but we dont really need an optimisation here
    public List<AddressedData<ARoute.Referer, JSONObject>> existingRoutes = new ArrayList<>();

    public ARController() {
        URL = Loader.instance().getConfigDir() + "/abse/routes";
        File file = new File(URL);
        if (!file.exists()) file.mkdirs();
        System.out.println(URL);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File mf : files) {
                if (!mf.getName().endsWith(".json")) continue;
                try {
                    //Loading
                    JSONObject obj = new JSONObject(new Scanner(mf).useDelimiter("\\Z").next());
                    existingRoutes.add(new AddressedData<>(ARoute.Referer.valueOf(obj.getString("ref")), obj));
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

    public void loadRoute(JSONObject obj, Object... adjData) {
        JSONObject pos = obj.getJSONObject("start");
        ARoute.Referer referer = ARoute.Referer.valueOf(obj.getString("ref"));
        BlockPos partPos = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        int ref_id = obj.getInt("ref_id");
        if (referer == ARoute.Referer.DUNGEON) {
            Room room = (Room) adjData[0];
            if (room == null || room.id != ref_id) return;
            partPos = ((Room) adjData[0]).transformInnerCoordinate(partPos);
        }
        ARoute route = new ARoute(referer, ref_id, partPos);
        route.name = obj.getString("name");
        route.author = obj.getString("author");
        int length = obj.getInt("length");
        obj = obj.getJSONObject("script");
        for (int i = 0; i < length; i++) {
            JSONObject jsonEle = obj.getJSONObject(Integer.toString(i));
            JSONObject part = jsonEle.getJSONObject("pos");
            BlockPos semiPos = new BlockPos(part.getInt("x"), part.getInt("y"), part.getInt("z"));
            if (route.referer == ARoute.Referer.DUNGEON) {
                semiPos = ((Room) adjData[0]).transformInnerCoordinate(semiPos);
            }
            switch (jsonEle.getString("type")) {
                case "wait":
                {
                    route.add(new ARWait(semiPos, jsonEle.getLong("time")));
                }
                break;
                case "walk":
                {
                    JSONObject to = jsonEle.getJSONObject("to");
                    Vec3 target = new Vec3(to.getDouble("x"), Sync.player().posY, to.getDouble("z"));
                    if (referer == ARoute.Referer.DUNGEON) {
                        target = ((Room) adjData[0]).transformInnerCoordinate(target);
                    }
                    route.add(new ARWalk(semiPos, target, jsonEle.getBoolean("jump"), jsonEle.getBoolean("sprint"),
                            jsonEle.getBoolean("instant") ? 0 : jsonEle.getDouble("rot_speed"))
                    );
                }
                break;
            }
        }
        loadedRoutes.add(route);
    }

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        new Thread(() -> {
            try {
                Thread.sleep(700);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            recallRoutes();
        }).start();
    }

    @SubscribeEvent
    void onRoomChange(RoomChangedEvent e) {
        recallRoutes();
    }

    public void recallRoutes() {
        loadedRoutes.clear();
        Room droom = Index.MAPPING_CONTROLLER.getPlayerRoom();
        for (AddressedData<ARoute.Referer, JSONObject> part : existingRoutes) {
            if (part.getNamespace() == ARoute.Referer.GENERAL) {
                this.loadRoute(part.getObject());
            }
            if (part.getNamespace() == ARoute.Referer.DUNGEON && droom != null) {
                this.loadRoute(
                        part.getObject(),
                        Index.MAPPING_CONTROLLER.getPlayerRoom()
                );
            }
            if (part.getNamespace() == ARoute.Referer.FLOOR) {
                //TODO: Add floor implementation
            }
        }
    }

    public void pause() {
        this.inRoute = false;
        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Index.PLAYER_CONTROLLER.globalToggle = false;
        }).start();

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
        GuiNotifier.call("Done", 10, true, 0x00FF22);
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
