package me.qigan.abse.vp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import org.lwjgl.opengl.GL11;
import scala.reflect.internal.pickling.Translations;

import javax.vecmath.Point3d;
import java.awt.*;
import java.util.Comparator;
import java.util.List;


public class Esp {

    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float width, float height, float u, float v, float textureWidth, float textureHeight, ResourceLocation texture, Color colSet) {
        drawModalRectWithCustomSizedTexture(x, y, width, height, u, v, textureWidth, textureHeight, texture, colSet, false);
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float width, float height, float u, float v, float textureWidth, float textureHeight, ResourceLocation texture, Color colSet, boolean linear) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        GlStateManager.color(colSet.getRed(), colSet.getGreen(), colSet.getBlue(), colSet.getAlpha());
        textureManager.bindTexture(texture);
        drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, textureWidth, textureHeight, linear);
        GlStateManager.color(1,1,1, 1);
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight, boolean linearTexture) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();

        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    public static void drawAsSingleLine(List<Point3d> vec, Color color, float lineWidth, boolean esp) {
        drawAsSingleLine(vec, color, lineWidth, esp, 1);
    }

    public static void drawAsSingleLine(List<Point3d> vec, Color color, float lineWidth, boolean esp, int beginMode) {
        if (vec.size() <= 2) return;
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        if (esp) {
            VisualApi.prepareGLL();
        }
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        VisualApi.setupLine(lineWidth, color);
        GlStateManager.translate(0, 0, 0);
        GL11.glBegin(beginMode);
        for (int i = 0; i < vec.size()-1; i++) {

            Point3d pt1 = vec.get(i);
            Point3d pt2 = vec.get(i+1);

            double x = pt1.x, y = pt1.y, z = pt1.z;
            double x1 = pt2.x, y1 = pt2.y, z1 = pt2.z;

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

        if (esp) {
            VisualApi.endGLL();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glColor4f(255, 255, 255, 1f);
        GL11.glPopMatrix();
    }

    public static void drawTracer(double x, double y, double z, double x1, double y1, double z1, Color color, float lineWidth) {
        drawTracer(x, y, z, x1, y1, z1, color, lineWidth, false);
    }

    public static void drawTracer(double x, double y, double z, double x1, double y1, double z1, Color color, float lineWidth, boolean esp) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        x = x - renderPosX;
        y = y - renderPosY;
        z = z - renderPosZ;

        x1 = x1 - renderPosX;
        y1 = y1 - renderPosY;
        z1 = z1 - renderPosZ;

        GL11.glPushMatrix();
        if (esp) {
            VisualApi.prepareGLL();
        }
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        VisualApi.setupLine(lineWidth, color);
        GlStateManager.translate(x, y, z);

        GL11.glBegin(1);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(x1, y1, z1);

        GL11.glEnd();
        if (esp) {
            VisualApi.endGLL();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glColor4f(255, 255, 255, 1f);
        GL11.glPopMatrix();
    }

    public static void drawFilledBox3D(double x, double y, double z, double width, double height, Color color, float lineWidth, boolean esp) {

        double posX = x - width / 2;
        double posY = y - height;
        double posZ = z - width / 2;

        GL11.glPushMatrix();
        if (esp) {
            VisualApi.prepareGLL();
        }
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        VisualApi.setupLine(lineWidth, color);
        GlStateManager.translate(posX, posY, posZ);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(width, 0, 0);
        GL11.glVertex3d(width, 0, width);
        GL11.glVertex3d(0, 0, width);

        GL11.glVertex3d(0, height, width);
        GL11.glVertex3d(width, height, width);
        GL11.glVertex3d(width, height, 0);
        GL11.glVertex3d(0, height, 0);

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, height, 0);
        GL11.glVertex3d(width, height, 0);
        GL11.glVertex3d(width, 0, 0);

        GL11.glVertex3d(0, 0, width);
        GL11.glVertex3d(0, height, width);
        GL11.glVertex3d(0, height, 0);
        GL11.glVertex3d(0, 0, 0);

        GL11.glVertex3d(width, 0, 0);
        GL11.glVertex3d(width, height, 0);
        GL11.glVertex3d(width, height, width);
        GL11.glVertex3d(width, 0, width);

        GL11.glVertex3d(width, 0, width);
        GL11.glVertex3d(width, height, width);
        GL11.glVertex3d(0, height, width);
        GL11.glVertex3d(0, 0, width);

        GL11.glEnd();


        if (esp) {
            VisualApi.endGLL();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glColor4f(255, 255, 255, 1f);
        GL11.glPopMatrix();
    }

    public static void autoFilledBox3D(BlockPos pos, Color color, float lf, boolean esp) {
        autoFilledBox3D(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d, 1, 1, color, lf, esp);
    }

    public static void autoFilledBox3D(Vec3 pos, Color color, float lf, boolean esp) {
        autoFilledBox3D(pos.xCoord, pos.yCoord, pos.zCoord, 1, 1, color, lf, esp);
    }

    public static void autoFilledBox3D(double x, double y, double z, double width, double height, Color color, float lf, boolean esp) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = x - renderPosX;
        double yPos = y - renderPosY;
        double zPos = z - renderPosZ;

        drawFilledBox3D(xPos, yPos, zPos, width, height, color, lf, esp);
    }
    
    public static void drawBox3D(double x, double y, double z, double width, double height, Color color, float lineWidth, boolean esp) {

        double posX = x - width / 2;
        double posY = y - height;
        double posZ = z - width / 2;

        GL11.glPushMatrix();
        if (esp) {
            VisualApi.prepareGLL();
        }
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        VisualApi.setupLine(lineWidth, color);
        GlStateManager.translate(posX, posY, posZ);
        GL11.glBegin(1);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, height, 0);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0 + width, 0, 0);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, 0 + width);
        GL11.glVertex3d(0 + width, 0, 0 + width);
        GL11.glVertex3d(0 + width, 0, 0);
        GL11.glVertex3d(0 + width, 0, 0 + width);
        GL11.glVertex3d(0, 0, 0 + width);
        GL11.glVertex3d(0 + width, 0, 0);
        GL11.glVertex3d(0 + width, 0 + height, 0);
        GL11.glVertex3d(0, 0, 0 + width);
        GL11.glVertex3d(0, 0 + height, 0 + width);
        GL11.glVertex3d(0 + width, 0, 0 + width);
        GL11.glVertex3d(0 + width, 0 + height, 0 + width);
        GL11.glVertex3d(0, 0 + height, 0);
        GL11.glVertex3d(0 + width, 0 + height, 0);
        GL11.glVertex3d(0, 0 + height, 0);
        GL11.glVertex3d(0, 0 + height, 0 + width);
        GL11.glVertex3d(0 + width, 0 + height, 0 + width);
        GL11.glVertex3d(0 + width, 0 + height, 0);
        GL11.glVertex3d(0 + width, 0 + height, 0 + width);
        GL11.glVertex3d(0, 0 + height, 0 + width);
        GL11.glEnd();
        if (esp) {
            VisualApi.endGLL();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glColor4f(255, 255, 255, 1f);
        GL11.glPopMatrix();
    }

    public static void autoBeaconBeam(double x, double y, double z, int rgb, float alpha, float pt) {
        x = x - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        y = y - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        z = z - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        renderBeaconBeam(x, y, z, rgb, alpha, pt);
    }

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     */
    public static void renderBeaconBeam(double x, double y, double z, int rgb, float alphaMultiplier, float partialTicks) {
        int height = 300;
        int bottomOffset = 0;
        int topOffset = bottomOffset + height;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        Minecraft.getMinecraft().getTextureManager().bindTexture(beaconBeam);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        double time = Minecraft.getMinecraft().theWorld.getTotalWorldTime() + (double)partialTicks;
        double d1 = MathHelper.func_181162_h(-time * 0.2D - (double)MathHelper.floor_double(-time * 0.1D));

        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        double d2 = time * 0.025D * -1.5D;
        double d4 = 0.5D + Math.cos(d2 + 2.356194490192345D) * 0.2D;
        double d5 = 0.5D + Math.sin(d2 + 2.356194490192345D) * 0.2D;
        double d6 = 0.5D + Math.cos(d2 + (Math.PI / 4D)) * 0.2D;
        double d7 = 0.5D + Math.sin(d2 + (Math.PI / 4D)) * 0.2D;
        double d8 = 0.5D + Math.cos(d2 + 3.9269908169872414D) * 0.2D;
        double d9 = 0.5D + Math.sin(d2 + 3.9269908169872414D) * 0.2D;
        double d10 = 0.5D + Math.cos(d2 + 5.497787143782138D) * 0.2D;
        double d11 = 0.5D + Math.sin(d2 + 5.497787143782138D) * 0.2D;
        double d14 = -1.0D + d1;
        double d15 = (double)(height) * 2.5D + d14;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0D, d15).color(r, g, b, alphaMultiplier).endVertex();
        tessellator.draw();

        GlStateManager.disableCull();
        double d12 = -1.0D + d1;
        double d13 = height + d12;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F*alphaMultiplier).endVertex();
        tessellator.draw();
    }


    
    public static void autoBox3D(double x, double y, double z, double width, double height, Color color, boolean esp) {
    	double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = x - renderPosX;
        double yPos = y - renderPosY;
        double zPos = z - renderPosZ;
        
        drawBox3D(xPos, yPos, zPos, width, height, color, 1f, esp);
    }

    public static void autoBox3D(double x, double y, double z, double width, double height, Color color, float lineW, boolean esp) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = x - renderPosX;
        double yPos = y - renderPosY;
        double zPos = z - renderPosZ;

        drawBox3D(xPos, yPos, zPos, width, height, color, lineW, esp);
    }
    
    public static void autoBox3D(Entity ent, Color color, boolean esp) {
    	double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = ent.lastTickPosX - renderPosX;
        double yPos = ent.lastTickPosY + ent.height - renderPosY;
        double zPos = ent.lastTickPosZ - renderPosZ;
        
        
        drawBox3D(xPos, yPos, zPos, ent.width, ent.height, color, 1f, esp);
    }

    public static void autoBox3D(Entity ent, Color color, float lineW, boolean esp) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = ent.lastTickPosX - renderPosX;
        double yPos = ent.lastTickPosY + ent.height - renderPosY;
        double zPos = ent.lastTickPosZ - renderPosZ;


        drawBox3D(xPos, yPos, zPos, ent.width, ent.height, color, lineW, esp);
    }

    public static void autoBox3D(BlockPos pos, Color color, float lineW, boolean esp) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = pos.getX() - renderPosX;
        double yPos = pos.getY() - renderPosY;
        double zPos = pos.getZ() - renderPosZ;


        drawBox3D(xPos+0.5, yPos+1, zPos+0.5, 1, 1, color, lineW, esp);
    }

    public static void autoBox3D(Vec3 pos, Color color, float lineW, boolean esp) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = pos.xCoord - renderPosX;
        double yPos = pos.yCoord - renderPosY;
        double zPos = pos.zCoord - renderPosZ;


        drawBox3D(xPos, yPos+1, zPos, 1, 1, color, lineW, esp);
    }


    public static void renderTextInWorld(String str, BlockPos pos, int col, double size, float partialTicks) {
        renderTextInWorld(str, pos.getX()+0.5d, pos.getY()+0.5d, pos.getZ()+0.5d, col, size, partialTicks);
    }

    public static void renderTextInWorld(String str, Vec3 pos, int col, double size, float partialTicks) {
        renderTextInWorld(str, pos.xCoord, pos.yCoord, pos.zCoord, col, size, partialTicks);
    }

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     */
    public static void renderTextInWorld(String str, double x, double y, double z, int col, double size, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        double x1 = x - viewerX;
        double y1 = y - viewerY - viewer.getEyeHeight();
        double z1 = z - viewerZ;

        double distSq = x1*x1 + y1*y1 + z1*z1;
        double dist = Math.sqrt(distSq);
        if(distSq > 144) {
            x1 *= 12/dist;
            y1 *= 12/dist;
            z1 *= 12/dist;
        }

        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.pushMatrix();

        GlStateManager.translate(x1, y1, z1);
        GlStateManager.translate(0, viewer.getEyeHeight(), 0);

        drawNametag(str, col, false, size);

        GlStateManager.popMatrix();

        GlStateManager.disableLighting();
    }

    public static void renderTextInWorld(String str, double x, double y, double z, int col, float partialTicks) {
        renderTextInWorld(str, x, y, z, col, 1d, partialTicks);
    }

    public static void drawPointInWorldCircle(Vec3 pos, double radius, int segments, float ls, Color color) {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double xPos = pos.xCoord - renderPosX;
        double yPos = pos.yCoord - renderPosY;
        double zPos = pos.zCoord - renderPosZ;

        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        VisualApi.setupLine(ls, color);
        GlStateManager.translate(0, 0, 0);
        GL11.glBegin(1);

        double aSeg = 2*Math.PI/segments;
        for (double angle = aSeg; angle < 2*Math.PI; angle+=aSeg) {
            GL11.glVertex3d(xPos+radius*Math.cos(angle-aSeg), yPos, zPos+radius*Math.sin(angle-aSeg));
            GL11.glVertex3d(xPos+radius*Math.cos(angle), yPos, zPos+radius*Math.sin(angle));
        }

        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glColor4f(255, 255, 255, 1f);
        GL11.glPopMatrix();
    }

    public static void  drawAllignedTextList(List<String> lines, int x, int y, boolean sortVertical, ScaledResolution res, S2Dtype mode) {
        drawAllignedTextList(lines, x, y, sortVertical, res, mode, new Color(0xFFFFFF));
    }

    public static void  drawAllignedTextList(List<String> lines, int x, int y, boolean sortVertical, ScaledResolution res, S2Dtype mode, Color color) {
        FontRenderer fnt = Minecraft.getMinecraft().fontRendererObj;
        int r = 0;
        int kp = lines.size()*10;
        if (sortVertical) {
            lines.sort((s, t1) -> (y < res.getScaledHeight()/2) ? Integer.compare(fnt.getStringWidth(t1), fnt.getStringWidth(s)) : Integer.compare(fnt.getStringWidth(s), fnt.getStringWidth(t1)));
        }

        GlStateManager.pushMatrix();
        for (String str : lines) {
            int ln = fnt.getStringWidth(str);
            Esp.drawOverlayString(Minecraft.getMinecraft().fontRendererObj, str,
                    (x > res.getScaledWidth()/2) ? x - ln : x,
                    y + (r * 10) - (y > res.getScaledHeight()/2 ? kp : 0), color.getRGB(), mode, color.getAlpha());
            r++;
        }
        GlStateManager.popMatrix();
    }

    public static void drawOverlayString(String str, int x, int y, int color, S2Dtype mode) {drawOverlayString(Minecraft.getMinecraft().fontRendererObj, str, x, y, color, mode);}

    public static void drawOverlayString(String str, int x, int y, Color color, S2Dtype mode) {drawOverlayString(str, x, y, color.getRGB(), mode);}

    public static void drawOverlayString(FontRenderer fntj, String str, int x, int y, Color color, S2Dtype mode) {drawOverlayString(fntj, str, x, y, color.getRGB(), mode);}

    public static void drawOverlayString(FontRenderer fntj, String str, int x, int y, int color, S2Dtype mode) {drawOverlayString(fntj, str, x, y, color, mode, 255);}
    public static void drawOverlayString(FontRenderer fntj, String str, int x, int y, int color, S2Dtype mode, int alpha) {
        int colorBlack = new Color(0, 0, 0, alpha).getRGB();
        String ssr = TextUtils.stripColor(str);
        switch (mode) {
            case SHADOW:
                fntj.drawStringWithShadow(str, x, y, color);
                break;
            case CORNERED:
                fntj.drawString(ssr, x-1, y, colorBlack);
                fntj.drawString(ssr, x+1, y, colorBlack);
                fntj.drawString(ssr, x, y-1, colorBlack);
                fntj.drawString(ssr, x, y+1, colorBlack);
                fntj.drawString(str, x, y, color);
                break;
            case CORNERED_SHADOW:
                fntj.drawString(ssr, x+2, y, colorBlack);
                fntj.drawString(ssr, x+3, y, colorBlack);
                fntj.drawString(ssr, x, y+2, colorBlack);
                fntj.drawString(ssr, x, y+3, colorBlack);

                fntj.drawString(ssr, x-1, y, colorBlack);
                fntj.drawString(ssr, x+1, y, colorBlack);
                fntj.drawString(ssr, x, y-1, colorBlack);
                fntj.drawString(ssr, x, y+1, colorBlack);
                fntj.drawString(str, x, y, color);
                break;

            case DEFAULT:
            default:
                fntj.drawString(str, x, y, color);
                break;
        }
    }

    public static void drawCenteredString(String str, int x, int y, Color color, S2Dtype mode) { drawCenteredString(str, x, y, color.getRGB(), mode); }

    public static void drawCenteredString(String str, int x, int y, int color, S2Dtype mode) { drawCenteredString(Minecraft.getMinecraft().fontRendererObj, str, x, y, color, mode);}

    public static void drawCenteredString(FontRenderer fntj, String str, int x, int y, int color, S2Dtype mode) {
        x-=fntj.getStringWidth(str)/2;
        drawOverlayString(fntj, str, x, y, color, mode);
    }

    public static void drawFilledRect(int x, int y, int width, int height, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y + height, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(x, y, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
    }

    public static void drawOutlineRect(int x, int y, int width, int height, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y + height, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(x, y, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
    }

    //Code from SkyblockAddons
    public static void renderNotification(String text, boolean renderCorner, int color) {
        int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        float scale = 4;
        if (stringWidth * scale > (res.getScaledWidth() * 0.9F)) {
            scale = (res.getScaledWidth() * 0.9F) / (float) stringWidth;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (res.getScaledWidth() / 2), (float) (res.getScaledHeight() / 2), 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

        if (renderCorner) drawOverlayString(text, (int) (-Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2), (int) -20.0F, color, S2Dtype.CORNERED);
        else drawOverlayString(text, (int) (-Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2), (int) -20.0F, color, S2Dtype.DEFAULT);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    public static void renderSubNotification(String text, boolean renderCorner, int color) {
        int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        float scale = 4;
        if (stringWidth * scale > (res.getScaledWidth() * 0.9F)) {
            scale = (res.getScaledWidth() * 0.9F) / (float) stringWidth;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (res.getScaledWidth() / 2), (float) (res.getScaledHeight() / 4) * 3, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

        if (renderCorner) drawOverlayString(text, (int) (-Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2), (int) -20.0F, color, S2Dtype.CORNERED);
        else drawOverlayString(text, (int) (-Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2), (int) -20.0F, color, S2Dtype.DEFAULT);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     */
    public static void drawNametag(String str, int color, boolean drawBg, double size) {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GlStateManager.pushMatrix();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1*size, -f1*size, f1*size);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 0;

        int j = fontrenderer.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        if (drawBg) {
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            tessellator.draw();
        }
        GlStateManager.enableTexture2D();
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, color);
        GlStateManager.depthMask(true);

        //fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);

        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
