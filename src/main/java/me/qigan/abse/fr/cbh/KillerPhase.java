package me.qigan.abse.fr.cbh;

import me.qigan.abse.Index;
import me.qigan.abse.config.WKeybind;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

public class KillerPhase extends Module {

    @SubscribeEvent(priority = EventPriority.LOW)
    void render(RenderWorldLastEvent e) {
        WKeybind bind = Index.KEY_MANAGER.binds.get("aimLock");
        if (!isEnabled()) return;
        if (bind.isDown()) {
            Entity ent = Minecraft.getMinecraft().objectMouseOver.entityHit;
            if (ent == null) return;
            if (ent instanceof EntityArmorStand || ent instanceof EntityXPOrb || ent instanceof EntityItem || ent instanceof EntityItemFrame) return;
            if ((ent instanceof EntityPlayer && ((EntityPlayer) ent).hurtTime == 0) || ent.hurtResistantTime < 13) {
                ClickSimTick.clickWCheck(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), 2);
            }
            Esp.autoBox3D(ent, Color.cyan, 2.5f, true);
            Esp.renderTextInWorld("\u00A7c" + ent.hurtResistantTime, ent.posX, ent.posY+1.5, ent.posZ, 0xFFFFFF, e.partialTicks);
        }
//        for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
//            Esp.renderTextInWorld("\u00A7c" + ent.hurtResistantTime, ent.posX, ent.posY, ent.posZ, 0xFFFFFF, e.partialTicks);
//        }
    }

    @Override
    public String id() {
        return "kilph";
    }

    @Override
    public Specification category() {
        return Specification.COMBAT;
    }

    @Override
    public String description() {
        return "This is wild, actually, works on [aim_lock] key";
    }

    @Override
    public String fname() {
        return "Killerphase";
    }
}
