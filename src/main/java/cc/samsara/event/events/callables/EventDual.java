package cc.samsara.event.events.callables;

import cc.samsara.event.types.EventModes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class EventDual extends EventCancellable {
    public EventModes eventMode;
}
