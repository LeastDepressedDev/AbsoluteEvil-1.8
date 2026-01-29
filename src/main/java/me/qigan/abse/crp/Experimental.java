package me.qigan.abse.crp;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.fr.exc.PhantomAim;
import me.qigan.abse.fr.exc.TimeoutTasks;
import me.qigan.abse.pathing.Path;
import me.qigan.abse.sync.Sync;

import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.*;
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


    /*
    Notes:
    When gui is updated by server it send you full Opened window thing, then it send you all the item packets and after that Packet window items

     */
    @SubscribeEvent
    void onPacketIncome(PacketEvent.ReceiveEvent e) {
        if (!isEnabled()) return;
        if (e.packet instanceof S2DPacketOpenWindow) {
            System.out.println(String.format("Opened window: %s --- %d", ((S2DPacketOpenWindow) e.packet).getWindowTitle(), ((S2DPacketOpenWindow) e.packet).getWindowId()));
        }
        if (e.packet instanceof S2EPacketCloseWindow) {
            System.out.println("Closed");
        }
        if (e.packet instanceof S2FPacketSetSlot) {
            String ln = ((S2FPacketSetSlot) e.packet).func_149174_e() != null ? ((S2FPacketSetSlot) e.packet).func_149174_e().getUnlocalizedName() : "Null slot";
            System.out.println(
                    String.format("Changed slot(%d) in window(%d) to ItemStack(%s)",
                            ((S2FPacketSetSlot) e.packet).func_149173_d(),
                            ((S2FPacketSetSlot) e.packet).func_149175_c(),
                            ln)
            );
        }
        if (e.packet instanceof S30PacketWindowItems) {
            System.out.println(String.format("Packet Window(%d) Items called for %d length",
                    ((S30PacketWindowItems) e.packet).func_148911_c(), ((S30PacketWindowItems) e.packet).getItemStacks().length));
        }
        if (e.packet instanceof S31PacketWindowProperty) {
            System.out.println("I have no fucking idea what this packet does but here we go");
            System.out.println(String.format("S31PacketWindowProperty for window(%d): %d -> %d",
                    ((S31PacketWindowProperty) e.packet).getWindowId(), ((S31PacketWindowProperty) e.packet).getVarIndex(), ((S31PacketWindowProperty) e.packet).getVarValue()));
        }
        if (e.packet instanceof S32PacketConfirmTransaction) {
            if (((S32PacketConfirmTransaction) e.packet).getWindowId() == 0) return;
            System.out.println(String.format("Confirm transaction packet: window:%d, action:%d", ((S32PacketConfirmTransaction) e.packet).getWindowId(), ((S32PacketConfirmTransaction) e.packet).getActionNumber()));
            System.out.println(String.format("Also Unknown Parameter(probably sucess or na?): %s", Boolean.toString(((S32PacketConfirmTransaction) e.packet).func_148888_e())));
        }
    }

    @SubscribeEvent
    void packet(PacketEvent.SendEvent e) {
        if (!isEnabled()) return;
        //Click ids 0 - lcm, 1 - rcm
        if (e.packet instanceof C0EPacketClickWindow) {
            System.out.println(Integer.toString(((C0EPacketClickWindow) e.packet).getMode()));
            System.out.println(Integer.toString(((C0EPacketClickWindow) e.packet).getActionNumber()));
            System.out.println(Integer.toString(((C0EPacketClickWindow) e.packet).getUsedButton()));
        }
//        if (e.packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
//            System.out.println("Look!");
//        }
//        if (e.packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
//            System.out.println("Pos look!");
//        }
//        if (e.packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
//            System.out.println("Position");
//        }
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("exptl_but1", "Routing", ValType.BUTTON, (Runnable) () -> {
            if (isEnabled()) Index.MOVEMENT_CONTROLLER.go(new Path(Sync.playerPosAsBlockPos(), new BlockPos(10, 9, 7)).build());
        }));
        list.add(new SetsData<>("exptl_but3", "Timeout thing test", ValType.BUTTON, (Runnable) () -> {
            if (isEnabled()) {
                TimeoutTasks.addTimeout(() -> Sync.player().addChatMessage(new ChatComponentText("\u00A7aGay test~!")), 3200);
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

    }

    @Override
    public void onDisable() {

    }
}
