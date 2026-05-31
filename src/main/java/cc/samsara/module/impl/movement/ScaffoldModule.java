package cc.samsara.module.impl.movement;

import cc.samsara.Samsara;
import cc.samsara.component.impl.player.RotationComponent;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.event.events.impl.render.Render2DEvent;
import cc.samsara.event.events.impl.render.Render3DEvent;
import cc.samsara.event.events.impl.render.ShaderEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.font.FontManager;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.module.impl.movement.scaffold.sprints.IntaveSprint;
import cc.samsara.module.impl.movement.scaffold.sprints.ModernWatchdogSprint;
import cc.samsara.module.impl.movement.scaffold.sprints.VanillaSprint;
import cc.samsara.module.impl.movement.scaffold.towers.VanillaTower;
import cc.samsara.module.impl.movement.scaffold.towers.WatchdogVanillaTower;
import cc.samsara.module.impl.visual.HudModule;
import cc.samsara.property.properties.*;
import cc.samsara.skija.utils.SkijaUtil;
import cc.samsara.util.math.RandomUtil;
import cc.samsara.util.math.TimeUtil;
import cc.samsara.util.network.PacketUtil;
import cc.samsara.util.player.MoveUtil;
import cc.samsara.util.player.RayTraceUtil;
import cc.samsara.util.player.RotationUtil;
import cc.samsara.util.player.scaffold.BlockCache;
import cc.samsara.util.player.scaffold.ScaffoldUtil;
import cc.samsara.util.player.scaffold.ScaffoldUtil.BlockSlot;
import cc.samsara.util.render.ChatUtil;
import cc.samsara.util.render.ColorUtil;
import cc.samsara.util.render.Render3DUtil;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Arrays;

import static cc.samsara.util.player.RotationUtil.wrapTo90;
import static cc.samsara.util.player.scaffold.ScaffoldUtil.getBlockStateRelativeToPlayer;

public class ScaffoldModule extends Module {

    public enum RotationMode {
        BASIC, EDGE, MODERN_WATCHDOG, GOD_BRIDGE, INTAVE, SNAP, BRUTE_FORCE, OFF
    }

    private final ModeProperty scaffoldMode = new ModeProperty("Scaffold Mode", "Normal", "Normal", "Modern Watchdog", "Telly", "Snap"),
            placeMode = new ModeProperty("Place Mode", "Update", "Update", "Click");
    private final NumberProperty offGroundTicksPlace = new NumberProperty("Start Placing After", 5, 0, 9, 1);
    private final BooleanProperty swing = new BooleanProperty("Swing Hand", true);
    private final BooleanProperty alternatedScaffoldOrder = new BooleanProperty("Alternated Scaffold Order", false);
    public final BooleanProperty safeWalk = new BooleanProperty("SafeWalk", true);
    private final BooleanProperty sameY = new BooleanProperty("Same Y", false),
            keepY = new BooleanProperty("Keep Y", false),
            analBoob = new BooleanProperty("Anal Boob", false);
    private final BooleanProperty dontPlaceInSameTick = new BooleanProperty("Don't place in same tick", true);
    private final BooleanProperty jump = new BooleanProperty("Jump", false);
    private final BooleanProperty dontJumpOnDiag = new BooleanProperty("Don't jump diagonally", false);
    private final NumberProperty jumpEvery = new NumberProperty("Jump Every", 1, 1, 30, 1);
    private final NumberProperty placeDelay = new NumberProperty("Place Delay", 0, 0, 10, 1);
    private final BooleanProperty sprintOnJump = new BooleanProperty("Sprint On Jump", false);
    public final BooleanProperty sprint = new BooleanProperty("Sprint", false);
    private final BooleanProperty changeFov = new BooleanProperty("Change FOV", false);
    private final ClassModeProperty sprintMode = new ClassModeProperty(
            "Sprint Mode",
            new VanillaSprint(this),
            new ModernWatchdogSprint(this),
            new IntaveSprint(this)
    );
    public final BooleanProperty tower = new BooleanProperty("Tower", false);
    private final ClassModeProperty towerMode = new ClassModeProperty("Tower Mode",
            new WatchdogVanillaTower(this),
            new VanillaTower(this)
    );
    private final BooleanProperty noOffhandCheck = new BooleanProperty("No Off Hand check", true);
    private final BooleanProperty blockCounter = new BooleanProperty("Block Counter", true);
    private final ModeProperty blockCounterMode = new ModeProperty("Block Counter Mode", "Samsara", "Samsara", "Adjust");
    private final BooleanProperty blockESP = new BooleanProperty("Block ESP", true);
    private final ModeProperty blockESPMode = new ModeProperty("Block ESP Mode", "Accurate", "Accurate", "Prediction");
    private final NumberProperty blockESPAlpha = new NumberProperty("Block ESP Alpha", 50, 1, 255, 1);
    private final BooleanProperty sneak = new BooleanProperty("Sneak", false);
    private final NumberProperty sneakEvery = new NumberProperty("Sneak Every", 1, 1, 30, 1);
    private final BooleanProperty funnySlotSwitch = new BooleanProperty("Autoswap to next stack", true);
    private final BooleanProperty rotation = new BooleanProperty("Rotations", true);
    private final ModeProperty rotationMode = new ModeProperty("Rotation Mode", "Basic", "Basic", "Edge", "Modern Watchdog", "God Bridge", "Intave", "Snap", "Brute Force", "Off");
    private final ModeProperty rotationSpeedMode = new ModeProperty("Rotation Speed Mode", "Manual", "Manual", "Dynamic");

