package me.qigan.abse.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton {
    
    @Shadow
    protected boolean hovered;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int xPosition;
    @Shadow
    public int yPosition;
    @Shadow
    public String displayString;
    public int id;
    @Shadow
    public boolean enabled;
    @Shadow
    public boolean visible;
    @Shadow
    public int packedFGColour;
    
    /**
     * @author qigan
     * @reason ricing
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            GlStateManager.pushMatrix();
            Tessellator tes = Tessellator.getInstance();
            WorldRenderer wr = tes.getWorldRenderer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
            
            tes.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.popMatrix();

            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;
            if (this.packedFGColour != 0) {
                j = this.packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }

    }

    @Shadow
    protected abstract int getHoverState(boolean p_getHoverState_1_);

    @Shadow
    protected abstract void mouseDragged(Minecraft p_mouseDragged_1_, int p_mouseDragged_2_, int p_mouseDragged_3_);

    private static void drawCenteredString(FontRenderer p_drawCenteredString_1_, String p_drawCenteredString_2_, int p_drawCenteredString_3_, int p_drawCenteredString_4_, int p_drawCenteredString_5_) {
        p_drawCenteredString_1_.drawStringWithShadow(p_drawCenteredString_2_, (float)(p_drawCenteredString_3_ - p_drawCenteredString_1_.getStringWidth(p_drawCenteredString_2_) / 2), (float)p_drawCenteredString_4_, p_drawCenteredString_5_);
    }
}
