package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.PostMotionEvent;
import cc.samsara.event.events.impl.game.SlowDownEvent;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.event.events.impl.input.SwordInputEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.events.impl.render.Render3DEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.module.impl.combat.killaura.ESPRenderer;
import cc.samsara.module.impl.combat.killaura.HeyPixelLogic;
import cc.samsara.module.impl.combat.killaura.HypixelLogic;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.ModeProperty;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.property.properties.body.BodyPart;
import cc.samsara.property.properties.body.BodyProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Unified KillAura Module — LiquidBounce-style component architecture.
 * 
 * === Architecture ===
 * ModuleKillAura (this)         ← config + event routing (lightweight)
 *   ├── HypixelLogic             ← Hypixel mode combat logic
 *   ├── HeyPixelLogic            ← HeyPixel mode combat logic
 *   └── ESPRenderer              ← Unified rendering
 * 
 * Mode "Hypixel"  = original Samsara logic (auto-block, watchdog bypasses)
 * Mode "HeyPixel" = adapted OpenZen logic (1.8/1.9 timing, multi-target, priority)
 * Both modes fully isolated via separate Logic classes with independent config Props.
 */
public class KillauraModule extends Module {

    // ─── MODE SELECTOR ───
    private final ModeProperty mode = new ModeProperty("Mode", "Hypixel", "HeyPixel", "Hypixel");

    // ─── HYPIXEL PROPERTIES ───
    private final ModeProperty auraMode = new ModeProperty("Aura Mode", "Single", "Single", "Switch").setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty raycast = new BooleanProperty("Ray Cast", false).setVisible(() -> mode.is("Hypixel"));
    private final NumberProperty switchDelay = new NumberProperty("Switch Delay", 200, 0, 5000, 1).setVisible(() -> mode.is("Hypixel") && auraMode.is("Switch"));
    private final NumberProperty scoutRange = new NumberProperty("Scout Range", 3, 0, 6, 0.1f).setVisible(() -> mode.is("Hypixel"));
    public final NumberProperty attackRange = new NumberProperty("Attack Range", 3, 0, 6, 0.1f).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty watchdogKeepSprint = new BooleanProperty("Watchdog Keep Sprint", true).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty disableKeepSprintOnKB = new BooleanProperty("Disable Keep Sprint on KB", true).setVisible(() -> mode.is("Hypixel") && watchdogKeepSprint.getProperty());
    public BooleanProperty autoBlock = new BooleanProperty("Auto Block", false).setVisible(() -> mode.is("Hypixel"));
    public final NumberProperty blockRange = new NumberProperty("Block Range", 3, 0, 6, 0.1f).setVisible(() -> mode.is("Hypixel") && autoBlock.getProperty());
    public ModeProperty autoBlockMode = new ModeProperty("Auto Block Mode", "Vanilla", "Vanilla", "None", "Blink", "Modern Watchdog", "Modern Watchdog Post", "Old Watchdog").setVisible(() -> mode.is("Hypixel") && autoBlock.getProperty());
    private final BooleanProperty blink = new BooleanProperty("Blink", false).setVisible(() -> mode.is("Hypixel") && autoBlockMode.is("Watchdog 2") && autoBlock.getProperty());
    private final BooleanProperty packetAutoBlock = new BooleanProperty("Packet Auto Block", false).setVisible(() -> mode.is("Hypixel") && autoBlock.getProperty());
    private final BooleanProperty interactWhenBlocking = new BooleanProperty("Interact When Blocking", false).setVisible(() -> mode.is("Hypixel") && autoBlock.getProperty());
    private final BooleanProperty forceBlockHitAnimation = new BooleanProperty("Force Block Hit Animation", false).setVisible(() -> mode.is("Hypixel") && autoBlock.getProperty());
    private final BooleanProperty rotation = new BooleanProperty("Rotations", true).setVisible(() -> mode.is("Hypixel"));
    private final ModeProperty legitRandomization = new ModeProperty("Noise Mode", "Off", "Off", "Snap", "MouseSim").setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final NumberProperty noiseYawMultiplier = new NumberProperty("Noise Yaw Multiplier", 0.08f, 0.01f, 0.5f, 0.01f).setVisible(() -> mode.is("Hypixel") && (legitRandomization.is("Snap") || legitRandomization.is("MouseSim")));
    private final NumberProperty noisePitchMultiplier = new NumberProperty("Noise Pitch Multiplier", 0.02f, 0.01f, 0.3f, 0.01f).setVisible(() -> mode.is("Hypixel") && (legitRandomization.is("Snap") || legitRandomization.is("MouseSim")));
    private final NumberProperty maxYawNoise = new NumberProperty("Max Yaw Noise", 1.2f, 0.1f, 5f, 0.1f).setVisible(() -> mode.is("Hypixel") && (legitRandomization.is("Snap") || legitRandomization.is("MouseSim")));
    private final NumberProperty maxPitchNoise = new NumberProperty("Max Pitch Noise", 0.75f, 0.1f, 5f, 0.1f).setVisible(() -> mode.is("Hypixel") && (legitRandomization.is("Snap") || legitRandomization.is("MouseSim")));
    private final BooleanProperty gcdFix = new BooleanProperty("GCD Fix", false).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final BodyProperty bodyPart = new BodyProperty("Rotation Target", BodyPart.HEAD).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    public NumberProperty rotationRange = new NumberProperty("Rotation Range", 3, 0, 6, 0.1f).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final NumberProperty minYawSpeed = new NumberProperty("Min Yaw Speed", 120, 1, 180, 1).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final NumberProperty maxYawSpeed = new NumberProperty("Max Yaw Speed", 120, 1, 180, 1).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final NumberProperty minPitchSpeed = new NumberProperty("Min Pitch Speed", 120, 1, 180, 1).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final NumberProperty maxPitchSpeed = new NumberProperty("Max Pitch Speed", 120, 1, 180, 1).setVisible(() -> mode.is("Hypixel") && rotation.getProperty());
    private final BooleanProperty latestCPS = new BooleanProperty("1.9+ CPS", false).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty onedotEight = new BooleanProperty("1.8 Swing Order", false).setVisible(() -> mode.is("Hypixel"));
    public NumberProperty minCPS = new NumberProperty("Min CPS", 10, 0, 20, 1).setVisible(() -> mode.is("Hypixel") && !latestCPS.getProperty());
    public NumberProperty maxCPS = new NumberProperty("Max CPS", 20, 0, 20, 1).setVisible(() -> mode.is("Hypixel") && !latestCPS.getProperty());
    private final ModeProperty critMode = new ModeProperty("Wait for Crit", "Off", "Off", "Normal", "Force").setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty criticalSprint = new BooleanProperty("Critical Sprint", false).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty throughWalls = new BooleanProperty("Through Walls", true).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty ignoreTeamMates = new BooleanProperty("Ignore Teammates", true).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty monsters = new BooleanProperty("Monsters", true).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty animals = new BooleanProperty("Animals", false).setVisible(() -> mode.is("Hypixel"));
    private final BooleanProperty invisible = new BooleanProperty("Invisible", false).setVisible(() -> mode.is("Hypixel"));

