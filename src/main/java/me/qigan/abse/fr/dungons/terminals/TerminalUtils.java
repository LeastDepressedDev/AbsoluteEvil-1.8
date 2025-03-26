package me.qigan.abse.fr.dungons.terminals;

import me.qigan.abse.config.AddressedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static String replaceColorName(String line) {
        return line
                .replace("light gray", "silver")
                .replace("wool", "white")
                .replace("bone", "white")
                .replace("ink", "back")
                .replace("lapis", "blue")
                .replace("cocoa", "brown")
                .replace("dandelion", "yellow")
                .replace("light gray", "silver")
                .replace("rose", "red")
                .replace("cactus", "green")
                ;
    }

    public static List<AddressedData<int[], List<int[]>>> clusterize(List<int[]> dots, double dist) {
        List<AddressedData<int[], List<int[]>>> groups = new ArrayList<>();
        for (int[] dot : dots) {
            boolean flag = true;
            for (AddressedData<int[], List<int[]>> group : groups) {
                int[] c = group.getNamespace();
                if (Math.pow(dot[0]-c[0], 2) + Math.pow(dot[1]-c[1], 2) <= dist*dist) {
                    group.getObject().add(dot);
                    group.setNamespace(new int[]{(dot[0]+c[0])/2, (dot[1]+c[1])/2});
                    flag = false;
                    break;
                }
            }
            if (flag) {
                groups.add(new AddressedData<>(dot.clone(), new ArrayList<>(Arrays.asList(dot))));
            }
        }
        return groups;
    }
}
