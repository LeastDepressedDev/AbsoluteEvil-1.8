package me.qigan.abse.fr.dungons.terminals;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class CorrectAllTerminal extends Terminal{

    public List<Integer> solution;
    public int step = 0;

    public CorrectAllTerminal(Matcher matchResult) {
        super(matchResult);
    }

    @Override
    public ClickInfo next(int id) {
        if (step >= solution.size()) return null;
        return new ClickInfo(solution.get(step++), 0, id);
    }

    @Override
    public void build(ItemStack[] ignored) {
        ContainerChest c = TerminalUtils.getOpenedChestContainer();
        if (c==null) return;
        List<ItemStack> inv = c.getInventory();
        solution = new ArrayList<>();

        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 7; x++) {
                int n = TerminalUtils.cordToSlot(new int[]{x, y});
                ItemStack stack = inv.get(n);
                if (stack == null) continue;
                if (stack.getMetadata() == 14) solution.add(n);
            }
        }
    }
}
