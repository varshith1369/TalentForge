package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Dashboard — "Student Placement Preparation Hub" layout.
 * All stats, sparklines, milestones and the donut chart are driven by
 * live SQLite data.  A Stack<String>-based undo stack lets the user roll
 * back the last simulated action in real time.
 */
public class DashboardPanel extends JPanel {

    public interface OnModuleClick {
        void onModuleClick(String moduleKey);
    }

    private OnModuleClick onModuleClick;
    private final String userFirstName;

    // ---- Sparkline stat cards (references kept for live updates) ----
    private SparklineStatCard aptitudeCard;
    private SparklineStatCard codingCard;
    private SparklineStatCard mockCard;
    private SparklineStatCard companiesCard;
    private SparklineStatCard offersCard;

    // ---- Donut + bar chart ----
    private DonutProgressChart donutChart;
    private PerformanceBarChart barChart;

    // ---- Welcome labels ----
    private JLabel welcomeTitle;
    private JLabel welcomeSub;

    // ---- Legend labels for real-time updates ----
    private JLabel aptLegendVal;
    private JLabel codLegendVal;
    private JLabel mockLegendVal;
    private JLabel projLegendVal;
    private JLabel otherLegendVal;

    // ---- Milestone rows (real-time checklist) ----
    private static class MilestoneRow extends JPanel {
        private final JLabel iconLbl;
        private final JLabel statusLbl;

        public MilestoneRow(String labelText) {
            setLayout(new BorderLayout(8, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

            iconLbl = new JLabel("○");
            iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            iconLbl.setForeground(Theme.MUTED_TEXT);

            JLabel nameLbl = new JLabel(labelText);
            nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            nameLbl.setForeground(Theme.PRIMARY_TEXT);

            statusLbl = new JLabel("Pending");
            statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            statusLbl.setForeground(Theme.MUTED_TEXT);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            left.setOpaque(false);
            left.add(iconLbl);
            left.add(nameLbl);

            add(left, BorderLayout.CENTER);
            add(statusLbl, BorderLayout.EAST);
        }

        public void updateStatus(boolean done, boolean inProgress, Color activeColor) {
            if (done) {
                iconLbl.setText("✓");
                iconLbl.setForeground(Theme.SUCCESS_COLOR);
                statusLbl.setText("Completed");
                statusLbl.setForeground(Theme.SUCCESS_COLOR);
            } else if (inProgress) {
                iconLbl.setText("◑");
                iconLbl.setForeground(activeColor);
                statusLbl.setText("In Progress");
                statusLbl.setForeground(activeColor);
            } else {
                iconLbl.setText("○");
                iconLbl.setForeground(Theme.MUTED_TEXT);
                statusLbl.setText("Pending");
                statusLbl.setForeground(Theme.MUTED_TEXT);
            }
        }
    }

    private MilestoneRow msAptitudeRow;
    private MilestoneRow msCodingRow;
    private MilestoneRow msMockRow;
    private MilestoneRow msProjectsRow;
    private MilestoneRow msApplyRow;

    // ---- Recent Activity Labels ----
    private JLabel activityCodSub;
    private JLabel activityAptSub;
    private JLabel activityMockSub;
    private JLabel activityResumeSub;
    private JLabel activityCompSub;

    // ---- Rollback ----
    private final Stack<String> actionHistory = new Stack<>();
    private JButton btnRollback;

    // ---- Weekly activity from DB (Mon-Sun, 7 values) ----
    private int[] weeklyData = new int[7]; // loaded fresh each refresh

    // Accent palette
    static final Color C_PURPLE = new Color(130, 80, 220);
    static final Color C_GREEN  = new Color(36,  198, 120);
    static final Color C_BLUE   = new Color(56,  140, 235);
    static final Color C_ORANGE = new Color(245, 148, 55);
    static final Color C_PINK   = new Color(235, 75,  135);

    // Module descriptors
    private static final Object[][] MODULES = {
        {"aptitude",  "Aptitude Practice",  "850+ Questions",       "Practice Now",  C_PURPLE, "\uD83E\uDDE0"},
        {"coding",    "Coding Practice",    "1200+ Problems",       "Start Coding",  C_GREEN,  "\u2328"},
        {"interview", "Mock Interviews",    "AI Mock Interviews",   "Start Mock",    C_BLUE,   "\uD83C\uDFA4"},
        {"resume",    "Resume Checker",     "AI Resume Review",     "Check Now",     C_ORANGE, "\uD83D\uDCC4"},
        {"companies", "Company-wise Prep",  "Top 100+ Companies",   "Explore",       C_PINK,   "\uD83C\uDFE2"},
        {"skills",    "Skill Tracker",      "Track Your Skills",    "Track Now",     C_GREEN,  "\uD83D\uDCCA"},
        {"planner",   "Placement Progress", "Your Progress",        "View Progress", C_PURPLE, "\uD83C\uDFAF"},
        {"notes",     "Job Alerts",         "Latest Opportunities", "View Jobs",     C_ORANGE, "\uD83D\uDD14"},
    };

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DashboardPanel(String userFullName) {
        String first = (userFullName == null || userFullName.isBlank())
                ? "there" : userFullName.trim().split("\\s+")[0];
        this.userFirstName = first;

        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        // Load real weekly data before building UI
        loadWeeklyData();

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(20, 22, 22, 22));

        root.add(buildHeader());
        root.add(vgap(16));
        root.add(buildStatRow());
        root.add(vgap(18));
        root.add(buildMidSection());
        root.add(vgap(18));
        root.add(buildBottomSection());
        root.add(vgap(18));
        root.add(buildSimulatorBar());    // practice simulator + rollback
        root.add(vgap(12));
        root.add(buildRecentActivity());

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refreshAll();
    }