    // ─── HEYPIXEL PROPERTIES ───
    public final BooleanProperty hpAttackPlayer = new BooleanProperty("Attack Player", true).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpAttackInvisible = new BooleanProperty("Attack Invisible", false).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpAttackAnimals = new BooleanProperty("Attack Animals", false).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpAttackMobs = new BooleanProperty("Attack Mobs", true).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpMultiAttack = new BooleanProperty("Multi Attack", true).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpInfSwitch = new BooleanProperty("Infinite Switch", false).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpPreferBaby = new BooleanProperty("Prefer Baby", false).setVisible(() -> mode.is("HeyPixel"));
    public final BooleanProperty hpKeepSprint = new BooleanProperty("Keep Sprint", true).setVisible(() -> mode.is("HeyPixel"));
    public final NumberProperty hpAimRange = new NumberProperty("Aim Range", 4.0f, 1.0f, 6.0f, 0.1f).setVisible(() -> mode.is("HeyPixel"));
    public final NumberProperty hpMaxAPS = new NumberProperty("Max APS", 12.0f, 1.0f, 20.0f, 1.0f).setVisible(() -> mode.is("HeyPixel"));
    public final NumberProperty hpMinAPS = new NumberProperty("Min APS", 9.0f, 1.0f, 20.0f, 1.0f).setVisible(() -> mode.is("HeyPixel"));
    public final NumberProperty hpSwitchSize = new NumberProperty("Switch Size", 1.0f, 1.0f, 5.0f, 1.0f).setVisible(() -> mode.is("HeyPixel") && !hpInfSwitch.getProperty());
    public final NumberProperty hpSwitchDelay = new NumberProperty("Switch Delay (Ticks)", 1.0f, 1.0f, 10.0f, 1.0f).setVisible(() -> mode.is("HeyPixel"));
    public final NumberProperty hpFoV = new NumberProperty("Field of View", 360.0f, 10.0f, 360.0f, 1.0f).setVisible(() -> mode.is("HeyPixel"));
    public final NumberProperty hpHurtTime = new NumberProperty("Hurt Time", 10.0f, 0.0f, 10.0f, 1.0f).setVisible(() -> mode.is("HeyPixel"));
    public final ModeProperty hpDelayMode = new ModeProperty("Delay Mode", "1.8", "1.9", "1.8").setVisible(() -> mode.is("HeyPixel"));
    public final ModeProperty hpPriorityMode = new ModeProperty("Priority", "Distance", "FoV", "Health").setVisible(() -> mode.is("HeyPixel"));

