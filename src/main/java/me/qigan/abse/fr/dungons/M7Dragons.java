package me.qigan.abse.fr.dungons;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.gui.overlay.GuiNotifier;
import me.qigan.abse.pathing.Path;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.vecmath.Point3d;
import java.awt.*;
import java.util.*;
import java.util.List;

public class M7Dragons extends Module {

    /* From drag prio ct module

    export const dragInfo = {

    POWER: { dragColor: "RED", renderColor: Renderer.RED, prio: [1, 3], spawned: false, easy: false },
    FLAME: { dragColor: "ORANGE", renderColor: Renderer.GOLD, prio: [2, 1], spawned: false, easy: true },
    ICE: { dragColor: "BLUE", renderColor: Renderer.BLUE, prio: [3, 4], spawned: false, easy: false },
    SOUL: { dragColor: "PURPLE", renderColor: Renderer.LIGHT_PURPLE, prio: [4, 5], spawned: false, easy: true },
    APEX: { dragColor: "GREEN", renderColor: Renderer.GREEN, prio: [5, 2], spawned: false, easy: true },
}*/

    public static enum DRAGON {
        RED(new BlockPos(23, 20, 59), "\u00A7cRed", 1,3, false, new BlockPos(26, 20, 59),
                new BlockPos(27, 6, 53), new float[]{-139, -90}, null, null),
        ORANGE(new BlockPos(89, 21, 56), "\u00A76Orange", 2, 1, true, new BlockPos(85, 21, 56),
                new BlockPos(85, 6, 59), new float[]{-13, -90}, null, null),
        BLUE(new BlockPos(88, 21, 94), "\u00A7bBlue", 3, 4, false, new BlockPos(84, 21, 94),
                new BlockPos(84, 6, 97), new float[]{29.6f, -90}, null, null),
        PURPLE(new BlockPos(56, 20, 129), "\u00A7dPurple", 4, 5, true, new BlockPos(56, 20, 125),
                new BlockPos(60, 8, 124), new float[]{81.7f, -62}, null, null),
        GREEN(new BlockPos(23, 21, 94), "\u00A7aGreen", 5, 2, true, new BlockPos(26, 21, 94),
                new BlockPos(26, 6, 94), new float[]{-180, -90}, null, null)

        ;

        public final String name;
        public final BlockPos pos;
        public final int[] prio;
        public final boolean isEasy;
        public final BlockPos timerLocation;
        public final BlockPos debuffPos;
        public final float[] debuffAngles;
        public final BlockPos stackPos;
        public final float[] stackAngles;


        DRAGON(BlockPos pos, String name, int normalPrio, int easyPrio, boolean isEasy, BlockPos timerLoc,
               BlockPos debuffPos, float[] debuffAngles, BlockPos stackPos, float[] stackAngles) {
            this.pos = pos;
            this.name = name;
            this.isEasy = isEasy;
            this.debuffPos = debuffPos;
            this.debuffAngles = debuffAngles;
            this.stackPos = stackPos;
            this.stackAngles = stackAngles;
            this.prio = new int[]{normalPrio, easyPrio};
            this.timerLocation = timerLoc;
        }

        public static DRAGON match(BlockPos pos) {
            for (DRAGON drag : values()) {
                if (Utils.compare(pos, drag.pos)) return drag;
            }
            return null;
        }
    }

    public static DRAGON locked_dragon = null;

    public static Set<DRAGON> done = new HashSet<>();
    public static Map<DRAGON, Long> spawning = new HashMap<>();
    public static final int SOUND_VOL = 5;

    public static boolean started = false;
    public static boolean first = true;

