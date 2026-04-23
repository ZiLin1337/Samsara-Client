package cc.samsara.event.events.callables;

import cc.samsara.event.events.Event;
import cc.samsara.event.types.EventModes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventMode implements Event {
    public EventModes eventType;
}
