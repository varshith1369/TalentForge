package com.talentforge.ui;

import com.talentforge.db.SkillTrackerService;
import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Analytics Dashboard module — aggregates all user stats into category
 * summaries and uses a PriorityQueue to surface the weakest skills that
 * need the most improvement.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>HashMap&lt;String, Integer&gt;</b> — tallies stats by category
 *       (e.g. "Coding" → problemsSolved). O(1) lookups for category scores.</li>
 *   <li><b>PriorityQueue&lt;SkillEntry&gt;</b> — min-heap ordered by skill level.
 *       {@code poll()} yields the weakest skills that need improvement first.</li>
 * </ul>
 */
public class AnalyticsPanel extends JPanel {

    // ====================== DATA STRUCTURES ======================

    /** HashMap — category name → score. O(1) lookup for tallied stats. */
    private final HashMap<String, Integer> categoryStats = new HashMap<>();

    /** PriorityQueue — min-heap so poll() gives weakest skill first. */
    private PriorityQueue<SkillEntry> weakestSkillsQueue;

    /** Holds all skills for the bar chart (sorted). */
    private final List<SkillEntry> allSkillsSorted = new ArrayList<>();

    // ====================== UI REFERENCES ========================
    private final JPanel categoryCardsPanel = new JPanel();
    private final JPanel weakestSkillsPanel = new JPanel();
    private final JPanel barChartPanel = new JPanel();
    private final JPanel recommendationsPanel = new JPanel();
    private JPanel donutContainer;
    private DonutProgressChart donutChart;

    // Category accent colours
    private static final Color COLOR_CODING    = new Color(99, 102, 241);
    private static final Color COLOR_APTITUDE  = new Color(236, 72, 153);
    private static final Color COLOR_INTERVIEW = new Color(14, 165, 233);
    private static final Color COLOR_COMPANIES = new Color(245, 158, 11);
    private static final Color COLOR_RESUME    = new Color(34, 197, 94);

    /** Lightweight record used in the PriorityQueue. */
    public static class SkillEntry implements Comparable<SkillEntry> {
        public final String name;
        public final int level;

        public SkillEntry(String name, int level) {
            this.name = name;
            this.level = level;
        }

        @Override
        public int compareTo(SkillEntry o) {
            return Integer.compare(this.level, o.level); // min-heap
        }
    }

