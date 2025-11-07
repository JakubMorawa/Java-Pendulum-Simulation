package pendulum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataSet {

    // ----------------------------
    // Layout constants
    // ----------------------------
    private static final int CORNER_X = 1300;
    private static final int CORNER_Y = 200;
    private static final int BOX_WIDTH_NAME = 300;
    private static final int BOX_WIDTH_VALUE = 200;
    private static final int BOX_HEIGHT = 40;
    private static final float BORDER_WIDTH = 2;
    private static final int GAP = BOX_HEIGHT;

    // ----------------------------
    // Drawing method
    // ----------------------------
    public static void drawDataSet(Graphics g, List<DataElement> elements) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        Font font = new Font("Poppins", Font.BOLD, 23);
        g2.setFont(font);

        for (int i = 0; i < elements.size(); i++) {
            DataElement e = elements.get(i);
            boolean changeable = e.isChangeable();
            Color boxColor = changeable ? Color.WHITE : new Color(255, 180, 180); // light red for non-changeable

            int y = CORNER_Y + GAP * i;

            // Draw name and value boxes
            drawBox(g2, CORNER_X, y, BOX_WIDTH_NAME, BOX_HEIGHT, boxColor);
            drawBox(g2, CORNER_X + BOX_WIDTH_NAME, y, BOX_WIDTH_VALUE, BOX_HEIGHT, boxColor);

            // Draw text inside boxes
            drawTextInBox(g2, e.getVariableName(), CORNER_X, y, BOX_WIDTH_NAME, BOX_HEIGHT);
            drawTextInBox(g2, String.format("%.2f", e.getValue()), CORNER_X + BOX_WIDTH_NAME, y, BOX_WIDTH_VALUE, BOX_HEIGHT);
        }
    }

    // ----------------------------
    // Helper: draw single box
    // ----------------------------
    private static void drawBox(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setColor(color);
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }

    // ----------------------------
    // Helper: draw text inside a box with clipping
    // ----------------------------
    private static void drawTextInBox(Graphics2D g2, String text, int x, int y, int width, int height) {
        Shape oldClip = g2.getClip();
        g2.setClip(x, y, width, height);

        FontMetrics metrics = g2.getFontMetrics();
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent(); // vertical centering
        g2.drawString(text, x + 5, textY);

        g2.setClip(oldClip);
    }

    // ----------------------------
    // Set Tab Buttons
    // ----------------------------
    public static List<Button> createTabButtons(List<DataElement> elements){
        final List<Button> tabButtons = new ArrayList<>();
        int TabX = CORNER_X + BOX_WIDTH_NAME + BOX_WIDTH_VALUE;
        int TabY = CORNER_Y;
        int tabWidth = 30;
        for (int i = 0; i < elements.size(); i++) {
            tabButtons.add(new Button(TabX, TabY + BOX_HEIGHT*i, tabWidth, BOX_HEIGHT, "", Colors.TAB.toColor(),0));
        }
        return tabButtons;
    }
}