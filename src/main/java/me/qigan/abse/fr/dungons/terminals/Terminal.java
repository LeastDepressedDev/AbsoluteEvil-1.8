package me.qigan.abse.fr.dungons.terminals;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Terminal {

    public static enum Linker {

        NUMBERS(Pattern.compile("Click in order!", Pattern.CASE_INSENSITIVE), NumbersTerminal.class),
        COLORS(Pattern.compile("Select all the (.+?) items!", Pattern.CASE_INSENSITIVE), ColorsTerminal.class),
        START_WITH(Pattern.compile("What starts with: '(.+?)'\\?", Pattern.CASE_INSENSITIVE), StartWithTerminal.class),
        CORRECT_ALL(Pattern.compile("Correct all the panes!", Pattern.CASE_INSENSITIVE), CorrectAllTerminal.class),
        RUBIX(Pattern.compile("Change all to same color!", Pattern.CASE_INSENSITIVE), RubixTerminal.class),
        MELODY(Pattern.compile("Click the button on time!", Pattern.CASE_INSENSITIVE), MelodyTerminal.class)

        ;

        public final Pattern regex;
        public final Class<?> terminalClass;

        Linker(Pattern regex, Class<?> terminalClass) {
            this.regex = regex;
            this.terminalClass = terminalClass;
        }

        public Terminal instance(Matcher matcher) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            return (Terminal) terminalClass.getConstructor(Matcher.class).newInstance(matcher);
        }
    }
    
    public static class ClickInfo {
        public final int slot;
        public final int type;
        public final int windowId;

        public ClickInfo(int slot, int type, int windowId) {
            this.slot = slot;
            this.type = type;
            this.windowId = windowId;
        }
    }

    public Terminal(Matcher matchResult) {}

    public abstract ClickInfo next(int id);
    public abstract void build(ItemStack[] stack);
}