    // ============================================================
    //  PUBLIC API
    // ============================================================

    public void setOnModuleClick(OnModuleClick cb) { this.onModuleClick = cb; }

    public void updateResumeScore(int score) {
        UserProfileCache.setResumeScore(score);
        syncUI();
    }

    /** Full refresh from DB — called on every navigation to dashboard. */
    public void refreshAll() {
        int uid = UserProfileCache.getCurrentUserId();
        if (uid != -1) {
            UserProfileCache.setAptitudeScore(StatsService.loadAptitudeScore(uid));
            UserProfileCache.setProblemsSolved(StatsService.loadProblemsSolved(uid));
            UserProfileCache.setMockInterviews(StatsService.loadMockInterviews(uid));
            UserProfileCache.setResumeScore(StatsService.loadResumeScore(uid));
            UserProfileCache.setCompaniesPrepared(StatsService.loadCompaniesPrepared(uid));
            UserProfileCache.setOffersTarget(StatsService.loadOffersTarget(uid));
            loadWeeklyData();
        }
        SwingUtilities.invokeLater(this::syncUI);
    }

    // ============================================================
    //  INTERNAL — DATA
    // ============================================================

    private void loadWeeklyData() {
        int uid = UserProfileCache.getCurrentUserId();
        if (uid != -1) {
            weeklyData = StatsService.loadWeeklyActivity(uid);
        }
    }

