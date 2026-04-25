package cc.samsara.module.impl.visual;

import cc.samsara.mixin.accessor.mc.IdentifierAccessor;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.EntityInteractEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class HitSoundModule extends Module {

    private final ModeProperty mode = new ModeProperty("Hit Sound", "Skeet",
            "Skeet", "Never Lose", "Dark loves them");

    public HitSoundModule() {
        super(Category.VISUAL);
        this.registerProperty(mode);
    }

    @EventTarget
    public void onInteractEvent(EntityInteractEvent event) {
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(getSoundIdentifier());
        if (soundEvent == null) {
            System.out.println("Sound Event is null.");
            return;
        }

        SoundInstance soundInstance = SimpleSoundInstance.forUI(soundEvent/*, SoundCategory.PLAYERS*/, 1.0f, 1.0f);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public ResourceLocation getSoundIdentifier() {
        return switch (mode.getProperty()) {
            case "Skeet" -> IdentifierAccessor.createIdentifier("samsara", "effects.skeet");
            case "Never Lose" -> IdentifierAccessor.createIdentifier("samsara", "effects.neverlose");
            case "Dark loves them" -> IdentifierAccessor.createIdentifier("samsara", "effects.dark");
            default ->
                throw new IllegalStateException("Unexpected value: " + mode.getProperty());
        };
    }
}
