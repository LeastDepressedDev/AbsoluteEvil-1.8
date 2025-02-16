package me.qigan.abse.fr.mining;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.Alert;
import me.qigan.abse.sync.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoMiningSecCheck extends Module {

    public static void alert(String reason) {
        AutoMining.active = false;
        Alert.call(reason, 500000, Index.MAIN_CFG.getIntVal("auto_mining_sec_vol"));
    }

    //TODO: Add alert if someone is going toward your route
    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || e.phase == TickEvent.Phase.END) return;
        if (!AutoMining.active) return;
        if (Index.MAIN_CFG.getBoolVal("auto_mining_sec_int")) {
            int sz = Index.MAIN_CFG.getIntVal("auto_mining_sec_int_size");
            int pace = AutoMining.progress;
            for (int i = 0; i < sz; i++) {
                Block state = Minecraft.getMinecraft().theWorld.getBlockState(AutoMining.blockRoute.get(pace)).getBlock();
                if (state == Blocks.air) alert("\u00A7cROUTE INCOMPLETE!");
                pace++;
                if (pace >= AutoMining.blockRoute.size()) pace = 0;
            }
        }
        if (Index.MAIN_CFG.getBoolVal("auto_mining_sec_player")) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if (ent instanceof EntityPlayer && ent.getEntityId() != player.getEntityId()) {
                    if (ent.getDistance(player.posX, player.posY, player.posZ) < 1.3) {
                        //TODO: Add check for a goblin
                        alert("\u00A7c!!PLAYER NEARBY!!");
                    }
                }
            }
        }
        if (Index.MAIN_CFG.getBoolVal("auto_mining_sec_fuel")) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
            if (stack != null && Utils.getSbData(stack).hasKey("drill_fuel")) {
                if (Utils.getSbData(stack).getInteger("drill_fuel") == 0) alert("\u00A7cINSUFICCIENT FUEL");
            }
        }
        if (Index.MAIN_CFG.getBoolVal("auto_mining_sec_far")) {
            double dist = Math.sqrt(Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(AutoMining.blockRoute.get(AutoMining.progress)));
            if (dist > 60) alert("\u00A7c!!TOO FAR AWAY!!");
        }
    }

    @SubscribeEvent
    void chat(ClientChatReceivedEvent e) {
        if (!isEnabled()) return;
        if (Index.MAIN_CFG.getBoolVal("auto_mining_sec_chat")) {
            String name = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();
            String text = e.message.getFormattedText().toLowerCase();
            if (text.contains(name)) alert("\u00A7eCHAT MENTIONED");
        }
    }

    @Override
    public String id() {
        return "auto_mining_sec";
    }

    @Override
    public String fname() {
        return "Auto mining security checks";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("auto_mining_sec_vol", "Volume", ValType.NUMBER, "2"));
        list.add(new SetsData<>("auto_mining_sec_int", "Route integrity check", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_sec_int_size", "Integrity size", ValType.NUMBER, "5"));
        list.add(new SetsData<>("auto_mining_sec_player", "Nearby player alert", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_sec_fuel", "Fuel check", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_sec_far", "Far away check", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_sec_chat", "Chat mentioned check", ValType.BOOLEAN, "true"));
        return list;
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public String description() {
        return "Provides security checks for auto mining";
    }
}
