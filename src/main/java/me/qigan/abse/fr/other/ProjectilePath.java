package me.qigan.abse.fr.other;

import me.qigan.abse.crp.Module;
import me.qigan.abse.vp.Esp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class ProjectilePath extends Module {

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        //TODO: Get Rid Of This Try
        if (!isEnabled()) return;
        try {
            if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null) return;
            if (Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() == Items.ender_pearl) {
                EntityThrowable ent = new EntityEnderPearl(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer);
                double dx = ent.posX, dy = ent.posY, dz = ent.posZ;
                double mdx = ent.motionX, mdy = ent.motionY, mdz = ent.motionZ;
                for (int i = 0; i < 800; i++) {
                    dx += mdx;
                    dy += mdy;
                    dz += mdz;
                    float f2 = 0.99F;
                    float f3 = 0.03F;

                    mdx *= f2;
                    mdy *= f2;
                    mdz *= f2;
                    mdy -= f3;

                    IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(dx, dy, dz));
                    if (state != null && state.getBlock() != null && state.getBlock() != Blocks.air) {
                        //Esp.autoBeaconBeam(dx, dy, dz, Color.cyan.getRGB(), 0.5f, e.partialTicks);
                        Esp.autoBox3D(dx, dy, dz, 0.5, 0.5, Color.cyan, 5f, true);
                        break;
                    }
                }
            } else if (Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() == Items.bow) {
                EntityArrow ent = new EntityArrow(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer, 2F);
                double dx = ent.posX, dy = ent.posY, dz = ent.posZ;
                double mdx = ent.motionX, mdy = ent.motionY, mdz = ent.motionZ;
                for (int i = 0; i < 800; i++) {
                    dx += mdx;
                    dy += mdy;
                    dz += mdz;
                    float f2 = 0.99F;
                    float f3 = 0.05F;

                    mdx *= f2;
                    mdy *= f2;
                    mdz *= f2;
                    mdy -= f3;

                    IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(dx, dy, dz));
                    if (state != null && state.getBlock() != null && state.getBlock() != Blocks.air) {
                        //Esp.autoBeaconBeam(dx, dy, dz, Color.yellow.getRGB(), 0.5f, e.partialTicks);
                        Esp.autoBox3D(dx, dy, dz, 0.5, 0.5, Color.yellow, 5f, true);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String id() {
        return "projpth";
    }

    @Override
    public Specification category() {
        return Specification.COMBAT;
    }

    @Override
    public String description() {
        return "Predics where the projectile lands[with random uga-buga]";
    }

    @Override
    public String fname() {
        return "Projectile path";
    }
}
