import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            // connection works
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL.\n\n" + e.getMessage()
                  + "\n\nCheck that MySQL is running, library_db.sql has been imported,\n"
                  + "and the USER/PASSWORD in DBConnection.java are correct.",
                    "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}