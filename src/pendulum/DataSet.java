package pendulum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataSet {

    // ----------------------------
    // Layout constants
    // ----------------------------
    private static final int CORNER_X = 1300;
    private static final int CORNER_Y = 250;
    private static final int BOX_WIDTH_NAME = 300;
    private static final int BOX_WIDTH_VALUE = 200;
    private static final int BOX_HEIGHT = 40;
    private static final int BORDER_WIDTH = 2;
    private static final int GAP = BOX_HEIGHT;
    private static final int TAB_WIDTH = 30;

    // ----------------------------
    // Drawing method
    // ----------------------------
    public static void drawDataSet(Graphics g, List<DataElement> elements) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        Font font = new Font("Poppins", Font.BOLD, 23);
        g2.setFont(font);

        for (int i = 0; i < elements.size(); i++) {
            DataElement e = elements.get(i); // e is the variable being drawn
            int y = CORNER_Y + GAP * i;
            int extraRoom = 0;

            // Name Box
            Color boxColor = e.isChangeable() ? Colors.CHANGEABLE.toColor() : Colors.UNCHANGEABLE.toColor();
            Utils.drawBox(g2, CORNER_X, y, BOX_WIDTH_NAME, BOX_HEIGHT, boxColor);
            Utils.drawTextInBox(g2, e.getVariableName(), CORNER_X, y, BOX_WIDTH_NAME, BOX_HEIGHT);

            //Value Box
            if(!e.isChangeable()) extraRoom = TAB_WIDTH + 1;// setting extra space if button doesn't exist
            Utils.drawBox(g2, CORNER_X + BOX_WIDTH_NAME, y, BOX_WIDTH_VALUE + extraRoom, BOX_HEIGHT, boxColor);
            Utils.drawTextInBox(g2, String.format("%.2f", e.getValue()), CORNER_X + BOX_WIDTH_NAME, y, BOX_WIDTH_VALUE + extraRoom, BOX_HEIGHT);
        }
    }

    // ----------------------------
    // Set Tab Buttons
    // ----------------------------
    public static List<Button> createTabButtons(List<DataElement> elements){
        final List<Button> tabButtons = new ArrayList<>();
        int TabX = CORNER_X + BOX_WIDTH_NAME + BOX_WIDTH_VALUE;
        int TabY = CORNER_Y;
        for (int i = 0; i < elements.size(); i++) {
            DataElement e = elements.get(i);
            if(e.isChangeable()){
                tabButtons.add(new Button(TabX, TabY + BOX_HEIGHT*i +1, TAB_WIDTH, BOX_HEIGHT-2, "", Colors.TAB.toColor(),0,2, 30));
                
            }
        }
        return tabButtons;
    }
}