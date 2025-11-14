package pendulum;

import java.awt.*;
import javax.swing.*;

public class PendulumWindow {

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
        });
    }
}