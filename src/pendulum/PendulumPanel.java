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

    public static void setSIM_HEIGHT(int SIM_HEIGHT) { PendulumPanel.SIM_HEIGHT = SIM_HEIGHT; }
    public static void setSIM_WIDTH(int SIM_WIDTH) { PendulumPanel.SIM_WIDTH = SIM_WIDTH; }

    // ----------------------------
    // Simulation & state
    // ----------------------------
    private static double time = 0;
    public static double getTime() { return time; }

    private boolean isRunning = true;
    private boolean cameraFollow = false;
    private boolean tracing = false;

    private final Pendulum pendulum = new Pendulum(200, 10, 0, 0, Math.PI / 4, 1);
    private final List<TrailPoint> trail = new ArrayList<>();

    // ----------------------------
    // Buttons
    // ----------------------------
    private final List<Button> buttons = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();

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

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSimSize();
            }
        });
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
        int marginRight = 1900;
        int baseY = 50;

        // Calculate starting X position for the rightmost button (Trace)
        int totalWidth = (buttonWidth + spacing) * 4 - spacing;
        int baseX = getWidth() - totalWidth + marginRight;

        // Stop / Start button
        Button stopButton = new Button(baseX, baseY, buttonWidth, buttonHeight, "STOP", Colors.STOP_RED.toColor(), 1);
        stopButton.setOnClick(() -> {
            isRunning = !isRunning;
            stopButton.setButtonColor(isRunning ? Colors.STOP_RED.toColor() : Colors.GO_GREEN.toColor());
            stopButton.setText(isRunning ? "STOP" : "START");
        });
        buttons.add(stopButton);

        // Reset button
        Button resetButton = new Button(baseX + (buttonWidth + spacing), baseY, buttonWidth, buttonHeight, "Reset", Colors.RESET.toColor(), 1);
        resetButton.setOnClick(() -> {
            pendulum.reset();
            trail.clear();
            if (cameraFollow) follow();
        });
        buttons.add(resetButton);

        // Follow button
        Button followButton = new Button(baseX + 2 * (buttonWidth + spacing), baseY, buttonWidth, buttonHeight, "Follow", Colors.FOLLOW.toColor(), 1);
        followButton.setOnClick(() -> cameraFollow = !cameraFollow);
        buttons.add(followButton);

        // Trace button
        Button traceButton = new Button(baseX + 3 * (buttonWidth + spacing), baseY, buttonWidth, buttonHeight, "Trace", Colors.TRACE.toColor(), 1);
        traceButton.setOnClick(() -> tracing = !tracing);
        buttons.add(traceButton);
    }

    public void setUpTabButton() {
        tabButtons.clear();
        tabButtons.addAll(DataSet.createTabButtons(pendulum.getPendulumData()));
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

        if (cameraFollow) {
            follow();
        } else {
            simCameraX = cameraCenterX + cameraDiffX;
            simCameraY = cameraCenterY + cameraDiffY;
        }

        trail.add(new TrailPoint(pendulum.getBobX(), pendulum.getBobY(), time));
        
        int TrailLimit = 1000;
        if(trail.size() > TrailLimit){
            trail.remove(0);
        }
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

        drawBackground(g);
        drawFullPanelVignette(g2);

        drawSimBox(g);
        drawGrid(g);

        Shape oldClip = g2.getClip();
        g2.setClip(CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT);

        if (tracing) drawTrailLine(g2);
        pendulum.draw(g, simCameraX, simCameraY);

        g2.setClip(oldClip);
        drawButtons(g2);

        DataSet.drawDataSet(g, pendulum.getPendulumData());
    }

    private void drawBackground(Graphics g) {
        g.setColor(new Color(200, 30, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawSimBox(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(CORNER_X - SIM_BORDER_STROKE, CORNER_Y - SIM_BORDER_STROKE,
                   SIM_WIDTH + 2 * SIM_BORDER_STROKE, SIM_HEIGHT + 2 * SIM_BORDER_STROKE);
        g.setColor(Color.WHITE);
        g.fillRect(CORNER_X, CORNER_Y, SIM_WIDTH, SIM_HEIGHT);
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

    //default if needed
    // private void drawTrail(Graphics g) {
    //     for (TrailPoint p : trail) p.draw(g, simCameraX, simCameraY);
    // }

    private void drawTrailLine(Graphics2D g2) {
    if (trail.size() < 2) return; // Need at least 2 points to draw a line

    for (int i = 0; i < trail.size() - 1; i++) {
        TrailPoint p1 = trail.get(i);
        TrailPoint p2 = trail.get(i + 1);

        // Use the average time of the two points to determine color
        double avgTime = (p1.getTime() + p2.getTime()) / 2.0;
        g2.setColor(timeToColor(avgTime));

        g2.setStroke(new BasicStroke(2)); // line thickness
        g2.drawLine(
            p1.getX() + simCameraX, p1.getY() + simCameraY,
            p2.getX() + simCameraX, p2.getY() + simCameraY
        );
        }
    }

    // Helper method for converting time to color (similar to TrailPoint)
    private Color timeToColor(double time) {
        float hue = (float) ((time % 10) / 10.0); // cycles every 10 seconds
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    private void drawButtons(Graphics2D g2) {
        for (Button b : buttons) b.draw(g2);
        for (Button b : tabButtons) b.draw(g2);
    }

    private void drawFullPanelVignette(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        int cx = width / 2;
        int cy = height / 2;

        float radius = (float) Math.hypot(cx, cy);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)};
        RadialGradientPaint paint = new RadialGradientPaint(cx, cy, radius, dist, colors);
        g2.setPaint(paint);
        g2.fillRect(0, 0, width, height);
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
        return x >= CORNER_X && x <= CORNER_X + SIM_WIDTH &&
               y >= CORNER_Y && y <= CORNER_Y + SIM_HEIGHT;
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
