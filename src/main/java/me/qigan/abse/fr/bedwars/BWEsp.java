package me.qigan.abse.fr.bedwars;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BWEsp extends Module {

    @SubscribeEvent
    void esp(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null) return;
        int ln = -1;
        Color yourCol = null;
        if (Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4) != null) {
            ln = Utils.getItemColor(Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4));
            if (ln != -1) yourCol = new Color(ln);
        }

        for (Entity ex : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (ex instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) ex;
                double dist = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(player);
                if (player.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID() && !Debug.GENERAL) continue;
                if (player.getEquipmentInSlot(4) != null) {
                    if (player.getEquipmentInSlot(4).getItem() == Items.leather_helmet) {
                        ItemStack item = player.getEquipmentInSlot(4);
                        int prec = Utils.getItemColor(item);
                        if (prec == -1) continue;
                        Color col = new Color(prec);
                        if (prec == ln && !Index.MAIN_CFG.getBoolVal("bwesp_team")) continue;
                        Esp.autoBox3D(ex, col, 2f, true);

                        String strToR = "\u00A7l";
                        ItemStack legs = player.getEquipmentInSlot(2);
                        if (legs != null) {
                            if (legs.getItem() == Items.leather_leggings) {
                                strToR += "\u00A7aLeather";
                            } else if (legs.getItem() == Items.chainmail_leggings) {
                                strToR += "\u00A77Chainmail";
                            } else if (legs.getItem() == Items.iron_leggings) {
                                strToR += "\u00A7fIron";
                            } else if (legs.getItem() == Items.diamond_leggings) {
                                strToR += "\u00A7bDiamond";
                            }

                            NBTTagList list = legs.getEnchantmentTagList();
                            if (list != null) {
                                for (int i = 0; i < list.tagCount(); i++) {
                                    NBTTagCompound comp = list.getCompoundTagAt(i);
                                    if (comp.getInteger("id") == 0) {
                                        strToR += " \u00A7dp" + comp.getInteger("lvl");
                                    }
                                }
                            }
                        }

                        Esp.renderTextInWorld(strToR, player.posX, player.posY + 0.4, player.posZ, 0xFFFFFF, e.partialTicks);
                        if (dist > 10) {
                            if (Index.MAIN_CFG.getBoolVal("bwesp_tracer")) Esp.drawTracer(
                                    Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + 1, Minecraft.getMinecraft().thePlayer.posZ,
                                    player.posX, player.posY, player.posZ, col, 2);
                            Esp.renderTextInWorld(player.getName(), player.posX, player.posY + 1, player.posZ, prec, e.partialTicks);
                        }
                        Esp.renderTextInWorld(player.getHealth() + "/" + player.getMaxHealth(), player.posX, player.posY + 0.7, player.posZ, 0xFF0000, e.partialTicks);
                    }
                } else if (player.isInvisible() || player.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer)) {
                    Esp.autoBox3D(ex, new Color(255, 255, 255, 255), 3f, true);
                    if (dist > 10) Esp.renderTextInWorld(player.getName(), player.posX, player.posY + 1, player.posZ, 0xFFFFFF, e.partialTicks);
                    Esp.renderTextInWorld(player.getHealth() + "/" + player.getMaxHealth(), player.posX, player.posY + 0.7, player.posZ, 0xFF0000, e.partialTicks);
                    if (Index.MAIN_CFG.getBoolVal("bwesp_tracer")) Esp.drawTracer(
                            Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + 1, Minecraft.getMinecraft().thePlayer.posZ,
                            player.posX, player.posY, player.posZ, new Color(0xFFFFFF), 2);
                }
            }
        }
    }

    @Override
    public String id() {
        return "bwesp";
    }

    @Override
    public Specification category() {
        return Specification.BEDWARS;
    }

    @Override
    public String fname() {
        return "Bedwars esp";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("bwesp_tracer", "Tracers", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("bwesp_team", "Track team", ValType.BOOLEAN, "false"));
        return list;
    }

    @Override
    public String description() {
        return "Kinda sus function for bedwars";
    }
}
