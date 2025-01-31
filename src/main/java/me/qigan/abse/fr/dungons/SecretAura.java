package me.qigan.abse.fr.dungons;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.exc.ClickSimTick;
import me.qigan.abse.fr.exc.TickTasks;
import me.qigan.abse.mapping.MappingController;
import me.qigan.abse.mapping.Room;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

@DangerousModule
public class SecretAura extends Module {

    public static Set<BlockPos> clicked = new HashSet<>();
    public static BlockPos last = null;
    public static long force = 0;

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        clicked.clear();
    }

    @SubscribeEvent
    void onOpen(GuiScreenEvent.InitGuiEvent e) {
        if (!Index.MAIN_CFG.getBoolVal("secar_fclose") || !Sync.inDungeon) return;
        if (e.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) e.gui;
            ContainerChest c1 = (ContainerChest) chest.inventorySlots;
            if (c1.getLowerChestInventory().getName().startsWith("Chest") && c1.getLowerChestInventory().getSizeInventory() == 27) {
                new Thread(() -> {
                    try {
                        int v = Index.MAIN_CFG.getIntVal("secar_fclose_d");
                        if (v >= 0) Thread.sleep(Math.max((long) (v+Utils.createRandomDouble(-10, 10)), 0));
                        Minecraft.getMinecraft().thePlayer.closeScreen();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
            }
        }
    }

    @SubscribeEvent
    void tick(TickEvent.ClientTickEvent e) {
        if (!isEnabled() || e.phase == TickEvent.Phase.END
                || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null
                || !Sync.inDungeon || !Sync.isClear()) return;
        try {
            BlockPos pos = null;
            if (Index.MAIN_CFG.getBoolVal("secar_rage")) {
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                Vec3 playerPos = new Vec3(player.posX, player.posY, player.posZ);
                boolean scare = false;
                for (int x = -5; x <= 5; x++) {
                    for (int y = -5; y <= 5; y++) {
                        for (int z = -5; z <= 5; z++) {
                            pos = new BlockPos(playerPos.xCoord+x,playerPos.yCoord+y,playerPos.zCoord+z);
                            Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                            if (!(block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.lever)) continue;
                            double d = playerPos.distanceTo(new Vec3(pos.getX()+0.5d, pos.getY()+0.5d, pos.getZ()+0.5d));
                            if (d < Minecraft.getMinecraft().playerController.getBlockReachDistance()) {
                                MappingController ctr = Index.MAPPING_CONTROLLER;
                                if (ctr.map != null && ctr.playerCell != null && ctr.roomMap != null) {
                                    int iter = ctr.map[ctr.playerCell[0]][ctr.playerCell[1]];
                                    Room rm = ctr.roomMap.get(iter);
                                    if (rm != null && rm.getType() == Room.Type.PUZZLE) continue;
                                }
                                scare = true;
                                break;
                            }
                            if (scare) break;
                        }
                        if (scare) break;
                    }
                    if (scare) break;
                }
            } else {
                pos = Minecraft.getMinecraft().thePlayer.rayTrace(Minecraft.getMinecraft().playerController.getBlockReachDistance(), 1f).getBlockPos();
                Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                if (!(block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.lever)) return;
                MappingController ctr = Index.MAPPING_CONTROLLER;
                if (ctr.map != null && ctr.playerCell != null && ctr.roomMap != null) {
                    int iter = ctr.map[ctr.playerCell[0]][ctr.playerCell[1]];
                    Room rm = ctr.roomMap.get(iter);
                    if (rm != null && rm.getType() == Room.Type.PUZZLE) return;
                }
            }

            if (pos == null) return;
            if (!clicked.contains(pos) && (System.currentTimeMillis()-force > Index.MAIN_CFG.getIntVal("secar_fd"))) {
                force = System.currentTimeMillis();
                if (Index.MAIN_CFG.getBoolVal("secar_rage")) {
                    final BlockPos finalPos = pos;
                    new Thread(() -> {
                        click(new PlayerInteractEvent(Minecraft.getMinecraft().thePlayer,
                                    PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, finalPos, null,
                                    Minecraft.getMinecraft().theWorld));
                        Sync.doBlockRightClick(finalPos);
                    }).start();
                } else {
                    new Thread(() -> {
                        try {
                            Thread.sleep(Index.MAIN_CFG.getIntVal("secar_d"));
                            ClickSimTick.click(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), 1);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).start();
                }
            }

        } catch (Exception exd) {
            exd.printStackTrace();
        }
    }

    @SubscribeEvent
    void click(PlayerInteractEvent e) {
        if (e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(e.pos).getBlock();
        if (!(block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.lever)) return;
        last = e.pos;
        clicked.add(e.pos);
    }

    @SubscribeEvent
    void chat(ClientChatReceivedEvent e) {
        if (Utils.cleanSB(e.message.getFormattedText()).startsWith("That chest is locked!"))
            TickTasks.call(() -> clicked.remove(last), 15);
    }


    @Override
    public String id() {
        return "secar";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Secret aura";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("secar_d", "Click delay[ms]", ValType.NUMBER, "100"));
        list.add(new SetsData<>("secar_fd", "Force delay[ms]", ValType.NUMBER, "250"));
        list.add(new SetsData<>("secar_fclose", "\u00A76Force close chest", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("secar_fclose_d", "Force close delay", ValType.NUMBER, "50"));
        return list;
    }

    @Override
    public String description() {
        return "This might push you over the limit!!!";
    }

    public static class SecretAuraRage extends Module {
        @Override
        public String id() {
            return "secar_rage";
        }

        @Override
        public Specification category() {
            return Specification.DUNGEONS;
        }

        @Override
        public String fname() {
            return "\u00A7cSecret aura rage mod";
        }

        @Override
        public String description() {
            return "Rage mod for secret aura!";
        }
    }
}
