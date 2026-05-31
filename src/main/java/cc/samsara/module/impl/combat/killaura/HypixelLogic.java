package cc.samsara.module.impl.combat.killaura;

import cc.samsara.Samsara;
import cc.samsara.component.impl.network.BlinkComponent;
import cc.samsara.interfaces.IAccess;
import cc.samsara.mixin.accessor.mc.KeyBindingAccessor;
import cc.samsara.module.Module;
import cc.samsara.module.impl.movement.ScaffoldModule;
import cc.samsara.property.properties.*;
import cc.samsara.util.math.TimeUtil;
import cc.samsara.util.network.PacketUtil;
import cc.samsara.util.player.PlayerUtil;
import cc.samsara.util.player.RayTraceUtil;
import cc.samsara.util.player.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hypixel mode combat logic for KillauraModule.
 * Handles target selection, rotation, auto-block, and attacking.
 */
public class HypixelLogic implements IAccess {

    // References to parent module properties (set via constructor)
    public static LivingEntity target;
    public List<LivingEntity> validTargets = new ArrayList<>();

    final TimeUtil cpsTime = new TimeUtil(), switchTime = new TimeUtil();
    public float[] rotations;
    public boolean renderBlockHit, isBlocking;
    boolean isBlinking = false;
    int autoBlockTicks = 0, killAuraTicks = 0;
    int targetIndex = 0;
    private final KillauraRotations rotationHandler = new KillauraRotations();


    // Property accessors (set by parent module)
    public interface Props {
        String getAuraMode();
        boolean getRaycast();
        int getSwitchDelay();
        float getScoutRange();
        float getAttackRange();
        boolean getWatchdogKeepSprint();
        boolean getDisableKeepSprintOnKB();
        boolean getAutoBlock();
        float getBlockRange();
        String getAutoBlockMode();
        boolean getBlink();
        boolean getPacketAutoBlock();
        boolean getInteractWhenBlocking();
        boolean getForceBlockHitAnimation();
        boolean getRotation();
        String getLegitRandomization();
        float getNoiseYawMultiplier();
        float getNoisePitchMultiplier();
        float getMaxYawNoise();
        float getMaxPitchNoise();
        boolean getGcdFix();
        float getRotationRange();
        float getMinYawSpeed();
        float getMaxYawSpeed();
        float getMinPitchSpeed();
        float getMaxPitchSpeed();
        boolean getLatestCPS();
        boolean getOneDotEight();
        int getMinCPS();
        int getMaxCPS();
        String getCritMode();
        boolean getCriticalSprint();
        boolean getThroughWalls();
        boolean getIgnoreTeamMates();
        boolean getMonsters();
        boolean getAnimals();
        boolean getInvisible();
        Module getModule();
    }

    private final Props props;

    public HypixelLogic(Props props) {
        this.props = props;
    }

    public void onEnable() {
        this.isBlocking = false;
        this.target = null;
        this.validTargets.clear();
        this.rotations = null;
        this.targetIndex = 0;
        this.killAuraTicks = 0;
        this.autoBlockTicks = 0;
    }

    public void onDisable() {
        target = null;
        setBlockState(false);
        this.killAuraTicks = 0;
        this.autoBlockTicks = 0;
    }

