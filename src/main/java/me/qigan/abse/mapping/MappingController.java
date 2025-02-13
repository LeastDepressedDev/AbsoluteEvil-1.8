package me.qigan.abse.mapping;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.mapping.mod.Remapping;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MappingController {

    public static List<BlockPos> debug = new ArrayList<>();
    public static Color debugColor = new Color(36, 165, 183);

    public int[][] roomMapper = null;
    public Set<int[]> loadedChunks = new HashSet<>();
    public Map<Integer, Room> roomReg = new HashMap<>();
    private int nextId = 1;

    public void newDungeon() {
        roomMapper = MappingUtils.newMap();
        roomReg.clear();
        nextId = 1;
        loadedChunks.clear();
    }


    private boolean loadCheck(int [] ax) {
        return loadedChunks.contains(new int[]{ax[0]/16, ax[1]/16}) && loadedChunks.contains(new int[]{(ax[0]+MappingConstants.ROOM_SIZE)/16, ax[1]/16})
                && loadedChunks.contains(new int[]{ax[0]/16, (ax[1]+MappingConstants.ROOM_SIZE)/16}) &&
                loadedChunks.contains(new int[]{(ax[0]+MappingConstants.ROOM_SIZE)/16, (ax[1]+MappingConstants.ROOM_SIZE)/16});
    }



    private void reqRoomCheck(int i, int j, WorldClient world) {
        int[] coord = MappingUtils.cellToReal(i, j);

        if (MappingUtils.rayUp(coord[0]-1, coord[1], world) != 255) {
            int p = roomMapper[i-1][j];
            if (p > 0) roomMapper[i][j] = p;
            return;
        }
        if (MappingUtils.rayUp(coord[0], coord[1]-1, world) != 255) {
            int p = roomMapper[i][j-1];
            if (p > 0) roomMapper[i][j] = p;
            return;
        }
        if (MappingUtils.rayUp(coord[0]+MappingConstants.ROOM_SIZE+1, coord[1], world) != 255) {
            int p = roomMapper[i+1][j];
            if (p > 0) roomMapper[i][j] = p;
            return;
        }
        if (MappingUtils.rayUp(coord[0], coord[1]+MappingConstants.ROOM_SIZE+1, world) != 255) {
            int p = roomMapper[i][j+1];
            if (p > 0) roomMapper[i][j] = p;
            return;
        }

        roomMapper[i][j] = nextId;
        roomReg.put(nextId, new Room(nextId));
        nextId++;
    }

    public void update() {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        for (int i = 0; i < roomMapper.length; i++) {
            for (int j = 0; j < roomMapper.length; j++) {
                if (roomMapper[i][j] == -1) {
                    if (loadCheck(MappingUtils.cellToReal(i, j))) {
                        roomMapper[i][j] = 0;
                    }
                }
            }
        }

        for (int i = 0; i < roomMapper.length; i++) {
            for (int j = 0; j < roomMapper.length; j++) {
                if (roomMapper[i][j] == 0) {
                    reqRoomCheck(i, j, world);
                }
            }
        }
    }

    public int[] getCell(double[] xz) {
        int[] cell = MappingUtils.realToCell(xz[0], xz[1]);
        return new int[]{Math.max(Math.min(cell[0], 6), 0), Math.max(Math.min(cell[1], 6), 0)};
    }

    public int getCellIter(int[] cell) {
        return roomMapper[cell[0]][cell[1]];
    }

    public int[] getPlayerCell() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return getCell(new double[]{player.posX, player.posZ});
    }

    public int getPlayerCellIter() {
        return getCellIter(getPlayerCell());
    }

    public Room getRoom(int[] xz) {
        return roomReg.get(getCellIter(xz));
    }

    public Room getPlayerRoom() {
        return getRoom(getPlayerCell());
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START || Minecraft.getMinecraft().theWorld == null || roomMapper == null) return;
        if (Sync.inDungeon) update();
    }

    @SubscribeEvent
    void onWorldLoad(WorldEvent.Load e) {
        newDungeon();
    }

    @SubscribeEvent
    void onChunkLoad(PacketEvent.ReceiveEvent e) {
        if (e.packet instanceof S21PacketChunkData) {
            S21PacketChunkData packet = (S21PacketChunkData) e.packet;
            loadedChunks.add(new int[]{packet.getChunkX(), packet.getChunkZ()});
        }
    }

    @SubscribeEvent
    void onWorldRender(RenderWorldLastEvent e) {
        if (Minecraft.getMinecraft().theWorld == null || roomMapper == null || !Index.MAIN_CFG.getBoolVal("remap_debug")) return;
        for (BlockPos pos : debug) {
            Esp.autoBox3D(pos, debugColor, 3f, true);
        }
    }

    @SubscribeEvent
    void onOverlayRender(RenderGameOverlayEvent.Text e) {
        if (Minecraft.getMinecraft().theWorld == null || roomMapper == null || !Index.MAIN_CFG.getBoolVal("remap_debug")) return;
        Point pt = new Point(100, 300);
        int[] k = MappingUtils.realToCell(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posZ);
        Esp.drawOverlayString(k[0] + ":" + k[1], pt.x, pt.y-30, Color.cyan, S2Dtype.DEFAULT);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Esp.drawCenteredString((k[0] == i && k[1] == j ? "\u00A7a" : "\u00A7c") + roomMapper[i][j], pt.x+15*i, pt.y+15*j, 0xFFFFFF, S2Dtype.DEFAULT);
            }
        }
        Esp.drawOverlayString(Remapping.createRoomInfo(), pt.x, pt.y+85, Color.cyan, S2Dtype.DEFAULT);
    }
}
