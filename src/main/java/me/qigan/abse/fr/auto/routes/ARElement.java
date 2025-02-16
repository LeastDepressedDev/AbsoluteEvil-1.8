package me.qigan.abse.fr.auto.routes;

import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class ARElement {

    public BlockPos pos;

    public ARElement(BlockPos pos) {
        this.pos = pos;
    }

    public boolean next() {return true;}

    public abstract void tick(TickEvent.ClientTickEvent e);
}
