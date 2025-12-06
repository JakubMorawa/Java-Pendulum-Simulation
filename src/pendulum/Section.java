package pendulum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Section {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int borderRadius;
    private final int borderWidth;
    private final Color backgroundColor;
    private final Color borderColor;
    private final int padding = 10;

    private final int elementWidth = 100;
    private final int elementHeight = 40;
    private final int elementSpacing = 10; 

    private final List<Button> buttons = new ArrayList<>();

    public Section(int x, int y, int width, int height, int borderRadius, int borderWidth, Color backgroundColor, Color borderColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.borderRadius = borderRadius;
        this.borderWidth = borderWidth;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }

    public void draw(Graphics2D g2) {
        // Draw border
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);
        g2.fillRoundRect(x - borderWidth, y - borderWidth, width + 2 * borderWidth, height + 2 * borderWidth, borderRadius, borderRadius);

        // Draw background
        g2.setColor(backgroundColor);
        int sizeDiff = 6;
        g2.fillRoundRect(x, y, width, height, borderRadius-sizeDiff, borderRadius-sizeDiff);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public void setupButtons(Graphics2D g2, java.util.List<Button> buttons) {
        int currentX = x + padding;
        int currentY = y + padding;

        for (Button button : buttons) {
            button.setBounds(currentX, currentY, elementWidth, elementHeight);
            currentY += elementHeight + elementSpacing;

            // Move to next column if exceeding section height
            if (currentY + elementHeight + padding > y + height) {
                currentY = y + padding;
                currentX += elementWidth + elementSpacing;
            }
        }
    }
}
