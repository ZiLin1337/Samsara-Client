package cc.samsara.mixin.game;

import cc.samsara.Samsara;
import cc.samsara.event.events.impl.game.WorldChangeEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "loadLevel", at = @At(value = "HEAD"))
    protected void onWorldLoad(CallbackInfo info) {
       // Samsara.getInstance().getEventManager().call(new WorldChangeEvent());
    }
}