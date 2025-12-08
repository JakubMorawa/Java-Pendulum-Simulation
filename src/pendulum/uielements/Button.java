package pendulum.uielements;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class Button extends JComponent implements UIElement{

    // ----------------------------
    // Fields
    // ----------------------------
    private int x, y, width, height;
    private final int borderWidth;
    private final int fontSize;
    private final int padding = 8;
    
    private String text;
    private Color buttonColor;

    private boolean hovered = false;
    private boolean pressed = false;

    private double timeSpentHovered = 0;
    private boolean openPopup = false;
    private final double popupDelay = 0.5;
    private final double popupFadeInTime = 0.2;
    private final int popupalphaMax = 150;

    private Runnable onClickAction;

    // Shape type constants (easy to expand later)
    public static final int TYPE_SQUARE = 0;
    public static final int TYPE_ROUND = 1;
    public static final int TYPE_CIRCLE = 2;

    int mx = 0;
    int my = 0;

    private final int buttonType;

    private String imgPath;
    private Image image;

    private int offsetX = 0;
    private int offsetY = 0;

    // ----------------------------
    // Constructors
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
        this.imgPath = null;
        loadImage();
    }

    // ----------------------------
    // Mouse Handling
    // ----------------------------
    public void handleMouse(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
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
    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(borderWidth));

        Color displayColor = buttonColor;
        if (pressed) displayColor = Utils.richDarken(buttonColor, 0.2f);
        else if (hovered){
            displayColor = Utils.lighten(buttonColor, 0.2);
            timeSpentHovered += 1.0/60.0;
            openPopup = timeSpentHovered > popupDelay;
        } 
        else {
            timeSpentHovered = 0;
            openPopup = false;
        }

        // Draw the appropriate shape based on type
        switch (buttonType) {
            case TYPE_ROUND -> Utils.drawRoundSquare(g2, x, y, width, height, displayColor,  0.5f, 0.3);
            case TYPE_CIRCLE -> Utils.drawCircle(g2, x, y, width, height, displayColor, Color.black, 1);
            default -> Utils.drawSquare(g2, x, y, width, height, displayColor, borderWidth);
        }

        if(imgPath != null) {
            drawImage(g2);
        }

    }

    public void drawPopUp(Graphics2D g2) {
        if (!openPopup) return;
            // Popup background
        Font popupFont = new Font("Poppins", Font.PLAIN, 14);
        String popupText = text;
        
        int popupWidth = g2.getFontMetrics(popupFont).stringWidth(popupText) + 20;
        int popupHeight = 30;
        int spacing = 10;
        int popupX = mx + spacing;
        int popupY = my + spacing;
        g2.setColor(new Color(0,0,0,timeSpentHovered > (popupDelay+popupFadeInTime) ? popupalphaMax : 
        (int)((timeSpentHovered-popupDelay)/popupFadeInTime*popupalphaMax)));
        g2.fillRoundRect(popupX, popupY, popupWidth, popupHeight, 3, 3);

        // Popup text
        // You can customize this as needed
        int popupTextX = popupX + (popupWidth - g2.getFontMetrics(popupFont).stringWidth(popupText)) / 2;
        int popupTextY = popupY + ((popupHeight + 10) / 2);
        Utils.drawTextWithBorder(g2, popupText, popupTextX, popupTextY, popupFont, Color.WHITE, Color.BLACK, 1);
        
    }

    private void loadImage() {
        if (imgPath == null) return;

        try {
            image = ImageIO.read(new File(imgPath));
        } catch (IOException e) {
            System.out.println("Error loading button image: " + imgPath);
        }
    }

    public void drawImage(Graphics2D g2) {
        if (image == null) return;

        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        int pad = padding;

        double scale = Math.min(
            (double) (width - 2 * pad) / imgWidth,
            (double) (height - 2 * pad) / imgHeight
        );

        int drawWidth = (int)(imgWidth * scale);
        int drawHeight = (int)(imgHeight * scale);

        int offsetX = x + pad + (width - 2*pad - drawWidth) / 2;
        int offsetY = y + pad + (height - 2*pad - drawHeight) / 2;

        g2.drawImage(image, offsetX, offsetY, drawWidth, drawHeight, null);
    }

    // ----------------------------
    // Setters
    // ----------------------------
    public void setButtonColor(Color color) { this.buttonColor = color; }
    public void setText(String text) { this.text = text; }
    public void setImg(String imgPath) { this.imgPath = imgPath; loadImage();}

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    @Override
    public int getPreferredWidth() {
        return width;
    }

    @Override
    public int getPreferredHeight() {
        return height;
    }
    
    @Override
    public int getOffsetX() {
        return offsetX;
    }

    @Override
    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}