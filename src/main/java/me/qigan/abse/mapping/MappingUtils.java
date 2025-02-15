package me.qigan.abse.mapping;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

public class MappingUtils {

    /**
     *
     * @param angle (IN DEGREES)
     */
    public static int[] transp(int x, int y, double angle) {
        angle = Math.toRadians(angle);
        //Math.
        return new int[]{
                (int) Math.round((double) x * Math.sin(angle) + (double) y * Math.cos(angle)),
                (int) Math.round((double) x * Math.cos(angle) - (double) y * Math.sin(angle))
        };
    }

    public static int[] transp(int[] coord, float angle) {
        return transp(coord[0], coord[1], angle);
    }

    public static int rayDown(int x, int z, WorldClient world) {
        for (int y = 255; y >= 0; y--) {
            if (!MappingConstants.AIRABLE.contains(world.getBlockState(new BlockPos(x, y, z)).getBlock())) return y;
        }
        return 0;
    }

    public static int rayUp(int x, int z, WorldClient world) {
        for (int y = 0; y <= 255; y++) {
            if (!MappingConstants.AIRABLE.contains(world.getBlockState(new BlockPos(x, y, z)).getBlock())) return y;
        }
        return 255;
    }

    public static int rayDown(int[] coord, WorldClient world) {
        return rayDown(coord[0], coord[1], world);
    }

    public static int rayUp(int[] coord, WorldClient world) {
        return rayUp(coord[0], coord[1], world);
    }

    public static int[] cellToReal(int[] coord) {
        return cellToReal(coord[0], coord[1]);
    }

    public static int[] cellToReal(int i, int j) {
        return new int[]{
                MappingConstants.MAP_BOUNDS[0].getX()+((MappingConstants.ROOM_SIZE+2)*i),
                MappingConstants.MAP_BOUNDS[0].getZ()+((MappingConstants.ROOM_SIZE+2)*j)
        };
    }

    public static int[] realToCell(double x, double z) {
        return new int[]{
                (int) Math.floor((x-MappingConstants.MAP_BOUNDS[0].getX())/(double) (MappingConstants.ROOM_SIZE+2)),
                (int) Math.floor((z-MappingConstants.MAP_BOUNDS[0].getZ())/(double) (MappingConstants.ROOM_SIZE+2))
        };
    }

    public static int[][] newMap() {
        int[][] mapInst = new int[6][6];
        for (int i = 0; i < mapInst.length; i++) {
            for (int j = 0; j < mapInst.length; j++) {
                mapInst[i][j] = -1;
            }
        }
        return mapInst;
    }
}
