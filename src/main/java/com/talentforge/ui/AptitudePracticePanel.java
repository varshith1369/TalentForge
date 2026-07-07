package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Aptitude Practice module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Queue&lt;Question&gt;</b> — sequential question delivery. Questions
 *       are polled one by one so the user never skips ahead.</li>
 *   <li><b>PriorityQueue&lt;Question&gt;</b> — adaptive difficulty. After each
 *       answer the PriorityQueue re-orders remaining questions: correct answers
 *       surface harder ones, wrong answers re-surface easier ones first.</li>
 * </ul>
 */
public class AptitudePracticePanel extends JPanel {

    /* ============================================================ */
    /*  QUESTION MODEL                                              */
    /* ============================================================ */
    private static class Question implements Comparable<Question> {
        final String category, text;
        final String[] options;
        final int correctIndex;
        final int difficulty; // 1=Easy, 2=Medium, 3=Hard
        int priority;         // lower = served first in adaptive mode

        Question(String category, int difficulty, String text, String[] options, int correctIndex) {
            this.category = category; this.difficulty = difficulty;
            this.text = text; this.options = options; this.correctIndex = correctIndex;
            this.priority = difficulty;
        }

        @Override public int compareTo(Question o) {
            return Integer.compare(this.priority, o.priority);
        }
    }

    /* ============================================================ */
    /*  BUILT-IN QUESTION BANK (25 questions)                       */
    /* ============================================================ */
    private static final Question[] QUESTION_BANK = {
        // ---- Quantitative ----
        new Question("Quantitative",1,"If a train travels 60 km in 1 hour, how long will it take to travel 150 km?",
            new String[]{"2 hours","2.5 hours","3 hours","3.5 hours"},1),
        new Question("Quantitative",1,"What is 15% of 200?",
            new String[]{"20","25","30","35"},2),
        new Question("Quantitative",2,"A shopkeeper marks an article 40% above cost price and gives a 20% discount. Profit%?",
            new String[]{"10%","12%","14%","16%"},1),
        new Question("Quantitative",2,"Find the Simple Interest on ₹5000 at 8% per annum for 3 years.",
            new String[]{"₹1000","₹1200","₹1500","₹1600"},1),
        new Question("Quantitative",2,"Two pipes can fill a tank in 12 min and 15 min respectively. Time to fill together?",
            new String[]{"6 min","6.67 min","7 min","8 min"},1),
        new Question("Quantitative",3,"If x + y = 10 and xy = 21, find x² + y².",
            new String[]{"48","56","58","62"},2),
        new Question("Quantitative",3,"A boat covers 24 km upstream and 36 km downstream in 6 hours each. Speed of stream?",
            new String[]{"1 km/h","2 km/h","3 km/h","4 km/h"},1),
        // ---- Logical Reasoning ----
        new Question("Logical",1,"Which number comes next: 2, 6, 12, 20, 30, ?",
            new String[]{"40","42","44","46"},1),
        new Question("Logical",1,"If A is the brother of B, B is the sister of C, C is the father of D, then A is D's?",
            new String[]{"Uncle","Father","Nephew","Brother"},0),
        new Question("Logical",2,"Find the odd one out: 3, 5, 7, 11, 15, 17",
            new String[]{"5","11","15","17"},2),
        new Question("Logical",2,"In a row of 40 students, Ramesh is 11th from the left. What is his position from the right?",
            new String[]{"28th","29th","30th","31st"},2),
        new Question("Logical",2,"If ROAD is coded as 52 and CAR is coded as 22, what is TRUCK coded as?",
            new String[]{"62","67","72","77"},1),
        new Question("Logical",3,"Three friends A, B, C jog around a circular track of 120m. A takes 30s, B takes 40s, C takes 60s. When do they first meet?",
            new String[]{"60s","90s","120s","240s"},2),
        // ---- Verbal ----
        new Question("Verbal",1,"Choose the synonym of BENEVOLENT:",
            new String[]{"Cruel","Kind","Strict","Lazy"},1),
        new Question("Verbal",1,"Choose the antonym of VERBOSE:",
            new String[]{"Talkative","Concise","Lengthy","Wordy"},1),
        new Question("Verbal",2,"Choose the correctly spelt word:",
            new String[]{"Grammer","Grammar","Gramer","Gramar"},1),
        new Question("Verbal",2,"Identify the error: 'He don't know the answer.'",
            new String[]{"He","don't","the","answer"},1),
        // ---- Data Interpretation ----
        new Question("Data Interpretation",2,"A pie chart shows: A=30%, B=25%, C=20%, D=15%, E=10%. If total is 1000, what is B+D?",
            new String[]{"350","375","400","425"},2),
        new Question("Data Interpretation",2,"Sales in 2021: Jan=150, Feb=120, Mar=180, Apr=160. Average monthly sales?",
            new String[]{"145","152.5","155","157.5"},1),
        new Question("Data Interpretation",3,"A bar chart shows revenue: Q1=₹2L, Q2=₹3L, Q3=₹2.5L, Q4=₹4L. What % increase from Q1 to Q4?",
            new String[]{"80%","90%","100%","110%"},2),
        // ---- Number Series ----
        new Question("Quantitative",2,"Find next: 1, 1, 2, 3, 5, 8, 13, ?",
            new String[]{"18","20","21","22"},2),
        new Question("Quantitative",1,"What is the LCM of 12 and 18?",
            new String[]{"24","36","48","72"},1),
        new Question("Logical",1,"Arrange in order: DCBA, ABCD, BACD, CDAB.",
            new String[]{"ABCD, BACD, DCBA, CDAB","ABCD, BACD, CDAB, DCBA","DCBA, CDAB, BACD, ABCD","CDAB, ABCD, BACD, DCBA"},1),
        new Question("Verbal",2,"Fill the blank: The committee has ______ its decision.",
            new String[]{"made","make","making","makes"},0),
        new Question("Quantitative",3,"If the radius of a circle is doubled, by what factor does the area increase?",
            new String[]{"2x","3x","4x","8x"},2),
    };

