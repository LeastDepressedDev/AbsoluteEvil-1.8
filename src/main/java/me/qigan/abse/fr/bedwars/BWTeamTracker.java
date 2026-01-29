package me.qigan.abse.fr.bedwars;

import me.qigan.abse.Holder;
import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.gui.overlay.ImportantChatOVR;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BWTeamTracker extends Module {

    public static Map<EntityPlayer, Integer> team = new HashMap<>();

    private static boolean swap;

    @SubscribeEvent
    void render(RenderGameOverlayEvent.Text e) {
        if (!isEnabled()) return;
        List<String> lines = new ArrayList<>(Arrays.asList("\u00A7aTeam: "));
        for (Map.Entry<EntityPlayer, Integer> mate : team.entrySet()) {
            lines.add("\u00A7f" + mate.getKey().getName() + ": " +
                    ((mate.getValue() == 0) ? "\u00A7aALIVE" : (mate.getValue() == -1) ? "\u00A7cDEAD" :
                            ("\u00A7e" + ((float)mate.getValue()/20f) + "s")
                    ));
        }

        Point pos = Index.POS_CFG.calc("bwt_display");

        Esp.drawAllignedTextList(lines, pos.x, pos.y, false, e.resolution, S2Dtype.SHADOW);
    }

    @SubscribeEvent
    void onUpdate(LivingEvent.LivingUpdateEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        int ln = -1;
        if (Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4) != null) {
            ln = Utils.getItemColor(Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4));
            if (ln != -1) {
                if (e.entity instanceof EntityPlayer) {
                    if (e.entity.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) return;
                    int col = Utils.getItemColor(((EntityPlayer) e.entity).getEquipmentInSlot(4));
                    if (ln == col) {
                        if (!team.containsKey(e.entity)) {
                            team.put((EntityPlayer) e.entity, 0);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        swap=!swap;
        if (swap) {
            for (Map.Entry<EntityPlayer, Integer> mate : team.entrySet()) {
                if (mate.getValue() > 0) {
                    team.put(mate.getKey(), mate.getValue() - 1);
                }
            }
            if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8) == null)
                return;
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8).getItem() == Items.bed) {
                team.clear();
                if (Index.MAIN_CFG.getBoolVal("imp_chat_ars")) ImportantChatOVR.clear();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    void chat(ClientChatReceivedEvent e) {
        String msg_s = Utils.cleanSB(e.message.getFormattedText());
        for (Map.Entry<EntityPlayer, Integer> mate : team.entrySet()) {
            if (msg_s.contains(mate.getKey().getName())) {
                if (Holder.quickFind("imp_chat").isEnabled())
                    ImportantChatOVR.add(e.message.getFormattedText().replaceAll("\u00A7l", ""));
            }

            if (msg_s.startsWith(mate.getKey().getName() + " ") && !msg_s.contains("purchased")) {
                team.put(mate.getKey(), (msg_s.contains("FINAL KILL")) ? -1 : (int) Index.MAIN_CFG.getDoubleVal("bwtt_rpt")*20);
                break;
            } else if (msg_s.contains(mate.getKey().getName())) {
                if (msg_s.contains("disconnected") || msg_s.contains("left") || msg_s.contains("quit")) {
                    team.put(mate.getKey(), -1);
                } else if (msg_s.contains("reconnected") || msg_s.contains("rejoined") || msg_s.contains("joined")) {
                    team.put(mate.getKey(), (int) Index.MAIN_CFG.getDoubleVal("bwtt_rpt")*20);
                }
            }
        }
    }

    @Override
    public String id() {
        return "bwtt";
    }

    @Override
    public Specification category() {
        return Specification.BEDWARS;
    }

    @Override
    public String fname() {
        return "Bedwars team tracker";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("bwtt_rpt", "Respawn time(seconds)", ValType.DOUBLE_NUMBER, "5"));
        return list;
    }

    @Override
    public String description() {
        return "Tracks your teamates";
    }
}
