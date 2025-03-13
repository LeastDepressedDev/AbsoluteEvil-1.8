package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.fr.auto.routes.ARoute;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public abstract class ARElement {

    public Vec3 startPos;
    public Vec3 endPos;

    public ARElement(Vec3 startPos, Vec3 endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public boolean next() {return true;}

    public abstract void tick(TickEvent.ClientTickEvent e, ARoute caller);
    public abstract void reset(ARoute caller);
    public abstract String elementString();
    public abstract JSONObject jsonObject();
    protected JSONObject posObject() {
        return new JSONObject().put("start", new JSONObject().put("x", startPos.xCoord).put("y", startPos.yCoord).put("z", startPos.zCoord))
                .put("end", new JSONObject().put("x", endPos.xCoord).put("y", endPos.yCoord).put("z", endPos.zCoord));
    }
}
