package me.qigan.abse.fr.auto.routes.elems;

import me.qigan.abse.fr.auto.routes.ARoute;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ARENull extends ARElement{
    public ARENull() {
        super(new BlockPos(0, 0, 0));
    }

    @Override
    public void tick(TickEvent.ClientTickEvent e, ARoute caller) {

    }

    @Override
    public void reset(ARoute caller) {

    }
}
