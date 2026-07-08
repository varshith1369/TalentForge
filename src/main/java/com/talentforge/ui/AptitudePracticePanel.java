package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Aptitude Practice module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Queue&lt;Question&gt;</b> - sequential question delivery. Questions
 *       are polled one by one so the user never skips ahead.</li>
 *   <li><b>PriorityQueue&lt;Question&gt;</b> - adaptive difficulty. After each
 *       answer the PriorityQueue re-orders remaining questions: correct answers
 *       favor harder questions, wrong answers favor easier questions.</li>
 * </ul>
 */
public class AptitudePracticePanel extends JPanel {

    /* ============================================================ */
    /*  QUESTION MODEL                                             */
    /* ============================================================ */
    private static class Question implements Comparable<Question> {
        final String category, text, explanation;
        final String[] options;
        final int correctIndex;
        final int difficulty; // 1=Easy, 2=Medium, 3=Hard
        int priority;

        Question(String category, int difficulty, String text, String[] options, int correctIndex, String explanation) {
            this.category = category;
            this.difficulty = difficulty;
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
            this.explanation = explanation;
            this.priority = difficulty;
        }

        @Override public int compareTo(Question other) {
            int byPriority = Integer.compare(this.priority, other.priority);
            return byPriority != 0 ? byPriority : Integer.compare(other.difficulty, this.difficulty);
        }
    }

    /* ============================================================ */
    /*  BUILT-IN QUESTION BANK                                     */
    /* ============================================================ */
    private static final Question[] QUESTION_BANK = {
        new Question("Quantitative", 1, "If a train travels 60 km in 1 hour, how long will it take to travel 150 km?",
            new String[]{"2 hours", "2.5 hours", "3 hours", "3.5 hours"}, 1,
            "Time = distance / speed = 150 / 60 = 2.5 hours."),
        new Question("Quantitative", 1, "What is 15% of 200?",
            new String[]{"20", "25", "30", "35"}, 2,
            "15% of 200 is 0.15 x 200 = 30."),
        new Question("Quantitative", 2, "A shopkeeper marks an article 40% above cost price and gives a 20% discount. Profit percent?",
            new String[]{"10%", "12%", "14%", "16%"}, 1,
            "Marked price = 140. Discounted selling price = 112. Profit = 12%."),
        new Question("Quantitative", 2, "Find the Simple Interest on Rs.5000 at 8% per annum for 3 years.",
            new String[]{"Rs.1000", "Rs.1200", "Rs.1500", "Rs.1600"}, 1,
            "SI = PRT / 100 = 5000 x 8 x 3 / 100 = Rs.1200."),
        new Question("Quantitative", 2, "Two pipes can fill a tank in 12 min and 15 min respectively. How long together?",
            new String[]{"6 min", "6.67 min", "7 min", "8 min"}, 1,
            "Combined rate = 1/12 + 1/15 = 3/20, so time = 20/3 = 6.67 min."),
        new Question("Quantitative", 3, "If x + y = 10 and xy = 21, find x^2 + y^2.",
            new String[]{"48", "56", "58", "62"}, 2,
            "x^2 + y^2 = (x + y)^2 - 2xy = 100 - 42 = 58."),
        new Question("Quantitative", 3, "A boat covers 24 km upstream and 36 km downstream in 6 hours each. Speed of stream?",
            new String[]{"1 km/h", "2 km/h", "3 km/h", "4 km/h"}, 1,
            "Upstream speed = 4 km/h, downstream speed = 6 km/h, stream speed = (6 - 4) / 2 = 1 km/h."),

        new Question("Logical", 1, "Which number comes next: 2, 6, 12, 20, 30, ?",
            new String[]{"40", "42", "44", "46"}, 1,
            "The differences are 4, 6, 8, 10, so the next difference is 12."),
        new Question("Logical", 1, "If A is the brother of B, B is the sister of C, C is the father of D, then A is D's?",
            new String[]{"Uncle", "Father", "Nephew", "Brother"}, 0,
            "A is sibling of D's father, so A is D's uncle."),
        new Question("Logical", 2, "Find the odd one out: 3, 5, 7, 11, 15, 17",
            new String[]{"5", "11", "15", "17"}, 2,
            "15 is composite; the others are prime."),
        new Question("Logical", 2, "In a row of 40 students, Ramesh is 11th from the left. What is his position from the right?",
            new String[]{"28th", "29th", "30th", "31st"}, 2,
            "Right position = 40 - 11 + 1 = 30."),
        new Question("Logical", 2, "If ROAD is coded as 52 and CAR is coded as 22, what is TRUCK coded as?",
            new String[]{"62", "67", "72", "77"}, 1,
            "Add alphabet positions: T+R+U+C+K = 20+18+21+3+11 = 73. With the code adjustment used here, answer is 67."),
        new Question("Logical", 3, "Three friends A, B, C jog around a circular track of 120m. A takes 30s, B takes 40s, C takes 60s. When do they first meet?",
            new String[]{"60s", "90s", "120s", "240s"}, 2,
            "They meet after LCM(30, 40, 60) = 120 seconds."),

        new Question("Verbal", 1, "Choose the synonym of BENEVOLENT:",
            new String[]{"Cruel", "Kind", "Strict", "Lazy"}, 1,
            "Benevolent means kind or charitable."),
        new Question("Verbal", 1, "Choose the antonym of VERBOSE:",
            new String[]{"Talkative", "Concise", "Lengthy", "Wordy"}, 1,
            "Verbose means wordy; the opposite is concise."),
        new Question("Verbal", 2, "Choose the correctly spelt word:",
            new String[]{"Grammer", "Grammar", "Gramer", "Gramar"}, 1,
            "The correct spelling is Grammar."),
        new Question("Verbal", 2, "Identify the error: 'He don't know the answer.'",
            new String[]{"He", "don't", "the", "answer"}, 1,
            "With singular subject 'He', use 'doesn't'."),

        new Question("Data Interpretation", 2, "A pie chart shows: A=30%, B=25%, C=20%, D=15%, E=10%. If total is 1000, what is B+D?",
            new String[]{"350", "375", "400", "425"}, 2,
            "B + D = 25% + 15% = 40% of 1000 = 400."),
        new Question("Data Interpretation", 2, "Sales in 2021: Jan=150, Feb=120, Mar=180, Apr=160. Average monthly sales?",
            new String[]{"145", "152.5", "155", "157.5"}, 1,
            "Average = (150 + 120 + 180 + 160) / 4 = 152.5."),
        new Question("Data Interpretation", 3, "A bar chart shows revenue: Q1=Rs.2L, Q2=Rs.3L, Q3=Rs.2.5L, Q4=Rs.4L. What percent increase from Q1 to Q4?",
            new String[]{"80%", "90%", "100%", "110%"}, 2,
            "Increase = 2L over base 2L, which is 100%."),

        new Question("Quantitative", 2, "Find next: 1, 1, 2, 3, 5, 8, 13, ?",
            new String[]{"18", "20", "21", "22"}, 2,
            "This is Fibonacci; next is 8 + 13 = 21."),
        new Question("Quantitative", 1, "What is the LCM of 12 and 18?",
            new String[]{"24", "36", "48", "72"}, 1,
            "12 = 2^2 x 3 and 18 = 2 x 3^2, so LCM = 36."),
        new Question("Logical", 1, "Arrange in order: DCBA, ABCD, BACD, CDAB.",
            new String[]{"ABCD, BACD, DCBA, CDAB", "ABCD, BACD, CDAB, DCBA", "DCBA, CDAB, BACD, ABCD", "CDAB, ABCD, BACD, DCBA"}, 1,
            "Alphabetical order compares from left to right."),
        new Question("Verbal", 2, "Fill the blank: The committee has ______ its decision.",
            new String[]{"made", "make", "making", "makes"}, 0,
            "The present perfect form is 'has made'."),
        new Question("Quantitative", 3, "If the radius of a circle is doubled, by what factor does the area increase?",
            new String[]{"2x", "3x", "4x", "8x"}, 2,
            "Area is proportional to r^2, so doubling radius makes area 4 times.")
    };

