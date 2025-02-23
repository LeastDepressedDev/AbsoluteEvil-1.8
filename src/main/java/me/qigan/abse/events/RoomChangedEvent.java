package me.qigan.abse.events;

import me.qigan.abse.mapping.Room;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RoomChangedEvent extends Event {
    public final Room previous;
    public final Room current;

    public RoomChangedEvent(Room previous, Room current) {
        this.previous = previous;
        this.current = current;
    }
}
