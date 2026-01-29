package me.qigan.abse.fr.other;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GhostBlocks extends Module {

    public static final List<Block> excepts = new ArrayList<Block>(Arrays.asList(
            Blocks.bedrock,
            Blocks.obsidian,
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.lever
    ));

    public static int tick = 0;
    /**
     * In addressed data: 1 is new block, 2 is old block
     */
    public static Map<BlockPos, AddressedData<IBlockState, IBlockState>> blocks = new HashMap<>();

    public static boolean placeBlock(BlockPos pos, IBlockState block) {
        IBlockState pb = Minecraft.getMinecraft().theWorld.getBlockState(pos);
        if (excepts.contains(pb.getBlock()) && !Index.MAIN_CFG.getBoolVal("ignore_except") || pb.getBlock() == Blocks.bed) return false;
        blocks.put(pos, new AddressedData<>(block, pb));
        return true;
    }

    public static boolean placeBlockLegacy(BlockPos pos, Block block) {
        if (excepts.contains(Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock())
                && !Index.MAIN_CFG.getBoolVal("ignore_except")) return false;
        Minecraft.getMinecraft().theWorld.setBlockState(pos, block.getDefaultState());
        return true;
    }

    @SubscribeEvent
    void renderWorldLast(RenderWorldLastEvent e) {
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (!Index.MAIN_CFG.getBoolVal("render_gb_box")) return;
        for (Map.Entry<BlockPos, AddressedData<IBlockState, IBlockState>> bp : blocks.entrySet()) {
            if (bp.getKey() == null) continue;
            Esp.autoBox3D(bp.getKey().getX()+0.5, bp.getKey().getY()+1, bp.getKey().getZ()+0.5, 1, 1 , new Color(255, 0,0, 255), 2f, false);
        }
    }

    @SubscribeEvent
    void clientTick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (tick >= Integer.parseInt(Index.MAIN_CFG.getStrVal("tick_upt"))) {
            tick = 0;
            for (Map.Entry<BlockPos, AddressedData<IBlockState, IBlockState>> bp : blocks.entrySet()) {
                Minecraft.getMinecraft().theWorld.setBlockState(bp.getKey(), bp.getValue().getNamespace());
            }
        } else {
            tick++;
        }
    }

    public static void restore(BlockPos pos) {
        IBlockState gb = blocks.get(pos).getObject();
        blocks.remove(pos);
        Minecraft.getMinecraft().theWorld.setBlockState(pos, gb);
    }

    public static void grestore() {
        TemporaryGb.grestore();
        while (blocks.size() > 0) {
            AddressedData<BlockPos, AddressedData<IBlockState, IBlockState>> data = Utils.mapToAddressedDataList(blocks).get(0);
            restore(data.getNamespace());
        }
    }

    @SubscribeEvent
    void gbKeybdTick(TickEvent.ClientTickEvent e) {
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (Index.KEY_MANAGER.get("ghostBlocksReset").isPressed()) grestore();
        if (!isEnabled()) return;
        try {
            BlockPos pos = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1).getBlockPos();
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
            if (Index.KEY_MANAGER.get("ghostBlocks").isDown()) {
                if (block != Blocks.air && block != null) placeBlock(pos, Blocks.air.getDefaultState());
            }
            if (Index.KEY_MANAGER.get("legGhostBlocks").isDown()) {
                placeBlockLegacy(pos, Blocks.air);
            }
        } catch (Exception ex) {}
    }

    @SubscribeEvent
    void worldChange(WorldEvent.Load e) {
        blocks.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    void renderOverlay(RenderGameOverlayEvent.Text e) {
        if (!isEnabled() || !Index.MAIN_CFG.getBoolVal("show_hud_ghostblocks")) return;

        Point coord = Index.POS_CFG.calc("gbmg");

        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Updating " + blocks.entrySet().size() + " blocks.", (float) coord.getX(), (float) coord.getY(), 3110656);
    }

    @Override
    public String id() {
        return "gb";
    }

    @Override
    public Specification category() {
        return Specification.QOL;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("render_gb_box", "Render ghost block box", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("ghostBlocks", "Ghost blocks key", ValType.KEYBINDING, Keyboard.KEY_F));
        list.add(new SetsData<>("ghostBlocksReset", "Reset key", ValType.KEYBINDING, Keyboard.KEY_Z));
        list.add(new SetsData<>("legGhostBlocks", "Legacy ghost blocks", ValType.KEYBINDING, Keyboard.KEY_NONE));
        list.add(new SetsData<>("ignore_except", "Ignore exception blocks", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("tick_upt", "Tick update", ValType.NUMBER, "0"));
        list.add(new SetsData<>("show_hud_ghostblocks", "Hud", ValType.BOOLEAN, "true"));
        return list;
    }

    @Override
    public String fname() {
        return "Ghost blocks";
    }

    @Override
    public String description() {
        return "Places ghost blocks on keybind press. (Except chests, bedrock, obsidian and levers)";
    }
}
