package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Mock Interview module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Stack&lt;InterviewQuestion&gt;</b> — navigation history. Each answered
 *       question is pushed onto the stack; "Previous" pops it to review answers.</li>
 *   <li><b>LinkedList&lt;InterviewQuestion&gt;</b> — ordered question queue for the
 *       current session, navigated front-to-back.</li>
 * </ul>
 */
public class MockInterviewPanel extends JPanel {

    /* ============================================================ */
    /*  QUESTION MODEL                                              */
    /* ============================================================ */
    private static class InterviewQuestion {
        final String category, question, hint, sampleAnswer;
        String userAnswer = "";
        int rating = 0; // 1–5 stars

        InterviewQuestion(String category, String question, String hint, String sampleAnswer) {
            this.category = category; this.question = question;
            this.hint = hint; this.sampleAnswer = sampleAnswer;
        }
    }

    /* ============================================================ */
    /*  BUILT-IN QUESTION BANK                                      */
    /* ============================================================ */
    private static final InterviewQuestion[] ALL_QUESTIONS = {
        // ---- HR ----
        new InterviewQuestion("HR","Tell me about yourself.",
            "Structure: Present → Past → Future. Keep it under 2 minutes.",
            "I am a final-year CSE student passionate about software development. I have worked on projects involving Java, SQL, and web technologies. I am looking to join a company where I can contribute to impactful products while continuing to grow."),
        new InterviewQuestion("HR","What are your greatest strengths?",
            "Pick 2–3 strengths and support each with a brief example.",
            "My greatest strengths are problem-solving, attention to detail, and adaptability. For instance, I refactored a legacy module during my internship, reducing processing time by 30%."),
        new InterviewQuestion("HR","Where do you see yourself in 5 years?",
            "Align your goals with the company's growth. Show ambition but be realistic.",
            "In 5 years, I see myself as a senior engineer leading a small team, having shipped multiple products. I plan to deepen my expertise in system design and distributed systems."),
        new InterviewQuestion("HR","Why do you want to work for this company?",
            "Research the company's mission, products, and culture beforehand.",
            "I admire your company's commitment to building developer-first tools. The engineering blog entries on distributed caching deeply resonated with my interests."),
        new InterviewQuestion("HR","Describe a challenge you overcame.",
            "Use the STAR method: Situation → Task → Action → Result.",
            "During a hackathon, our team lost a key member midway. I redistributed tasks, built the backend myself, and we delivered the product on time, winning 2nd place."),
        new InterviewQuestion("HR","What is your biggest weakness?",
            "Be honest, then explain how you are actively working on it.",
            "I sometimes overthink edge cases, which can slow early development. I've been practising time-boxing design decisions to stay productive."),
        new InterviewQuestion("HR","Do you prefer working alone or in a team?",
            "Both have merits — demonstrate flexibility.",
            "I enjoy both. I like the autonomy of independent work for deep-focus tasks, and I thrive in team settings for brainstorming and code review cycles."),
        // ---- Technical ----
        new InterviewQuestion("Technical","Explain the difference between a process and a thread.",
            "Focus on memory space, creation overhead, and communication.",
            "A process is an independent program instance with its own memory space. A thread is a lightweight unit of execution within a process, sharing memory. Threads communicate via shared memory; processes communicate via IPC mechanisms."),
        new InterviewQuestion("Technical","What is polymorphism? Give an example.",
            "Mention compile-time (overloading) vs run-time (overriding) polymorphism.",
            "Polymorphism allows objects of different classes to be treated through a common interface. Run-time polymorphism: a Shape reference can point to a Circle or Rectangle, and calling draw() invokes the correct method based on the actual object."),
        new InterviewQuestion("Technical","What is the time complexity of binary search?",
            "Think about how the search space halves each step.",
            "O(log n) — because each comparison halves the search space. Space complexity is O(1) for iterative, O(log n) for recursive."),
        new InterviewQuestion("Technical","Explain ACID properties in databases.",
            "Atomicity, Consistency, Isolation, Durability — give an example for each.",
            "ACID ensures reliable transactions. Atomicity: all operations succeed or none do. Consistency: data stays valid. Isolation: concurrent transactions don't interfere. Durability: committed data persists even after crashes."),
        new InterviewQuestion("Technical","What is a deadlock and how do you prevent it?",
            "Think: mutual exclusion, hold-and-wait, no preemption, circular wait.",
            "Deadlock occurs when two processes each hold a resource the other needs. Prevention strategies: impose ordering on locks, use timeouts, or use lock hierarchies to break circular waits."),
        new InterviewQuestion("Technical","Explain REST vs SOAP.",
            "Focus on protocol, flexibility, and use cases.",
            "REST uses HTTP methods (GET, POST, PUT, DELETE) with JSON; it is lightweight and stateless. SOAP is a protocol with strict XML messaging, built-in security (WS-Security), and ACID compliance — suited for enterprise banking systems."),
        new InterviewQuestion("Technical","What is the difference between HashMap and HashTable?",
            "Key differences: synchronization, null keys, and performance.",
            "HashMap is non-synchronized (faster) and allows one null key and multiple null values. HashTable is synchronized (thread-safe) and does not allow null keys or values."),
        new InterviewQuestion("Technical","What is a microservices architecture?",
            "Contrast with monolith; mention communication (REST/gRPC), deployment, scalability.",
            "Microservices decompose an application into small, independently deployable services each responsible for a single business domain. They communicate via REST or message queues and can be scaled individually. Trade-offs include network latency and operational complexity."),
        // ---- Behavioral ----
        new InterviewQuestion("Behavioral","Tell me about a time you worked in a team under pressure.",
            "Use STAR: focus on your specific contribution.",
            "During our final-year project, we had a 48-hour deadline extension after requirements changed. I coordinated the task split, built the authentication module, and tested integration overnight. We delivered on time."),
        new InterviewQuestion("Behavioral","Describe a situation where you had to learn something quickly.",
            "Show adaptability and a structured approach to learning.",
            "I was assigned to fix a bug in a React codebase on my first week at an internship. I used the official docs, YouTube tutorials, and pair-programmed with a senior. I fixed the bug and learned state management in 2 days."),
        new InterviewQuestion("Behavioral","How do you handle disagreements with teammates?",
            "Emphasize listening, empathy, and data-driven resolution.",
            "I start by listening fully to their perspective. I then present my reasoning with data or prototypes. If we still disagree, I escalate to the team lead for a tie-breaking decision based on project goals."),
        new InterviewQuestion("Behavioral","Give an example of when you went above and beyond.",
            "Show initiative and ownership.",
            "I noticed our CI/CD pipeline took 20 minutes for builds. Without being asked, I researched Docker layer caching and reduced build time to 6 minutes, which the whole team appreciated."),
        new InterviewQuestion("Behavioral","How do you prioritize tasks when everything is urgent?",
            "Mention frameworks like Eisenhower matrix or impact-vs-effort.",
            "I list all tasks, estimate impact and effort, then use the Eisenhower matrix. High-impact, low-effort tasks get done first. I communicate timelines clearly if I have to push lower-priority items."),
    };

