package me.qigan.abse.fr.other;

import me.qigan.abse.crp.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NFBA extends Module {
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	void thr(RenderWorldLastEvent e) {
		if (!isEnabled()) return;
		try {
			for (Entity ent: Minecraft.getMinecraft().theWorld.loadedEntityList) {
				if (ent instanceof EntityFallingBlock) {
					//EntityFallingBlock ne = (EntityFallingBlock) ent;
					Minecraft.getMinecraft().theWorld.removeEntity(ent);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String id() {
		return "nfba";
	}

	@Override
	public Specification category() {
		return Specification.QOL;
	}

	@Override
	public String fname() {
		return "Hide falling blocks";
	}

	@Override
	public String description() {
		return "No falling block animations(hide falling blocks)";
	}
}