    /** Today's weekday index 0=Mon … 6=Sun */
    private int todayIndex() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); // Sun=1
        return (dow + 5) % 7; // Mon=0
    }

    /** Weekly delta: sum of last 7 days vs prior 7 days (approximate with half-week diff). */
    private int weeklyDelta(int currentValue) {
        // sum of weekly activity vs a simple week-ago estimate
        int todayIdx = todayIndex();
        int thisWeek = 0;
        for (int i = 0; i <= todayIdx; i++) thisWeek += weeklyData[i];
        return thisWeek; // show this-week contribution
    }

    // ============================================================
    //  INTERNAL — SYNC UI
    // ============================================================

    private void syncUI() {
        int apt = UserProfileCache.getAptitudeScore();
        int cod = UserProfileCache.getProblemsSolved();
        int mck = UserProfileCache.getMockInterviews();
        int cmp = UserProfileCache.getCompaniesPrepared();
        int off = UserProfileCache.getOffersTarget();

        // Update sparkline card values and subtitles — 100% from real DB data
        int activityToday = weeklyData[todayIndex()];
        int activityWeek  = 0; for (int d : weeklyData) activityWeek += d;

        if (aptitudeCard  != null) { aptitudeCard.setStatValue(apt);  aptitudeCard.setSubtitle(apt > 0 ? "Score updated" : "Start practising!"); }
        if (codingCard    != null) { codingCard.setStatValue(cod);    codingCard.setSubtitle(activityToday > 0 ? "+" + activityToday + " solved today" : cod > 0 ? "Keep going!" : "No problems yet"); }
        if (mockCard      != null) { mockCard.setStatValue(mck);      mockCard.setSubtitle(mck >= 8 ? "Great Performance \uD83C\uDF1F" : mck >= 5 ? "Good Performance" : mck > 0 ? "Keep Going!" : "No interviews yet"); }
        if (companiesCard != null) { companiesCard.setStatValue(cmp); companiesCard.setSubtitle(cmp > 0 ? cmp + " explored" : "None yet"); }
        if (offersCard    != null) { offersCard.setStatValue(off);    offersCard.setSubtitle(off > 0 ? "Target: " + off : "Set your goal"); }

        // Sparklines use real weekly activity data only
        if (aptitudeCard  != null) aptitudeCard.setSparkData(weeklyData.clone());
        if (codingCard    != null) codingCard.setSparkData(weeklyData.clone());
        if (mockCard      != null) mockCard.setSparkData(buildMockSparkline(mck));

        // Donut
        int overall = computeOverall(apt, cod, mck);
        if (donutChart != null) donutChart.setSegments(buildSegments(apt, cod, mck), overall);

        // Update Legend label percentages in real time — all from real DB data
        int res = UserProfileCache.getResumeScore();
        if (aptLegendVal  != null) aptLegendVal.setText(Math.min(100, apt) + "%");
        if (codLegendVal  != null) codLegendVal.setText(Math.min(100, cod) + "%");
        if (mockLegendVal != null) mockLegendVal.setText(Math.min(100, mck * 10) + "%");
        if (projLegendVal != null) projLegendVal.setText(res > 0 ? res + "%" : "0%"); // Resume Score

        // Bar chart
        if (barChart != null) barChart.updateScores(
            Math.min(100, apt), Math.min(100, cod), Math.min(100, mck * 10));

        // Milestones
        refreshMilestones(apt, cod, mck);

        // Update Recent Activity labels dynamically (res already declared above)
        if (activityCodSub    != null) activityCodSub.setText(cod + " solved total");
        if (activityAptSub    != null) activityAptSub.setText("Score: " + apt + "/100");
        if (activityMockSub   != null) activityMockSub.setText("Completed: " + mck + "/10");
        if (activityResumeSub != null) activityResumeSub.setText(res > 0 ? "Score: " + res + "%" : "Not checked yet");
        if (activityCompSub   != null) activityCompSub.setText(cmp > 0 ? cmp + " companies" : "None yet");

        // Rollback button
        if (btnRollback != null) btnRollback.setEnabled(!actionHistory.isEmpty());

        // Greeting
        if (welcomeTitle != null) {
            int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String g = h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
            welcomeTitle.setText("<html>" + g + ", <b>" + userFirstName + "!</b> \uD83D\uDC4B</html>");
        }
    }

    private int computeOverall(int apt, int cod, int mck) {
        int a = Math.min(100, apt);
        int c = Math.min(100, cod);
        int m = Math.min(100, mck * 10);
        if (a == 0 && c == 0 && m == 0) return 0;
        int count = (a > 0 ? 1 : 0) + (c > 0 ? 1 : 0) + (m > 0 ? 1 : 0);
        return count == 0 ? 0 : (a + c + m) / (count == 0 ? 1 : 3);
    }

    private DonutProgressChart.Segment[] buildSegments(int apt, int cod, int mck) {
        // Only real tracked metrics — no hardcoded percentages
        int res = UserProfileCache.getResumeScore();
        return new DonutProgressChart.Segment[]{
            new DonutProgressChart.Segment("Aptitude",       Math.min(100, apt),        C_PURPLE),
            new DonutProgressChart.Segment("Coding",         Math.min(100, cod),        C_GREEN),
            new DonutProgressChart.Segment("Mock Interview", Math.min(100, mck * 10),   C_BLUE),
            new DonutProgressChart.Segment("Resume",         Math.min(100, res),        C_PINK),
        };
    }

    /**
     * Mock sparkline — flat line showing cumulative mock count rising day by day.
     * Uses only real interview count; no artificial offsets.
     */
    private int[] buildMockSparkline(int mck) {
        // Show cumulative mocks across the week based on weekly activity
        int[] spark = new int[7];
        int cumulative = Math.max(0, mck - 6);
        for (int i = 0; i < 7; i++) {
            if (weeklyData[i] > 0) cumulative++;
            spark[i] = Math.min(mck, cumulative);
        }
        spark[6] = mck;
        return spark;
    }

    private void refreshMilestones(int apt, int cod, int mck) {
        int res = UserProfileCache.getResumeScore();
        if (msAptitudeRow != null) msAptitudeRow.updateStatus(apt >= 70, apt > 0 && apt < 70, C_PURPLE);
        if (msCodingRow   != null) msCodingRow.updateStatus(cod >= 100, cod > 0 && cod < 100, C_GREEN);
        if (msMockRow     != null) msMockRow.updateStatus(mck >= 5,   mck > 0 && mck < 5,   C_BLUE);
        if (msProjectsRow != null) msProjectsRow.updateStatus(res >= 80, res > 0 && res < 80, C_PINK); // Resume Score milestone
        if (msApplyRow    != null) msApplyRow.updateStatus(false, false, Theme.MUTED_TEXT);
    }

    // ============================================================
    //  SIMULATOR + ROLLBACK
    // ============================================================

    private void simulate(String action) {
        int uid = UserProfileCache.getCurrentUserId();
        if (uid == -1) return;

        // Update today's weekly activity
        int today = todayIndex();

        switch (action) {
            case "solve" -> {
                int v = UserProfileCache.getProblemsSolved() + 1;
                UserProfileCache.setProblemsSolved(v);
                StatsService.saveProblemsSolved(uid, v);
                weeklyData[today]++;
                StatsService.saveDailyActivity(uid, today, weeklyData[today]);
            }
            case "aptitude" -> {
                int v = Math.min(100, UserProfileCache.getAptitudeScore() + 5);
                UserProfileCache.setAptitudeScore(v);
                StatsService.saveAptitudeScore(uid, v);
                weeklyData[today] = Math.max(weeklyData[today], v / 10);
                StatsService.saveDailyActivity(uid, today, weeklyData[today]);
            }
            case "mock" -> {
                int v = Math.min(10, UserProfileCache.getMockInterviews() + 1);
                UserProfileCache.setMockInterviews(v);
                StatsService.saveMockInterviews(uid, v);
                weeklyData[today]++;
                StatsService.saveDailyActivity(uid, today, weeklyData[today]);
            }
            case "company" -> {
                int v = UserProfileCache.getCompaniesPrepared() + 1;
                UserProfileCache.setCompaniesPrepared(v);
                StatsService.saveCompaniesPrepared(uid, v);
            }
        }

        actionHistory.push(action);
        SwingUtilities.invokeLater(this::syncUI);
    }

    private void rollback() {
        if (actionHistory.isEmpty()) return;
        String last = actionHistory.pop();
        int uid = UserProfileCache.getCurrentUserId();
        if (uid == -1) return;

        int today = todayIndex();

        switch (last) {
            case "solve" -> {
                int v = Math.max(0, UserProfileCache.getProblemsSolved() - 1);
                UserProfileCache.setProblemsSolved(v);
                StatsService.saveProblemsSolved(uid, v);
                weeklyData[today] = Math.max(0, weeklyData[today] - 1);
                StatsService.saveDailyActivity(uid, today, weeklyData[today]);
            }
            case "aptitude" -> {
                int v = Math.max(0, UserProfileCache.getAptitudeScore() - 5);
                UserProfileCache.setAptitudeScore(v);
                StatsService.saveAptitudeScore(uid, v);
                weeklyData[today] = Math.max(0, weeklyData[today] - 1);
                StatsService.saveDailyActivity(uid, today, weeklyData[today]);
            }
            case "mock" -> {
                int v = Math.max(0, UserProfileCache.getMockInterviews() - 1);
                UserProfileCache.setMockInterviews(v);
                StatsService.saveMockInterviews(uid, v);
                weeklyData[today] = Math.max(0, weeklyData[today] - 1);
                StatsService.saveDailyActivity(uid, today, weeklyData[today]);
            }
            case "company" -> {
                int v = Math.max(0, UserProfileCache.getCompaniesPrepared() - 1);
                UserProfileCache.setCompaniesPrepared(v);
                StatsService.saveCompaniesPrepared(uid, v);
            }
        }

        SwingUtilities.invokeLater(this::syncUI);
    }

    // ============================================================
    //  UI BUILDERS
    // ============================================================

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        welcomeTitle = new JLabel("<html>Welcome back, <b>" + userFirstName + "!</b> \uD83D\uDC4B</html>");
        welcomeTitle.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        welcomeTitle.setForeground(Theme.PRIMARY_TEXT);

        welcomeSub = new JLabel("Let's crack your dream placement!");
        welcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeSub.setForeground(Theme.MUTED_TEXT);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(welcomeTitle);
        texts.add(Box.createVerticalStrut(2));
        texts.add(welcomeSub);

        p.add(texts, BorderLayout.WEST);
        return p;
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Sparklines seeded from real weekly data; updated in syncUI
        int apt = UserProfileCache.getAptitudeScore();
        int cod = UserProfileCache.getProblemsSolved();
        int mck = UserProfileCache.getMockInterviews();
        int cmp = UserProfileCache.getCompaniesPrepared();
        int off = UserProfileCache.getOffersTarget();

        // All sparklines come from real weekly activity — no artificial arrays
        aptitudeCard  = new SparklineStatCard("Aptitude Score",     apt, 100, "Loading…", C_PURPLE, makeIcon("\uD83E\uDDE0"), weeklyData.clone());
        codingCard    = new SparklineStatCard("Coding Score",       cod, 100, "Loading…", C_GREEN,  makeIcon("\u2328"),       weeklyData.clone());
        mockCard      = new SparklineStatCard("Mock Interview",     mck,  10, "Loading…", C_BLUE,   makeIcon("\uD83C\uDFA4"), buildMockSparkline(mck));
        companiesCard = new SparklineStatCard("Companies Prepared", cmp,   0, "Loading…", C_ORANGE, makeIcon("\uD83C\uDFE2"), weeklyData.clone());
        offersCard    = new SparklineStatCard("Offers Target",      off,   0, "Loading…", C_PINK,   makeIcon("\uD83C\uDFC6"), weeklyData.clone());

        row.add(aptitudeCard);
        row.add(codingCard);
        row.add(mockCard);
        row.add(companiesCard);
        row.add(offersCard);
        return row;
    }

    private JPanel buildMidSection() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0; left.gridy = 0; left.weightx = 0.60; left.weighty = 1.0;
        left.fill = GridBagConstraints.BOTH; left.insets = new Insets(0, 0, 0, 12);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1; right.gridy = 0; right.weightx = 0.40; right.weighty = 1.0;
        right.fill = GridBagConstraints.BOTH;

        p.add(buildModulesPanel(), left);
        p.add(buildPlacementProgressPanel(), right);
        return p;
    }

    private JPanel buildModulesPanel() {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(16, 16, 16, 16)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 12, 0));
        top.add(sectionLabel("Preparation Modules"), BorderLayout.WEST);

        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setOpaque(false);
        for (Object[] mod : MODULES) {
            grid.add(buildModuleCard((String)mod[0], (String)mod[1],
                    (String)mod[2], (String)mod[3], (Color)mod[4], (String)mod[5]));
        }

        card.add(top, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildModuleCard(String key, String title, String sub,
                                   String action, Color accent, String emoji) {
        JPanel card = new JPanel() {
            boolean hov = false;
            { setOpaque(false);
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              addMouseListener(new MouseAdapter() {
                  public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                  public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                  public void mouseClicked(MouseEvent e) { if (onModuleClick != null) onModuleClick.onModuleClick(key); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22) : Theme.PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hov ? 160 : 60));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel iconLbl = new JLabel(emoji); iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel titleLbl = new JLabel("<html><b>" + title + "</b></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); titleLbl.setForeground(Theme.PRIMARY_TEXT);
        JLabel subLbl = new JLabel("<html>" + sub + "</html>");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10)); subLbl.setForeground(Theme.MUTED_TEXT);
        subLbl.setBorder(new EmptyBorder(3, 0, 6, 0));
        JLabel actLbl = new JLabel(action);
        actLbl.setFont(new Font("Segoe UI", Font.BOLD, 10)); actLbl.setForeground(accent);

        card.add(iconLbl); card.add(titleLbl); card.add(subLbl);
        card.add(Box.createVerticalGlue()); card.add(actLbl);
        return card;
    }

    private JPanel buildPlacementProgressPanel() {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(16, 16, 16, 16)));

        int apt = UserProfileCache.getAptitudeScore();
        int cod = UserProfileCache.getProblemsSolved();
        int mck = UserProfileCache.getMockInterviews();

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(sectionLabel("Placement Progress"), BorderLayout.WEST);

        donutChart = new DonutProgressChart(buildSegments(apt, cod, mck), computeOverall(apt, cod, mck));
        donutChart.setPreferredSize(new Dimension(160, 160));

        // Legend
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(new EmptyBorder(10, 12, 0, 0));

        int res = UserProfileCache.getResumeScore();

        aptLegendVal  = new JLabel(Math.min(100, apt) + "%");
        codLegendVal  = new JLabel(Math.min(100, cod) + "%");
        mockLegendVal = new JLabel(Math.min(100, mck * 10) + "%");
        projLegendVal = new JLabel(res > 0 ? res + "%" : "0%");  // Resume Score — real
        otherLegendVal = null; // removed — was hardcoded dummy

        legend.add(buildLegendRow("Aptitude",      aptLegendVal,  C_PURPLE));
        legend.add(Box.createVerticalStrut(6));
        legend.add(buildLegendRow("Coding",         codLegendVal,  C_GREEN));
        legend.add(Box.createVerticalStrut(6));
        legend.add(buildLegendRow("Mock Interview",  mockLegendVal, C_BLUE));
        legend.add(Box.createVerticalStrut(6));
        legend.add(buildLegendRow("Resume Score",   projLegendVal, C_PINK));

        JPanel donutRow = new JPanel(new BorderLayout());
        donutRow.setOpaque(false);
        donutRow.add(donutChart, BorderLayout.WEST);
        donutRow.add(legend, BorderLayout.CENTER);

        // Milestones
        JLabel msHdr = new JLabel("Milestones");
        msHdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        msHdr.setForeground(Theme.PRIMARY_TEXT);
        msHdr.setBorder(new EmptyBorder(8, 0, 6, 0));

        msAptitudeRow = new MilestoneRow("Aptitude Score \u2265 70%");
        msCodingRow   = new MilestoneRow("Solve 100 DSA Problems");
        msMockRow     = new MilestoneRow("Attend 5 Mock Interviews");
        msProjectsRow = new MilestoneRow("Resume Score \u2265 80%");   // real metric
        msApplyRow    = new MilestoneRow("Apply to 50 Companies");

        // Set initial states — driven from real data
        int res2 = UserProfileCache.getResumeScore();
        msAptitudeRow.updateStatus(apt >= 70, apt > 0 && apt < 70, C_PURPLE);
        msCodingRow.updateStatus(cod >= 100, cod > 0 && cod < 100, C_GREEN);
        msMockRow.updateStatus(mck >= 5, mck > 0 && mck < 5, C_BLUE);
        msProjectsRow.updateStatus(res2 >= 80, res2 > 0 && res2 < 80, C_PINK); // Resume milestone
        msApplyRow.updateStatus(false, false, Theme.MUTED_TEXT);

        JPanel milestones = new JPanel();
        milestones.setOpaque(false);
        milestones.setLayout(new BoxLayout(milestones, BoxLayout.Y_AXIS));
        milestones.add(msHdr);
        milestones.add(msAptitudeRow);
        milestones.add(Box.createVerticalStrut(5));
        milestones.add(msCodingRow);
        milestones.add(Box.createVerticalStrut(5));
        milestones.add(msMockRow);
        milestones.add(Box.createVerticalStrut(5));
        milestones.add(msProjectsRow);
        milestones.add(Box.createVerticalStrut(5));
        milestones.add(msApplyRow);

        card.add(topRow, BorderLayout.NORTH);
        card.add(donutRow, BorderLayout.CENTER);
        card.add(milestones, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildLegendRow(String label, JLabel pctLbl, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JPanel dot = new JPanel() {
            { setOpaque(false); setPreferredSize(new Dimension(10, 10)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color); g2.fillOval(0, 2, 8, 8); g2.dispose();
            }
        };

        JLabel n = new JLabel(label);
        n.setFont(new Font("Segoe UI", Font.PLAIN, 11)); n.setForeground(Theme.PRIMARY_TEXT);
        pctLbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); pctLbl.setForeground(color);

        row.add(dot, BorderLayout.WEST);
        row.add(n, BorderLayout.CENTER);
        row.add(pctLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBottomSection() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0; gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0; gc.weightx = 0.45; gc.insets = new Insets(0, 0, 0, 10);
        p.add(buildPerformanceOverview(), gc);

        gc.gridx = 1; gc.weightx = 0.30; gc.insets = new Insets(0, 0, 0, 10);
        p.add(buildUpcomingSchedule(), gc);

        gc.gridx = 2; gc.weightx = 0.25; gc.insets = new Insets(0, 0, 0, 0);
        p.add(buildCompanyQuestions(), gc);

        return p;
    }

    private JPanel buildPerformanceOverview() {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(16, 16, 12, 16)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(sectionLabel("Performance Overview"), BorderLayout.WEST);

        JComboBox<String> period = new JComboBox<>(new String[]{"This Month", "This Week", "3 Months"});
        period.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        period.setPreferredSize(new Dimension(110, 26));
        topRow.add(period, BorderLayout.EAST);

        int apt = UserProfileCache.getAptitudeScore();
        int cod = UserProfileCache.getProblemsSolved();
        int mck = UserProfileCache.getMockInterviews();
        // Use real values, fall back to safe defaults if 0
        barChart = new PerformanceBarChart(
            Math.min(100, apt == 0 ? 0 : apt),
            Math.min(100, cod == 0 ? 0 : cod),
            Math.min(100, mck == 0 ? 0 : mck * 10));
        barChart.setPreferredSize(new Dimension(0, 200));

        card.add(topRow, BorderLayout.NORTH);
        card.add(barChart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildUpcomingSchedule() {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(16, 16, 16, 16)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(sectionLabel("Upcoming Schedule"), BorderLayout.WEST);
        topRow.add(linkLabel("View Calendar"), BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(8, 0, 0, 0));

        Object[][] events = {
            {C_PURPLE, "\uD83E\uDDE0", "Aptitude Test",   "Quantitative Aptitude",   "Today, 06:00 PM"},
            {C_GREEN,  "\u2328",       "Coding Contest",  "Weekly Coding Challenge",  "Tomorrow, 07:00 PM"},
            {C_BLUE,   "\uD83C\uDFA4", "Mock Interview",  "Technical Round – 1",      "24 May, 05:00 PM"},
            {C_ORANGE, "\uD83C\uDFE2", "Company Test",    "TCS NQT",                  "25 May, 11:00 AM"},
        };
        for (Object[] ev : events) {
            list.add(buildScheduleRow((Color)ev[0], (String)ev[1], (String)ev[2], (String)ev[3], (String)ev[4]));
            list.add(Box.createVerticalStrut(10));
        }

        card.add(topRow, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildScheduleRow(Color accent, String emoji, String title, String sub, String time) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel badge = new JLabel(emoji) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        badge.setPreferredSize(new Dimension(34, 34));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setOpaque(false);

        JLabel titleLbl = new JLabel("<html><b>" + title + "</b><br><span style='color:gray;font-size:9pt'>" + sub + "</span></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(Theme.PRIMARY_TEXT);

        JLabel timeLbl = new JLabel("<html><div style='text-align:right;font-size:9pt'>" + time + "</div></html>");
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLbl.setForeground(Theme.MUTED_TEXT);

        row.add(badge, BorderLayout.WEST);
        row.add(titleLbl, BorderLayout.CENTER);
        row.add(timeLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildCompanyQuestions() {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(16, 14, 16, 14)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(sectionLabel("Company-wise Questions"), BorderLayout.WEST);
        topRow.add(linkLabel("View All"), BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(3, 2, 10, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 0, 0, 0));

        Object[][] companies = {
            {"TCS",       "1250+ Qs", new Color(27,  77,  144), "TCS"},
            {"Infosys",   "980+ Qs",  new Color(0,   112, 182), "INF"},
            {"Wipro",     "870+ Qs",  new Color(111, 178, 68),  "WIP"},
            {"Accenture", "760+ Qs",  new Color(162, 0,   109), "ACC"},
            {"Amazon",    "900+ Qs",  new Color(255, 153, 0),   "AMZ"},
            {"Microsoft", "650+ Qs",  new Color(0,   120, 212), "MSF"},
        };
        for (Object[] co : companies)
            grid.add(buildCompanyCard((String)co[0], (String)co[1], (Color)co[2], (String)co[3]));

        card.add(topRow, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCompanyCard(String name, String qs, Color accent, String abbr) {
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            boolean hov = false;
            { setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              addMouseListener(new MouseAdapter() {
                  public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                  public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                  public void mouseClicked(MouseEvent e) { if (onModuleClick != null) onModuleClick.onModuleClick("companies"); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22) : Theme.PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hov ? 140 : 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel badge = new JLabel(abbr) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(Color.WHITE);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(32, 22));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(name);
        n.setFont(new Font("Segoe UI", Font.BOLD, 11)); n.setForeground(Theme.PRIMARY_TEXT);
        JLabel q = new JLabel(qs);
        q.setFont(new Font("Segoe UI", Font.PLAIN, 10)); q.setForeground(Theme.MUTED_TEXT);
        info.add(n); info.add(q);

        card.add(badge, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    /**
     * Simulator bar with real-time action buttons and the Rollback stack undo.
     * Sits between the bottom section and Recent Activity.
     */
    private JPanel buildSimulatorBar() {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 10));
        card.setAccentColor(C_PURPLE);
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(14, 18, 14, 18)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel heading = new JLabel("\uD83C\uDFAF Practice Simulator — Simulate & Undo Actions");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(Theme.PRIMARY_TEXT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        btnRow.add(simButton("Solve DSA Problem +1",        C_GREEN,  "solve"));
        btnRow.add(simButton("Aptitude Score +5",           C_PURPLE, "aptitude"));
        btnRow.add(simButton("Mock Interview +1",           C_BLUE,   "mock"));
        btnRow.add(simButton("Company Prepared +1",         C_ORANGE, "company"));

        btnRollback = new JButton("\u21A9  Rollback Last Action");
        btnRollback.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRollback.setForeground(Color.WHITE);
        btnRollback.setBackground(new Color(200, 50, 50));
        btnRollback.setBorder(new EmptyBorder(7, 16, 7, 16));
        btnRollback.setFocusPainted(false);
        btnRollback.setOpaque(true);
        btnRollback.setEnabled(false);
        btnRollback.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRollback.addActionListener(e -> rollback());
        btnRow.add(btnRollback);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(heading, BorderLayout.NORTH);
        body.add(btnRow,  BorderLayout.CENTER);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JButton simButton(String label, Color accent, String actionKey) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(accent);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> simulate(actionKey));
        return btn;
    }

    private JPanel buildRecentActivity() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(sectionLabel("Recent Activity"), BorderLayout.WEST);

        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Recent activity is derived from real stats (dynamic label text)
        int apt = UserProfileCache.getAptitudeScore();
        int cod = UserProfileCache.getProblemsSolved();
        int mck = UserProfileCache.getMockInterviews();
        int res = UserProfileCache.getResumeScore();
        int cmp = UserProfileCache.getCompaniesPrepared();

        activityCodSub = new JLabel(cod + " solved total");
        activityAptSub = new JLabel("Score: " + apt + "/100");
        activityMockSub = new JLabel("Completed: " + mck + "/10");
        activityResumeSub = new JLabel(res > 0 ? "Score: " + res + "%" : "Not checked yet");
        activityCompSub = new JLabel(cmp + " companies");

        row.add(buildActivityCard(C_GREEN,  "\u2705", "DSA Problems Solved",     activityCodSub,        "Today"));
        row.add(buildActivityCard(C_PURPLE, "\uD83D\uDCCA", "Aptitude Score",    activityAptSub,        "Today"));
        row.add(buildActivityCard(C_BLUE,   "\uD83C\uDFA4", "Mock Interviews",   activityMockSub,       "This week"));
        row.add(buildActivityCard(C_ORANGE, "\uD83D\uDCC4", "Resume Score",      activityResumeSub,     "This week"));
        row.add(buildActivityCard(C_PINK,   "\uD83C\uDFE2", "Companies Prepared", activityCompSub,      "This month"));

        wrapper.add(topRow, BorderLayout.NORTH);
        wrapper.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        wrapper.add(row, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildActivityCard(Color accent, String emoji, String title, JLabel subLbl, String time) {
        ElevatedCard card = new ElevatedCard(new BorderLayout(0, 4));
        card.setAccentColor(accent);
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(), new EmptyBorder(10, 12, 10, 12)));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        JLabel iconLbl = new JLabel(emoji);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLbl.setForeground(Theme.MUTED_TEXT);
        top.add(iconLbl); top.add(timeLbl);

        JLabel titleLbl = new JLabel("<html><b>" + title + "</b></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(Theme.PRIMARY_TEXT);
        
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(Theme.MUTED_TEXT);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(top); body.add(Box.createVerticalStrut(4));
        body.add(titleLbl); body.add(subLbl);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ============================================================
    //  HELPERS
    // ============================================================

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Theme.PRIMARY_TEXT);
        return l;
    }

    private JLabel linkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(C_PURPLE);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private Icon makeIcon(String emoji) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.drawString(emoji, x, y + 18);
                g2.dispose();
            }
            @Override public int getIconWidth()  { return 22; }
            @Override public int getIconHeight() { return 22; }
        };
    }

    private static Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
}