package pendulum;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public final class PendulumPanel extends JPanel {
    
    // ----------------------------
    // Constants
    // ----------------------------
    private static final int CORNER_X = 50;
    private static final int CORNER_Y = 50;
    private static final int SIM_BORDER_STROKE = 5;
    private static final int SIM_SPACING = 50;
    private static final int RESIZE_THRESHOLD = 1800;
    
    // ----------------------------
    // Simulation size & camera
    // ----------------------------
    private static int SIM_WIDTH = 1200;
    private static int SIM_HEIGHT = 1200;
    private static int simCameraX, simCameraY;
    private static int cameraDiffX = 0, cameraDiffY = 0;
    private static int cameraCenterX, cameraCenterY;
    
    public static void setSIM_HEIGHT(int SIM_HEIGHT) {
        PendulumPanel.SIM_HEIGHT = SIM_HEIGHT;
    }
    
    public static void setSIM_WIDTH(int SIM_WIDTH) {
        PendulumPanel.SIM_WIDTH = SIM_WIDTH;
    }
    
    // ----------------------------
    // Simulation & state
    // ----------------------------
    private static double time = 0;
    
    public static double getTime() {
        return time;
    }
    
    private boolean isRunning = true;
    private boolean cameraFollow = false;
    private boolean tracing = false;
    
    private final Pendulum pendulum = new Pendulum(200, 10, 0, 0, Math.PI / 4, 0);
    private final List<TrailPoint> trail = new ArrayList<>();
    
    // ----------------------------
    // Buttons & Text Field
    // ----------------------------
    private final List<Button> buttons = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private int buttonSelected = 0;
    private int buttonNameIndexSelected = 0;
    private double userValue = 0; // Parsed value
    private List<DataElement> dataElements = pendulum.getPendulumData();
    private JTextField textField = new JTextField();

    {
        for (int i = 0; i < dataElements.size(); i++) {
            if (dataElements.get(i).isChangeable()) {
                buttonNameIndexSelected = i;
                break;
            }
        }
    }

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
        setupSimBoxMouseDetection();
        setupMouseHandlers();
        setupButtons();
        setUpTabButton();
        startMainLoop();
        makeTextField();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSimSize();
            }
        });
    }

    private void makeTextField() {
        setLayout(null);

        // Create text field
        textField = new JTextField();
        textField.setBounds(1605, 150, 150, 40);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setBackground(Color.WHITE);

        // Focus listener (changes color when active)
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBackground(Colors.ACTIVE.toColor()); // light blue
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBackground(Color.WHITE);
            }
        });

        add(textField);

        // Common parsing logic in a lambda
        Runnable parseInput = () -> {
            String text = textField.getText().trim();
            try {
                userValue = Double.parseDouble(text);

                switch (buttonSelected) {
                    case 0 -> pendulum.setLength(userValue);
                    case 1 -> pendulum.setMass(userValue);
                    case 2 -> pendulum.setPivotX(userValue);
                    case 3 -> pendulum.setPivotY(userValue);
                    case 4 -> pendulum.setAngle(userValue);
                    case 5 -> pendulum.setInitialAngle(userValue);
                    case 6 -> pendulum.setAngularVelocity(userValue);
                    case 7 -> pendulum.setInitialAngularVelocity(userValue);
                    default -> {}
                }

                textField.setBackground(Color.WHITE); // optional: reset color on success
                PendulumPanel.this.requestFocusInWindow(); // remove focus from text field

            } catch (NumberFormatException ex) {
                textField.setBackground(Colors.ERROR.toColor()); // light red
            }
        };

        // Create "SET" button
        Button setButton = new Button(1760, 150, 70, 40, "SET", Colors.SET.toColor(), 1, 2, 20);
        setButton.setOnClick(parseInput);
        buttons.add(setButton);

        // Trigger same logic when Enter is pressed in the text field
        textField.addActionListener(e -> parseInput.run());
    }

    // ----------------------------
    // Initialization
    // ----------------------------
    private void initCamera() {
        cameraCenterX = CORNER_X + SIM_WIDTH / 2;
        cameraCenterY = CORNER_Y + SIM_HEIGHT / 4;
        simCameraX = cameraCenterX;
        simCameraY = cameraCenterY;
    }

    private void setupButtons() {
        int buttonWidth = 130;
        int buttonHeight = 60;
        int spacing = 20;
        int marginRight = 1300;
        int baseY = 50;
        int baseX = marginRight;
        int topButtonBorder = 3;
        int topButtonFontSize = 30;

        // Stop / Start button
        Button stopButton = new Button(baseX, baseY, buttonWidth, buttonHeight, "STOP", Colors.STOP_RED.toColor(), 1, topButtonBorder, topButtonFontSize);
        stopButton.setOnClick(() -> {
            isRunning = !isRunning;
            stopButton.setButtonColor(isRunning ? Colors.STOP_RED.toColor() : Colors.GO_GREEN.toColor());
            stopButton.setText(isRunning ? "STOP" : "START");
        });
        buttons.add(stopButton);

        // Reset button
        Button resetButton = new Button(baseX + (buttonWidth + spacing), baseY, buttonWidth, buttonHeight, "Reset", Colors.RESET.toColor(), 1, topButtonBorder, topButtonFontSize);
        resetButton.setOnClick(() -> {
            pendulum.reset();
            trail.clear();
            time = 0;
            cameraDiffX = 0;
            cameraDiffY = 0;
            if (cameraFollow) follow();
        });
        buttons.add(resetButton);

        // Follow button
        Button followButton = new Button(baseX + 2 * (buttonWidth + spacing), baseY, buttonWidth, buttonHeight, "Follow", Colors.FOLLOW.toColor(), 1, topButtonBorder, topButtonFontSize);
        followButton.setOnClick(() -> cameraFollow = !cameraFollow);
        buttons.add(followButton);

        // Trace button
        Button traceButton = new Button(baseX + 3 * (buttonWidth + spacing), baseY, buttonWidth, buttonHeight, "Trace", Colors.TRACE.toColor(), 1, topButtonBorder, topButtonFontSize);
        traceButton.setOnClick(() -> tracing = !tracing);
        buttons.add(traceButton);
    }

    private void setUpTabButton() {
        tabButtons.clear();
        tabButtons.addAll(DataSet.createTabButtons(pendulum.getPendulumData()));

        for (int i = 0; i < tabButtons.size(); i++) {
            Button b = tabButtons.get(i);
            int changeableElementIndex = 0;
            int elementIndex = 0;
            for (int j = 0; j < pendulum.getPendulumData().size(); j++) {
                if (pendulum.getPendulumData().get(j).isChangeable()) {
                    if (changeableElementIndex == i) {
                        elementIndex = j;
                        break;
                    }
                    changeableElementIndex++;
                }
            }

            int buttonNameIndex = elementIndex;
            int buttonIndex = i;
            b.setOnClick(() -> {
                buttonNameIndexSelected = buttonNameIndex;
                buttonSelected = buttonIndex;
                updateTabButtons();
            });
        }
        updateTabButtons();
    }

    private void updateTabButtons() {
        for (int i = 0; i < tabButtons.size(); i++) {
            Button b = tabButtons.get(i);
            b.setButtonColor(buttonSelected != i ? Colors.TAB.toColor() : Colors.TAB_SELECTED.toColor());
        }
        
    }

    private void setupMouseHandlers() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMouseButtons(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleMouseButtons(e); }
            @Override
            public void mouseMoved(MouseEvent e) { handleMouseButtons(e); }

            private void handleMouseButtons(MouseEvent e) {
                for (Button b : buttons) b.handleMouse(e);
                for (Button b : tabButtons) b.handleMouse(e);
                repaint();
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // ----------------------------
    // Main Loop
    // ----------------------------
    private void startMainLoop() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            update();
            repaint();
        }), 0, 16, TimeUnit.MILLISECONDS);
    }

    private void update() {
        if (isRunning) pendulum.update(0.16);
        time += 0.16;

        if (cameraFollow) follow();
        else {
            simCameraX = cameraCenterX + cameraDiffX;
            simCameraY = cameraCenterY + cameraDiffY;
        }

        trail.add(new TrailPoint(pendulum.getBobX(), pendulum.getBobY(), time));
        int TrailLimit = 500;
        if (trail.size() > TrailLimit) trail.remove(0);
    }

    public void follow() {
        simCameraX = -pendulum.getBobX() + SIM_WIDTH / 2 + CORNER_X;
        simCameraY = -pendulum.getBobY() + SIM_HEIGHT / 2 + CORNER_Y;
    }

    // ----------------------------
    // Simulation size handling
    // ----------------------------
    private void updateSimSize() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth > RESIZE_THRESHOLD || panelHeight > RESIZE_THRESHOLD) {
            SIM_WIDTH = SIM_HEIGHT = 1200;
        } else {
            int newSize = Math.min(panelWidth, panelHeight);
            SIM_WIDTH = SIM_HEIGHT = (int) (newSize * 0.7);
        }

        repaint();
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        drawbackground(g);
        drawFullPanelVignette(g2);

        Utils.drawSquare(g2, CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT, Color.WHITE, SIM_BORDER_STROKE);//draw sim box
        drawInsideSimBox(g2);

        drawButtons(g2);
        DataSet.drawDataSet(g, pendulum.getPendulumData());

        String variableNameSelected = pendulum.getPendulumData().get(buttonNameIndexSelected).getVariableName();
        drawTextFieldName(g2, 1300, 150, 300,40, variableNameSelected);
    }

    private void drawbackground(Graphics g) {
        g.setColor(new Color(200, 30, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawInsideSimBox(Graphics2D g2) {
        Shape oldClip = g2.getClip();
        g2.setClip(CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT);

        drawGrid(g2);
        if (tracing) drawTrailLine(g2);
        pendulum.draw(g2, simCameraX, simCameraY);

        g2.setClip(oldClip);
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(0, 150, 255));

        int xOffset = simCameraX % SIM_SPACING;
        int yOffset = simCameraY % SIM_SPACING;

        for (int x = CORNER_X + xOffset; x <= CORNER_X + SIM_WIDTH; x += SIM_SPACING)
            g.drawLine(x, CORNER_Y, x, CORNER_Y + SIM_HEIGHT);

        for (int y = CORNER_Y + yOffset; y <= CORNER_Y + SIM_HEIGHT; y += SIM_SPACING)
            g.drawLine(CORNER_X, y, CORNER_X + SIM_WIDTH, y);
    }

    private void drawTrailLine(Graphics2D g2) {
        if (trail.size() < 2) return;

        for (int i = 0; i < trail.size() - 1; i++) {
            TrailPoint p1 = trail.get(i);
            TrailPoint p2 = trail.get(i + 1);

            double avgTime = (p1.getTime() + p2.getTime()) / 2.0;
            g2.setColor(timeToColor(avgTime));
            g2.setStroke(new BasicStroke(2));

            g2.drawLine(p1.getX() + simCameraX, p1.getY() + simCameraY,
                        p2.getX() + simCameraX, p2.getY() + simCameraY);
        }
    }

    private Color timeToColor(double time) {
        float hue = (float) ((time % 10) / 10.0);
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    private void drawButtons(Graphics2D g2) {
        for (Button b : buttons) b.draw(g2);
        for (Button b : tabButtons) b.draw(g2);
    }

    private void drawFullPanelVignette(Graphics2D g2) {
        Utils.drawVignette(
            g2,
            getWidth(),
            getHeight(),
            new Color(0, 0, 0, 0),     // inner transparent color
            new Color(0, 0, 0, 150)    // outer dark fade color
        );
    }

    private void drawTextFieldName(Graphics2D g2, int x, int y, int width, int height, String name){
        Utils.drawBox(g2, x, y, width, height, Color.white);
        Utils.drawTextInBox(g2, name, x, y, width, height);
    }

    // ----------------------------
    // Mouse detection & camera dragging
    // ----------------------------
    private void setupSimBoxMouseDetection() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isMouseInSimBox(e.getX(), e.getY())) {
                    mousePressedInSimBox = true;
                    cameraOffsetAtPressX = cameraDiffX;
                    cameraOffsetAtPressY = cameraDiffY;
                    mouseLastX = e.getX();
                    mouseLastY = e.getY();
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressedInSimBox = false;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                handleMouseLeaving();
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseInSimBox = isMouseInSimBox(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateMousePosition(e);
                if (mousePressedInSimBox) {
                    if (mouseInSimBox) {
                        calculateCameraDiff();
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        handleMouseLeaving();
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private boolean isMouseInSimBox(int x, int y) {
        return x >= CORNER_X && x <= CORNER_X + SIM_WIDTH
            && y >= CORNER_Y && y <= CORNER_Y + SIM_HEIGHT;
    }

    private void handleMouseLeaving() {
        mousePressedInSimBox = false;
        mouseInSimBox = false;
    }

    private void updateMousePosition(MouseEvent e) {
        mouseCurrentX = e.getX();
        mouseCurrentY = e.getY();
        mouseInSimBox = isMouseInSimBox(mouseCurrentX, mouseCurrentY);
    }

    private void calculateCameraDiff() {
        cameraDiffX = mouseCurrentX - mouseLastX + cameraOffsetAtPressX;
        cameraDiffY = mouseCurrentY - mouseLastY + cameraOffsetAtPressY;
    }
}
