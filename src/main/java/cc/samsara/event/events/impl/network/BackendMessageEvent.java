package cc.samsara.event.events.impl.network;

import cc.samsara.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BackendMessageEvent implements Event {
    private final String message;
}
