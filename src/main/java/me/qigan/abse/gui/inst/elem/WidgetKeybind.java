package me.qigan.abse.gui.inst.elem;

import me.qigan.abse.Index;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.settings.GameSettings;

import java.awt.*;

public class WidgetKeybind extends WidgetButton{

    public final String keybindId;
    public int keyCode;
    private boolean selected = false;

    public WidgetKeybind(String id, int x, int y, int width, int height) {
        super(x, y, width, height, null);
        this.keybindId = id;
        try {
            this.keyCode = Index.KEY_MANAGER.get(id).keyCode;
        } catch (Exception ex) {}
    }

    @Override
    public boolean draw(int mouseX, int mouseY, float partialTicks) {
        if (selected) {
            this.text("> " + GameSettings.getKeyDisplayString(keyCode) + " <");
        } else {
            this.text(GameSettings.getKeyDisplayString(keyCode));
        }

        return super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {
        if (Utils.pointInMovedDim(new Point(mouseX, mouseY), new Point(cordX, cordY), new Dimension(boxX, boxY)) && enabled) {
            if (!selected) selected = true;
            else {
                keyCode = mouseButton-100;
                selected = false;
                updateKBD(this.keybindId, this.keyCode);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (selected) {
            this.keyCode = keyCode;
            selected = false;
            updateKBD(this.keybindId, this.keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    public static void updateKBD(String kbName, int code) {
        Index.KEY_MANAGER.set(kbName, code);
    }
}