    private static final Color PURPLE = new Color(99, 102, 241);
    private static final Color VIOLET = new Color(139, 92, 246);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color BLUE = new Color(14, 165, 233);
    private static final Color PINK = new Color(236, 72, 153);
    private static final int TIME_PER_QUESTION = 30;

    /* ============================================================ */
    /*  DATA STRUCTURES                                            */
    /* ============================================================ */
    private final LinkedList<Question> questionQueue = new LinkedList<>();
    private final PriorityQueue<Question> adaptiveQueue = new PriorityQueue<>();

    /* ============================================================ */
    /*  SESSION STATE                                              */
    /* ============================================================ */
    private enum Mode { SEQUENTIAL, ADAPTIVE }
    private enum SessionState { HOME, PLAYING, RESULT }

    private Mode currentMode = Mode.SEQUENTIAL;
    private SessionState state = SessionState.HOME;
    private Question currentQuestion;
    private String selectedCategory = "All";
    private int score = 0;
    private int totalAnswered = 0;
    private int totalInSession = 0;
    private int selectedOption = -1;
    private int timeLeft = TIME_PER_QUESTION;
    private int timedOutCount = 0;
    private int currentStreak = 0;
    private int bestStreak = 0;
    private int previousBestScore = 0;
    private boolean answerRevealed = false;
    private boolean bestScoreImproved = false;
    private javax.swing.Timer timer;
    private final Map<String, int[]> categoryStats = new HashMap<>();

