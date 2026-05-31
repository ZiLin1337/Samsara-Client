package cc.samsara.mixin.render;


import cc.samsara.Samsara;
import cc.samsara.module.impl.visual.MoreChatHistoryModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public class MixinChatHud {

    @ModifyExpressionValue(method = {"addMessageToDisplayQueue", "addMessage", "addRecentChat"},
            at = @At(value = "CONSTANT", args = "intValue=100")
    )
    public int changeMaxHistory(int original) {
        MoreChatHistoryModule moreChatHistoryModule = Samsara.getInstance().getModuleManager().getModule(MoreChatHistoryModule.class);
        if (moreChatHistoryModule.isToggled()) {
            return original + 16284;
        }
        return original;
    }
}
