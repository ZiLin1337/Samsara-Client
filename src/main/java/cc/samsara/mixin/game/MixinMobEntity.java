package cc.samsara.mixin.game;

import cc.samsara.Samsara;
import cc.samsara.module.impl.movement.EntityControlModule;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MixinMobEntity extends LivingEntity{

    protected MixinMobEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    private void HookEnityControleSaddel(CallbackInfoReturnable<Boolean> cir) {
        if (Samsara.getInstance().getModuleManager().getModule(EntityControlModule.class).isToggled()){
        cir.setReturnValue(true);
        }
    }
}
