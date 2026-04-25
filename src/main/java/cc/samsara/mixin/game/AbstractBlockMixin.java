package cc.samsara.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import cc.samsara.Samsara;
import cc.samsara.event.events.impl.game.BlockShapeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {

    @ModifyReturnValue(method = "getCollisionShape", at = @At("RETURN"))
    private VoxelShape getCollisionShapeInject(VoxelShape original, BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (pos == null || Samsara.getInstance().getEventManager() == null) {
            return original;
        }

        final BlockShapeEvent blockShapeEvent = new BlockShapeEvent(state, pos, original);
        Samsara.getInstance().getEventManager().call(blockShapeEvent);
        return blockShapeEvent.getShape();
    }
}
