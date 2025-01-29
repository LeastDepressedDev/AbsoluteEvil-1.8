package me.qigan.abse.fr.dungons.m7p3;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.LagTracker;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DeviceIssue extends Module {

    public static final BlockPos[] BOUNDING_SS_CONST = {new BlockPos(107, 120, 92), new BlockPos(109, 123, 95)};
    public static final BlockPos[] BLOCK_SPAWN_SS_CONST = {new BlockPos(111, 120, 92), new BlockPos(111, 123, 95)};
    public static final BlockPos CENTER_BUTTON_SS_CONST = new BlockPos(110, 121, 91);
    public static final int SS_CLICK_LIM = 15;

    //TODO: GET RID OF STUPID HASHSET
    public static final Set<BlockPos> seqLst = new HashSet<>();
    public static final List<BlockPos> seqBp = new ArrayList<>();

    //private static boolean phaseShift = false;
    public static int clickedSS = 0;
    public static int stepIter = 1;
    private static int iterSS = 0;
    private static boolean delta = false;

    private static void resetSS() {
        seqLst.clear();
        seqBp.clear();
        clickedSS = 0;
        stepIter = 0;
        iterSS = 0;
        //phaseShift = false;
    }

    private static boolean scanButSS() {
        return Minecraft.getMinecraft().theWorld.getBlockState(BLOCK_SPAWN_SS_CONST[0].add(-1, 0, 0)).getBlock() == Blocks.stone_button;
    }

    private static void shift() {
        iterSS = 0;
        stepIter++;
        seqBp.clear();
        seqLst.clear();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    void chatResTrigger(ClientChatReceivedEvent e) {
        if (!isEnabled()) return;
        if (Utils.cleanSB(e.message.getFormattedText()).contains("~~" + Index.MAIN_CFG.getStrVal("ss_reset_word"))) {
            resetSS();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    void onClick(PlayerInteractEvent e) {
        if (!isEnabled() || e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (Utils.compare(e.pos, CENTER_BUTTON_SS_CONST)) resetSS();
        if (iterSS >= seqLst.size()) return;
        if (Utils.compare(e.pos, seqBp.get(iterSS))) {
            clickedSS++;
            iterSS++;
        }
    }


    //FIRST CONST MODULLO
    public static boolean ready = true;
    public static boolean started = false;
    public static boolean done = false;


    private static int time = 0;
    private static int iter = 0;

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        ready = true;
        started = false;
        done = false;
        time = 0;
        iter = 0;
        resetSS();
    }

    @SubscribeEvent
    void onSpawn(EntityJoinWorldEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null) return;
        if (ready && e.entity.getDistanceSqToCenter(CENTER_BUTTON_SS_CONST) <= 7 && e.entity instanceof EntityArmorStand) {
            ready = false;
            if (Minecraft.getMinecraft().thePlayer.isSneaking()) {
                delta = true;
                started = true;
            }
            else done = true;
        }
    }

    private static boolean prePhase = true;
    private static boolean phase = true;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null || e.phase == TickEvent.Phase.END) return;
        if (!Sync.inDungeon || !Utils.posInDim(Sync.playerPosAsBlockPos(), BOUNDING_SS_CONST)) return;

        phase = scanButSS();

        if (phase != prePhase) {
            if (!phase) {
                shift();
            } else {
                if (delta) iterSS++;
                delta = false;
            }
            prePhase = phase;
        }

        if (phase) {
            if (Index.KEY_MANAGER.get("ssKey").isDown() && Index.MAIN_CFG.getBoolVal("auto_ss_click") && iterSS < seqBp.size()) {
                BlockPos pos = seqBp.get(iterSS);
                double dx = pos.getX() + 0.9d - Minecraft.getMinecraft().thePlayer.posX;
                double dy = pos.getY() - Minecraft.getMinecraft().thePlayer.posY - 1.1d;
                double dz = pos.getZ() + 0.5d - Minecraft.getMinecraft().thePlayer.posZ;
                Float[] vecs = Utils.getRotationsTo(dx, dy, dz, new float[]{
                        Minecraft.getMinecraft().thePlayer.rotationYaw,
                        Minecraft.getMinecraft().thePlayer.rotationPitch
                });
                SmoothAimControl.set(vecs, 2, 23, Index.MAIN_CFG.getDoubleVal("auto_ss_speed"));
                BlockPos bp = Minecraft.getMinecraft().thePlayer.rayTrace(4.2d, 1f).getBlockPos();
                if (Utils.compare(seqBp.get(iterSS), bp) /*&& !Index.MAIN_CFG.getBoolVal("auto_ss_adv")*/) {
                    if (LagTracker.ticks_since < 3 && LagTracker.diff() < Index.MAIN_CFG.getIntVal("ss_l_control")) ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), Index.MAIN_CFG.getIntVal("ss_hold"));
                }
            }
        } else {
            for (int dx = 0; dx < 4; dx++) {
                for (int dy = 0; dy < 4; dy++) {
                    if (Minecraft.getMinecraft().theWorld.getBlockState(BLOCK_SPAWN_SS_CONST[0].add(0, dy, dx)).getBlock() == Blocks.sea_lantern
                            && !seqLst.contains(BLOCK_SPAWN_SS_CONST[0].add(-1, dy, dx))) {
                        seqLst.add(BLOCK_SPAWN_SS_CONST[0].add(-1, dy, dx));
                        seqBp.add(BLOCK_SPAWN_SS_CONST[0].add(-1, dy, dx));
                    }
                }
            }
        }

        //SS Auto skip
        if (!Utils.posInDim(Sync.playerPosAsBlockPos(), BOUNDING_SS_CONST)) return;
        if (started) {
            int lim = Index.MAIN_CFG.getIntVal("ss_count");
            if (iter < lim) {
                if (time == 0) {
                    time = Index.MAIN_CFG.getIntVal("ss_del");
                    iter++;
                    ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), Index.MAIN_CFG.getIntVal("ss_hold"));
                } else {
                    time--;
                }
            } else if (iter == lim) {
                done = true;
            }
        }
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled() || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        if (Utils.posInDim(Sync.playerPosAsBlockPos(), BOUNDING_SS_CONST)) {
            if (done) {
                Esp.renderTextInWorld(started ? "\u00A76Done" : "\u00A7cCancelled",
                        CENTER_BUTTON_SS_CONST.getX(), CENTER_BUTTON_SS_CONST.getY() + 1, CENTER_BUTTON_SS_CONST.getZ(),
                        0xFFFFFF, e.partialTicks);
            } else {
                Esp.renderTextInWorld("\u00A7aAuto skip enabled",
                        CENTER_BUTTON_SS_CONST.getX(), CENTER_BUTTON_SS_CONST.getY() + 1, CENTER_BUTTON_SS_CONST.getZ(),
                        0xFFFFFF, e.partialTicks);
                Esp.renderTextInWorld(Minecraft.getMinecraft().thePlayer.isSneaking() ? "\u00A7eSkipping" : "\u00A74Not skipping",
                        CENTER_BUTTON_SS_CONST.getX(), CENTER_BUTTON_SS_CONST.getY() + 0.7, CENTER_BUTTON_SS_CONST.getZ(),
                        0xFFFFFF, e.partialTicks);
            }

            if (Index.MAIN_CFG.getBoolVal("render_ss_step")) {
                for (int i = iterSS; i < seqBp.size(); i++) {
                    BlockPos pos = seqBp.get(i);
                    Color col = i == iterSS ? Color.green : ((i == iterSS + 1) ? Color.yellow : Color.red);
                    Esp.autoBox3D(pos, col, 4f, true);
                    double x = pos.getX()+0.95d;
                    double y = pos.getY()+0.5d;
                    double z = pos.getZ()+0.5d;
                    Esp.renderTextInWorld(Integer.toString(i+1), x, y, z, col.getRGB(), 1d, e.partialTicks);
                }
            }

//            if (phase && MainWrapper.Keybinds.ssKey.isKeyDown() && Index.MAIN_CFG.getBoolVal("auto_ss_click") && iterSS < seqBp.size()) {
//                BlockPos bp = Minecraft.getMinecraft().thePlayer.rayTrace(4.2d, 1f).getBlockPos();
//                if (Utils.compare(seqBp.get(iterSS), bp) && Index.MAIN_CFG.getBoolVal("auto_ss_adv")) {
//                    ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), Index.MAIN_CFG.getIntVal("ss_hold"));
//                }
//            }
        }
    }

    @Override
    public String id() {
        return "devices";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Device issue";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("ss_reset_word", "Reset word", ValType.STRING, "res"));
        list.add(new SetsData<>("ss_hold", "Hold time[tick]", ValType.NUMBER, "1"));
        list.add(new SetsData<>("devices_auto_ss", "Auto skip SS", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("ss_count", "Clicks amount", ValType.NUMBER, "3"));
        list.add(new SetsData<>("ss_del", "Delay ticks", ValType.NUMBER, "3"));
        list.add(new SetsData<>("auto_ss_click", "Auto SS clicks", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("auto_ss_speed", "Auto SS speed", ValType.DOUBLE_NUMBER, "7.5"));
        list.add(new SetsData<>("auto_ss_adv", "Use advanced detect alg", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("render_ss_step", "Render clicks on SS", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("ss_l_control", "Lag control[ms]", ValType.NUMBER, "850"));
        return list;
    }

    @Override
    public String description() {
        return "Device utils for f7-m7";
    }
}
