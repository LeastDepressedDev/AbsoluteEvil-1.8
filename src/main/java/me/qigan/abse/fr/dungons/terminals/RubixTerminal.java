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
        return null;
    }

    @Override
    public void build(ItemStack[] ignored) {
        ContainerChest c = TerminalUtils.getOpenedChestContainer();
        if (c==null) return;
        for (int i = 0; i < 5; i++) {
            List<AddressedData<int[], Integer>> part = new ArrayList<>();
            for (int y = 1; y <= 3; y++) {
                for (int x = 3; x <= 5; x++) {
                    //TODO: Finish
                }
            }
        }
    }
}
