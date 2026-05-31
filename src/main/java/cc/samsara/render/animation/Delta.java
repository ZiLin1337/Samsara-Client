package cc.samsara.render.animation;

import cc.samsara.interfaces.IAccess;

/**
 * Frame delta time tracker.
 * Uses Minecraft's DeltaTracker instead of GLFW for Fabric compatibility.
 */
public class Delta implements IAccess {
    private static double deltaTime;

    /**
     * Called every frame to update the delta time.
     */
    public static void update() {
        deltaTime = mc.getDeltaTracker().getRealtimeDeltaTicks() * 50.0;
    }

    public static double getDeltaTime() {
        return deltaTime;
    }
}
