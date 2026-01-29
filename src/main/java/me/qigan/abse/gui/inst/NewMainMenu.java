package me.qigan.abse.gui.inst;

import me.qigan.abse.Holder;
import me.qigan.abse.Index;
import me.qigan.abse.crp.Module;
import me.qigan.abse.gui.QGuiScreen;
import me.qigan.abse.gui.inst.elem.*;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewMainMenu extends QGuiScreen {

    public static FontRenderer fntj = Minecraft.getMinecraft().fontRendererObj;

    //Colors
    public static Color BG_COL_1 = new Color(32, 106, 125);
    public static Color LINES_COL = new Color(41, 73, 84);
    public static Color LL_COL = new Color(12, 54, 55);
    public static Color SEMI_BG_COL_1 = new Color(2, 34, 35);
    public static Color BG_COL_2 = new Color(4, 17, 20);

    //Collections
    public List<WidgetElement> elements = new ArrayList<>();
    public static List<RenderableModule> modToRender = null;

    //Shit for rescaling
    public static double scaleFactorW = 1;
    public static double scaleFactorH = 1;

    //Points and triggers
    private Point clickDef = null;
    private Point matrixSavePrev = null;
    private Point lastPosMousePt = null;
    private boolean translationTrigger = false;

    //Matrix related
    private static Dimension PREV_DIM;

    public static Point MATRIX_BEGIN = null;
    public static Dimension MATRIX_SIZES = new Dimension(600, 400);
    public static int scroll = 0;
    public static int scrollSpeed = 12;

    //Req for opening
    public static boolean queue = false;

    //Inner gui related stuff
    public static final String CONFIG_HELP_STRING = "All configs are located in \"config/abse/configs\".\nTo select config for future actions \n click on its name in config list.";
    public static Module.Specification selectedCategory = Module.Specification.COMBAT;
    public static int viewMode = 0;

    public static WidgetUpperBar upperBar = new WidgetUpperBar();
    public static WidgetOpenSelector configSelector;

    public NewMainMenu(QGuiScreen screen) {
        super(screen);
    }

    public NewMainMenu(GuiScreen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void initGui() {
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        scaleFactorW = (double) res.getScaledWidth() / 960;
        scaleFactorH = (double) res.getScaledHeight() / 600;
        scale(res);

        elements.add(new WidgetText("ABSE", 5, 6).scale(6, 6.5));
        elements.add(new WidgetText("Absolute Evil v" + Index.VERSION, 1,
                (int) (MATRIX_SIZES.height-((float)MATRIX_SIZES.height/50))-8).recolor(Color.darkGray));

        elements.add(new WidgetButton((int) (5*MATRIX_SIZES.width/6f)+2, (int) (MATRIX_SIZES.height-((float)MATRIX_SIZES.height/50))-20,
                (int) (MATRIX_SIZES.width/6d-11), 20, Index::absoluteFix).textScale(1.2).text("Absolute fix"));
        elements.add(new WidgetHoveringTextBox("\u00A7aIf somethings breaks, just press it!", (int) (5*MATRIX_SIZES.width/6f)+2, (int) (MATRIX_SIZES.height-((float)MATRIX_SIZES.height/50))-20,
                (int) (MATRIX_SIZES.width/6d-11), 20));

        elements.add(new WidgetButton((int) (MATRIX_SIZES.width-((float) MATRIX_SIZES.width/70))-50, 10, 40, 40, () -> Minecraft.getMinecraft().displayGuiScreen(new PositionsGui(this)))
                .textScale(4.1d).text("\u2693").textOffset(2, -3));
        elements.add(new WidgetHoveringTextBox("Position settings gui", (int) (MATRIX_SIZES.width-((float) MATRIX_SIZES.width/70))-50, 10, 40, 40));

        elements.add(new WidgetCategoryRenderer());
        elements.add(upperBar);
        configSelector = new WidgetOpenSelector((int) (5*MATRIX_SIZES.width/6f)+6, (int) (MATRIX_SIZES.height/7)+12,
                (int) (MATRIX_SIZES.width/6d-19), 200, buildCfgList()).setPageSize(10).doRenderPages();
        elements.add(configSelector);

        elements.add(new WidgetButton((int) (5*MATRIX_SIZES.width/6f)+6, configSelector.cordY + configSelector.boxY + 12,
                (int) (MATRIX_SIZES.width/6d-19), 20, ConfigActions::load).text("Load").textScale(1.2f));
        elements.add(new WidgetButton((int) (5*MATRIX_SIZES.width/6f)+6, configSelector.cordY + configSelector.boxY + 35,
                (int) (MATRIX_SIZES.width/6d-19), 20, ConfigActions::save).text("Save").textScale(1.2f));
        elements.add(new WidgetButton((int) (5*MATRIX_SIZES.width/6f)+6, configSelector.cordY + configSelector.boxY + 58,
                (int) (MATRIX_SIZES.width/6d-19), 20, ConfigActions::create).text("Create").textScale(1.2f));

        elements.add(new WidgetText("\u00A77[?]", (int) (5*MATRIX_SIZES.width/6f)+6, (int) (MATRIX_SIZES.height/7)+4));
        elements.add(new WidgetHoveringTextBox(CONFIG_HELP_STRING, (int) (5*MATRIX_SIZES.width/6f)+6, (int) (MATRIX_SIZES.height/7)+4, 15, 6));

        if (modToRender == null) updateRenderedModules();

        super.initGui();
    }

    public static class ConfigActions {
        public static void load() {
            if (configSelector.sel == -1) {
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cFailed to load! Config was not selected!"));
            } else {
                String cfgName = configSelector.opts.get(configSelector.sel);
                Index.CFG_MANAGER.loadFrom(cfgName);
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7aLoaded config: \u00A76" + cfgName));
                updateRenderedModules();
            }
        }

        public static void save() {
            if (configSelector.sel == -1) {
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7cFailed to save! Config was not selected!"));
            } else {
                String cfgName = configSelector.opts.get(configSelector.sel);
                Index.CFG_MANAGER.saveTo(cfgName);
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7aSaved config: \u00A76" + cfgName));
            }
        }

        public static void create() {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7eCOMING SOON! Create config manually in config/abse/configs."));
        }
    }

    public static List<String> buildCfgList() {
        List<String> list = new ArrayList<>();
        File[] files = Index.CFG_MANAGER.getConfigFiles();
        for (File file : files) list.add(file.getName().substring(0, file.getName().length()-4));
        return list;
    }

    private static int compareInnerText(String in, String obj) {
        obj = obj.toLowerCase(Locale.ROOT);
        in = in.toLowerCase(Locale.ROOT);
        if (obj.startsWith(in)) return 0;

        String[] parts = obj.split(" ");

        for (String pt : parts) {
            if (pt.startsWith(in)) return 1;
        }

        return obj.contains(in) ? 2 : -1;
    }

    public static void updateRenderedModules() {
        scroll = 0;
        modToRender = new ArrayList<>();

        //TODO: Rewrite and optimize this shitty search!
        List<RenderableModule> zeroCat = new ArrayList<>();
        List<RenderableModule> firstCat = new ArrayList<>();
        List<RenderableModule> secondCat = new ArrayList<>();
        for (Module mod : Holder.MRL) {
            if (mod.category() == selectedCategory && viewMode == 0 || viewMode == 1) {
                int cmp = compareInnerText(upperBar.searchBar.innerText, mod.fname());
                if (cmp == 0 || mod.id().startsWith(upperBar.searchBar.innerText))
                    zeroCat.add(new RenderableModule(mod));
                else if (cmp == 1)
                    firstCat.add(new RenderableModule(mod));
                else if (cmp == 2)
                    secondCat.add(new RenderableModule(mod));
            }
        }

        modToRender.addAll(zeroCat);
        modToRender.addAll(firstCat);
        modToRender.addAll(secondCat);
    }

    /**
     * Function to change gui position relative to current Window size.
     * Works by m̶a̶g̶i̶c̶math
     */
    private void scale(ScaledResolution res) {
        if (MATRIX_BEGIN == null || res.getScaledWidth() != PREV_DIM.width || res.getScaledHeight() != PREV_DIM.height) {
            MATRIX_BEGIN = new Point((int) ((double) res.getScaledWidth()/2-((double) MATRIX_SIZES.width/2)),
                    (int) ((double) res.getScaledHeight()/2-((double) MATRIX_SIZES.height/2)));
            PREV_DIM = new Dimension(res.getScaledWidth(), res.getScaledHeight());
        }
    }

    private void drawGuiShape() {
        int l_bound_w = (int) (MATRIX_SIZES.width-((float) MATRIX_SIZES.width/70));
        int l_bound_h = (int) (MATRIX_SIZES.height-((float)MATRIX_SIZES.height/50));
        //Main shapes
        Gui.drawRect(0, 0, l_bound_w, l_bound_h, BG_COL_2.getRGB());
        Gui.drawRect(0, 0, (int) (MATRIX_SIZES.width/4f), l_bound_h, SEMI_BG_COL_1.getRGB());
        Gui.drawRect(0, 0, (int) (MATRIX_SIZES.width/4f), (MATRIX_SIZES.height/7), LL_COL.getRGB());

        //Lines
        Gui.drawRect((int) (MATRIX_SIZES.width/4f)-2, 0, (int) (MATRIX_SIZES.width/4f), l_bound_h, LINES_COL.getRGB());
        Gui.drawRect(0, (MATRIX_SIZES.height/7), l_bound_w, (MATRIX_SIZES.height/7)+2, LINES_COL.getRGB());
        //    Config line!!!
        Gui.drawRect((int) (5*MATRIX_SIZES.width/6f), (MATRIX_SIZES.height/7), (int) (5*MATRIX_SIZES.width/6f)+2, l_bound_h, LINES_COL.getRGB());
    }

    private void drawModules(int mouseX, int mouseY, float partialTicks, Point innerPoint) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((int) (MATRIX_SIZES.width/4f)+10, (int) (MATRIX_SIZES.height/7f)+15-scroll, 0d);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, (int) ((new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight()-MATRIX_BEGIN.y-(MATRIX_SIZES.height-4)*scaleFactorH)*Minecraft.getMinecraft().gameSettings.guiScale),
                100000, (int) ((6d*MATRIX_SIZES.height/7d-15)*scaleFactorH*Minecraft.getMinecraft().gameSettings.guiScale));
        int d = 0;
        for (RenderableModule mod : modToRender) {
            GlStateManager.pushMatrix();
            mod.updatablePosition = new Point(0, d);
            mod.insertRealCords(mouseX, mouseY+scroll);
            GlStateManager.translate(0, d, 0d);
            //Why -8? Shut up and don't ask stupid questions
            mod.draw(innerPoint.x-(int) (MATRIX_SIZES.width/4f)-5, innerPoint.y-d-(int) (MATRIX_SIZES.height/7f)-8+scroll, partialTicks);
            GlStateManager.popMatrix();

            d+=mod.calcSize();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lastPosMousePt = new Point(mouseX, mouseY);

        GlStateManager.pushMatrix();
        GlStateManager.translate(MATRIX_BEGIN.x, MATRIX_BEGIN.y, 0f);
        GlStateManager.scale(scaleFactorW, scaleFactorH, 0d);
        //Corner
        Gui.drawRect(0, 0, MATRIX_SIZES.width, MATRIX_SIZES.height, BG_COL_1.getRGB());
            GlStateManager.pushMatrix();
        GlStateManager.translate(((float) MATRIX_SIZES.width/140), ((float) MATRIX_SIZES.height/70), 0f);

        drawGuiShape();

        Point innerCords = Utils.scaleDim(new Point(mouseX-MATRIX_BEGIN.x, mouseY-MATRIX_BEGIN.y), 1/scaleFactorW, 1/scaleFactorH);
        innerCords.x-=3;
        innerCords.y-=6;
        List<WidgetUpdatable> postRender = new ArrayList<>();
        for (WidgetElement elem : elements) {
            if (elem instanceof WidgetUpdatable) {
                if (elem instanceof WidgetHoveringTextBox) {
                    postRender.add((WidgetUpdatable) elem);
                    continue;
                }
                ((WidgetUpdatable) elem).draw(innerCords.x, innerCords.y, partialTicks);
            }
        }
            GlStateManager.popMatrix();


        drawModules(mouseX, mouseY, partialTicks, innerCords);

            GlStateManager.pushMatrix();
        GlStateManager.translate(((float) MATRIX_SIZES.width/140), ((float) MATRIX_SIZES.height/70), 0f);
        for (WidgetUpdatable post : postRender) {
            if (post instanceof WidgetHoveringTextBox) {
                ((WidgetHoveringTextBox) post).insertRealCords(mouseX, mouseY);
            }
            post.draw(innerCords.x, innerCords.y, partialTicks);
        }
            GlStateManager.popMatrix();
        GlStateManager.popMatrix();

//    Debug render things
//        Esp.drawOverlayString(new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() + "||" + new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight(),
//                10, 10, 0xFFFFFF, S2Dtype.CORNERED);
//        Esp.drawOverlayString(innerCords.x + "||" + innerCords.y,
//                10, 25, 0xFFFFFF, S2Dtype.CORNERED);


//        GlStateManager.pushMatrix();
//        Tessellator lvt_9_1_ = Tessellator.getInstance();
//        WorldRenderer lvt_10_1_ = lvt_9_1_.getWorldRenderer();
//        GlStateManager.enableBlend();
//        GlStateManager.disableTexture2D();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//        GlStateManager.shadeModel(GL11.GL_SMOOTH);
//        //GlStateManager.color(lvt_6_1_, lvt_7_1_, lvt_8_1_, lvt_5_3_);
//        lvt_10_1_.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
//        lvt_10_1_.pos((double) 200, (double) 200, 0.0).color(1f, 1f, 1f, 1f).endVertex();
//        for (double alpha = 0; alpha <= 2*Math.PI; alpha+=Math.PI/32) {
//            lvt_10_1_.pos((double) 200+50*Math.cos(-alpha), (double) 200+50*Math.sin(-alpha), 0.0).color(1f, 1f, 1f, (float) alpha/(float)Math.PI/2f).endVertex();
//        }
//        lvt_9_1_.draw();
//        GlStateManager.enableTexture2D();
//        GlStateManager.disableBlend();
//        GlStateManager.shadeModel(GL11.GL_FLAT);
//        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        clickDef = new Point(mouseX, mouseY);
        Point realCords = new Point(mouseX-MATRIX_BEGIN.x, mouseY-MATRIX_BEGIN.y);
        Point innerCords = Utils.scaleDim(realCords, 1/scaleFactorW, 1/scaleFactorH);
        innerCords.x-=3;
        innerCords.y-=6;
        matrixSavePrev = new Point(MATRIX_BEGIN);
        translationTrigger = Utils.pointInMovedDim(clickDef, MATRIX_BEGIN, Utils.scaleDim(MATRIX_SIZES, scaleFactorW, scaleFactorH))
                &&
                !Utils.pointInMovedDim(clickDef,
                new Point((int) ((MATRIX_BEGIN.x+(float) MATRIX_SIZES.width/40)), (int) ((MATRIX_BEGIN.y+(float) MATRIX_SIZES.height/40))),
                Utils.scaleDim(new Dimension((int) ((MATRIX_SIZES.width-2*((float) MATRIX_SIZES.width/40))),
                        (int) ((MATRIX_SIZES.height-2*((float) MATRIX_SIZES.height/40)))), scaleFactorW, scaleFactorH)
                );

        Dimension subDim = new Dimension((int) (5*MATRIX_SIZES.width/6f)+2-((int) (MATRIX_SIZES.width/4f)+10),
                (int) (MATRIX_SIZES.height-((float)MATRIX_SIZES.height/50)-(int) (MATRIX_SIZES.height/7f)));
        if (Utils.pointInMovedDim(innerCords, new Point((int) (MATRIX_SIZES.width/4f)+10, (int) (MATRIX_SIZES.height/7f)), subDim)) {
            for (WidgetElement elem : elements) {
                if (elem instanceof WidgetTextField) ((WidgetTextField) elem).deselect();
            }
            upperBar.searchBar.deselect();

            for (RenderableModule elem : modToRender) {
                elem.onClick(innerCords.x - (int) (MATRIX_SIZES.width / 4f) - 10, innerCords.y - (int) (MATRIX_SIZES.height / 7f) - 10 + scroll, mouseButton);
            }
        } else {
            for (WidgetElement elem : elements) {
                ((WidgetUpdatable) elem).onClick(innerCords.x, innerCords.y, mouseButton);
            }

            for (RenderableModule elem : modToRender) {
                for (WidgetUpdatable upt : elem.triggers) {
                    if (upt instanceof WidgetTextField) ((WidgetTextField) upt).deselect();
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        translationTrigger = false;
        clickDef = null;
        matrixSavePrev = null;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (translationTrigger) MATRIX_BEGIN = Utils.diff(matrixSavePrev, new Point( clickDef.x - mouseX, clickDef.y - mouseY));
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (WidgetElement elem : elements) {
            if (elem instanceof WidgetKeybind) {
                if (((WidgetKeybind) elem).isSelected()) {
                    ((WidgetKeybind) elem).keyTyped(typedChar, keyCode);
                    return;
                }
            }
        }
        for (RenderableModule rm : modToRender) {
            for (WidgetUpdatable upt : rm.triggers) {
                if (upt instanceof WidgetKeybind) {
                    if (((WidgetKeybind) upt).isSelected()) {
                        ((WidgetKeybind) upt).keyTyped(typedChar, keyCode);
                        return;
                    }
                }
            }
        }
        for (WidgetElement elem : elements) {
            if (elem instanceof WidgetUpdatable) {
                ((WidgetUpdatable) elem).keyTyped(typedChar, keyCode);
            }
        }
        for (RenderableModule rm : modToRender) {
            rm.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (lastPosMousePt == null) return;
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        int dwheel = Mouse.getEventDWheel();
        if (dwheel != 0) {
            Point realCords = new Point(lastPosMousePt.x-MATRIX_BEGIN.x, lastPosMousePt.y-MATRIX_BEGIN.y);
            Point innerCords = Utils.scaleDim(realCords, 1/scaleFactorW, 1/scaleFactorH);
            innerCords.x-=3;
            innerCords.y-=6;
            Dimension subDim = new Dimension((int) (5*MATRIX_SIZES.width/6f)+2-((int) (MATRIX_SIZES.width/4f)+10),
                    (int) (MATRIX_SIZES.height-((float)MATRIX_SIZES.height/50)-(int) (MATRIX_SIZES.height/7f)));
            if (Utils.pointInMovedDim(innerCords, new Point((int) (MATRIX_SIZES.width/4f)+10, (int) (MATRIX_SIZES.height/7f)), subDim)) {
                if (dwheel < 0) {
                    if (scroll + scrollSpeed < 1000) scroll += scrollSpeed;
                } else {
                    if (scroll - scrollSpeed >= 0) scroll -= scrollSpeed;
                }
            }
        }
    }

    public static void forceSaveTextFields() {
        for (RenderableModule module : modToRender) module.onClose();
    }

    @Override
    public void onGuiClosed() {
        forceSaveTextFields();
        super.onGuiClosed();
    }
}
