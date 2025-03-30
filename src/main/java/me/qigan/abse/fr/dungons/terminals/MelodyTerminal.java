package me.qigan.abse.fr.dungons.terminals;

import net.minecraft.item.ItemStack;

import java.util.regex.Matcher;

public class MelodyTerminal extends Terminal{
    public MelodyTerminal(Matcher matchResult) {super(matchResult);}

    @Override
    public ClickInfo next(int id) {return null;}

    @Override
    public void build(ItemStack[] stack) {}
}
