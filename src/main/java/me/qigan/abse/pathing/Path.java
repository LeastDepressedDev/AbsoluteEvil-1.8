package me.qigan.abse.pathing;

import me.qigan.abse.config.AddressedData;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.*;

public class Path{

    protected final int limit;

    protected final Set<BlockPos> usedGeneric = new HashSet<>();
    protected final Float pitchOverride = null;

    protected final Map<BlockPos, Integer> map = new HashMap<>();

    public final BlockPos from;
    public BlockPos to;

    protected final List<AddressedData<BlockPos, Integer>> path = new ArrayList<>();
    private boolean failed = true;

    public boolean allowDiagonalWave = true;
    public boolean sprint = false;


    //Abstract point mec
    public boolean absPt = false;
    private double distancePt = Double.MAX_VALUE;
    private BlockPos newPoint = null;


    public Path(BlockPos from, BlockPos to) {
        this.from = Utils.unify(from);
        this.to = Utils.unify(to);
        this.limit = 140;
    }

    public Path(BlockPos from, BlockPos to, int lim) {
        this.from = Utils.unify(from);
        this.to = Utils.unify(to);
        this.limit = lim;
    }


    public Path copyParam(Path pth) {
        this.absPt = pth.absPt;
        this.sprint = pth.sprint;
        this.allowDiagonalWave = pth.allowDiagonalWave;
        return this;
    }

    public Path allowAbstractPoint() {
        this.absPt = true;
        return this;
    }

    public Path disallowDiagonalWave() {
        this.allowDiagonalWave = false;
        return this;
    }

    public Path allowSprint() {
        this.sprint = true;
        return this;
    }


    //Wave algoritm
    public Path build() {
        if (Utils.compare(from, to)) return this;
        Set<BlockPos> wave = new HashSet<>();
        wave.add(from);
        for (int i = 0; i < limit; i++) {
            wave = createWaveStep(wave, i);
            if (wave.contains(to)) {
                failed = false;
                bph(to);
                path.add(new AddressedData<>(to, 1));
                break;
            }
        }
        if (absPt && newPoint != null) {
            failed = false;
            this.to = newPoint;
            bph(to);
            path.add(new AddressedData<>(to, 1));
        }
        return this;
    }

    private Set<BlockPos> createWaveStep(Set<BlockPos> poses, int val) {
        Set<BlockPos> wave = new HashSet<>();
        for (BlockPos pos : poses) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (y == 1 && x != 0 && z != 0) continue;
                        if (x != 0 && z != 0 && !allowDiagonalWave) continue;
                        BlockPos subPos = pos.add(x, y, z);
                        if (usedGeneric.contains(subPos) || poses.contains(subPos)) continue;
                        if (!MappingConstants.NOT_COLLIDABLE.contains(Minecraft.getMinecraft().theWorld.getBlockState(subPos.add(0, -1, 0)).getBlock())
                                && MappingConstants.NOT_COLLIDABLE.contains(Minecraft.getMinecraft().theWorld.getBlockState(subPos.add(0, 1, 0)).getBlock())
                                && MappingConstants.NOT_COLLIDABLE.contains(Minecraft.getMinecraft().theWorld.getBlockState(subPos).getBlock())) {
                            wave.add(subPos);
                            map.put(subPos, val);
                            usedGeneric.add(subPos);
                            if (absPt && subPos.distanceSq(to) < distancePt) {
                                distancePt = subPos.distanceSq(to);
                                newPoint = subPos;
                            }
                        }
                    }
                }
            }
        }
        return wave;
    }

    private void bph(BlockPos pos) {
        if (pos == from) {
            path.add(new AddressedData<>(pos, 1));
            return;
        }
        double distSQ = limit;
        BlockPos dl = null;
        int vars = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos subPos = pos.add(x, y, z);
                    if (map.containsKey(subPos) && map.get(subPos) < map.get(pos)) {
                        vars++;
                        if (pos.distanceSq(subPos) < distSQ) {
                            distSQ = pos.distanceSq(subPos);
                            dl = subPos;
                        }
                    }
                }
            }
        }
        if (dl != null) {
            bph(dl);
            path.add(new AddressedData<>(dl, vars));
        }
        else fail();
    }

    private void fail() {
        failed = true;
    }

    public final boolean isFailed() {
        return failed;
    }

    public List<AddressedData<BlockPos, Integer>> getPosPath() {
        return path;
    }
}
