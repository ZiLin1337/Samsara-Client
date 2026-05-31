package cc.samsara.ui.screens.client;

import cc.samsara.interfaces.Fonts;
import cc.samsara.interfaces.IAccess;
import cc.samsara.ui.animations.Animation;
import cc.samsara.ui.animations.Easing;

public class MainMenu implements IAccess, Fonts {
    private static final Animation slideAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 800);
    private static boolean panelHovered = false;

    public static void render(int mouseX, int mouseY) {
     /*   String[] messages = {
                "Made with <3 By Kawase",
                "Protected By Polymorphism",
                "Special Thanks To Badaiim (For Contributions)",
                "Special Thanks To My Bf Ihasedich (For Providing Me Alts)"
        };

        float backgroundWidth = 0;
        for (String message : messages) {
            backgroundWidth = Math.max(backgroundWidth, product_regular_10.getStringWidth(message));
        }

        final float backgroundHeight = messages.length * 13 + 25 + 12 + 450;
        final float y1 = 3;
        final float y2 = y1 + backgroundHeight;
        final float panelWidth = backgroundWidth + 10;
        final float visibleX = 3;
        final float hiddenX = -panelWidth - 10;

        boolean isHovered = RenderUtil.isHovered(mouseX, mouseY, visibleX, y1, panelWidth, backgroundHeight);
        if (isHovered != panelHovered) {
            panelHovered = isHovered;
            slideAnimation.reset();
        }

        slideAnimation.setStartPoint(slideAnimation.getValue());
        slideAnimation.setEndPoint(panelHovered ? visibleX : hiddenX);
        slideAnimation.run(slideAnimation.getEndPoint());
        if (panelHovered || !slideAnimation.isFinished()) {
            float currentX = (float) slideAnimation.getValue();
            float x2 = (currentX + panelWidth);

            render.drawRoundedRectangle(currentX, y1,x2, y2, 5, 50, new Color(0, 0, 0, 150));

            // Samsara title
            float titleWidth = product_bold_20.getStringWidth(Samsara.NAME);
            float titleX = currentX + ((panelWidth - titleWidth) / 2);
            RenderUtil.drawGradientString(
                    product_bold_20, Samsara.NAME, titleX, y1 + 1,
                    Samsara.getInstance().getFirstColor(), Samsara.getInstance().getSecondColor(), true
            );

            float changelogWidth = product_regular_10.getStringWidth("Changelog");
            float changelogX = currentX + ((panelWidth - changelogWidth) / 2);
            float changelogY = y1 + 20;
            RenderUtil.drawGradientString(
                    product_regular_10, "Changelog", changelogX, changelogY,
                    Samsara.getInstance().getFirstColor(), Samsara.getInstance().getSecondColor(), true
            );
        }*/
    }
}