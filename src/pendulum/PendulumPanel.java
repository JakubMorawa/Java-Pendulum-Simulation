package pendulum;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.*;

public final class PendulumPanel extends JPanel {
    // ----------------------------
    // Simulation size & camera
    // ----------------------------
    private static final int CORNER_X = 50;
    private static final int CORNER_Y = 50;

    private static final int SIM_BORDER_STROKE = 5;
    private static final int SIM_SPACING = 50;

    private static final int MAX_SIM_SIZE = 1200;

    private static int SIM_WIDTH = MAX_SIM_SIZE;
    private static int SIM_HEIGHT = MAX_SIM_SIZE;

    private static int simCameraX, simCameraY;
    private static int cameraDiffX = 0, cameraDiffY = 0;
    private static int cameraCenterX, cameraCenterY;

    public static void setSIM_HEIGHT(int SIM_HEIGHT) { PendulumPanel.SIM_HEIGHT = SIM_HEIGHT; }
    public static void setSIM_WIDTH(int SIM_WIDTH) { PendulumPanel.SIM_WIDTH = SIM_WIDTH; }

    // ----------------------------
    // Simulation & state
    // ----------------------------
    private static final int TRAIL_LIMIT = 500;
    private static final double DELTA_TIME = 0.16;
    private static double time = 0;
    public static double getTime() { return time; }

    private boolean isRunning = true;
    private boolean cameraFollow = false;
    private boolean tracing = false;
    private boolean showArrow = false;

    private final Pendulum pendulum = new Pendulum(200, 10, 0, 0, Math.PI / 4, 0);
    private final List<TrailPoint> trail = new ArrayList<>();

    // ----------------------------
    // Buttons & Text Field
    // ----------------------------
    private static final int TEXT_FIELD_X = 1605;
    private static final int TEXT_FIELD_Y = 190;
    private static final int TEXT_FIELD_WIDTH = 150;
    private static final int TEXT_FIELD_HEIGHT = 40;

    private static final int CAM_LABEL_WIDTH = 100;
    private static final int CAM_LABEL_HEIGHT = 40;

    private static final int CAM_FIELD_WIDTH = 80;
    private static final int CAM_FIELD_HEIGHT = 40;

    private static final int CAM_SECTION_X = 1300;
    private static final int CAM_SECTION_Y = 130;

    private static final int CAM_GAP_BETWEEN_LABEL_AND_FIELD = 5;
    private static final int CAM_GAP_BETWEEN_AXES = 30;

    private JTextField cameraXField;
    private JTextField cameraYField;

    private static final int BUTTON_WIDTH = 130;
    private static final int BUTTON_HEIGHT = 60;
    private static final int BUTTON_SPACING = 20;
    private static final int BUTTON_MARGIN_RIGHT = 1300;
    private static final int BUTTON_TOP_Y = 50;
    private static final int BUTTON_BORDER = 3;

    private final List<Button> buttons = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private int buttonSelected = 0;
    private int buttonNameIndexSelected = 0;
    private double userValue = 0;
    private List<DataElement> dataElements = pendulum.getPendulumData();
    private JTextField textField;

    private final List<Consumer<Double>> pendulumSetters = List.of(
        pendulum::setLength,
        pendulum::setMass,
        pendulum::setPivotX,
        pendulum::setPivotY,
        pendulum::setAngle,
        pendulum::setInitialAngle,
        pendulum::setAngularVelocity,
        pendulum::setInitialAngularVelocity
    );

    {
        for (int i = 0; i < dataElements.size(); i++) {
            if (dataElements.get(i).isChangeable()) {
                buttonNameIndexSelected = i;
                break;
            }
        }
    }
    // ----------------------------
    // Graph
    // ----------------------------
    private static final int GRAPH_X = 1900;
    private static final int GRAPH_Y = 250;
    private static final int GRAPH_WIDTH = 600;
    private static final int GRAPH_HEIGHT = GRAPH_WIDTH;
    private static final int GRAPH_STROKE = 2;

    Graph graph = new Graph(GRAPH_X, GRAPH_Y, GRAPH_WIDTH, GRAPH_HEIGHT, GRAPH_STROKE, Colors.GRAPH_BACKGROUND.toColor(), Colors.GRID.toColor(),trail);


    // ----------------------------
    // Mouse & drag state
    // ----------------------------
    private boolean mouseInSimBox = false;
    private boolean mousePressedInSimBox = false;
    private int mouseLastX, mouseLastY;
    private int mouseCurrentX, mouseCurrentY;
    private int cameraOffsetAtPressX, cameraOffsetAtPressY;