    private final NumberProperty
            minYawSpeed = new NumberProperty("Min Yaw Speed", 120, 1, 180, 1),
            maxYawSpeed = new NumberProperty("Max Yaw Speed", 120, 1, 180, 1),
            minPitchSpeed = new NumberProperty("Min Pitch Speed", 120, 1, 180, 1),
            maxPitchSpeed = new NumberProperty("Max Pitch Speed", 120, 1, 180, 1);

    private final ModeProperty dynamicRotationSpeed = new ModeProperty("Dynamic Speed Curve", "Fast", "Fast", "Normal", "Slow");

    private final BooleanProperty oneEightyOnJumpKey = new BooleanProperty("180 on jump key", false);
    private final ModeProperty rayCastMode = new ModeProperty("Ray Cast Mode", "Off", "Off", "Normal", "Advanced", "MC", "Samsara");

    private final BooleanProperty bruteForceYaw = new BooleanProperty("Brute Force Yaw", true),
            bruteForcePitch = new BooleanProperty("Brute Force Pitch", true);
    private final BooleanProperty spoofRayCast = new BooleanProperty("Spoof Ray Cast", false),
            stable = new BooleanProperty("Stable", false);
    private final NumberProperty yawValue = new NumberProperty("Yaw Value", 180, 0, 360, 1),
            pitchValue = new NumberProperty("Pitch Value", 81.7f, -90, 90, 0.1f);

    private final BooleanProperty spin = new BooleanProperty("Spin", false);
    private final NumberProperty spinSpeed = new NumberProperty("Spin Speed", 30f, 0, 100, 1);

    private BlockCache blockCache;
    public int blocksPlaced, ticksOnAir, startSlot;
    public double startY;
    private float[] rotations;
    public boolean firstJump;
    private boolean didBecomeNotNull;
    public int spoofedSlot = -1;
    private int lastPlaceTick;
    private int jumpPlaced;
    private boolean towering;
    private float currentSpinYaw = 0.0f;

