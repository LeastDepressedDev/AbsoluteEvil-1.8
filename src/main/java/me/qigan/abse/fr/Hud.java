package me.qigan.abse.fr;

import me.qigan.abse.Holder;
import me.qigan.abse.Index;
import me.qigan.abse.config.WKeybind;
import me.qigan.abse.crp.EnabledByDefault;
import me.qigan.abse.crp.Module;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.fr.cbh.CombatHelperAim;
import me.qigan.abse.vp.Esp;
import me.qigan.abse.vp.S2Dtype;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@EnabledByDefault
public class Hud extends Module{
    @Override
    public String id() {
        return "hud";
    }

    @Override
    public Specification category() {
        return Specification.SPECIAL;
    }

    @Override
    public String fname() {
        return "Hud";
    }

    @Override
    public String description() {
        return "Hud thingies";
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void rov(RenderGameOverlayEvent.Text e) {
        if (!isEnabled()) return;
        List<String> lines = new ArrayList<>();
        for (Module mdl : Holder.MRL) {
            if (mdl.isEnabled()) {
                if (mdl.fname().equalsIgnoreCase(mdl.renderName())) lines.add("\u00A7a" + mdl.fname());
                else lines.add(mdl.renderName());
            }
        }

        WKeybind bind1 = Index.KEY_MANAGER.get("unlimitedRange");
        WKeybind bind2 = Index.KEY_MANAGER.get("aimBreak");
        if (bind1.isDown()) lines.add("\u00A7cRender distance++");
        if (bind2.isDown() && !Index.MAIN_CFG.getBoolVal("cbh_aim_tbkm") ||
                Index.MAIN_CFG.getBoolVal("cbh_aim_tbkm") && CombatHelperAim.BREAK_TOGGLE)
            lines.add("\u00A7cAim break! " + (Index.MAIN_CFG.getBoolVal("cbh_aim_tbkm") ? "[toggle]" : "[hold]"));

        Point point = Index.POS_CFG.calc("module_list");
        Esp.drawAllignedTextList(lines, point.x, point.y, true, e.resolution, S2Dtype.SHADOW);
    }
}
