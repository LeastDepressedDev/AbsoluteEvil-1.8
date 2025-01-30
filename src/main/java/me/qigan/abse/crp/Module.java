package me.qigan.abse.crp;

import java.util.ArrayList;
import java.util.List;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.config.SetsData;
import me.qigan.abse.config.ValType;
import me.qigan.abse.config.WKeybind;

public abstract class Module {

	public static enum Category {

		MINIGAMES("Mini-games"),
		SKYBLOCK("Skyblock"),
		OTHER("Other")

		;

		public final String name;

        Category(String name) {
            this.name = name;
        }
    }

	public static enum Specification {
		//MINI-GAmes
		COMBAT(Category.MINIGAMES, "Combat"),
		BEDWARS(Category.MINIGAMES, "Bedwars"),

		//SKYBLOCK
		DUNGEONS(Category.SKYBLOCK, "Dungeons"),
		//OVERWORLD(Category.SKYBLOCK, "Overworld"),
		SB_QOL(Category.SKYBLOCK, "Qol"),
		MINING(Category.SKYBLOCK, "Mining"),

		//OTHER
		QOL(Category.OTHER, "Qol"),
		SPECIAL(Category.OTHER, "Special")

		;

		public final Category category;
		public final String name;

        Specification(Category category, String name) {
            this.category = category;
            this.name = name;
        }

		public static List<Specification> allByCategory(Category category) {
			List<Specification> specs = new ArrayList<>();
			for (Specification spec : Specification.values()) {
				if (spec.category == category) specs.add(spec);
			}
			return specs;
		}
    }
	
	public abstract String id();
	public abstract Specification category();
	public String fname() {
		return this.id();
	}
	public String renderName() {return this.fname();}
	public abstract String description();
	public boolean isEnabled() {
		return Index.MAIN_CFG.getBoolVal(id());
	}
	public List<SetsData<?>> sets() {
		return new ArrayList<>();
	}
	public void onRegister() {}
	public WKeybind moduleBind() {return Index.KEY_MANAGER.get(id());}
}