    public void onTick() {
        if (Samsara.getInstance().getModuleManager().getModule(ScaffoldModule.class).isToggled() ||
                (Samsara.getInstance().getModuleManager().getModule(cc.samsara.module.impl.combat.AntiFireBallModule.class).isToggled() &&
                        cc.samsara.module.impl.combat.AntiFireBallModule.target != null)) {
            return;
        }

        killAuraTicks++;

        this.validTargets = PlayerUtil.getTargets(
                props.getAnimals(), props.getMonsters(),
                props.getIgnoreTeamMates(), props.getInvisible(),
                props.getThroughWalls(), props.getScoutRange());

        List<LivingEntity> distanceSorted = validTargets.stream()
                .sorted(Comparator.comparingDouble(RotationUtil::getDistanceToEntityBox))
                .toList();

        if (props.getAuraMode().equals("Switch")) {
            if (validTargets.size() == 1) {
                this.validTargets = distanceSorted;
            } else {
                this.validTargets = validTargets.stream()
                        .filter(e -> RotationUtil.getDistanceToEntityBox(e) <= props.getAttackRange())
                        .sorted(Comparator.comparingDouble(RotationUtil::getDistanceToEntityBox))
                        .collect(Collectors.toList());
            }
        } else {
            this.validTargets = distanceSorted;
        }

        if (validTargets.isEmpty()) {
            target = null;
            targetIndex = 0;
            setBlockState(false);
            return;
        }

        if (props.getAuraMode().equals("Single")) {
            target = validTargets.getFirst();
            targetIndex = 0;
        } else {
            if (targetIndex >= validTargets.size()) targetIndex = 0;
            if (switchTime.finished(props.getSwitchDelay())) {
                targetIndex = (targetIndex + 1) % validTargets.size();
                switchTime.reset();
            }
            target = validTargets.get(targetIndex);
        }

        if (target == null) {
            setBlockState(false);
            return;
        }

        handleCombat();
    }

    private void handleCombat() {
        float dist = (float) RotationUtil.getDistanceToEntityBox(target);

        if (dist <= props.getRotationRange() && props.getRotation()) {
            rotations = rotationHandler.calculateAndApply(rotationPropsAdapter(props), target, dist);
        }

        boolean canAutoBlock = dist <= props.getBlockRange() && props.getAutoBlock()
                && mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS);

        RayTraceUtil.updateCrosshairTarget(props.getThroughWalls());
        BlockHitResult hitResult;
        boolean rayCast = (hitResult = RotationUtil.calculateIntercept(target.getBoundingBox(),
                mc.player.getEyePosition(1.0f), RotationUtil.getCurrentHitVec(props.getAttackRange()))) != null
                && hitResult.getType() != HitResult.Type.MISS
                && (props.getThroughWalls() || RotationUtil.rayCastEntity(3.0) == target);
        boolean inRange = PlayerUtil.isEntityWithinRange(target, props.getAttackRange());
        boolean canAttack = inRange && (rayCast || !props.getRaycast())
                && (!props.getCritMode().equals("Normal") || !mc.player.onGround())
                && (!(props.getCritMode().equals("Normal") || props.getCritMode().equals("Force"))
                    || !isGoingToPerformCritical(props.getCriticalSprint(), true));

        renderBlockHit = props.getForceBlockHitAnimation();

        if (canAutoBlock) {
            if (!props.getAutoBlockMode().equals("Modern Watchdog")) autoBlock(canAttack);
        } else {
            setBlockState(false);
        }

        if (canAutoBlock && props.getAutoBlockMode().equals("Modern Watchdog")) {
            if (mc.player.tickCount % 2 == 0 && rayCast) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            autoBlock(canAttack);
        }

        if (!canAttack || (props.getAutoBlock()
                && props.getAutoBlockMode().toLowerCase().contains("watchdog")
                && !props.getAutoBlockMode().equals("Modern Watchdog Post")))
            return;

        boolean shouldAttack;
        if (props.getLatestCPS()) {
            shouldAttack = mc.player.getAttackStrengthScale(0.0f) >= 0.95;
        } else if (props.getAutoBlockMode().equals("Modern Watchdog Post") && props.getAutoBlock()) {
            shouldAttack = mc.player.tickCount % 2 == 0;
        } else {
            shouldAttack = cpsTime.finished(getCPS());
        }

