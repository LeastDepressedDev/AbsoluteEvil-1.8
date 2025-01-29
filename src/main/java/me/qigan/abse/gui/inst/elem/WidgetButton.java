package me.qigan.abse.gui.inst.elem;

import me.qigan.abse.gui.inst.NewMainMenu;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class WidgetButton extends WidgetUpdatable{

    public static Color colorEnabled = new Color(36, 97, 129);
    public static Color colorDisabled = new Color(23, 59, 76);
    public double textScale = 1;
    protected boolean enabled = true;

    protected String text = "";
    private final Runnable fun;

    private Dimension textOffset = new Dimension(0, 0);

    public WidgetButton(int x, int y, int width, int height, Runnable runnable) {
        super(x, y);
        this.fun = runnable;
        box(width, height);
    }

    public WidgetButton text(String text) {
        this.text = text;
        return this;
    }

    public WidgetButton textScale(double scale) {
        this.textScale = scale;
        return this;
    }

    public WidgetButton textOffset(int x, int y) {
        textOffset = new Dimension(x, y);
        return this;
    }

    public WidgetButton enabled(boolean v) {
        this.enabled = v;
        return this;
    }

    public Runnable getFun() {
        return fun;
    }

    public String getText() {
        return text;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean draw(int mouseX, int mouseY, float partialTicks) {

        Color colRef = enabled ? colorEnabled : colorDisabled;

        Color colIn = new Color(colRef.getRed()-20, colRef.getGreen()-20, colRef.getBlue()-20);
        if (Utils.pointInMovedDim(new Point(mouseX, mouseY), new Point(cordX, cordY), new Dimension(boxX, boxY)) && enabled) {
            Gui.drawRect(cordX-1, cordY-1, cordX+boxX+1, cordY+boxY+1, Color.white.getRGB());
        }
        Gui.drawRect(cordX, cordY, cordX+boxX, cordY+boxY, colRef.getRGB());
        Gui.drawRect(cordX+2, cordY+2, cordX+boxX-2, cordY+boxY-2, colIn.getRGB());

        if (text.length() > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(cordX + (double) boxX / 2 + textOffset.width, cordY + (double) boxY / 2 + textOffset.height, 0d);
            GlStateManager.scale(textScale, textScale, 0d);
            Esp.drawOverlayString(NewMainMenu.fntj, text, -NewMainMenu.fntj.getStringWidth(text) / 2, -3, 0xFFFFFF, S2Dtype.DEFAULT);
            GlStateManager.popMatrix();
        }
        return true;
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {
        if (Utils.pointInMovedDim(new Point(mouseX, mouseY), new Point(cordX, cordY), new Dimension(boxX, boxY)) && enabled) fun.run();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }
}
