package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;

public class ARSlots {
    public int pickaxe = -1;
    public int pearls = -1;
    public int ewp = -1;
    public int tnt = -1;

    public void proc() {
        pickaxe = findPicake();
        pearls = findPearls();
        ewp = findSlot("ASPECT_OF_THE_VOID");
        tnt = findSlot("INFINITE_SUPERBOOM_TNT");
        if (tnt == -1) tnt = findSlot("SUPERBOOM_TNT");
    }

    private static int findPicake() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) continue;
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.diamond_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.golden_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.iron_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.stone_pickaxe
                    || Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.wooden_pickaxe) return i;
        }
        return -1;
    }

    private static int findPearls() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) continue;
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.ender_pearl) return i;
        }
        return -1;
    }

    private static int findSlot(String str) {
        for (int i = 0; i < 9; i++) {
            if (Utils.getSbData(Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i))
                    .getString("id").equalsIgnoreCase(str)) return i;
        }
        return -1;
    }
}
