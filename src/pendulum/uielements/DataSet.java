package pendulum.uielements;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import pendulum.DataElement;

public class DataSet {

    // ----------------------------
    // Layout constants
    // ----------------------------
    private static final int CORNER_X = 1300;
    private static final int CORNER_Y = 350;
    private static int BOX_WIDTH_NAME = 0;
    private static final int BOX_WIDTH_VALUE = 200;
    private static final int BOX_HEIGHT = 40;
    private static final int BORDER_WIDTH = 2;
    private static final int GAP = BOX_HEIGHT;
    private static final int TAB_WIDTH = 30;
    private static final Font FONT = new Font("Poppins", Font.BOLD, 22);

    // ----------------------------
    // Drawing method
    // ----------------------------
    public static void drawDataSet(Graphics2D g2, List<DataElement> elements) {
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        Font font = new Font("Poppins", Font.BOLD, 22);
        g2.setFont(font);

        for (int i = 0; i < elements.size(); i++) {
            DataElement e = elements.get(i); // e is the variable being drawn
            int y = CORNER_Y + GAP * i;
            int extraRoom = 0;

            // Name Box
            Color boxColor = e.isChangeable() ? UIColors.CHANGEABLE.toColor() : UIColors.UNCHANGEABLE.toColor();
            Utils.drawBox(g2, CORNER_X, y, BOX_WIDTH_NAME, BOX_HEIGHT, boxColor);
            Utils.drawTextInBox(g2, e.getVariableName(), CORNER_X, y, BOX_WIDTH_NAME, BOX_HEIGHT,22);

            //Value Box
            if(!e.isChangeable()) extraRoom = TAB_WIDTH + 1;// setting extra space if button doesn't exist
            Utils.drawBox(g2, CORNER_X + BOX_WIDTH_NAME, y, BOX_WIDTH_VALUE + extraRoom, BOX_HEIGHT, boxColor);
            Utils.drawTextInBox(g2, String.format("%.2f", e.getValue()), CORNER_X + BOX_WIDTH_NAME, y, BOX_WIDTH_VALUE + extraRoom, BOX_HEIGHT,22);
        }
    }

    private static void findLargestWidth(DataElement e) {
        //temp graphics to measure text width
        BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = temp.createGraphics();
        g2.getFontMetrics(FONT).stringWidth(e.getVariableName());
        if (g2.getFontMetrics(FONT).stringWidth(e.getVariableName()) + 10 > BOX_WIDTH_NAME) {
            BOX_WIDTH_NAME = g2.getFontMetrics(FONT).stringWidth(e.getVariableName()) + 10;
        }
        g2.dispose();
    }

    // ----------------------------
    // Set Tab Buttons
    // ----------------------------
    public static List<Button> createTabButtons(List<DataElement> elements){
        for (int i = 0; i < elements.size(); i++) {
            findLargestWidth(elements.get(i));
        }
        final List<Button> tabButtons = new ArrayList<>();
        int TabX = CORNER_X + BOX_WIDTH_NAME + BOX_WIDTH_VALUE;
        int TabY = CORNER_Y;
        for (int i = 0; i < elements.size(); i++) {
            DataElement e = elements.get(i);
            if(e.isChangeable()){
                tabButtons.add(new Button(TabX, TabY + BOX_HEIGHT*i +1, TAB_WIDTH, BOX_HEIGHT-2, "", UIColors.TAB.toColor(),0,2, 22));
                
            }
        }
        return tabButtons;
    }
}