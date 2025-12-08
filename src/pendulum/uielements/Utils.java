package pendulum.uielements;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility class for color manipulation, shape drawing, text rendering,
 * and special visual effects. Designed for simplifying common Graphics2D tasks.
 */
public class Utils {

    // ============================================================
    // COLOR MANIPULATION
    // ============================================================

    /**
     * Darkens and enriches a color by reducing brightness and slightly
     * increasing saturation.
     *
     * @param color        Original color
     * @param darkenFactor Value between 0 and 1 controlling intensity
     * @return Modified darker, richer color
     */
    public static Color richDarken(Color color, float darkenFactor) {
        darkenFactor = Math.max(0f, Math.min(1f, darkenFactor));

        float[] hsb = Color.RGBtoHSB(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            null
        );

        hsb[2] = Math.max(0f, hsb[2] * (1f - darkenFactor));       // Brightness
        hsb[1] = Math.min(1f, hsb[1] * (1f + darkenFactor));       // Saturation

        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    /**
     * Lightens a color by interpolating it toward white.
     */
    public static Color lighten(Color color, double factor) {
        int r = (int) Math.min(color.getRed() + (255 - color.getRed()) * factor, 255);
        int g = (int) Math.min(color.getGreen() + (255 - color.getGreen()) * factor, 255);
        int b = (int) Math.min(color.getBlue() + (255 - color.getBlue()) * factor, 255);
        return new Color(r, g, b);
    }

    /**
     * Darkens a color by interpolating it toward black.
     */
    public static Color darken(Color color, double factor) {
        int r = (int) Math.max(color.getRed() * (1 - factor), 0);
        int g = (int) Math.max(color.getGreen() * (1 - factor), 0);
        int b = (int) Math.max(color.getBlue() * (1 - factor), 0);
        return new Color(r, g, b);
    }

    // ============================================================
    // BASIC SHAPES
    // ============================================================

    /** Draws a colored square with a black border of adjustable width. */
    public static void drawSquare(Graphics2D g2, int x, int y, int width, int height,
                                Color color, int borderWidth) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x - borderWidth, y - borderWidth, width + 2 * borderWidth, height + 2 * borderWidth);

