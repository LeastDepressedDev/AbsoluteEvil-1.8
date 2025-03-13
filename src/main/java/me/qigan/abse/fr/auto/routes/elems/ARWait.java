package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.fr.auto.routes.ARoute;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class ARWait extends ARElement{

    public enum State {
        BEGIN,
        RELAY,
        END
    }

    public final long delay;

    //INITIALS
    private long lastTime = 0;
    public State state = State.BEGIN;

    public ARWait(Vec3 pos, long delay) {
        super(pos);
        this.delay = delay;
    }

    @Override
    public boolean next() {
        return state==State.END;
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {
        switch (state) {
            case BEGIN:
                if (Sync.player().getPositionVector().distanceTo(pos) <= 0.6) {
                    lastTime = System.currentTimeMillis();
                    state = State.RELAY;
                }
                break;
            case RELAY:
                if (System.currentTimeMillis()-lastTime>delay) state = State.END;
                break;
        }
    }

    @Override
    public void reset(ARoute caller) {
        lastTime = 0;
        state = State.BEGIN;
    }

    @Override
    public String elementString() {
        return "\u00A76" + (state == State.RELAY ? "Waiting\u00A7f(\u00A7a"+ Long.toString(System.currentTimeMillis()-lastTime) + "\u00A7f)" :
                "Wait\u00A7f(\u00A7a" + delay + "\u00A7f)");
    }

    @Override
    public JSONObject jsonObject() {
        return new JSONObject().put("type", "wait");
    }
}
