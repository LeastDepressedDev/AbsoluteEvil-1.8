package me.qigan.abse.config;

import me.qigan.abse.config.alg.AlignMidRelative;
import me.qigan.abse.config.alg.AlignRelative;
import me.qigan.abse.config.alg.AlignRelativePercent;
import me.qigan.abse.config.alg.AlignType;
import me.qigan.abse.config.alg.spec.AlignBWA;
import net.minecraftforge.fml.common.Loader;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionConfig {


    /**
     * Line format:
     * 0 - ux
     * 1 - u
     * 2 - hbx
     * 3 - hby
     * 4 - Align type
     * 5+ - arguments
     */

    public static Loc2d decode(String line) {
        String[] segments = line.split(";");
        try {
            return new Loc2d(Integer.parseInt(segments[0]), Integer.parseInt(segments[1]),
                    //Integer.parseInt(segments[2]), Integer.parseInt(segments[3]),
                    AlignType.Type.match(segments[4], segments));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to decode - " + line);
            System.out.println("Cancelling...");
            return null;
        }
    }

    public static String encode(Loc2d loc) {
        String mainStr = loc.ux + ";" + loc.uy + ";" + loc.aligner.hitSelectorSize().width + ";" + loc.aligner.hitSelectorSize().height + ";" + loc.aligner.type().c + ";" + loc.aligner.maxStyle() + ";" + loc.aligner.style();
        switch (loc.aligner.type()) {
            case RELATIVE:
                AlignRelative rel = (AlignRelative) loc.aligner;
                mainStr += ";" + rel.relativeX + ";" + rel.relativeY;
        }
        return mainStr;
    }





    public final Map<String, Loc2d> poses;
    public final AddressedWriter writer;

    public PositionConfig() {
        this.poses = new HashMap<>();
        this.writer = new AddressedWriter(Loader.instance().getConfigDir() + "/abse.pos");
    }

    /**
     * Position config register:
     * Reg format - if (!poses.containsKey("xxx")) this.poses.put("xxx", new Loc2d(nx, ny, aligner));
     */
    public final PositionConfig defts(boolean startup) {
        register("gbmg", new Loc2d(30, 70, new AlignRelativePercent(120, 20, 0)), startup);
        register("module_list", new Loc2d(100, 0, new AlignRelativePercent(100, 100, 0)), startup);
        register("ghost_utils", new Loc2d(0, 40, new AlignRelativePercent(100, 80, 0)), startup);
        register("bwa_display", new Loc2d(0, 100, new AlignBWA()), startup);
        register("bwt_display", new Loc2d(20, 30, new AlignRelativePercent(70, 100, 0)), startup);
        register("fbd_display", new Loc2d(10, 0, new AlignMidRelative(70, 20, 0)), startup);
        register("imp_chat", new Loc2d(100, 100, new AlignRelativePercent(400, 100, 0)), startup);
        register("ingui_disp", new Loc2d(50, 100, new AlignRelativePercent(100, 200, 1)), startup);
        register("m7visz", new Loc2d(30, 40, new AlignRelativePercent(100, 120, 0)), startup);
        register("invtracker", new Loc2d(20, 60, new AlignRelativePercent(100, 120, 0)), startup);
        register("ar_loc", new Loc2d(60, 55, new AlignRelativePercent(100, 250, 0)), startup);
        return this;
    }

    private void register(String key, Loc2d lc, boolean spt) {
        if (!this.poses.containsKey(key) || !spt) this.poses.put(key, lc);
    }

    public final void update() {
        List<AddressedData<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, Loc2d> loc : poses.entrySet()) {
            System.out.println("Updated: " + loc.getKey() + " - " + loc.getValue().aligner.type());
            list.add(new AddressedData<>(loc.getKey(), encode(loc.getValue())));
        }
        this.writer.reset(list);
    }

    public final PositionConfig load() {
        for (AddressedData<String, String> data : this.writer.get()) {
            Loc2d part = decode(data.getObject());
            if (part != null) {
                this.poses.put(data.getNamespace(), decode(data.getObject()));
            }
        }
        return this;
    }

    public Point calc(String key) {
        return this.poses.get(key).get();
    }
}
