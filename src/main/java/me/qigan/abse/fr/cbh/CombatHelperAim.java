package me.qigan.abse.fr.cbh;

import me.qigan.abse.Holder;
import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.config.WKeybind;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.fr.exc.PhantomAim;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import me.qigan.abse.vp.VisualApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CombatHelperAim extends Module {
    private static int skip = 0;
    private static int atkTick = 0;

    public static boolean BREAK_TOGGLE = false;

    public static Target prim;

    public static class Target {
        public final Entity ref;
        public final double theta;
        public final double zeta;
        public final double fovAt;

        public boolean lockTheta = true;
        public boolean lockZeta = true;

        public Target(Entity ref, double theta, double zeta, double fovAt) {
            this.ref = ref;
            this.theta = theta;
            this.zeta = zeta;
            this.fovAt = fovAt;
        }

        public Target unlockTheta() {
            this.lockTheta = false;
            return this;
        }

        public Target unlockZeta() {
            this.lockZeta = false;
            return this;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    void overLay(RenderGameOverlayEvent.Pre e) {
        if (!isEnabled() || Index.MAIN_CFG.getBoolVal("cbh_hide_target")) return;
        if (e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && prim != null) {
            float fYaw = Minecraft.getMinecraft().thePlayer.rotationYawHead;
            float fPitch = Minecraft.getMinecraft().thePlayer.rotationPitch;

            double dw = e.resolution.getScaledWidth()/180d;
            double dh = e.resolution.getScaledHeight()/90d;

            int x = (int) ((e.resolution.getScaledWidth()/2f)+(dw*((prim.theta-fYaw))));
            int y = (int) ((e.resolution.getScaledHeight()/2f)+(dh*(prim.zeta-fPitch)));

            GL11.glPushMatrix();
            GlStateManager.enableBlend();

            GL11.glTranslatef(x, y, 0);
            Esp.drawModalRectWithCustomSizedTexture(-4, -4, 8, 8, 0, 0, 8, 8,
                    new ResourceLocation("abse", "target.png"), Color.white);
            GL11.glPopMatrix();
            Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);

//            GL11.glPushMatrix();
//            VisualApi.setupLine(1f, Color.cyan);
//            GL11.glBegin(1);
//            double fov = Index.MAIN_CFG.getDoubleVal("cbh_fovat");
//            double v = 0;
//            Point pt = new Point((int) ((double) e.resolution.getScaledWidth() /2+fov/2*Math.cos(v)),
//                    (int) ((double) e.resolution.getScaledHeight() /2+fov/2*Math.sin(v)));
//            final double step = Math.PI/64;
//            while (v < 2*Math.PI) {
//                v+=step;
//                GL11.glVertex2i(pt.x, pt.y);
//                pt = new Point((int) ((double) e.resolution.getScaledWidth() /2+fov/2*Math.cos(v)),
//                        (int) ((double) e.resolution.getScaledHeight() /2+fov/2*Math.sin(v)));
//                GL11.glVertex2i(pt.x, pt.y);
//            }
//            GL11.glPopMatrix();
        }
    }

    @SubscribeEvent
    void debugText(RenderGameOverlayEvent.Text e) {
        if (!isEnabled() || !Debug.GENERAL) return;
        EntityPlayerSP mplayer = Minecraft.getMinecraft().thePlayer;
        List<String> tr = new ArrayList<>();
        tr.add("\u00a7erealY: " + mplayer.rotationYawHead);
        tr.add("\u00a7erealP: " + mplayer.rotationPitch);
        tr.add("\u00a7eTarget: " + ((prim == null || prim.ref == null) ? "None" : prim.ref.getName()));
        if (prim != null) {
            tr.add("\u00a7etargetTheta: " + prim.theta);
            tr.add("\u00a7etargetZeta: " + prim.zeta);
        }
        Esp.drawAllignedTextList(tr, e.resolution.getScaledWidth()/2+120, e.resolution.getScaledHeight()/2+10, false, e.resolution, S2Dtype.SHADOW);
    }

    private static boolean isActive() {
        WKeybind bind = Index.KEY_MANAGER.get("aimLock");
        return Index.MAIN_CFG.getBoolVal("cbh_kbk") ? bind.isDown() : Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown();
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        WKeybind bind = Index.KEY_MANAGER.get("aimBreak");
        if (Index.MAIN_CFG.getBoolVal("cbh_aim_tbkm") &&
                bind.isPressed()) BREAK_TOGGLE=!BREAK_TOGGLE;
        if (isActive() && !(Index.MAIN_CFG.getBoolVal("cbh_aim_tbkm") ? BREAK_TOGGLE : bind.isDown())) atkTick = Index.MAIN_CFG.getIntVal("cbh_atk");
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        if (SmoothAimControl.OVERRIDE || !isEnabled()) return;
        if (skip == 0) {
            if (atkTick == 0) {
                prim = null;
                return;
            }

            boolean advcState = Holder.quickFind("cbh_aim_adv").isEnabled();
            boolean randState = Holder.quickFind("cbh_aim_rand").isEnabled();
            boolean selState = Holder.quickFind("cbh_aim_sel").isEnabled();
            int ln = -1;
            if (selState && Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4) != null) {
                ln = Utils.getItemColor(Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4));
            }

            float fYaw = Minecraft.getMinecraft().thePlayer.rotationYawHead;
            float fPitch = Minecraft.getMinecraft().thePlayer.rotationPitch;
            skip = Index.MAIN_CFG.getIntVal("cbh_tickskip");
            Target primary = null;

            double distLim = Index.MAIN_CFG.getDoubleVal("cbh_dist");
            double s = Index.MAIN_CFG.getDoubleVal("cbh_speed");

                for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                    if (ent.getName().equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName())) continue;
                    if (ent instanceof EntityPlayer || Debug.GENERAL) {
                        if (selState && !Debug.GENERAL) {
                            EntityPlayer player = (EntityPlayer) ent;
                            if (Index.MAIN_CFG.getBoolVal("cbh_aim_sel_team") && player.getEquipmentInSlot(4) != null) {
                                if (player.getEquipmentInSlot(4).getItem() == Items.leather_helmet) {
                                    int prec = Utils.getItemColor(player.getEquipmentInSlot(4));
                                    if (prec == ln) continue;
                                }
                            }
                        }
                        double f = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(ent);

                        final Float[] rotations = Utils.getRotationsTo(Minecraft.getMinecraft().thePlayer, ent);
                        double dx = rotations[0]-Minecraft.getMinecraft().thePlayer.rotationYawHead;
                        double dy = rotations[1]-Minecraft.getMinecraft().thePlayer.rotationPitch;
                        double inScreenDistance = Math.sqrt(dx*dx + dy*dy);
                        if (!ent.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && f < distLim
                                && inScreenDistance < Index.MAIN_CFG.getDoubleVal("cbh_fovat")/2
                                && (primary == null || (Minecraft.getMinecraft().thePlayer.canEntityBeSeen(ent) &&
                                        inScreenDistance < primary.fovAt))) {
                            primary = new Target(ent, rotations[0], rotations[1], inScreenDistance);
                        }
                    }
                }

                prim = primary;

            if (prim != null) {
                double d = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(prim.ref);
                if (d > Index.MAIN_CFG.getDoubleVal("cbh_aim_distbp")) {
                    if (randState) s+=CombatHelperAimRandomize.createRandomDouble();
                    double v = (prim.theta - fYaw) * (s / (10 * d));
                    double u = (prim.zeta - fPitch) * (s / (10 * d));
                    if (Index.MAIN_CFG.getBoolVal("cbh_exptl")) {
                        PhantomAim.setupSmoothParam(s, 10 * d);
                        PhantomAim.call(new Float[]{(float) primary.theta,(float) primary.zeta}, 300, true);
                    } else {
                    if (advcState) {
                        if (prim.lockTheta) Minecraft.getMinecraft().thePlayer.rotationYaw += (Math.abs(prim.theta - fYaw) < Index.MAIN_CFG.getDoubleVal("cbh_aim_px")) ? 0 : (float) v;
                        if (prim.lockZeta) Minecraft.getMinecraft().thePlayer.rotationPitch += (Math.abs(prim.zeta - fPitch) < Index.MAIN_CFG.getDoubleVal("cbh_aim_py")) ? 0 : (float) u;
                    } else {
                        if (prim.lockTheta) Minecraft.getMinecraft().thePlayer.rotationYaw += (float) v;
                        if (prim.lockZeta) Minecraft.getMinecraft().thePlayer.rotationPitch += (float) u;
                    }
                    }
                }
            }

            atkTick--;
        } else {
            skip--;
        }
    }

    @Override
    public String id() {
        return "cbh_aim";
    }

    @Override
    public Specification category() {
        return Specification.COMBAT;
    }

    @Override
    public String fname() {
        return "Combat helper[aim]";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("cbh_speed", "Speed Modifier", ValType.DOUBLE_NUMBER, "4"));
        list.add(new SetsData<>("cbh_dist", "Distance", ValType.DOUBLE_NUMBER, "5"));
        list.add(new SetsData<>("cbh_tickskip", "Tick skip[don't change if you are not sure]", ValType.NUMBER, "1"));
        list.add(new SetsData<>("cbh_atk", "Attack tick mod", ValType.NUMBER, "20"));
        list.add(new SetsData<>("cbh_hide_target", "Hide aim cursor", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("aimBreak", "Aim break key", ValType.KEYBINDING, Keyboard.KEY_NONE));
        list.add(new SetsData<>("aimLock", "Aim lock key", ValType.KEYBINDING, Keyboard.KEY_G));
        list.add(new SetsData<>("cbh_kbk", "Use keybind key", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("cbh_fovat", "Fov", ValType.DOUBLE_NUMBER, "60"));
        list.add(new SetsData<>("cbh_exptl", "Experimental settings", ValType.BOOLEAN, "false"));
        return list;
    }

    @Override
    public String description() {
        return "Complicated thing as a massive problem";
    }
}
