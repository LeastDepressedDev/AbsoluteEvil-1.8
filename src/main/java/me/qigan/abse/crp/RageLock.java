package me.qigan.abse.crp;

@EnabledByDefault
public class RageLock extends Module{
    @Override
    public String id() {
        return "rage_lock";
    }

    @Override
    public Specification category() {
        return Specification.SPECIAL;
    }

    @Override
    public String fname() {
        return "!Rage lock!";
    }

    @Override
    public String description() {
        return "Locks all the rage utilities.";
    }
}
