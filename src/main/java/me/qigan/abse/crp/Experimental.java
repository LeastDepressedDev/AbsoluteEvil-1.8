package me.qigan.abse.crp;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.mapping.MappingController;
import me.qigan.abse.pathing.Path;
import me.qigan.abse.sync.Sync;

import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

@AutoDisable
@DangerousModule
public class Experimental extends Module implements EDLogic {

    public static BlockPos dPos = new BlockPos(0, 0, 0);

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
    void tick(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        Esp.autoBox3D(dPos, Color.red, 2f, true);
    }

    @SubscribeEvent
    void entTick(PacketEvent e) {
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
        list.add(new SetsData<>("exptl_but3", "TestEX", ValType.BUTTON, (Runnable) () -> {
            Sync.doBlockRightClick(new BlockPos(5, 5, 5));
        }));
        return list;
    }

    @Override
    public String description() {
        return "Being used for testing some crazy stuff";
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        Index.MOVEMENT_CONTROLLER.stop();
    }
}
