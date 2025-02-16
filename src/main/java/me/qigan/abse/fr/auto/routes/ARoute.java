package me.qigan.abse.fr.auto.routes;

import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class ARoute {
    public int step = 0;
    public List<ARElement> elems = new ArrayList<>();

    public boolean update(TickEvent.ClientTickEvent e) {
        if (step >= elems.size()) return true;
        elems.get(step).tick(e);

        if (elems.get(step).next()) step++;
        return false;
    }

    public ARElement stepElement() {
        return elems.get(step);
    }
}
