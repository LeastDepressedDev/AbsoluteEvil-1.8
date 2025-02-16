package me.qigan.abse.fr.mining;

import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.Module;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class RouteRecorder extends Module {

    public static LinkedList<BlockPos> blockRoute = new LinkedList<>();

    public static void clear() {
        blockRoute.clear();
    }

    public static void undo() {
        try {
            blockRoute.removeLast();
        } catch (NoSuchElementException ex) {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cCannot undo! Route is empty."));
        }
    }

    @SubscribeEvent
    void interact(PlayerInteractEvent e) {
        if (!isEnabled()) return;
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && Minecraft.getMinecraft().thePlayer.isSneaking() && Minecraft.getMinecraft().thePlayer.getHeldItem() == null) {
            if (blockRoute.isEmpty() || !Utils.compare(blockRoute.getLast(), e.pos)) blockRoute.add(e.pos);
        }
    }

    public static void save() {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        for (BlockPos pos : blockRoute) {
            arr.put(new JSONObject().put("x", pos.getX()).put("y", pos.getY()).put("z", pos.getZ()));
        }
        obj.put("name", "Undefined").put("author", "Unknown").put("size", blockRoute.size()).put("pos", arr);

        File file = new File(Loader.instance().getConfigDir() + "/abse/mining/new.json");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(obj.toString());
            writer.flush();
            writer.close();
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cSaved as new.json."));
        } catch (IOException e) {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cSaving failed."));
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        for (int i = 0; i < blockRoute.size(); i++) {
            BlockPos pos = blockRoute.get(i);
            Esp.autoBox3D(pos, Color.cyan, 3f, true);
            Esp.renderTextInWorld((i+1) + "#", pos, Color.cyan.getRGB(), 1.1d, e.partialTicks);
        }
    }

    @Override
    public String id() {
        return "mining_rrc";
    }

    @Override
    public String fname() {
        return "Mining route recorder";
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("mining_rrc_save", "Save", ValType.BUTTON, (Runnable) RouteRecorder::save));
        list.add(new SetsData<>("mining_rrc_undo", "Undo", ValType.BUTTON, (Runnable) RouteRecorder::undo));
        list.add(new SetsData<>("mining_rrc_clear", "Clear", ValType.BUTTON, (Runnable) RouteRecorder::clear));
        return list;
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public String description() {
        return "Records route for mining.\n Shift right click on block with hand to add it.";
    }
}
