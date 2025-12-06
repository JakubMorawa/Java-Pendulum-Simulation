package pendulum;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public class Button extends JComponent {

    // ----------------------------
    // Fields
    // ----------------------------
    private int x, y, width, height;
    private int borderWidth;
    private int fontSize;
    private int padding = 10;

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

    private String imgPath;

    // ----------------------------
    // Constructor
    // ----------------------------
    public Button(int x, int y, int width, int height, String text, Color buttonColor, int buttonType, int borderWidth, int fontSize) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.buttonColor = buttonColor;
        this.buttonType = buttonType;
        this.borderWidth = borderWidth;
        this.fontSize = fontSize;
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

    private void onClick() {if (onClickAction != null) onClickAction.run();}

    public void setOnClick(Runnable action) {this.onClickAction = action;}

    @Override
    public boolean contains(int mx, int my) {return mx >= x && mx <= x + width && my >= y && my <= y + height;}

    // ----------------------------
    // Drawing
    // ----------------------------
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(borderWidth));

        Color displayColor = buttonColor;
        if (pressed) displayColor = Utils.richDarken(buttonColor, 0.2f);
        else if (hovered) displayColor = Utils.lighten(buttonColor, 0.2);

        // Draw the appropriate shape based on type
        switch (buttonType) {
            case TYPE_ROUND -> Utils.drawRoundSquare(g2, x, y, width, height, displayColor,  0.5f, 0.3);
            case TYPE_CIRCLE -> Utils.drawCircle(g2, x, y, width, height, displayColor, Color.black, 1);
            default -> Utils.drawSquare(g2, x, y, width, height, displayColor, borderWidth);
        }

        // Draw text
        Font font = new Font("Poppins", Font.BOLD, fontSize);
        int textX = x + (width - g2.getFontMetrics(font).stringWidth(text)) / 2;
        int textY = y + height / 2 + fontSize/2 -3;
        Utils.drawTextWithBorder(g2, text, textX, textY, font, Color.WHITE, Color.BLACK, 2);

        if(imgPath != null) {
            Utils.drawImage(g2, imgPath, x+padding, y+padding, width-2*padding, height-2*padding);
        }
    }

    // ----------------------------
    // Setters
    // ----------------------------

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setButtonColor(Color color) { this.buttonColor = color; }
    public void setText(String text) { this.text = text; }
    public void setImg(String imgPath) { this.imgPath = imgPath; }
}