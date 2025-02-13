package me.qigan.abse.mapping;

import me.qigan.abse.config.AddressedData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {

    public enum Shape {
        UNKNOWN,
        r1X1,
        r1X2,
        r1X3,
        r1X4,
        r2X2,
        rL
    }

    public enum Rotation {
        UNKNOWN(0f),
        SOUTH(0f),
        WEST(-90f),
        NORTH(-180f),
        EAST(-270f);

        public final float angle;

        Rotation(float angle) {
            this.angle = angle;
        }
    }

    public enum Type {
        UNKNOWN,
        SPAWN,
        REGULAR,
        BOSS,
        BLOOD,
        PUZZLE,
        TRAP,
        FAIRY
    }

    /*
     * Big Fucking space to not get lost in this stupid af god
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */

    public List<int[]> segments;
    public int[] core = new int[]{-1, -1};
    public Type type = Type.UNKNOWN;
    public Shape shape = Shape.UNKNOWN;
    public Rotation rotation = Rotation.UNKNOWN;
    public int id = -1;
    public final int iter;

    private int height = 0;

    public Room(int iterN) {
        this.segments = new ArrayList<>();
        this.iter = iterN;
    }

    private boolean updateCore(int[] seg) {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        int[] coord = MappingUtils.cellToReal(seg);
        if (world.getBlockState(new BlockPos(coord[0], height, coord[1]))
                .getBlock() == Blocks.lapis_block &&
                world.getBlockState(new BlockPos(coord[0] + MappingConstants.ROOM_SIZE, height, coord[1] + MappingConstants.ROOM_SIZE))
                        .getBlock() == Blocks.lapis_block &&
                world.getBlockState(new BlockPos(coord[0], height, coord[1] + MappingConstants.ROOM_SIZE))
                        .getBlock() == Blocks.lapis_block &&
                world.getBlockState(new BlockPos(coord[0] + MappingConstants.ROOM_SIZE, height, coord[1]))
                        .getBlock() == Blocks.lapis_block) {
            this.type = Type.FAIRY;
            this.rotation = Rotation.EAST;
            this.core = seg;
            return true;
        }

        if (world.getBlockState(new BlockPos(coord[0] + MappingConstants.ROOM_SIZE, height, coord[1] + 3))
                .getBlock() == Blocks.stained_hardened_clay) rotation = Rotation.SOUTH;
        else if (world.getBlockState(new BlockPos(coord[0] + MappingConstants.ROOM_SIZE - 3, height, coord[1] + MappingConstants.ROOM_SIZE))
                .getBlock() == Blocks.stained_hardened_clay) rotation = Rotation.WEST;
        else if (world.getBlockState(new BlockPos(coord[0], height, coord[1] + MappingConstants.ROOM_SIZE - 3))
                .getBlock() == Blocks.stained_hardened_clay) rotation = Rotation.NORTH;
        else if (world.getBlockState(new BlockPos(coord[0] + 3, height, coord[1]))
                .getBlock() == Blocks.stained_hardened_clay) rotation = Rotation.EAST;

        if (rotation == Rotation.UNKNOWN) {
            return false;
        } else {
            defineRoomType();
            return true;
        }
    }

    public BlockPos transformInnerCoordinate(BlockPos pos) {
        int[] coord = MappingUtils.transp(pos.getZ() - 15, pos.getX() - 15, this.rotation.angle);
        int[] cellC = MappingUtils.cellToReal(this.core);
        return new BlockPos(coord[0] + cellC[0] + 15, pos.getY(), coord[1] + cellC[1] + 15);
    }

    public void defineRoomType() {
        if (shape != Shape.r1X1) return;
        BlockPos pos = new BlockPos(21, height, 0);
        for (int i = 0; i < 5; i++) {
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(this.transformInnerCoordinate(pos.add(0, 0, i))).getBlock();
            if (block == Blocks.redstone_block) this.type = Type.BLOOD;
            else if (block == Blocks.emerald_ore) this.type = Type.PUZZLE;
            else if (block == Blocks.netherrack) this.type = Type.BOSS;
            else if (block == Blocks.tnt) this.type = Type.TRAP;
        }
    }

    public void add(int[] seg) {
        segments.add(seg);
        if (core[0] == -1) {
            if (updateCore(seg)) {
                this.height = MappingUtils.rayDown(MappingUtils.cellToReal(seg), Minecraft.getMinecraft().theWorld);
            }
        } else if (this.id == -1) {
            this.id = Rooms.match(this);
            if (this.id > -1) {
                RoomTemplate tmpl = Rooms.rooms.get(this.id);
                this.shape = tmpl.getShape();
            }
        }
    }

    public Rotation getRotation() {
        return rotation;
    }

    public int getId() {
        return id;
    }

    public Shape getShape() {
        return shape;
    }

    public Type getType() {
        return type;
    }

    public int getHeight() {
        return height;
    }
}
