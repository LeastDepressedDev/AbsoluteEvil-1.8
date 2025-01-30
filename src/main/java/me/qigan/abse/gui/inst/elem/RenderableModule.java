package me.qigan.abse.gui.inst.elem;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.fr.macro.Macro;
import me.qigan.abse.gui.inst.NewMainMenu;
import me.qigan.abse.sync.Utils;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenderableModule extends WidgetUpdatable {

    public static int VERTICAL_GAP = 5;
    public static int INNER_GAP = 3;
    public static String UNSUPPORTED_STRING = "Unsupported!";

    public final Module module;
    public boolean optionOpened = false;

    public final WidgetSwitch sch;
    public final WidgetHoveringTextBox textBox;
    public final WidgetHoveringTextBox disablerState;

    public Point updatablePosition = new Point(0, 0);
    //This one doesn't reset on purpose
    public Point realCords = new Point(0, 0);

    public final List<WidgetUpdatable> triggers;

    public RenderableModule(Module module) {
        super(0, 0);
        box(335, 16);
        this.module = module;
        this.sch = new WidgetSwitch(310, 2, Index.MAIN_CFG.getBoolVal(this.module.id()), () -> Index.MAIN_CFG.toggle(module.id()));
        this.textBox = new WidgetHoveringTextBox(this.module.description(), 0, 0, boxX, boxY).timed(700);
        this.disablerState = new WidgetHoveringTextBox("\u00A7cTemporary disabled!", 310, 2,
                WidgetSwitch.CONST_SIZE_W, WidgetSwitch.CONST_SIZE_H);
        this.triggers = new ArrayList<>();
        for (int i = 0; i < module.sets().size(); i++) {
            SetsData<?> data = module.sets().get(i);
            int calcYPos = cordY+boxY+(INNER_GAP+10)*i+4;

            switch (data.dataType) {
                case BOOLEAN:
                    triggers.add(new WidgetSwitch(305, calcYPos, Index.MAIN_CFG.getBoolVal(data.setId), () -> Index.MAIN_CFG.toggle(data.setId)));
                    break;
                case BUTTON:
                    triggers.add(new WidgetButton(305, calcYPos, WidgetSwitch.CONST_SIZE_W, WidgetSwitch.CONST_SIZE_H, (Runnable) data.defVal));
                    break;
                case STRING:
                    triggers.add(new WidgetTextField(250, calcYPos, 85, WidgetSwitch.CONST_SIZE_H)
                            .setText(Index.MAIN_CFG.getStrVal(data.setId)).allowInstaSync());
                    break;
                case NUMBER:
                    triggers.add(new WidgetTextField(250, calcYPos, 85, WidgetSwitch.CONST_SIZE_H)
                            .setText(Index.MAIN_CFG.getStrVal(data.setId)).filter("-1234567890").allowInstaSync());
                    break;
                case DOUBLE_NUMBER:
                    triggers.add(new WidgetTextField(250, calcYPos, 85, WidgetSwitch.CONST_SIZE_H)
                            .setText(Index.MAIN_CFG.getStrVal(data.setId)).filter("-.1234567890").allowInstaSync());
                    break;

                case COMMENT:
                default:
                    triggers.add(null);
            }
        }
        triggers.add(new WidgetKeybind(module.id(), 305, cordY+boxY+(INNER_GAP+10)*module.sets().size()+4, WidgetSwitch.CONST_SIZE_W, WidgetSwitch.CONST_SIZE_H));
    }

    public void insertRealCords(Point point) {
        this.realCords = point;
    }

    public void insertRealCords(int x, int y) {
        insertRealCords(new Point(x, y));
    }

    @Override
    public boolean draw(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        Gui.drawRect(cordX, cordY, boxX, boxY+sizeFunction(), NewMainMenu.SEMI_BG_COL_1.getRGB());
        String fname = module.fname();

        Macro mac = module.getClass().getAnnotation(Macro.class);
        if (mac != null) fname += "\u00A7a\u2714";
        DangerousModule annot = module.getClass().getAnnotation(DangerousModule.class);
        if (annot != null) fname += "\u00A7c\u26A0";
        if (Debug.DISABLE_STATE.contains(module.id())) fname = "\u00A77" + fname;
        Esp.drawOverlayString(NewMainMenu.fntj, fname, cordX+2, cordY+4, 0xFFFFFF, S2Dtype.SHADOW);
        GlStateManager.pushMatrix();
        GlStateManager.translate(cordX+293, cordY+1, 0d);
        GlStateManager.scale(1.7d, 1.7d, 0d);
        if (!module.sets().isEmpty()) Esp.drawOverlayString(NewMainMenu.fntj, "\u2699", 0, 0, 0x00FF30, S2Dtype.CORNERED);
        GlStateManager.popMatrix();

        if (optionOpened) {
            Gui.drawRect(3, boxY+1, boxX-3, boxY+2, NewMainMenu.LINES_COL.getRGB());

            for (int i = 0; i < module.sets().size(); i++) {
                SetsData<?> data = module.sets().get(i);
                int calcYPos = cordY+boxY+(INNER_GAP+10)*i+4;
                Esp.drawOverlayString(NewMainMenu.fntj, data.guiName, cordX+2, calcYPos, 0xFFFFFF, S2Dtype.SHADOW);

                if (triggers.get(i) != null) triggers.get(i).draw(mouseX, mouseY, partialTicks);
            }
            triggers.get(module.sets().size()).draw(mouseX, mouseY, partialTicks);
        }



        sch.draw(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        doTheDraw(mouseX, mouseY, partialTicks);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();
        return true;
    }

    private void doTheDraw(int mx, int my, float t) {
        if (Debug.DISABLE_STATE.contains(module.id())) {
            if (disablerState.draw(mx, my, t)) return;
        }
        textBox.draw(mx, my, t);
    }

    public boolean isOptionOpened() {
        return optionOpened;
    }

    private int sizeFunction() {
        return (optionOpened ? (INNER_GAP+10)*(module.sets().size()+1)+INNER_GAP : 0);
    }

    public int calcSize() {
        return boxY+VERTICAL_GAP+sizeFunction();
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {
        mouseY-=updatablePosition.y;
        if (mouseButton == 1 && Utils.pointInMovedDim(new Point(mouseX-2, mouseY), new Point(0, 0), new Dimension(boxX,
                (optionOpened ? calcSize()-VERTICAL_GAP : boxY)))) optionOpened=!optionOpened;
        if (!Debug.DISABLE_STATE.contains(module.id())) this.sch.onClick(mouseX, mouseY, mouseButton);
        if (!optionOpened) return;
        for (int i = 0; i < triggers.size(); i++) {
            WidgetUpdatable upt = triggers.get(i);
            if (upt == null) continue;
            if (i == triggers.size()-1 || !Debug.DISABLE_STATE.contains(module.sets().get(i).setId)) upt.onClick(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (WidgetUpdatable upt : triggers) {
            if (upt == null) continue;
            upt.keyTyped(typedChar, keyCode);
        }
    }

    public void onClose() {
        for (int i = 0; i < module.sets().size(); i++) {
            WidgetUpdatable tg = triggers.get(i);
            if (tg == null) continue;
            if (tg instanceof WidgetTextField) {
                Index.MAIN_CFG.set(module.sets().get(i).setId, ((WidgetTextField) tg).innerText);
            }
        }
    }
}
