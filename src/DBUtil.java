import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

/**
 * Small utility for creating DB connections from a properties file
 * and showing a simple settings dialog when connection cannot be made.
 */
public class DBUtil {
    private static final String PROPS_FILE = "db.properties";

    public static Connection getConnection(Component parent) throws SQLException {
        Properties p = loadProps();
        String host = p.getProperty("host", "localhost");
        String port = p.getProperty("port", "3333");
        String db = p.getProperty("database", "empnav");
        String user = p.getProperty("user", "root");
        String pass = p.getProperty("password", "1234");

        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, db);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException cnf) {
            String msg = "JDBC driver not found. Make sure the MySQL Connector/J JAR is on the classpath.\n" +
                    "Expected driver: com.mysql.cj.jdbc.Driver";
            JOptionPane.showMessageDialog(parent, msg, "DB Error - Driver missing", JOptionPane.ERROR_MESSAGE);
            throw new SQLException(msg, cnf);
        } catch (SQLException ex) {
            // If database does not exist, offer to create database and table
            try {
                int vendorCode = ex.getErrorCode();
                boolean unknownDb = vendorCode == 1049 || ex.getMessage().toLowerCase().contains("unknown database");
                if (unknownDb) {
                    int create = JOptionPane.showConfirmDialog(parent,
                            "Database '" + db + "' does not exist. Create database and required table now?",
                            "Create database?", JOptionPane.YES_NO_OPTION);
                    if (create == JOptionPane.YES_OPTION) {
                        try {
                            String urlNoDb = String.format("jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true", host, port);
                            Connection adminCon = DriverManager.getConnection(urlNoDb, user, pass);
                            Statement st = adminCon.createStatement();
                            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + db + "`");
                            st.executeUpdate("USE `" + db + "`");
                            st.executeUpdate("CREATE TABLE IF NOT EXISTS records (id INT AUTO_INCREMENT PRIMARY KEY, empname VARCHAR(200), salary VARCHAR(50), photo LONGBLOB)");
                            st.close();
                            adminCon.close();
                            JOptionPane.showMessageDialog(parent, "Database and table created. Reconnecting...");
                            return DriverManager.getConnection(url, user, pass);
                        } catch (SQLException inner) {
                            JOptionPane.showMessageDialog(parent, "Failed to create database/table: " + inner.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                            // fall through to show settings option
                        }
                    }
                }
            } catch (Exception ignore) {}

            // Show a helpful dialog with actions
            String message = "Unable to connect to database:\n" + ex.getMessage() + "\n\n" +
                    "Common fixes:\n" +
                    " - Ensure your database server is running and reachable at " + host + ":" + port + "\n" +
                    " - Verify credentials (user/password) and database name\n" +
                    " - If your DB uses default MySQL port, try port 3306\n" +
                    " - Ensure the MySQL Connector/J jar is on the application's classpath\n\n" +
                    "You can open DB settings to edit connection parameters.";

            int choice = JOptionPane.showOptionDialog(parent, message, "DB Error: Connection failed",
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    new String[]{"Open DB Settings", "OK"}, "Open DB Settings");

            if (choice == JOptionPane.YES_OPTION) {
                openSettingsDialog(parent, p);
            }

            throw ex;
        }
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        File f = new File(PROPS_FILE);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                p.load(fis);
            } catch (IOException ignored) {}
        }
        // defaults will be used if not present
        return p;
    }

    private static void openSettingsDialog(Component parent, Properties current) {
        JTextField hostField = new JTextField(current.getProperty("host", "localhost"));
        JTextField portField = new JTextField(current.getProperty("port", "3333"));
        JTextField dbField = new JTextField(current.getProperty("database", "empnav"));
        JTextField userField = new JTextField(current.getProperty("user", "root"));
        JPasswordField passField = new JPasswordField(current.getProperty("password", "1234"));

        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Host:")); panel.add(hostField);
        panel.add(new JLabel("Port:")); panel.add(portField);
        panel.add(new JLabel("Database:")); panel.add(dbField);
        panel.add(new JLabel("User:")); panel.add(userField);
        panel.add(new JLabel("Password:")); panel.add(passField);

        int res = JOptionPane.showConfirmDialog(parent, panel, "DB Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Properties np = new Properties();
            np.setProperty("host", hostField.getText().trim());
            np.setProperty("port", portField.getText().trim());
            np.setProperty("database", dbField.getText().trim());
            np.setProperty("user", userField.getText().trim());
            np.setProperty("password", new String(passField.getPassword()));
            try (FileOutputStream fos = new FileOutputStream(PROPS_FILE)) {
                np.store(fos, "DB connection settings for EmployeeNavigationSystem");
                JOptionPane.showMessageDialog(parent, "Saved DB settings to " + PROPS_FILE + "\nTry the operation again.");
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(parent, "Unable to save settings: " + ioe.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
