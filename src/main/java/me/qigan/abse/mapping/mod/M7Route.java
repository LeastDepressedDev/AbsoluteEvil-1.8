package me.qigan.abse.mapping.mod;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.qol.GhostBlocks;
import me.qigan.abse.mapping.routing.BBox;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class M7Route extends Module {

    private boolean readyUp = false;

    public static enum Category {
        NONE,
        OTHER,
        HEALER_LEGIT,
        HEALER_FULL,
        BASIC_PREDEV,
        ALIGNERS,
        TERM_AURA,
        CORE,
        CUSTOM
    }

    public static abstract class DynamicRouteElement implements Runnable {

        public final Block before;
        public final Block after;
        public final BlockPos pos;

        public DynamicRouteElement(BlockPos pos, Block before, Block after) {
            this.before = before;
            this.after = after;
            this.pos = pos;
        }

        @Override
        public abstract void run();
    }

    public static void setup() {
        register(new DynamicRouteElement(new BlockPos(91, 132, 45), Blocks.redstone_block, Blocks.emerald_block) {
            @Override
            public void run() {
                new BBox(90, 131, 44, 91, 133, 46, Blocks.air.getDefaultState()).run();
                new BBox(88, 131, 46, 91, 133, 46, Blocks.air.getDefaultState()).run();
            }
        });
        register(new DynamicRouteElement(new BlockPos(91, 116, 44), Blocks.redstone_block, Blocks.emerald_block) {
            @Override
            public void run() {
                new BBox(93, 115, 44, 93, 135, 45, Blocks.stained_glass.getDefaultState()).run();
                new BBox(92, 130, 43, 92, 117, 43, Blocks.stained_glass.getDefaultState()).run();
            }
        });
        register(new DynamicRouteElement(new BlockPos(80, 222, 52), Blocks.redstone_block, Blocks.emerald_block) {
            @Override
            public void run() {
                new BBox(78, 240, 52, 78, 221, 52, Blocks.stained_glass.getDefaultState()).run();
            }
        });
        register(new DynamicRouteElement(new BlockPos(66, 222, 52), Blocks.redstone_block, Blocks.emerald_block) {
            @Override
            public void run() {
                new BBox(68, 240, 52, 68, 221, 52, Blocks.stained_glass.getDefaultState()).run();
            }
        });
    }

    public static void register(DynamicRouteElement e) {
        dynamics.put(e.pos, e);
    }

    public static List<BBox> bounds = new ArrayList<BBox>(Arrays.asList(
            new BBox(85, 219, 61, 92, 213, 61, Blocks.air.getDefaultState()).category(Category.OTHER),
            new BBox(91, 165, 41, 95, 167, 40, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(54, 64, 80, 54, 63, 78, Blocks.air.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(57, 108, 123, 56, 111, 118, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(88, 165, 41, 95, 166, 41, Blocks.oak_fence.getDefaultState()).category(Category.HEALER_FULL), // New with alligner here ->
            new BBox(70, 221, 38, 71, 221, 38, Blocks.ender_chest.getDefaultState()).category(Category.BASIC_PREDEV),
            new BBox(77, 221, 37, 77, 222, 32, Blocks.ender_chest.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(78, 221, 38, 78, 240, 38, Blocks.stained_glass.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(77, 220, 37, 78, 220, 37, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(100, 167, 47, 100, 165, 46, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(100, 169, 46, 100, 169, 46, Blocks.ender_chest.getDefaultState()).category(Category.BASIC_PREDEV),
            new BBox(52, 114, 111, 51, 114, 111, Blocks.ender_chest.getDefaultState()).category(Category.BASIC_PREDEV),
            new BBox(52, 113, 111, 51, 113, 111, Blocks.air.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(72, 106,142, 63, 106, 132, Blocks.rail.getDefaultState()).category(Category.BASIC_PREDEV),
            new BBox(53, 106, 137, 55, 106, 139, Blocks.rail.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(17, 131, 136, 18, 128, 135, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(18, 131, 135, 18, 131, 136, Blocks.wooden_slab.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP)).category(Category.HEALER_FULL),
            new BBox(19, 132, 135, 20, 129, 135, Blocks.oak_fence.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(52, 132, 140, 39, 136, 140, Blocks.stained_glass.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(32, 132, 137, 60, 140, 137, Blocks.stained_glass.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(52, 106, 137, 52, 129, 139, Blocks.stained_glass.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(85, 118, 35, 83, 106, 35, Blocks.stained_glass.getDefaultState()).category(Category.ALIGNERS),
            new BBox(58, 134, 142, 58, 134, 142, Blocks.lapis_block.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(62, 134, 142, 62, 134, 142, Blocks.lapis_block.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(58, 123, 122,57, 125, 118, Blocks.air.getDefaultState()).category(Category.OTHER),
            new BBox(1, 113, 86, 10, 113, 86, Blocks.stone_slab.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM)).category(Category.ALIGNERS),
            new BBox(5, 113, 85, 5, 113, 87, Blocks.stone_slab.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM)).category(Category.ALIGNERS),
            new BBox(10, 113, 85, 10, 113, 87, Blocks.stone_slab.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM)).category(Category.ALIGNERS),
            new BBox(65, 127, 37, 67, 129, 37, Blocks.stained_glass.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(57, 116, 57, 58, 116, 56, Blocks.stained_glass.getDefaultState()).category(Category.CORE),
            new BBox(58, 115, 58, 57, 114, 52, Blocks.air.getDefaultState()).category(Category.CORE),
            new BBox(57, 116, 58, 58, 116, 58, Blocks.oak_fence.getDefaultState()).category(Category.CORE),
            new BBox(53, 114, 52, 52, 114, 53, Blocks.stained_glass.getDefaultState()).category(Category.CORE),
            new BBox(51, 114, 52, 51, 115, 59, Blocks.air.getDefaultState()).category(Category.CORE),
            new BBox(51, 115, 51, 52, 117, 51, Blocks.oak_fence.getDefaultState()).category(Category.CORE),
            new BBox(92, 107, 63, 92, 135, 66, Blocks.glass.getDefaultState()).category(Category.ALIGNERS),
            new BBox(92, 135, 106, 92, 107, 120, Blocks.glass.getDefaultState()).category(Category.ALIGNERS),
            new BBox(110, 135, 71, 108, 107, 70, Blocks.glass.getDefaultState()).category(Category.ALIGNERS),
            new BBox(56, 114, 50, 57, 120, 50, Blocks.stained_glass.getDefaultState()).category(Category.CORE),
            new BBox(37, 106, 139, 36, 106, 138, Blocks.ladder.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(24, 106, 141, 25, 106, 142, Blocks.rail.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(3, 119, 56, 1, 106, 54, Blocks.stained_glass.getDefaultState()).category(Category.ALIGNERS),
            new BBox(13, 119, 56, 15, 106, 54, Blocks.stained_glass.getDefaultState()).category(Category.ALIGNERS),
            new BBox(51, 113, 110, 52, 110, 110, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(97, 120, 121, 97, 125, 120, Blocks.oak_fence.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(96, 120, 122, 97, 122, 123, Blocks.air.getDefaultState()).category(Category.HEALER_FULL),
            new BBox(97, 122, 122, 96, 122, 122, Blocks.stone_slab.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP)).category(Category.HEALER_FULL),
            new BBox(39, 109, 31, 42, 109, 31, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(61, 127, 34, 61, 129, 36, Blocks.stained_glass.getDefaultState()).category(Category.HEALER_LEGIT),
            new BBox(-1, 109, 84, -1, 113, 70, Blocks.stone_slab.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP)).category(Category.TERM_AURA),
            new BBox(45, 121, 31, 45, 123, 30, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(17, 123, 94, 17, 123, 92, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(-1, 119, 94, -1, 119, 92, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(-1, 109, 110, -2, 109, 110, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(-2, 109, 110, -2, 109, 113, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(66, 109, 31, 68, 109, 31, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(68, 109, 31, 68, 109, 32, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(38, 109, 141, 40, 109, 141, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA),
            new BBox(41, 108, 141, 51, 114, 141, Blocks.dark_oak_fence.getDefaultState()).category(Category.ALIGNERS),
            new BBox(38, 124, 124, 41, 124, 124, Blocks.dark_oak_fence.getDefaultState()).category(Category.TERM_AURA)
            ));

    public static Map<BlockPos, DynamicRouteElement> dynamics = new HashMap<>();

    public static void placeRoute() {
        GhostBlocks.blocks.clear();
        for (BBox b : bounds) {
            if (b.cat == Category.NONE) continue;
            if (Index.MAIN_CFG.getBoolVal("m7r_t_"+b.cat.name())) b.run();
        }
        if (Index.MAIN_CFG.getBoolVal("m7r_dg")) {
            for (Map.Entry<BlockPos, DynamicRouteElement> etr : dynamics.entrySet()) {
                GhostBlocks.placeBlock(etr.getKey(), etr.getValue().before.getDefaultState());
            }
        }
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00A7a[ABSE] Healer route set!"));
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled() || !Index.MAIN_CFG.getBoolVal("m7r_debug") || Minecraft.getMinecraft().theWorld == null) return;
        Integer i = 0;
        for (BBox b : bounds) {
            Esp.renderTextInWorld(i.toString(),
                    (Math.max(b.x1, b.x2)+Math.min(b.x1, b.x2))/2d,
                    (Math.max(b.y1, b.y2)+Math.min(b.y1, b.y2))/2d,
                    (Math.max(b.z1, b.z2)+Math.min(b.z1, b.z2))/2d,
                    Color.RED.getRGB(), 2d, e.partialTicks
            );
            i++;
        }
    }

    //TODO: FIX THIS FUCKING SHIT
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMsg(EntityJoinWorldEvent e) {
        if (!isEnabled()) return;
        try {
            //System.out.println(e.message.getFormattedText());
            if (e.entity instanceof EntityEnderCrystal && readyUp) {
                placeRoute();
                this.readyUp = false;
            }
        } catch (Exception ex) {}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChange(EntityJoinWorldEvent e) {
        try {
        if (e.entity.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            this.readyUp = true;
        }
        } catch (Exception ex) {}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    void click(PlayerInteractEvent e) {
        if (!Index.MAIN_CFG.getBoolVal("m7r_dg")) return;
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (e.pos == null) return;
            BlockPos bp = Utils.unify(e.pos);
            DynamicRouteElement ele = dynamics.get(bp);
            if (ele != null && Minecraft.getMinecraft().theWorld.getBlockState(bp).getBlock() == ele.before) {
                GhostBlocks.placeBlock(bp, ele.after.getDefaultState());
                ele.run();
                e.setCanceled(true);
            }
        }
    }

    @Override
    public String id() {
        return "m7r";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "M7 auto route";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("m7r_place", "Place route", ValType.BUTTON, (Runnable) M7Route::placeRoute));
        list.add(new SetsData<>("m7r_dg", "Use dynamic routes", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("m7r_debug", "Do debug render", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("m7r_sep_com", "Partial route switches by categories", ValType.COMMENT, null));
        for (M7Route.Category cat : Category.values()) {
            if (cat == Category.NONE) continue;
            list.add(new SetsData<>("m7r_t_"+cat.name(), cat.name(), ValType.BOOLEAN, "true"));
        }
        return list;
    }

    @Override
    public String description() {
        return "auto f7-m7 route";
    }
}
