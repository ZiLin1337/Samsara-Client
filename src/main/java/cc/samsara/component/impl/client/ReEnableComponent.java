package cc.samsara.component.impl.client;

import cc.samsara.Samsara;
import cc.samsara.component.Component;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.events.impl.render.Render2DEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.impl.movement.SpeedModule;
import cc.samsara.util.math.TimeUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class ReEnableComponent extends Component {
    private final TimeUtil timeUtil = new TimeUtil();
    private boolean didDisable;

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.SEND || !Samsara.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled())
            return;

        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            Samsara.getInstance().getModuleManager().getModule(SpeedModule.class).toggle();
            didDisable = true;
            timeUtil.reset();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (didDisable && timeUtil.finished(1000))
            didDisable = false;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
       /* if (!didDisable)
            return;

        // just a notice having that timer run continuously might not be the best thing.
        String text = "Disabled Due To Flag";
        Window window = mc.getWindow();

       *//* RenderUtil.drawGradientString(roboto_bold_11, text,
                (window.getScaledWidth() - product_regular_10.getStringWidth(text)) / 2,
                (window.getScaledHeight() / 2) + 5,
                Samsara.getInstance().getFirstColor(),
                Samsara.getInstance().getSecondColor(), true);*//*
        roboto_bold_11.drawCenteredStringWithShadow(text,
                (float) (window.getScaledWidth()) / 2,
                ((float) window.getScaledHeight() / 2) + 5,
                Samsara.getInstance().getFirstColor());*/
    }
}
