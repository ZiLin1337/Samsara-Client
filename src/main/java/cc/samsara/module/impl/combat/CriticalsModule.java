package cc.samsara.module.impl.combat;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.event.events.impl.game.MoveEvent;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.ModeProperty;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.util.player.PlayerUtil;
import net.minecraft.world.entity.LivingEntity;

public class CriticalsModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Watchdog 2", "Watchdog 2", "Legit", "Vanilla");
    private final NumberProperty criticalTicks = new NumberProperty("Critical On Ticks", 3, 1, 10, 1);

    KillauraModule killauraModule = Samsara.getInstance().getModuleManager().getModule(KillauraModule.class);
    private double lastMotionY = 0;
    private int tickCounter = 0;

    public CriticalsModule() {
        super(Category.COMBAT);
        registerProperties(mode, criticalTicks.setVisible(() -> mode.is("Watchdog 2")));
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        this.setSuffix(mode.getProperty());
        spawnCriticalParticles();
        switch (mode.getProperty()) {
            case "Watchdog 2" -> {
                if (killauraModule.isToggled() && killauraModule.target != null) {
                    int maxOnTicks = criticalTicks.getProperty().intValue();
                    tickCounter = (tickCounter + 1) % (maxOnTicks + 1);

                    if (tickCounter < maxOnTicks) {
                        if (PlayerUtil.getMotionY() == lastMotionY) {
                            event.setOnGround(false);
                        }

                        lastMotionY = PlayerUtil.getMotionY();
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMotion(InputTickEvent event) {
        if (mode.is("Legit")) {
            if (killauraModule.isToggled() && killauraModule.target != null) {
                if (mc.player.onGround()) {
                    event.jump = true;
                }
            }
        }
    }

    private void spawnCriticalParticles() {
        LivingEntity target = Samsara.getInstance().getModuleManager().getModule(KillauraModule.class).target;
        if (target != null && mc.player.swinging)
            mc.player.crit(target);
    }
}
