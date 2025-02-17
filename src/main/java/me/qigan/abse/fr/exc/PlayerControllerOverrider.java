package me.qigan.abse.fr.exc;

public class PlayerControllerOverrider {
    public boolean globalToggle = false;

    /**
     * 0 - forward
     * 1 - back
     * 2 - left
     * 3 - right
     */
    public boolean[] goStateOvr;
    public boolean sneak = false;
    public boolean jump = false;

    public PlayerControllerOverrider() {
        this.goStateOvr = new boolean[]{false, false, false, false};
    }

    public void reset() {
        globalToggle = false;
        stop();
    }

    public void dropPos() {
        this.goStateOvr = new boolean[]{false, false, false, false};
    }

    public void dropStates() {
        this.sneak = false;
        this.jump = false;
    }

    public void stop() {
        dropPos(); dropStates();
    }
}
