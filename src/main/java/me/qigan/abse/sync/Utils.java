package me.qigan.abse.sync;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import me.qigan.abse.config.AddressedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Utils {

    public static Dictionary<Character, Integer> romanHash = new Hashtable<>();
    public static void setupRoman() {
        romanHash.put('I', 1);
        romanHash.put('V', 5);
        romanHash.put('X', 10);
    }

    public static boolean isPosLinkedToPos(BlockPos pos, BlockPos rel) {
        return Utils.compare(rel.add(0, 1, 0), pos) || Utils.compare(rel.add(0, -1, 0), pos) ||
                Utils.compare(rel.add(1, 0, 0), pos) || Utils.compare(rel.add(-1, 0, 0), pos) ||
                Utils.compare(rel.add(0, 0, 1), pos) || Utils.compare(rel.add(0, 0, -1), pos);
    }

    public static void selectHotbarSlot(int n) {
        Minecraft.getMinecraft().thePlayer.inventory.currentItem = Math.max(0, Math.min(8, n));
    }

    public static BlockPos unify(BlockPos pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int colorLimit(int amt) {
        return Math.min(Math.max(amt, 0), 255);
    }

    public static double precision(double val, int digits) {
        double dd = Math.pow(10, digits);
        return Math.floor(val * dd) / dd;
    }

    public static int romanToInt(String s) {
        char[] arr = s.toCharArray();
        int accumulator = 0;
        for (int i = 0; i < s.length(); i++) {
            if (arr[i] == 'I' && (arr[i + 1] == 'V' || arr[i + 1] == 'X')) {
                accumulator += romanHash.get(arr[i + 1]) - romanHash.get(arr[i]);
                i++;
            } else {
                accumulator += romanHash.get(arr[i]);
            }
        }
        return accumulator;
    }

    public static Dimension scaleDim(Dimension dim, double factorW, double factorH) {
        return new Dimension((int) (dim.width*factorW), (int) (dim.height*factorH));
    }

    public static Point scaleDim(Point pt, double factorW, double factorH) {
        return new Point((int) (pt.x*factorW), (int) (pt.y*factorH));
    }


    public static boolean compare(BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) return false;
        return pos1.getX() == pos2.getX() &&
                pos1.getY() == pos2.getY() &&
                pos1.getZ() == pos2.getZ();
    }

    public static boolean posInDim(BlockPos pos, BlockPos[] dim, boolean in) {
        if (in) {
            return (pos.getX() >= dim[0].getX() && pos.getX() <= dim[1].getX()) &&
                    (pos.getY() >= dim[0].getY() && pos.getY() <= dim[1].getY()) &&
                    (pos.getZ() >= dim[0].getZ() && pos.getZ() <= dim[1].getZ());
        } else {
            return (pos.getX() > dim[0].getX() && pos.getX() < dim[1].getX()) &&
                    (pos.getY() > dim[0].getY() && pos.getY() < dim[1].getY()) &&
                    (pos.getZ() > dim[0].getZ() && pos.getZ() < dim[1].getZ());
        }
    }

    public static boolean posInDim(BlockPos pos, BlockPos[] dim) {
        return posInDim(pos, dim, true);
    }

    public static BlockPos[] rfPos2E(BlockPos pos1, BlockPos pos2) {
        return new BlockPos[]{lowerPos(pos1, pos2), higherPos(pos1, pos2)};
    }

    public static BlockPos lowerPos(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos higherPos(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ()));
    }

    public static boolean pointInDim(Point pt, Dimension dim) {
        return pt.x <= dim.width && pt.y <= dim.height && pt.x >= 0 && pt.y >= 0;
    }

    public static boolean pointInMovedDim(Point pt, Point begin, Dimension dim) {
        return pointInDim(diff(pt, begin), dim);
    }
    
    public static Point diff(Point first,Point second) {
        Point pt = new Point(0, 0);
        pt.x = first.x-second.x;
        pt.y = first.y-second.y;
        return pt;
    }

    public static Vec3 generateEntityHitVec(Entity entity) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        //player.getPositionVector().squareDistanceTo()
        float cbm = entity.getCollisionBorderSize();
        AxisAlignedBB axis = entity.getEntityBoundingBox().expand(cbm, cbm, cbm);

        MovingObjectPosition semiPos = axis.calculateIntercept(player.getPositionEyes(1), entity.getPositionVector().addVector(0, entity.height/2, 0));
        return semiPos == null || semiPos.hitVec == null ? null : semiPos.hitVec.subtract(entity.getPositionVector());
    }

    public static MovingObjectPosition generateBlockHit(BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        WorldClient wrld = Minecraft.getMinecraft().theWorld;
        Vec3 posVec = new Vec3(pos.getX()+0.5d, pos.getY()+0.5d, pos.getZ()+0.5d);

        AxisAlignedBB axis = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock().getCollisionBoundingBox(wrld, pos, wrld.getBlockState(pos));
        return axis.calculateIntercept(player.getPositionEyes(1), posVec);
    }

    /**
     * This two function ARE NOT broken, this is a part of a plan
     */
    public static double createRandomDouble(double up, double down) {
        Random rand = new Random();
        return rand.nextBoolean() ? rand.nextInt()%up : down;
    }

    public static double createRandomDouble(double up, double down, long seed) {
        Random rand = new Random(seed);
        return rand.nextBoolean() ? rand.nextInt()%up : down;
    }
    
    public static Float[] getRotationsTo(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null) {
            return null;
        }

        final double diffX = entity2.posX - entity1.posX;
        final double diffZ = entity2.posZ - entity1.posZ;
        double diffY;

        if (entity2 instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity2;
            diffY = entityLivingBase.posY + entityLivingBase.getEyeHeight() - (entity1.posY + entity1.getEyeHeight());
        } else {
            diffY = (entity2.getEntityBoundingBox().minY + entity2.getEntityBoundingBox().maxY) / 2.0D - (entity1.posY + entity1.getEyeHeight());
        }

        return getRotationsTo(diffX, diffY, diffZ, new float[]{entity1.rotationYaw, entity1.rotationPitch});
    }

    
    public static Float[] getRotationsTo(BlockPos from, BlockPos to, float[] angles) {
        return getRotationsTo(
                to.getX() - from.getX(),
                to.getY() - from.getY(),
                to.getZ() - from.getZ(),
                angles
        );
    }

    
    public static Float[] getRotationsTo(final double diffX, final double diffY, final double diffZ, float[] angles) {
        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        final float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
        return new Float[] {angles[0] + MathHelper.wrapAngleTo180_float(yaw - angles[0]), angles[1] + MathHelper.wrapAngleTo180_float(pitch - angles[1]) };
    }

    public static <K, V> List<AddressedData<K, V>> mapToAddressedDataList(Map<K, V> map) {
        List<AddressedData<K, V>> list = new ArrayList<>();
        for (Map.Entry<K, V> kv : map.entrySet()) {
            list.add(new AddressedData<>(kv.getKey(), kv.getValue()));
        }
        return list;
    }

    /**
     * Return `-1` if item don't have a color
     */
    public static int getItemColor(ItemStack stack) {
        //.serializeNBT().getCompoundTag("tag").getCompoundTag("display").getInteger("color")
        if (stack == null) return -1;
        NBTTagCompound tag = stack.serializeNBT();
        if (tag.hasKey("tag")) {
            tag = tag.getCompoundTag("tag");
            if (tag.hasKey("display")) {
                tag = tag.getCompoundTag("display");
                if (tag.hasKey("color")) {
                    return tag.getInteger("color");
                }
            }
        }
        return -1;
    }

    public static String cleanSB(String ln) {
        if (ln == null) return "";
        char[] nvString = StringUtils.stripControlCodes(ln).toCharArray();
        StringBuilder cleaned = new StringBuilder();

        for (char c : nvString) {
            if ((int) c > 20 && (int) c < 127) {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }

    public static NBTTagCompound getSbData(ItemStack stack) {
        try {
            return stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes");
        } catch (Exception e) {
            return new NBTTagCompound();
        }
    }

    public static NBTTagCompound getDisplay(ItemStack stack) {
        if (stack == null) return null;
        return ((stack.hasTagCompound() && stack.getTagCompound().hasKey("display"))) ? stack.getTagCompound().getCompoundTag("display") : null;
    }


    public static List<String> getDataFromTab() {
        List<String> result = new ArrayList<String>();
        for (NetworkPlayerInfo ev: Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
            String strm = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(ev).toLowerCase();
            result.add(strm);
        }
        return result;
    }

    public static IChatComponent getTabInfo() {
        return ReflectionHelper.getPrivateValue(GuiPlayerTabOverlay.class, Minecraft.getMinecraft().ingameGUI.getTabList(), "footer");
    }

    public static List<String> getScoreboard() {
        List<String> lines = new ArrayList<String>();
        if (Minecraft.getMinecraft().theWorld == null) return lines;
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = new ArrayList<Score>();
        for (Score scr : scores) {
            if (scr != null && scr.getPlayerName() != null && !scr.getPlayerName().startsWith("#")) list.add(scr);
        }

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(cleanSB(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName())));
        }

        return lines;
    }
}
