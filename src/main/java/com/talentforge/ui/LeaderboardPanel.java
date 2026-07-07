package com.talentforge.ui;

import com.talentforge.db.StatsService;
import com.talentforge.db.StatsService.UserRank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Leaderboard Panel.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>PriorityQueue&lt;UserRank&gt;</b> — Max-heap ranking system. Surfaces the top
 *       students in order by poll() operations to render the leaderboard list.</li>
 * </ul>
 */
public class LeaderboardPanel extends JPanel {

    /* ============================================================ */
    /*  DATA STRUCTURES                                             */
    /* ============================================================ */
    private final PriorityQueue<UserRank> rankQueue = new PriorityQueue<>((a, b) -> Integer.compare(b.getScore(), a.getScore()));

    /* ============================================================ */
    /*  UI REFERENCES                                               */
    /* ============================================================ */
    private final JPanel leaderboardListPanel = new JPanel();
    private final JPanel podiumPanel = new JPanel();

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        loadRankings();
        buildUI();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    private void loadRankings() {
        rankQueue.clear();

        // Load all users from SQLite DB
        List<UserRank> dbRanks = StatsService.loadAllUserRanks();
        for (UserRank ur : dbRanks) {
            rankQueue.offer(ur);
        }

        // Add some premium mock contestants if DB is small to make the competition exciting
        if (dbRanks.size() < 4) {
            rankQueue.offer(new UserRank(-10, "Priya Sharma", 85, 95, 12, 92));
            rankQueue.offer(new UserRank(-20, "Rahul Verma", 72, 88, 10, 85));
            rankQueue.offer(new UserRank(-30, "Aarav Mehta", 60, 90, 8, 80));
            rankQueue.offer(new UserRank(-40, "Sneha Iyer", 95, 98, 15, 96));
        }
    }

    /* ============================================================ */
    /*  UI ASSEMBLY                                                 */
    /* ============================================================ */
    private void buildUI() {
        // TOP Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JLabel title = new JLabel("🏆 Leaderboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitle = new JLabel("See how you rank against other peers based on coding solved, aptitude scores, mock interviews, and resume quality.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        JButton refreshBtn = createGradientButton("↻ Refresh", 100, 34);
        refreshBtn.addActionListener(e -> refreshAll());

        header.add(left, BorderLayout.CENTER);
        header.add(refreshBtn, BorderLayout.EAST);

        // Core Layout
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Podium view
        podiumPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 0));
        podiumPanel.setOpaque(false);
        buildPodium();

        mainContent.add(podiumPanel);
        mainContent.add(Box.createVerticalStrut(24));

        // Standings List
        leaderboardListPanel.setLayout(new BoxLayout(leaderboardListPanel, BoxLayout.Y_AXIS));
        leaderboardListPanel.setOpaque(false);
        rebuildRankingList();

        JScrollPane scroll = new JScrollPane(leaderboardListPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        mainContent.add(scroll);

        add(header, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private void buildPodium() {
        podiumPanel.removeAll();

        // Extract Top 3 from PriorityQueue copy
        PriorityQueue<UserRank> temp = new PriorityQueue<>(rankQueue);
        UserRank first = temp.poll();
        UserRank second = temp.poll();
        UserRank third = temp.poll();

        if (second != null) {
            podiumPanel.add(buildPodiumCard(second, "2nd", new Color(192, 192, 192), 120));
        }
        if (first != null) {
            podiumPanel.add(buildPodiumCard(first, "1st", new Color(255, 215, 0), 140));
        }
        if (third != null) {
            podiumPanel.add(buildPodiumCard(third, "3rd", new Color(205, 127, 50), 120));
        }

        podiumPanel.revalidate();
        podiumPanel.repaint();
    }

    private JPanel buildPodiumCard(UserRank rank, String rankText, Color medalColor, int height) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        ElevatedCard card = new ElevatedCard(new BorderLayout());
        card.setAccentColor(medalColor);
        card.setPreferredSize(new Dimension(160, height));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel badge = new JLabel(rankText);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        badge.setForeground(medalColor);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(rank.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Theme.PRIMARY_TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel score = new JLabel(rank.getScore() + " pts");
        score.setFont(new Font("Segoe UI", Font.BOLD, 12));
        score.setForeground(Theme.MUTED_TEXT);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(badge);
        inner.add(Box.createVerticalStrut(8));
        inner.add(name);
        inner.add(Box.createVerticalStrut(4));
        inner.add(score);

        card.add(inner, BorderLayout.CENTER);
        container.add(card, BorderLayout.CENTER);

        return container;
    }

    private void rebuildRankingList() {
        leaderboardListPanel.removeAll();

        // Extract all elements from PriorityQueue copy
        PriorityQueue<UserRank> temp = new PriorityQueue<>(rankQueue);
        int rank = 1;

        while (!temp.isEmpty()) {
            UserRank ur = temp.poll();
            leaderboardListPanel.add(buildRankRow(ur, rank++));
            leaderboardListPanel.add(Box.createVerticalStrut(8));
        }

        leaderboardListPanel.revalidate();
        leaderboardListPanel.repaint();
    }

    private ElevatedCard buildRankRow(UserRank rank, int rankNum) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(16, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Rank badge
        JLabel rankLbl = new JLabel(String.valueOf(rankNum));
        rankLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        rankLbl.setForeground(rankNum == 1 ? new Color(255, 215, 0) : rankNum == 2 ? new Color(192, 192, 192) : rankNum == 3 ? new Color(205, 127, 50) : Theme.PRIMARY_TEXT);
        rankLbl.setPreferredSize(new Dimension(30, 20));

        // Name
        JLabel nameLbl = new JLabel(rank.name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(Theme.PRIMARY_TEXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(rankLbl);
        left.add(nameLbl);

        // Stats summary
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        right.setOpaque(false);

        JLabel cod = new JLabel("💻 " + rank.problemsSolved);
        cod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cod.setForeground(Theme.MUTED_TEXT);

        JLabel apt = new JLabel("🧠 " + rank.aptitudeScore + "%");
        apt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        apt.setForeground(Theme.MUTED_TEXT);

        JLabel res = new JLabel("📄 " + rank.resumeScore + "%");
        res.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        res.setForeground(Theme.MUTED_TEXT);

        JLabel total = new JLabel(rank.getScore() + " pts");
        total.setFont(new Font("Segoe UI", Font.BOLD, 13));
        total.setForeground(Theme.PRIMARY_START);

        right.add(cod);
        right.add(apt);
        right.add(res);
        right.add(total);

        inner.add(left, BorderLayout.CENTER);
        inner.add(right, BorderLayout.EAST);
        card.add(inner, BorderLayout.CENTER);

        return card;
    }

    private void refreshAll() {
        loadRankings();
        buildPodium();
        rebuildRankingList();
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        refreshAll();
    }

    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
