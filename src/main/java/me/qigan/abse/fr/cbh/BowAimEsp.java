package me.qigan.abse.fr.cbh;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BowAimEsp extends Module {

    @SubscribeEvent
    void rend(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null || Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() != Items.bow) return;
        for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (ent instanceof EntityPlayer || Debug.GENERAL) {
                if (ent.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) continue;
                double dist = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(ent);
                if (dist < 114) {
                    double dl = Index.MAIN_CFG.getDoubleVal("baimesp_dist");
                    double sz = 0.7 + dist / Math.sqrt(4*dl);
                    double fy = ent.posY + 1 + (Math.pow(dist, 2) / dl)/* + (Minecraft.getMinecraft().thePlayer.posY - ent.posY)*/;
                    Color col = new Color(Index.MAIN_CFG.getIntVal("baimesp_col"));
                    col = new Color(col.getRed(), col.getGreen(), col.getBlue(), Math.min(Index.MAIN_CFG.getIntVal("baimesp_a"), 255));
                    Esp.autoBox3D(ent.posX, fy, ent.posZ, sz, sz, col, 2, true);
                } else {
                    Esp.renderTextInWorld("\u00A7l\u00A7cOut of range!", ent.posX, ent.posY + 4.5, ent.posZ, 0xFFFFFF, e.partialTicks);
                }
            }
        }
    }

    @Override
    public String id() {
        return "baimesp";
    }

    @Override
    public Specification category() {
        return Specification.COMBAT;
    }

    @Override
    public String fname() {
        return "Bow aim esp";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("baimesp_dist", "Distance impact", ValType.DOUBLE_NUMBER, "280"));
        list.add(new SetsData<>("baimesp_col", "Color[int]", ValType.NUMBER, Integer.toString(0xFF0000)));
        list.add(new SetsData<>("baimesp_a", "Alpha[0; 255]", ValType.NUMBER, "255"));
        return list;
    }

    @Override
    public String description() {
        return "Show you where to shoot";
    }
}
