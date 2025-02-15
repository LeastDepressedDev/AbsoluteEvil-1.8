package me.qigan.abse.mapping;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.mapping.mod.Remapping;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
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
    public Set<String> loadedChunks = new HashSet<>();
    public Map<Integer, Room> roomReg = new HashMap<>();
    private int nextId = 1;

    private int tick = 0;

    public void newDungeon() {
        roomMapper = MappingUtils.newMap();
        roomReg.clear();
        nextId = 1;
        loadedChunks.clear();
        tick = 0;
    }


    private boolean loadCheck(int [] ax) {
//        System.out.println(loadedChunks.contains(new AddressedData<>(ax[0]/16, ax[1]/16)) + " " +
//                loadedChunks.contains(new AddressedData<>((ax[0]+MappingConstants.ROOM_SIZE)/16, ax[1]/16)) + " " +
//                loadedChunks.contains(new AddressedData<>(ax[0]/16, (ax[1]+MappingConstants.ROOM_SIZE)/16)) + " " +
//                loadedChunks.contains(new AddressedData<>((ax[0]+MappingConstants.ROOM_SIZE)/16, (ax[1]+MappingConstants.ROOM_SIZE)/16)));
        return loadedChunks.contains(ax[0]/16 + " " + ax[1]/16) &&
                loadedChunks.contains((ax[0]+MappingConstants.ROOM_SIZE)/16 + " " + ax[1]/16)
                && loadedChunks.contains(ax[0]/16 + " " + (ax[1]+MappingConstants.ROOM_SIZE)/16) &&
                loadedChunks.contains((ax[0]+MappingConstants.ROOM_SIZE)/16 + " " + (ax[1]+MappingConstants.ROOM_SIZE)/16);
    }



    private void reqRoomCheck(int i, int j, WorldClient world) {
        int[] coord = MappingUtils.cellToReal(i, j);

        //if (!(roomReg.get(roomMapper[i][j]) != null && roomReg.get(roomMapper[i][j]).core[0] != -1)) {
            if (MappingUtils.rayDown(coord[0] - 1, coord[1], world) != 0 && i - 1 > 0) {
                int p = roomMapper[i - 1][j];
                if (p > 0 && p != roomMapper[i][j]) {
                    roomMapper[i][j] = p;
                    roomReg.get(p).add(new int[]{i, j});
                    return;
                }
            }
            if (MappingUtils.rayDown(coord[0], coord[1] - 1, world) != 0 && j - 1 > 0) {
                int p = roomMapper[i][j - 1];
                if (p > 0 && p != roomMapper[i][j]) {
                    roomMapper[i][j] = p;
                    roomReg.get(p).add(new int[]{i, j});
                    return;
                }
            }
            if (MappingUtils.rayDown(coord[0] + MappingConstants.ROOM_SIZE + 1, coord[1], world) != 0 && i + 1 < 6) {
                int p = roomMapper[i + 1][j];
                if (p > 0 && p != roomMapper[i][j]) {
                    roomMapper[i][j] = p;
                    roomReg.get(p).add(new int[]{i, j});
                    return;
                }
            }
            if (MappingUtils.rayDown(coord[0], coord[1] + MappingConstants.ROOM_SIZE + 1, world) != 0 && j + 1 < 6) {
                int p = roomMapper[i][j + 1];
                if (p > 0 && p != roomMapper[i][j]) {
                    roomMapper[i][j] = p;
                    roomReg.get(p).add(new int[]{i, j});
                    return;
                }
            }
        //}

        if (roomMapper[i][j] == 0) {
            roomMapper[i][j] = nextId;
            roomReg.put(nextId, new Room(nextId));
            roomReg.get(nextId).add(new int[]{i, j});
            nextId++;
            return;
        }

        Room rm = roomReg.get(roomMapper[i][j]);
        if (rm.core[0] == -1 || rm.id == -1) {
            rm.add(new int[]{i, j});
        }
    }

    public void update() {
        if (!Utils.posInDim(Sync.playerPosAsBlockPos(), MappingConstants.MAP_BOUNDS)) return;
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
                if (roomMapper[i][j] > -1) {
                    reqRoomCheck(i, j, Minecraft.getMinecraft().theWorld);
                }
            }
        }
    }

    public int[] getCell(double[] xz) {
        int[] cell = MappingUtils.realToCell(xz[0], xz[1]);
        return new int[]{Math.max(Math.min(cell[0], 6), 0), Math.max(Math.min(cell[1], 6), 0)};
    }

    public int getCellIter(int[] cell) {
        if (cell[0] > 5 || cell[0] < 0 || cell[1] > 5 || cell[1] < 0) return -1;
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

    private int calcTick() {
        int d = Index.MAIN_CFG.getIntVal("remap_tick");
        if (Index.MAIN_CFG.getBoolVal("remap_opt")) {
            if (nextId > 17) d += 6;
        }
        return d;
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END || Minecraft.getMinecraft().theWorld == null || roomMapper == null) return;
        if (Sync.inDungeon) {
            if (tick <= 0) {
                try {
                    update();
                    tick = calcTick();

                    if (Index.MAIN_CFG.getBoolVal("remap")) {
                        Room room = getPlayerRoom();
                        if (room != null && room.id != -1) {
                            Rooms.routes.get(room.id).placeRoute(room);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else tick--;
        }
    }

    @SubscribeEvent
    void onWorldLoad(WorldEvent.Load e) {
        newDungeon();
    }

    @SubscribeEvent
    void onChunkLoad(PacketEvent.ReceiveEvent e) {
        if (e.packet instanceof S21PacketChunkData) {
            S21PacketChunkData packet = (S21PacketChunkData) e.packet;
//            if (Index.MAIN_CFG.getBoolVal("remap_debug")) {
//                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[MAP DEBUG] " + packet.getChunkX() + " " + packet.getChunkZ()));
//            }
            loadedChunks.add(packet.getChunkX() + " " + packet.getChunkZ());
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
        try {
            Point pt = new Point(100, 300);
            int[] k = MappingUtils.realToCell(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posZ);
            Esp.drawOverlayString(k[0] + ":" + k[1], pt.x, pt.y - 30, Color.cyan, S2Dtype.DEFAULT);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Esp.drawCenteredString((k[0] == i && k[1] == j ? "\u00A7a" : "\u00A7c") + roomMapper[i][j], pt.x + 15 * i, pt.y + 15 * j, 0xFFFFFF, S2Dtype.DEFAULT);
                }
            }
            Esp.drawOverlayString(Remapping.createRoomInfo(), pt.x, pt.y + 85, Color.cyan, S2Dtype.DEFAULT);
        } catch (Exception ex) {

        }
    }
}
