package cc.samsara.module.impl.combat.killaura;

import cc.samsara.Samsara;
import cc.samsara.component.impl.player.RotationComponent;
import cc.samsara.interfaces.IAccess;
import cc.samsara.module.impl.combat.AntiBotModule;
import cc.samsara.module.impl.combat.KillauraModule;
import cc.samsara.util.player.TeamUtil;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * HeyPixel mode combat logic for KillauraModule.
 * Adapted from OpenZen KillAura. Full target selection, priority, CPS, multi-attack.
 */
public class HeyPixelLogic implements IAccess {

    // Config accessor
    public interface Props {
        boolean getHpAttackPlayer();
        boolean getHpAttackInvisible();
        boolean getHpAttackAnimals();
        boolean getHpAttackMobs();
        boolean getHpMultiAttack();
        boolean getHpInfSwitch();
        boolean getHpPreferBaby();
        boolean getHpKeepSprint();
        boolean getHpIgnoreSkipTicks();
        float getHpAimRange();
        float getHpMaxAPS();
        float getHpMinAPS();
        int getHpSwitchSize();
        int getHpSwitchDelay();
        float getHpFoV();
        int getHpHurtTime();
        String getHpDelayMode();
        String getHpPriorityMode();
        boolean getThroughWalls();    // shared with Hypixel
        boolean getIgnoreTeamMates(); // shared
    }

    // State
    public Entity hpTarget;
    public Entity hpAimingTarget;
    public List<Entity> hpTargetList = new ArrayList<>();
    private int hpAttackTimes;
    private float hpAttacks;
    private int hpTargetIndex;
    private int hpSprintTickCounter;
    private int hpSprintCounter;
    private float hpCurrentYaw, hpCurrentPitch;
    private net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> lastWorldKey;

    private final Props props;

    public HeyPixelLogic(Props props) {
        this.props = props;
    }

    public void onEnable() {
        hpTarget = null;
        hpAimingTarget = null;
        hpTargetList.clear();
        hpAttacks = 0.0f;
        hpTargetIndex = 0;
        hpSprintTickCounter = 0;
        hpSprintCounter = 0;
        hpAttackTimes = 0;
        if (mc.level != null) lastWorldKey = mc.level.dimension();
    }

    public void onDisable() {
        hpTarget = null;
        hpAimingTarget = null;
        hpTargetList.clear();
        hpAttacks = 0.0f;
        hpSprintTickCounter = 0;
        hpSprintCounter = 0;
        hpAttackTimes = 0;
    }

    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        // World change detection
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> currentKey = mc.level.dimension();
        if (!currentKey.equals(lastWorldKey)) {
            hpTarget = null;
            hpAimingTarget = null;
            hpAttacks = 0.0f;
            hpTargetList.clear();
            lastWorldKey = currentKey;
            return;
        }