    // ----------------------------
    // Constructor
    // ----------------------------
    public PendulumPanel() {
        initCamera();
        setupMouseHandling();
        setupButtons();
        setupTabButtons();
        startMainLoop();
        setupTextField();
        setupCameraTextFields();
    }

    // ----------------------------
    // Text Field
    // ----------------------------
    private void setupTextField() {
        setLayout(null);

        Runnable parseInput = () -> {
            try {
                userValue = Double.parseDouble(textField.getText().trim());
                if (buttonSelected < pendulumSetters.size())
                    pendulumSetters.get(buttonSelected).accept(userValue);
                textField.setBackground(Color.WHITE);
                pendulum.update(0.00000);
                requestFocusInWindow();
            } catch (NumberFormatException ex) {
                textField.setBackground(Colors.ERROR.toColor());
            }
        };

        textField = TextFieldUtils.createNumericTextField(
            TEXT_FIELD_X,
            TEXT_FIELD_Y,
            TEXT_FIELD_WIDTH,
            TEXT_FIELD_HEIGHT,
            new Font("Poppins", Font.BOLD, 16),
            Colors.ACTIVE.toColor(),
            Colors.ERROR.toColor(),
            true, // allow decimals
            val -> {
                userValue = val.doubleValue();
                if (buttonSelected < pendulumSetters.size())
                    pendulumSetters.get(buttonSelected).accept(userValue);
            }
        );

        Button setButton = createButton(
            TEXT_FIELD_X + TEXT_FIELD_WIDTH + 5,
            TEXT_FIELD_Y,
            70,
            TEXT_FIELD_HEIGHT,
            22,
            "SET",
            Colors.SET.toColor(),
            parseInput
        );

        buttons.add(setButton);
        add(textField);
    }

    private void setupCameraTextFields() {
    Font camFont = new Font("Poppins", Font.BOLD, 16);

    int xLabelX = CAM_SECTION_X;
    int xFieldX = xLabelX + CAM_LABEL_WIDTH + CAM_GAP_BETWEEN_LABEL_AND_FIELD;

    int yLabelX = xFieldX + CAM_FIELD_WIDTH + CAM_GAP_BETWEEN_AXES;
    int yFieldX = yLabelX + CAM_LABEL_WIDTH + CAM_GAP_BETWEEN_LABEL_AND_FIELD;

    cameraXField = TextFieldUtils.createNumericTextField(
        xFieldX,
        CAM_SECTION_Y,
        CAM_FIELD_WIDTH,
        CAM_FIELD_HEIGHT,
        camFont,
        Colors.ACTIVE.toColor(),
        Colors.ERROR.toColor(),
        false, // no decimals
        val -> cameraDiffX = val.intValue()
    );

    cameraYField = TextFieldUtils.createNumericTextField(
        yFieldX,
        CAM_SECTION_Y,
        CAM_FIELD_WIDTH,
        CAM_FIELD_HEIGHT,
        camFont,
        Colors.ACTIVE.toColor(),
        Colors.ERROR.toColor(),
        false, // no decimals
        val -> cameraDiffY = val.intValue()
    );

    add(cameraXField);
    add(cameraYField);
}


    // ----------------------------
    // Buttons
    // ----------------------------
    private void setupButtons() {
        int baseX = BUTTON_MARGIN_RIGHT;
        int baseY = BUTTON_TOP_Y;

        // Stop / Start
        createButton(baseX, baseY, BUTTON_WIDTH, BUTTON_HEIGHT, 30,"STOP", Colors.STOP_RED.toColor(), () -> {
            isRunning = !isRunning;
            Button b = buttons.get(0);
            b.setButtonColor(isRunning ? Colors.STOP_RED.toColor() : Colors.GO_GREEN.toColor());
            b.setText(isRunning ? "STOP" : "START");
        });

        // Reset
        createButton(baseX + BUTTON_WIDTH + BUTTON_SPACING, baseY, BUTTON_WIDTH, BUTTON_HEIGHT, 30,"Reset", Colors.RESET.toColor(), () -> {
            pendulum.reset();
            trail.clear();
            time = 0;
            cameraDiffX = 0;
            cameraDiffY = 0;
            if (cameraFollow) follow();
        });

        // Follow
        createButton(baseX + 2 * (BUTTON_WIDTH + BUTTON_SPACING), baseY, BUTTON_WIDTH, BUTTON_HEIGHT, 30,"Follow", Colors.FOLLOW.toColor(), () -> cameraFollow = !cameraFollow);

        // Trace
        createButton(baseX + 3 * (BUTTON_WIDTH + BUTTON_SPACING), baseY, BUTTON_WIDTH, BUTTON_HEIGHT, 30, "Trace", Colors.TRACE.toColor(), () -> tracing = !tracing);

        createButton(baseX + 4 * (BUTTON_WIDTH + BUTTON_SPACING), baseY, BUTTON_WIDTH, BUTTON_HEIGHT, 30, "Arrow", Colors.ARROW_BUTTON.toColor(), () -> showArrow = !showArrow);
    }