    // ─── SHARED PROPERTIES ───
    private final BooleanProperty esp = new BooleanProperty("ESP", false);
    private final NumberProperty espWidth = new NumberProperty("ESP Width", 0.8f, 0, 10, 0.1f).setVisible(esp::getProperty);
    public final ModeProperty espStyle = new ModeProperty("ESP Style", "Auto", "Auto", "Circle", "Box").setVisible(esp::getProperty);

    // ─── COMPONENTS ───
    private final HypixelLogic hypixelLogic;
    private final HypixelLogic.Props hypixelProps;
    private final HeyPixelLogic heypixelLogic;
    private final HeyPixelLogic.Props heypixelProps;
    private final ESPRenderer espRenderer;

    // ─── Static target reference (used by AntiFireBall, Velocity NoXZ, etc.) ───
    public static LivingEntity target;

    public KillauraModule() {
        super(Category.COMBAT);

        // Build property accessor proxies before registering properties
        this.hypixelProps = buildHypixelProps();
        this.heypixelProps = buildHeyPixelProps();
        this.hypixelLogic = new HypixelLogic(hypixelProps);
        this.heypixelLogic = new HeyPixelLogic(heypixelProps);
        this.espRenderer = new ESPRenderer(buildEspModeAccessor());

        registerProperties(
            mode,
            auraMode, raycast, switchDelay, scoutRange, attackRange,
            watchdogKeepSprint, disableKeepSprintOnKB,
            autoBlock, blockRange, autoBlockMode, blink,
            packetAutoBlock, interactWhenBlocking, forceBlockHitAnimation,
            rotation, legitRandomization,
            noiseYawMultiplier, noisePitchMultiplier, maxYawNoise, maxPitchNoise,
            gcdFix, bodyPart, rotationRange,
            minYawSpeed, maxYawSpeed, minPitchSpeed, maxPitchSpeed,
            latestCPS, onedotEight, critMode, criticalSprint,
            minCPS, maxCPS, throughWalls, ignoreTeamMates, monsters, animals, invisible,
            hpAttackPlayer, hpAttackInvisible, hpAttackAnimals, hpAttackMobs,
            hpMultiAttack, hpInfSwitch, hpPreferBaby, hpKeepSprint,
            hpAimRange, hpMaxAPS, hpMinAPS, hpSwitchSize, hpSwitchDelay,
            hpFoV, hpHurtTime, hpDelayMode, hpPriorityMode,
            esp, espWidth, espStyle
        );
    }

    // ─── Property proxy builders ───
    private HypixelLogic.Props buildHypixelProps() {
        return new HypixelLogic.Props() {
            public String getAuraMode() { return auraMode.getProperty(); }
            public boolean getRaycast() { return raycast.getProperty(); }
            public int getSwitchDelay() { return switchDelay.getProperty().intValue(); }
            public float getScoutRange() { return scoutRange.getProperty().floatValue(); }
            public float getAttackRange() { return attackRange.getProperty().floatValue(); }
            public boolean getWatchdogKeepSprint() { return watchdogKeepSprint.getProperty(); }
            public boolean getDisableKeepSprintOnKB() { return disableKeepSprintOnKB.getProperty(); }
            public boolean getAutoBlock() { return autoBlock.getProperty(); }
            public float getBlockRange() { return blockRange.getProperty().floatValue(); }
            public String getAutoBlockMode() { return autoBlockMode.getProperty(); }
            public boolean getBlink() { return blink.getProperty(); }
            public boolean getPacketAutoBlock() { return packetAutoBlock.getProperty(); }
            public boolean getInteractWhenBlocking() { return interactWhenBlocking.getProperty(); }
            public boolean getForceBlockHitAnimation() { return forceBlockHitAnimation.getProperty(); }
            public boolean getRotation() { return rotation.getProperty(); }
            public String getLegitRandomization() { return legitRandomization.getProperty(); }
            public float getNoiseYawMultiplier() { return noiseYawMultiplier.getProperty().floatValue(); }
            public float getNoisePitchMultiplier() { return noisePitchMultiplier.getProperty().floatValue(); }
            public float getMaxYawNoise() { return maxYawNoise.getProperty().floatValue(); }
            public float getMaxPitchNoise() { return maxPitchNoise.getProperty().floatValue(); }
            public boolean getGcdFix() { return gcdFix.getProperty(); }
            public float getRotationRange() { return rotationRange.getProperty().floatValue(); }
            public float getMinYawSpeed() { return minYawSpeed.getProperty().floatValue(); }
            public float getMaxYawSpeed() { return maxYawSpeed.getProperty().floatValue(); }
            public float getMinPitchSpeed() { return minPitchSpeed.getProperty().floatValue(); }
            public float getMaxPitchSpeed() { return maxPitchSpeed.getProperty().floatValue(); }
            public boolean getLatestCPS() { return latestCPS.getProperty(); }
            public boolean getOneDotEight() { return onedotEight.getProperty(); }
            public int getMinCPS() { return minCPS.getProperty().intValue(); }
            public int getMaxCPS() { return maxCPS.getProperty().intValue(); }
            public String getCritMode() { return critMode.getProperty(); }
            public boolean getCriticalSprint() { return criticalSprint.getProperty(); }
            public boolean getThroughWalls() { return throughWalls.getProperty(); }
            public boolean getIgnoreTeamMates() { return ignoreTeamMates.getProperty(); }
            public boolean getMonsters() { return monsters.getProperty(); }
            public boolean getAnimals() { return animals.getProperty(); }
            public boolean getInvisible() { return invisible.getProperty(); }
            public Module getModule() { return KillauraModule.this; }
        };
    }

