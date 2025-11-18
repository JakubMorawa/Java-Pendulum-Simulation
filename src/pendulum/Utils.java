package pendulum;

import java.awt.*;

public class Utils {

    // ============================================================
    // COLOR MANIPULATION
    // ============================================================

    /**
     * Darkens and enriches a color by decreasing brightness and slightly increasing saturation.
     */
    public static Color richDarken(Color color, float darkenFactor) {
        darkenFactor = Math.max(0f, Math.min(1f, darkenFactor));

        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        // Decrease brightness (not below 0)
        hsb[2] = Math.max(0f, hsb[2] * (1f - darkenFactor));

        // Slightly increase saturation (not above 1)
        hsb[1] = Math.min(1f, hsb[1] * (1f + darkenFactor));

        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    public static Color lighten(Color color, double factor) {
        int r = (int) Math.min(color.getRed() + (255 - color.getRed()) * factor, 255);
        int g = (int) Math.min(color.getGreen() + (255 - color.getGreen()) * factor, 255);
        int b = (int) Math.min(color.getBlue() + (255 - color.getBlue()) * factor, 255);
        return new Color(r, g, b);
    }

    public static Color darken(Color color, double factor) {
        int r = (int) Math.max(color.getRed() * (1 - factor), 0);
        int g = (int) Math.max(color.getGreen() * (1 - factor), 0);
        int b = (int) Math.max(color.getBlue() * (1 - factor), 0);
        return new Color(r, g, b);
    }

    // ============================================================
    // BASIC SHAPES
    // ============================================================

    public static void drawSquare(Graphics2D g2, int x, int y, int width, int height, Color color, int borderWidth) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x - borderWidth, y - borderWidth, width + 2 * borderWidth, height + 2 * borderWidth);

        g2.setColor(color);
        g2.fillRect(x, y, width, height);
    }

    public static void drawBox(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setColor(color);
        g2.fillRect(x, y, width, height);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }

    public static void drawRoundSquare(Graphics2D g2, int x, int y, int width, int height,
                                       Color fillColor, float borderDarken, double innerLighten) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Base fill
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height, 25, 25);

        // Outer dark border
        g2.setColor(Utils.richDarken(fillColor, borderDarken));
        g2.drawRoundRect(x, y, width, height, 25, 25);

        // Inner light border
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Utils.lighten(fillColor, innerLighten));

        int border = 2;
        g2.drawRoundRect(x + border, y + border, width - 2 * border, height - 2 * border, 25, 25);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public static void drawCircle(Graphics2D g2, int x, int y, int width, int height,
                              Color fillColor, Color borderColor, float borderWidth) {

            // Enable smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill the circle
            g2.setColor(fillColor);
            g2.fillOval(x, y, width, height);

            // Draw the border with custom stroke
            g2.setColor(borderColor);
            Stroke oldStroke = g2.getStroke(); // save the old stroke
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawOval(x, y, width, height);
            g2.setStroke(oldStroke); // restore the old stroke

            // Turn off antialiasing if you want to reset the state
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

    // ============================================================
    // TEXT RENDERING
    // ============================================================

    /**
     * Draws text centered vertically within a box.
     */
    public static void drawTextInBox(Graphics2D g2, String text, int x, int y, int width, int height, int fontSize) {
        Shape oldClip = g2.getClip();
        g2.setClip(x, y, width, height);
        Font font = new Font("Poppins", Font.BOLD, fontSize);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();

        g2.drawString(text, x + 5, textY);
        g2.setClip(oldClip);
    }


    //Draws text with an outline (border) for better readability.
    public static void drawTextWithBorder(Graphics2D g2, String text, int x, int y,
                                          Font font, Color textColor, Color borderColor, int thickness) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);
        g2.setColor(borderColor);

        // Border text (outline)
        for (int i = -thickness; i <= thickness; i++) {
            for (int j = -thickness; j <= thickness; j++) {
                if (i * i + j * j <= thickness * thickness) {
                    g2.drawString(text, x + i, y + j);
                }
            }
        }

        // Foreground text
        g2.setColor(textColor);
        g2.drawString(text, x, y);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    // ============================================================
    // SPECIAL EFFECTS
    // ============================================================

    public static void drawVignette(Graphics2D g2, int width, int height,
                                    Color innerColor, Color outerColor) {

        int cx = width / 2;
        int cy = height / 2;
        float radius = (float) Math.hypot(cx, cy);

        float[] dist = {0.0f, 1.0f};
        Color[] colors = {innerColor, outerColor};

        RadialGradientPaint paint = new RadialGradientPaint(cx, cy, radius, dist, colors);
        g2.setPaint(paint);
        g2.fillRect(0, 0, width, height);
    }

    public static void drawGrid(Graphics2D g2, int cameraX, int cameraY, int cornerX, int cornerY, int width, int height, int spacing, Color color, int stroke) {
        g2.setStroke(new BasicStroke(stroke));
        g2.setColor(color);

        // Vertical lines
        for (int x = cameraX; x <= cornerX + width; x += spacing) {
            g2.drawLine(x, cornerY, x, cornerY + height);
        }
        for (int x = cameraX; x >= cornerX; x -= spacing) {
            g2.drawLine(x, cornerY, x, cornerY + height);
        }

        // Horizontal lines
        for (int y = cameraY; y <= cornerY + height; y += spacing) {
            g2.drawLine(cornerX, y, cornerX + width, y);
        }
        for (int y = cameraY; y >= cornerY ; y -= spacing) {
            g2.drawLine(cornerX, y, cornerX + width, y);
        }
        
        // g2.setStroke(new BasicStroke(10));
        // g2.setColor(Color.yellow);
        // g2.drawLine(cornerX, cameraY, cornerX + width, cameraY);
        // g2.setStroke(new BasicStroke(3));
    }



    public static void drawArrow(Graphics2D g2, int x, int y, int i, int j, int border, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(border));

        // Arrowhead size
        int arrowSize = 12;

        // Angle of the arrow line
        double angle = Math.atan2(j, i);

        // Compute the original end point
        int endX = x + i;
        int endY = y + j;

        if (i*i+j*j>=arrowSize*arrowSize) {
            // NEW: shorten the line so the arrowhead is not overlapped
            int lineEndX = (int) (endX - arrowSize * Math.cos(angle));
            int lineEndY = (int) (endY - arrowSize * Math.sin(angle));
    
            // Draw shortened line
            g2.drawLine(x, y, lineEndX, lineEndY);
    
            // Arrowhead triangle points
            int x1 = (int) (endX - arrowSize * Math.cos(angle - Math.PI / 6));
            int y1 = (int) (endY - arrowSize * Math.sin(angle - Math.PI / 6));
    
            int x2 = (int) (endX - arrowSize * Math.cos(angle + Math.PI / 6));
            int y2 = (int) (endY - arrowSize * Math.sin(angle + Math.PI / 6));
    
            // Create the filled triangle arrowhead
            Polygon arrowHead;
            arrowHead = new Polygon();
            arrowHead.addPoint(endX, endY); // tip
            arrowHead.addPoint(x1, y1);     // left edge
            arrowHead.addPoint(x2, y2);     // right edge
            g2.fillPolygon(arrowHead);
        }else{
            g2.drawLine(x, y, endX, endY);
        }
    }
}