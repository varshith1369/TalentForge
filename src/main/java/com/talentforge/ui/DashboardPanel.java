package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

/**
 * An extraordinary, modern Dashboard featuring:
 * - A dynamic glassmorphism welcome banner
 * - Polished stat cards with trend indicators
 * - Interactive module cards with hover effects and detailed descriptions
 */
public class DashboardPanel extends JPanel {

    public interface OnModuleClick {
        void onModuleClick(String moduleKey);
    }

    private OnModuleClick onModuleClick;
    private final String userFirstName;
    private CircularProgressRing resumeScoreRing;
    private JLabel resumeScoreValue;
    private JLabel resumeScoreTrend;

    public DashboardPanel(String userFullName) {
        this.userFirstName = (userFullName == null || userFullName.isBlank())
                ? "there" : userFullName.trim().split("\\s+")[0];

        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildWelcomeBanner());
        content.add(Box.createVerticalStrut(28));
        content.add(sectionLabel("Your Progress"));
        content.add(Box.createVerticalStrut(12));
        content.add(buildStatRow());
        content.add(Box.createVerticalStrut(32));
        content.add(sectionLabel("Continue Preparing"));
        content.add(Box.createVerticalStrut(12));
        content.add(buildModuleGrid());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void setOnModuleClick(OnModuleClick callback) {
        this.onModuleClick = callback;
    }

    // ── 1. Glassmorphism Welcome Banner ────────────────────────────────────────

