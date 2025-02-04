package me.qigan.abse.fr.macro;

import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.sync.Sync;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Macro
public class SuplyPearl extends Module {

    private static int getPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) continue;
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.ender_pearl) return i;
        }
        return -1;
    }

    @SubscribeEvent
    void interaction(PlayerInteractEvent e) {
        if (!isEnabled() || !Sync.inKuudra) return;
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null) return;
            if (Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() == Item.getItemFromBlock(Blocks.chest)) {
                int slot = getPearlSlot();
                if (slot == -1) return;
                Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;
                ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 2);
            }
        }
    }

    @Override
    public String id() {
        return "suply_pearl";
    }

    @Override
    public Specification category() {
        return Specification.SB_QOL;
    }

    @Override
    public String fname() {
        return "Supply pearl";
    }

    @Override
    public String description() {
        return "throws ender pearl on supply";
    }
}
