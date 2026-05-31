package cc.samsara.module.impl.combat.killaura;

import cc.samsara.event.events.impl.render.Render3DEvent;
import cc.samsara.interfaces.IAccess;
import cc.samsara.util.render.Render3DUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.awt.Color;

/**
 * Unified ESP rendering for both modes.
 * Supports Circle (Hypixel style) and Box (HeyPixel style) via style selection.
 */
public class ESPRenderer implements IAccess {

    private final ModeAccessor mode;
    private final EntityAccessor targets;

    public interface ModeAccessor {
        String getEspStyle();
        boolean isEspEnabled();
        boolean isHypixel();
        Entity getTarget();
        Entity getHpTarget();
    }

    public interface EntityAccessor {
        Entity getRenderTarget();
    }

    public ESPRenderer(ModeAccessor mode) {
        this.mode = mode;
        this.targets = null; // used from mode directly
    }

    public void onRender3D(Render3DEvent event) {
        if (!mode.isEspEnabled()) return;

        Entity renderTarget = mode.isHypixel() ? mode.getTarget() : mode.getHpTarget();
        if (renderTarget == null) return;

        String style = mode.getEspStyle();

        if (style.equals("Auto")) {
            if (mode.isHypixel()) {
                Render3DUtil.drawCircleESP(event.getMatricies(), renderTarget);
            } else {
                renderBoxESP(event.getMatricies(), renderTarget);
            }
        } else if (style.equals("Circle")) {
            Render3DUtil.drawCircleESP(event.getMatricies(), renderTarget);
        } else if (style.equals("Box")) {
            renderBoxESP(event.getMatricies(), renderTarget);
        }
    }

    private void renderBoxESP(PoseStack stack, Entity entity) {
        stack.pushPose();
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 camPos = cam.getPosition();
        stack.translate(-camPos.x(), -camPos.y(), -camPos.z());

        double dx = entity.getX() - entity.xOld;
        double dy = entity.getY() - entity.yOld;
        double dz = entity.getZ() - entity.zOld;
        Vec3 playerDelta = mc.player.getDeltaMovement();
        Vec3 offset = new Vec3(dx + playerDelta.x + 0.005,
                dy + playerDelta.y - 0.002,
                dz + playerDelta.z + 0.005);

        int hurtTime = entity instanceof LivingEntity le ? le.hurtTime : 0;
        Color color = hurtTime == 0 ? new Color(0, 0, 0, 130) : new Color(255, 0, 0, 200);

        AABB base = entity.getBoundingBox().move(offset);
        AABB padded = new AABB(
                base.minX - 0.175, base.minY - 0.125, base.minZ - 0.175,
                base.maxX + 0.175, base.maxY + 0.225, base.maxZ + 0.175);
        Render3DUtil.drawBoxESP(stack, padded, color, color.getAlpha());
        stack.popPose();
    }
}
