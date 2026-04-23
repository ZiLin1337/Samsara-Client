package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MotionEvent extends EventCancellable {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
}
