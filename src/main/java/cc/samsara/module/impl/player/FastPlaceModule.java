package cc.samsara.module.impl.player;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import samsara.mixin.accessor.mc.MinecraftClientSessionAccessor;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.util.player.PlayerUtil;
import cc.samsara.util.player.scaffold.ScaffoldWalkUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class FastPlaceModule extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 1, 0, 4, 1);
    private final BooleanProperty blocksOnly = new BooleanProperty("Blocks only", true);
    private final BooleanProperty autoSwap = new BooleanProperty("Auto Swap", true);
    private final NumberProperty swapAmount = new NumberProperty("Swap Amount", 1, 1, 64, 1);

    public FastPlaceModule() {
        super(Category.PLAYER);
        registerProperties(delay, blocksOnly, autoSwap, swapAmount);
    }

    @EventTarget
    public void on(TickEvent e) {
        if (blocksOnly.getProperty() && !PlayerUtil.isHoldingBlocks()) return;
        if (((MinecraftClientSessionAccessor) mc).getItemUseCooldown() > delay.getProperty().intValue()) {
            ((MinecraftClientSessionAccessor) mc).setItemUseCooldown(delay.getProperty().intValue());
        }

        if (autoSwap.getProperty() && mc.options.keyUse.isDown()) {
            ItemStack current = mc.player.getMainHandItem();
            if (current.getItem() instanceof BlockItem blockItem && ScaffoldWalkUtil.canPlaceBlock(blockItem.getBlock()) && current.getCount() <= swapAmount.getProperty().intValue()) {
                int biggestBlockSlot = findBiggestBlockStack();
                if (biggestBlockSlot != -1 && biggestBlockSlot != mc.player.getInventory().getSelectedSlot()) {
                    mc.player.getInventory().setSelectedSlot(biggestBlockSlot);
                }
            }
        }
    }

    private int findBiggestBlockStack() {
        int biggestStackSlot = -1;
        int biggestStackSize = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem && ScaffoldWalkUtil.canPlaceBlock(blockItem.getBlock())) {
                if (stack.getCount() > biggestStackSize) {
                    biggestStackSize = stack.getCount();
                    biggestStackSlot = i;
                }
            }
        }
        return biggestStackSlot;
    }
}