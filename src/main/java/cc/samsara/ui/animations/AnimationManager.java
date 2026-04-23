package cc.samsara.ui.animations;

import cc.samsara.Samsara;
import cc.samsara.module.Module;

import java.util.HashMap;

/**
 * @author plusbox (thx pluh)
 * @since 3/23/2024
 */
public final class AnimationManager {
    private final HashMap<Module, Animation> moduleAnimationMap = new HashMap<>();

    public void start() {
        for (final Module module : Samsara.getInstance().getModuleManager().getModules()) {
            moduleAnimationMap.put(module, new Animation(Easing.EASE_IN_OUT_SINE, 250));
        }
    }

    public Animation getAnimation(final Module module) {
        return moduleAnimationMap.get(module);
    }
}

