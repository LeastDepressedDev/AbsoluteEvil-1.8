package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.Index;
import me.qigan.abse.fr.auto.routes.ARoute;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ARWalk extends ARElement{

    public enum State {
        BEGIN,
        ROTATE,
        WALK,
        DONE
    }

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public final boolean doJump;
    public final boolean allowSprint;
    public final boolean doStop;

    //INITIALS
    public State state = State.BEGIN;
    private boolean jc = true;
    private long jcTime = 0;

    public ARWalk(Vec3 startPos, Vec3 target, boolean jump, boolean sprint, boolean doStop) {
        super(startPos, target);
        this.doJump = jump;
        this.allowSprint = sprint;
        this.doStop = doStop;
    }

    @Override
    public boolean next() {
        return state==State.DONE;
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {
        updateState(caller);
        switch (state) {
            case BEGIN:
                if (Sync.player().getPositionVector().distanceTo(startPos) > 0.6) return;
                break;
            case ROTATE:
                Float[] rots = Utils.getRotationsTo(
                        endPos.xCoord-Sync.player().posX,
                        endPos.yCoord-Sync.player().posY,
                        endPos.zCoord-Sync.player().posZ,
                        new float[]{Sync.rotations()[0], Sync.rotations()[1]}
                );
                rots[1] = null;
                Index.AR_CONTROLLER.rotate(rots);
                break;
            case WALK:
                Index.PLAYER_CONTROLLER.goStateOvr[0] = true;
                if (doJump) Index.PLAYER_CONTROLLER.jump = !jc && System.currentTimeMillis()-jcTime<400;
                Sync.player().setSprinting(allowSprint);
                if (jc) {
                    jcTime = System.currentTimeMillis();
                    jc = false;
                }
                break;
        }
    }

    @Override
    public void reset(ARoute caller) {
        state = State.BEGIN;
        jc = true;
        jcTime = 0;
    }

    @Override
    public String elementString() {
        return "\u00A7fWalk\u00A7f(\u00A77" + df.format(endPos.xCoord) + "\u00A7f,\u00A77" + df.format(endPos.zCoord) + "\u00A7f)";
    }

    @Override
    public JSONObject jsonObject() {
        return new JSONObject().put("type", "walk").put("pos", this.posObject())
                .put("jump", this.doJump).put("sprint", this.allowSprint).put("stop", this.doStop)
                .put("to", new JSONObject().put("x", endPos.xCoord).put("z", endPos.zCoord));
    }

    private void updateState(ARoute caller) {
        if (Utils.getDistanceHorizontal(Sync.player().getPositionVector(), endPos) <= 0.45) {
            state = State.DONE;
            if (!doStop) {
                if (caller.step+1<caller.elems.size() && caller.elems.get(caller.step+1) instanceof ARWalk) {}
                else {
                    Index.PLAYER_CONTROLLER.stop();
                }
            }
            if (doStop) {
                Index.PLAYER_CONTROLLER.stop();
                Sync.player().motionX = 0;
                Sync.player().motionZ = 0;
            }
            return;
        }
        Float[] rots = Utils.getRotationsTo(
                endPos.xCoord-Sync.player().posX,
                endPos.yCoord-Sync.player().posY,
                endPos.zCoord-Sync.player().posZ,
                new float[]{Sync.player().rotationYaw, Sync.player().rotationPitch}
        );
        if (rots[0] != null && Math.abs(rots[0]-Sync.rotations()[0]) < 0.5d) {
            state = State.WALK;
        } else {
            state = State.ROTATE;
        }
    }
}
