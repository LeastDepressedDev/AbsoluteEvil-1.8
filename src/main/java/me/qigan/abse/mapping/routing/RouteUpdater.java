package me.qigan.abse.mapping.routing;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.mapping.Room;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.VisualApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RouteUpdater {
    public static List<BlockPos> path = new ArrayList<>();
    public static List<AddressedData<BlockPos, Color>> outlines = new ArrayList<>();
    public static List<AddressedData<BlockPos, String>> comments = new ArrayList<>();

    @SubscribeEvent
    void render(RenderWorldLastEvent e) {
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (path.size() > 0 && Index.MAIN_CFG.getBoolVal("remap_path")) {
            BlockPos startPos = path.get(0);
            Esp.autoBox3D(startPos.add(0, -1, 0), Color.green, 2f, true);
            Esp.renderTextInWorld("start", startPos.add(0, -1, 0), Color.green.getRGB(), 1d, e.partialTicks);
            BlockPos endPos = path.get(path.size()-1);
            Esp.autoBox3D(endPos.add(0, -1, 0), Color.red, 2f, true);
            Esp.renderTextInWorld("end", endPos.add(0, -1, 0), Color.red.getRGB(), 1d, e.partialTicks);

            drawPath(path, 5.0f, Color.green);
        }
        if (Index.MAIN_CFG.getBoolVal("remap_targets")) {
            for (AddressedData<BlockPos, Color> block : outlines) {
                Esp.autoBox3D(block.getNamespace(), block.getObject(), 4f, false);
            }
        }
        if (Index.MAIN_CFG.getBoolVal("remap_comments")) {
            for (AddressedData<BlockPos, String> cmt : comments) {
                Esp.renderTextInWorld(cmt.getObject(), cmt.getNamespace(), 0xFFFFFF, 1d, e.partialTicks);
            }
        }
    }

    public static void drawPath(List<BlockPos> vec, float ls, Color color) {
        if (vec.size() <= 2) return;
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        VisualApi.setupLine(ls, color);
        GlStateManager.translate(0, 0, 0);
        GL11.glBegin(1);
        for (int i = 0; i < vec.size()-1; i++) {

            BlockPos pt1 = vec.get(i);
            BlockPos pt2 = vec.get(i+1);

            double x = pt1.getX()+0.5d, y = pt1.getY()+0.5d, z = pt1.getZ()+0.5d;
            double x1 = pt2.getX()+0.5d, y1 = pt2.getY()+0.5d, z1 = pt2.getZ()+0.5d;

            x -= renderPosX;
            y -= renderPosY;
            z -= renderPosZ;

            x1 -= renderPosX;
            y1 -= renderPosY;
            z1 -= renderPosZ;

            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x1, y1, z1);
        }
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glColor4f(255, 255, 255, 1f);
        GL11.glPopMatrix();
    }

    @SubscribeEvent
    void load(WorldEvent.Load e) {
        path = new ArrayList<>();
        comments = new ArrayList<>();
        outlines = new ArrayList<>();
    }

    public static void update(Route route, Room room) {
        path = new ArrayList<>();
        comments = new ArrayList<>();
        outlines = new ArrayList<>();

        for (BlockPos point : route.getPath()) path.add(room.transformInnerCoordinate(point));
        for (AddressedData<BlockPos, Color> point : route.getOutlines()) outlines.add(new AddressedData<>(room.transformInnerCoordinate(point.getNamespace()), point.getObject()));
        for (AddressedData<BlockPos, String> point : route.getComments()) comments.add(new AddressedData<>(room.transformInnerCoordinate(point.getNamespace()), point.getObject()));
    }
}
