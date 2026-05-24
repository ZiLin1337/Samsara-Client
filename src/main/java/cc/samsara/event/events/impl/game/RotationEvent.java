package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RotationEvent extends Event {
    private float yaw;
    private float pitch;
}
