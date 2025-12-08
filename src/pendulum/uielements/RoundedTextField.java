package pendulum.uielements;

import java.awt.*;
import javax.swing.JTextField;

public class RoundedTextField extends JTextField implements UIElement {
    private final int radius;
    private Color borderColor = Color.GRAY;
    private int offsetX = 0;
    private int offsetY = 0;

    public RoundedTextField(int radius) {
        super();
        this.radius = radius;
        setOpaque(false);
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }

    // ---------------- UIElement methods ----------------
    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    @Override
    public int getPreferredHeight() {
        return getHeight();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
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
