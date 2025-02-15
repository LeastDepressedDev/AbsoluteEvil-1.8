package me.qigan.abse.mapping.auto;

import me.qigan.abse.Index;
import me.qigan.abse.mapping.MappingUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public class SeqClick extends QueuedSeq {


    public SeqClick(boolean right, BlockPos pos) {

        if (pos != null && Index.MAPPING_CONTROLLER.getPlayerRoom() != null) {
            pos = Index.MAPPING_CONTROLLER.getPlayerRoom().transformInnerCoordinate(pos);
        }
        IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);

    }
}