    /* ============================================================ */
    /*  DATA STRUCTURES                                             */
    /* ============================================================ */
    /** LinkedList — ordered question sequence for the current session */
    private LinkedList<InterviewQuestion> sessionQueue = new LinkedList<>();

    /** Stack — history of answered questions (for Previous/review) */
    private final Stack<InterviewQuestion> historyStack = new Stack<>();

    /* ============================================================ */
    /*  SESSION STATE                                               */
    /* ============================================================ */
    private enum SessionState { HOME, PLAYING, REVIEWING, RESULT }
    private SessionState state = SessionState.HOME;

    private InterviewQuestion currentQuestion;
    private String selectedCategory = "All";
    private int sessionTotal = 0;
    private int ratingSum = 0;
    private int ratingCount = 0;

    // UI
    private final JPanel mainCards;
    private final CardLayout cards = new CardLayout();
    private JLabel qCountLabel, qCategoryBadge;
    private JTextArea answerArea;
    private JLabel questionLabel;
    private JPanel starsPanel;
    private JButton[] starBtns = new JButton[5];
    private int selectedRating = 0;
    private JButton prevBtn, nextBtn, hintBtn;
    private JPanel homePanel, playPanel;

    public MockInterviewPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        mainCards = new JPanel(cards);
        mainCards.setOpaque(false);

