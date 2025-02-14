package me.qigan.abse.mapping.routing;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.mapping.Room;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Route {
    private List<BBox> pre = new ArrayList<>();
    private List<BlockPos> path = new ArrayList<>();
    private List<AddressedData<BlockPos, Color>> outlines = new ArrayList<>();
    private List<AddressedData<BlockPos, String>> comments = new ArrayList<>();

    public Route() {

    }

    public final Route blocks(BBox... boxes) {
        pre = new ArrayList<>();
        pre.addAll(Arrays.asList(boxes));
        return this;
    }

    public final Route path(BlockPos... points) {
        path = new ArrayList<>();
        path.addAll(Arrays.asList(points));
        return this;
    }

    @SafeVarargs
    public final Route outlines(AddressedData<BlockPos, Color>... blocks) {
        outlines = new ArrayList<>();
        outlines.addAll(Arrays.asList(blocks));
        return this;
    }

    @SafeVarargs
    public final Route comments(AddressedData<BlockPos, String>... coms) {
        comments = new ArrayList<>();
        comments.addAll(Arrays.asList(coms));
        return this;
    }

    public final void placeRoute(Room room) {
        if (room.id == -1) return;
        if (Index.MAIN_CFG.getBoolVal("remap_blocks")) {
            for (BBox box : this.pre) {
                box.runRelative(room);
            }
        }
        RouteUpdater.update(this, room);
    }

    public List<BBox> getPreBlocks() {
        return pre;
    }

    public List<BlockPos> getPath() {
        return path;
    }

    public List<AddressedData<BlockPos, String>> getComments() {
        return comments;
    }

    public List<AddressedData<BlockPos, Color>> getOutlines() {
        return outlines;
    }
}
