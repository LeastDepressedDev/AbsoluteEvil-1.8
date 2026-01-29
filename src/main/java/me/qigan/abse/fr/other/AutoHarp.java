package me.qigan.abse.fr.other;

import me.qigan.abse.crp.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoHarp extends Module {

    public static List<ItemStack> pre = new ArrayList<>();

    @SubscribeEvent
    void onGui(GuiScreenEvent.InitGuiEvent.Post e) {
        if (!isEnabled()) return;
        if (e.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) e.gui;
            ContainerChest c1 = (ContainerChest) chest.inventorySlots;
            if (c1.getLowerChestInventory().getName().startsWith("Harp - ")) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00A7cWARNING!!! \u00A7a Auto harp is enabled!"));
                pre = new ArrayList<>();
            }
//            for (int i = 0; i < c1.inventoryItemStacks.size(); i++) {
//                if (c1.inventoryItemStacks.get(i).getItem() == Item.getItemFromBlock(Blocks.quartz_block)) {
//                    event.gui.mc.playerController.windowClick(c1.windowId, i, 0, 0, event.gui.mc.thePlayer);
//                }
//            }
        }
    }

    private boolean check(List<ItemStack> cur) {
        if (cur.size() != pre.size()) {
            return true;
        }
        for (int i = 18; i < 36; i++) {
            if (cur.get(i) != pre.get(i)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    void runtime(GuiScreenEvent.DrawScreenEvent e) {
        if (!isEnabled()) return;
        if (e.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) e.gui;
            ContainerChest c1 = (ContainerChest) chest.inventorySlots;
            if (c1.getLowerChestInventory().getName().startsWith("Harp - ") && check(c1.getInventory())) {
                for (int i = 36; i < 45; i++) {
                    if (c1.getInventory().get(i) == null) continue;
                    if (c1.getInventory().get(i).getItem() == Item.getItemFromBlock(Blocks.quartz_block)) {
                        e.gui.mc.playerController.windowClick(c1.windowId, i, 0, 4, e.gui.mc.thePlayer);
                    }
                }
                pre = new ArrayList<>(c1.getInventory());
            }
        }
    }


    @Override
    public String id() {
        return "Auto harp";
    }

    @Override
    public Specification category() {
        return Specification.QOL;
    }

    @Override
    public String description() {
        return "Kept as a memory of a legendary function, that started era of rat on sb.";
    }
}
