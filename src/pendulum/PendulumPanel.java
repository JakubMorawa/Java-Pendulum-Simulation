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
import pendulum.uielements.Button;
import pendulum.uielements.DataSet;
import pendulum.uielements.Graph;
import pendulum.uielements.Label;
import pendulum.uielements.RoundedTextField;
import pendulum.uielements.Section;
import pendulum.uielements.TextFieldUtils;
import pendulum.uielements.UIColors;
import pendulum.uielements.Utils;

public final class PendulumPanel extends JPanel {

    // Simulation size & camera
    private long lastFpsTime = 0;
    private int frameCount = 0;
    private int currentFps = 0;

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
    private static final double ZOOM_MIN = 0.2;
    private static final double ZOOM_MAX = 2.0;

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
    private int selectedPendulum = 1;
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

    private static final Font UI_FONT = new Font("Poppins", Font.BOLD, 16);

    private Section buttonSection = new Section(1550, 50, 0, 100, 30, 3, UIColors.BACKGROUND.toColor(), Utils.richDarken(UIColors.BACKGROUND.toColor(), 0.4f));
    private Section pendelumSection = new Section(1810 , 50, 0, 100, 30, 3, UIColors.BACKGROUND.toColor(), Utils.richDarken(UIColors.BACKGROUND.toColor(), 0.4f));
    private Section inputSection = new Section(1300, 250, 0, 0, 30, 3, UIColors.BACKGROUND.toColor(), Utils.richDarken(UIColors.BACKGROUND.toColor(), 0.4f));
    private Section cameraSection = new Section(1300, 50, 10, 10, 30, 3, UIColors.BACKGROUND.toColor(), Utils.richDarken(UIColors.BACKGROUND.toColor(), 0.4f));

