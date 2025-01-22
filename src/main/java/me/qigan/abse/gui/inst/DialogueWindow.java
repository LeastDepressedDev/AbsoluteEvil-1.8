package me.qigan.abse.gui.inst;

import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.gui.QGuiScreen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueWindow extends QGuiScreen {

    public static Map<String, String> result = new HashMap<>();


    public DialogueWindow(QGuiScreen screen, List<AddressedData<String, ValType>> req) {
        super(screen);
    }

    @Override
    public void initGui() {



        super.initGui();
    }
}