        g2.setColor(color);
        g2.fillRect(x, y, width, height);
    }

    /** Draws a solid filled box with a thin black border. */
    public static void drawBox(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setColor(color);
        g2.fillRect(x, y, width, height);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }

    /**
     * Draws a rounded rectangle with a fill, a darker outer border,
     * and a lighter inner border.
     */
    public static void drawRoundSquare(Graphics2D g2, int x, int y, int width, int height,
                                    Color fillColor, float borderDarken, double innerLighten) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height, 25, 25);

        // Dark outer border
        g2.setColor(Utils.richDarken(fillColor, borderDarken));
        g2.drawRoundRect(x, y, width, height, 25, 25);

        // Light inner border
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Utils.lighten(fillColor, innerLighten));
        g2.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 25, 25);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public static void drawRoundSquare(Graphics2D g2, int x, int y, int width, int height,
                                    Color fillColor, float borderDarken, double innerLighten, int arc, int borderWidth) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dark outer border
        g2.setStroke(new BasicStroke(borderWidth));
        g2.setColor(Utils.richDarken(fillColor, borderDarken));
        g2.fillRoundRect(x - borderWidth, y - borderWidth, width + 2 * borderWidth, height + 2 * borderWidth, arc+8, arc+8);

        // Fill
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height, arc, arc);

        // Light inner border
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Utils.lighten(fillColor, innerLighten));
        g2.drawRoundRect(x + 2, y + 2, width - 4, height - 4, arc, arc);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /** Draws a filled circle with a custom border width and color. */
    public static void drawCircle(Graphics2D g2, int x, int y, int width, int height,
                                Color fillColor, Color borderColor, float borderWidth) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill
        g2.setColor(fillColor);
        g2.fillOval(x, y, width, height);

        // Border
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(borderWidth));
        g2.setColor(borderColor);
        g2.drawOval(x, y, width, height);
        g2.setStroke(oldStroke);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    // ============================================================
    // TEXT RENDERING
    // ============================================================

    /**
     * Draws text vertically centered inside a rectangular region.
     */
    public static void drawTextInBox(Graphics2D g2, String text, int x, int y,
                                    int width, int height, int fontSize) {

        Shape oldClip = g2.getClip();
        g2.setClip(x, y, width, height);

        Font font = new Font("Poppins", Font.BOLD, fontSize);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();

        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, x + 5, textY);

        g2.setClip(oldClip);
    }

    public static void drawTextCentered(Graphics2D g2, String text, int x, int y,
                                    int width, int height, int fontSize) {

        Shape oldClip = g2.getClip();
        g2.setClip(x, y, width, height);

        Font font = new Font("Poppins", Font.BOLD, fontSize);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;

        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, textX, textY);

        g2.setClip(oldClip);
    }


    /**
     * Draws text with an outline for improved visibility.
     */
    public static void drawTextWithBorder(Graphics2D g2, String text, int x, int y,
                                        Font font, Color textColor, Color borderColor,
                                        int thickness) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);

        // Border (outline)
        g2.setColor(borderColor);
        for (int i = -thickness; i <= thickness; i++) {
            for (int j = -thickness; j <= thickness; j++) {
                if (i * i + j * j <= thickness * thickness) {
                    g2.drawString(text, x + i, y + j);
                }
            }
        }

        // Main text
        g2.setColor(textColor);
        g2.drawString(text, x, y);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    // ============================================================
    // SPECIAL EFFECTS
    // ============================================================

    /** Draws a radial vignette effect over the entire area. */
    public static void drawVignette(Graphics2D g2, int width, int height,
                                    Color innerColor, Color outerColor) {

        int cx = width / 2;
        int cy = height / 2;
        float radius = (float) Math.hypot(cx, cy);

        float[] dist = {0f, 1f};
        Color[] colors = {innerColor, outerColor};

        RadialGradientPaint paint = new RadialGradientPaint(cx, cy, radius, dist, colors);
        g2.setPaint(paint);
        g2.fillRect(0, 0, width, height);
    }

    /** Draws a grid extending from a camera point with configurable spacing. */
    public static void drawGrid(Graphics2D g2, int cameraX, int cameraY,
                                int cornerX, int cornerY,
                                int width, int height,
                                int spacing, Color color, int stroke) {

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
        for (int y = cameraY; y >= cornerY; y -= spacing) {
            g2.drawLine(cornerX, y, cornerX + width, y);
        }
    }

    /**
     * Draws an arrow from a start point with a given vector direction.
     * Automatically shortens the line to avoid the arrowhead overlapping.
     */
    public static void drawArrow(Graphics2D g2, int x, int y,
                                int dx, int dy,
                                int border, Color color) {

        g2.setColor(color);
        g2.setStroke(new BasicStroke(border));

        final int arrowSize = 12;
        double angle = Math.atan2(dy, dx);

        int endX = x + dx;
        int endY = y + dy;

        if (dx * dx + dy * dy >= arrowSize * arrowSize) {
            // Shorten line so arrowhead doesn't overlap
            int lineEndX = (int) (endX - arrowSize * Math.cos(angle));
            int lineEndY = (int) (endY - arrowSize * Math.sin(angle));

            g2.drawLine(x, y, lineEndX, lineEndY);

            // Arrowhead
            int x1 = (int) (endX - arrowSize * Math.cos(angle - Math.PI / 6));
            int y1 = (int) (endY - arrowSize * Math.sin(angle - Math.PI / 6));

            int x2 = (int) (endX - arrowSize * Math.cos(angle + Math.PI / 6));
            int y2 = (int) (endY - arrowSize * Math.sin(angle + Math.PI / 6));

            Polygon arrowHead = new Polygon();
            arrowHead.addPoint(endX, endY);
            arrowHead.addPoint(x1, y1);
            arrowHead.addPoint(x2, y2);
            g2.fillPolygon(arrowHead);

        } else {
            g2.drawLine(x, y, endX, endY);
        }
    }

    public static void drawImage(Graphics2D g2, String path, int x, int y, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) return;

            // Original image dimensions
            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            // Compute scale ratio (smallest to fit both width and height)
            double scale = Math.min((double) width / imgWidth, (double) height / imgHeight);

            // Scaled dimensions
            int drawWidth = (int) (imgWidth * scale);
            int drawHeight = (int) (imgHeight * scale);

            // Centering offsets
            int offsetX = x + (width - drawWidth) / 2;
            int offsetY = y + (height - drawHeight) / 2;

            // Draw the image
            g2.drawImage(image, offsetX, offsetY, drawWidth, drawHeight, null);
        } catch (IOException e) {
            System.out.println("Error loading image: " + path);
        }
    }


}
