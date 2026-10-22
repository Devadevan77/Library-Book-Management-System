import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookPanel extends JPanel {

    private record BookInput(String title, String author, String isbn, String category, int copies) { }

    private static final String SELECT_BOOKS = """
            SELECT book_id AS ID, title AS Title, author AS Author, isbn AS ISBN,
                   category AS Category, total_copies AS Total, available_copies AS Available
            FROM books
            """;

    private final JTextField titleField = Theme.field();
    private final JTextField authorField = Theme.field();
    private final JTextField isbnField = Theme.field();
    private final JTextField categoryField = Theme.field();
    private final JTextField copiesField = Theme.field();
    private final JTextField searchField = Theme.field();
    private final DefaultTableModel model = Theme.model();
    private final JTable table = Theme.table(model);
    private int selectedId = -1;

    public BookPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Form
        JPanel form = Theme.panel(new GridLayout(3, 4, 10, 8));
        form.add(Theme.label("Title *"));        form.add(titleField);
        form.add(Theme.label("Author *"));       form.add(authorField);
        form.add(Theme.label("ISBN"));           form.add(isbnField);
        form.add(Theme.label("Category"));       form.add(categoryField);
        form.add(Theme.label("Total Copies *")); form.add(copiesField);
        form.add(Theme.label(""));               form.add(Theme.label(""));

        // Buttons
        JButton addBtn = Theme.button("Add Book");
        JButton updateBtn = Theme.button("Update Selected");
        JButton deleteBtn = Theme.button("Delete Selected");
        JButton clearBtn = Theme.button("Clear Form");
        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn.addActionListener(e -> clearForm());
        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        // Search row
        JButton searchBtn = Theme.button("Search");
        JButton showAllBtn = Theme.button("Show All");
        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        showAllBtn.addActionListener(e -> refresh());
        JPanel searchButtons = Theme.panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchButtons.add(searchBtn);
        searchButtons.add(showAllBtn);
        JPanel searchRow = Theme.panel(new BorderLayout(8, 0));
        searchRow.add(Theme.label("Search (title, author, ISBN, category):"), BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchButtons, BorderLayout.EAST);

        JPanel formArea = Theme.panel(new BorderLayout(0, 10));
        formArea.add(form, BorderLayout.NORTH);
        formArea.add(buttons, BorderLayout.CENTER);
        formArea.add(searchRow, BorderLayout.SOUTH);

        JPanel top = Theme.panel(new BorderLayout(0, 10));
        top.add(Theme.heading("Manage Books"), BorderLayout.NORTH);
        top.add(formArea, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
    }

    /** Reloads all books and clears the form. */
    public void refresh() {
        try {
            DBConnection.loadTable(model, SELECT_BOOKS + " ORDER BY book_id");
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
        searchField.setText("");
        clearForm();
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = ((Number) model.getValueAt(row, 0)).intValue();
        titleField.setText(cell(row, 1));
        authorField.setText(cell(row, 2));
        isbnField.setText(cell(row, 3));
        categoryField.setText(cell(row, 4));
        copiesField.setText(cell(row, 5));
    }

    private String cell(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        categoryField.setText("");
        copiesField.setText("");
        selectedId = -1;
        table.clearSelection();
    }

    /** Reads and validates the form. Returns null (after showing a message) if invalid. */
    private BookInput readForm() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String category = categoryField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            Theme.info(this, "Title and author are required.");
            return null;
        }
        int copies;
        try {
            copies = Integer.parseInt(copiesField.getText().trim());
        } catch (NumberFormatException e) {
            Theme.info(this, "Total copies must be a whole number.");
            return null;
        }
        if (copies < 1) {
            Theme.info(this, "Total copies must be at least 1.");
            return null;
        }
        return new BookInput(title, author,
                isbn.isEmpty() ? null : isbn,
                category.isEmpty() ? null : category,
                copies);
    }

    private void addBook() {
        BookInput in = readForm();
        if (in == null) {
            return;
        }
        String sql = "INSERT INTO books (title, author, isbn, category, total_copies, available_copies) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, in.title());
            ps.setString(2, in.author());
            ps.setString(3, in.isbn());
            ps.setString(4, in.category());
            ps.setInt(5, in.copies());
            ps.setInt(6, in.copies());   // a new book starts fully available
            ps.executeUpdate();
            Theme.info(this, "Book added.");
            refresh();
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }

    private void updateBook() {
        if (selectedId < 0) {
            Theme.info(this, "Select a book from the table first.");
            return;
        }
        BookInput in = readForm();
        if (in == null) {
            return;
        }
        // MySQL evaluates SET assignments left to right, so available_copies is changed
        // BEFORE total_copies, while total_copies still holds the old value.
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, category = ?, "
                   + "available_copies = available_copies + (? - total_copies), total_copies = ? "
                   + "WHERE book_id = ? AND available_copies + (? - total_copies) >= 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, in.title());
            ps.setString(2, in.author());
            ps.setString(3, in.isbn());
            ps.setString(4, in.category());
            ps.setInt(5, in.copies());
            ps.setInt(6, in.copies());
            ps.setInt(7, selectedId);
            ps.setInt(8, in.copies());
            if (ps.executeUpdate() == 0) {
                Theme.info(this, "Cannot update: total copies cannot be lower than the number of copies currently issued.");
                return;
            }
            Theme.info(this, "Book updated.");
            refresh();
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }

    private void deleteBook() {
        if (selectedId < 0) {
            Theme.info(this, "Select a book from the table first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete the selected book?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            Theme.info(this, "Book deleted.");
            refresh();
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refresh();
            return;
        }
        String like = "%" + keyword + "%";
        try {
            DBConnection.loadTable(model,
                    SELECT_BOOKS + " WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ? ORDER BY title",
                    like, like, like, like);
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }
}