        if (shouldAttack) {
            if (props.getOneDotEight()) mc.player.swing(InteractionHand.MAIN_HAND);
            mc.gameMode.attack(mc.player, target);
            if (!props.getOneDotEight()) mc.player.swing(InteractionHand.MAIN_HAND);
            cpsTime.reset();
            mc.player.resetAttackStrengthTicker();
        }
    }

    private boolean isGoingToPerformCritical(boolean critSprint, boolean fallDist) {
        if (!mc.player.onGround() && !mc.player.onClimbable() && !mc.player.isInWater()
                && !mc.player.isUnderWater() && !mc.player.hasEffect(MobEffects.BLINDNESS)
                && !mc.player.isPassenger()) {
            if (!fallDist || mc.player.fallDistance > 0F) return !critSprint || !mc.player.isSprinting();
        }
        return false;
    }

    void autoBlock(boolean canHit) {
        switch (props.getAutoBlockMode()) {
            case "Vanilla" -> setBlockState(true);
            case "Blink" -> {
                if (mc.player.tickCount % 2 == 0) {
                    blinkStop(); isBlinking = false;
                    PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                    isBlocking = false;
                } else {
                    blinkStart(); isBlinking = true; setBlockState(true);
                }
            }
            case "Modern Watchdog Post" -> {
                if (mc.player.isUsingItem()) {
                    boolean pressed = false;
                    for (int i = 0; i < 9; i++) {
                        if (((KeyBindingAccessor) mc.options.keyHotbarSlots[i]).getTimesPressed() == 0) continue;
                        pressed = true; break;
                    }
                    if (!pressed) {
                        int sel = mc.player.getInventory().getSelectedSlot();
                        PacketUtil.send(new ServerboundSetCarriedItemPacket((sel + 1) % 9));
                        mc.player.releaseUsingItem();
                        PacketUtil.send(new ServerboundSetCarriedItemPacket(sel));
                    }
                }
                isBlocking = true;
            }
            case "Old Watchdog" -> {
                if (cpsTime.finished(getCPS())) {
                    unBlock(true);
                    if (canHit) { mc.gameMode.attack(mc.player, target); mc.player.swing(InteractionHand.MAIN_HAND); }
                    mc.getConnection().getConnection().send(ServerboundInteractPacket.createInteractionPacket(target, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
                    block(false, true);
                    cpsTime.reset();
                }
            }
            case "Modern Watchdog" -> {
                BlockHitResult hr;
                boolean rc = (hr = RotationUtil.calculateIntercept(target.getBoundingBox(), mc.player.getEyePosition(1.0f), RotationUtil.getCurrentHitVec(props.getAttackRange()))) != null
                        && hr.getType() != HitResult.Type.MISS && (props.getThroughWalls() || RotationUtil.rayCastEntity(3.0) == target);
                switch (autoBlockTicks++) {
                    case 1 -> {
                        if (rc) PacketUtil.send(ServerboundInteractPacket.createInteractionPacket(target, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
                        if (rotations != null) PacketUtil.sendSequenced(seq -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, seq, rotations[0], rotations[1]));
                        isBlocking = true; blinkStop(); isBlinking = false;
                    }
                    case 2 -> { blinkStart(); switchAndUnblock(true); isBlocking = false; autoBlockTicks = 1; }
                }
            }
        }
    }

    void block(boolean interact, boolean packet) {
        if (interact) mc.gameMode.interact(mc.player, target, InteractionHand.MAIN_HAND);
        if (packet) {
            if (rotations != null) PacketUtil.sendSequenced(seq -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, seq, rotations[0], rotations[1]));
        } else mc.options.keyUse.setDown(true);
        renderBlockHit = true; isBlocking = true;
    }

    void unBlock(boolean packet) {
        if (props.getAutoBlockMode().equals("Old Watchdog") && isBlocking) {
            int sel = mc.player.getInventory().getSelectedSlot();
            PacketUtil.send(new ServerboundSetCarriedItemPacket((sel + 1) % 9));
            mc.player.releaseUsingItem();
            PacketUtil.send(new ServerboundSetCarriedItemPacket(sel));
        }
        if (packet) PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        else mc.options.keyUse.setDown(false);
        renderBlockHit = false; isBlocking = false;
    }

    void switchAndUnblock(boolean packet) {
        int sel = mc.player.getInventory().getSelectedSlot();
        PacketUtil.send(new ServerboundSetCarriedItemPacket((sel + 1) % 9));
        if (packet) PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        else mc.options.keyUse.setDown(false);
        PacketUtil.send(new ServerboundSetCarriedItemPacket(sel));
        isBlocking = false;
    }

    void setBlockState(boolean state) {
        if (state) block(props.getInteractWhenBlocking(), props.getPacketAutoBlock());
        else {
            unBlock(props.getPacketAutoBlock());
            if (isBlinking) { blinkStop(); isBlinking = false; }
        }
    }

    void blinkStart() { Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking(); }
    void blinkStop() { Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking(); }

    int getCPS() { return (int) ((Math.random() * (props.getMaxCPS() - props.getMinCPS())) + props.getMinCPS()); }

    private KillauraRotations.HypixelRotProps rotationPropsAdapter(HypixelLogic.Props p) {
        return new KillauraRotations.HypixelRotProps() {
            public boolean getRotation() { return p.getRotation(); }
            public float getRotationRange() { return p.getRotationRange(); }
            public float getMinYawSpeed() { return p.getMinYawSpeed(); }
            public float getMaxYawSpeed() { return p.getMaxYawSpeed(); }
            public float getMinPitchSpeed() { return p.getMinPitchSpeed(); }
            public float getMaxPitchSpeed() { return p.getMaxPitchSpeed(); }
            public String getLegitRandomization() { return p.getLegitRandomization(); }
            public float getNoiseYawMultiplier() { return p.getNoiseYawMultiplier(); }
            public float getNoisePitchMultiplier() { return p.getNoisePitchMultiplier(); }
            public float getMaxYawNoise() { return p.getMaxYawNoise(); }
            public float getMaxPitchNoise() { return p.getMaxPitchNoise(); }
            public boolean getGcdFix() { return p.getGcdFix(); }
        };
    }

    public boolean getSwordInputBlocking() {
        return target != null && !props.getAutoBlockMode().equals("Modern Watchdog Post")
                && !props.getAutoBlockMode().equals("None") && !props.getAutoBlockMode().equals("Fake")
                && RotationUtil.getDistanceToEntityBox(target) <= props.getBlockRange() && props.getAutoBlock();
    }

    public boolean getSlowDownCancelled() {
        return props.getAutoBlock() && props.getAutoBlockMode().equals("Modern Watchdog") && target != null
                && RotationUtil.getDistanceToEntityBox(target) <= props.getBlockRange();
    }

    public void handlePacket(cc.samsara.event.events.impl.network.PacketEvent event) {
        if (event.getPacket() instanceof ServerboundClientTickEndPacket tick) {
            if (props.getWatchdogKeepSprint() && (!props.getDisableKeepSprintOnKB() || mc.player.hurtTime == 0)) {
                if (!Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class).isBlinking()) {
                    event.setCancelled(true);
                    PacketUtil.sendNoEvent(tick);
                }
            }
            if (props.getAutoBlock() && props.getAutoBlockMode().equals("Modern Watchdog Post")
                    && mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS) && target != null
                    && PlayerUtil.isEntityWithinRange(target, props.getBlockRange())) {
                var hr = RayTraceUtil.getHitResult();
                boolean safe = hr != null && hr.getEntity() == target;
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                if (safe) mc.gameMode.interact(mc.player, hr.getEntity(), InteractionHand.MAIN_HAND);
            }
        }
        if (event.getPacket() instanceof ServerboundPlayerActionPacket pkt && props.getAutoBlock()
                && props.getAutoBlockMode().equals("Old Watchdog") && killAuraTicks < 3
                && pkt.getAction().equals(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM)) {
            event.setCancelled(true);
        }
    }
}
