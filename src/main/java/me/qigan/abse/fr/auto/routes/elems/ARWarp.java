package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.Index;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.fr.auto.routes.ARoute;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ARWarp extends ARElement{

    public static enum State {
        BEGIN,
        ROTATING,
        LEAPING,
        END
    }

    private final Float[] angles;
    public State state = State.BEGIN;
    private long forceDelay = 0;
    private static final DecimalFormat df = new DecimalFormat("#.##");
    boolean rotatedFull = false;

    public ARWarp(Vec3 startPos, Vec3 endPos, Float[] angles) {
        super(startPos, endPos);
        this.angles = angles;
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {
        int pdl = Index.MAIN_CFG.getIntVal("ar_ewpd");
        this.updateState(caller);
        if (System.currentTimeMillis()-forceDelay<pdl+200) return;
        if (caller.rage()) {

        } else {
            switch (state) {
                case ROTATING:
                    Index.AR_CONTROLLER.rotate(Utils.getRelAngles(new Float[]{Sync.rotations()[0], Sync.rotations()[1]}, angles));
                    break;
                case LEAPING:
                    forceDelay = System.currentTimeMillis();
                    if (Index.AR_CONTROLLER.slots.ewp == -1) {
                        Sync.player().addChatMessage(new ChatComponentText("\u00A7cAotv not found. Interrupting!"));
                        Index.AR_CONTROLLER.interrupt(caller);
                        return;
                    }
                    Sync.player().inventory.currentItem = Index.AR_CONTROLLER.slots.ewp;
                    new Thread(() -> {
                        try {
                            Thread.sleep(pdl/10);
                            Index.PLAYER_CONTROLLER.sneak = true;
                            Thread.sleep(pdl/4);
                            ClickSimTick.clickWCheck(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1);
                            Thread.sleep(pdl/4);
                            Index.PLAYER_CONTROLLER.sneak = false;
                            Thread.sleep(pdl/7);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).start();
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SendEvent e) {
        boolean flag = false;
        //if (e.packet instanceof CPacketPlaye)
        if (e.packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
            C03PacketPlayer.C05PacketPlayerLook packet = (C03PacketPlayer.C05PacketPlayerLook) e.packet;
            if (Math.abs(packet.getYaw()-Sync.rotations()[0]) <= 0.1 &&
                    Math.abs(packet.getPitch()-Sync.rotations()[1]) <= 0.1)
                flag = true;
        }
        if (e.packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook packet = (C03PacketPlayer.C06PacketPlayerPosLook) e.packet;
            if (Math.abs(packet.getYaw()-Sync.rotations()[0]) <= 0.1 &&
                    Math.abs(packet.getPitch()-Sync.rotations()[1]) <= 0.1)
                flag = true;
        }
        if (e.packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition packet = (C03PacketPlayer.C04PacketPlayerPosition) e.packet;
            if (Math.abs(packet.getYaw()-Sync.rotations()[0]) <= 0.1 &&
                    Math.abs(packet.getPitch()-Sync.rotations()[1]) <= 0.1)
                flag = true;
        }
        rotatedFull = flag;
    }

    @Override
    public boolean next() {
        return state == State.END;
    }

    private void updateState(ARoute caller) {
        if (Sync.player().getPositionVector().distanceTo(endPos) <= 0.45) {
            state = State.END;
            return;
        }
        Float[] rels = Utils.getRelAngles(new Float[]{Sync.rotations()[0], Sync.rotations()[1]}, angles);
        if (Math.abs(rels[0]-Sync.rotations()[0]) < 0.1d && Math.abs(rels[1]-Sync.rotations()[1]) < 0.1d) {
            state = State.LEAPING;
        } else {
            state = State.ROTATING;
        }
    }

    @Override
    public void reset(ARoute caller) {
        state = State.BEGIN;
        forceDelay = 0;
        rotatedFull = false;
    }

    @Override
    public String elementString() {
        return String.format("\u00A7dEwp \u00A7f(\u00A77%d\u00A7f,\u00A77 %d\u00A7f,\u00A77 %d)", (int) endPos.xCoord, (int) endPos.yCoord, (int) endPos.zCoord) + "\u00A7f[\u00A77" + df.format(angles[0]) + "\u00A7f,\u00A77" + df.format(angles[1]) + "\u00A7f]";
    }

    @Override
    public JSONObject jsonObject() {
        return new JSONObject().put("type", "ewp").put("pos", this.posObject()).put("target",
                new JSONObject().put("yaw", angles[0]).put("pitch", angles[1]));
    }
}
