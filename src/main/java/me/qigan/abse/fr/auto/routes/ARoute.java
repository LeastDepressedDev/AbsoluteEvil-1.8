package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.fr.auto.routes.elems.ARElement;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class ARoute {

    public static enum Referer {
        DUNGEON, //Catacombs on clear
        FLOOR, //Specific catacombs floor
        GENERAL //Works everywhere
    }

    public int step = 0;
    public List<ARElement> elems = new ArrayList<>();
    public final Referer referer;
    public final int ref_id;
    public final BlockPos startingPos;

    public ARoute(Referer ref, int id, BlockPos sPos) {
        this.referer = ref;
        this.ref_id = id;
        this.startingPos = sPos;
    }

    public boolean update(TickEvent.ClientTickEvent e) {
        if (step >= elems.size()) return true;
        elems.get(step).tick(e, this);

        if (elems.get(step).next()) step++;
        return false;
    }

    public ARElement stepElement() {
        return elems.get(step);
    }
}
