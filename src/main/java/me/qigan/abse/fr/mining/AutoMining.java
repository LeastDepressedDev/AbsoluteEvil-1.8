package me.qigan.abse.fr.mining;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.Alert;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.fr.exc.SmoothAimControl;
import me.qigan.abse.fr.exc.TickTasks;
import me.qigan.abse.events.PacketEvent;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

//Pass msg: Click here to purchase a new 6 hour pass for 10,000 Coins

@DangerousModule
public class AutoMining extends Module {

    public static final double DIST = 4.5d;
    public static final Random rand = new Random();

    public static List<BlockPos> blockRoute = new ArrayList<>();
    public static Set<BlockPos> skipPos = new HashSet<>();

    public static boolean active = false;
    public static BlockPos mining = null;
    public static int tickSince = 0;
    public static float[] offsets = new float[]{0, 0, 0};
    public static long lastUseAbility = 0;

    public static int aotvSlot = -1;
    public static int hypSlot = -1;

    public static STAGE stage = STAGE.IDLE;
    public static int progress = 0;


    public static int forceDelay = 0;

    public enum STAGE {
        IDLE,
        MINING,
        MOVING,
        MOBS
    }

    @SubscribeEvent
    void worldChange(WorldEvent.Load e) {
        clearRoute();
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || e.phase == TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        if (forceDelay > 0) {
            forceDelay--;
            return;
        }
        aotvSlot = findSlot("ASPECT_OF_THE_VOID");
        hypSlot = findSlot("NECRON_BLADE", "HYPERION", "ASTRAEA", "SCYLLA", "VALKYRIE");
        if (aotvSlot == -1 && !Index.MAIN_CFG.getBoolVal("auto_mining_debug")) return;
        if (hypSlot == -1 && Index.MAIN_CFG.getBoolVal("auto_mining_mobs")) return;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        stateProc();

        switch (stage) {
            case IDLE: return;
            case MINING: {
                if (mining != null) {
                    if (Index.MAIN_CFG.getBoolVal("auto_mining_abil") && System.currentTimeMillis()-lastUseAbility > 130000) clickAbility();
                    if (Index.MAIN_CFG.getBoolVal("auto_mining_manual")) return;
                    simTowardRotation(mining, 1d);
                    tickSince++;

                    BlockPos trace = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
                    if (trace != null) {
                        Block block = Minecraft.getMinecraft().theWorld.getBlockState(trace).getBlock();
                        if (block != Blocks.air) {
                            if (Utils.compare(trace, mining) || block == Blocks.stained_glass || block == Blocks.stained_glass_pane) {
                                ClickSimTick.updatableClick(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), 1);
                            }
                        } else if (Index.MAIN_CFG.getBoolVal("auto_mining_advm") && mining != null &&
                                player.getDistance(mining.getX()+0.5, mining.getY()-1, mining.getZ()+0.5) > 4) {
                            ClickSimTick.updatableClick(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), 1);
                        }
                    }
                } else next();
            }
            break;
            case MOVING: {
                if (Index.MAIN_CFG.getBoolVal("auto_mining_manual")) return;
                BlockPos target = blockRoute.get(progress);
                simTowardRotation(target, 1.3d);

                BlockPos trace = player.rayTrace(56, 1f).getBlockPos();
                if (Utils.compare(trace, target)) {
                    int slot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
                    Utils.selectHotbarSlot(aotvSlot);
                    TickTasks.call(() -> {
                        ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 2);
                        TickTasks.call(() -> Utils.selectHotbarSlot(slot), 6);
                        rollOffset();
                    }, 10);

                    ClickSimTick.updatableClick(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), 13);
                    forceDelay = 18;
                } else {
                    BlockPos pos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
                    if (pos != null) {
                        Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                        if (block == Blocks.stained_glass || block == Blocks.stained_glass_pane) {
                            ClickSimTick.updatableClick(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), 1);
                        }
                    }
                }
            }
            break;
            case MOBS: {
                SmoothAimControl.set(new Float[]{null, 90f}, 2, 20, 11);
                if (player.rotationPitch == 90f) {
                    int slot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
                    Utils.selectHotbarSlot(hypSlot);
                    TickTasks.call(() -> {
                        ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 2);
                        TickTasks.call(() -> Utils.selectHotbarSlot(slot), 8);
                    }, 5);
                    forceDelay = 15;
                }
            }
            break;
        }
    }

    private static void next() {
        if (progress+1 < blockRoute.size()) progress++;
        else progress = 0;
    }

    @SubscribeEvent
    void chat(ClientChatReceivedEvent e) {
        if (!isEnabled()) return;
        String text = Utils.cleanSB(e.message.getFormattedText());
        if (text.equalsIgnoreCase("Mining Speed Boost is now available!") && Index.MAIN_CFG.getBoolVal("auto_mining_abil")) clickAbility();
        if (text.equalsIgnoreCase("You used your Mining Speed Boost Pickaxe Ability!")) {
            lastUseAbility = System.currentTimeMillis();
        }
    }

    private static void stateProc() {
        if (active) {

            if (Index.MAIN_CFG.getBoolVal("auto_mining_mobs")) {
                for (Entity ent : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                    if (ent instanceof EntityIronGolem || ent instanceof EntityMagmaCube) {
                        if (ent.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) < 4) {
                            stage = STAGE.MOBS;
                            return;
                        }
                    }
                }
            }

            ClickSimTick.updatableClick(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), 2);
            reselect();

            if (checkIn()) {
                if (stage != STAGE.MINING) reselect();
                stage = STAGE.MINING;
            } else {
                stage = STAGE.MOVING;
            }
        } else {
            if (stage != STAGE.IDLE) TickTasks.clearQ();
            stage = STAGE.IDLE;
            mining = null;
        }
    }

    private static boolean checkIn() {
        return Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(blockRoute.get(progress).add(0, 1, 0)) < 2;
    }

    private static void clickAbility() {
        forceDelay = 8;
        TickTasks.call(() -> ClickSimTick.clickWCheck(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 2), 4);
        lastUseAbility = System.currentTimeMillis();
    }

    private static int findSlot(String... str) {
        for (int i = 0; i < 9; i++) {
            for (String line : str) {
                if (Utils.getSbData(Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i))
                        .getString("id").equalsIgnoreCase(line)) return i;
            }
        }
        return -1;
    }

    public static void simTowardRotation(BlockPos target, double speedMod) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(target).getBlock();
        double dx = target.getX() + 0.5d - player.posX + (block == Blocks.stained_glass_pane ? 0 : offsets[0]);
        double dy = target.getY() - player.posY - 1d + (isClosedBlock(target) || block == Blocks.stained_glass_pane ? (target.getY() < player.posY ? +0.5d : -0.5d ) : offsets[1]);
        double dz = target.getZ() + 0.5d - player.posZ + (block == Blocks.stained_glass_pane ? 0 : offsets[2]);
        Float[] angles = Utils.getRotationsTo(dx, dy, dz, new float[]{player.rotationYaw, player.rotationPitch});
        if (Minecraft.getMinecraft().thePlayer.rotationYaw == angles[0] && Minecraft.getMinecraft().thePlayer.rotationPitch == angles[1]
        && Index.MAIN_CFG.getBoolVal("auto_mining_skips") && tickSince > Index.MAIN_CFG.getIntVal("auto_mining_st")) {
            skipPos.add(target);
            reselect();
        }
        SmoothAimControl.set(angles, 2, 20, Index.MAIN_CFG.getDoubleVal("auto_mining_aim")*speedMod);
    }

    private static boolean isClosedBlock(BlockPos pos) {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        return world.getBlockState(pos.add(1, 0 ,0)).getBlock() != Blocks.air &&
                world.getBlockState(pos.add(-1, 0 ,0)).getBlock() != Blocks.air &&
                world.getBlockState(pos.add(0, 0 ,-1)).getBlock() != Blocks.air &&
                world.getBlockState(pos.add(0, 0 ,1)).getBlock() != Blocks.air;
    }

    public static void rollOffset() {
        float spread = Index.MAIN_CFG.getIntVal("auto_mining_sprd");
        offsets[0] = rand.nextInt()%spread/1000f;
        offsets[1] = rand.nextInt()%spread/1000f;
        offsets[2] = rand.nextInt()%spread/1000f;
    }


    //Double check cuz yeah!

    @SubscribeEvent
    void blockDestroyPacket(PacketEvent.ReceiveEvent e) {
        if (!isEnabled()) return;
        if (e.packet instanceof S22PacketMultiBlockChange) {
            S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange) e.packet;
            for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks()) {
                if (Utils.compare(data.getPos(), mining)) {
                    forceDelay = Index.MAIN_CFG.getIntVal("auto_mining_force");
                }
            }
        }
    }

    @SubscribeEvent
    void blockDestroyEvent(BlockEvent.BreakEvent e) {
        if (!isEnabled()) return;
        if (Utils.compare(e.pos, mining)) {
            forceDelay = Index.MAIN_CFG.getIntVal("auto_mining_force");
            rollOffset();
            reselect();
        }
    }



    @SubscribeEvent
    void onGuiOpen(GuiScreenEvent.InitGuiEvent e) {
        if (!isEnabled()) return;
        active = false;
        Alert.forceStop();
        forceDelay = 0;
    }

    @SubscribeEvent
    void onScreenRender(RenderGameOverlayEvent.Text e) {
        if (!isEnabled()) return;
        List<String> toRender = new ArrayList<>(Arrays.asList(
                "\u00A76Status: " + (active ? "\u00A7aACTIVE" : "\u00A7cSTOPPED"),
                "\u00A76Phase: \u00a7d" + stage.name(),
                "\u00A76Mining pos: " + (mining == null ? "\u00A7cUNSET" : "\u00A7aSet")
        ));
        if (aotvSlot == -1 && !Index.MAIN_CFG.getBoolVal("auto_mining_debug")) toRender.add(0, "\u00A7c!!!MISSING REQUIREMENT!!!");
        Esp.drawAllignedTextList(toRender, e.resolution.getScaledWidth()/2+20, e.resolution.getScaledHeight()/2-50, false, e.resolution, S2Dtype.SHADOW);
    }

    public static BlockPos crystalScan(BlockPos playerPos, BlockPos lastMinedPos) {
        BlockPos sel = null;
        int corner = Index.MAIN_CFG.getBoolVal("auto_mining_advm") ? 5 : 4;
        for (int x = -corner; x <= corner; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -corner; z <= corner; z++) {
                    //TODO: Complete selector
                    BlockPos centralPos = blockRoute.get(progress).add(0, 2, 0);
                    BlockPos scanPos = centralPos.add(x, y, z);
                    if (skipPos.contains(scanPos)) continue;
                    IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(scanPos);
                    Block curBlock = state.getBlock();
                    double sq = scanPos.distanceSq(centralPos);
                    double subDist = DIST+(Index.MAIN_CFG.getBoolVal("auto_mining_advm") ? 0.5 : 0)/*+(curBlock == Blocks.stained_glass_pane ? -0.7 : 0)*/;
                    if (sq > subDist*subDist) continue;
                    if ((Math.abs(scanPos.getX() - centralPos.getX()) <= 1 && Math.abs(scanPos.getZ() - centralPos.getZ()) <= 1)
                            && scanPos.getY() < centralPos.getY()-2) continue;
                    if (curBlock != Blocks.stained_glass && curBlock != Blocks.stained_glass_pane) continue;
                    if (Index.MAIN_CFG.getBoolVal("auto_mining_fil")) {
                        Collection<EnumDyeColor> colors = AutoMiningFilter.allowedColors();
                        if (!colors.contains(state.getValue(BlockStainedGlass.COLOR))) continue;
                    }

                    if (sel == null) {
                        sel = scanPos;
                    } else {
                        if (lastMinedPos == null) {
                            if (sel.distanceSq(playerPos) > scanPos.distanceSq(playerPos)) sel = scanPos;
                        } else {
                            if (sel.distanceSq(playerPos) + sel.distanceSq(lastMinedPos) >
                                    scanPos.distanceSq(playerPos) + scanPos.distanceSq(lastMinedPos)) sel = scanPos;
                        }
                    }
                }
            }
        }
        return sel;
    }

    @SubscribeEvent
    void renderWorld(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        BlockPos prePos = null;
        for (int i = 0; i < blockRoute.size(); i++) {
            BlockPos curPos = blockRoute.get(i);
            if (prePos != null) Esp.drawTracer(prePos.getX()+0.5d, prePos.getY()+0.5d, prePos.getZ()+0.5d,
                    curPos.getX()+0.5d, curPos.getY()+0.5d, curPos.getZ()+0.5d,
                    Color.magenta, 3f, true);
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(curPos).getBlock();
            Esp.autoBox3D(curPos, block == Blocks.air ? Color.red : (i == progress ? Color.green : Color.yellow), 2f, true);
            prePos = curPos;
            if (i == blockRoute.size()-1) {
                BlockPos sp = blockRoute.get(0);
                Esp.drawTracer(prePos.getX()+0.5d, prePos.getY()+0.5d, prePos.getZ()+0.5d,
                        sp.getX()+0.5d, sp.getY()+0.5d, sp.getZ()+0.5d,
                        Color.magenta, 3f, true);
            }
        }
        if (mining != null) Esp.autoBox3D(mining, Color.magenta, 4f, false);
    }

    @SubscribeEvent
    void interactEvent(PlayerInteractEvent e) {
        if (!isEnabled() || blockRoute.isEmpty()) return;
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (Utils.compare(blockRoute.get(progress), e.pos)) {
                active = true;
            }
        }
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
//        list.add(new SetsData<>("test_route", "Test route", ValType.BUTTON, (Runnable) () -> {
//            blockRoute = new ArrayList<>(Arrays.asList(
//                    new BlockPos(5, 5, 5),
//                    new BlockPos(5, 7, 10),
//                    new BlockPos(3, 3, 9)
//            ));
//        }));
        list.add(new SetsData<>("auto_mining_comment0", "Manual mode is disabling automation but keeps visuals enabled", ValType.COMMENT, null));
        list.add(new SetsData<>("auto_mining_manual", "Manual mode", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("auto_mining_debug", "\u00A7cDebug mode", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("auto_mining_comment1", "Routes are located in config/abse/mining", ValType.COMMENT, null));
        list.add(new SetsData<>("auto_mining_load", "Load route", ValType.BUTTON, (Runnable) AutoMining::loadRoute));
        list.add(new SetsData<>("auto_mining_load_path", "Route file name", ValType.STRING, ""));
        list.add(new SetsData<>("auto_mining_next", "Skip point", ValType.BUTTON, (Runnable) AutoMining::next));
        list.add(new SetsData<>("auto_mining_reset", "Reset route", ValType.BUTTON, (Runnable) AutoMining::routeReset));
        list.add(new SetsData<>("auto_mining_clear", "Clear route", ValType.BUTTON, (Runnable) AutoMining::clearRoute));
        list.add(new SetsData<>("auto_mining_comment2", "Active settings: ", ValType.COMMENT, null));
        list.add(new SetsData<>("auto_mining_mobs", "Kill mobs", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_skips", "Skip unreachable blocks", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_st", "Skip after ticks", ValType.NUMBER, "90"));
        list.add(new SetsData<>("auto_mining_advm", "Advanced movement", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("auto_mining_abil", "Auto ability on ready", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_force", "Force delay ticks", ValType.NUMBER, "1"));
        list.add(new SetsData<>("auto_mining_aim", "Aim speed", ValType.DOUBLE_NUMBER, "11"));
        list.add(new SetsData<>("auto_mining_sprd", "Spread[cord/1000]", ValType.NUMBER, "300"));
        return list;
    }

    @Override
    public String id() {
        return "auto_mining";
    }

    @Override
    public String fname() {
        return "Auto mining";
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public String description() {
        return "Solution for your poor nw. Instruction:\n1. Get aotv with etherwarp.\n2. To start mining right click on green box with mining tool\n  Warn: it will start mining even if you are not holding your mining tool.\n3. To stop mining open any gui(inventroy, chat, etc)\n4. Enjoy free money go brrrr.";
    }

    public static void reselect() {
        BlockPos pseudoMining = crystalScan(Sync.playerPosAsBlockPos().add(0, 1, 0), mining);
        if (!Utils.compare(pseudoMining, mining)) {
            rollOffset();
            tickSince = 0;
        }
        mining = pseudoMining;
    }

    public static void loadRoute() {
        File file = new File(Loader.instance().getConfigDir() + "/abse/mining/" + Index.MAIN_CFG.getStrVal("auto_mining_load_path"));
        if (file.isFile() && file.exists()) {
            routeReset();
            try {
                List<BlockPos> path = new ArrayList<>();
                JSONObject obj = new JSONObject(new Scanner(file).useDelimiter("\\Z").next());
                JSONArray posses = obj.getJSONArray("pos");
                if (posses.length() != obj.getInt("size")) {
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cInvalid route configuration!"));
                    return;
                }
                for (int i = 0; i < posses.length(); i++) {
                    JSONObject pos = posses.getJSONObject(i);
                    path.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
                }
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7aLoaded " + obj.getString("name") + " by " + obj.getString("author") + ". Length: " + obj.getInt("size") + "."));
                blockRoute = path;
                for (BlockPos pos : blockRoute) {
                    skipPos.add(pos.add(0, -1, 0));
                }
            } catch (FileNotFoundException e) {
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cUnpredicted thing went wrong!"));
                throw new RuntimeException(e);
            }
        } else {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cRoute file not found!"));
        }
    }

    public static void init() {
        File file = new File(Loader.instance().getConfigDir() + "/abse/mining");
        if (!file.exists()) file.mkdirs();
    }

    public static void routeReset() {
        active = false;
        progress = 0;
    }

    public static void clearRoute() {
        routeReset();
        blockRoute.clear();
    }
}
