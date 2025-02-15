package me.qigan.abse.mapping.auto;

import me.qigan.abse.Index;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.mapping.MappingUtils;
import me.qigan.abse.mapping.Room;
import me.qigan.abse.sync.Sync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;

public class SeqWalk extends QueuedSeq {

    public final Float[] angle;
    public double dist;
    public int phase = 0;
    private BlockPos sp;

    public SeqWalk(Float[] angle, double distance) {
        this.dist = distance;
        Room rm = Index.MAPPING_CONTROLLER.getPlayerRoom();
        if (rm == null) {
            this.angle = angle;
        } else {
            this.angle = new Float[2];
            this.angle[1] = angle[1];
            angle[0] = ((float) ((int) Minecraft.getMinecraft().thePlayer.rotationYawHead/360) * 360) + angle[0] + rm.getRotation().angle;
        }
    }

    @Override
    public void run() {
        switch (phase) {
            case 0: {
                SmoothAimControl.set(this.angle, 2, 20, 18);
                sp = Sync.playerPosAsBlockPos();
                if (Minecraft.getMinecraft().thePlayer.rotationYawHead == angle[0]) phase++;
            }
            break;
            case 1: {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                BlockPos pos = Sync.playerPosAsBlockPos();
                if (sp.distanceSq(pos.getX(), pos.getY(), pos.getZ()) > dist) phase++;
            }
            break;
            case 2: {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                phase++;
                finished = true;
            }
            break;
        }
    }
}
