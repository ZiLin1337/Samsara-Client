package cc.samsara.module.impl.visual;

import cc.samsara.ui.screens.client.HudEditorScreen;
import cc.samsara.module.Category;
import cc.samsara.module.Module;

public class HudEditorModule extends Module {
    private final HudEditorScreen hudEditorScreen;

    public HudEditorModule() {
        super(Category.VISUAL);
        hudEditorScreen = new HudEditorScreen();
    }

    @Override
    public void onEnable() {
        mc.setScreen(hudEditorScreen);
        this.setToggled(false);
        super.onEnable();
    }
}
