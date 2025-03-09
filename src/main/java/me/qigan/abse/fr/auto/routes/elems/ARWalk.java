package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.Index;
import me.qigan.abse.fr.auto.routes.ARoute;
import me.qigan.abse.mapping.MappingUtils;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ARWalk extends ARElement{

    public enum State {
        BEGIN,
        ROTATE,
        WALK,
        DONE
    }

    public final Vec3 to;
    public final boolean doJump;
    public final boolean allowSprint;
    public final double speed;

    //INITIALS
    public State state = State.BEGIN;
    private boolean jc = true;
    private long jcTime = 0;

    public ARWalk(Vec3 pos, Vec3 target, boolean jump, boolean sprint, double speed) {
        super(pos);
        this.to = target;
        this.doJump = jump;
        this.allowSprint = sprint;
        this.speed = speed;
    }

    @Override
    public boolean next() {
        return state==State.DONE;
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {
        Index.PLAYER_CONTROLLER.stop();
        switch (state) {
            case BEGIN:
                if (Sync.player().getPositionVector().distanceTo(pos) > 0.6) return;
                break;
            case ROTATE:
                Float[] rots = Utils.getRotationsTo(
                        to.xCoord-Sync.player().posX,
                        to.yCoord-Sync.player().posY,
                        to.zCoord-Sync.player().posZ,
                        new float[]{Sync.player().rotationYaw, Sync.player().rotationPitch}
                );
                rots[1] = null;
                Index.AR_CONTROLLER.rotate(rots, speed);
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
        updateState();
    }

    @Override
    public void reset(ARoute caller) {
        state = State.BEGIN;
        jc = true;
        jcTime = 0;
    }

    private void updateState() {
        if (Sync.player().getPositionVector().distanceTo(to) <= 0.45) {
            state = State.DONE;
            Index.PLAYER_CONTROLLER.stop();
            Sync.player().motionX = 0;
            Sync.player().motionZ = 0;
            return;
        }
        Float[] rots = Utils.getRotationsTo(
                to.xCoord-Sync.player().posX,
                to.yCoord-Sync.player().posY,
                to.zCoord-Sync.player().posZ,
                new float[]{Sync.player().rotationYaw, Sync.player().rotationPitch}
        );
        if (rots[0] != null && Math.abs(rots[0]-Sync.player().rotationYaw) < 0.5d) {
            state = State.WALK;
        } else {
            state = State.ROTATE;
        }
    }
}
