import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD =
            System.getenv("LIBRARY_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Runs a SELECT with a PreparedStatement and loads the result into a table model. */
    public static void loadTable(DefaultTableModel model, String sql, Object... params) throws SQLException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columns = meta.getColumnCount();

                Vector<String> headers = new Vector<>();
                for (int i = 1; i <= columns; i++) {
                    headers.add(meta.getColumnLabel(i));
                }
                Vector<Vector<Object>> rows = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= columns; i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
                model.setDataVector(rows, headers);
            }
        }
    }
}