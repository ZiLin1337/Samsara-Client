package cc.samsara.module.impl.visual;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.render.Render3DEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.ColorProperty;
import cc.samsara.util.player.StorageUtil;
import cc.samsara.util.render.Render3DUtil;
import java.awt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import static cc.samsara.util.player.StorageUtil.collectLoadedAroundPlayer;

public class ChestESPModule extends Module {
    private final ColorProperty color = new ColorProperty("Color", Color.red);

    public ChestESPModule() {
        super(Category.VISUAL);
        this.registerProperties(color);
    }

    @Override
    public void onEnable() {
        StorageUtil.setCollectData(true);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        StorageUtil.setCollectData(false);
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        collectLoadedAroundPlayer();
        for (BlockPos blockPos : StorageUtil.snapshotAll()) {
            Render3DUtil.drawBoxESP(event.getMatricies(), new AABB(blockPos), color.getProperty(),150);
        }
    }
}