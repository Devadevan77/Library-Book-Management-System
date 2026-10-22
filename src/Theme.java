import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLIntegrityConstraintViolationException;

public class Theme {

    public static final Color GREEN       = new Color(34, 87, 60);
    public static final Color GREEN_LIGHT = new Color(67, 120, 88);
    public static final Color CREAM       = new Color(250, 245, 230);
    public static final Color CREAM_DARK  = new Color(238, 228, 200);
    public static final Color TEXT        = new Color(45, 45, 35);

    public static final Font TITLE_FONT   = new Font("Serif", Font.BOLD, 26);
    public static final Font HEADING_FONT = new Font("Serif", Font.BOLD, 20);
    public static final Font BODY_FONT    = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BOLD_FONT    = new Font("SansSerif", Font.BOLD, 13);

    public static JPanel panel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CREAM);
        return p;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BODY_FONT);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING_FONT);
        l.setForeground(GREEN);
        return l;
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(BODY_FONT);
        Border line = BorderFactory.createLineBorder(GREEN_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return f;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(BOLD_FONT);
        b.setBackground(GREEN);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** A table model whose cells cannot be edited by typing. */
    public static DefaultTableModel model() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(BODY_FONT);
        t.setRowHeight(26);
        t.setGridColor(CREAM_DARK);
        t.setSelectionBackground(GREEN_LIGHT);
        t.setSelectionForeground(Color.WHITE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader h = t.getTableHeader();
        h.setBackground(GREEN);
        h.setForeground(Color.WHITE);
        h.setFont(BOLD_FONT);
        return t;
    }

    public static JScrollPane scroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(CREAM);
        sp.setBorder(BorderFactory.createLineBorder(GREEN_LIGHT));
        return sp;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Library", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, Exception e) {
        String message;
        if (e instanceof SQLIntegrityConstraintViolationException) {
            message = "This record already exists (duplicate ISBN or email), or it is linked to other "
                    + "records (for example, a book or member with issue history cannot be deleted).";
        } else {
            message = "Database error: " + e.getMessage();
        }
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}