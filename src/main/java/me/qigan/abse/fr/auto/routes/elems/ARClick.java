package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.Index;
import me.qigan.abse.crp.Experimental;
import me.qigan.abse.fr.auto.routes.ARoute;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.fr.macro.LegitGhostBlocksMacro;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class ARClick extends ARElement{

    public final BlockPos clickPos;
    public final boolean gp;

    private long forceDelay = 0;

    boolean clickDispatched = false;

    public ARClick(Vec3 pos, BlockPos clickPos, boolean gpu) {
        super(pos, pos);
        this.clickPos = clickPos;
        this.gp = gpu;
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {
        if (Sync.player().getPositionVector().distanceTo(endPos) > 0.6) return;
        MovingObjectPosition semiPos = Utils.generateBlockHit(clickPos);
        if (semiPos == null || semiPos.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) return;
        if (Sync.player().getPositionVector().distanceTo(semiPos.hitVec) > Minecraft.getMinecraft().playerController.getBlockReachDistance()) {
            Index.AR_CONTROLLER.interrupt(caller);
        }
        if (caller.rage()) {

        } else {
            Float[] rots = Utils.getRotationsTo(Sync.player().getPositionEyes(1), semiPos.hitVec,
                    new float[]{Sync.rotations()[0], Sync.rotations()[1]});
            Index.AR_CONTROLLER.rotate(rots);
            if (Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                    Utils.compare(Minecraft.getMinecraft().objectMouseOver.getBlockPos(), clickPos)) {
                ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1);
                return;
            }
            if (gp && Math.abs(Sync.rotations()[0]-rots[0]) < 0.5 && System.currentTimeMillis()-forceDelay>130) {
                forceDelay = System.currentTimeMillis();
                LegitGhostBlocksMacro.performSingle();
            }
        }
    }

    @SubscribeEvent
    void click(PlayerInteractEvent e) {
        if (e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (Utils.compare(e.pos, clickPos)) clickDispatched = true;
    }

    @Override
    public void reset(ARoute caller) {
        clickDispatched = false;
    }

    @Override
    public boolean next() {
        return clickDispatched;
    }

    @Override
    public String elementString() {
        return String.format("\u00A7aClick(%d, %d, %d)", clickPos.getX(), clickPos.getY(), clickPos.getZ());
    }

    @Override
    public JSONObject jsonObject() {
        return new JSONObject().put("type", "click").put("pos", this.posObject())
                .put("cord", new JSONObject()
                        .put("x", clickPos.getX())
                        .put("y", clickPos.getY())
                        .put("z", clickPos.getZ()))
                .put("gpu", this.gp);
    }
}
