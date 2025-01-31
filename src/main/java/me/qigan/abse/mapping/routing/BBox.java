package me.qigan.abse.mapping.routing;

import me.qigan.abse.fr.qol.GhostBlocks;
import me.qigan.abse.mapping.Room;
import me.qigan.abse.mapping.mod.M7Route;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public class BBox {
    public final int x1;
    public final int y1;
    public final int z1;
    public final int x2;
    public final int y2;
    public final int z2;

    public IBlockState block;
    public M7Route.Category cat = M7Route.Category.NONE;


    public BBox(int x1, int y1, int z1, int x2, int y2, int z2, IBlockState bts) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.block = bts;
    }

    public BBox category(M7Route.Category category) {
        this.cat = category;
        return this;
    }

    public void run() {
        for (int i = Math.min(x1, x2); i <= Math.max(x1, x2); i++) {
            for (int j = Math.min(y1, y2); j <= Math.max(y1, y2); j++) {
                for (int l = Math.min(z1, z2); l <= Math.max(z1, z2); l++) {
                    //Minecraft.getMinecraft().thePlayer.worldObj.setBlockToAir()
                    GhostBlocks.placeBlock(new BlockPos(i, j, l), this.block);
                }
            }
        }
    }

    public void runRelative(Room room) {
        for (int i = Math.min(x1, x2); i <= Math.max(x1, x2); i++) {
            for (int j = Math.min(y1, y2); j <= Math.max(y1, y2); j++) {
                for (int l = Math.min(z1, z2); l <= Math.max(z1, z2); l++) {
                    //Minecraft.getMinecraft().thePlayer.worldObj.setBlockToAir()
                    GhostBlocks.placeBlock(room.transformInnerCoordinate(new BlockPos(i, j, l)), this.block);
                }
            }
        }
    }
}