    public AnalyticsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        loadData();

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        Theme.addListener(this::onThemeChanged);
    }

    /* ================================================================ */
    /*  DATA LOADING                                                    */
    /* ================================================================ */

    private void loadData() {
        int userId = UserProfileCache.getCurrentUserId();

        // ---- HashMap — tally stats by category ----
        categoryStats.clear();
        categoryStats.put("Coding", UserProfileCache.getProblemsSolved());
        categoryStats.put("Aptitude", UserProfileCache.getAptitudeScore());
        categoryStats.put("Interviews", UserProfileCache.getMockInterviews());
        categoryStats.put("Companies", UserProfileCache.getCompaniesPrepared());
        categoryStats.put("Resume", UserProfileCache.getResumeScore());

        // ---- PriorityQueue — load skills and insert into min-heap ----
        HashMap<String, Integer> skills = SkillTrackerService.loadSkills(userId);

        weakestSkillsQueue = new PriorityQueue<>();
        allSkillsSorted.clear();

        for (Map.Entry<String, Integer> entry : skills.entrySet()) {
            SkillEntry se = new SkillEntry(entry.getKey(), entry.getValue());
            weakestSkillsQueue.offer(se);            // PriorityQueue insertion
            allSkillsSorted.add(se);
        }

        // Sort allSkillsSorted by level ascending (for bar chart)
        allSkillsSorted.sort(Comparator.comparingInt(a -> a.level));
    }

    /* ================================================================ */
    /*  MAIN CONTENT                                                    */
    /* ================================================================ */

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));

        root.add(buildHeader());
        root.add(Box.createVerticalStrut(20));
        root.add(buildCategoryCardsSection());
        root.add(Box.createVerticalStrut(20));

        // Two-column layout: Weakest Skills (left) + Donut (right)
        root.add(buildMiddleRow());
        root.add(Box.createVerticalStrut(20));
        root.add(buildBarChartSection());
        root.add(Box.createVerticalStrut(20));
        root.add(buildRecommendationsSection());

        return root;
    }

    /* ================================================================ */
    /*  HEADER                                                          */
    /* ================================================================ */

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("📈  Analytics Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel sub = new JLabel("Comprehensive overview of your placement readiness");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED_TEXT);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        JButton refreshBtn = createGradientButton("↻  Refresh", 100, 34);
        refreshBtn.addActionListener(e -> refreshAll());

        header.add(left, BorderLayout.CENTER);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    /* ================================================================ */
    /*  CATEGORY STAT CARDS (HashMap display)                           */
    /* ================================================================ */

    private JPanel buildCategoryCardsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel sectionTitle = new JLabel("📊 Category Performance");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(Theme.PRIMARY_TEXT);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        section.add(sectionTitle, BorderLayout.NORTH);

        categoryCardsPanel.setLayout(new GridLayout(1, 5, 12, 0));
        categoryCardsPanel.setOpaque(false);

        rebuildCategoryCards();

        section.add(categoryCardsPanel, BorderLayout.CENTER);
        return section;
    }

    private void rebuildCategoryCards() {
        categoryCardsPanel.removeAll();

        // Read from HashMap — O(1) lookups per category
        categoryCardsPanel.add(buildCatCard("Coding",
                categoryStats.getOrDefault("Coding", 0) + " solved",
                "💻", COLOR_CODING));
        categoryCardsPanel.add(buildCatCard("Aptitude",
                categoryStats.getOrDefault("Aptitude", 0) + "%",
                "🧠", COLOR_APTITUDE));
        categoryCardsPanel.add(buildCatCard("Interviews",
                categoryStats.getOrDefault("Interviews", 0) + " done",
                "🎤", COLOR_INTERVIEW));
        categoryCardsPanel.add(buildCatCard("Companies",
                categoryStats.getOrDefault("Companies", 0) + " prepped",
                "🏢", COLOR_COMPANIES));
        categoryCardsPanel.add(buildCatCard("Resume",
                categoryStats.getOrDefault("Resume", 0) + "%",
                "📄", COLOR_RESUME));

        categoryCardsPanel.revalidate();
        categoryCardsPanel.repaint();
    }

    private ElevatedCard buildCatCard(String name, String value, String emoji, Color accent) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        card.setAccentColor(accent);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(Theme.PRIMARY_TEXT);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLbl.setForeground(Theme.MUTED_TEXT);

        inner.add(emojiLbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(valLbl);
        inner.add(Box.createVerticalStrut(2));
        inner.add(nameLbl);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /* ================================================================ */
    /*  MIDDLE ROW: Weakest Skills (PriorityQueue) + Donut              */
    /* ================================================================ */

    private JPanel buildMiddleRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        row.add(buildWeakestSkillsCard());
        row.add(buildDonutCard());
        return row;
    }

    /** Uses the PriorityQueue — polls bottom-5 weakest skills. */
    private ElevatedCard buildWeakestSkillsCard() {
        ElevatedCard card = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("🎯 Skills to Improve");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        inner.add(title, BorderLayout.NORTH);

        weakestSkillsPanel.setLayout(new BoxLayout(weakestSkillsPanel, BoxLayout.Y_AXIS));
        weakestSkillsPanel.setOpaque(false);

        rebuildWeakestSkills();

        inner.add(weakestSkillsPanel, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void rebuildWeakestSkills() {
        weakestSkillsPanel.removeAll();

        if (weakestSkillsQueue == null || weakestSkillsQueue.isEmpty()) {
            JLabel empty = new JLabel("No skills tracked yet.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Theme.MUTED_TEXT);
            weakestSkillsPanel.add(empty);
        } else {
            // Rebuild a copy of the PriorityQueue so we can poll()
            PriorityQueue<SkillEntry> copy = new PriorityQueue<>(weakestSkillsQueue);
            int shown = 0;
            Color[] barColors = {
                    new Color(239, 68, 68),   // red
                    new Color(245, 158, 11),  // amber
                    new Color(234, 179, 8),   // yellow
                    new Color(14, 165, 233),  // sky
                    new Color(34, 197, 94),   // green
            };

            while (!copy.isEmpty() && shown < 5) {
                SkillEntry entry = copy.poll();  // PriorityQueue poll() — O(log n)
                Color barColor = barColors[Math.min(shown, barColors.length - 1)];
                weakestSkillsPanel.add(buildSkillProgressRow(entry, barColor));
                weakestSkillsPanel.add(Box.createVerticalStrut(8));
                shown++;
            }
        }

        weakestSkillsPanel.revalidate();
        weakestSkillsPanel.repaint();
    }

    private JPanel buildSkillProgressRow(SkillEntry entry, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel name = new JLabel(entry.name);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        name.setForeground(Theme.PRIMARY_TEXT);
        name.setPreferredSize(new Dimension(110, 20));

        // Progress bar
        JPanel barOuter = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                g2.setColor(Theme.isDark()
                        ? new Color(45, 45, 60)
                        : new Color(230, 232, 240));
                g2.fillRoundRect(0, 0, w, h, h, h);

                // Filled
                double pct = entry.level / 10.0;
                int filledW = (int) (w * pct);
                if (filledW > 0) {
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, 0, filledW, h, h, h);
                }

                g2.dispose();
            }
        };
        barOuter.setOpaque(false);
        barOuter.setPreferredSize(new Dimension(0, 14));

        JLabel levelLbl = new JLabel(entry.level + "/10");
        levelLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        levelLbl.setForeground(barColor);
        levelLbl.setPreferredSize(new Dimension(40, 20));
        levelLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(name, BorderLayout.WEST);
        row.add(barOuter, BorderLayout.CENTER);
        row.add(levelLbl, BorderLayout.EAST);
        return row;
    }

    /** Overall readiness donut chart. */
    private ElevatedCard buildDonutCard() {
        ElevatedCard card = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("🏆 Overall Readiness");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        inner.add(title, BorderLayout.NORTH);

        // Compute weighted readiness (skills avg contributes 40%, category stats contribute 60%)
        int readiness = computeOverallReadiness();

        // Build donut segments from category stats
        DonutProgressChart.Segment[] segments = buildDonutSegments();

        Color[] colors = { COLOR_CODING, COLOR_APTITUDE, COLOR_INTERVIEW, COLOR_COMPANIES, COLOR_RESUME };
        String[] labels = { "Coding", "Aptitude", "Interviews", "Companies", "Resume" };
        int[] values = {
                categoryStats.getOrDefault("Coding", 0),
                categoryStats.getOrDefault("Aptitude", 0),
                categoryStats.getOrDefault("Interviews", 0),
                categoryStats.getOrDefault("Companies", 0),
                categoryStats.getOrDefault("Resume", 0)
        };

        donutChart = new DonutProgressChart(segments, readiness);
        donutChart.setPreferredSize(new Dimension(150, 150));

        donutContainer = new JPanel(new BorderLayout());
        donutContainer.setOpaque(false);
        donutContainer.add(donutChart, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setOpaque(false);
        legend.setBorder(new EmptyBorder(8, 0, 0, 0));

        for (int i = 0; i < labels.length; i++) {
            legend.add(buildLegendRow(labels[i], values[i], colors[i]));
            legend.add(Box.createVerticalStrut(4));
        }

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(donutContainer, BorderLayout.CENTER);
        center.add(legend, BorderLayout.SOUTH);

        inner.add(center, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildLegendRow(String label, int value, Color color) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        // Color dot
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(color);

        JLabel nameLbl = new JLabel(label + ": " + value);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLbl.setForeground(Theme.MUTED_TEXT);

        row.add(dot);
        row.add(nameLbl);
        return row;
    }

    private int computeOverallReadiness() {
        // Skills average (out of 10, scaled to 100)
        double skillsAvg = 0;
        if (!allSkillsSorted.isEmpty()) {
            skillsAvg = allSkillsSorted.stream().mapToInt(s -> s.level).average().orElse(0);
            skillsAvg = skillsAvg * 10; // scale to 100
        }

        // Category scores
        int coding = Math.min(100, categoryStats.getOrDefault("Coding", 0) * 2);     // ~50 solved = 100
        int aptitude = Math.min(100, categoryStats.getOrDefault("Aptitude", 0));
        int interviews = Math.min(100, categoryStats.getOrDefault("Interviews", 0) * 10);  // ~10 = 100
        int resume = Math.min(100, categoryStats.getOrDefault("Resume", 0));
        int companies = Math.min(100, categoryStats.getOrDefault("Companies", 0) * 10);    // ~10 = 100

        double catAvg = (coding + aptitude + interviews + resume + companies) / 5.0;

        // Weighted: 40% skills, 60% categories
        return (int) (skillsAvg * 0.4 + catAvg * 0.6);
    }

    /**
     * Build the five DonutProgressChart segments from current category HashMap data.
     * Each segment's percent is clamped to [0, 100].
     */
    private DonutProgressChart.Segment[] buildDonutSegments() {
        int coding    = Math.min(100, categoryStats.getOrDefault("Coding", 0) * 2);
        int aptitude  = Math.min(100, categoryStats.getOrDefault("Aptitude", 0));
        int interviews = Math.min(100, categoryStats.getOrDefault("Interviews", 0) * 10);
        int companies = Math.min(100, categoryStats.getOrDefault("Companies", 0) * 10);
        int resume    = Math.min(100, categoryStats.getOrDefault("Resume", 0));

        return new DonutProgressChart.Segment[] {
            new DonutProgressChart.Segment("Coding",     coding,     COLOR_CODING),
            new DonutProgressChart.Segment("Aptitude",   aptitude,   COLOR_APTITUDE),
            new DonutProgressChart.Segment("Interviews", interviews, COLOR_INTERVIEW),
            new DonutProgressChart.Segment("Companies",  companies,  COLOR_COMPANIES),
            new DonutProgressChart.Segment("Resume",     resume,     COLOR_RESUME),
        };
    }

    /* ================================================================ */
    /*  SKILL DISTRIBUTION BAR CHART                                    */
    /* ================================================================ */

    private JPanel buildBarChartSection() {
        ElevatedCard section = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("📊 Skill Distribution (Weakest First)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        inner.add(title, BorderLayout.NORTH);

        barChartPanel.setLayout(new BoxLayout(barChartPanel, BoxLayout.Y_AXIS));
        barChartPanel.setOpaque(false);

        rebuildBarChart();

        inner.add(barChartPanel, BorderLayout.CENTER);
        section.add(inner, BorderLayout.CENTER);

        // Wrap in a panel with max height
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(section, BorderLayout.CENTER);
        return wrapper;
    }

    private void rebuildBarChart() {
        barChartPanel.removeAll();

        if (allSkillsSorted.isEmpty()) {
            JLabel empty = new JLabel("No skills tracked yet.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Theme.MUTED_TEXT);
            barChartPanel.add(empty);
        } else {
            for (SkillEntry entry : allSkillsSorted) {
                Color barColor = getBarColor(entry.level);
                barChartPanel.add(buildHorizontalBar(entry, barColor));
                barChartPanel.add(Box.createVerticalStrut(6));
            }
        }

        barChartPanel.revalidate();
        barChartPanel.repaint();
    }

    private Color getBarColor(int level) {
        if (level <= 2) return new Color(239, 68, 68);      // red
        if (level <= 4) return new Color(245, 158, 11);      // amber
        if (level <= 6) return new Color(234, 179, 8);       // yellow
        if (level <= 8) return new Color(14, 165, 233);      // sky
        return new Color(34, 197, 94);                        // green
    }

    private JPanel buildHorizontalBar(SkillEntry entry, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel name = new JLabel(entry.name);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        name.setForeground(Theme.PRIMARY_TEXT);
        name.setPreferredSize(new Dimension(120, 20));

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight() - 4;
                int y = 2;

                g2.setColor(Theme.isDark() ? new Color(45, 45, 60) : new Color(230, 232, 240));
                g2.fillRoundRect(0, y, w, h, h, h);

                double pct = entry.level / 10.0;
                int filledW = Math.max(0, (int) (w * pct));
                if (filledW > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, barColor, filledW, 0,
                            barColor.brighter());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, y, filledW, h, h, h);
                }

                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 20));

        JLabel val = new JLabel(entry.level + "/10");
        val.setFont(new Font("Segoe UI", Font.BOLD, 11));
        val.setForeground(barColor);
        val.setPreferredSize(new Dimension(40, 20));
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(name, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    /* ================================================================ */
    /*  RECOMMENDATIONS                                                 */
    /* ================================================================ */

    private JPanel buildRecommendationsSection() {
        ElevatedCard card = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("💡 Recommendations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        inner.add(title, BorderLayout.NORTH);

        recommendationsPanel.setLayout(new BoxLayout(recommendationsPanel, BoxLayout.Y_AXIS));
        recommendationsPanel.setOpaque(false);

        rebuildRecommendations();

        inner.add(recommendationsPanel, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void rebuildRecommendations() {
        recommendationsPanel.removeAll();

        List<String> tips = new ArrayList<>();

        int aptitude = categoryStats.getOrDefault("Aptitude", 0);
        int coding = categoryStats.getOrDefault("Coding", 0);
        int interviews = categoryStats.getOrDefault("Interviews", 0);
        int resume = categoryStats.getOrDefault("Resume", 0);
        int companies = categoryStats.getOrDefault("Companies", 0);

        if (aptitude < 50) {
            tips.add("⚠️  Your aptitude score is below 50%. Practice more quantitative and logical reasoning problems.");
        }
        if (coding < 10) {
            tips.add("⚠️  You've solved fewer than 10 coding problems. Aim for at least 50 to build confidence.");
        }
        if (interviews < 3) {
            tips.add("💬  You've done fewer than 3 mock interviews. Regular practice improves communication skills.");
        }
        if (resume < 60) {
            tips.add("📄  Your resume score is below 60%. Use the Resume Checker module to identify improvements.");
        }
        if (companies < 3) {
            tips.add("🏢  You've prepared for fewer than 3 companies. Broaden your target list for better placement odds.");
        }

        // Skill-specific tips from PriorityQueue
        if (weakestSkillsQueue != null && !weakestSkillsQueue.isEmpty()) {
            PriorityQueue<SkillEntry> copy = new PriorityQueue<>(weakestSkillsQueue);
            SkillEntry weakest = copy.poll();
            if (weakest != null && weakest.level <= 3) {
                tips.add("🎯  Your weakest skill is \"" + weakest.name
                        + "\" at level " + weakest.level + "/10. Focus on improving it first.");
            }
        }

        if (tips.isEmpty()) {
            tips.add("✅  Great job! You're well-prepared across all areas. Keep up the momentum!");
        }

        for (String tip : tips) {
            JLabel lbl = new JLabel(tip);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(Theme.PRIMARY_TEXT);
            lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
            recommendationsPanel.add(lbl);
        }

        recommendationsPanel.revalidate();
        recommendationsPanel.repaint();
    }

    /* ================================================================ */
    /*  REFRESH / THEME                                                 */
    /* ================================================================ */

    public void refreshAll() {
        loadData();
        rebuildCategoryCards();
        rebuildWeakestSkills();
        rebuildBarChart();
        rebuildRecommendations();

        // Update donut
        if (donutChart != null && donutContainer != null) {
            donutContainer.removeAll();
            donutChart = new DonutProgressChart(buildDonutSegments(), computeOverallReadiness());
            donutChart.setPreferredSize(new Dimension(150, 150));
            donutContainer.add(donutChart, BorderLayout.CENTER);
            donutContainer.revalidate();
            donutContainer.repaint();
        }
    }

    private void onThemeChanged() {
        setBackground(Theme.PAGE_BG);
        SwingUtilities.invokeLater(() -> {
            rebuildCategoryCards();
            rebuildWeakestSkills();
            rebuildBarChart();
            rebuildRecommendations();
            revalidate();
            repaint();
        });
    }

    /* ================================================================ */
    /*  UTILITY                                                         */
    /* ================================================================ */

    private JButton createGradientButton(String text, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, Theme.PRIMARY_START,
                        getWidth(), 0, Theme.PRIMARY_END);
                g2.setPaint(gp);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