    private HeyPixelLogic.Props buildHeyPixelProps() {
        return new HeyPixelLogic.Props() {
            public boolean getHpIgnoreSkipTicks() { return false; }
            public boolean getHpAttackPlayer() { return hpAttackPlayer.getProperty(); }
            public boolean getHpAttackInvisible() { return hpAttackInvisible.getProperty(); }
            public boolean getHpAttackAnimals() { return hpAttackAnimals.getProperty(); }
            public boolean getHpAttackMobs() { return hpAttackMobs.getProperty(); }
            public boolean getHpMultiAttack() { return hpMultiAttack.getProperty(); }
            public boolean getHpInfSwitch() { return hpInfSwitch.getProperty(); }
            public boolean getHpPreferBaby() { return hpPreferBaby.getProperty(); }
            public boolean getHpKeepSprint() { return hpKeepSprint.getProperty(); }
            public float getHpAimRange() { return hpAimRange.getProperty().floatValue(); }
            public float getHpMaxAPS() { return hpMaxAPS.getProperty().floatValue(); }
            public float getHpMinAPS() { return hpMinAPS.getProperty().floatValue(); }
            public int getHpSwitchSize() { return hpSwitchSize.getProperty().intValue(); }
            public int getHpSwitchDelay() { return hpSwitchDelay.getProperty().intValue(); }
            public float getHpFoV() { return hpFoV.getProperty().floatValue(); }
            public int getHpHurtTime() { return hpHurtTime.getProperty().intValue(); }
            public String getHpDelayMode() { return hpDelayMode.getProperty(); }
            public String getHpPriorityMode() { return hpPriorityMode.getProperty(); }
            public boolean getThroughWalls() { return throughWalls.getProperty(); }
            public boolean getIgnoreTeamMates() { return ignoreTeamMates.getProperty(); }
        };
    }

    private ESPRenderer.ModeAccessor buildEspModeAccessor() {
        return new ESPRenderer.ModeAccessor() {
            public String getEspStyle() { return espStyle.getProperty(); }
            public boolean isEspEnabled() { return esp.getProperty(); }
            public boolean isHypixel() { return mode.is("Hypixel"); }
            public Entity getTarget() { return target; }
            public Entity getHpTarget() { return heypixelLogic.hpTarget; }
        };
    }

    // ─── ENABLE / DISABLE ───
    @Override
    public void onEnable() {
        hypixelLogic.onEnable();
        heypixelLogic.onEnable();
        target = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        hypixelLogic.onDisable();
        heypixelLogic.onDisable();
        target = null;
        super.onDisable();
    }

    // ─── TICK ───
    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) return;

        if (mode.is("HeyPixel")) {
            heypixelLogic.onTick();
        } else {
            hypixelLogic.onTick();
            // Sync static target reference
            target = hypixelLogic.target;
        }
    }

    // ─── PACKET ───
    @EventTarget
    public void onPacket2(PacketEvent event) {
        if (mode.is("Hypixel")) {
            hypixelLogic.handlePacket(event);
        } else {
            heypixelLogic.handlePacket(event);
        }
    }

    // ─── RENDER ───
    @EventTarget
    public void onRender3D(Render3DEvent event) {
        espRenderer.onRender3D(event);
    }

    // ─── INPUT ───
    @EventTarget
    public void onSwordInputEvent(SwordInputEvent event) {
        if (mode.is("Hypixel")) {
            event.setBlocking(hypixelLogic.getSwordInputBlocking());
        }
    }

    // Placeholder for fake animation rendering - may need to be reimplemented
    public boolean shouldRenderFakeAnim() {
        return false;
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        if (mode.is("Hypixel") && hypixelLogic.getSlowDownCancelled()) {
            event.setCancelled(true);
            event.setSlowDown(1.0f);
        }
    }

    @EventTarget
    public void onPostMotion(PostMotionEvent event) {}
    @EventTarget
    public void onUpdate(UpdateEvent event) {}
}
