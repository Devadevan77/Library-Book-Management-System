import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class ReportsPanel extends JPanel {

    public static final String ISSUED_SQL = """
            SELECT t.transaction_id AS ID, b.title AS Book, m.name AS Borrower,
                   t.issue_date AS Issued, t.due_date AS Due
            FROM transactions t
            JOIN books b ON t.book_id = b.book_id
            JOIN members m ON t.member_id = m.member_id
            WHERE t.return_date IS NULL
            ORDER BY t.due_date
            """;

    private static final String AVAILABLE_SQL = """
            SELECT book_id AS ID, title AS Title, author AS Author,
                   category AS Category, available_copies AS Available
            FROM books
            WHERE available_copies > 0
            ORDER BY title
            """;

    private final DefaultTableModel availableModel = Theme.model();
    private final DefaultTableModel issuedModel = Theme.model();

    public ReportsPanel() {
        setLayout(new GridLayout(2, 1, 0, 15));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        add(section("Available Books", availableModel));
        add(section("Currently Issued Books", issuedModel));
    }

    private JPanel section(String title, DefaultTableModel model) {
        JPanel p = Theme.panel(new BorderLayout(0, 8));
        p.add(Theme.heading(title), BorderLayout.NORTH);
        p.add(Theme.scroll(Theme.table(model)), BorderLayout.CENTER);
        return p;
    }

    public void refresh() {
        try {
            DBConnection.loadTable(availableModel, AVAILABLE_SQL);
            DBConnection.loadTable(issuedModel, ISSUED_SQL);
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }
}