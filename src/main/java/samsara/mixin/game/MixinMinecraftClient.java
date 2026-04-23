package samsara.mixin.game;

import cc.samsara.Samsara;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.game.WorldChangeEvent;
import cc.samsara.event.events.impl.input.ClickEvent;
import cc.samsara.event.events.impl.input.SwordInputEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.interfaces.IAccess;
import cc.samsara.module.impl.combat.AntiBotModule;
import cc.samsara.module.impl.combat.KillauraModule;
import cc.samsara.module.impl.combat.NoMissDelayModule;
import cc.samsara.module.impl.player.MultiActionModule;
import cc.samsara.module.impl.visual.GlowEspModule;
import cc.samsara.ui.imgui.ImGuiImpl;
import cc.samsara.util.Data;
import cc.samsara.util.player.PlayerUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraftClient implements IAccess {

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    private int rightClickDelay;

    @Inject(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void hookLegitClickEvent(CallbackInfo ci) {
        LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player == null) return;

        ClickEvent event = new ClickEvent();
        Samsara.getInstance().getEventManager().call(event);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(final GameConfig args, final CallbackInfo callback) {
        final int[] width = {1};
        final int[] height = {1};

        GLFW.glfwGetFramebufferSize(mc.getWindow().handle(), width, height);
        Samsara.getInstance().getSkija().init(width[0] > 0 ? width[0] : 1, height[0] > 0 ? height[0] : 1);
    }

    @Shadow
    @Final
    private Window window;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initImGui(GameConfig args, CallbackInfo ci) {
        ImGuiImpl.create(window.handle());
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void closeImGui(CallbackInfo ci) {
        ImGuiImpl.dispose();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V"))
    private void startClient(CallbackInfo callback) {
        Samsara.getInstance().init();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void hookEventTickPre(CallbackInfo ci) {
        if (mc.player != null) {
            Data.offGroundTicks = mc.player.onGround() ? 0 : ++Data.offGroundTicks;
            Data.onGroundTicks = !mc.player.onGround() ? 0 : ++Data.onGroundTicks;
            if (mc.player.onGround())
                Data.TIME_SINCE_GROUND.reset();
        }

        Samsara.getInstance().getEventManager().call(new TickEvent(EventModes.PRE));
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V",
            shift = At.Shift.AFTER))
    private void hookGuiRender(boolean b, CallbackInfo ci) {
        //     SkijaUtil.scoped(() -> Samsara.getInstance().getEventManager().call(new Render2DEvent()));
        // ImGuiImpl.render();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void hookEventTickPost(CallbackInfo ci) {
        Samsara.getInstance().getEventManager().call(new TickEvent(EventModes.POST));
    }

    @Inject(method = "updateLevelInEngines", at = @At("HEAD"))
    private void hookWorldChangeEvent(ClientLevel world, CallbackInfo callbackInfo) {
        Samsara.getInstance().getEventManager().call(new WorldChangeEvent());
    }

    @Redirect(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;isDown()Z",
                    ordinal = 2
            )
    )
    private boolean hookKillAuraAutoBlock(KeyMapping keyBinding) {
        KillauraModule killAura = Samsara.getInstance().getModuleManager().getModule(KillauraModule.class);
        SwordInputEvent swordInputEvent = new SwordInputEvent();
     /*   ChatUtil.print( (killAura.isToggled() &&
                killAura.target != null
                && !killAura.autoBlockMode.is("None") && !killAura.autoBlockMode.is("Fake") && killAura.autoBlock.getProperty()));*/
        Samsara.getInstance().getEventManager().call(swordInputEvent);

        return keyBinding.isDown() || swordInputEvent.isBlocking();
    }

    @ModifyExpressionValue(method = "startAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;missTime:I", ordinal = 0))
    private int injectNoMissCooldown(int original) {
        NoMissDelayModule noMissDelayModule = Samsara.getInstance().getModuleManager().getModule(NoMissDelayModule.class);
        if (noMissDelayModule.isToggled() && noMissDelayModule.attackcooldown.getProperty()) {
            return 0;
        }
        return original;
    }

    @WrapWithCondition(method = "startAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;missTime:I", ordinal = 1))
    private boolean noClickCooldown(Minecraft instance, int value) {
        NoMissDelayModule noMissDelayModule = Samsara.getInstance().getModuleManager().getModule(NoMissDelayModule.class);
        return !(noMissDelayModule.isToggled() && noMissDelayModule.attackcooldown.getProperty());
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void injectswingCancel(CallbackInfoReturnable<Boolean> cir) {
        NoMissDelayModule module = Samsara.getInstance().getModuleManager().getModule(NoMissDelayModule.class);
        if (module.isToggled() || player == null || hitResult == null) {

            if (module.weaponOnly.getProperty() && !PlayerUtil.isHoldingWeapon()) return;
            HitResult.Type type = hitResult.getType();

            if (module.attackonMiss.getProperty()) {
                if ((type == HitResult.Type.MISS && !module.allowAirHit.getProperty()) ||
                        (type == HitResult.Type.BLOCK && !module.allowBlockHit.getProperty())) {
                    cir.setReturnValue(true); // cancel the swing so you dont miss
                }
            }
        }
    }

    // auth
    @Unique
    private boolean serenity$shown = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (serenity$shown || mc.level != null) return;

        serenity$shown = true;
        Screen authScreen = Samsara.getInstance().getAuthScreen();
        mc.setScreen(authScreen);
    }

    @Redirect(method = "shouldEntityAppearGlowing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isCurrentlyGlowing()Z"))
    public boolean hookGlow(Entity entity) {
        boolean glowing = entity.isCurrentlyGlowing();
        GlowEspModule glowEspModule = Samsara.getInstance().getModuleManager().getModule(GlowEspModule.class);
        if (glowEspModule.isToggled() && entity instanceof Player && !AntiBotModule.isBot((Player) entity)) {
            return true;
        }
        return glowing;
    }

    @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean breakWhileUsing(boolean original) {
        return original && !Samsara.getInstance().getModuleManager().getModule(MultiActionModule.class).breakIfUsing.getProperty();
    }

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    private boolean placeWhileBreaking(boolean original) {
        return original && !Samsara.getInstance().getModuleManager().getModule(MultiActionModule.class).placeIfBreaking.getProperty();
    }


/*    @Redirect(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z",
                    ordinal = 2
            )
    )
    private boolean hookAutoBlock(KeyBinding keyBinding) {
        if (Samsara.getInstance().getModuleManager() == null) return keyBinding.isPressed();

        KillauraModule killauraModule = Samsara.getInstance().getModuleManager().getModule(KillauraModule.class);

        return keyBinding.isPressed() || (killauraModule.isToggled() && !killauraModule.autoBlockMode.is("None") && !killauraModule.autoBlockMode.is("Fake") && killauraModule.target != null);
    }*/
}
