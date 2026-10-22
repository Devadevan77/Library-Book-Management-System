import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = Theme.panel(cards);

    private final BookPanel bookPanel = new BookPanel();
    private final MemberPanel memberPanel = new MemberPanel();
    private final IssueReturnPanel issuePanel = new IssueReturnPanel();
    private final ReportsPanel reportsPanel = new ReportsPanel();

    public MainFrame() {
        super("Library Book Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel banner = new JLabel("Library Book Management System");
        banner.setFont(Theme.TITLE_FONT);
        banner.setForeground(Theme.CREAM);
        banner.setBackground(Theme.GREEN);
        banner.setOpaque(true);
        banner.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        add(banner, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 10));
        nav.setBackground(Theme.GREEN);
        addNav(nav, "Books", "books", bookPanel::refresh);
        addNav(nav, "Members", "members", memberPanel::refresh);
        addNav(nav, "Issue / Return", "issue", issuePanel::refresh);
        addNav(nav, "Reports", "reports", reportsPanel::refresh);

        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.setBackground(Theme.GREEN);
        navWrap.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        navWrap.add(nav, BorderLayout.NORTH);
        add(navWrap, BorderLayout.WEST);

        content.add(bookPanel, "books");
        content.add(memberPanel, "members");
        content.add(issuePanel, "issue");
        content.add(reportsPanel, "reports");
        add(content, BorderLayout.CENTER);

        bookPanel.refresh();
        cards.show(content, "books");
    }

    private void addNav(JPanel nav, String text, String cardName, Runnable refresh) {
        JButton b = Theme.button(text);
        b.setBackground(Theme.GREEN_LIGHT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setPreferredSize(new Dimension(170, 44));
        b.addActionListener(e -> {
            refresh.run();
            cards.show(content, cardName);
        });
        nav.add(b);
    }
}