package pendulum.uielements;

import java.awt.*;

public class Section {
    private final int x;
    private final int y;
    private int width;
    private int height;
    private final int borderRadius;
    private final int borderWidth;
    private final Color backgroundColor;
    private final Color borderColor;
    private final int padding = 10;

    private final int elementSpacing = 10; 

    private int currentElement = 0;
    private int currentX = 0;
    private int currentY = 0;
    private int row = 0;

    public Section(int x, int y, int width, int height, int borderRadius, int borderWidth, Color backgroundColor, Color borderColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.borderRadius = borderRadius;
        this.borderWidth = borderWidth;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        currentX = x + padding;
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

    public void nextRow() {
        row++;
        currentX = 0;
        currentElement = 0;
    }


    public void formatElement(UIElement element) {

        int elementWidth  = element.getPreferredWidth();
        int elementHeight = element.getPreferredHeight();

        // Base X position
        currentX = x + padding + (currentElement * (elementWidth + elementSpacing));

        // Base Y position
        currentY = y + padding + (row * (elementHeight + elementSpacing));

        // APPLY OFFSET
        int finalX = currentX + element.getOffsetX();
        int finalY = currentY + element.getOffsetY();

        // --- UPDATE SECTION WIDTH BASED ON OFFSET ---
        int rightEdge = (finalX - x) + padding + elementWidth;
        if (rightEdge > width) {
            width = rightEdge;
        }

        // --- UPDATE SECTION HEIGHT BASED ON OFFSET ---
        int bottomEdge = (finalY - y) + padding + elementHeight;
        if (bottomEdge > height) {
            height = bottomEdge;
        }

        // Set final element position
        element.setBounds(finalX, finalY, elementWidth, elementHeight);
    }

    public void addElement(UIElement element) {
        formatElement(element);
        currentElement++;
    }
}
