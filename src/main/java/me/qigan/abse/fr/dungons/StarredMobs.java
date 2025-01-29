package me.qigan.abse.fr.dungons;

import me.qigan.abse.Index;
import me.qigan.abse.config.WKeybind;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.crp.Module;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class StarredMobs extends Module {
    public static final Color starCol = new Color(0, 141, 167, 255);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    void render(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        WKeybind bind = Index.KEY_MANAGER.get("unlimitedRange");
        if (Minecraft.getMinecraft().theWorld != null) {
            for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if (ent.getPosition().distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 500 || bind.isDown()) {
                    if (ent instanceof EntityArmorStand) {
                        if (ent.getName().contains("\u272F") && ent.getName().contains("\u2764")) {
                            Esp.autoBox3D(ent.posX, ent.posY - 0.1, ent.posZ, 1, (ent.getName().contains("Fels") ? 3 : 2), starCol, 3f, true);
                            if (bind.isDown()) {
                                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                                Esp.drawTracer(player.posX, player.posY, player.posZ, ent.posX, ent.posY, ent.posZ, starCol, 2f);
                            }
                        }
                    } else if (ent.getName().equalsIgnoreCase("Shadow Assassin")) {
                        Esp.autoBox3D(ent, new Color(255, 0,0, 255), 3f, true);
                        Esp.renderTextInWorld("SA", ent.posX, ent.posY+1, ent.posZ, 0xFF4F38 , e.partialTicks);
                    }
                }
            }
        }
    }

    @Override
    public String id() {
        return "sm";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Show starred mobs";
    }

    @Override
    public String description() {
        return "Show Starred mobs";
    }
}
