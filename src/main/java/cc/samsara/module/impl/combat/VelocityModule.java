package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.module.impl.combat.velocity.*;
import cc.samsara.property.properties.ClassModeProperty;

public class VelocityModule extends Module {
    // Club velocity code is in MixinEntityVelocityUpdateS2CPacket.java.
    public final ClassModeProperty mode = new ClassModeProperty("Mode",
            new AirVelocity(this), new CancelVelocity(this),
            new IntaveVelocity(this),
            new NormalVelocity(this), new ReverseVelocity(this),
            new VulcanVelocity(this)/*, new WatchdogAirVelocity(this),
            new WatchdogFullVelocity(this)*/, new MMCVelocity(this),
            new DelayVelocity(this), new BufferVelocity(this),
            new ModernWatchdogVelocity(this), new GrimVelocity(this),
            new JumpVelocity(this), new ReducePacketVelocity(this)
    );

    public VelocityModule() {
        super(Category.COMBAT);
        this.registerProperty(mode);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        this.setSuffix(mode.getProperty().getFormatedName());
    }
}
