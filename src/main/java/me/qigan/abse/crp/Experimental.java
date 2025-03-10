package me.qigan.abse.crp;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.fr.exc.PhantomAim;
import me.qigan.abse.mapping.MappingController;
import me.qigan.abse.pathing.Path;
import me.qigan.abse.sync.Sync;

import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

@AutoDisable
@DangerousModule
public class Experimental extends Module implements EDLogic {

    public static Set<Vec3> expRender = new HashSet<>();

    @Override
    public String id() {
        return "exptl";
    }

    @Override
    public Specification category() {
        return Specification.SPECIAL;
    }

    @Override
    public String fname() {
        char[] str = "Experimental".toCharArray();
        String nstr = "";
        for (int i = 0; i < str.length; i++) {
            nstr += (i % 2 == 0) ? ("\u00A7e" + str[i]) : ("\u00A77" + str[i]);
        }
        return nstr;
    }

    @SubscribeEvent
    void Zov(TickEvent.ClientTickEvent e) {
        if (!isEnabled()) return;
    }

    @SubscribeEvent
    void tick(RenderWorldLastEvent e) {
        for (Vec3 vec : expRender) {
            Esp.autoBox3D(vec, Color.red, 1.7f, true);
        }
        if (!isEnabled()) return;

        //PhantomAim.call(new Float[]{0f, 0f}, 300, false);
    }

    @SubscribeEvent
    void packet(PacketEvent.SendEvent e) {
        if (!isEnabled()) return;


    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("exptl_but1", "Routing", ValType.BUTTON, (Runnable) () -> {
            if (isEnabled()) Index.MOVEMENT_CONTROLLER.go(new Path(Sync.playerPosAsBlockPos(), new BlockPos(10, 9, 7)).build());
        }));
        list.add(new SetsData<>("exptl_but2", "Mapping", ValType.BUTTON, (Runnable) () -> {
            if (isEnabled()) {
                MappingController.debug.clear();
            }
        }));
        list.add(new SetsData<>("exptl_but3", "Aura test", ValType.BUTTON, (Runnable) () -> {
            if (isEnabled()) {
                Sync.doBlockRightClick(new BlockPos(5, 5, 5));
            }
        }));
        return list;
    }

    @Override
    public String description() {
        return "Being used for testing some crazy stuff";
    }

    @Override
    public void onEnable() {
        Index.PLAYER_CONTROLLER.globalToggle = true;
        Index.PLAYER_CONTROLLER.goStateOvr[2] = true;
    }

    @Override
    public void onDisable() {
        Index.PLAYER_CONTROLLER.reset();
        Index.MOVEMENT_CONTROLLER.stop();
    }
}