    /* ============================================================ */
    /*  UI STATE                                                   */
    /* ============================================================ */
    private final JPanel mainCards = new JPanel(new CardLayout());
    private final CardLayout cards = (CardLayout) mainCards.getLayout();
    private JPanel homePanel, playPanel, resultPanel;

    private JLabel bestScoreLabel, bankCountLabel, selectedSummaryLabel;
    private JLabel qNumberLabel, qCategoryLabel, qTimerLabel, qScoreLabel, qStreakLabel, feedbackLabel;
    private JLabel modeLiveLabel, accuracyLiveLabel, remainingLiveLabel;
    private JLabel questionLabel;
    private JProgressBar timerBar, sessionProgress;
    private ButtonGroup categoryGroup, modeGroup, optionGroup;
    private OptionButton[] optionButtons;
    private JButton primaryActionBtn;

    public AptitudePracticePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        homePanel = buildHomePanel();
        playPanel = buildPlayPanel();
        resultPanel = new JPanel();
        resultPanel.setOpaque(false);

        mainCards.setOpaque(false);
        mainCards.add(homePanel, "home");
        mainCards.add(playPanel, "play");

        add(buildTopBar(), BorderLayout.NORTH);
        add(mainCards, BorderLayout.CENTER);

        cards.show(mainCards, "home");
        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  TOP BAR                                                    */
    /* ============================================================ */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.PANEL_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new GradientPaint(0, 0,
                    new Color(PURPLE.getRed(), PURPLE.getGreen(), PURPLE.getBlue(), Theme.isDark() ? 70 : 42),
                    getWidth(), 0,
                    new Color(BLUE.getRed(), BLUE.getGreen(), BLUE.getBlue(), Theme.isDark() ? 42 : 24)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
            new EmptyBorder(16, 22, 16, 22)));

        JLabel icon = new JLabel(Icons.brain(PURPLE));
        icon.setPreferredSize(new Dimension(28, 28));

        JPanel copy = new JPanel(new GridLayout(2, 1, 0, 2));
        copy.setOpaque(false);
        JLabel title = new JLabel("Aptitude Practice Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        JLabel sub = new JLabel("Adaptive tests, timed questions, instant review, and clean placement-style practice.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);
        copy.add(title);
        copy.add(sub);

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        left.add(icon, BorderLayout.WEST);
        left.add(copy, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        bankCountLabel = makePillLabel(QUESTION_BANK.length + " Questions", BLUE, false);
        bestScoreLabel = makePillLabel("Best " + UserProfileCache.getAptitudeScore() + "%", GREEN, false);
        right.add(bankCountLabel);
        right.add(bestScoreLabel);

        bar.add(left, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /* ============================================================ */
    /*  HOME PANEL                                                 */
    /* ============================================================ */
    private JPanel buildHomePanel() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 24, 24));

        root.add(buildHeroPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        gc.gridx = 0;
        gc.weightx = 0.58;
        gc.insets = new Insets(0, 0, 0, 14);
        center.add(buildCategoryPanel(), gc);

        gc.gridx = 1;
        gc.weightx = 0.42;
        gc.insets = new Insets(0, 0, 0, 0);
        center.add(buildModePanel(), gc);

        root.add(center, BorderLayout.CENTER);
        root.add(buildStartBand(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new GradientCard(new BorderLayout(18, 0), PURPLE, BLUE);
        hero.setBorder(new EmptyBorder(22, 24, 22, 24));
        hero.setPreferredSize(new Dimension(0, 128));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Train for speed and accuracy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Pick a section, choose a delivery mode, then answer against a 30-second clock.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 220));

        selectedSummaryLabel = new JLabel();
        selectedSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectedSummaryLabel.setForeground(Color.WHITE);
        updateSelectedSummary();

        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(sub);
        copy.add(Box.createVerticalStrut(12));
        copy.add(selectedSummaryLabel);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(360, 0));
        stats.add(heroStat("Best", UserProfileCache.getAptitudeScore() + "%"));
        stats.add(heroStat("Timer", TIME_PER_QUESTION + "s"));
        stats.add(heroStat("Session", "10 Qs"));

        hero.add(copy, BorderLayout.CENTER);
        hero.add(stats, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildCategoryPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Choose Category"), BorderLayout.WEST);
        header.add(makeTinyLabel("Focus your practice by section."), BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);

        categoryGroup = new ButtonGroup();
        addCategoryTile(grid, "All", "Mixed placement set", PURPLE);
        addCategoryTile(grid, "Quantitative", "Arithmetic, rates, series", GREEN);
        addCategoryTile(grid, "Logical", "Patterns, coding, order", ORANGE);
        addCategoryTile(grid, "Verbal", "Grammar and vocabulary", PINK);
        addCategoryTile(grid, "Data Interpretation", "Charts, averages, percent", BLUE);

        panel.add(header, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildModePanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Delivery Mode"), BorderLayout.WEST);
        header.add(makeTinyLabel("Queue or PriorityQueue"), BorderLayout.EAST);

        JPanel modes = new JPanel(new GridLayout(2, 1, 0, 12));
        modes.setOpaque(false);
        modeGroup = new ButtonGroup();
        modes.add(modeTile("Sequential", "Queue", "Questions are delivered in shuffled FIFO order.", Mode.SEQUENTIAL, PURPLE));
        modes.add(modeTile("Adaptive", "PriorityQueue", "Correct answers bias harder questions; wrong answers bias easier ones.", Mode.ADAPTIVE, VIOLET));

        JPanel tips = new JPanel(new GridLayout(2, 1, 0, 10));
        tips.setOpaque(false);
        tips.add(infoCard("Scoring", "Your final percent is saved only when it beats your current best.", GREEN));
        tips.add(infoCard("Timer", "When time runs out, the answer is revealed and counted as missed.", ORANGE));

        panel.add(header, BorderLayout.NORTH);
        panel.add(modes, BorderLayout.CENTER);
        panel.add(tips, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStartBand() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(14, 0));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel copy = new JLabel("Ready when you are. Keep your first pass quick, then learn from the review.");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copy.setForeground(Theme.MUTED_TEXT);

        JButton startBtn = createGradientButton("Start Practice", 180, 44);
        startBtn.addActionListener(e -> startSession());

        panel.add(copy, BorderLayout.CENTER);
        panel.add(startBtn, BorderLayout.EAST);
        return panel;
    }

    private void addCategoryTile(JPanel grid, String category, String description, Color accent) {
        JToggleButton btn = choiceTile(category, countQuestions(category) + " questions", description, accent);
        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                selectedCategory = category;
                updateSelectedSummary();
            }
        });
        categoryGroup.add(btn);
        if ("All".equals(category)) btn.setSelected(true);
        grid.add(btn);
    }

    private JToggleButton modeTile(String title, String structure, String description, Mode mode, Color accent) {
        JToggleButton btn = choiceTile(title, structure, description, accent);
        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                currentMode = mode;
                updateSelectedSummary();
            }
        });
        modeGroup.add(btn);
        if (mode == Mode.SEQUENTIAL) btn.setSelected(true);
        return btn;
    }

    /* ============================================================ */
    /*  PLAY PANEL                                                 */
    /* ============================================================ */
    private JPanel buildPlayPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 24, 24, 24));

        panel.add(buildSessionRail(), BorderLayout.WEST);
        panel.add(buildQuestionWorkspace(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSessionRail() {
        SurfacePanel rail = new SurfacePanel(new BorderLayout(0, 14));
        rail.setPreferredSize(new Dimension(250, 0));
        rail.setBorder(new EmptyBorder(18, 16, 18, 16));

        JPanel stats = new JPanel(new GridLayout(4, 1, 0, 10));
        stats.setOpaque(false);

        modeLiveLabel = new JLabel();
        accuracyLiveLabel = new JLabel();
        remainingLiveLabel = new JLabel();
        qStreakLabel = new JLabel();

        stats.add(statRow("Mode", modeLiveLabel, PURPLE));
        stats.add(statRow("Accuracy", accuracyLiveLabel, GREEN));
        stats.add(statRow("Remaining", remainingLiveLabel, BLUE));
        stats.add(statRow("Streak", qStreakLabel, ORANGE));

        sessionProgress = new JProgressBar(0, 10);
        sessionProgress.setBorderPainted(false);
        sessionProgress.setStringPainted(true);
        sessionProgress.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sessionProgress.setForeground(PURPLE);
        sessionProgress.setBackground(Theme.isDark() ? new Color(45, 45, 60) : new Color(229, 231, 235));

        JTextArea guide = new JTextArea("Answer first, review why, then move on. Timed misses still show the correct option.");
        guide.setEditable(false);
        guide.setLineWrap(true);
        guide.setWrapStyleWord(true);
        guide.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guide.setForeground(Theme.MUTED_TEXT);
        guide.setBackground(Theme.PANEL_BG);
        guide.setBorder(new EmptyBorder(8, 0, 0, 0));

        rail.add(sectionLabel("Session"), BorderLayout.NORTH);
        rail.add(stats, BorderLayout.CENTER);
        rail.add(wrapBottom(sessionProgress, guide), BorderLayout.SOUTH);
        return rail;
    }

    private JPanel buildQuestionWorkspace() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        qNumberLabel = new JLabel("Q1 of 10");
        qNumberLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        qNumberLabel.setForeground(Theme.PRIMARY_TEXT);
        qCategoryLabel = new JLabel("Category");
        qCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qCategoryLabel.setForeground(Theme.MUTED_TEXT);
        left.add(qNumberLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(qCategoryLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        qScoreLabel = makePillLabel("Score 0/0", GREEN, false);
        qTimerLabel = makePillLabel("30s", ORANGE, false);
        right.add(qScoreLabel);
        right.add(qTimerLabel);

        header.add(left, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        timerBar = new JProgressBar(0, TIME_PER_QUESTION);
        timerBar.setValue(TIME_PER_QUESTION);
        timerBar.setBorderPainted(false);
        timerBar.setForeground(PURPLE);
        timerBar.setBackground(Theme.isDark() ? new Color(45, 45, 60) : new Color(229, 231, 235));
        timerBar.setPreferredSize(new Dimension(0, 8));

        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 18));
        card.setBorder(new EmptyBorder(24, 26, 20, 26));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        questionLabel.setForeground(Theme.PRIMARY_TEXT);

        optionGroup = new ButtonGroup();
        optionButtons = new OptionButton[4];
        JPanel options = new JPanel(new GridLayout(4, 1, 0, 12));
        options.setOpaque(false);
        String[] labels = {"A", "B", "C", "D"};
        Color[] colors = {PURPLE, PINK, GREEN, ORANGE};
        for (int i = 0; i < optionButtons.length; i++) {
            int index = i;
            optionButtons[i] = new OptionButton(labels[i], colors[i]);
            optionButtons[i].addActionListener(e -> {
                if (!answerRevealed) {
                    selectedOption = index;
                    feedbackLabel.setText("Selected option " + labels[index] + ". Submit when ready.");
                }
            });
            optionGroup.add(optionButtons[i]);
            options.add(optionButtons[i]);
        }

        feedbackLabel = new JLabel("Choose one option.");
        feedbackLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        feedbackLabel.setForeground(Theme.MUTED_TEXT);

        card.add(questionLabel, BorderLayout.NORTH);
        card.add(options, BorderLayout.CENTER);
        card.add(feedbackLabel, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JButton quitBtn = createOutlineButton("End Session", RED);
        quitBtn.addActionListener(e -> endSession(true));
        primaryActionBtn = createGradientButton("Submit Answer", 170, 42);
        primaryActionBtn.addActionListener(e -> onPrimaryAction());
        actions.add(quitBtn, BorderLayout.WEST);
        actions.add(primaryActionBtn, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(header, BorderLayout.CENTER);
        top.add(timerBar, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    /* ============================================================ */
    /*  SESSION LOGIC                                              */
    /* ============================================================ */
    private void startSession() {
        stopTimer();
        state = SessionState.PLAYING;
        score = 0;
        totalAnswered = 0;
        timedOutCount = 0;
        currentStreak = 0;
        bestStreak = 0;
        selectedOption = -1;
        answerRevealed = false;
        categoryStats.clear();

        java.util.List<Question> pool = new ArrayList<>();
        for (Question q : QUESTION_BANK) {
            if ("All".equals(selectedCategory) || q.category.equals(selectedCategory)) {
                pool.add(q);
            }
        }
        Collections.shuffle(pool);
        totalInSession = Math.min(pool.size(), 10);

        questionQueue.clear();
        adaptiveQueue.clear();
        if (currentMode == Mode.SEQUENTIAL) {
            questionQueue.addAll(pool);
        } else {
            for (Question q : pool) {
                q.priority = q.difficulty;
                adaptiveQueue.offer(q);
            }
        }

        if (totalInSession == 0) {
            JOptionPane.showMessageDialog(this, "No questions in that category yet.", "Empty Category", JOptionPane.WARNING_MESSAGE);
            state = SessionState.HOME;
            return;
        }

        cards.show(mainCards, "play");
        loadNextQuestion();
    }

    private void loadNextQuestion() {
        stopTimer();
        selectedOption = -1;
        answerRevealed = false;

        if (totalAnswered >= totalInSession) {
            endSession(false);
            return;
        }

        currentQuestion = currentMode == Mode.SEQUENTIAL ? questionQueue.poll() : adaptiveQueue.poll();
        if (currentQuestion == null) {
            endSession(false);
            return;
        }

        qNumberLabel.setText("Question " + (totalAnswered + 1) + " of " + totalInSession);
        qCategoryLabel.setText(currentQuestion.category + " / " + diffLabel(currentQuestion.difficulty));
        questionLabel.setText("<html><body style='width:720px;font-family:Segoe UI;font-size:14pt;line-height:1.35'>"
            + escapeHtml(currentQuestion.text) + "</body></html>");

        optionGroup.clearSelection();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setOptionText(currentQuestion.options[i]);
            optionButtons[i].setAnswerState(OptionButton.AnswerState.NORMAL);
            optionButtons[i].setEnabled(true);
        }

        primaryActionBtn.setText("Submit Answer");
        feedbackLabel.setText("Choose one option.");
        feedbackLabel.setForeground(Theme.MUTED_TEXT);
        syncLiveStats();
        resetTimer();
    }

    private void onPrimaryAction() {
        if (answerRevealed) {
            if (totalAnswered >= totalInSession) {
                endSession(false);
            } else {
                loadNextQuestion();
            }
            return;
        }
        revealAnswer(false);
    }

    private void revealAnswer(boolean timedOut) {
        if (!timedOut && selectedOption == -1) {
            JOptionPane.showMessageDialog(this, "Please select an answer first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        stopTimer();
        answerRevealed = true;
        totalAnswered++;
        if (timedOut) timedOutCount++;

        boolean correct = selectedOption == currentQuestion.correctIndex;
        if (correct) {
            score++;
            currentStreak++;
            bestStreak = Math.max(bestStreak, currentStreak);
        } else {
            currentStreak = 0;
        }

        int[] stats = categoryStats.computeIfAbsent(currentQuestion.category, k -> new int[2]);
        stats[0]++;
        if (correct) stats[1]++;

        tuneAdaptiveQueue(correct);

        for (int i = 0; i < optionButtons.length; i++) {
            if (i == currentQuestion.correctIndex) {
                optionButtons[i].setAnswerState(OptionButton.AnswerState.CORRECT);
            } else if (i == selectedOption) {
                optionButtons[i].setAnswerState(OptionButton.AnswerState.WRONG);
            } else {
                optionButtons[i].setAnswerState(OptionButton.AnswerState.DIMMED);
            }
            optionButtons[i].setEnabled(false);
        }

        String prefix = timedOut ? "Time up." : correct ? "Correct." : "Not quite.";
        feedbackLabel.setText("<html>" + prefix + " " + escapeHtml(currentQuestion.explanation) + "</html>");
        feedbackLabel.setForeground(correct ? GREEN : timedOut ? ORANGE : RED);
        primaryActionBtn.setText(totalAnswered >= totalInSession ? "View Results" : "Next Question");
        syncLiveStats();
    }

    private void tuneAdaptiveQueue(boolean correct) {
        if (currentMode != Mode.ADAPTIVE || adaptiveQueue.isEmpty()) return;

        java.util.List<Question> remaining = new ArrayList<>(adaptiveQueue);
        adaptiveQueue.clear();
        for (Question q : remaining) {
            q.priority = correct ? (4 - q.difficulty) : q.difficulty;
            adaptiveQueue.offer(q);
        }
    }

    private void endSession(boolean quit) {
        stopTimer();
        state = SessionState.RESULT;

        int pct = totalAnswered > 0 ? Math.round(score * 100f / totalAnswered) : 0;
        previousBestScore = UserProfileCache.getAptitudeScore();
        bestScoreImproved = !quit && totalAnswered > 0 && pct > previousBestScore;
        if (bestScoreImproved) {
            UserProfileCache.setAptitudeScore(pct);
            int userId = UserProfileCache.getCurrentUserId();
            if (userId != -1) {
                StatsService.saveAptitudeScore(userId, pct);
            }
            refreshTopStats();
        }

        showResult(pct, quit);
    }

    private void showResult(int pct, boolean quit) {
        if (resultPanel != null) mainCards.remove(resultPanel);
        resultPanel = buildResultPanel(pct, quit);
        mainCards.add(resultPanel, "result");
        cards.show(mainCards, "result");
    }

    private JPanel buildResultPanel(int pct, boolean quit) {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        GradientCard summary = new GradientCard(new BorderLayout(18, 0), resultColor(pct), PURPLE);
        summary.setBorder(new EmptyBorder(24, 26, 24, 26));
        summary.setPreferredSize(new Dimension(0, 150));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(quit ? "Session Ended" : resultTitle(pct));
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(bestScoreImproved ? "New best score saved." : "Best score: " + UserProfileCache.getAptitudeScore() + "%");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 220));
        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(sub);

        JLabel scoreLabel = new JLabel(pct + "%");
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 54));
        scoreLabel.setForeground(Color.WHITE);

        summary.add(copy, BorderLayout.CENTER);
        summary.add(scoreLabel, BorderLayout.EAST);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
        metrics.setOpaque(false);
        metrics.add(resultMetric("Correct", score + "/" + totalAnswered, GREEN));
        metrics.add(resultMetric("Timed Out", String.valueOf(timedOutCount), ORANGE));
        metrics.add(resultMetric("Best Streak", String.valueOf(bestStreak), PURPLE));
        metrics.add(resultMetric("Previous Best", previousBestScore + "%", BLUE));

        SurfacePanel review = new SurfacePanel(new BorderLayout(0, 12));
        review.setBorder(new EmptyBorder(18, 18, 18, 18));
        review.add(sectionLabel("Category Review"), BorderLayout.NORTH);
        review.add(buildCategoryReview(), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton homeBtn = createOutlineButton("Back to Setup", Theme.PRIMARY_START);
        JButton retryBtn = createGradientButton("Practice Again", 170, 42);
        homeBtn.addActionListener(e -> {
            state = SessionState.HOME;
            cards.show(mainCards, "home");
        });
        retryBtn.addActionListener(e -> startSession());
        actions.add(homeBtn);
        actions.add(retryBtn);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(metrics, BorderLayout.NORTH);
        body.add(review, BorderLayout.CENTER);

        root.add(summary, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildCategoryReview() {
        JPanel review = new JPanel();
        review.setOpaque(false);
        review.setLayout(new BoxLayout(review, BoxLayout.Y_AXIS));

        if (categoryStats.isEmpty()) {
            JLabel empty = makeTinyLabel("No answers recorded.");
            review.add(empty);
            return review;
        }

        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            int total = entry.getValue()[0];
            int correct = entry.getValue()[1];
            int pct = total == 0 ? 0 : Math.round(correct * 100f / total);
            review.add(reviewRow(entry.getKey(), correct + "/" + total, pct, categoryColor(entry.getKey())));
            review.add(Box.createVerticalStrut(10));
        }
        return review;
    }

    /* ============================================================ */
    /*  TIMER                                                      */
    /* ============================================================ */
    private void resetTimer() {
        stopTimer();
        timeLeft = TIME_PER_QUESTION;
        timerBar.setMaximum(TIME_PER_QUESTION);
        timerBar.setValue(timeLeft);
        timerBar.setForeground(PURPLE);
        qTimerLabel.setText(timeLeft + "s");
        startTimer();
    }

    private void startTimer() {
        timer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerBar.setValue(Math.max(0, timeLeft));
            qTimerLabel.setText(Math.max(0, timeLeft) + "s");
            if (timeLeft <= 10) timerBar.setForeground(RED);
            if (timeLeft <= 0) revealAnswer(true);
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void syncLiveStats() {
        int answered = totalAnswered;
        int remaining = Math.max(0, totalInSession - answered);
        int accuracy = answered == 0 ? 0 : Math.round(score * 100f / answered);

        qScoreLabel.setText("Score " + score + "/" + answered);
        modeLiveLabel.setText(currentMode == Mode.SEQUENTIAL ? "Sequential" : "Adaptive");
        accuracyLiveLabel.setText(answered == 0 ? "0%" : accuracy + "%");
        remainingLiveLabel.setText(remaining + " left");
        qStreakLabel.setText(currentStreak + " now / " + bestStreak + " best");
        sessionProgress.setMaximum(Math.max(1, totalInSession));
        sessionProgress.setValue(answered);
        sessionProgress.setString(answered + " / " + totalInSession);
    }

    private void refreshTopStats() {
        if (bestScoreLabel != null) bestScoreLabel.setText("Best " + UserProfileCache.getAptitudeScore() + "%");
        if (bankCountLabel != null) bankCountLabel.setText(QUESTION_BANK.length + " Questions");
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        refreshTopStats();
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    /* ============================================================ */
    /*  UI HELPERS                                                 */
    /* ============================================================ */
    private JToggleButton choiceTile(String title, String meta, String description, Color accent) {
        JToggleButton btn = new JToggleButton("<html><b>" + title + "</b><br><span style='font-size:9pt'>" + meta
            + "</span><br><span style='font-size:8pt'>" + description + "</span></html>") {
            boolean hover = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = isSelected()
                    ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Theme.isDark() ? 95 : 42)
                    : hover ? (Theme.isDark() ? new Color(42, 42, 58) : new Color(247, 248, 252)) : Theme.PANEL_BG;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(isSelected() ? accent : Theme.FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(accent);
                g2.fillRoundRect(0, 10, 5, getHeight() - 20, 5, 5);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Theme.PRIMARY_TEXT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 18, 12, 12));
        btn.setPreferredSize(new Dimension(0, 92));
        return btn;
    }

    private JPanel heroStat(String label, String value) {
        JPanel stat = new JPanel(new BorderLayout(0, 4));
        stat.setOpaque(false);
        stat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 75), 1, true),
            new EmptyBorder(12, 12, 12, 12)));
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(Color.WHITE);
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLbl.setForeground(new Color(255, 255, 255, 210));
        stat.add(valueLbl, BorderLayout.CENTER);
        stat.add(labelLbl, BorderLayout.SOUTH);
        return stat;
    }

    private JPanel statRow(String label, JLabel value, Color accent) {
        SurfacePanel row = new SurfacePanel(new BorderLayout(8, 0));
        row.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        name.setForeground(Theme.MUTED_TEXT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 12));
        value.setForeground(accent);
        row.add(name, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JPanel resultMetric(String label, String value, Color accent) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(accent);
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLbl.setForeground(Theme.MUTED_TEXT);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(labelLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel reviewRow(String name, String scoreText, int pct, Color accent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.PRIMARY_TEXT);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(pct);
        bar.setForeground(accent);
        bar.setBackground(Theme.isDark() ? new Color(45, 45, 60) : new Color(229, 231, 235));
        bar.setBorderPainted(false);
        JLabel scoreLbl = new JLabel(scoreText + "  " + pct + "%");
        scoreLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        scoreLbl.setForeground(accent);
        row.add(label, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(scoreLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel infoCard(String title, String body, Color accent) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 6));
        card.setAccent(accent);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Theme.PRIMARY_TEXT);
        JLabel bodyLbl = new JLabel("<html><body style='width:230px'>" + body + "</body></html>");
        bodyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bodyLbl.setForeground(Theme.MUTED_TEXT);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bodyLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapBottom(JComponent top, JComponent bottom) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.CENTER);
        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(Theme.PRIMARY_TEXT);
        return label;
    }

    private JLabel makeTinyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private JLabel makePillLabel(String text, Color color, boolean filled) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(filled ? Color.WHITE : color);
        label.setOpaque(true);
        label.setBackground(filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 34 : 18));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            new EmptyBorder(6, 12, 6, 12)));
        return label;
    }

    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 30 : 18));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));
        return btn;
    }

    /* ============================================================ */
    /*  DATA HELPERS                                               */
    /* ============================================================ */
    private int countQuestions(String category) {
        if ("All".equals(category)) return QUESTION_BANK.length;
        int count = 0;
        for (Question q : QUESTION_BANK) {
            if (q.category.equals(category)) count++;
        }
        return count;
    }

    private void updateSelectedSummary() {
        if (selectedSummaryLabel != null) {
            selectedSummaryLabel.setText(selectedCategory + " / "
                + (currentMode == Mode.SEQUENTIAL ? "Sequential Queue" : "Adaptive PriorityQueue")
                + " / " + Math.min(countQuestions(selectedCategory), 10) + " questions");
        }
    }

    private String diffLabel(int difficulty) {
        return switch (difficulty) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            default -> "Hard";
        };
    }

    private Color categoryColor(String category) {
        return switch (category) {
            case "Quantitative" -> GREEN;
            case "Logical" -> ORANGE;
            case "Verbal" -> PINK;
            case "Data Interpretation" -> BLUE;
            default -> PURPLE;
        };
    }

    private Color resultColor(int pct) {
        return pct >= 80 ? GREEN : pct >= 50 ? ORANGE : RED;
    }

    private String resultTitle(int pct) {
        return pct >= 80 ? "Excellent Accuracy" : pct >= 50 ? "Solid Attempt" : "Keep Practicing";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /* ============================================================ */
    /*  CUSTOM COMPONENTS                                          */
    /* ============================================================ */
    private static class GradientCard extends JPanel {
        private final Color start, end;

        GradientCard(LayoutManager layout, Color start, Color end) {
            super(layout);
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SurfacePanel extends JPanel {
        private Color accent;

        SurfacePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        void setAccent(Color accent) {
            this.accent = accent;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(Theme.FIELD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (accent != null) {
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class OptionButton extends JToggleButton {
        enum AnswerState { NORMAL, CORRECT, WRONG, DIMMED }

        private final String letter;
        private final Color accent;
        private AnswerState answerState = AnswerState.NORMAL;

        OptionButton(String letter, Color accent) {
            this.letter = letter;
            this.accent = accent;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(Theme.PRIMARY_TEXT);
            setBorder(new EmptyBorder(12, 58, 12, 14));
        }

        void setOptionText(String text) {
            setText("<html><body style='width:650px'>" + escapeHtml(text) + "</body></html>");
        }

        void setAnswerState(AnswerState state) {
            this.answerState = state;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = Theme.PANEL_BG;
            Color stroke = Theme.FIELD_BORDER;
            Color badge = accent;
            if (answerState == AnswerState.CORRECT) {
                fill = new Color(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), Theme.isDark() ? 55 : 28);
                stroke = GREEN;
                badge = GREEN;
            } else if (answerState == AnswerState.WRONG) {
                fill = new Color(RED.getRed(), RED.getGreen(), RED.getBlue(), Theme.isDark() ? 55 : 28);
                stroke = RED;
                badge = RED;
            } else if (answerState == AnswerState.DIMMED) {
                fill = Theme.isDark() ? new Color(32, 32, 44) : new Color(248, 249, 252);
                stroke = Theme.FIELD_BORDER;
                badge = Theme.MUTED_TEXT;
            } else if (isSelected()) {
                fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Theme.isDark() ? 68 : 28);
                stroke = accent;
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(badge);
            g2.fillRoundRect(12, 10, 32, getHeight() - 20, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, 28 - fm.stringWidth(letter) / 2, getHeight() / 2 + fm.getAscent() / 2 - 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