    public ScaffoldModule() {
        super(Category.MOVEMENT);
        registerProperties(scaffoldMode, alternatedScaffoldOrder, offGroundTicksPlace.setVisible(() -> scaffoldMode.is("Telly")),
                placeMode, placeDelay, swing, sameY, keepY, changeFov,
                dontPlaceInSameTick,
                analBoob.setVisible(() -> sameY.getProperty() || keepY.getProperty()), jump, dontJumpOnDiag.setVisible(jump::getProperty),
                jumpEvery.setVisible(jump::getProperty), sprint, sprintOnJump,
                sprintMode.setVisible(sprint::getProperty), tower,
                towerMode.setVisible(tower::getProperty),
                noOffhandCheck, blockCounter, blockCounterMode.setVisible(blockCounter::getProperty),
                blockESP, blockESPMode.setVisible(blockESP::getProperty), blockESPAlpha.setVisible(blockESP::getProperty),
                sneak, sneakEvery.setVisible(sneak::getProperty), rayCastMode,
                funnySlotSwitch, rotation, oneEightyOnJumpKey.setVisible(rotation::getProperty),
                rotationMode.setVisible(rotation::getProperty),
                rotationSpeedMode.setVisible(rotation::getProperty),
                minYawSpeed.setVisible(() -> rotation.getProperty() && rotationSpeedMode.is("Manual")),
                maxYawSpeed.setVisible(() -> rotation.getProperty() && rotationSpeedMode.is("Manual")),
                minPitchSpeed.setVisible(() -> rotation.getProperty() && rotationSpeedMode.is("Manual")),
                maxPitchSpeed.setVisible(() -> rotation.getProperty() && rotationSpeedMode.is("Manual")),
                dynamicRotationSpeed.setVisible(() -> rotation.getProperty() && rotationSpeedMode.is("Dynamic")),
                bruteForceYaw.setVisible(() -> rotation.getProperty() && !rotationMode.is("Intave")),
                bruteForcePitch.setVisible(() -> rotation.getProperty() && !rotationMode.is("Intave")),
                spoofRayCast.setVisible(() -> rotation.getProperty() && rotationMode.is("Modern Watchdog")),
                stable.setVisible(() -> rotation.getProperty() && rotationMode.is("Modern Watchdog")),
                yawValue.setVisible(rotation::getProperty),
                pitchValue.setVisible(rotation::getProperty),
                spin, spinSpeed.setVisible(spin::getProperty),
                safeWalk
        );
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            startSlot = mc.player.getInventory().getSelectedSlot();
            startY = mc.player.getY();
            blocksPlaced = 0;
            didBecomeNotNull = false;
            firstJump = true;
            if (!sprint.getProperty()) {
                mc.options.toggleSprint().set(false);
                mc.player.setSprinting(false);
                mc.options.keySprint.setDown(false);
            }
        }

        if (rotation.getProperty() && changeFov.getProperty()) {
            mc.options.fovEffectScale().set(0.0);
        }

