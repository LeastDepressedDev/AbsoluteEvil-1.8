package me.qigan.abse.config;

public class WKeybind {
    public int keyCode;
    public final String kbName;
    public String displayName = "";
    private boolean isDown = false;
    private boolean pressed = false;

    public WKeybind(String kbName, int keyCode) {
        this.keyCode = keyCode;
        this.kbName = kbName;
    }

    public WKeybind setDisplayName(String name) {
        this.displayName = name;
        return this;
    }

    public String getDisplayName() {
        return displayName.isEmpty() ? kbName : displayName;
    }

    public void update(boolean state) {
        if (state && !isDown) this.pressed = true;
        this.isDown = state;
    }

    public boolean isPressed() {
        if (!pressed) return false;
        this.pressed = false;
        return true;
    }

    public boolean isDown() {
        return isDown;
    }
}
