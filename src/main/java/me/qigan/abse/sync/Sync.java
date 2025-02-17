package me.qigan.abse.sync;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import me.qigan.abse.Index;
import me.qigan.abse.crp.Experimental;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.mapping.MappingConstants;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

public class Sync {
//The Catacombs

    public static boolean inDungeon = false;
    public static boolean inKuudra = false;
    public static final int tickr = 40;
    public static int tick = 0;
    private static char cls = 'U';

    public static BlockPos playerPosAsBlockPos() {
        return new BlockPos(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
    }

    //TODO: Make it better
    public static char getPlayerDungeonClass() {
        if (!Sync.inDungeon) return 'N';
        return cls;
    }

    /**
     * This function is very thoughtfully and pure copy pasted from MC 1.8.9 source code :skull:
     */
    protected static Vec3 getVectorForRotation(float p_getVectorForRotation_1_, float p_getVectorForRotation_2_) {
        float f = MathHelper.cos(-p_getVectorForRotation_2_ * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-p_getVectorForRotation_2_ * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-p_getVectorForRotation_1_ * 0.017453292F);
        float f3 = MathHelper.sin(-p_getVectorForRotation_1_ * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    public static MovingObjectPosition rayTrace(double p_rayTrace_1_, float p_rayTrace_3_, float[] angular, BlockPos exc) {
        Vec3 vec3 = Minecraft.getMinecraft().thePlayer.getPositionEyes(p_rayTrace_3_);
        Vec3 vec31 = getCustomLook(p_rayTrace_3_, new float[]{angular[0], angular[1], angular[0], angular[1]});
        Vec3 vec32 = vec3.addVector(vec31.xCoord * p_rayTrace_1_, vec31.yCoord * p_rayTrace_1_, vec31.zCoord * p_rayTrace_1_);
        return rayTraceThroughBlocks(Minecraft.getMinecraft().theWorld, exc, vec3, vec32, false, false, true);
    }

    public static MovingObjectPosition rayTraceThroughBlocks(World world, BlockPos excluder, Vec3 p_rayTraceBlocks_1_, Vec3 p_rayTraceBlocks_2_, boolean p_rayTraceBlocks_3_, boolean p_rayTraceBlocks_4_, boolean p_rayTraceBlocks_5_) {
        if (!Double.isNaN(p_rayTraceBlocks_1_.xCoord) && !Double.isNaN(p_rayTraceBlocks_1_.yCoord) && !Double.isNaN(p_rayTraceBlocks_1_.zCoord)) {
            if (!Double.isNaN(p_rayTraceBlocks_2_.xCoord) && !Double.isNaN(p_rayTraceBlocks_2_.yCoord) && !Double.isNaN(p_rayTraceBlocks_2_.zCoord)) {
                int i = MathHelper.floor_double(p_rayTraceBlocks_2_.xCoord);
                int j = MathHelper.floor_double(p_rayTraceBlocks_2_.yCoord);
                int k = MathHelper.floor_double(p_rayTraceBlocks_2_.zCoord);
                int l = MathHelper.floor_double(p_rayTraceBlocks_1_.xCoord);
                int i1 = MathHelper.floor_double(p_rayTraceBlocks_1_.yCoord);
                int j1 = MathHelper.floor_double(p_rayTraceBlocks_1_.zCoord);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                MovingObjectPosition movingobjectposition2;
                if ((!p_rayTraceBlocks_4_ || block.getCollisionBoundingBox(world, blockpos, iblockstate) != null) && block.canCollideCheck(iblockstate, p_rayTraceBlocks_3_)) {
                    movingobjectposition2 = block.collisionRayTrace(world, blockpos, p_rayTraceBlocks_1_, p_rayTraceBlocks_2_);
                    if (movingobjectposition2 != null) {
                        return movingobjectposition2;
                    }
                }

                movingobjectposition2 = null;
                int k1 = 200;

                while(k1-- >= 0) {
                    if (Double.isNaN(p_rayTraceBlocks_1_.xCoord) || Double.isNaN(p_rayTraceBlocks_1_.yCoord) || Double.isNaN(p_rayTraceBlocks_1_.zCoord)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return p_rayTraceBlocks_5_ ? movingobjectposition2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0;
                    double d1 = 999.0;
                    double d2 = 999.0;
                    if (i > l) {
                        d0 = (double)l + 1.0;
                    } else if (i < l) {
                        d0 = (double)l + 0.0;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double)i1 + 1.0;
                    } else if (j < i1) {
                        d1 = (double)i1 + 0.0;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double)j1 + 1.0;
                    } else if (k < j1) {
                        d2 = (double)j1 + 0.0;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0;
                    double d4 = 999.0;
                    double d5 = 999.0;
                    double d6 = p_rayTraceBlocks_2_.xCoord - p_rayTraceBlocks_1_.xCoord;
                    double d7 = p_rayTraceBlocks_2_.yCoord - p_rayTraceBlocks_1_.yCoord;
                    double d8 = p_rayTraceBlocks_2_.zCoord - p_rayTraceBlocks_1_.zCoord;
                    if (flag2) {
                        d3 = (d0 - p_rayTraceBlocks_1_.xCoord) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - p_rayTraceBlocks_1_.yCoord) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - p_rayTraceBlocks_1_.zCoord) / d8;
                    }

                    if (d3 == -0.0) {
                        d3 = -1.0E-4;
                    }

                    if (d4 == -0.0) {
                        d4 = -1.0E-4;
                    }

                    if (d5 == -0.0) {
                        d5 = -1.0E-4;
                    }

                    EnumFacing enumfacing;
                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        p_rayTraceBlocks_1_ = new Vec3(d0, p_rayTraceBlocks_1_.yCoord + d7 * d3, p_rayTraceBlocks_1_.zCoord + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        p_rayTraceBlocks_1_ = new Vec3(p_rayTraceBlocks_1_.xCoord + d6 * d4, d1, p_rayTraceBlocks_1_.zCoord + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        p_rayTraceBlocks_1_ = new Vec3(p_rayTraceBlocks_1_.xCoord + d6 * d5, p_rayTraceBlocks_1_.yCoord + d7 * d5, d2);
                    }

                    l = MathHelper.floor_double(p_rayTraceBlocks_1_.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor_double(p_rayTraceBlocks_1_.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor_double(p_rayTraceBlocks_1_.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();
                    System.out.println(blockpos.toString());
                    if (!p_rayTraceBlocks_4_ || block1.getCollisionBoundingBox(world, blockpos, iblockstate1) != null) {
                        if (block1.canCollideCheck(iblockstate1, p_rayTraceBlocks_3_)) {
                            MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(world, blockpos, p_rayTraceBlocks_1_, p_rayTraceBlocks_2_);
                            if (movingobjectposition1 != null && Utils.compare(blockpos, excluder)) {
                                return movingobjectposition1;
                            }
                        } else {
                            movingobjectposition2 = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, p_rayTraceBlocks_1_, enumfacing, blockpos);
                        }
                    }
                }

                return p_rayTraceBlocks_5_ ? movingobjectposition2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * angle2Set = {yaw, pitch, prevYaw, prevPitch}
     */
    public static Vec3 getCustomLook(float sigma, float[] angle2Set){
        if (sigma == 1.0F) {
            return getVectorForRotation(angle2Set[1], angle2Set[0]);
        } else {
            float f = angle2Set[3] + (angle2Set[1] - angle2Set[3]) * sigma;
            float f1 = angle2Set[2] + (angle2Set[0] - angle2Set[2]) * sigma;
            return getVectorForRotation(f, f1);
        }
    }

    public static void doBlockRightClick(BlockPos pos) {
        if (Index.MAIN_CFG.getBoolVal("rage_lock")) return;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
//        Drunk brain rot code
        if (Minecraft.getMinecraft().thePlayer.getDistance(pos.getX()+0.5d, pos.getY()+0.5d-player.eyeHeight, pos.getZ()+0.5d) >
                Minecraft.getMinecraft().playerController.getBlockReachDistance()+.45d) return;
//        Float[] rots = Utils.getRotationsTo(pos.getX()+0.5d-player.posX, pos.getY()+0.5d-player.posY-player.eyeHeight, pos.getZ()+0.5d-player.posZ, new float[]{
//                player.rotationYaw, player.rotationPitch
//        });
//        if (rots == null || rots[0] == null || rots[1] == null) return;
//
//        MovingObjectPosition semiPos = rayTrace(Minecraft.getMinecraft().playerController.getBlockReachDistance(), 1f, new float[]{
//                rots[0], rots[1], rots[0], rots[1]
//        }, pos);
//        if (semiPos == null || semiPos.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) return;
//        System.out.println(String.format("%f %f %f", semiPos.hitVec.xCoord, semiPos.hitVec.yCoord, semiPos.hitVec.zCoord));
        MovingObjectPosition semiPos = Utils.generateBlockHit(pos);
        if (semiPos == null || semiPos.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) return;
        System.out.println(String.format("%f %f %f", semiPos.hitVec.xCoord, semiPos.hitVec.yCoord, semiPos.hitVec.zCoord));
        if (Minecraft.getMinecraft().playerController.onPlayerRightClick(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld,
                Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem(), pos,
                semiPos.sideHit, semiPos.hitVec)) {
            Minecraft.getMinecraft().thePlayer.swingItem();
        }
    }

    //--
    public static void doBlockLeftClick(BlockPos pos) {
        if (Index.MAIN_CFG.getBoolVal("rage_lock")) return;
        if (Minecraft.getMinecraft().thePlayer.getDistance(pos.getX(), pos.getY()-1, pos.getZ()) >
                Minecraft.getMinecraft().playerController.getBlockReachDistance()-.15d) return;
        Minecraft.getMinecraft().thePlayer.swingItem();
        Minecraft.getMinecraft().playerController.clickBlock(pos, EnumFacing.fromAngle(Minecraft.getMinecraft().thePlayer.rotationYawHead));
    }

    public static void ovrCheck() {
        for (String str : Utils.getScoreboard()) {
            if (str.startsWith("[")) {
                String nstr = Utils.cleanSB(str);
                if (nstr.contains(Minecraft.getMinecraft().getSession().getUsername())) {
                    cls = nstr.toCharArray()[1];
                }
            }
            if (str.contains("The Catacombs")) {
                inDungeon = true;
                return;
            } else if (str.contains("Time Elapsed:")) {
                for (String subl : Utils.getScoreboard()) {
                    if (subl.contains("Cleared:")) {
                        inDungeon = true;
                        return;
                    }
                }
            } else if (str.contains("Kuudra's Hollow")) {
                inKuudra = true;
                return;
            }
        }
        inDungeon = false;
        inKuudra = false;
    }

    public static EntityPlayerSP player() {
        return Minecraft.getMinecraft().thePlayer;
    }

    /**
     * Checks whether the Skyblock Dungeon Map is in the player's hotbar
     * @return whether the map exists
     */
    public static boolean mapExists() {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack mapSlot = mc.thePlayer.inventory.getStackInSlot(8); //check last slot where map should be
        if (mapSlot == null || mapSlot.getItem() != Items.filled_map || !mapSlot.hasDisplayName()) return false; //make sure it is a map, not SB Menu or Spirit Bow, etc
        return mapSlot.getDisplayName().contains("Magical Map");
    }

    /**
     * Reads the hotbar map and converts it into a 2D Integer array of RGB colors which can be used by the rest of the
     * code
     *
     * @return null if map not found, otherwise 128x128 Array of the RGB Integer colors of each point on the map
     */
    public static Integer[][] updatedMap() {
        if (!mapExists()) return null; //make sure map exists
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack mapSlot = mc.thePlayer.inventory.getStackInSlot(8); //get map ItemStack
        MapData mapData = Items.filled_map.getMapData(mapSlot, mc.theWorld);
        if(mapData == null) return null;
        Integer[][] map = new Integer[128][128];

        //for loop code modified from net.minecraft.client.gui.MapItemRenderer.updateMapTexture()
        for (int i = 0; i < 16384; ++i) {
            int x = i % 128; //get x coordinate of pixel being read
            int y = i / 128; //get y coordinate of pixel being read
            int j = mapData.colors[i] & 255;
            int rgba;
            if (j / 4 == 0) {
                rgba = (i + i / 128 & 1) * 8 + 16 << 24;
            } else {
                rgba = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
            }
            map[x][y] = rgba & 0x00FFFFFF; //get rgb value from rgba
        }

        return map;
    }


    /**
     * This function finds the coordinates of the NW and NE corners of the entrance room on the hotbar map. This is
     * later used to determine the size of the room grid on the hotbar map. Different floors have slightly different
     * pixel widths of the rooms, so it is important for the mod to be able to identify the location and size of various
     * portions of the room grid. Since all rooms within a floor are the same size on the hotbar map and since the
     * entrance room is always there on the hotbar map, we get two corners from the entrance room to determine the
     * scaling of the map as soon as the player enters.
     *
     * This function works by iterating through the map and looking for a green entrance room pixel. Once it finds one
     * and determines that the map pixel above is a blank spot, it checks for map pixels on the left and right side.
     *
     * @return `entranceMapCorners[0]` is the coordinate of the left NW corner and `entranceMapCorners[1]` is the
     * coordinate of the right NE corner
     */
    public static Point[] entranceMapCorners(Integer[][] map) {
        if (map == null) return null;
        Point[] corners = new Point[2];

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                if (map[x][y] != null && map[x][y] == 31744 && map[x][y-1] != null && map[x][y-1] == 0) { //check for Green entrance room pixels and make sure row above is blank
                    if (map[x - 1][y] != null && map[x - 1][y] == 0) {
                        corners[0] = new Point(x, y); //Left corner
                    } else if (map[x + 1][y] != null && map[x + 1][y] == 0) {
                        corners[1] = new Point(x, y); //Right Corner
                    }
                }
            }
            if (corners[0] != null && corners[1] != null) break;
        }
        return corners;
    }

    public static boolean isClear() {
        return inDungeon && Utils.posInDim(Sync.playerPosAsBlockPos(), MappingConstants.MAP_BOUNDS);
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (tick >= tickr) {
            tick = 0;
            ovrCheck();
        }
        else tick++;
    }
}