        this.jumpPlaced = 0;
        spoofedSlot = startSlot;
        lastPlaceTick = -1;
        currentSpinYaw = 0.0f;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player != null)
            mc.player.getInventory().setSelectedSlot(startSlot);

        if (analBoob.getProperty()) {
            mc.options.keyUse.setDown(false);
        }

        if (rotation.getProperty() && changeFov.getProperty()) {
            mc.options.fovEffectScale().set(1.0);
        }
        spoofedSlot = -1;
        RotationComponent.setSpoofRotations(false);
        
        float yawSpeed = RandomUtil.getAdvancedRandom(minYawSpeed.getProperty().floatValue(), maxYawSpeed.getProperty().floatValue());
        float pitchSpeed = RandomUtil.getAdvancedRandom(minPitchSpeed.getProperty().floatValue(), maxPitchSpeed.getProperty().floatValue());
        
        RotationComponent.setRotations(rotations, yawSpeed, pitchSpeed);

        super.onDisable();
    }

    @EventTarget
    public void onTickInput(InputTickEvent event) {
        if (!sprint.getProperty())
            mc.player.setSprinting(false);

        mc.options.keySprint.setDown(false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) {
            if (!alternatedScaffoldOrder.getProperty())
                rotations = calculateRotations();
        } else {
            if (placeMode.is("Click"))
                place();
        }

        if (alternatedScaffoldOrder.getProperty()) {
            // calculate block data
            blockCache = ScaffoldUtil.getBlockData((int) (((sameY.getProperty() || keepY.getProperty() || Samsara.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) && !mc.options.keyJump.isDown()) || firstJump ? startY : mc.player.getY()));
            rotations = calculateRotations();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.options.keyAttack.setDown(false);
        mc.options.keyUse.setDown(false);

        boolean canTower = tower.getProperty() && mc.options.keyJump.isDown();
        boolean canSprint = (sprint.getProperty() && MoveUtil.isMoving());

        if (sprintOnJump.getProperty() && mc.options.keyJump.isDown() && sprint.getProperty()) {
            mc.player.setSprinting(MoveUtil.isMoving());
        }

        if (!sprintOnJump.getProperty() && !canTower) {
            mc.options.keySprint.setDown(canSprint);
            mc.player.setSprinting(canSprint);
        }

        rotate();

        if (placeMode.is("Update")) {
            place();
        }
    }

    private final TimeUtil wasPressedTime = new TimeUtil();

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.options.keyJump.isDown()) {
            wasPressedTime.reset();
        }

        if (!wasPressedTime.finished(200))
            startY = mc.player.getY();

        if (firstJump && offGroundTicks == 8) {
            firstJump = false;
        }

        sneak();
        tower();
    }

    @EventTarget
    public void onInputTickEvent(InputTickEvent event) {
        jump(event);
    }

    private BlockPos lastBlockPos;

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!blockESP.getProperty())
            return;

        if (blockCache != null) {
            lastBlockPos = blockCache.blockPos;
        }

        if (lastBlockPos == null) return;

        Render3DUtil.drawBoxESP(
                event.getMatricies(),
                new AABB(blockESPMode.is("Accurate") ? lastBlockPos :
                        (MoveUtil.isMoving() ? lastBlockPos.relative(mc.player.getMotionDirection()) : lastBlockPos.above())),
                Samsara.getInstance().getFirstColor(), blockESPAlpha.getProperty().intValue()
        );
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!blockCounter.getProperty())
            return;

        Window window = mc.getWindow();

        float centerX = (float) window.getGuiScaledWidth() / 2;
        float centerY = (float) window.getGuiScaledHeight() / 2 + 5;

        String placedStr = String.valueOf(ScaffoldUtil.getTotalBlocksInInventory());

        switch (blockCounterMode.getProperty()) {
            case "Adjust" -> {
                float textWidth = tahoma_regular_8.getStringWidth("Blocks"),
                        placedWidth = tahoma_bold_8.getStringWidth(placedStr);

                float drawX = centerX - (placedWidth + 2 + textWidth) / 2.0f;

                tahoma_bold_8.drawStringWithShadow(
                        placedStr,
                        drawX,
                        centerY,
                        Samsara.getInstance().getFirstColor()
                );

                tahoma_regular_8.drawStringWithShadow(
                        "Blocks",
                        drawX + placedWidth + 2,
                        centerY,
                        Color.white
                );
            }

            case "Samsara" -> {
                float textWidth = FontManager.getFont("tenacity", 8).getStringWidth("Blocks"),
                        placedWidth = FontManager.getFont("tenacity-bold", 8).getStringWidth(placedStr);
                float totalWidth = placedWidth + 2.0f + textWidth;

                float drawX = centerX - totalWidth / 2.0f;
                float textY = centerY + 2.5f;

                SkijaUtil.roundedRectangle(
                        drawX - 3f,
                        centerY,
                        totalWidth + 3f * 2,
                        15,
                        3,
                        new Color(15, 15, 18, Samsara.getInstance().getModuleManager().getModule(HudModule.class).backgroundAlpha.getProperty().intValue())
                );

                FontManager.getFont("tenacity-bold", 8).drawStringWithShadow(
                        placedStr,
                        drawX,
                        textY,
                        Samsara.getInstance().getFirstColor()
                );

                FontManager.getFont("tenacity", 8).drawStringWithShadow("Blocks",
                        drawX + placedWidth + 2.0f,
                        textY,
                        Color.white
                );
            }
        }
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        if (!blockCounter.getProperty())
            return;

        Window window = mc.getWindow();

        if (blockCounterMode.is("Samsara")) {
            final float textWidth = FontManager.getFont("tenacity", 8).getStringWidth("Blocks"),
                    placedWidth = FontManager.getFont("tenacity-bold", 8).getStringWidth(String.valueOf(ScaffoldUtil.getTotalBlocksInInventory()));

            final float totalWidth = placedWidth + 2.0f + textWidth;

            SkijaUtil.drawShaderRoundRectangle(
                    (((float) window.getGuiScaledWidth() / 2) - totalWidth / 2.0f) - 3f,
                    (float) window.getGuiScaledHeight() / 2 + 5,
                    totalWidth + 3f * 2,
                    15,
                    3
            );
        }
    }

    private void place() {
        BlockSlot[] slots = ScaffoldUtil.getBlockSlots(noOffhandCheck.getProperty());
        if (slots.length == 0) {
            return;
        }

        Arrays.sort(slots, (a, b) -> {
            int countA = ScaffoldUtil.getStackCount(a);
            int countB = ScaffoldUtil.getStackCount(b);
            return Integer.compare(countB, countA);
        });

        int selectedIndex = funnySlotSwitch.getProperty()
                ? (int) RandomUtil.getAdvancedRandom(0, slots.length)
                : 0;
        BlockSlot selectedSlot = slots[selectedIndex];

        if (selectedSlot.hand() == InteractionHand.MAIN_HAND) {
            mc.player.getInventory().setSelectedSlot(selectedSlot.slot());
        }

        ticksOnAir = getBlockStateRelativeToPlayer(0, -1, 0).isAir() ? ++ticksOnAir : 0;

        if (!alternatedScaffoldOrder.getProperty())
            blockCache = ScaffoldUtil.getBlockData((int) (((sameY.getProperty() || keepY.getProperty() || Samsara.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) && !mc.options.keyJump.isDown()) || firstJump ? startY : mc.player.getY()));

        if (blockCache != null && ticksOnAir > placeDelay.getProperty().intValue() &&
                (lastPlaceTick != mc.player.tickCount || !dontPlaceInSameTick.getProperty())
        ) {
            if (isRayCastSafe()) {

                mc.gameMode.useItemOn(mc.player, selectedSlot.hand(),
                        new BlockHitResult(ScaffoldUtil.getVec(blockCache), blockCache.direction, blockCache.blockPos, false));

                if (swing.getProperty()) {
                    mc.player.swing(selectedSlot.hand());
                } else {
                    PacketUtil.send(new ServerboundSwingPacket(selectedSlot.hand()));
                }

                if (tower.getProperty() && towerMode.is("Watchdog Vertical") && mc.options.keyJump.isDown()) {
                    mc.gameMode.useItemOn(mc.player, selectedSlot.hand(),
                            new BlockHitResult(ScaffoldUtil.getVec(blockCache), blockCache.direction,
                                    new BlockPos(blockCache.blockPos.getX() - 1, blockCache.blockPos.getY(),
                                            blockCache.blockPos.getZ()), false));
                }

                blocksPlaced++;
                jumpPlaced++;
                ticksOnAir = 0;
                lastPlaceTick = mc.player.tickCount;
            }
        }
    }

    private void rotate() {
        if (!rotation.getProperty()) {
            RotationComponent.setSpoofRotations(false);
            return;
        }

        float yawSpeed, pitchSpeed;

        if (rotationSpeedMode.is("Dynamic")) {
            float calculateSpeedFast = mc.player.onGround() ? 180 : offGroundTicks < 2 ? 130 : 40;

            float calculateSpeedNormal = switch (offGroundTicks) {
                case 0 -> 180;
                case 1 -> 130;
                case 3 -> 60;
                case 6, 7, 8 -> 80;
                default -> 40;
            };

            float calculateSpeedSlow = switch (offGroundTicks) {
                case 0 -> 180;
                default -> 40;
            };

            yawSpeed = switch (dynamicRotationSpeed.getProperty()) {
                case "Fast" -> calculateSpeedFast;
                case "Normal" -> calculateSpeedNormal;
                case "Slow" -> calculateSpeedSlow;
                default -> 40;
            };
            pitchSpeed = yawSpeed; // Use same curve for pitch
        } else {
            yawSpeed = RandomUtil.getAdvancedRandom(minYawSpeed.getProperty().floatValue(), maxYawSpeed.getProperty().floatValue());
            pitchSpeed = RandomUtil.getAdvancedRandom(minPitchSpeed.getProperty().floatValue(), maxPitchSpeed.getProperty().floatValue());
        }

        if (rotationMode.is("Modern Watchdog")) {
            yawSpeed = (offGroundTicks < (MoveUtil.isGoingDiagonally() ? 1 : 2) ? 180 : MoveUtil.isGoingDiagonally() ? 60 : 0);
        }

        RotationComponent.setRotations(rotations, yawSpeed, pitchSpeed);

        if (spin.getProperty()) {
            currentSpinYaw += spinSpeed.getProperty().floatValue();
            if (currentSpinYaw >= 360f) {
                currentSpinYaw -= 360f;
            }

            RotationComponent.setSpoofRotations(true);
            RotationComponent.setFakeYaw(currentSpinYaw);
            RotationComponent.setFakePitch(RotationComponent.getPitch());
        }
    }

    private float watchdogYawFixRandom;
    private float watchcockYaw;
    private float[] lastBruteForceRots = null;

    private BlockCache lastBlockCache;

    private float[] calculateRotations() {
        float y = mc.player.getYRot() - yawValue.getProperty().floatValue() +
                (mc.options.keyRight.isDown() ? 45 : mc.options.keyLeft.isDown() ? -45 :
                        mc.options.keyDown.isDown() ? 180 : 0);

        if (blockCache != null)
            lastBlockCache = blockCache;

        float baseYaw;
        float basePitch = pitchValue.getProperty().floatValue();

        if (!MoveUtil.isMoving() && rotationMode.is("Modern Watchdog"))
            return new float[]{y, basePitch};

        if (rotationMode.is("Modern Watchdog")) {
            if (lastBlockCache == null) return new float[]{y, basePitch};
            boolean leftSide;
            Vec3 position = new Vec3(lastBlockCache.blockPos.getX() + 0.5, lastBlockCache.blockPos.getY(), lastBlockCache.blockPos.getZ() + 0.5); // center pos

            double yawRad = MoveUtil.getDirection();
            double dirX = -Math.sin(yawRad);
            double dirZ = Math.cos(yawRad);

            double toEntryX = position.x - mc.player.getX();
            double toEntryZ = position.z - mc.player.getZ();

            leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;
            float straightValue = 260;
            baseYaw = mc.player.getYRot() + (MoveUtil.isGoingDiagonally() ? -135 : (!leftSide ? straightValue : -straightValue));
        } else {
            baseYaw = mc.player.getYRot() - yawValue.getProperty().floatValue() +
                    (mc.options.keyRight.isDown() ? 45 : mc.options.keyLeft.isDown() ? -45 :
                            mc.options.keyDown.isDown() ? 180 : 0);
        }

        float forwardYaw = mc.player.getYRot() + 45;

        if (canPlace()) {
            boolean found = false;
            float finalBruteForceYaw = baseYaw;
            float finalBruteForcePitch = basePitch;

            if (rotationMode.is("Brute Force") || ((bruteForceYaw.getProperty() || bruteForcePitch.getProperty()) && !rotationMode.is("Intave"))) {
                if (blockCache != null) {
                    if (bruteForcePitch.getProperty() || rotationMode.is("Brute Force")) {
                        for (float possiblePitch = 90; possiblePitch > 30 && !found; possiblePitch -=
                                (possiblePitch > (mc.player.hasEffect(net.minecraft.world.effect.MobEffects.SPEED) ? 60 : 80) ? 0.5f : 2f)) {
                            if (RayTraceUtil.getOver(blockCache.direction, blockCache.blockPos, true, 5, baseYaw, possiblePitch)) {
                                finalBruteForceYaw = baseYaw;
                                finalBruteForcePitch = possiblePitch;
                                found = true;
                                lastBruteForceRots = new float[]{finalBruteForceYaw, finalBruteForcePitch};
                            }
                        }
                    }

                    if ((bruteForceYaw.getProperty() || rotationMode.is("Brute Force")) && !found) {
                        for (float yawOffset = 0; yawOffset <= 180 && !found; yawOffset += 5) {
                            for (float direction = -1; direction <= 1 && !found; direction += 2) {
                                float possibleYaw = baseYaw + (yawOffset * direction);
                                for (float possiblePitch = 85; possiblePitch > 30 && !found; possiblePitch -= 5) {
                                    if (RayTraceUtil.getOver(blockCache.direction, blockCache.blockPos, true, 5, possibleYaw, possiblePitch)) {
                                        finalBruteForceYaw = possibleYaw;
                                        finalBruteForcePitch = possiblePitch;
                                        found = true;
                                        lastBruteForceRots = new float[]{finalBruteForceYaw, finalBruteForcePitch};
                                    }
                                }
                            }
                        }
                    }
                }
            }

            baseYaw = (rotationMode.is("Brute Force") || bruteForceYaw.getProperty()) ? finalBruteForceYaw : baseYaw;
            basePitch = (rotationMode.is("Brute Force") || bruteForcePitch.getProperty()) ? finalBruteForcePitch : basePitch;

            switch (rotationMode.getProperty()) {
                case "Basic" -> {
                    return new float[]{baseYaw, basePitch};
                }

                case "Modern Watchdog" -> {
                    if (blockCache != null) {
                        float[] rots = getDirectionToBlock(blockCache.blockPos.getX(), blockCache.blockPos.getY(), blockCache.blockPos.getZ(),
                                blockCache.direction);
                        basePitch = rots[1];
                    }

                    return new float[]{baseYaw, basePitch};
                }

                case "Edge" -> {
                    float dirYaw = MoveUtil.getRawDirectionNoKeys();
                    if (oneEightyOnJumpKey.getProperty() && !MoveUtil.isMoving()) {
                        return new float[]{mc.player.getYRot() - 180, basePitch};
                    }

                    if (MoveUtil.isPressingForwardAndStrafe())
                        return new float[]{baseYaw, basePitch};

                    if (!didBecomeNotNull) {
                        if (blockCache != null)
                            didBecomeNotNull = true;

                        return new float[]{MoveUtil.isGoingDiagonally() ? dirYaw - 140 + watchdogYawFixRandom : dirYaw + 100 + watchdogYawFixRandom, basePitch};
                    } else {
                        if (mc.player.tickCount % 3 == 0) {
                            watchdogYawFixRandom = RandomUtil.getAdvancedRandom(0f, 9f);
                        }

                        boolean leftSide;
                        Vec3 position = new Vec3(blockCache.blockPos.getX() + 0.5, blockCache.blockPos.getY(), blockCache.blockPos.getZ() + 0.5); // center pos

                        double yawRad = Math.toRadians(dirYaw);
                        double dirX = -Math.sin(yawRad);
                        double dirZ = Math.cos(yawRad);

                        double toEntryX = position.x - mc.player.getX();
                        double toEntryZ = position.z - mc.player.getZ();

                        leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;

                        if (MoveUtil.isGoingDiagonally()) {
                            watchcockYaw = dirYaw - 140;
                        } else {
                            watchcockYaw = dirYaw + (leftSide ? 100 + watchdogYawFixRandom : -100 - watchdogYawFixRandom);
                        }

                        return new float[]{watchcockYaw, 82};
                    }
                }

                case "God Bridge" -> {
                    Options options = mc.options;
                    boolean up = options.keyUp.isDown(), down = options.keyDown.isDown(), left = options.keyLeft.isDown(), right = options.keyRight.isDown();
                    float yRot = Mth.wrapDegrees(mc.player.getYRot() - 180 + (up == down ? left == right ? 0 : left ? -90 : 90 : up ? left == right ? 0 : left ? -45 : 45 : left == right ? 180 : left ? -135 : 135));
                    if (yRot >= -22.5F && yRot < 22.5F) {
                        yRot = 45;
                    }
                    if (yRot < -22.5F && yRot >= -67.5F) yRot = -45;
                    if (yRot < -67.5F && yRot >= -112.5F) {
                        yRot = -45;
                    }
                    if (yRot < -112.5F && yRot >= -157.5F) yRot = -135;
                    if (yRot < -157.5F && yRot >= -180 || yRot >= 157.5F && yRot < 180) {
                        yRot = -135;
                    }
                    if (yRot >= 112.5F && yRot < 157.5F) yRot = 135;
                    if (yRot >= 67.5F && yRot < 112.5F) {
                        yRot = 135;
                    }
                    if (yRot >= 22.5F && yRot < 67.5F) yRot = 45;

                    return new float[]{yRot, 75.7F};
                }

                case "Intave" -> {
                    if (mc.options.keyJump.isDown())
                        return getDirectionToBlock(blockCache.blockPos.getX(), blockCache.blockPos.getY(), blockCache.blockPos.getZ(),
                                blockCache.direction);

                    if (ScaffoldUtil.isAirBlock(mc.player.blockPosition())) {
                        if (blockCache != null) {
                            return getDirectionToBlock(blockCache.blockPos.getX(), blockCache.blockPos.getY(), blockCache.blockPos.getZ(),
                                    blockCache.direction);
                        }
                    } else {
                        return new float[]{forwardYaw, basePitch};
                    }
                }

                case "Snap" -> {
                    if (mc.options.keyJump.isDown())
                        return RotationUtil.getDirectionToBlock(blockCache.blockPos, blockCache.direction);

                    if (ScaffoldUtil.isAirBlock(mc.player.blockPosition())) {
                        if (blockCache != null) {
                            return RotationUtil.getDirectionToBlock(blockCache.blockPos, blockCache.direction);
                        }
                    } else {
                        return new float[]{mc.player.getYRot(), basePitch};
                    }
                }
                case "Brute Force" -> {
                    return new float[]{baseYaw, basePitch};
                }
                case "Off" -> {
                    return new float[]{mc.player.getYRot(), mc.player.getXRot()};
                }
            }
        } else {
            return new float[]{mc.player.getYRot(), basePitch};
        }

        return new float[]{forwardYaw, basePitch};
    }

    public static float[] getDirectionToBlock(double x, double y, double z, Direction direction) {
        ClientLevel world = mc.level;
        if (world == null) return new float[]{0, 0};

        Entity marker = new Entity(EntityType.ITEM, world) {
            @Override
            protected void defineSynchedData(SynchedEntityData.Builder builder) {
            }

            @Override
            public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
                return false;
            }

            @Override
            protected void readAdditionalSaveData(ValueInput view) {
            }

            @Override
            protected void addAdditionalSaveData(ValueOutput view) {
            }
        };

        marker.setPos(
                x + 0.5D + direction.getStepX() * 0.5D,
                y + 0.5D + direction.getStepY() * 0.5D,
                z + 0.5D + direction.getStepZ() * 0.5D
        );

        return getRotationFromPosition(marker.getX(), marker.getY(), marker.getZ());
    }

    public static float[] getRotationFromPosition(double x, double y, double z) {
        LocalPlayer player = mc.player;
        if (player == null) return new float[]{0, 0};

        double xDiff = x - player.getX();
        double zDiff = z - player.getZ();
        double yDiff = y - player.getY() - 1.2D;
        double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0F / (float) Math.PI - 90.0F);
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0F / (float) Math.PI);

        return new float[]{yaw, pitch};
    }

    private void sneak() {
        if (!sneak.getProperty()) {
            return;
        }

        mc.options.keyShift.setDown(blocksPlaced % sneakEvery.getProperty().floatValue() == 0);
    }

    private boolean didJump;

    private void jump(InputTickEvent event) {
        if (!jump.getProperty()
                || !mc.player.onGround()
                || !MoveUtil.isMoving()
                || mc.options.keyJump.isDown()
                || Samsara.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) {
            didJump = false;
            return;
        }

        final boolean isGodBridgeMode = rotationMode.getValue() == RotationMode.GOD_BRIDGE && rotation.getProperty();
        final boolean shouldSkipDiagonal = MoveUtil.isGoingDiagonally() && dontJumpOnDiag.getProperty();

        boolean shouldJump;

        if (isGodBridgeMode) {
            shouldJump = ticksOnAir > 0 && jumpPlaced > jumpEvery.getProperty().intValue();
        } else {
            shouldJump = blocksPlaced != 0 && blocksPlaced % jumpEvery.getProperty().intValue() == 0 || jumpEvery.getProperty().intValue() == 1;
        }

        if (shouldJump && !shouldSkipDiagonal && !didJump) {
            event.jump = true;
            didJump = true;

            if (isGodBridgeMode) {
                jumpPlaced = -1;
            }
        } else {
            event.jump = false;
        }

        if (mc.player.onGround() && !mc.options.keyJump.isDown()) {
            didJump = false;
        }
    }

    private void tower() {
        if (!tower.getProperty() || !mc.options.keyJump.isDown()) {
            if (towering) {
                MoveUtil.stop();
            }
            towering = false;
            return;
        }

        mc.options.keyUse.setDown(false);
        towering = true;
    }

    private boolean canPlace() {
        return switch (scaffoldMode.getProperty()) {
            case "Telly" -> offGroundTicks > offGroundTicksPlace.getProperty().intValue();
            case "Modern Watchdog" -> !mc.player.onGround();
            default -> true;
        };
    }

    private boolean isRayCastSafe() {
        boolean spoof = rotationMode.getValue() == RotationMode.MODERN_WATCHDOG && spoofRayCast.getProperty();

        float yaw = spoof ?
                mc.player.getYRot() - 180 : RotationComponent.getYaw(), pitch = RotationComponent.getPitch();

        if (blockCache != null && MoveUtil.isGoingDiagonally() && spoof) {
            float[] rots = getDirectionToBlock(blockCache.blockPos.getX(), blockCache.blockPos.getY(), blockCache.blockPos.getZ(),
                    blockCache.direction);
            yaw = rots[0];
        }

        return switch (rayCastMode.getProperty()) {
            case "Advanced" -> RayTraceUtil.getOver(blockCache.direction, blockCache.blockPos, true, 5, yaw, pitch);
            case "Normal" -> RayTraceUtil.getOver(blockCache.direction, blockCache.blockPos, false, 5, yaw, pitch);
            case "MC" -> isMCRayCastSafe();
            case "Samsara" -> RayTraceUtil.isLookingAtBlock(blockCache.direction, blockCache.blockPos, true, 4.5f, yaw, pitch);
            default -> true;
        };
    }

    private boolean isMCRayCastSafe() {
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK
                && mc.hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(blockCache.blockPos)
                    && blockHitResult.getDirection().equals(blockCache.direction);
        }
        return false;
    }
}
