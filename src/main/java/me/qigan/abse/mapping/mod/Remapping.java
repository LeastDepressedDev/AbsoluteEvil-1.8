package me.qigan.abse.mapping.mod;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.mapping.MappingUtils;
import me.qigan.abse.mapping.MappingController;
import me.qigan.abse.mapping.Room;
import me.qigan.abse.vp.Esp;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Remapping extends Module {

    public static List<AddressedData<BlockPos, Block>> repos = new ArrayList<>();

    @Override
    public String id() {
        return "remap";
    }

    @Override
    public Specification category() {
        return Specification.DUNGEONS;
    }

    @Override
    public String fname() {
        return "Remapping";
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!Index.MAIN_CFG.getBoolVal("remap_track") || Minecraft.getMinecraft().theWorld == null) return;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int[] xz = MappingUtils.cellToReal(i, j);
                int y = MappingUtils.rayDown(xz[0], xz[1], Minecraft.getMinecraft().theWorld);
                Esp.autoBox3D(new BlockPos(xz[0], y, xz[1]), Color.red, 3f, true);
            }
        }
        for (AddressedData<BlockPos, Block> bp : repos) {
            Esp.autoBox3D(bp.getNamespace(), Color.cyan, 2f, true);
            Esp.renderTextInWorld(bp.getObject().toString(), bp.getNamespace().getX()+0.5, bp.getNamespace().getY()+0.5, bp.getNamespace().getZ()+0.5,
                    Color.cyan.getRGB(), e.partialTicks);
        }
    }

    @SubscribeEvent
    void interact(PlayerInteractEvent e) {
        if (!Index.MAIN_CFG.getBoolVal("remap_track")) return;
        if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null && e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            repos.add(new AddressedData<>(e.pos, Minecraft.getMinecraft().theWorld.getBlockState(e.pos).getBlock()));
        }
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("remap_track", "Track mode", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("remap_track_clear", "Clear track", ValType.BUTTON, (Runnable) () -> repos.clear()));
        list.add(new SetsData<>("remap_track_sum", "Summarize[In console]", ValType.BUTTON, (Runnable) () -> {
            String ln = "Detect format\n";
            Room rm = Index.MAPPING_CONTROLLER.roomReg.get(Index.MAPPING_CONTROLLER.getPlayerCellIter());
            BlockPos pos = rm.transformInnerCoordinate(new BlockPos(0, 69, 0));
            for (AddressedData<BlockPos, Block> ele : repos) {
                //int[] cc = Mapping.cellToReal(rm.center[0], rm.center[1]);
                int dx = ele.getNamespace().getX() - pos.getX();
                int dz = ele.getNamespace().getZ() - pos.getZ();
                BlockPos fPos = new BlockPos(
                        (rm.getRotation() == Room.Rotation.SOUTH ? dx :
                                rm.getRotation() == Room.Rotation.WEST ? dz :
                                        rm.getRotation() == Room.Rotation.NORTH ? -dx : -dz),
                        ele.getNamespace().getY(),
                        (rm.getRotation() == Room.Rotation.SOUTH ? dz :
                                rm.getRotation() == Room.Rotation.WEST ? -dx :
                                        rm.getRotation() == Room.Rotation.NORTH ? -dz : dx));
                MappingController.debug.add(rm.transformInnerCoordinate(fPos));
                ln += "new AddressedData<>(new BlockPos(" + fPos.getX() + ", " + fPos.getY() + ", " + fPos.getZ() + "), Blocks." + ele.getObject().getRegistryName().substring(10) + "), \n";
            }
            System.out.println(ln);
            ln = "Routing format\n";
            for (AddressedData<BlockPos, Block> ele : repos) {
                //int[] cc = Mapping.cellToReal(rm.center[0], rm.center[1]);
                int dx = ele.getNamespace().getX() - pos.getX() ;
                int dz = ele.getNamespace().getZ() - pos.getZ();
                BlockPos fPos = new BlockPos(
                        (rm.getRotation() == Room.Rotation.SOUTH ? dx :
                                rm.getRotation() == Room.Rotation.WEST ? dz :
                                        rm.getRotation() == Room.Rotation.NORTH ? -dx : -dz),
                        ele.getNamespace().getY(),
                        (rm.getRotation() == Room.Rotation.SOUTH ? dz :
                                rm.getRotation() == Room.Rotation.WEST ? -dx :
                                        rm.getRotation() == Room.Rotation.NORTH ? -dz : dx));
                MappingController.debug.add(rm.transformInnerCoordinate(fPos));
                ln += "new BlockPos(" + fPos.getX() + ", " + (fPos.getY()+1) + ", " + fPos.getZ() + "),\n";
            }
            System.out.println(ln);
        }));
        list.add(new SetsData<>("remap_inf", "Room info", ValType.BUTTON, (Runnable) () -> Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(createRoomInfo()))));


        //Routing
        list.add(new SetsData<>("remap_text1", "Following settings are responsible for routing", ValType.COMMENT, null));
        list.add(new SetsData<>("remap_blocks", "Replace Blocks", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("remap_path", "Render path", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("remap_targets", "Render targets", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("remap_comments", "Render comments", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("remap_tick", "Delay tick[more tick means more optimisation]", ValType.NUMBER, "4"));
        list.add(new SetsData<>("remap_opt", "Use optimisation algorithm", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("remap_debug", "Do debug render", ValType.BOOLEAN, "false"));
        list.add(new SetsData<>("remap_force", "Force new dungeon", ValType.BUTTON, (Runnable) () -> Index.MAPPING_CONTROLLER.newDungeon()));
        return list;
    }

    public static String createRoomInfo() {
        Room rm = Index.MAPPING_CONTROLLER.roomReg.get(Index.MAPPING_CONTROLLER.getPlayerCellIter());
        if (rm == null) return "";
        return rm.getId() + ":   " + rm.core[0] + "-" + rm.core[1] + "\n"
                + rm.getShape() + "||" + rm.getType() + "||" + rm.getRotation() + "||" + rm.getHeight() + "\n"
                + "id:" + rm.getId();
    }

    @Override
    public String description() {
        return "Fast clear = good run";
    }
}
