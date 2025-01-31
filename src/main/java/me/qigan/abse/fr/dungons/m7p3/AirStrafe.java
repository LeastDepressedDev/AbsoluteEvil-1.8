package me.qigan.abse.fr.dungons.m7p3;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.crp.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class AirStrafe extends Module {

    private float[] xz(float yaw) {
        double rads = yaw * Math.PI / 180;
        return new float[]{(float) -Math.sin(rads), (float) Math.cos(rads)};
    }

    void strafe() {
        if (!isEnabled() || !Index.KEY_MANAGER.get("airStrafe").isDown()) return;
        int key = Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode();
        float[] axis = xz(Minecraft.getMinecraft().thePlayer.rotationYaw);
        double speed = (5.612 / 20) * (Minecraft.getMinecraft().thePlayer.capabilities.getWalkSpeed()*10);
        KeyBinding.setKeyBindState(key, false);
        Minecraft.getMinecraft().thePlayer.setVelocity(0, Minecraft.getMinecraft().thePlayer.motionY, 0);
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft.getMinecraft().thePlayer.setVelocity(speed*axis[0], Minecraft.getMinecraft().thePlayer.motionY, speed*axis[1]);
            KeyBinding.setKeyBindState(key, Keyboard.isKeyDown(key));
        });
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) return;
        if (!isEnabled() || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.isRiding()) return;
        strafe();
    }

    @Override
    public String id() {
        return "arstf";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("airStrafe", "Strafe key[hold]", ValType.KEYBINDING, Keyboard.KEY_X));
        return list;
    }

    @Override
    public String fname() {
        return "Air strafe";
    }

    @Override
    public String description() {
        return "Great thing to have on terminals(Allows you to correct your movement in air)";
    }
}
