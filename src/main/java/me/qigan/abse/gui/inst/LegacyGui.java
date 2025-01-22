package me.qigan.abse.gui.inst;

import me.qigan.abse.Holder;
import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.DangerousModule;
import me.qigan.abse.crp.Module;
import me.qigan.abse.fr.Debug;
import me.qigan.abse.fr.macro.Macro;
import me.qigan.abse.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LegacyGui extends QGuiScreen {

    private static final Map<Integer, String> sch = new HashMap<>();
    private static final Map<Integer, String> tsc = new HashMap<>();

    public static Map<Integer, Runnable> actButtons = new HashMap<>();

    public static final int PAGE_SIZE = 16;

    private GuiButton pageUp;
    private GuiButton pageDown;
    private GuiButton posPicker;
    private GuiButton absoluteFix;
    private GuiButton mappingSettings;

    public final int page;



    /**
     * A bit of a constants
     */
    //sizeW - component width; sizeH - component height
    public static final int sizeW = 130;
    public static final int sizeH = 10;
    //Space between components
    public static final int comMove = 20;
    //Separator things
    public static final int sepSpace = 30;



    public static boolean queue = false;

    public static boolean opened = false;

    public LegacyGui(int page, QGuiScreen cls) {
        super(cls);
        this.page = page;
    }

    @Override
    public void initGui() {
        int Id = 1;
        opened = true;
        sch.clear();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int height = sr.getScaledHeight();
        int width = sr.getScaledWidth();

        // Id = 1
        pageDown = new GuiButton(Id, width/6*2, 20, 40, 20, "<");
        if (page <= 0) {
            pageDown.enabled = false;
        }
        buttonList.add(pageDown);
        Id++;
        // Id = 2
        pageUp = new GuiButton(Id, width/6*4, 20, 40, 20, ">");
        if ((page+1)*PAGE_SIZE > Holder.MRL.size()) {
            pageUp.enabled = false;
        }
        buttonList.add(pageUp);
        Id++;
        // Id = 3
        posPicker = new GuiButton(Id, width-100, height-20, 100, 20, "Position picker");
        buttonList.add(posPicker);
        Id++;
        // Id = 4
        absoluteFix = new GuiButton(Id, 0, height-20, 100, 20, "Absolute fix");
        buttonList.add(absoluteFix);
        Id++;
        textBoxList.add(new HoveringTextBox(0, height-20, 100, 20, "\u00A7aIf somethings breaks, just press it!"));
        // Id = 5
        mappingSettings = new GuiButton(Id, 0, height-40, 100, 20, "Mapping settings");
        mappingSettings.enabled = false;
        buttonList.add(mappingSettings);
        Id++;

        int i = 0;
        for (int u = PAGE_SIZE * page; u < Holder.MRL.size() && u < PAGE_SIZE*(page+1); u++) {
            Module mod = Holder.MRL.get(u);
            textBoxList.add(new HoveringTextBox(40, 60 + i*2*sizeH, sizeW, sizeH, "\u00A7f" + mod.description()));
            GuiLabel label = new GuiLabel(fontRendererObj, Id, 40, 60 + i*2*sizeH, sizeW, sizeH, 0xFFFFFF);
            Macro mac = mod.getClass().getAnnotation(Macro.class);
            label.func_175202_a(((mac != null) ? "\u00A7a" : "") + mod.fname() + ":");
            labelList.add(label);
            Id++;
            DangerousModule annot = mod.getClass().getAnnotation(DangerousModule.class);
            if (annot != null) {
                GuiLabel dang = new GuiLabel(fontRendererObj, Id, 0, 60 + i*2*sizeH - sizeH, sizeW, sizeH, 0xFF0000);
                dang.func_175202_a("Dangerous!");
                labelList.add(dang);
                Id++;
            }
            GuiButton button = new GuiButton(Id, 60+sizeW, 60 + i*2*sizeH, sizeW, sizeH, "\u00A7l" + (mod.isEnabled() ? "\u00A7a ON" : "\u00A7c OFF"));
            if (Debug.DISABLE_STATE.contains(mod.id())) {
                button.enabled = false;
                textBoxList.add(new HoveringTextBox(60+sizeW, 60 + i*2*sizeH, sizeW, sizeH, "\u00A7f\u00A7cTemporary disabled!"));
            }
            sch.put(button.id, mod.id());
            buttonList.add(button);
            Id++;

            //const move size = 100
            int sumSize = 100 + 2*sizeW;
            for (SetsData<?> ddr : mod.sets()) {
                String call = ddr.guiName + (ddr.dataType == ValType.COMMENT ? "" : ":");
                int size = Minecraft.getMinecraft().fontRendererObj.getStringWidth(call);
                int cumSize;
                switch (ddr.dataType) {
                    case BUTTON:
                        cumSize = sizeW;
                        break;
                    case BOOLEAN:
                    case STRING:
                    case NUMBER:
                    case DOUBLE_NUMBER:
                        cumSize = size + comMove + sizeW/2;
                        break;
                    case COMMENT:
                        cumSize = size;
                        break;
                    default:
                        cumSize = Minecraft.getMinecraft().fontRendererObj.getStringWidth(ddr.guiName + ": Unsupported");
                        break;
                }
                if (sumSize + cumSize > width) {i++; sumSize = 60;}
                switch (ddr.dataType) {
                    case BOOLEAN:
                    {
                        GuiLabel label1 = new GuiLabel(fontRendererObj, Id, sumSize, 60 + i*2*sizeH, sizeW/2, sizeH, 0xFFFFFF);
                        label1.func_175202_a(call);
                        labelList.add(label1);
                        Id++;
                        GuiButton button1 = new GuiButton(Id, sumSize+size+comMove, 60 + i*2*sizeH, sizeW/2, sizeH,
                                "\u00A7l" + (Index.MAIN_CFG.getBoolVal(ddr.setId) ? "\u00A7a ON" : "\u00A7c OFF"));
                        if (Debug.DISABLE_STATE.contains(ddr.setId)) {
                            button1.enabled = false;
                            textBoxList.add(new HoveringTextBox(sumSize+size+comMove, 60 + i*2*sizeH, sizeW/2, sizeH, "\u00A7f\u00A7cTemporary disabled!"));
                        }
                        sch.put(button1.id, ddr.setId);
                        buttonList.add(button1);
                        Id++;
                    }
                    break;
                    case STRING:
                    {
                        GuiLabel label1 = new GuiLabel(fontRendererObj, Id, sumSize, 60 + i*2*sizeH, sizeW/2, sizeH, 0xFFFFFF);
                        label1.func_175202_a(call);
                        labelList.add(label1);
                        Id++;
                        GuiTextField textField = new GuiTextField(Id, fontRendererObj, sumSize+size+comMove, 60 + i*2*sizeH, sizeW/2, sizeH);
                        textField.setText(Index.MAIN_CFG.getStrVal(ddr.setId));
                        textField.setMaxStringLength(256);
                        tsc.put(textField.getId(), ddr.setId);
                        textFieldList.add(textField);
                        Id++;
                    }
                    break;
                    case NUMBER:
                    {
                        GuiLabel label1 = new GuiLabel(fontRendererObj, Id, sumSize, 60 + i*2*sizeH, sizeW/2, sizeH, 0xFFFFFF);
                        label1.func_175202_a(call);
                        labelList.add(label1);
                        Id++;
                        GuiNumberField textField = new GuiNumberField(Id, fontRendererObj, sumSize+size+comMove, 60 + i*2*sizeH, sizeW/2, sizeH);
                        textField.setText(Index.MAIN_CFG.getStrVal(ddr.setId));
                        textField.setMaxStringLength(256);
                        tsc.put(textField.getId(), ddr.setId);
                        textFieldList.add(textField);
                        Id++;
                    }
                    break;
                    case DOUBLE_NUMBER:
                    {
                        GuiLabel label1 = new GuiLabel(fontRendererObj, Id, sumSize, 60 + i*2*sizeH, sizeW/2, sizeH, 0xFFFFFF);
                        label1.func_175202_a(call);
                        labelList.add(label1);
                        Id++;
                        GuiDoubleNumberField textField = new GuiDoubleNumberField(Id, fontRendererObj, sumSize+size+comMove, 60 + i*2*sizeH, sizeW/2, sizeH);
                        textField.setText(Index.MAIN_CFG.getStrVal(ddr.setId));
                        textField.setMaxStringLength(256);
                        tsc.put(textField.getId(), ddr.setId);
                        textFieldList.add(textField);
                        Id++;
                    }
                    break;
                    case BUTTON:
                    {
                        GuiButton button1 = new GuiButton(Id, sumSize, 60 + i*2*sizeH, sizeW, sizeH, ddr.guiName);
                        if (Debug.DISABLE_STATE.contains(ddr.setId)) {
                            button1.enabled = false;
                            textBoxList.add(new HoveringTextBox(sumSize, 60 + i*2*sizeH, sizeW, sizeH, "\u00A7f\u00A7cTemporary disabled!"));
                        }
                        sch.put(button1.id, ddr.setId);
                        actButtons.put(button1.id, (Runnable) ddr.defVal);
                        buttonList.add(button1);
                        Id++;
                    }
                    break;
                    case COMMENT:
                    {
                        GuiLabel label1 = new GuiLabel(fontRendererObj, Id, sumSize, 60 + i*2*sizeH, sizeW/2, sizeH, 0xFFFFFF);
                        label1.func_175202_a(call);
                        labelList.add(label1);
                        Id++;
                    }
                    break;
                    default:
                    {
                        GuiLabel label1 = new GuiLabel(fontRendererObj, Id, sumSize, 60 + i*2*sizeH, sizeW/2, sizeH, 0xFFFFFF);
                        label1.func_175202_a(ddr.guiName + ": Unsupported");
                        labelList.add(label1);
                        Id++;
                    }
                    break;
                }
                sumSize += cumSize + sepSpace;
            }

            i++;
        }
        super.initGui();
    }

    private void reopen(final int page) {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new LegacyGui(page, null)));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (Index.MAIN_CFG.getBoolVal("debug")) System.out.println(typedChar + " - " + keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        super.drawDefaultBackground();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        double height = sr.getScaledHeight();
        double width = sr.getScaledWidth();

        drawString(Minecraft.getMinecraft().fontRendererObj, "\u00A7l\u00A7aAbsolute Evil", (int) width/2-25, 10, 0xFFFFFF);

//        Esp.drawModalRectWithCustomSizedTexture(-100, -100, (float) width + 100, (float) height + 100, 0, 0, 640, 404,
//                new ResourceLocation("abse", "test.png"), new MColor(1, 1, 1, 1f));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        for (GuiTextField textField : textFieldList) {
            Index.MAIN_CFG.set(tsc.get(textField.getId()), textField.getText());
        }

        opened = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 1:
                if (page > 0) {
                    reopen(page-1);
                }
                break;
            case 2:
                if ((page+1)*PAGE_SIZE <= Holder.MRL.size()) {
                    reopen(page+1);
                }
                break;
            case 3:
                Minecraft.getMinecraft().displayGuiScreen(new PositionsGui(this));
                break;
            case 4:
                Index.absoluteFix();
                break;
            case 5:
                Minecraft.getMinecraft().displayGuiScreen(new MappingSettings(this));
                break;
            default:
                if (actButtons.containsKey(button.id)) {
                    actButtons.get(button.id).run();
                } else {
                    button.displayString = (button.displayString.contains("ON") ? "\u00A7l\u00A7c OFF" : "\u00A7l\u00A7a ON");
                    Index.MAIN_CFG.toggle(sch.get(button.id));
                }
                break;
        }
    }
}
