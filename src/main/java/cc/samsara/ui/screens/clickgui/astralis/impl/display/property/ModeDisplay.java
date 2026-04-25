package cc.samsara.ui.screens.clickgui.astralis.impl.display.property;

import cc.samsara.font.UIFont;
import cc.samsara.font.FontManager;
import cc.samsara.property.properties.ModeProperty;
import cc.samsara.ui.screens.clickgui.astralis.Component;
import cc.samsara.ui.screens.clickgui.astralis.impl.display.ModuleDisplay;
import cc.samsara.util.render.RenderUtil;

import java.util.List;

public class ModeDisplay extends Component {
    private final ModeProperty property;

    public ModeDisplay(final ModeProperty property, final float width, final float height) {
        this.property = property;
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);
        final UIFont uiFont = FontManager.getFont("Sf-Ui", 10);
        uiFont.drawStringWithShadow(property.getName(), x + ModuleDisplay.PADDING, y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);
        uiFont.drawStringWithShadow(property.getProperty(), x + getWidth() - (uiFont.getStringWidth(property.getProperty()) + ModuleDisplay.PADDING), y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);
    }

    @Override
    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            final List<String> modes = List.of(property.getModes());
            final int length = modes.size();
            final int index = modes.indexOf(property.getProperty());
            if (button == 0) {
                property.setProperty(index < length - 1 ? modes.get(index + 1) : modes.getFirst());
            } else {
                property.setProperty(index > 0 ? modes.get(index - 1) : modes.getLast());
            }
        }
    }
}
