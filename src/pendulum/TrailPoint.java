package pendulum;

import java.awt.*;

public class TrailPoint {

    private final int x;
    private final int y;
    private final double time;

    public TrailPoint(int x, int y, double time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    public void draw(Graphics g, int offsetX, int offsetY) {
        Graphics2D g2 = (Graphics2D) g;

        // Convert time to color (example: cycling RGB based on time)
        Color color = timeToColor(time);
        g2.setColor(color);

        final int radius = 2; // size of the point
        g2.fillOval(x + offsetX - radius, y + offsetY - radius, radius * 2, radius * 2);
    }

    // ----------------------------
    // Helper: convert time to RGB
    // ----------------------------
    private Color timeToColor(double time) {
        // Simple example: cycle through hues over time
        float hue = (float) ((time % 10) / 10.0); // cycles every 10 seconds
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    // ----------------------------
    // Getters
    // ----------------------------
    public int getX() { return x; }
    public int getY() { return y; }
    public double getTime() { return time; }
}