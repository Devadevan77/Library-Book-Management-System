import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class IssueReturnPanel extends JPanel {

    private record Item(int id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private final JComboBox<Item> memberBox = new JComboBox<>();
    private final JComboBox<Item> bookBox = new JComboBox<>();
    private final DefaultTableModel model = Theme.model();
    private final JTable table = Theme.table(model);

    public IssueReturnPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        memberBox.setFont(Theme.BODY_FONT);
        bookBox.setFont(Theme.BODY_FONT);
        memberBox.setPreferredSize(new Dimension(220, 32));
        bookBox.setPreferredSize(new Dimension(260, 32));

        JButton issueBtn = Theme.button("Issue Book");
        issueBtn.addActionListener(e -> issueBook());

        JPanel form = Theme.panel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        form.add(Theme.label("Member:"));
        form.add(memberBox);
        form.add(Theme.label("Book:"));
        form.add(bookBox);
        form.add(issueBtn);
        form.add(Theme.label("(due date = 14 days from today)"));

        JPanel top = Theme.panel(new BorderLayout(0, 10));
        top.add(Theme.heading("Issue & Return Books"), BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        JButton returnBtn = Theme.button("Return Selected Book");
        returnBtn.addActionListener(e -> returnBook());
        JPanel bottom = Theme.panel(new BorderLayout(0, 8));
        bottom.add(Theme.label("Select a row below, then click the button to return that book:"), BorderLayout.NORTH);
        bottom.add(returnBtn, BorderLayout.WEST);

        add(top, BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Reloads the member list, the available-book list and the issued-books table. */
    public void refresh() {
        try (Connection con = DBConnection.getConnection()) {
            memberBox.removeAllItems();
            try (PreparedStatement ps = con.prepareStatement("SELECT member_id, name FROM members ORDER BY name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    memberBox.addItem(new Item(rs.getInt(1), rs.getString(2) + " (#" + rs.getInt(1) + ")"));
                }
            }
            bookBox.removeAllItems();
            try (PreparedStatement ps = con.prepareStatement(
                         "SELECT book_id, title, available_copies FROM books WHERE available_copies > 0 ORDER BY title");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookBox.addItem(new Item(rs.getInt(1), rs.getString(2) + " (" + rs.getInt(3) + " available)"));
                }
            }
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
        try {
            DBConnection.loadTable(model, ReportsPanel.ISSUED_SQL);
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }

    private void issueBook() {
        Item member = (Item) memberBox.getSelectedItem();
        Item book = (Item) bookBox.getSelectedItem();
        if (member == null || book == null) {
            Theme.info(this, "Please select a member and an available book.");
            return;
        }
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(14);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Step 1: reduce stock only if a copy is still available
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET available_copies = available_copies - 1 "
                      + "WHERE book_id = ? AND available_copies > 0")) {
                    ps.setInt(1, book.id());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        Theme.info(this, "Sorry, this book is no longer available.");
                        refresh();
                        return;
                    }
                }
                // Step 2: record the transaction
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO transactions (book_id, member_id, issue_date, due_date) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, book.id());
                    ps.setInt(2, member.id());
                    ps.setDate(3, Date.valueOf(issueDate));
                    ps.setDate(4, Date.valueOf(dueDate));
                    ps.executeUpdate();
                }
                con.commit();
                Theme.info(this, "Book issued successfully.\nDue date: " + dueDate);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
        refresh();
    }

    private void returnBook() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Theme.info(this, "Select an issued book from the table first.");
            return;
        }
        int transactionId = ((Number) model.getValueAt(row, 0)).intValue();

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Step 1: mark as returned only if it has not been returned yet
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE transactions SET return_date = ? WHERE transaction_id = ? AND return_date IS NULL")) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, transactionId);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        Theme.info(this, "This book has already been returned.");
                        refresh();
                        return;
                    }
                }
                // Step 2: put the copy back in stock
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET available_copies = available_copies + 1 "
                      + "WHERE book_id = (SELECT book_id FROM transactions WHERE transaction_id = ?)")) {
                    ps.setInt(1, transactionId);
                    ps.executeUpdate();
                }
                con.commit();
                Theme.info(this, "Book returned successfully.");
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
        refresh();
    }
}