package pendulum;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public class Button extends JComponent {

    // ----------------------------
    // Fields
    // ----------------------------
    private final int x, y, width, height;
    private static final int BORDER_WIDTH = 3;

    private String text;
    private Color buttonColor;

    private boolean hovered = false;
    private boolean pressed = false;

    private Runnable onClickAction;

    // Shape type constants (easy to expand later)
    public static final int TYPE_SQUARE = 0;
    public static final int TYPE_ROUND = 1;
    public static final int TYPE_CIRCLE = 2;

    private final int buttonType;

    // ----------------------------
    // Constructor
    // ----------------------------
    public Button(int x, int y, int width, int height, String text, Color buttonColor, int buttonType) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.buttonColor = buttonColor;
        this.buttonType = buttonType;
    }

    // ----------------------------
    // Mouse Handling
    // ----------------------------
    public void handleMouse(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        boolean inside = contains(mx, my);

        switch (e.getID()) {
            case MouseEvent.MOUSE_MOVED -> hovered = inside;
            case MouseEvent.MOUSE_PRESSED -> pressed = inside;
            case MouseEvent.MOUSE_RELEASED -> {
                if (pressed && inside) onClick();
                pressed = false;
            }
        }
    }

    private void onClick() {
        if (onClickAction != null) onClickAction.run();
    }

    public void setOnClick(Runnable action) {
        this.onClickAction = action;
    }

    @Override
    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(BORDER_WIDTH));

        Color displayColor = buttonColor;
        if (pressed) displayColor = darken(buttonColor, 0.2);
        else if (hovered) displayColor = lighten(buttonColor, 0.2);

        // Draw the appropriate shape based on type
        switch (buttonType) {
            case TYPE_ROUND -> drawRoundSquare(g2, x, y, width, height, displayColor);
            case TYPE_CIRCLE -> drawCircle(g2, x, y, width, height, displayColor);
            default -> drawSquare(g2, x, y, width, height, displayColor);
        }

        // Draw text
        Font font = new Font("Poppins", Font.BOLD, 30);
        int textX = x + (width - g2.getFontMetrics(font).stringWidth(text)) / 2;
        int textY = y + height / 2 + 10;

        drawTextWithBorder(g2, text, textX, textY, font, Color.WHITE, Color.BLACK, 2);
    }

    // ----------------------------
    // Shape Drawing
    // ----------------------------
    public static void drawRoundSquare(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(x, y, width, height, 25, 25);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, width, height, 25, 25);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public static void drawSquare(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setColor(color);
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }

    public static void drawCircle(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, width, height);
        g2.setColor(Color.BLACK);
        g2.drawOval(x, y, width, height);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    // ----------------------------
    // Text Rendering
    // ----------------------------
    public static void drawTextWithBorder(Graphics2D g2, String text, int x, int y, Font font, Color textColor, Color borderColor, int thickness) {
        g2.setFont(font);
        g2.setColor(borderColor);

        for (int i = -thickness; i <= thickness; i++) {
            for (int j = -thickness; j <= thickness; j++) {
                if (i * i + j * j <= thickness * thickness) g2.drawString(text, x + i, y + j);
            }
        }

        g2.setColor(textColor);
        g2.drawString(text, x, y);
    }

    // ----------------------------
    // Color Utilities
    // ----------------------------
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

    // ----------------------------
    // Setters
    // ----------------------------
    public void setButtonColor(Color color) { this.buttonColor = color; }
    public void setText(String text) { this.text = text; }
}