        // Block if inventory open
        if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen) {
            hpClearState();
            return;
        }

        hpUpdateTargets();
        hpAimingTarget = hpGetTarget();

        if (hpTargetList.isEmpty()) {
            hpTarget = null;
            hpAttacks = 0.0f;
            return;
        }

        boolean isSwitch = props.getHpSwitchSize() > 1 || props.getHpInfSwitch() || props.getHpMultiAttack();

        if (hpTargetIndex >= hpTargetList.size()) hpTargetIndex = 0;

        if (hpTargetList.size() > 1 && (hpAttackTimes >= props.getHpSwitchDelay())) {
            hpAttackTimes = 0;
            for (int i = 0; i < hpTargetList.size(); ++i) {
                ++hpTargetIndex;
                if (hpTargetIndex >= hpTargetList.size()) hpTargetIndex = 0;
                if (hpTargetIndex < hpTargetList.size()) break;
            }
        }

        if (hpTargetIndex >= hpTargetList.size() || !isSwitch) hpTargetIndex = 0;
        hpTarget = hpTargetList.get(hpTargetIndex);

        if (props.getHpDelayMode().equals("1.8")) {
            float aps = props.getHpMaxAPS();
            float minAps = props.getHpMinAPS();
            if (props.getHpKeepSprint()) { aps *= 2.0f; minAps *= 2.0f; }
            hpAttacks += (float) (minAps + Math.random() * (aps - minAps)) / 20.0f;
        } else if (hpSprintCounter > 0) {
            hpSprintCounter--;
        } else if (mc.player.getAttackStrengthScale(0.0f) >= 0.9f) {
            hpDoAttack();
        }
    }

    private void hpClearState() {
        hpTarget = null; hpAimingTarget = null; hpTargetList.clear();
        hpAttacks = 0.0f; hpSprintTickCounter = 0; hpSprintCounter = 0; hpAttackTimes = 0;
    }

    private void hpUpdateTargets() { hpTargetList = hpGetTargets(); }

    private Entity hpGetTarget() { return !hpTargetList.isEmpty() ? hpTargetList.get(0) : null; }

    private boolean hpIsValidTarget(Entity entity) {
        if (mc.player == null || entity == mc.player) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDeadOrDying() || living.getHealth() <= 0.0f) return false;
        if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand) return false;
        if (entity.isInvisible() && !props.getHpAttackInvisible()) return false;
        if (entity instanceof Player) {
            if (props.getIgnoreTeamMates() && TeamUtil.isSameTeam(entity)) return false;
            if (!props.getHpAttackPlayer()) return false;
            if (entity.getBbWidth() < 0.5 || living.isSleeping()) return false;
            if (entity.isSpectator()) return false;
            if (AntiBotModule.isBot((Player) entity)) return false;
        }
        if ((entity instanceof net.minecraft.world.entity.Mob || entity instanceof net.minecraft.world.entity.animal.AbstractGolem) && !props.getHpAttackMobs()) return false;
        if ((entity instanceof net.minecraft.world.entity.animal.Animal || entity instanceof net.minecraft.world.entity.npc.Villager) && !props.getHpAttackAnimals()) return false;
        return true;
    }

    private boolean hpIsValidAttack(Entity entity) {
        if (mc.player == null) return false;
        if (!hpIsValidTarget(entity)) return false;
        if (entity instanceof LivingEntity le && le.hurtTime > props.getHpHurtTime()) return false;
        Vec3 closest = hpClosestPoint(mc.player.getEyePosition(), entity.getBoundingBox());
        if (closest.distanceTo(mc.player.getEyePosition()) > props.getHpAimRange()) return false;
        return hpIsInFov(entity, props.getHpFoV() / 2.0f);
    }

    private Vec3 hpClosestPoint(Vec3 eye, AABB box) {
        return new Vec3(Mth.clamp(eye.x, box.minX, box.maxX), Mth.clamp(eye.y, box.minY, box.maxY), Mth.clamp(eye.z, box.minZ, box.maxZ));
    }

    private boolean hpIsInFov(Entity entity, float fov) {
        Vec3 dir = entity.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        return Math.abs(Mth.wrapDegrees(yaw - mc.player.getYRot())) <= fov;
    }

    private void hpDoAttack() {
        if (hpTargetList.isEmpty() || mc.player == null || mc.gameMode == null) return;
        ++hpAttackTimes;
        hpCurrentYaw = mc.player.getYRot();
        hpCurrentPitch = mc.player.getXRot();

        if (RotationComponent.targetRotation != null) {
            mc.player.setYRot(RotationComponent.targetRotation[0]);
            mc.player.setXRot(RotationComponent.targetRotation[1]);
        }

        if (props.getHpMultiAttack()) {
            int attacked = 0;
            for (Entity e : hpTargetList) {
                if (mc.player == null) break;
                if (e.distanceTo(mc.player) >= 3.0) continue;
                mc.gameMode.attack(mc.player, e);
                mc.player.swing(InteractionHand.MAIN_HAND);
                if (++attacked >= 2) break;
            }
        } else {
            HitResult hr = mc.hitResult;
            if (hr != null && hr.getType() == HitResult.Type.ENTITY) {
                mc.gameMode.attack(mc.player, ((EntityHitResult) hr).getEntity());
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        mc.player.setYRot(hpCurrentYaw);
        mc.player.setXRot(hpCurrentPitch);

        if (props.getHpDelayMode().equals("1.9")) {
            hpSprintCounter = (int) mc.player.getCurrentItemAttackStrengthDelay();
        }
    }

    private List<Entity> hpGetTargets() {
        if (mc.player == null || mc.level == null) return new ArrayList<>();
        List<Entity> list = new ArrayList<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (hpIsValidAttack(e)) list.add(e);
        }
        switch (props.getHpPriorityMode()) {
            case "Distance" -> list.sort(Comparator.comparingDouble(e -> e.distanceTo(mc.player)));
            case "FoV" -> list.sort(Comparator.comparingDouble(this::hpAngleDiff));
            case "Health" -> list.sort(Comparator.comparingDouble(e -> e instanceof LivingEntity le ? le.getHealth() : 999));
        }
        if (props.getHpPreferBaby() && list.stream().anyMatch(e -> e instanceof LivingEntity le && le.isBaby())) {
            list.removeIf(e -> !(e instanceof LivingEntity) || !((LivingEntity) e).isBaby());
        }
        list.sort(Comparator.comparingInt(e -> e instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal ? 1 : 0));
        if (props.getHpInfSwitch()) return list;
        int limit = Math.min(list.size(), props.getHpSwitchSize());
        return new ArrayList<>(list.subList(0, limit));
    }

    private double hpAngleDiff(Entity entity) {
        Vec3 dir = entity.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        return Math.abs(Mth.wrapDegrees(yaw - mc.player.getYRot()));
    }

    public void handlePacket(cc.samsara.event.events.impl.network.PacketEvent event) {
        if (props.getHpKeepSprint() && event.getPacket() instanceof ServerboundClientTickEndPacket) {
            if (hpSprintTickCounter % 2 == 0 && mc.player != null) {
                mc.player.setSprinting(false);
            }
            hpSprintTickCounter++;
        }
    }
}
