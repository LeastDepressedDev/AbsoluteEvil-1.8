package me.qigan.abse.fr.cbh;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.BallisticCalculator;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BowAimEsp extends Module {

    @SubscribeEvent
    void rend(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null || Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() != Items.bow) return;

        int opac = Index.MAIN_CFG.getIntVal("baimesp_a");
        double dl = Index.MAIN_CFG.getDoubleVal("baimesp_dist");

        for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (ent instanceof EntityPlayer || Debug.GENERAL) {
                if (ent.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) continue;
                double dist = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(ent);
                double dd = Math.sqrt(Math.pow(Minecraft.getMinecraft().thePlayer.posX - ent.posX, 2)
                + Math.pow(Minecraft.getMinecraft().thePlayer.posZ - ent.posZ, 2));
                AddressedData<Float, Double> angle = new AddressedData<>(null, null);
                if (dist < 114) {
                    double sz = 0.7 + dist / Math.sqrt(8*dl);
                    angle = BallisticCalculator.solveForArrowAngle(dd, ent.posY-Minecraft.getMinecraft().thePlayer.posY+ent.height/2, 0.05, 16);
                    if (angle.getNamespace() == null) {
                        Esp.renderTextInWorld("\u00A7l\u00A7cOut of range!", ent.posX, ent.posY + 4.5, ent.posZ, 0xFFFFFF, e.partialTicks);
                        return;
                    }
                    double fy = Math.tan(angle.getNamespace())*dd+Minecraft.getMinecraft().thePlayer.getEyeHeight()+Minecraft.getMinecraft().thePlayer.posY+sz/2;
                    Color col = new Color(Index.MAIN_CFG.getIntVal("baimesp_col"));
                    col = new Color(col.getRed(), col.getGreen(), col.getBlue(), Math.min(opac, 255));

                    Esp.autoBox3D(ent.posX, fy, ent.posZ, sz, sz, col, 2, true);
                    if (SpinTown.prediction.containsKey(ent)) {
                        Vec3 pred = SpinTown.prediction.get(ent);
                        dd = Math.sqrt(Math.pow(Minecraft.getMinecraft().thePlayer.posX - pred.xCoord, 2)
                                + Math.pow(Minecraft.getMinecraft().thePlayer.posZ - pred.zCoord, 2));
                        angle = BallisticCalculator.solveForArrowAngle(dd, pred.yCoord-Minecraft.getMinecraft().thePlayer.posY+ent.height/2, 0.05, 16);
                        if (angle.getNamespace() == null) {return;}
                        fy = Math.tan(angle.getNamespace())*dd+Minecraft.getMinecraft().thePlayer.getEyeHeight()+Minecraft.getMinecraft().thePlayer.posY+sz/2;
                        col = new Color(Color.orange.getRed(), Color.orange.getGreen(), Color.orange.getBlue(), Math.min(opac, 255));

                        Esp.autoBox3D(pred.xCoord, fy, pred.zCoord, sz, sz, col, 2, true);
                    }
                    if (Index.MAIN_CFG.getBoolVal("baimesp_balt") && angle.getObject() != null) {
                        Esp.renderTextInWorld(String.format("%.3f", angle.getObject()/20d) + "s", ent.posX, fy-2, ent.posZ, 0x00FF10, e.partialTicks);
                    }
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
        list.add(new SetsData<>("baimesp_balt", "Render estimated time", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("baimesp_col", "Color[int]", ValType.NUMBER, Integer.toString(0xFF0000)));
        list.add(new SetsData<>("baimesp_a", "Alpha[0; 255]", ValType.NUMBER, "255"));
        return list;
    }

    @Override
    public String description() {
        return "Show you where to shoot";
    }
}
