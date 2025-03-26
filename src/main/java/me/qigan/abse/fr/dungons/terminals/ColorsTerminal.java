package me.qigan.abse.fr.dungons.terminals;

import me.qigan.abse.Index;
import me.qigan.abse.config.AddressedData;
import me.qigan.abse.sync.Utils;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class ColorsTerminal extends Terminal{

    public final String key;
    public int step = 0;
    public int[] solution;

    public ColorsTerminal(Matcher matchResult) {
        super(matchResult);
        key = matchResult.group(1).toLowerCase();
    }

    @Override
    public ClickInfo next(int id) {
        if (step >= solution.length) return null;
        return new ClickInfo(solution[step++], 0, id);
    }

    @Override
    public void build(ItemStack[] ignored) {
        ContainerChest c = TerminalUtils.getOpenedChestContainer();
        if (c==null) return;
        List<ItemStack> inv = c.getInventory();
        List<int[]> slots = new ArrayList<>();

        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 7; x++) {
                ItemStack stack = inv.get(TerminalUtils.cordToSlot(new int[]{x, y}));
                if (stack == null || stack.isItemEnchanted()) continue;
                String name = TerminalUtils.replaceColorName(Utils.cleanSB(stack.getDisplayName()).toLowerCase());
                if (name.startsWith(key)) slots.add(new int[]{x, y});
            }
        }

        solution = new int[slots.size()];
        int i = 0;
        if (Index.MAIN_CFG.getBoolVal("terms_clust")) {
            for (AddressedData<int[], List<int[]>> group : TerminalUtils.clusterize(slots, 3.2)) {
                for (int[] dot : group.getObject()) {
                    solution[i++] = TerminalUtils.cordToSlot(dot);
                }
            }
        } else {
            for (int[] dot : slots) {
                solution[i++] = TerminalUtils.cordToSlot(dot);
            }
        }
    }
}
