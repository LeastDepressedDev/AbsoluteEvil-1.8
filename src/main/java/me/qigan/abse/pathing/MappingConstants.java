package me.qigan.abse.pathing;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.*;

public class MappingConstants {

    public static final BlockPos[] MAP_BOUNDS = new BlockPos[]{new BlockPos(-200, 0, -200), new BlockPos(-10, 255, -10)};
    public static final int ROOM_SIZE = 30;

    public static final Set<Block> NOT_COLLIDABLE = new HashSet<>();
    public static final Set<Block> AIRABLE = new HashSet<>();

    public static void setup() {
        NOT_COLLIDABLE.add(Blocks.air);
        NOT_COLLIDABLE.add(Blocks.torch);
        NOT_COLLIDABLE.add(Blocks.ladder);
        NOT_COLLIDABLE.add(Blocks.lever);
        NOT_COLLIDABLE.add(Blocks.stone_button);
        NOT_COLLIDABLE.add(Blocks.wooden_button);
        NOT_COLLIDABLE.add(Blocks.cake);
        NOT_COLLIDABLE.add(Blocks.carpet);
        NOT_COLLIDABLE.add(Blocks.bed);
        NOT_COLLIDABLE.add(Blocks.vine);
        NOT_COLLIDABLE.add(Blocks.tallgrass);



        AIRABLE.add(Blocks.air);
        AIRABLE.add(Blocks.emerald_block);
        AIRABLE.add(Blocks.melon_block);
    }
}
