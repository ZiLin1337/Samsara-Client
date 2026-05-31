package cc.samsara.module.impl.movement;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;

/**
 * Stub implementation for ScaffoldRecodeModule
 * This is a placeholder class to satisfy compilation requirements
 */
public class ScaffoldRecodeModule extends Module {
    
    public final BooleanProperty spoofItem = new BooleanProperty("Spoof Item", false);
    public int spoofedSlot = -1;
    
    public ScaffoldRecodeModule() {
        super(Category.MOVEMENT);
        registerProperties(spoofItem);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
    }
    
    @Override
    public void onDisable() {
        spoofedSlot = -1;
        super.onDisable();
    }
}