    private Button createButton(int x, int y, int w, int h, int fontSize,String text, Color color, Runnable action) {
        Button btn = new Button(x, y, w, h, text, color, 1, BUTTON_BORDER, fontSize);
        btn.setOnClick(action);
        buttons.add(btn);
        return btn;
    }

    private void setupTabButtons() {
        tabButtons.clear();
        tabButtons.addAll(DataSet.createTabButtons(pendulum.getPendulumData()));

        for (int i = 0, changeableIndex = 0; i < tabButtons.size(); i++, changeableIndex++) {
            int elementIndex = 0;
            int ci = 0;
            for (int j = 0; j < dataElements.size(); j++) {
                if (dataElements.get(j).isChangeable()) {
                    if (ci == changeableIndex) { elementIndex = j; break; }
                    ci++;
                }
            }

            int finalElementIndex = elementIndex;
            int finalI = i;
            tabButtons.get(i).setOnClick(() -> {
                buttonSelected = finalI;
                buttonNameIndexSelected = finalElementIndex;
                updateTabButtons();
            });
        }

        updateTabButtons();
    }

    private void updateTabButtons() {
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setButtonColor(buttonSelected != i ? Colors.TAB.toColor() : Colors.TAB_SELECTED.toColor());
        }
    }

    // ----------------------------
    // Mouse handling
    // ----------------------------

    private void setupMouseHandling() {

        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handleMousePress(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseInSimBox = isMouseInSimBox(e.getX(), e.getY());

                for (Button b : buttons) b.handleMouse(e);
                for (Button b : tabButtons) b.handleMouse(e);
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    private void handleMousePress(MouseEvent e) {
        for (Button b : buttons) b.handleMouse(e);
        for (Button b : tabButtons) b.handleMouse(e);

        if (isMouseInSimBox(e.getX(), e.getY())) {
            mousePressedInSimBox = true;
            cameraOffsetAtPressX = cameraDiffX;
            cameraOffsetAtPressY = cameraDiffY;
            mouseLastX = e.getX();
            mouseLastY = e.getY();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        mousePressedInSimBox = false;
        setCursor(Cursor.getDefaultCursor());
        for (Button b : buttons) b.handleMouse(e);
        for (Button b : tabButtons) b.handleMouse(e);
    }

    private void handleMouseDrag(MouseEvent e) {
        updateMousePosition(e);
        if (mousePressedInSimBox && mouseInSimBox) calculateCameraDiff();
    }

    private boolean isMouseInSimBox(int x, int y) {
        return x >= CORNER_X && x <= CORNER_X + SIM_WIDTH && y >= CORNER_Y && y <= CORNER_Y + SIM_HEIGHT;
    }

    private void updateMousePosition(MouseEvent e) { mouseCurrentX = e.getX(); mouseCurrentY = e.getY(); }

    private void calculateCameraDiff() { cameraDiffX = mouseCurrentX - mouseLastX + cameraOffsetAtPressX; cameraDiffY = mouseCurrentY - mouseLastY + cameraOffsetAtPressY; }

    // ----------------------------
    // Simulation camera
    // ----------------------------
    private void initCamera() {
        cameraCenterX = CORNER_X + SIM_WIDTH / 2;
        cameraCenterY = CORNER_Y + SIM_HEIGHT / 2;
        cameraDiffX = 0;
        cameraDiffY = 0;
    }

    private void follow() {
        simCameraX = -pendulum.getBobX() + SIM_WIDTH / 2 + CORNER_X;
        simCameraY = -pendulum.getBobY() + SIM_HEIGHT / 2 + CORNER_Y;
    }

    // ----------------------------
    // Main loop
    // ----------------------------
    private void startMainLoop() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> { updateSimulation(); repaint(); }), 0, 16, TimeUnit.MILLISECONDS);
    }

    private void updateSimulation() {
        if (isRunning){
            pendulum.update(DELTA_TIME);
            time += DELTA_TIME;

            trail.add(new TrailPoint(pendulum.getBobX(), pendulum.getBobY(), time));
            if (trail.size() > TRAIL_LIMIT) trail.remove(0);
        }

        if (cameraFollow) follow();
        else { simCameraX = cameraCenterX + cameraDiffX; simCameraY = cameraCenterY + cameraDiffY; }
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        drawBackground(g2);
        drawVignette(g2);

        drawSimulation(g2);

        drawButtons(g2);
        drawDataSet(g);

        drawBoxWithText(g2,dataElements.get(buttonNameIndexSelected).getVariableName(),1300, TEXT_FIELD_Y, 300, 40,22); //TextFieldName

        drawCameraLabels(g2);
        
        drawGraph(g2, graph);

        
    }
    
    private void drawDataSet(Graphics g) {
        DataSet.drawDataSet(g, pendulum.getPendulumData());
    }
    private void drawArrow(Graphics2D g2) {
        Utils.drawArrow(g2, pendulum.getBobX()+simCameraX, pendulum.getBobY()+simCameraY, (int)pendulum.getVelocityX(), -(int)pendulum.getVelocityY(), 3, Colors.ARROW.toColor());
    }
    
    private void drawCameraLabels(Graphics2D g2) {
        int xLabelX = CAM_SECTION_X;
        int xLabelY = CAM_SECTION_Y;
        
        int yLabelX = xLabelX + CAM_LABEL_WIDTH + CAM_FIELD_WIDTH + CAM_GAP_BETWEEN_LABEL_AND_FIELD + CAM_GAP_BETWEEN_AXES;
        int yLabelY = CAM_SECTION_Y;
        
        drawBoxWithText(g2, "X: " + cameraDiffX, xLabelX, xLabelY, CAM_LABEL_WIDTH, CAM_LABEL_HEIGHT,16);
        drawBoxWithText(g2, "Y: " + cameraDiffY, yLabelX, yLabelY, CAM_LABEL_WIDTH, CAM_LABEL_HEIGHT,16);
    }
    
    private void drawBackground(Graphics2D g2) { g2.setColor(new Color(200, 30, 100)); g2.fillRect(0,0,getWidth(),getHeight()); }
    
    private void drawVignette(Graphics2D g2) { Utils.drawVignette(g2,getWidth(),getHeight(),new Color(0,0,0,0),new Color(0,0,0,150)); }
    
    private void drawSimulation(Graphics2D g2) {

        // Draw background & clip to simulation box
        Utils.drawSquare(g2, CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT, Color.WHITE, SIM_BORDER_STROKE);
        Shape oldClip = g2.getClip();
        g2.setClip(CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT);

        // Draw zoomed & panned grid
        Utils.drawGrid(g2, simCameraX, simCameraY, CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT, SIM_SPACING, Colors.GRID.toColor(), 1);

        // Draw axes (world coordinates)
        g2.setColor(Colors.AXES.toColor());
        g2.drawLine(0, simCameraY, CORNER_X + SIM_WIDTH, simCameraY);
        g2.drawLine(simCameraX, 0, simCameraX, CORNER_Y + SIM_HEIGHT);

        // Draw trail, arrow, and pendulum in world coordinates
        if (tracing) drawTrail(g2);
        if (showArrow) drawArrow(g2);
        pendulum.draw(g2, simCameraX, simCameraY);


        g2.setClip(oldClip);
    }

    private void drawTrail(Graphics2D g2) {
        if(trail.size()<2) return;
        for(int i=0;i<trail.size()-1;i++){
            TrailPoint p1=trail.get(i), p2=trail.get(i+1);
            double avgTime=(p1.getTime()+p2.getTime())/2;
            g2.setColor(Color.getHSBColor((float)(avgTime%10/10.0),1.0f,1.0f));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(p1.getX()+simCameraX,p1.getY()+simCameraY,p2.getX()+simCameraX,p2.getY()+simCameraY);
        }
    }

    private void drawGraph(Graphics2D g2, Graph graph) {graph.draw(g2);}

    private void drawButtons(Graphics2D g2) { for(Button b: buttons) b.draw(g2); for(Button b: tabButtons) b.draw(g2); }

    private void drawBoxWithText(Graphics2D g2,String text,int x,int y,int w,int h, int fontSize){
        Utils.drawBox(g2,x,y,w,h,Color.white);
        Utils.drawTextInBox(g2,text,x,y,w,h,fontSize);
    }

}
