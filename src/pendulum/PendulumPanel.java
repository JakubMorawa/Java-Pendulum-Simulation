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

    // Simulation size & camera
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

    private double zoom = 1.0;
    private static final double ZOOM_MIN = 0.4;
    private static final double ZOOM_MAX = 6.0;

    public static void setSIM_HEIGHT(int SIM_HEIGHT) { PendulumPanel.SIM_HEIGHT = SIM_HEIGHT; }
    public static void setSIM_WIDTH(int SIM_WIDTH) { PendulumPanel.SIM_WIDTH = SIM_WIDTH; }

    // Simulation state
    private static final int TRAIL_LIMIT = 5000;
    private static final double DELTA_TIME = 0.16;
    private static double time = 0;
    public static double getTime() { return time; }

    private boolean isRunning = true;
    private boolean cameraFollow = false;
    private boolean tracing = false;
    private boolean showArrow = false;


    // Pendulums
    private int pendulumObserved = 1;
    private final Pendulum pendulum1 = new Pendulum(200, 10, 0, 0, Math.PI / 4, 0.1);
    private final Pendulum pendulum2 = new Pendulum(200, 10, 0, 0, Math.PI / 4, -0.1);
    private final Pendulum pendulum3 = new Pendulum(200, 10, 0, 0, Math.PI / 4, 0.2);
    private final Pendulum pendulum4 = new Pendulum(100, 10, 0, 0, Math.PI / 4, -0.2);
    private final List<Pendulum> pendulums = new ArrayList<>();

    private void setupPendulums() {
        pendulums.add(pendulum1);
        pendulums.add(pendulum2);
        pendulums.add(pendulum3);
        pendulums.add(pendulum4);
    }
    private final List<TrailPoint> trail = new ArrayList<>();

    // GUI Elements
    private JTextField textField;
    private JTextField cameraXField;
    private JTextField cameraYField;

    private Section topSection = new Section(2000, 20, 800, 100, 30, 3, UIColors.BACKGROUND.toColor(), Utils.richDarken(UIColors.BACKGROUND.toColor(), 0.4f));
    private Section[] sections = {
        topSection
    };

    private final List<Button> buttons = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private int buttonSelected = 0;
    private int buttonNameIndexSelected = 0;
    private double userValue = 0;

    private List<DataElement> dataElements = pendulum1.getPendulumData();
    private List<Label> labels = new ArrayList<>();

    private List<Consumer<Double>> pendulumSetters = List.of(
            pendulum1::setLength,
            pendulum1::setMass,
            pendulum1::setPivotX,
            pendulum1::setPivotY,
            pendulum1::setAngle,
            pendulum1::setInitialAngle,
            pendulum1::setAngularVelocity,
            pendulum1::setInitialAngularVelocity
    );

    // Camera label positions
    private final int xLabelX = 1300;
    private final int xLabelY = 130;
    private final int yLabelX = xLabelX + 100 + 80 + 5 + 30;//magic number with 215
    private final int yLabelY = xLabelY;

    // Graph
    private static final int GRAPH_X = 1900;
    private static final int GRAPH_Y = 250;
    private static final int GRAPH_WIDTH = 600;
    private static final int GRAPH_HEIGHT = GRAPH_WIDTH;
    private static final int GRAPH_STROKE = 2;

    private Graph graph = new Graph(
            GRAPH_X, GRAPH_Y, GRAPH_WIDTH, GRAPH_HEIGHT, GRAPH_STROKE,
            UIColors.GRAPH_BACKGROUND.toColor(), UIColors.GRID.toColor(), trail
    );

    // Mouse & drag state
    private boolean mouseInSimBox = false;
    private boolean mousePressedInSimBox = false;
    private int mouseLastX, mouseLastY;
    private int mouseCurrentX, mouseCurrentY;
    private int cameraOffsetAtPressX, cameraOffsetAtPressY;

    public PendulumPanel() {
        setupCamera();
        setupPendulums();

        setupMouseHandling();
        setupButtons();
        setupTabButtons();

        setupLabels();
        updateLabelText();

        startMainLoop();
        setupTextField();
        setupCameraTextFields();

        // Initialize first changeable data element
        for (int i = 0; i < dataElements.size(); i++) {
            if (dataElements.get(i).isChangeable()) {
                buttonNameIndexSelected = i;
                break;
            }
        }
    }

    
    // ----------------------------
    // Text Fields
    // ----------------------------
    private void setupTextField() {
        setLayout(null);

        Runnable parseInput = () -> {
            try {
                userValue = Double.parseDouble(textField.getText().trim());
                if (buttonSelected < pendulumSetters.size())
                    pendulumSetters.get(buttonSelected).accept(userValue);
                textField.setBackground(Color.WHITE);
                pendulum1.update(0.00000);//chnage it only for a little bit
                requestFocusInWindow();
            } catch (NumberFormatException ex) {
                textField.setBackground(UIColors.ERROR.toColor());
            }
        };

        textField = TextFieldUtils.createNumericTextField(
                1605, 190, 150, 40,
                new Font("Poppins", Font.BOLD, 16),
                UIColors.ACTIVE.toColor(),
                UIColors.ERROR.toColor(),
                true,
                val -> {
                    userValue = val.doubleValue();
                    if (buttonSelected < pendulumSetters.size())
                        pendulumSetters.get(buttonSelected).accept(userValue);
                }
        );

        Button setButton = createButton(1605 + 150 + 5, 190, 70, 40, 22, "SET", UIColors.SET.toColor(), 0, parseInput);
        buttons.add(setButton);
        add(textField);
    }

    private void setupCameraTextFields() {
        Font camFont = new Font("Poppins", Font.BOLD, 16);

        int xFieldX = xLabelX + 100 + 5;
        int yFieldX = yLabelX + 100 + 5;

        cameraXField = TextFieldUtils.createNumericTextField(
                xFieldX, xLabelY, 80, 40, camFont, UIColors.ACTIVE.toColor(), UIColors.ERROR.toColor(),
                false, val -> cameraDiffX = val.intValue()
        );

        cameraYField = TextFieldUtils.createNumericTextField(
                yFieldX, yLabelY, 80, 40, camFont, UIColors.ACTIVE.toColor(), UIColors.ERROR.toColor(),
                false, val -> cameraDiffY = val.intValue()
        );

        add(cameraXField);
        add(cameraYField);
    }

    // ----------------------------
    // Buttons & Labels
    // ----------------------------
    private void setupButtons() {
        int baseX = 1300;
        int baseY = 50;
        int buttonType = Button.TYPE_ROUND;

        createButton(baseX, baseY, 60, 60, 30, "", UIColors.STOP_RED.toColor(), buttonType,() ->  {
            isRunning = !isRunning;
            Button b = buttons.get(0);
            b.setButtonColor(isRunning ? UIColors.STOP_RED.toColor() : UIColors.GO_GREEN.toColor());
            b.setText(isRunning ? "STOP" : "START");

            
        });
        Button b = buttons.get(0);
        b.setImg("src/pendulum/resources/blurredStopIcon.png");

        createButton(baseX + 150 + 20, baseY, 130, 60, 30, "Reset", UIColors.RESET.toColor(),  buttonType, () -> {
            for (int i = 0; i < pendulums.size(); i++) {
                pendulums.get(i).reset();
            } //TODO THIS CODE IS NOT WORKING CORRECTLY PENDULUMS RESET ONE BY ONE
            trail.clear();
            time = 0;
            cameraDiffX = 0;
            cameraDiffY = 0;
            if (cameraFollow) follow();
        });

        createButton(baseX + 2 * (150 + 20), baseY, 130, 60, 30, "Follow", UIColors.FOLLOW.toColor(), buttonType, 
                () -> cameraFollow = !cameraFollow);

        createButton(baseX + 3 * (150 + 20), baseY, 130, 60, 30, "Trace", UIColors.TRACE.toColor(), buttonType, 
                () -> tracing = !tracing);

        createButton(baseX + 4 * (150 + 20), baseY, 130, 60, 30, "Arrow", UIColors.ARROW_BUTTON.toColor(), buttonType, 
                () -> showArrow = !showArrow);


        //pendulum buttons

        createButton(yLabelX + 215 + 100 + 30 + 120 + 10, yLabelY, 60, 40, 20, "Prev", UIColors.PENDULUM_BUTTON.toColor(), 0, 
                () -> {if (pendulumObserved > 1) pendulumObserved--;
                    pendulumSetters = pendulumSetters(pendulumObserved);
                });
        //magic number mess ylabelx + 215 just the original position, go by 100 + 30 to make gap + 120 + 10 to go past label and make gap, then 60 + 10 to make the next gap
        createButton(yLabelX + 215 + 100 + 30 + 120 + 10 + 60 + 10, yLabelY, 60, 40, 20, "Next", UIColors.PENDULUM_BUTTON.toColor(), 0, 
                () ->  {if (pendulumObserved < pendulums.size()) pendulumObserved++;
                    pendulumSetters = pendulumSetters(pendulumObserved);
                });
    }

    private Button createButton(int x, int y, int w, int h, int fontSize, String text, Color color, int buttonType, Runnable action) {
        Button btn = new Button(x, y, w, h, text, color, buttonType, 3, fontSize);
        btn.setOnClick(action);
        buttons.add(btn);
        return btn;
    }

    private void setupTabButtons() {
        tabButtons.clear();
        tabButtons.addAll(DataSet.createTabButtons(pendulum1.getPendulumData()));

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
        for (int i = 0; i < tabButtons.size(); i++)
            tabButtons.get(i).setButtonColor(buttonSelected != i ? UIColors.TAB.toColor() : UIColors.TAB_SELECTED.toColor());
    }

    private void setupLabels() {
        labels.add(new Label( 1300, 190, 300, 40, 22, Color.WHITE));
        labels.add(new Label( xLabelX, xLabelY, 100, 40, 16, Color.WHITE));
        labels.add(new Label( yLabelX, yLabelY, 100, 40, 16, Color.WHITE));
        labels.add(new Label( yLabelX + 215, yLabelY, 100, 40, 16, Color.WHITE));//magic number with 215
        labels.add(new Label( yLabelX + 215 + 100 + 30, yLabelY, 120, 40, 16, Color.WHITE));
    }

    private void updateLabelText() {
        for (int i = 0; i < labels.size(); i++) {
            Label iLabel = labels.get(i);

            String newText = switch (i) {
                case 0 -> dataElements.get(buttonNameIndexSelected).getVariableName();
                case 1 -> "X: " + cameraDiffX;
                case 2 -> "Y: " + cameraDiffY;
                case 3 -> "Zoom: " + (int)(zoom*100);
                case 4 -> "Pendulum: " + pendulumObserved;
                default -> "Label no Text";
            };

            iLabel.setText(newText);
        }
    }

    private List<Consumer<Double>> pendulumSetters(int pendulumObserved){
        Pendulum p = pendulums.get(pendulumObserved-1);
        List<Consumer<Double>> setterList = List.of(
            p::setLength,
            p::setMass,
            p::setPivotX,
            p::setPivotY,
            p::setAngle,
            p::setInitialAngle,
            p::setAngularVelocity,
            p::setInitialAngularVelocity
    );
        return setterList;
    }

    
    // ----------------------------
    // Main loop
    // ----------------------------
    private void startMainLoop() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            updateSimulation();
            repaint();
        }), 0, 16, TimeUnit.MILLISECONDS);
    }
    
    private void updateSimulation() {
        if (isRunning) {
            for(Pendulum p: pendulums){
                p.update(DELTA_TIME);
            }
            time += DELTA_TIME;

            Pendulum p = pendulums.get(pendulumObserved-1);
            trail.add(new TrailPoint(p.getBobX(),p.getBobY(), time)); // gets pendulum at the end of the list
            if (trail.size() > TRAIL_LIMIT) trail.remove(0);
        }

        if (cameraFollow) follow();
        else {
            simCameraX = cameraCenterX + cameraDiffX;
            simCameraY = cameraCenterY + cameraDiffY;
        }
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
        drawDataSet(g2);
        drawLabels(g2);
        drawGraph(g2, graph);
        drawSections(g2);
    }

    private void drawSections(Graphics2D g2) {
        for(Section s: sections){
            s.draw(g2);
        }
    }
    private void drawBackground(Graphics2D g2) { g2.setColor(UIColors.BACKGROUND.toColor()); g2.fillRect(0,0,getWidth(),getHeight()); }
    
    private void drawVignette(Graphics2D g2) {
        Utils.drawVignette(g2, 
        getWidth(), 
        getHeight(), 
        new Color(0,0,0,0), 
        UIColors.VIGNETTE.toColor());
    }
    
    private void drawSimulation(Graphics2D g2) {
        Utils.drawSquare(g2, CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT, Color.WHITE, SIM_BORDER_STROKE);
        Shape oldClip = g2.getClip();
        g2.setClip(CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT);
        
        int spacingZoomed = (int)(SIM_SPACING*zoom);
        Utils.drawGrid(g2, simCameraX, simCameraY, CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT, spacingZoomed, UIColors.GRID.toColor(), 1);
        
        g2.setColor(UIColors.AXES.toColor());
        g2.drawLine(CORNER_X, simCameraY, CORNER_X + SIM_WIDTH, simCameraY);
        g2.drawLine(simCameraX, CORNER_Y, simCameraX, CORNER_Y + SIM_HEIGHT);
        
        if (tracing) drawTrail(g2);
        if (showArrow) drawArrow(g2);
        int LastBobX;
        int LastBobY;

        // Draw pendulum lines and bobs, updated to handle multiple pendulums without overlapping
        for(int i = 0; i < pendulums.size(); i++){
            if(i == 0){
                pendulums.get(i).drawLine(g2, simCameraX, simCameraY, zoom);
            } else {
                LastBobX = (int)(pendulums.get(i-1).getBobX());
                LastBobY = (int)(pendulums.get(i-1).getBobY());
                pendulums.get(i).setPivotX(LastBobX);
                pendulums.get(i).setPivotY(LastBobY);
                pendulums.get(i).drawLine(g2, simCameraX, simCameraY, zoom);
            }
        }
        for(int i = 0; i < pendulums.size(); i++){
            if(i == 0){
                pendulums.get(i).drawBob(g2, simCameraX, simCameraY, zoom);
            } else {
                LastBobX = (int)(pendulums.get(i-1).getBobX());
                LastBobY = (int)(pendulums.get(i-1).getBobY());
                pendulums.get(i).setPivotX(LastBobX);
                pendulums.get(i).setPivotY(LastBobY);
                pendulums.get(i).drawBob(g2, simCameraX, simCameraY, zoom);
            }
        }
        

        g2.setClip(oldClip);
    }
    
    private void drawTrail(Graphics2D g2) {
        if (trail.size() < 2) return;
        for (int i = 0; i < trail.size() - 1; i++) {
            TrailPoint p1 = trail.get(i);
            TrailPoint p2 = trail.get(i + 1);
            double avgTime = (p1.getTime() + p2.getTime()) / 2.0;
            g2.setColor(Color.getHSBColor((float)((avgTime % 10) / 10.0), 1.0f, 1.0f));
            g2.setStroke(new BasicStroke((float)(2 * zoom)));
            int x1 = (int)(p1.getX() * zoom) + simCameraX;
            int y1 = (int)(p1.getY() * zoom) + simCameraY;
            int x2 = (int)(p2.getX() * zoom) + simCameraX;
            int y2 = (int)(p2.getY() * zoom) + simCameraY;
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawArrow(Graphics2D g2) {
        Pendulum p = pendulums.get(pendulumObserved-1);
        Utils.drawArrow(g2, (int)(p.getBobX()*zoom)+simCameraX, (int)(p.getBobY()*zoom)+simCameraY, (int)(p.getVelocityX()*zoom*2), -(int)(p.getVelocityY()*zoom*2), 3, UIColors.ARROW.toColor());//multiply by 2 to make it more visible
    }

    private void drawButtons(Graphics2D g2) { for(Button b: buttons) b.draw(g2); for(Button b : tabButtons) b.draw(g2); }

    private void drawLabels(Graphics2D g2) {
        updateLabelText();
        for(int i = 0; i < labels.size(); i++){
            updateLabelText();
            labels.get(i).draw(g2);
        } 
    }

    private void drawGraph(Graphics2D g2, Graph graph) { graph.draw(g2); }
    
    private void drawDataSet(Graphics g) { DataSet.drawDataSet(g, pendulums.get(pendulumObserved-1).getPendulumData()); }
    
    // ----------------------------
    // Mouse & Camera handling
    // ----------------------------
    
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

    private void setupCamera() {
        cameraCenterX = CORNER_X + SIM_WIDTH / 2;
        cameraCenterY = CORNER_Y + SIM_HEIGHT / 2;
        cameraDiffX = 0;
        cameraDiffY = 0;
    }

    private void follow() {
        simCameraX = -pendulum1.getBobX() + SIM_WIDTH / 2 + CORNER_X;
        simCameraY = -pendulum1.getBobY() + SIM_HEIGHT / 2 + CORNER_Y;
    }


    private void setupMouseHandling() {
        MouseAdapter mouseHandler = new MouseAdapter() {
    
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) handleMousePress(e);
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
    
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!isMouseInSimBox(e.getX(), e.getY())) return;
    
                double scroll = e.getPreciseWheelRotation();
                double oldZoom = zoom;
                double zoomFactor = 0.1;
    
                if (scroll < 0) zoom -= zoomFactor;
                else zoom += zoomFactor;
    
                zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom));
    
                double mx = e.getX();
                double my = e.getY();
    
                simCameraX = (int) ((simCameraX - mx) * (zoom / oldZoom));
                simCameraY = (int) ((simCameraY - my) * (zoom / oldZoom));
            }
        };
    
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }
}
