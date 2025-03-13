package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.fr.auto.routes.ARoute;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class ARENull extends ARElement{
    public ARENull() {
        super(new Vec3(0, 0, 0), new Vec3(0, 0, 0));
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {}

    @Override
    public void reset(ARoute caller) {}

    @Override
    public String elementString() {
        return "\u00A70|NullElement|";
    }

    @Override
    public JSONObject jsonObject() {
        return new JSONObject().put("type", "null").put("pos", this.posObject());
    }
}
