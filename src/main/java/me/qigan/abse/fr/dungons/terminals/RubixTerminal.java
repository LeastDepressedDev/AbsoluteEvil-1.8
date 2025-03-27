package me.qigan.abse.fr.dungons.terminals;

import me.qigan.abse.config.AddressedData;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import scala.swing.Table;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;

public class RubixTerminal extends Terminal{

    public static int[] ORDER = {14, 1, 4, 13, 11};
    public static Dictionary<Integer, Integer> MAPPER = new Hashtable<>();
    public List<int[]> solution = null;
    public int step = 0;

    public RubixTerminal(Matcher matchResult) {
        super(matchResult);
        MAPPER.put(14, 0);
        MAPPER.put(1, 1);
        MAPPER.put(4, 2);
        MAPPER.put(13, 3);
        MAPPER.put(11, 4);
    }

    @Override
    public ClickInfo next(int id) {
        if (step >= solution.size()) return null;
        int[] p = solution.get(step++);
        return new ClickInfo(p[0], p[1], id);
    }

    @Override
    public void build(ItemStack[] ignored) {
        ContainerChest c = TerminalUtils.getOpenedChestContainer();
        if (c==null) return;
        List<ItemStack> inv = c.getInventory();
        int ext = 256;
        for (int i = 0; i < 5; i++) {
            List<int[]> part = new ArrayList<>();
            for (int y = 1; y <= 3; y++) {
                for (int x = 3; x <= 5; x++) {
                    int sn = TerminalUtils.cordToSlot(new int[]{x, y});
                    ItemStack stack = inv.get(sn);
                    if (stack == null) continue;
                    Integer current = MAPPER.get(stack.getMetadata());
                    if (current == null) continue;
                    int p = (i - current + ORDER.length) % ORDER.length;
                    int n = (current - i + ORDER.length) % ORDER.length;

                    if (p < n) {
                        for (int _i = 0; _i < p; _i++) {
                            part.add(new int[]{sn, 0});
                        }
                    } else {
                        for (int _i = 0; _i < n; _i++) {
                            part.add(new int[]{sn, 1});
                        }
                    }
                }
            }
            if (part.size() < ext) {
                solution = part;
                ext = solution.size();
            }
        }
    }
}