    /* ============================================================ */
    /*  DATA STRUCTURES                                             */
    /* ============================================================ */
    /** Queue — sequential question delivery (FIFO) */
    private final LinkedList<Question> questionQueue = new LinkedList<>();

    /** PriorityQueue — adaptive difficulty (min-heap by priority) */
    private final PriorityQueue<Question> adaptiveQueue = new PriorityQueue<>();

    /* ============================================================ */
    /*  SESSION STATE                                               */
    /* ============================================================ */
    private enum Mode { SEQUENTIAL, ADAPTIVE }
    private enum SessionState { HOME, PLAYING, RESULT }

    private Mode currentMode = Mode.SEQUENTIAL;
    private SessionState state = SessionState.HOME;
    private Question currentQuestion;
    private int score = 0;
    private int totalAnswered = 0;
    private int totalInSession = 0;
    private String selectedCategory = "All";

    // Timer
    private javax.swing.Timer timer;
    private int timeLeft = 30;
    private static final int TIME_PER_QUESTION = 30;

    // UI
    private final JPanel mainCards;
    private final CardLayout cards = new CardLayout();
    private JLabel qNumberLabel, qCategoryLabel, qTimerLabel, qScoreLabel;
    private JLabel questionLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton nextBtn;
    private JProgressBar timerBar;
    private JPanel homePanel, playPanel, resultPanel;

    public AptitudePracticePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        mainCards = new JPanel(cards);
        mainCards.setOpaque(false);

        homePanel = buildHomePanel();
        playPanel = buildPlayPanel();
        resultPanel = new JPanel(); // built dynamically
        resultPanel.setOpaque(false);

        mainCards.add(homePanel, "home");
        mainCards.add(playPanel, "play");

