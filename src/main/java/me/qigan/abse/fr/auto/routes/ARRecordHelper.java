package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.crp.Module;

public class ARRecordHelper extends Module {



    @Override
    public String id() {
        return "arr_h";
    }

    @Override
    public String fname() {
        return "AR Helper";
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public String description() {
        return "Helps you with recording";
    }
}
