package me.qigan.abse.fr.qol;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.crp.MainWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class TemporaryGb extends Module {

    public static class TempGb {
        public final BlockPos pos;
        public final IBlockState block;
        public int tick;

        public TempGb(BlockPos pos, IBlockState block, int tick) {
            this.pos = pos;
            this.block = block;
            this.tick = tick;

            GhostBlocks.placeBlock(pos, block);
            container.add(pos);
        }
    }

    public static final List<TempGb> temps = new ArrayList<TempGb>();
    public static final List<BlockPos> container = new ArrayList<BlockPos>();

    public static void grestore() {
        temps.clear();
        container.clear();
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null) return;
        try {
            BlockPos pos = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1).getBlockPos();
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
            if (Index.KEY_MANAGER.get("ghostChest").isPressed()) {
                BlockPos np = pos.add(0, 1, 0);
                if (Minecraft.getMinecraft().theWorld.getBlockState(np).getBlock() == Blocks.air) {
                    if (!container.contains(np)) temps.add(new TempGb(np, Blocks.ender_chest.getDefaultState(), Index.MAIN_CFG.getIntVal("temp_gb_time")));
                }
            }
            if (Index.KEY_MANAGER.get("tempGhostBlocks").isDown()) {
                if (block != Blocks.air && block != null) {
                    if (!container.contains(pos)) temps.add(new TempGb(pos, Blocks.air.getDefaultState(), Index.MAIN_CFG.getIntVal("temp_gb_time")));
                }
            }
            for (TempGb t : temps) {
                t.tick--;
                if (t.tick <= 0) {
                    GhostBlocks.restore(t.pos);
                    container.remove(t.pos);
                    temps.remove(t);
                }
            }
        } catch (Exception ex) {}
    }

    @Override
    public String id() {
        return "tgb";
    }

    @Override
    public Specification category() {
        return Specification.QOL;
    }

    @Override
    public String fname() {
        return "Temp ghost blocks";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("temp_gb_time", "Time(client ticks)", ValType.NUMBER, "200"));
        list.add(new SetsData<>("tempGhostBlocks", "Remove blocks key", ValType.KEYBINDING, Keyboard.KEY_NONE));
        list.add(new SetsData<>("ghostChest", "Ghost chest key", ValType.KEYBINDING, Keyboard.KEY_NONE));
        return list;
    }

    @Override
    public String description() {
        return "Places temporary ghost block";
    }
}
