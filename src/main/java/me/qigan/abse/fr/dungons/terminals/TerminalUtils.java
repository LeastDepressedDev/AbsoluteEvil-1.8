package me.qigan.abse.fr.dungons.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TerminalUtils {
    public static int cordToSlot(int[] cord) {
        return cord[0]+cord[1]*9;
    }

    public static ContainerChest getOpenedChestContainer() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
            return (ContainerChest) chest.inventorySlots;
        }
        return null;
    }
}