        homePanel = buildHomePanel();
        playPanel = buildPlayPanel();

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

        JLabel icon = new JLabel("🎤  Mock Interview");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        icon.setForeground(Theme.PRIMARY_TEXT);

        JLabel sub = new JLabel("HR · Technical · Behavioral — Practice your interview responses");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(icon, BorderLayout.CENTER);
        left.add(sub, BorderLayout.SOUTH);

        JLabel sessions = new JLabel("Sessions: " + UserProfileCache.getMockInterviews());
        sessions.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sessions.setForeground(new Color(99, 102, 241));

        bar.add(left, BorderLayout.CENTER);
        bar.add(sessions, BorderLayout.EAST);
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
        card.setBorder(new EmptyBorder(32, 48, 32, 48));

        JLabel title = new JLabel("🎤 Start Mock Interview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Choose category and answer questions at your own pace");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED_TEXT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 20, 0));

        // Category cards
        JLabel catTitle = new JLabel("Select Category:");
        catTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        catTitle.setForeground(Theme.PRIMARY_TEXT);
        catTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        catTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel catGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        catGrid.setOpaque(false);
        catGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] cats = {
            {"All", "🌐", "#6366F1"},
            {"HR", "👤", "#34C759"},
            {"Technical", "💻", "#0A84FF"},
            {"Behavioral", "🧠", "#FF9F0A"},
        };
        ButtonGroup grp = new ButtonGroup();
        for (String[] cat : cats) {
            Color c = Color.decode(cat[2]);
            JToggleButton btn = buildCatCard(cat[0], cat[1], c);
            if (cat[0].equals("All")) btn.setSelected(true);
            btn.addItemListener(e -> { if (btn.isSelected()) selectedCategory = cat[0]; });
            grp.add(btn);
            catGrid.add(btn);
        }

        // Number of questions
        JLabel numTitle = new JLabel("Number of questions: 5");
        numTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        numTitle.setForeground(Theme.MUTED_TEXT);
        numTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        numTitle.setBorder(new EmptyBorder(16, 0, 4, 0));

        JSlider numSlider = new JSlider(3, 10, 5);
        numSlider.setOpaque(false);
        numSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        numSlider.addChangeListener(e -> numTitle.setText("Number of questions: " + numSlider.getValue()));

        JButton startBtn = createGradientButton("▶  Start Interview", 200, 44);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> startSession(numSlider.getValue()));

        // Tips
        JLabel tip = new JLabel("<html><body style='width:340px;color:#888;font-size:11pt'>" +
            "💡 Tip: Use the STAR method for behavioral questions: " +
            "<b>S</b>ituation → <b>T</b>ask → <b>A</b>ction → <b>R</b>esult</body></html>");
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);
        tip.setBorder(new EmptyBorder(16, 0, 0, 0));

        card.add(title); card.add(sub);
        card.add(catTitle); card.add(catGrid);
        card.add(numTitle); card.add(numSlider);
        card.add(Box.createVerticalStrut(20)); card.add(startBtn); card.add(tip);

        panel.add(card);
        return panel;
    }

    private JToggleButton buildCatCard(String name, String emoji, Color color) {
        JToggleButton btn = new JToggleButton("<html><center>" + emoji + "<br/>" + name + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(100, 70));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue(),35));
                btn.setForeground(color);
                btn.setBorder(BorderFactory.createLineBorder(color, 2, true));
            } else {
                btn.setBackground(Theme.PANEL_BG);
                btn.setForeground(Theme.MUTED_TEXT);
                btn.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
            }
        });
        btn.setBackground(Theme.PANEL_BG);
        btn.setForeground(Theme.MUTED_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        return btn;
    }

    /* ============================================================ */
    /*  PLAY PANEL                                                  */
    /* ============================================================ */
    private JPanel buildPlayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));

        // ---- TOP STATUS BAR ----
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        qCountLabel = new JLabel("Question 1 of 5");
        qCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qCountLabel.setForeground(Theme.PRIMARY_TEXT);

        qCategoryBadge = new JLabel("HR");
        qCategoryBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        qCategoryBadge.setForeground(Color.WHITE);
        qCategoryBadge.setBackground(new Color(99, 102, 241));
        qCategoryBadge.setOpaque(true);
        qCategoryBadge.setBorder(new EmptyBorder(2, 8, 2, 8));

        statusBar.add(qCountLabel, BorderLayout.CENTER);
        statusBar.add(qCategoryBadge, BorderLayout.EAST);

        // ---- QUESTION CARD ----
        ElevatedCard qCard = new ElevatedCard(new BorderLayout());
        JPanel qInner = new JPanel(new BorderLayout(0, 12));
        qInner.setOpaque(false);
        qInner.setBorder(new EmptyBorder(20, 22, 20, 22));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        questionLabel.setForeground(Theme.PRIMARY_TEXT);

        // Answer text area
        answerArea = new JTextArea(6, 40);
        answerArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        answerArea.setForeground(Theme.PRIMARY_TEXT);
        answerArea.setBackground(Theme.isDark() ? new Color(28, 28, 42) : new Color(248, 249, 252));
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)));
        answerArea.putClientProperty("JTextArea.placeholder", "Type your answer here...");

        JScrollPane answerScroll = new JScrollPane(answerArea);
        answerScroll.setBorder(null);

        // Hint + Sample answer toggle
        hintBtn = createOutlineButton("💡 Show Hint", new Color(245, 158, 11));
        JButton sampleBtn = createOutlineButton("📖 Sample", new Color(14, 165, 233));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.add(hintBtn);
        actionRow.add(sampleBtn);

        hintBtn.addActionListener(e -> {
            if (currentQuestion != null)
                JOptionPane.showMessageDialog(this, "💡 " + currentQuestion.hint, "Hint", JOptionPane.INFORMATION_MESSAGE);
        });
        sampleBtn.addActionListener(e -> {
            if (currentQuestion != null)
                JOptionPane.showMessageDialog(this, "<html><body style='width:400px'>" +
                    "<b>Sample Answer:</b><br/><br/>" + currentQuestion.sampleAnswer + "</body></html>",
                    "Sample Answer", JOptionPane.INFORMATION_MESSAGE);
        });

        // ---- STAR RATING ----
        JLabel rateLabel = new JLabel("Rate your answer:");
        rateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rateLabel.setForeground(Theme.MUTED_TEXT);

        starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starsPanel.setOpaque(false);
        starsPanel.add(rateLabel);

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            starBtns[i] = new JButton("☆");
            starBtns[i].setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            starBtns[i].setForeground(new Color(245, 158, 11));
            starBtns[i].setContentAreaFilled(false);
            starBtns[i].setBorderPainted(false);
            starBtns[i].setFocusPainted(false);
            starBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            starBtns[i].addActionListener(e -> setRating(rating));
            starsPanel.add(starBtns[i]);
        }

        qInner.add(questionLabel, BorderLayout.NORTH);
        qInner.add(answerScroll, BorderLayout.CENTER);
        qInner.add(actionRow, BorderLayout.SOUTH);

        qCard.add(qInner, BorderLayout.CENTER);
        qCard.add(starsPanel, BorderLayout.SOUTH);

        // ---- NAVIGATION ----
        JPanel navRow = new JPanel(new BorderLayout());
        navRow.setOpaque(false);

        prevBtn = createOutlineButton("← Previous", Theme.MUTED_TEXT);
        prevBtn.addActionListener(e -> previousQuestion());

        nextBtn = createGradientButton("Next →", 130, 38);
        nextBtn.addActionListener(e -> nextQuestion());

        JButton endBtn = createOutlineButton("✕ End", new Color(239, 68, 68));
        endBtn.addActionListener(e -> endSession());

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightNav.setOpaque(false);
        rightNav.add(endBtn);
        rightNav.add(nextBtn);

        navRow.add(prevBtn, BorderLayout.WEST);
        navRow.add(rightNav, BorderLayout.EAST);

        panel.add(statusBar, BorderLayout.NORTH);
        panel.add(qCard, BorderLayout.CENTER);
        panel.add(navRow, BorderLayout.SOUTH);

        return panel;
    }

    /* ============================================================ */
    /*  SESSION LOGIC                                               */
    /* ============================================================ */
    private void startSession(int count) {
        historyStack.clear();
        sessionQueue.clear();
        ratingSum = 0; ratingCount = 0; selectedRating = 0;

        // Build session pool from category
        List<InterviewQuestion> pool = new ArrayList<>();
        for (InterviewQuestion q : ALL_QUESTIONS) {
            if (selectedCategory.equals("All") || q.category.equals(selectedCategory))
                pool.add(q);
        }
        Collections.shuffle(pool);

        // Fill LinkedList queue
        sessionTotal = Math.min(count, pool.size());
        for (int i = 0; i < sessionTotal; i++) {
            pool.get(i).userAnswer = "";
            pool.get(i).rating = 0;
            sessionQueue.add(pool.get(i));
        }

        if (sessionQueue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions in that category!", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cards.show(mainCards, "play");
        loadCurrentQuestion();
    }

    private void loadCurrentQuestion() {
        if (sessionQueue.isEmpty()) { endSession(); return; }

        currentQuestion = sessionQueue.peekFirst(); // look at front without removing
        int answered = historyStack.size();
        int total = historyStack.size() + sessionQueue.size();

        qCountLabel.setText("Question " + (answered + 1) + " of " + total);
        qCategoryBadge.setText("  " + currentQuestion.category + "  ");

        Color catColor = switch (currentQuestion.category) {
            case "HR" -> new Color(34, 197, 94);
            case "Technical" -> new Color(14, 165, 233);
            case "Behavioral" -> new Color(245, 158, 11);
            default -> new Color(99, 102, 241);
        };
        qCategoryBadge.setBackground(catColor);

        questionLabel.setText("<html><body style='width:500px'>" + currentQuestion.question + "</body></html>");
        answerArea.setText(currentQuestion.userAnswer);

        // Restore rating
        selectedRating = currentQuestion.rating;
        updateStars(selectedRating);

        prevBtn.setEnabled(!historyStack.isEmpty());
    }

    private void nextQuestion() {
        if (currentQuestion == null) return;

        // Save answer + rating from UI
        currentQuestion.userAnswer = answerArea.getText().trim();
        currentQuestion.rating = selectedRating;

        if (selectedRating > 0) {
            ratingSum += selectedRating;
            ratingCount++;
        }

        // Push to history Stack, pop from session Queue (LinkedList)
        historyStack.push(sessionQueue.pollFirst());
        selectedRating = 0;

        if (sessionQueue.isEmpty()) {
            endSession();
        } else {
            loadCurrentQuestion();
        }
    }

    private void previousQuestion() {
        if (historyStack.isEmpty()) return;

        // Save current progress
        if (currentQuestion != null) {
            currentQuestion.userAnswer = answerArea.getText().trim();
            currentQuestion.rating = selectedRating;
        }

        // Pop from Stack, push back to front of LinkedList
        InterviewQuestion prev = historyStack.pop();
        sessionQueue.addFirst(prev);

        selectedRating = 0;
        loadCurrentQuestion();
    }

    private void endSession() {
        // Save last question
        if (currentQuestion != null) {
            currentQuestion.userAnswer = answerArea.getText().trim();
            currentQuestion.rating = selectedRating;
            if (selectedRating > 0) { ratingSum += selectedRating; ratingCount++; }
        }

        // Increment session count
        int sessions = UserProfileCache.getMockInterviews() + 1;
        UserProfileCache.setMockInterviews(sessions);
        StatsService.saveMockInterviews(UserProfileCache.getCurrentUserId(), sessions);

        showResult();
    }

    private void showResult() {
        // Build all answered questions from historyStack + remaining
        List<InterviewQuestion> allAnswered = new ArrayList<>(historyStack);
        Collections.reverse(allAnswered); // chronological order
        sessionQueue.forEach(allAnswered::add);

        double avgRating = ratingCount > 0 ? (double) ratingSum / ratingCount : 0;

        JPanel result = buildResultPanel(allAnswered, avgRating);
        mainCards.add(result, "result");
        cards.show(mainCards, "result");
    }

    private JPanel buildResultPanel(List<InterviewQuestion> answered, double avgRating) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));

        // Summary card
        ElevatedCard summary = new ElevatedCard(new BorderLayout());
        JPanel sumInner = new JPanel(new BorderLayout(0, 8));
        sumInner.setOpaque(false);
        sumInner.setBorder(new EmptyBorder(20, 24, 20, 24));

        String emoji = avgRating >= 4 ? "🎉" : avgRating >= 3 ? "👍" : "💪";
        JLabel header = new JLabel(emoji + "  Interview Complete!");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(Theme.PRIMARY_TEXT);

        JLabel ratingLbl = new JLabel("Average self-rating: " + String.format("%.1f", avgRating) + " / 5.0  ⭐");
        ratingLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ratingLbl.setForeground(Theme.MUTED_TEXT);

        JLabel sessLbl = new JLabel("Total mock interviews completed: " + UserProfileCache.getMockInterviews());
        sessLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sessLbl.setForeground(new Color(99, 102, 241));

        sumInner.add(header, BorderLayout.NORTH);
        sumInner.add(ratingLbl, BorderLayout.CENTER);
        sumInner.add(sessLbl, BorderLayout.SOUTH);
        summary.add(sumInner, BorderLayout.CENTER);

        // Review list
        JPanel reviewList = new JPanel();
        reviewList.setLayout(new BoxLayout(reviewList, BoxLayout.Y_AXIS));
        reviewList.setOpaque(false);
        reviewList.setBorder(new EmptyBorder(16, 0, 8, 0));

        JLabel reviewTitle = new JLabel("📋 Review Your Answers");
        reviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reviewTitle.setForeground(Theme.PRIMARY_TEXT);
        reviewTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        reviewList.add(reviewTitle);

        for (InterviewQuestion q : answered) {
            reviewList.add(buildReviewCard(q));
            reviewList.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(reviewList);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JButton homeBtn = createGradientButton("🔄 New Session", 160, 38);
        homeBtn.addActionListener(e -> cards.show(mainCards, "home"));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnRow.add(homeBtn);

        panel.add(summary, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    private ElevatedCard buildReviewCard(InterviewQuestion q) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        JPanel inner = new JPanel(new BorderLayout(0, 8));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel qLabel = new JLabel("<html><b>" + q.question + "</b></html>");
        qLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel stars = new JLabel(q.rating > 0 ? "⭐".repeat(q.rating) + " (" + q.rating + "/5)" : "Not rated");
        stars.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        stars.setForeground(new Color(245, 158, 11));

        String ansText = q.userAnswer.isEmpty() ? "(No answer given)" : q.userAnswer;
        JTextArea ansDisplay = new JTextArea(ansText);
        ansDisplay.setEditable(false);
        ansDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ansDisplay.setForeground(Theme.MUTED_TEXT);
        ansDisplay.setOpaque(false);
        ansDisplay.setLineWrap(true);
        ansDisplay.setWrapStyleWord(true);
        ansDisplay.setBorder(null);

        inner.add(qLabel, BorderLayout.NORTH);
        inner.add(ansDisplay, BorderLayout.CENTER);
        inner.add(stars, BorderLayout.SOUTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /* ============================================================ */
    /*  STAR RATING                                                 */
    /* ============================================================ */
    private void setRating(int rating) {
        selectedRating = rating;
        updateStars(rating);
    }

    private void updateStars(int rating) {
        for (int i = 0; i < 5; i++) {
            starBtns[i].setText(i < rating ? "★" : "☆");
        }
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        if (answerArea != null) {
            answerArea.setBackground(Theme.isDark() ? new Color(28,28,42) : new Color(248,249,252));
            answerArea.setForeground(Theme.PRIMARY_TEXT);
        }
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
        btn.setPreferredSize(new Dimension(130, 38));
        return btn;
    }
}
