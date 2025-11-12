import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class emp extends JFrame {
    JTextField txtname, txtsal;
    JLabel imagelabel;
    JButton browse, save, home;
    String path = null;
    byte[] userimage = null;
    Connection con;
    PreparedStatement pst;

    public emp() {
        setTitle("Employee Registration");
        setSize(520, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main container with padding
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        // Fonts and sizes
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font inputFont = new Font("Arial", Font.PLAIN, 14);
        Dimension textSize = new Dimension(300, 32);
        Dimension imageSize = new Dimension(300, 300);
        Dimension btnSize = new Dimension(140, 36);

        // Name label + field
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        txtname = new JTextField(20);
        txtname.setFont(inputFont);
        txtname.setPreferredSize(textSize);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(8, 8, 8, 8);
        content.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        content.add(txtname, gbc);

        // Salary label + field
        JLabel salaryLabel = new JLabel("Salary:");
        salaryLabel.setFont(labelFont);
        txtsal = new JTextField(20);
        txtsal.setFont(inputFont);
        txtsal.setPreferredSize(textSize);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        content.add(salaryLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        content.add(txtsal, gbc);

        // Image area (centered)
        JPanel imagePanel = new JPanel(new BorderLayout(0, 8));
        imagePanel.setBackground(Color.WHITE);
        JLabel imageTitle = new JLabel("Selected Photo", SwingConstants.CENTER);
        imageTitle.setFont(labelFont);
        imagelabel = new JLabel();
        imagelabel.setPreferredSize(imageSize);
        imagelabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagelabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        imagelabel.setOpaque(true);
        imagelabel.setBackground(Color.WHITE);

        imagePanel.add(imageTitle, BorderLayout.NORTH);
        JPanel centerImg = new JPanel(new GridBagLayout());
        centerImg.setBackground(Color.WHITE);
        centerImg.add(imagelabel);
        imagePanel.add(centerImg, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(16, 8, 8, 8);
        content.add(imagePanel, gbc);

    // Buttons panel
    browse = new JButton("Browse Image");
    save = new JButton("Save");
    home = new JButton("Home");
    browse.setPreferredSize(btnSize);
    save.setPreferredSize(btnSize);
    browse.setFont(new Font("Arial", Font.BOLD, 13));
    save.setFont(new Font("Arial", Font.BOLD, 13));

    // Small visual styling
    browse.setBackground(new Color(70,130,180));
    browse.setForeground(Color.WHITE);
    save.setBackground(new Color(60,179,113));
    save.setForeground(Color.WHITE);
    browse.setFocusPainted(false);
    save.setFocusPainted(false);

    home.setPreferredSize(new Dimension(90, 34));
    home.setFont(new Font("Arial", Font.PLAIN, 12));
    home.setBackground(new Color(200,200,200));
    home.setFocusPainted(false);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    btnPanel.setBackground(Color.WHITE);
    btnPanel.add(home);
    btnPanel.add(Box.createHorizontalStrut(10));
    btnPanel.add(browse);
    btnPanel.add(save);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);
        content.add(btnPanel, gbc);

        // Actions
        browse.addActionListener(e -> chooseImage());
        save.addActionListener(e -> saveData());
        home.addActionListener(e -> {
            dispose();
            Main.showMainWindow();
        });

        add(content, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    private void chooseImage() {
        JFileChooser picchoose = new JFileChooser();
        int res = picchoose.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File pic = picchoose.getSelectedFile();
        path = pic.getAbsolutePath();
        try {
            BufferedImage img = ImageIO.read(pic);
            ImageIcon imageic = new ImageIcon(img.getScaledInstance(250, 250, Image.SCALE_SMOOTH));
            imagelabel.setIcon(imageic);

            try (FileInputStream fis = new FileInputStream(pic);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buff = new byte[1024];
                int readNum;
                while ((readNum = fis.read(buff)) != -1) {
                    bos.write(buff, 0, readNum);
                }
                userimage = bos.toByteArray();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Image error: " + ex.getMessage());
        }
    }

    private void saveData() {
        String empname = txtname.getText().trim();
        String salary = txtsal.getText().trim();
        if (empname.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter name"); return; }

        try {
            // Use DBUtil which reads db.properties (or defaults) and offers a settings dialog on failure
            con = DBUtil.getConnection(this);

            pst = con.prepareStatement("INSERT INTO records(empname,salary,photo) VALUES(?,?,?)");
            pst.setString(1, empname);
            pst.setString(2, salary);
            pst.setBytes(3, userimage);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Record Added!");
            txtname.setText("");
            txtsal.setText("");
            imagelabel.setIcon(null);
            userimage = null;
        } catch(SQLException ex) {
            // DBUtil already displayed actionable messages; show concise message here
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (pst != null) pst.close(); if (con!=null) con.close(); } catch(Exception ignored) {}
        }
    }
}
