package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.fr.auto.routes.ARoute;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class ARElement {

    public BlockPos pos;

    public ARElement(BlockPos pos) {
        this.pos = pos;
    }

    public boolean next() {return true;}

    public abstract void tick(TickEvent.ClientTickEvent e, ARoute caller);
    public abstract void reset(ARoute caller);
}
