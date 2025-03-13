package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.fr.auto.routes.ARoute;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public abstract class ARElement {

    public Vec3 pos;

    public ARElement(Vec3 pos) {
        this.pos = pos;
    }

    public boolean next() {return true;}

    public abstract void tick(TickEvent.ClientTickEvent e, ARoute caller);
    public abstract void reset(ARoute caller);
    public abstract String elementString();
    public abstract JSONObject jsonObject();
    protected JSONObject posObject() {
        return new JSONObject().put("x", pos.xCoord).put("y", pos.yCoord).put("z", pos.zCoord);
    }
}
