import javax.swing.*;
import java.awt.*;

public class Main {
    private static JFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showMainWindow);
    }

    public static void showMainWindow() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame == null) {
                mainFrame = new JFrame("Employee System");
                mainFrame.setSize(400, 200);
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Create a main panel with some padding
                JPanel mainPanel = new JPanel();
                mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                mainPanel.setLayout(new GridLayout(2, 1, 10, 10));  // 2 rows, 1 column, with gaps

                JButton addButton = new JButton("Add Employee");
                JButton navButton = new JButton("Navigation");

                // Style the buttons
                addButton.setPreferredSize(new Dimension(200, 40));
                navButton.setPreferredSize(new Dimension(200, 40));
                Font buttonFont = new Font("Arial", Font.PLAIN, 14);
                addButton.setFont(buttonFont);
                navButton.setFont(buttonFont);

                addButton.addActionListener(e -> new emp().setVisible(true));
                navButton.addActionListener(e -> new empnav().setVisible(true));

                mainPanel.add(addButton);
                mainPanel.add(navButton);

                mainFrame.add(mainPanel);
            }

            // If already created, just bring to front and show
            mainFrame.setLocationRelativeTo(null);
            if (!mainFrame.isVisible()) mainFrame.setVisible(true);
            mainFrame.toFront();
            mainFrame.requestFocus();
        });
    }
}
