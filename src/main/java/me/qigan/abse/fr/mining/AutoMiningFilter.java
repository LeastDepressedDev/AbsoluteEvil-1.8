package me.qigan.abse.fr.mining;

import me.qigan.abse.Index;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.crp.EnabledByDefault;
import me.qigan.abse.crp.Module;
import net.minecraft.item.EnumDyeColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EnabledByDefault
public class AutoMiningFilter extends Module {

    public static Collection<EnumDyeColor> allowedColors() {
        List<EnumDyeColor> colors = new ArrayList<>();
        if (Index.MAIN_CFG.getBoolVal("auto_mining_fil_ruby")) colors.add(EnumDyeColor.RED);
        if (Index.MAIN_CFG.getBoolVal("auto_mining_fil_amb")) colors.add(EnumDyeColor.ORANGE);
        if (Index.MAIN_CFG.getBoolVal("auto_mining_fil_sap")) colors.add(EnumDyeColor.LIGHT_BLUE);
        if (Index.MAIN_CFG.getBoolVal("auto_mining_fil_am")) colors.add(EnumDyeColor.PURPLE);
        if (Index.MAIN_CFG.getBoolVal("auto_mining_fil_jade")) colors.add(EnumDyeColor.LIME);
        if (Index.MAIN_CFG.getBoolVal("auto_mining_fil_top")) colors.add(EnumDyeColor.YELLOW);
        return colors;
    }

    @Override
    public List<SetsData<?>> sets() {
        List<SetsData<?>> list = new ArrayList<>();
        list.add(new SetsData<>("auto_mining_fil_ruby", "Ruby", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_fil_amb", "Amber", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_fil_sap", "Sapphire", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_fil_am", "Amethyst", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_fil_jade", "Jade", ValType.BOOLEAN, "true"));
        list.add(new SetsData<>("auto_mining_fil_top", "Topaz", ValType.BOOLEAN, "true"));
        return list;
    }

    @Override
    public String id() {
        return "auto_mining_fil";
    }

    @Override
    public String fname() {
        return "Mining filter settings";
    }

    @Override
    public Specification category() {
        return Specification.AUTO;
    }

    @Override
    public String description() {
        return "Filter settings for auto mining";
    }
}
