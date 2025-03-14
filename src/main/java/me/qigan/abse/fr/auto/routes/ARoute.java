package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.fr.auto.routes.elems.ARENull;
import me.qigan.abse.fr.auto.routes.elems.ARElement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ARoute {

    public static enum Referer {
        DUNGEON, //Catacombs on clear
        FLOOR, //Specific catacombs floor
        GENERAL //Works everywhere
    }

    public String name = "null";
    public String author = "null";

    public int step = 0;
    public List<ARElement> elems = new ArrayList<>();
    public final Referer referer;
    public final int ref_id;
    public final Vec3 startingPos;

    private long force = 0;

    public ARoute(Referer ref, int id, Vec3 sPos) {
        this.referer = ref;
        this.ref_id = id;
        this.startingPos = sPos;
    }

    public boolean update(TickEvent.ClientTickEvent e) {
        if (step >= elems.size()) return true;
        if (System.currentTimeMillis()-force > Index.MAIN_CFG.getIntVal("ar_wait")) {
            elems.get(step).tick(e, this);

            if (elems.get(step).next()) {
                MinecraftForge.EVENT_BUS.unregister(elems.get(step));
                if (step+1<elems.size()) MinecraftForge.EVENT_BUS.register(elems.get(step+1));
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

    public boolean rage() {
        return false;
    }

    public JSONObject saveObj() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("author", author);
        obj.put("ref", referer.toString());
        obj.put("ref_id", ref_id);
        obj.put("length", elems.size());
        obj.put("start", new JSONObject()
                .put("x", startingPos.xCoord).put("y", startingPos.yCoord).put("z", startingPos.zCoord));
        JSONObject script = new JSONObject();
        int i = 0;
        for (ARElement element : elems) {
            script.put(Integer.toString(i), element.jsonObject());
            i++;
        }
        obj.put("script", script);


        JSONObject aligners = new JSONObject();
        obj.put("ghost_aligners", aligners);
        return obj;
    }

    public ARElement stepElement() {
        if (step >= elems.size()) return new ARENull();
        return elems.get(step);
    }

    public void clear() {
        elems.clear();
        reset();
    }

    public void add(ARElement element) {
        this.elems.add(element);
    }
}