    public static Path currentPath = null;

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        first = true;
        started = false;
        locked_dragon = null;
        currentPath = null;
        spawning.clear();
        done.clear();
    }

    @SubscribeEvent
    void playSound(PacketEvent.ReceiveEvent e) {
        if (!Sync.inDungeon) return;
        try {
            if (e.packet instanceof S29PacketSoundEffect) {
                S29PacketSoundEffect s = (S29PacketSoundEffect) e.packet;
                BlockPos bp = new BlockPos((int) s.getX(), (int) s.getY(), (int) s.getZ());
                DRAGON drag = DRAGON.match(bp);
                if (drag != null) {
                    if (s.getSoundName().equalsIgnoreCase("random.explode") && started) {
                        if (!done.contains(drag)) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(drag.name + " is done!"));
                            for (int i = 0; i < SOUND_VOL; i++) {
                                Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().thePlayer.playSound("abse:skeet_hit", 1f, 1f));
                            }
                            done.add(drag);
                        }
                    }
                }
            }
            if (e.packet instanceof S2APacketParticles) {
                S2APacketParticles ptl = (S2APacketParticles) e.packet;
                if (ptl.getParticleType() != EnumParticleTypes.ENCHANTMENT_TABLE) return;
                BlockPos bp = new BlockPos((int) ptl.getXCoordinate(), (int) ptl.getYCoordinate(), (int) ptl.getZCoordinate());
                DRAGON drag = check(bp);
                if (drag != null) {
                    if (!spawning.containsKey(drag)) {
                        started = true;
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(drag.name + " is spawning!"));
                        spawning.put(drag, System.currentTimeMillis());
                        if (spawning.size() > 1) first();
                        else if (!first) anonc(drag);
                    }
                }
            }
        } catch (ConcurrentModificationException ex) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7aStupid ConcurrentMod exception hit: Printing to console"));
            ex.printStackTrace();
        }
    }


    /**
     * Force split cuz im lazy af
     */
    private void first() {
        if (!first) return;
        first = false;
        //double p = Sync.getPowerInDungeon();
        List<DRAGON> drags = new ArrayList<>();
        for (Map.Entry<DRAGON, Long> ele : spawning.entrySet()) {
            drags.add(ele.getKey());
        }
        //if (p >= Index.MAIN_CFG.getDoubleVal("m7drags_epower")) {
            int in = 1/*p >= Index.MAIN_CFG.getDoubleVal("m7drags_power") ? 0 : 1*/;
            char c = Sync.getPlayerDungeonClass();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00A7aClass: " + c));
            drags.sort(Comparator.comparingInt(o -> o.prio[in]));
            System.out.println("Drags: ");
            for (DRAGON drag : drags) {
                System.out.println(drag);
            }
            if (Index.MAIN_CFG.getBoolVal("m7drags_manual")) {
                c = Index.MAIN_CFG.getStrVal("m7drags_mps").toUpperCase().charAt(0);
            }
            switch (c) {
                case 'H':
                case 'B':
                case 'M':
                    anonc(drags.get(0));
                    break;
                case 'A':
                case 'T':
                case 'U':
                    anonc(drags.get(1));
                    break;
            }
//        } else {
//            drags.sort(Comparator.comparingInt(o -> o.prio[0]));
//            anonc(drags.get(0));
//        }
    }

    private void anonc(DRAGON dragon) {
        try {
            if (Index.MAIN_CFG.getBoolVal("m7drags_pathing")) {
                BlockPos to = null;
                switch (Sync.getPlayerDungeonClass()) {
                    case 'H':
                    case 'T':
                    case 'M':
                        to = dragon.debuffPos;
                        break;
                    case 'A':
                    case 'B':
                        to = dragon.stackPos;
                }
                if (to != null) currentPath = new Path(Sync.playerPosAsBlockPos(), to).build();
            }
            if (!done.contains(dragon)) GuiNotifier.call(dragon.name, 40, true, 0xFFFFFF);
        } catch (ConcurrentModificationException ex) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7aStupid ConcurrentMod exception hit: Printing to console"));
            ex.printStackTrace();
        }
    }

    DRAGON check(BlockPos pos) {
        if (!(pos.getY() >= 14 && pos.getY() <= 19)) return null;
        if (pos.getX() >= 27 && pos.getX() <= 32) {
            if (pos.getZ() == 59) return DRAGON.RED;
            if (pos.getZ() == 94) return DRAGON.GREEN;
        } else if (pos.getX() >= 79 && pos.getX() <= 85) {
            if (pos.getZ() == 94) return DRAGON.BLUE;
            if (pos.getZ() == 56) return DRAGON.ORANGE;
        } else if (pos.getX() == 56 && (pos.getZ() <= 130 && pos.getZ() >= 110)) return DRAGON.PURPLE;
        return null;
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().thePlayer.posY > 25 || !Sync.inDungeon) return;
        try {
            long timeRN = System.currentTimeMillis();
            for (DRAGON drag : DRAGON.values()) {
                if (!done.contains(drag)) Esp.renderTextInWorld(drag.name, drag.pos, 0xFFFFFF, 1.6, e.partialTicks);
            }
            for (Map.Entry<DRAGON, Long> ele : spawning.entrySet()) {
                long dif = timeRN - ele.getValue();
                if (dif >= 5500) spawning.remove(ele.getKey());
                if (dif <= 5000) Esp.renderTextInWorld(Long.toString(5000 - dif), ele.getKey().timerLocation,
                        Color.green.getRGB(), 4, e.partialTicks);
            }
            if (Index.MAIN_CFG.getBoolVal("m7drags_pathing") && currentPath != null) {
                Esp.autoBox3D(currentPath.from, Color.green, 2f, false);
                Esp.autoBox3D(currentPath.to, Color.green, 2f, false);
                List<Point3d> points = new ArrayList<>();
                for (AddressedData<BlockPos, ?> pos : currentPath.getPosPath()) {
                    points.add(new Point3d(pos.getNamespace().getX() + 0.5, pos.getNamespace().getY() + 1, pos.getNamespace().getZ() + 0.5));
                }
                Esp.drawAsSingleLine(points, Color.cyan, 4f, false);
            }
        } catch (ConcurrentModificationException ex) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7aStupid ConcurrentMod exception hit: Printing to console"));
            ex.printStackTrace();
        }
    }

    @Override
    public String id() {
        return "m7drags";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "M7 dragons";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("m7drags_pathing", "Pathing", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("m7drags_manual", "Fix active class", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("m7drags_mps", "Active class letter", ValType.STRING, "H"));
        //list.add(new SetsData<>("m7drags_epower", "Easy power", ValType.DOUBLE_NUMBER, "17"));
        return list;
    }

    @Override
    public String description() {
        return "Utils to track dragon skips and spawns";
    }
}