        add(buildTopBar(), BorderLayout.NORTH);
        add(mainCards, BorderLayout.CENTER);

        cards.show(mainCards, "home");
        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  TOP BAR                                                     */
    /* ============================================================ */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PANEL_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.FIELD_BORDER),
            new EmptyBorder(12,20,12,20)));

        JLabel icon = new JLabel("🧠  Aptitude Practice");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        icon.setForeground(Theme.PRIMARY_TEXT);

        JLabel sub = new JLabel("Quantitative · Logical · Verbal · Data Interpretation");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(icon, BorderLayout.CENTER);
        left.add(sub, BorderLayout.SOUTH);

        bar.add(left, BorderLayout.CENTER);
        return bar;
    }

    /* ============================================================ */
    /*  HOME PANEL                                                  */
    /* ============================================================ */
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.PANEL_BG);
        card.setBorder(new EmptyBorder(32, 40, 32, 40));

        JLabel title = new JLabel("Choose Your Practice Mode");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Select category and mode to begin");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED_TEXT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Category buttons
        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        catLabel.setForeground(Theme.PRIMARY_TEXT);
        catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        catLabel.setBorder(new EmptyBorder(20, 0, 8, 0));

        JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        catRow.setOpaque(false);
        catRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] cats = {"All", "Quantitative", "Logical", "Verbal", "Data Interpretation"};
        Color[] catColors = {
            Theme.PRIMARY_START, new Color(34,197,94), new Color(245,158,11),
            new Color(236,72,153), new Color(14,165,233)
        };
        ButtonGroup catGroup = new ButtonGroup();
        for (int i = 0; i < cats.length; i++) {
            JToggleButton btn = createCatToggle(cats[i], catColors[i]);
            if (i == 0) btn.setSelected(true);
            catGroup.add(btn);
            catRow.add(btn);
        }

        // Mode selection
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        modeLabel.setForeground(Theme.PRIMARY_TEXT);
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeLabel.setBorder(new EmptyBorder(16, 0, 8, 0));

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modeRow.setOpaque(false);
        modeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JToggleButton seqBtn = createCatToggle("📋 Sequential (Queue)", new Color(99,102,241));
        JToggleButton adaptBtn = createCatToggle("🎯 Adaptive (PriorityQueue)", new Color(139,92,246));
        seqBtn.setSelected(true);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(seqBtn); modeGroup.add(adaptBtn);
        seqBtn.addActionListener(e -> currentMode = Mode.SEQUENTIAL);
        adaptBtn.addActionListener(e -> currentMode = Mode.ADAPTIVE);
        modeRow.add(seqBtn); modeRow.add(adaptBtn);

        // Start button
        JButton startBtn = createGradientButton("▶  Start Practice", 200, 44);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> startSession());

        // Stats row
        int savedScore = UserProfileCache.getAptitudeScore();
        JLabel savedScoreLbl = new JLabel("Your best score: " + savedScore + "%");
        savedScoreLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        savedScoreLbl.setForeground(Theme.MUTED_TEXT);
        savedScoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        savedScoreLbl.setBorder(new EmptyBorder(12, 0, 0, 0));

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(catLabel);
        card.add(catRow);
        card.add(modeLabel);
        card.add(modeRow);
        card.add(Box.createVerticalStrut(24));
        card.add(startBtn);
        card.add(savedScoreLbl);

        panel.add(card);
        return panel;
    }

    private JToggleButton createCatToggle(String text, Color color) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(color); btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
                if (!text.contains("Adaptive") && !text.contains("Sequential")) {
                    selectedCategory = text;
                }
            } else {
                btn.setBackground(Theme.PANEL_BG); btn.setForeground(color);
                btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
            }
        });
        btn.setBackground(Theme.PANEL_BG); btn.setForeground(color);
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        return btn;
    }

    /* ============================================================ */
    /*  PLAY PANEL                                                  */
    /* ============================================================ */
    private JPanel buildPlayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 40, 24, 40));

        // ---- HEADER ROW ----
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        qNumberLabel = new JLabel("Q1 of 10");
        qNumberLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qNumberLabel.setForeground(Theme.PRIMARY_TEXT);

        qCategoryLabel = new JLabel("Category");
        qCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qCategoryLabel.setForeground(Theme.MUTED_TEXT);

        qTimerLabel = new JLabel("⏱ 30s");
        qTimerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qTimerLabel.setForeground(new Color(245, 158, 11));

        qScoreLabel = new JLabel("Score: 0/0");
        qScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qScoreLabel.setForeground(new Color(34, 197, 94));

        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);
        headerLeft.add(qNumberLabel, BorderLayout.CENTER);
        headerLeft.add(qCategoryLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        headerRight.setOpaque(false);
        headerRight.add(qScoreLabel);
        headerRight.add(qTimerLabel);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Timer bar
        timerBar = new JProgressBar(0, TIME_PER_QUESTION);
        timerBar.setValue(TIME_PER_QUESTION);
        timerBar.setForeground(new Color(99, 102, 241));
        timerBar.setBackground(Theme.isDark() ? new Color(45,45,60) : new Color(230,232,240));
        timerBar.setBorderPainted(false);
        timerBar.setPreferredSize(new Dimension(0, 6));

        // ---- QUESTION CARD ----
        ElevatedCard qCard = new ElevatedCard(new BorderLayout());
        JPanel qInner = new JPanel();
        qInner.setLayout(new BoxLayout(qInner, BoxLayout.Y_AXIS));
        qInner.setOpaque(false);
        qInner.setBorder(new EmptyBorder(24, 24, 16, 24));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        questionLabel.setForeground(Theme.PRIMARY_TEXT);

        // Wrap in HTML for word wrap
        questionLabel.setText("<html><body style='width:500px'></body></html>");

        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        Color[] optColors = {
            new Color(99,102,241), new Color(236,72,153),
            new Color(34,197,94), new Color(245,158,11)
        };
        String[] optLabels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            JPanel optRow = new JPanel(new BorderLayout(12, 0));
            optRow.setOpaque(false);
            optRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            optRow.setBorder(new EmptyBorder(4, 0, 4, 0));

            JLabel badge = new JLabel(optLabels[i]);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
            badge.setForeground(optColors[i]);
            badge.setPreferredSize(new Dimension(24, 24));
            badge.setHorizontalAlignment(SwingConstants.CENTER);

            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            optionButtons[i].setForeground(Theme.PRIMARY_TEXT);
            optionButtons[i].setOpaque(false);
            optionGroup.add(optionButtons[i]);

            optRow.add(badge, BorderLayout.WEST);
            optRow.add(optionButtons[i], BorderLayout.CENTER);
            optionsPanel.add(optRow);
        }

        qInner.add(questionLabel);
        qInner.add(optionsPanel);
        qCard.add(qInner, BorderLayout.CENTER);

        // ---- BUTTONS ----
        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton quitBtn = createOutlineButton("✕ End Session", new Color(239, 68, 68));
        quitBtn.addActionListener(e -> endSession(true));

        nextBtn = createGradientButton("Next →", 130, 38);
        nextBtn.addActionListener(e -> nextQuestion());

        btnRow.add(quitBtn, BorderLayout.WEST);
        btnRow.add(nextBtn, BorderLayout.EAST);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(header, BorderLayout.CENTER);
        topBar.add(timerBar, BorderLayout.SOUTH);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(qCard, BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    /* ============================================================ */
    /*  SESSION LOGIC                                               */
    /* ============================================================ */
    private void startSession() {
        score = 0; totalAnswered = 0;

        // Gather questions matching selected category
        java.util.List<Question> pool = new ArrayList<>();
        for (Question q : QUESTION_BANK) {
            if (selectedCategory.equals("All") || q.category.equals(selectedCategory)) {
                pool.add(q);
            }
        }
        Collections.shuffle(pool);

        if (currentMode == Mode.SEQUENTIAL) {
            // Fill Queue sequentially
            questionQueue.clear();
            questionQueue.addAll(pool);
            totalInSession = Math.min(pool.size(), 10);
        } else {
            // Fill PriorityQueue for adaptive mode
            adaptiveQueue.clear();
            for (Question q : pool) {
                q.priority = q.difficulty; // reset
                adaptiveQueue.offer(q);
            }
            totalInSession = Math.min(pool.size(), 10);
        }

        if (totalInSession == 0) {
            JOptionPane.showMessageDialog(this, "No questions in that category yet!", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cards.show(mainCards, "play");
        loadNextQuestion();
        startTimer();
    }

    private void loadNextQuestion() {
        if (currentMode == Mode.SEQUENTIAL) {
            if (questionQueue.isEmpty() || totalAnswered >= totalInSession) { endSession(false); return; }
            currentQuestion = questionQueue.poll();   // Queue.poll() — FIFO
        } else {
            if (adaptiveQueue.isEmpty() || totalAnswered >= totalInSession) { endSession(false); return; }
            currentQuestion = adaptiveQueue.poll();   // PriorityQueue.poll() — adaptive
        }

        // Update UI
        qNumberLabel.setText("Q" + (totalAnswered + 1) + " of " + totalInSession);
        qCategoryLabel.setText(currentQuestion.category + " · " + diffLabel(currentQuestion.difficulty));
        questionLabel.setText("<html><body style='width:500px;font-family:Segoe UI;font-size:14pt'>"
            + currentQuestion.text + "</body></html>");

        optionGroup.clearSelection();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(currentQuestion.options[i]);
            optionButtons[i].setForeground(Theme.PRIMARY_TEXT);
            optionButtons[i].setEnabled(true);
        }
        nextBtn.setText("Next →");
        nextBtn.setEnabled(true);

        resetTimer();
    }

    private String diffLabel(int d) {
        return switch (d) { case 1 -> "Easy"; case 2 -> "Medium"; default -> "Hard"; };
    }

    private void nextQuestion() {
        // Check selected answer
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) { selected = i; break; }
        }
        if (selected == -1 && timeLeft > 0) {
            JOptionPane.showMessageDialog(this, "Please select an answer!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        stopTimer();
        totalAnswered++;

        boolean correct = selected == currentQuestion.correctIndex;
        if (correct) {
            score++;
            // Adaptive: correct → bump priority up (serve harder next)
            if (currentMode == Mode.ADAPTIVE && !adaptiveQueue.isEmpty()) {
                java.util.List<Question> temp = new ArrayList<>(adaptiveQueue);
                adaptiveQueue.clear();
                for (Question q : temp) { q.priority = Math.max(1, q.priority - 1); adaptiveQueue.offer(q); }
            }
        } else {
            // Adaptive: wrong → bump priority down (serve easier next)
            if (currentMode == Mode.ADAPTIVE && !adaptiveQueue.isEmpty()) {
                java.util.List<Question> temp = new ArrayList<>(adaptiveQueue);
                adaptiveQueue.clear();
                for (Question q : temp) { q.priority = q.priority + 1; adaptiveQueue.offer(q); }
            }
        }

        // Show correct answer
        optionButtons[currentQuestion.correctIndex].setForeground(new Color(34, 197, 94));
        if (selected != -1 && !correct) optionButtons[selected].setForeground(new Color(239, 68, 68));
        for (JRadioButton b : optionButtons) b.setEnabled(false);

        qScoreLabel.setText("Score: " + score + "/" + totalAnswered);

        if (totalAnswered >= totalInSession || (currentMode == Mode.SEQUENTIAL && questionQueue.isEmpty())
                || (currentMode == Mode.ADAPTIVE && adaptiveQueue.isEmpty())) {
            nextBtn.setText("View Results");
            nextBtn.addActionListener(e -> endSession(false));
        } else {
            javax.swing.Timer delay = new javax.swing.Timer(900, ev -> loadNextQuestion());
            delay.setRepeats(false);
            delay.start();
        }
    }

    private void endSession(boolean quit) {
        stopTimer();
        if (!quit && totalAnswered == 0) { cards.show(mainCards, "home"); return; }

        int pct = totalAnswered > 0 ? (score * 100 / totalAnswered) : 0;

        // Save if this is the best score
        int saved = UserProfileCache.getAptitudeScore();
        if (pct > saved) {
            UserProfileCache.setAptitudeScore(pct);
            StatsService.saveAptitudeScore(UserProfileCache.getCurrentUserId(), pct);
        }

        showResult(pct, quit);
    }

    private void showResult(int pct, boolean quit) {
        mainCards.remove(resultPanel);
        resultPanel = buildResultPanel(pct, quit);
        mainCards.add(resultPanel, "result");
        cards.show(mainCards, "result");
    }

    private JPanel buildResultPanel(int pct, boolean quit) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.PANEL_BG);
        card.setBorder(new EmptyBorder(32, 48, 32, 48));

        String emoji = pct >= 80 ? "🎉" : pct >= 50 ? "👍" : "💪";
        String msg   = pct >= 80 ? "Excellent!" : pct >= 50 ? "Good job!" : "Keep practicing!";

        JLabel emjLbl = new JLabel(emoji);
        emjLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emjLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLbl = new JLabel(pct + "%");
        scoreLbl.setFont(new Font("Segoe UI", Font.BOLD, 48));
        scoreLbl.setForeground(pct >= 80 ? new Color(34,197,94) : pct >= 50 ? new Color(245,158,11) : new Color(239,68,68));
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLbl = new JLabel(msg);
        msgLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        msgLbl.setForeground(Theme.PRIMARY_TEXT);
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detailLbl = new JLabel("Correct: " + score + " / " + totalAnswered + " questions");
        detailLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailLbl.setForeground(Theme.MUTED_TEXT);
        detailLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailLbl.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel savedLbl = new JLabel(!quit && pct > UserProfileCache.getAptitudeScore() ?
            "🏆 New best score saved!" : "Best score: " + UserProfileCache.getAptitudeScore() + "%");
        savedLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        savedLbl.setForeground(new Color(99, 102, 241));
        savedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        savedLbl.setBorder(new EmptyBorder(4, 0, 24, 0));

        JButton homeBtn = createGradientButton("Try Again", 160, 40);
        homeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        homeBtn.addActionListener(e -> { cards.show(mainCards, "home"); });

        card.add(emjLbl); card.add(Box.createVerticalStrut(8));
        card.add(scoreLbl); card.add(msgLbl); card.add(detailLbl); card.add(savedLbl);
        card.add(homeBtn);

        panel.add(card);
        return panel;
    }

    /* ============================================================ */
    /*  TIMER                                                       */
    /* ============================================================ */
    private void resetTimer() {
        stopTimer();
        timeLeft = TIME_PER_QUESTION;
        timerBar.setValue(timeLeft);
        qTimerLabel.setText("⏱ " + timeLeft + "s");
        qTimerLabel.setForeground(new Color(245, 158, 11));
        startTimer();
    }

    private void startTimer() {
        timer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerBar.setValue(timeLeft);
            qTimerLabel.setText("⏱ " + timeLeft + "s");
            if (timeLeft <= 10) qTimerLabel.setForeground(new Color(239, 68, 68));
            if (timeLeft <= 0) nextQuestion();
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) { timer.stop(); timer = null; }
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        repaint();
    }

    /* ============================================================ */
    /*  BUTTON HELPERS                                              */
    /* ============================================================ */
    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,Theme.PRIMARY_START,getWidth(),0,Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue(),18));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));
        return btn;
    }
}
