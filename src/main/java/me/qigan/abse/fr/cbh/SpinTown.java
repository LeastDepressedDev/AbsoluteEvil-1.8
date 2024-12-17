package me.qigan.abse.fr.cbh;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.BallisticCalculator;
import me.qigan.abse.crp.EDLogic;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.Vec3List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SpinTown extends Module implements EDLogic {

    public static List<AddressedData<Entity, Vec3List>> tracks = new ArrayList<>();
    public static Set<Entity> exists = new HashSet<>();

    public static Map<Entity, Vec3> prediction = new HashMap<>();

    @SubscribeEvent
    void onTick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null || e.phase == TickEvent.Phase.END) return;
        double imp = Index.MAIN_CFG.getDoubleVal("sptnd_imp");
        for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (ent instanceof EntityPlayer && ent.getUniqueID() != Minecraft.getMinecraft().thePlayer.getUniqueID()
                    || Debug.GENERAL && ent instanceof EntityCreeper) {
                if (!exists.contains(ent)) {
                    exists.add(ent);
                    tracks.add(new AddressedData<>(ent, new Vec3List()));
                }
            }
        }
        Set<AddressedData<Entity, Vec3List>> toRm = new HashSet<>();
        for (int i = 0; i < tracks.size(); i++) {
            AddressedData<Entity, Vec3List> pt = tracks.get(i);
            Entity ent = pt.getNamespace();
            if (!ent.isEntityAlive()) {
                toRm.add(pt);
                exists.remove(ent);
                prediction.remove(ent);
                continue;
            }
            Vector3d delta = new Vector3d();
            delta.x = ent.posX-ent.lastTickPosX;
            delta.y = ent.posY-ent.lastTickPosY;
            delta.z = ent.posZ-ent.lastTickPosZ;
            pt.getObject().add(delta);
            if (pt.getObject().size() > 20) {
                pt.getObject().remove(0);
                Double dt = BallisticCalculator.solveForTWPDC(pt, 1d);
                if (dt != null) {
                    dt += imp;
                    Vec3 pred = new Vec3(ent.posX, ent.posY, ent.posZ)
                            .addVector(pt.getObject().vecSum.x/20*dt, pt.getObject().vecSum.y/20*dt, pt.getObject().vecSum.z/20*dt);
                    prediction.put(ent, pred);
                } else prediction.remove(pt.getNamespace());
            }
        }
        for (AddressedData<Entity, Vec3List> ints : toRm) {
            tracks.remove(ints);
        }
    }

//    @SubscribeEvent
//    void render(RenderWorldLastEvent e) {
//        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
//        List<AddressedData<Entity, Vec3List>> entries = new ArrayList<>(tracks);
//        for (AddressedData<Entity, Vec3List> pt : entries) {
//            Vector3d dispose = pt.getObject().vecSum;
//            Vec3 coord1 = new Vec3(dispose.x+pt.getNamespace().posX, dispose.y+pt.getNamespace().posY, dispose.z+pt.getNamespace().posZ);
//            //Esp.autoBox3D(coord1.xCoord, coord1.yCoord, coord1.zCoord, 0.5, 0.5, Color.CYAN, 2f, true);
//        }
//    }

    @SubscribeEvent
    void worldLoad(WorldEvent.Load e) {
        reset();
    }

    @Override
    public String id() {
        return "sptnd";
    }

    @Override
    public Specification category() {
        return Specification.COMBAT;
    }

    @Override
    public String description() {
        return "Have you ever wanted to play like spintown?\nWell! Now you can!";
    }

    @Override
    public String fname() {
        return "Spintown mode";
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        reset();
    }

    private static void reset() {
        tracks.clear();
        exists.clear();
        prediction.clear();
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("sptnd_imp", "Ping impact", ValType.NUMBER, "3"));
        return list;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && Index.MAIN_CFG.getBoolVal("baimesp");
    }
}
