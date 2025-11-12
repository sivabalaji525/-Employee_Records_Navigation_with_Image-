import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class empnav extends JFrame {
    private JTextField txtname, txtsal;
    private JLabel imagelabel;
    private JButton first, prev, next, last, search, home;
    private Connection con;
    private Statement stat;
    private ResultSet rs;

    public empnav() {
        setTitle("Employee Navigation");
        setSize(800, 700);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // === MAIN PANEL ===
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font inputFont = new Font("Arial", Font.PLAIN, 16);
        Dimension textFieldSize = new Dimension(550, 35);

        // === INPUT PANEL ===
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        inputPanel.setBackground(Color.WHITE);

        // Name
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        txtname = new JTextField(20);
        txtname.setFont(inputFont);
        txtname.setPreferredSize(textFieldSize);

        // Salary
        JLabel salaryLabel = new JLabel("Salary:");
        salaryLabel.setFont(labelFont);
        txtsal = new JTextField(20);
        txtsal.setFont(inputFont);
        txtsal.setPreferredSize(textFieldSize);

        // Add inputs
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 15);
        inputPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        inputPanel.add(txtname, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(salaryLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        inputPanel.add(txtsal, gbc);

        // === IMAGE PANEL ===
        JPanel imagePanel = new JPanel(new BorderLayout(0, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        imagePanel.setBackground(Color.WHITE);

        JLabel imageTitle = new JLabel("Employee Photo", SwingConstants.CENTER);
        imageTitle.setFont(labelFont);

        imagelabel = new JLabel();
        imagelabel.setPreferredSize(new Dimension(650, 320));
        imagelabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagelabel.setVerticalAlignment(SwingConstants.CENTER);
        imagelabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        imagelabel.setBackground(Color.WHITE);
        imagelabel.setOpaque(true);

        imagePanel.add(imageTitle, BorderLayout.NORTH);
        imagePanel.add(imagelabel, BorderLayout.CENTER);

        // === BUTTON PANEL ===
        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 15, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        Dimension buttonSize = new Dimension(120, 40);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        first = createStyledButton("First", buttonSize, buttonFont);
        prev = createStyledButton("Previous", buttonSize, buttonFont);
        next = createStyledButton("Next", buttonSize, buttonFont);
        last = createStyledButton("Last", buttonSize, buttonFont);
        search = createStyledButton("Search", buttonSize, buttonFont);

        home = createStyledButton("Home", buttonSize, buttonFont);
        home.setBackground(new Color(200, 200, 200));
        home.setForeground(Color.BLACK);

        buttonPanel.add(first);
        buttonPanel.add(prev);
        buttonPanel.add(next);
        buttonPanel.add(last);
        buttonPanel.add(search);
        buttonPanel.add(home);

        // === ADD PANELS TO MAIN ===
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(inputPanel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(imagePanel, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.NORTH);

        // === CONNECT TO DB (without loading first record) ===
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                connectDB();
                return null;
            }
        }.execute();

        // === CLEAR FIELDS ON START ===
        txtname.setText("");
        txtsal.setText("");
        imagelabel.setIcon(null);

        // === BUTTON ACTIONS ===
        first.addActionListener(e -> moveFirst());
        prev.addActionListener(e -> movePrev());
        next.addActionListener(e -> moveNext());
        last.addActionListener(e -> moveLast());
        search.addActionListener(e -> searchByName());
        home.addActionListener(e -> {
            dispose();
            Main.showMainWindow();
        });
    }

    // === CONNECT TO DATABASE ===
    private void connectDB() {
        try {
            con = DBUtil.getConnection(this);
            stat = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stat.executeQuery("SELECT empname, salary, photo FROM records");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === DISPLAY RECORD DATA ===
    private void showData() {
        try {
            String name = rs.getString("empname");
            String sal = rs.getString("salary");

            txtname.setText(name != null ? name : "");
            txtsal.setText(sal != null ? sal : "");

            Blob blob = rs.getBlob("photo");
            if (blob != null) {
                byte[] imgbytes = blob.getBytes(1, (int) blob.length());
                ImageIcon imageic = new ImageIcon(new ImageIcon(imgbytes)
                        .getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH));
                imagelabel.setIcon(imageic);
            } else {
                imagelabel.setIcon(null);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Show error: " + ex.getMessage());
        }
    }

    // === NAVIGATION METHODS ===
    private void moveFirst() { try { if (rs != null && rs.first()) showData(); } catch (Exception ignored) {} }
    private void movePrev()  { try { if (rs != null && !rs.isFirst()) { rs.previous(); showData(); } } catch (Exception ignored) {} }
    private void moveNext()  { try { if (rs != null && !rs.isLast())   { rs.next(); showData(); } } catch (Exception ignored) {} }
    private void moveLast()  { try { if (rs != null && rs.last()) showData(); } catch (Exception ignored) {} }

    // === BUTTON STYLE CREATOR ===
    private JButton createStyledButton(String text, Dimension size, Font font) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180)); // Steel Blue
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 102, 153), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237)); // Cornflower Blue
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180)); // Steel Blue
            }
        });
        return button;
    }

    // === SEARCH RECORD ===
    private void searchByName() {
        String name = JOptionPane.showInputDialog(this, "Enter employee name to search:");
        if (name == null || name.trim().isEmpty()) return;

        try {
            String sql = "SELECT empname, salary, photo FROM records WHERE empname = ?";
            PreparedStatement pst = con.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, name.trim());
            ResultSet result = pst.executeQuery();

            if (result.next()) {
                rs = result;
                showData();
                JOptionPane.showMessageDialog(this, "Record Found!");
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found!");
            }

            pst.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            if (rs != null) rs.close();
            if (stat != null) stat.close();
            if (con != null) con.close();
        } catch (Exception ignored) {}
    }

    // === MAIN METHOD (for standalone test) ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new empnav().setVisible(true));
    }
}