    private Section[] sections = {
        buttonSection,
        pendelumSection,
        inputSection,
        cameraSection
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

    private final String pausePath = "src\\pendulum\\resources\\images\\pause.png";
    private final String playPath = "src\\pendulum\\resources\\images\\play.png";
    private final String resetPath = "src\\pendulum\\resources\\images\\reset.png";
    private final String followPath = "src\\pendulum\\resources\\images\\follow.png";
    private final String tracePath = "src\\pendulum\\resources\\images\\trace.png";
    private final String arrowPath = "src\\pendulum\\resources\\images\\arrow.png";
    private final String nextPath = "src\\pendulum\\resources\\images\\next.png";
    private final String prevPath = "src\\pendulum\\resources\\images\\previous.png";
    private final String plusPath = "src\\pendulum\\resources\\images\\plus.png";

    // Camera label positions
    private final int xLabelX = 1300;
    private final int xLabelY = 130;
    private final int LABEL_SPACING = 215;
    private final int yLabelX = xLabelX + 100 + 80 + 5 + 30;
    private final int yLabelY = xLabelY;

    // Graph
    private static final int GRAPH_X = 1300;
    private static final int GRAPH_Y = 870;
    private static final int GRAPH_WIDTH = 370;
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
                pendulum1.update(0);//chnage it only for a little bit
                requestFocusInWindow();
            } catch (NumberFormatException ex) {
                textField.setBackground(UIColors.ERROR.toColor());
            }
        };

        textField = TextFieldUtils.createNumericTextField(
                1605, 190, 150, 40,
                UI_FONT,
                UIColors.ACTIVE.toColor(),
                UIColors.ERROR.toColor(),
                true,
                val -> {
                    userValue = val.doubleValue();
                    if (buttonSelected < pendulumSetters.size())
                        pendulumSetters.get(buttonSelected).accept(userValue);
                }
        );
        ((RoundedTextField) textField).setOffset(150, 0);
        inputSection.addElement((RoundedTextField) textField);


        Button setButton = createButton(40, 40, 22, "SET", plusPath,UIColors.SET.toColor(), 1, parseInput);
        buttons.add(setButton);
        setButton.setOffset(370, 0);
        inputSection.addElement(setButton);
        add(textField);
    }

    private void setupCameraTextFields() {

        int xFieldX = xLabelX + 100 + 5;
        int yFieldX = yLabelX + 100 + 5;

        cameraXField = TextFieldUtils.createNumericTextField(
                xFieldX, xLabelY, 80, 40, UI_FONT, UIColors.ACTIVE.toColor(), UIColors.ERROR.toColor(),
                false, val -> cameraDiffX = val.intValue()
        );

        cameraYField = TextFieldUtils.createNumericTextField(
                yFieldX, yLabelY, 80, 40, UI_FONT, UIColors.ACTIVE.toColor(), UIColors.ERROR.toColor(),
                false, val -> cameraDiffY = val.intValue()
        );

        add(cameraXField);
        ((RoundedTextField) cameraXField).setOffset(20, -50);
        cameraSection.addElement((RoundedTextField) cameraXField);

        add(cameraYField);
        ((RoundedTextField) cameraYField).setOffset(-70, 0);
        cameraSection.addElement((RoundedTextField) cameraYField);
    }

    // ----------------------------
    // Buttons & Labels
    // ----------------------------
    private void setupButtons() {
        int buttonType = Button.TYPE_ROUND;
        int topSectionSize = 60;

        Button stopBtn = createButton(topSectionSize, topSectionSize, 30, "Stop Siumulation", pausePath, UIColors.STOP_RED.toColor(), buttonType,() ->  {
            isRunning = !isRunning;
            Button b = buttons.get(0); 
            b.setButtonColor(isRunning ? UIColors.STOP_RED.toColor() : UIColors.GO_GREEN.toColor());
            b.setImg(isRunning ? pausePath: playPath);
        });
        buttonSection.addElement(stopBtn);

        Button resetBtn = createButton(topSectionSize, topSectionSize,30, "Reset Simulation", resetPath ,UIColors.RESET.toColor(),  buttonType, () -> {
            int lastBobX = 0, lastBobY = 0;
            for (int i = 0; i < pendulums.size(); i++) {
                Pendulum p = pendulums.get(i);
                if (i > 0) {
                    p.setPivotX(lastBobX);
                    p.setPivotY(lastBobY);
                }
                p.reset();
                
                lastBobX = (int) p.getBobX();
                lastBobY = (int) p.getBobY();
            }
            trail.clear();
            time = 0; cameraDiffX = 0; cameraDiffY = 0;
            if (cameraFollow) follow();
        });
        buttonSection.addElement(resetBtn);
        
        Button followBtn = createButton(topSectionSize, topSectionSize,30, "Follow Pendulum", followPath, UIColors.FOLLOW.toColor(), buttonType, 
        () -> cameraFollow = !cameraFollow);
        buttonSection.addElement(followBtn);
        
        buttonSection.nextRow();
        
        Button traceBtn = createButton(topSectionSize, topSectionSize,30, "Trace", tracePath, UIColors.TRACE.toColor(), buttonType, 
        () -> tracing = !tracing);
        buttonSection.addElement(traceBtn);
        
        
        Button arrowBtn = createButton(topSectionSize, topSectionSize,30, "Arrow", arrowPath, UIColors.ARROW_BUTTON.toColor(), buttonType, 
        () -> showArrow = !showArrow);
        buttonSection.addElement(arrowBtn);
        
        
        //pendulum buttons
        int pendelumSectionSize = 52;

        Button prevBtn = createButton(pendelumSectionSize, pendelumSectionSize, 20, "Previous Pendulum", prevPath,UIColors.PENDULUM_BUTTON.toColor(), buttonType, 
                () -> {if (selectedPendulum > 1) selectedPendulum--;
                    pendulumSetters = pendulumSetters(selectedPendulum);
                });

        pendelumSection.addElement(prevBtn);

        Button nextBtn = createButton(pendelumSectionSize, pendelumSectionSize, 20, "Next Pendulum", nextPath, UIColors.PENDULUM_BUTTON.toColor(), buttonType, 
                () ->  {if (selectedPendulum < pendulums.size()) selectedPendulum++;
                    pendulumSetters = pendulumSetters(selectedPendulum);
                });
        pendelumSection.addElement(nextBtn);
    }

    private Button createButton(int x, int y, int w, int h, int fontSize, String text, Color color, int buttonType, Runnable action) {
        Button btn = new Button(x, y, w, h, text, color, buttonType, 3, fontSize);
        btn.setOnClick(action);
        buttons.add(btn);
        return btn;
    }

    private Button createButton(int w, int h , int fontSize, String text, String imgPath, Color color, int buttonType, Runnable action) {
        Button btn = new Button(0,0, w, h, text, color, buttonType, 3, fontSize);
        btn.setImg(imgPath);
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
        Label elementLabel = new Label(1300, 190, 300, 40, 22, Color.WHITE, Label.TYPE_ROUND, 3);
        labels.add(elementLabel);
        
        Label zoomLabel = new Label(yLabelX + LABEL_SPACING, yLabelY, 100, 40, 16, Color.WHITE, Label.TYPE_ROUND, 3);
        cameraSection.addElement(zoomLabel);
        cameraSection.nextRow();

        inputSection.addElement(elementLabel);
        
        Label cameraXLabel = new Label(xLabelX, xLabelY, 100, 40, 16, Color.WHITE, Label.TYPE_ROUND, 3);
        labels.add(cameraXLabel);
        cameraSection.addElement(cameraXLabel);
        cameraSection.nextRow();
        
        Label cameraYLabel = new Label(yLabelX, yLabelY, 100, 40, 16, Color.WHITE, Label.TYPE_ROUND, 3);
        labels.add(cameraYLabel);
        cameraSection.addElement(cameraYLabel);
        
        
        labels.add(zoomLabel);
        
        // public Label(int x, int y, int width, int height, int fontSize, Color color, int labelType, int borderWidth, Color borderColor) {
        Label pendulumLabel = new Label(0,0, 113, 40, 16, UIColors.LABEL_BACKGROUND.toColor(), Label.TYPE_ROUND, 3);
        labels.add(pendulumLabel);

        pendulumLabel.setOffset(0, 15);
        pendelumSection.nextRow();
        pendelumSection.addElement(pendulumLabel);
    }
    

    private void updateLabelText() {
        for (int i = 0; i < labels.size(); i++) {
            Label iLabel = labels.get(i);

            String newText = switch (i) {
                case 0 -> dataElements.get(buttonNameIndexSelected).getVariableName();
                case 1 -> "X: " + cameraDiffX;
                case 2 -> "Y: " + cameraDiffY;
                case 3 -> "Zoom: " + (int)(zoom*100);
                case 4 -> "Pendulum: " + selectedPendulum;
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

            Pendulum p = pendulums.get(selectedPendulum-1);
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

        drawSections(g2);

        drawButtons(g2);
        drawDataSet(g2);
        drawLabels(g2);
        drawGraph(g2, graph);
        
        drawPopUps(g2);

        long now = System.nanoTime();
        frameCount++;

        if (now - lastFpsTime >= 1_000_000_000L) { // 1 second
            currentFps = frameCount;
            frameCount = 0;
            lastFpsTime = now;
        }

        // Draw FPS text
        g.setColor(Color.WHITE);
        g.setFont(UI_FONT);
        g.drawString("FPS: " + currentFps, 10, 20);
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
                Pendulum previouusPendulum = pendulums.get(i-1);
                LastBobX = (int)(previouusPendulum.getBobX());
                LastBobY = (int)(previouusPendulum.getBobY());
                pendulums.get(i).setPivotX(LastBobX);
                pendulums.get(i).setPivotY(LastBobY);
                pendulums.get(i).drawLine(g2, simCameraX, simCameraY, zoom);
            }
        }
        for(int i = 0; i < pendulums.size(); i++){

            Color color = (i == selectedPendulum-1) ? UIColors.BOB_GREEN_COLOR.toColor() : UIColors.BOB_COLOR.toColor();
            pendulums.get(i).setBobColor(color);


            if(i == 0){
                pendulums.get(i).drawBob(g2, simCameraX, simCameraY, zoom);
            } else {
                Pendulum previouusPendulum = pendulums.get(i-1);
                LastBobX = (int)(previouusPendulum.getBobX());
                LastBobY = (int)(previouusPendulum.getBobY());
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
        Pendulum p = pendulums.get(selectedPendulum-1);
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
    
    private void drawDataSet(Graphics2D g2) { DataSet.drawDataSet(g2, pendulums.get(selectedPendulum-1).getPendulumData()); }

    private void drawPopUps(Graphics2D g2) {
        for (Button b : buttons) {
            b.drawPopUp(g2);
        }
        for (Button b : tabButtons) {
            b.drawPopUp(g2);
        }
    }
    
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
        Pendulum p = pendulums.get(selectedPendulum-1);
        simCameraX = -p.getBobX() + SIM_WIDTH / 2 + CORNER_X;
        simCameraY = -p.getBobY() + SIM_HEIGHT / 2 + CORNER_Y;
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
