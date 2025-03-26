package me.qigan.abse.fr.dungons.terminals;

import me.qigan.abse.config.AddressedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumbersTerminal extends Terminal {

    public int step = 0;
    public AddressedData<Integer, Integer>[] solution;

    public NumbersTerminal(Matcher matchResult) {
        super(matchResult);
    }

    @Override
    public ClickInfo next(int id) {
        if (step >= solution.length) return null;
        ClickInfo clickInfo = new ClickInfo(solution[step].getObject(), 0, id);;
        step++;
        return clickInfo;
    }

    @Override
    public void build(ItemStack[] ignored) {
        ContainerChest c = TerminalUtils.getOpenedChestContainer();
        if (c==null) return;
        List<ItemStack> inv = c.getInventory();
        List<AddressedData<Integer, Integer>> slots = new ArrayList<>();

        for (int y = 1; y <= 2; y++) {
            for (int x = 1; x <= 7; x++) {
                int n = TerminalUtils.cordToSlot(new int[]{x, y});
                ItemStack stack = inv.get(n);
                if (stack == null) continue;
                if (stack.getMetadata() == 14) slots.add(new AddressedData<>(stack.stackSize, n));
            }
        }

        solution = slots.stream().sorted((a, b) -> a.getNamespace()-b.getNamespace()).toArray(AddressedData[]::new);
    }
}
