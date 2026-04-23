package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.callables.EventDual;
import cc.samsara.event.types.EventModes;

public class TickEvent extends EventDual {
    public TickEvent(EventModes eventModes) {
        super(eventModes);
    }
}
