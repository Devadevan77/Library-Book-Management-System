import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MemberPanel extends JPanel {

    private final JTextField nameField = Theme.field();
    private final JTextField emailField = Theme.field();
    private final JTextField phoneField = Theme.field();
    private final DefaultTableModel model = Theme.model();
    private final JTable table = Theme.table(model);

    public MemberPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel form = Theme.panel(new GridLayout(2, 4, 10, 8));
        form.add(Theme.label("Name *"));  form.add(nameField);
        form.add(Theme.label("Email"));   form.add(emailField);
        form.add(Theme.label("Phone"));   form.add(phoneField);
        form.add(Theme.label(""));        form.add(Theme.label(""));

        JButton registerBtn = Theme.button("Register Member");
        JButton clearBtn = Theme.button("Clear Form");
        registerBtn.addActionListener(e -> registerMember());
        clearBtn.addActionListener(e -> clearForm());
        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(registerBtn);
        buttons.add(clearBtn);

        JPanel formArea = Theme.panel(new BorderLayout(0, 10));
        formArea.add(form, BorderLayout.NORTH);
        formArea.add(buttons, BorderLayout.CENTER);

        JPanel top = Theme.panel(new BorderLayout(0, 10));
        top.add(Theme.heading("Library Members"), BorderLayout.NORTH);
        top.add(formArea, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
    }

    public void refresh() {
        try {
            DBConnection.loadTable(model,
                    "SELECT member_id AS ID, name AS Name, email AS Email, phone AS Phone, "
                  + "joined_date AS Joined FROM members ORDER BY member_id");
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }

    private void registerMember() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            Theme.info(this, "Member name is required.");
            return;
        }
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            Theme.info(this, "Please enter a valid email address.");
            return;
        }
        if (!phone.isEmpty() && !phone.matches("[0-9+\\- ]{7,15}")) {
            Theme.info(this, "Phone should contain 7-15 digits (spaces, + and - allowed).");
            return;
        }

        String sql = "INSERT INTO members (name, email, phone, joined_date) VALUES (?, ?, ?, CURDATE())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email.isEmpty() ? null : email);
            ps.setString(3, phone.isEmpty() ? null : phone);
            ps.executeUpdate();
            Theme.info(this, "Member registered.");
            clearForm();
            refresh();
        } catch (SQLException e) {
            Theme.showError(this, e);
        }
    }
}