package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.fr.auto.routes.elems.ARENull;
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

    public String name = "";
    public String author = "";

    public int step = 0;
    public List<ARElement> elems = new ArrayList<>();
    public final Referer referer;
    public final int ref_id;
    public final BlockPos startingPos;

    private long force = 0;

    public ARoute(Referer ref, int id, BlockPos sPos) {
        this.referer = ref;
        this.ref_id = id;
        this.startingPos = sPos;
    }

    public boolean update(TickEvent.ClientTickEvent e) {
        if (step >= elems.size()) return true;
        if (System.currentTimeMillis()-force > Index.MAIN_CFG.getIntVal("ar_wait")) {
            elems.get(step).tick(e, this);

            if (elems.get(step).next()) {
                System.out.println(Integer.toString(step));
                force = System.currentTimeMillis();
                step++;
            }
        }
        return false;
    }

    public void reset() {
        step = 0;
        for (ARElement elem : elems) {
            elem.reset(this);
        }
    }

    public ARElement stepElement() {
        if (step >= elems.size()) return new ARENull();
        return elems.get(step);
    }

    public void add(ARElement element) {
        this.elems.add(element);
    }
}