    private JPanel buildWelcomeBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Rich Multi-stop Gradient
                LinearGradientPaint gradient = new LinearGradientPaint(
                        0, 0, getWidth(), getHeight(),
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{new Color(79, 70, 229), new Color(124, 58, 237), new Color(219, 39, 119)}
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Abstract geometric glass shapes
                g2.setColor(new Color(255, 255, 255, 15));
                
                // Big circle top right
                g2.fillOval(getWidth() - 150, -80, 250, 250);
                
                // Medium circle bottom right
                g2.fillOval(getWidth() - 300, getHeight() - 50, 180, 180);
                
                // Wave pattern
                Path2D wave = new Path2D.Double();
                wave.moveTo(0, getHeight());
                wave.curveTo(getWidth() * 0.25, getHeight() - 60,
                             getWidth() * 0.5, getHeight() + 20,
                             getWidth() * 0.8, getHeight() - 80);
                wave.lineTo(getWidth(), getHeight() - 40);
                wave.lineTo(getWidth(), getHeight());
                wave.closePath();
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fill(wave);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));
        banner.setMaximumSize(new Dimension(2000, 140));
        banner.setPreferredSize(new Dimension(100, 140));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel titleText = new JLabel("Keep pushing your limits, " + userFirstName + "!");
        titleText.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleText.setForeground(Color.WHITE);
        // Soft drop shadow on text via a custom UI or simply a layered approach. We'll rely on the bold white against dark gradient.
        
        JLabel subtitle = new JLabel("You're building real placement-ready skills. Let's make today count.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(255, 255, 255, 220));
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        textCol.add(titleText);
        textCol.add(subtitle);

        banner.add(textCol, BorderLayout.WEST);
        return banner;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Theme.PRIMARY_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ── 2. Stat Row ───────────────────────────────────────────────────────────

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row.setPreferredSize(new Dimension(10, 130));

        row.add(statCard(Icons.code(Color.WHITE), new Color(56, 189, 248), "0", "Problems Solved", ""));
        row.add(statCard(Icons.brain(Color.WHITE), new Color(139, 92, 246), "0", "Aptitude Score", ""));
        int initialScore = UserProfileCache.getResumeScore();
        resumeScoreRing = new CircularProgressRing(initialScore, new Color(244, 114, 182), 55);
        row.add(buildResumeStatCard(initialScore));
        row.add(statCard(Icons.video(Color.WHITE), new Color(52, 211, 153), "0", "Mock Interviews", ""));

        return row;
    }

    private ElevatedCard buildResumeStatCard(int score) {
        ElevatedCard card = new ElevatedCard(null);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            card.getBorder(), BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel ringWrap = new JPanel(new GridBagLayout());
        ringWrap.setOpaque(false);
        ringWrap.add(resumeScoreRing);

        JPanel textCol = new JPanel(new GridLayout(3, 1, 0, 0));
        textCol.setOpaque(false);

        JLabel label = new JLabel("Resume Score");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Theme.MUTED_TEXT);
        label.setVerticalAlignment(SwingConstants.BOTTOM);

        resumeScoreValue = new JLabel(score > 0 ? String.valueOf(score) : "\u2014");
        resumeScoreValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        resumeScoreValue.setForeground(Theme.PRIMARY_TEXT);
        resumeScoreValue.setVerticalAlignment(SwingConstants.CENTER);

        resumeScoreTrend = trendBadge("Run Check \u2192", new Color(244, 114, 182));
        resumeScoreTrend.setVerticalAlignment(SwingConstants.TOP);

        textCol.add(label);
        textCol.add(resumeScoreValue);
        textCol.add(resumeScoreTrend);

        card.add(ringWrap, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);
        return card;
    }

    public void updateResumeScore(int score) {
        // Persist to DB and Cache
        UserProfileCache.setResumeScore(score);
        StatsService.saveResumeScore(UserProfileCache.getCurrentUserId(), score);

        SwingUtilities.invokeLater(() -> {
            resumeScoreRing.setPercent(score);
            resumeScoreValue.setText(String.valueOf(score));
            resumeScoreTrend.setText("Updated \u2713");
            resumeScoreTrend.setForeground(new Color(22, 163, 74)); // Green check
        });
    }

    private ElevatedCard statCard(Icon icon, Color color, String valueStr, String labelStr, String trendStr) {
        ElevatedCard card = new ElevatedCard(null);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            card.getBorder(), BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        IconBadge badge = new IconBadge(icon, color, color.darker(), false, 55);
        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        JPanel textCol = new JPanel(new GridLayout(3, 1, 0, 0));
        textCol.setOpaque(false);

        JLabel label = new JLabel(labelStr);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Theme.MUTED_TEXT);
        label.setVerticalAlignment(SwingConstants.BOTTOM);

        JLabel value = new JLabel(valueStr);
        value.setFont(new Font("Segoe UI", Font.BOLD, 22));
        value.setForeground(Theme.PRIMARY_TEXT);
        value.setVerticalAlignment(SwingConstants.CENTER);

        JLabel trend = trendBadge(trendStr, color);
        trend.setVerticalAlignment(SwingConstants.TOP);

        textCol.add(label);
        textCol.add(value);
        textCol.add(trend);

        card.add(badgeWrap, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);
        return card;
    }

    private JLabel trendBadge(String text, Color color) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(color);
        return badge;
    }

    // ── 3. Interactive Module Grid ────────────────────────────────────────────

    private JPanel buildModuleGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 20, 20));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        grid.setPreferredSize(new Dimension(10, 380));

        grid.add(new InteractiveModuleCard("coding", Icons.code(Color.WHITE), new Color(56, 189, 248), 
            "Coding Practice", "Master algorithms & data structures"));
        grid.add(new InteractiveModuleCard("aptitude", Icons.brain(Color.WHITE), new Color(139, 92, 246), 
            "Aptitude Practice", "Enhance logical & quant reasoning"));
        grid.add(new InteractiveModuleCard("resume", Icons.document(Color.WHITE), new Color(244, 114, 182), 
            "Resume Checker", "Get your ATS score & feedback"));
        grid.add(new InteractiveModuleCard("interview", Icons.video(Color.WHITE), new Color(52, 211, 153), 
            "Mock Interview", "Practice with AI feedback"));
        grid.add(new InteractiveModuleCard("companies", Icons.building(Color.WHITE), new Color(251, 191, 36), 
            "Company Prep", "Company-specific interview guides"));
        grid.add(new InteractiveModuleCard("notes", Icons.document(Color.WHITE), new Color(96, 165, 250), 
            "Notes & Revision", "Quick references & cheat sheets"));
        grid.add(new InteractiveModuleCard("planner", Icons.calendar(Color.WHITE), new Color(167, 139, 250), 
            "Study Planner", "Track and organize your prep"));
        grid.add(new InteractiveModuleCard("leaderboard", Icons.trophy(Color.WHITE), new Color(248, 113, 113), 
            "Leaderboard", "See where you stand globally"));

        return grid;
    }

    private class InteractiveModuleCard extends ElevatedCard {
        private boolean hovered = false;
        private final Color baseColor;

        public InteractiveModuleCard(String key, Icon icon, Color color, String title, String subtitle) {
            super(null);
            this.baseColor = color;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Extra padding inside the card
            setBorder(BorderFactory.createCompoundBorder(
                getBorder(), BorderFactory.createEmptyBorder(20, 16, 20, 16)));

            IconBadge badge = new IconBadge(icon, color, color.darker(), false, 48);
            badge.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel titleLabel = new JLabel("<html><body>" + title + "</body></html>");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLabel.setForeground(Theme.PRIMARY_TEXT);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel subLabel = new JLabel("<html><body style='width: 140px'>" + subtitle + "</body></html>");
            subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subLabel.setForeground(Theme.MUTED_TEXT);
            subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel arrow = new JLabel("\u2192");
            arrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
            arrow.setForeground(new Color(0, 0, 0, 0)); // Hidden by default
            arrow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.add(badge, BorderLayout.WEST);
            topRow.add(arrow, BorderLayout.EAST);
            topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(topRow);
            add(Box.createVerticalStrut(16));
            add(titleLabel);
            add(Box.createVerticalStrut(6));
            add(subLabel);
            add(Box.createVerticalGlue());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    arrow.setForeground(baseColor);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    arrow.setForeground(new Color(0, 0, 0, 0));
                    repaint();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onModuleClick != null) onModuleClick.onModuleClick(key);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Draws the shadow and default white bg
            if (hovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw a very faint overlay of the base color
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 12));
                // We must match the ARC and bounds of ElevatedCard
                int SHADOW_SIZE = 8;
                int ARC = 14;
                int w = getWidth() - SHADOW_SIZE * 2;
                int h = getHeight() - SHADOW_SIZE * 2;
                g2.fillRoundRect(SHADOW_SIZE, SHADOW_SIZE, w, h, ARC, ARC);
                
                // Draw an accent border outline
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(SHADOW_SIZE, SHADOW_SIZE, w, h, ARC, ARC);
                
                g2.dispose();
            }
        }
    }
}