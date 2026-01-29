package me.qigan.abse.fr.bedwars;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.Alert;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FireballDetector extends Module {

    public static int tickCd = 0;
    public static boolean glRend = false;

    public static List<EntityFireball> scan = new ArrayList<>();

    @SubscribeEvent
    void spawn(EntityJoinWorldEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().thePlayer == null) return;
        if (e.entity instanceof EntityFireball) {
            if (Minecraft.getMinecraft().thePlayer.getDistanceToEntity(e.entity) > 5) {
                scan.add((EntityFireball) e.entity);
            }
        }
    }

    @SubscribeEvent
    void death(LivingDeathEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().thePlayer == null) return;
        if (e.entity.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) scan.clear();
    }

    @SubscribeEvent
    void detect(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null)
            return;
        for (EntityFireball ent : scan) {
            if (!ent.isEntityAlive()) {
                scan.remove(ent);
                return;
            }
            double moveX = ent.posX - ent.prevPosX;
            double moveY = ent.posY - ent.prevPosY;
            double moveZ = ent.posZ - ent.prevPosZ;

            for (int i = 0; i <= 100; i++) {
                if (Minecraft.getMinecraft().thePlayer.getDistance(ent.posX + moveX * i, ent.posY + moveY * i, ent.posZ + moveZ * i) < 7) {
                    Alert.call("\u00A7cFireball incoming", 2, 2);
                }
            }
        }
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        boolean rendStr = false;
        for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (ent instanceof EntityFireball) {
                double moveX = ent.posX - ent.prevPosX;
                double moveY = ent.posY - ent.prevPosY;
                double moveZ = ent.posZ - ent.prevPosZ;

                Esp.drawTracer(ent.posX, ent.posY, ent.posZ, ent.posX + moveX * 100, ent.posY + moveY * 100, ent.posZ + moveZ * 100, new Color(0xFF0000), 1f);
            } else if (ent instanceof EntityPlayer) {
                EntityPlayer cur = (EntityPlayer) ent;
                if (ent.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID() ||
                     !Index.MAIN_CFG.getBoolVal("fbd_team_track") && BWTeamTracker.team.containsKey(cur)) continue;
                if (cur.getHeldItem() != null && cur.getHeldItem().getItem() == Items.fire_charge) {
                    if (Index.MAIN_CFG.getBoolVal("fbd_degree")) {
                        Float[] angles = Utils.getRotationsTo(cur, Minecraft.getMinecraft().thePlayer);
                        if (Math.abs(angles[0] - cur.rotationYaw) < Index.MAIN_CFG.getDoubleVal("fbd_degree_yaw") &&
                                Math.abs(angles[1] - cur.rotationPitch) < Index.MAIN_CFG.getDoubleVal("fbd_degree_pitch"))
                            rendStr = true;
                    } else rendStr = true;
                }
            }
        }
        glRend = rendStr;
    }

    @SubscribeEvent
    void renderOVR(RenderGameOverlayEvent.Text e) {
        if(!isEnabled() || !glRend) return;
        Point loc = Index.POS_CFG.calc("fbd_display");
        Esp.drawOverlayString("Fireball warning!", loc.x, loc.y, 0xFF0000, S2Dtype.CORNERED);
    }

    @SubscribeEvent
    void worldLoad(WorldEvent.Load e) {
        if (!isEnabled()) return;
        scan.clear();
    }

    @Override
    public String id() {
        return "fanonc";
    }

    @Override
    public Specification category() {
        return Specification.BEDWARS;
    }

    @Override
    public String fname() {
        return "Fireball detector";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("fbd_team_track", "Track team", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("fbd_degree", "Apply angular check", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("fbd_degree_yaw", "Yaw interval", ValType.DOUBLE_NUMBER, "45"));
        list.add(new SetsData<>("fbd_degree_pitch", "Pitch interval", ValType.DOUBLE_NUMBER, "20"));
        return list;
    }

    @Override
    public String description() {
        return "Now you know when fireball is going for you";
    }
}
