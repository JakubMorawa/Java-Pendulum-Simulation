package pendulum;

import java.awt.*;
import java.util.List;

public class Graph {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Color graphColor;
    private final int borderWidth;
    private final Color gridColor;
    private final int spacing = 10;
    private final List<TrailPoint> trail;
    @SuppressWarnings("FieldMayBeFinal")
    private int cameraX;
    @SuppressWarnings("FieldMayBeFinal")
    private int cameraY;
    @SuppressWarnings("FieldMayBeFinal")
    private int originX;
    @SuppressWarnings("FieldMayBeFinal")
    private int originY;

    public Graph(int x, int y, int width, int height, int borderWidth,
                 Color graphColor, Color gridColor, List<TrailPoint> trail) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.graphColor = graphColor;
        this.borderWidth = borderWidth;
        this.gridColor = gridColor;
        this.trail = trail;

        cameraX = 0;
        cameraY = width/2;

        originX = x + cameraX;
        originY = y + cameraY;
    }

    public void draw(Graphics2D g2) {

        Utils.drawSquare(g2, x, y, width, height, graphColor, borderWidth);

        Shape oldClip = g2.getClip();
        g2.setClip(x, y, width, height);

        Utils.drawGrid(g2, cameraX, cameraY, x, y, width, height, spacing, gridColor, 1);

        // draw axis lines
        g2.setColor(Colors.AXES.toColor());
        g2.drawLine(x, originY, x + width, originY);  // horizontal
        g2.drawLine(originX, y, originX, y + height); // vertical

        drawTrail(g2);

        g2.setClip(oldClip);
    }

    private void drawTrail(Graphics2D g2) {
        if (trail == null || trail.size() < 2) return;

        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);

        // Scale factor to convert time -> pixels
        final int timeScale = 5;

        // Compute x position of latest point in pixel space
        double lastTimeX = trail.get(trail.size() - 1).getTime() * timeScale;

        // Shift camera so the latest point stays visible
        if (lastTimeX - cameraX > width - 100) {   // 100px margin
            cameraX = (int)(lastTimeX - width + 100);
        }

        for (int i = 0; i < trail.size() - 1; i++) {
            TrailPoint p1 = trail.get(i);
            TrailPoint p2 = trail.get(i + 1);

            int x1 = (int)(p1.getTime() * timeScale) - cameraX + originX;
            int x2 = (int)(p2.getTime() * timeScale) - cameraX + originX;

            int y1 = originY - p1.getX(); // flip vertically if needed
            int y2 = originY - p2.getX();

            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
