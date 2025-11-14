package pendulum;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PendulumWindow {

    private static boolean isFullscreen = true;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pendulum Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            PendulumPanel panel = new PendulumPanel();

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            // Start in fullscreen
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setMinimumSize(new Dimension(2560, 1440));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();

            // Add a key listener for fullscreen toggle (F11)
            panel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_F11) {
                        toggleFullscreen(frame);
                    }
                }        
            });
        });
    }

    private static void toggleFullscreen(JFrame frame) {
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        if (isFullscreen) {
            // Exit fullscreen → return to windowed mode
            device.setFullScreenWindow(null);
            frame.dispose();
            frame.setUndecorated(false);
            frame.setVisible(true);
            frame.setSize(1920, 1440); // example size for windowed mode
            frame.setLocationRelativeTo(null);
        } else {
            // Enter fullscreen mode
            frame.dispose();
            frame.setUndecorated(true);
            device.setFullScreenWindow(frame);
        }
        isFullscreen = !isFullscreen;
    }
}