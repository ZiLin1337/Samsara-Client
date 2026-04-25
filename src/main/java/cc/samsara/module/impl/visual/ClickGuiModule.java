package cc.samsara.module.impl.visual;

import cc.samsara.Samsara;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.ColorProperty;
import cc.samsara.property.properties.ModeProperty;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.ui.screens.clickgui.astralis.AstralisClickGUI;
import cc.samsara.ui.screens.clickgui.dropdown.DropdownCGUIScreen;
import cc.samsara.ui.imgui.windows.ClickGuiWindow;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ClickGuiModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Samsara", "Imgui", "Dropdown", "Samsara");
    public NumberProperty imguiAlpha = new NumberProperty("Imgui Alpha", 240, 0, 255, 1);
    public BooleanProperty blur = new BooleanProperty("Blur", true);
    public BooleanProperty onlyBlurImgui = new BooleanProperty("Only Blur Imgui", false);
    public NumberProperty blurRadius = new NumberProperty("Blur Radius", 7, 1, 50, 1);
    public BooleanProperty gradient = new BooleanProperty("Gradient", true);
    public BooleanProperty onlyGradientImgui = new BooleanProperty("Only Gradient With Imgui", false);
    public NumberProperty gradientAlpha = new NumberProperty("Gradient Alpha", 55, 1, 255, 1);
    public ColorProperty color = new ColorProperty("Color", new Color(78, 73, 165));
    public ColorProperty hovColor = new ColorProperty("Hover Color", new Color(66, 61, 140));

    private final ClickGuiWindow imgui = new ClickGuiWindow();

    private AstralisClickGUI samsaraScreen;
    private DropdownCGUIScreen dropdownScreen;
   // private final PowerShellCguiScreen powerShellCguiScreen = new PowerShellCguiScreen();

    public ClickGuiModule() {
        super(Category.VISUAL, GLFW.GLFW_KEY_RIGHT_SHIFT);
        registerProperties(mode,
                blur.setVisible(() -> mode.is("Imgui")),
                onlyBlurImgui.setVisible(() -> blur.getProperty() && blur.getVisible().get()),
                blurRadius.setVisible(() -> blur.getProperty() && blur.getVisible().get()),
                gradient.setVisible(() -> mode.is("Imgui")),
                onlyGradientImgui.setVisible(() -> gradient.getProperty() && gradient.getVisible().get()),
                gradientAlpha.setVisible(() -> gradient.getProperty() && gradient.getVisible().get()),
                imguiAlpha.setVisible(() -> mode.is("Imgui")),
                color.setVisible(() -> mode.is("Imgui")),
                hovColor.setVisible(() -> mode.is("Imgui"))
        );
    }

    @Override
    public void onEnable() {
        switch (mode.getProperty()) {
            case "Imgui" ->  mc.setScreen(imgui);
            case "Samsara" -> {
                if (samsaraScreen == null) {
                    samsaraScreen = new AstralisClickGUI();
                }

                mc.setScreen(samsaraScreen);
            }
            case "Dropdown" -> {
                if (dropdownScreen == null) {
                    dropdownScreen = new DropdownCGUIScreen();
                }

                Samsara.getInstance().getEventManager().register(dropdownScreen);
                mc.setScreen(dropdownScreen);
            }
        }

        this.toggle();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
