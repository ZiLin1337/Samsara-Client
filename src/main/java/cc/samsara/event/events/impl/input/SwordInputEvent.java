package cc.samsara.event.events.impl.input;

import cc.samsara.event.events.Event;
import cc.samsara.event.events.callables.EventCancellable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Kawase
 * @since 12.10.2025
 */
@Getter
@Setter
public class SwordInputEvent implements Event {
    private boolean isBlocking = false;
}
