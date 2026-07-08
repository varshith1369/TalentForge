package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Mock Interview module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Stack&lt;InterviewQuestion&gt;</b> - navigation history. Each answered
 *       question is pushed onto the stack; Previous pops it to review answers.</li>
 *   <li><b>LinkedList&lt;InterviewQuestion&gt;</b> - ordered question queue for the
 *       current session, navigated front-to-back.</li>
 * </ul>
 */
public class MockInterviewPanel extends JPanel {

    /* ============================================================ */
    /*  QUESTION MODEL                                             */
    /* ============================================================ */
    private static class InterviewQuestion {
        final String category;
        final String question;
        final String hint;
        final String sampleAnswer;
        String userAnswer = "";
        int rating = 0;
        boolean completed = false;

        InterviewQuestion(String category, String question, String hint, String sampleAnswer) {
            this.category = category;
            this.question = question;
            this.hint = hint;
            this.sampleAnswer = sampleAnswer;
        }
    }

    private enum SessionState { HOME, PLAYING, RESULT }

    private static final Color INDIGO = new Color(59, 130, 246);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color ROSE = new Color(244, 63, 94);
    private static final Color SLATE = new Color(100, 116, 139);

    /* ============================================================ */
    /*  BUILT-IN QUESTION BANK                                     */
    /* ============================================================ */
    private static final InterviewQuestion[] ALL_QUESTIONS = {
        new InterviewQuestion("HR", "Tell me about yourself.",
            "Use a Present -> Past -> Future structure. Keep it under two minutes and relevant to the role.",
            "I am a final-year CSE student focused on backend and product engineering. Over the last year I have built Java and SQL projects, worked on team deliverables, and improved how I explain technical decisions. I am now looking for a role where I can contribute to real product work and keep growing as an engineer."),
        new InterviewQuestion("HR", "What are your greatest strengths?",
            "Pick two or three strengths and back each one with a small example.",
            "My strongest qualities are problem solving, ownership, and calm communication. For example, during a project deadline I reorganized our task split, handled a blocked backend task myself, and kept the team aligned so we still shipped on time."),
        new InterviewQuestion("HR", "Where do you see yourself in five years?",
            "Connect your goals to learning, responsibility, and impact rather than titles alone.",
            "In five years I want to be a strong engineer trusted with system design and mentoring newer teammates. I hope to have built depth in scalable backend systems and to be contributing to product decisions, not just implementation."),
        new InterviewQuestion("HR", "Why do you want to work for this company?",
            "Reference the company's product, engineering culture, or mission. Avoid generic praise.",
            "I am interested in this company because your products solve practical problems at scale and the engineering culture seems thoughtful. I like teams that care about quality, learning, and customer impact, and that aligns with how I want to grow."),
        new InterviewQuestion("HR", "Describe a challenge you overcame.",
            "Use STAR: Situation, Task, Action, Result.",
            "During a hackathon our frontend developer had to leave midway. I took over the integration work, simplified the scope, and coordinated testing with the rest of the team. We finished the core product and placed second."),
        new InterviewQuestion("HR", "What is your biggest weakness?",
            "Be honest, then show how you are actively improving it.",
            "I can spend too long polishing early details before the main structure is solid. I have been improving that by time-boxing drafts first, then refining after I know the core direction is right."),

        new InterviewQuestion("Technical", "Explain the difference between a process and a thread.",
            "Talk about memory isolation, cost of creation, and communication.",
            "A process is an independent program instance with its own memory space, while a thread is a lightweight execution unit inside a process that shares memory with sibling threads. Threads are cheaper to create, but they require more careful synchronization because they share state."),
        new InterviewQuestion("Technical", "What is polymorphism? Give an example.",
            "Mention both overloading and overriding, but focus on runtime behavior if you give one example.",
            "Polymorphism means a common interface can support different concrete behaviors. For example, a Shape reference can point to a Circle or Rectangle, and calling draw on that reference runs the correct implementation for the actual object."),
        new InterviewQuestion("Technical", "What is the time complexity of binary search?",
            "Explain why the search space halves every step.",
            "Binary search runs in O(log n) time because every comparison removes half of the remaining search space. The iterative version uses O(1) extra space, while a recursive version uses call-stack space."),
        new InterviewQuestion("Technical", "Explain ACID properties in databases.",
            "Define Atomicity, Consistency, Isolation, and Durability with one crisp example or sentence each.",
            "Atomicity means a transaction fully succeeds or fully fails. Consistency means valid data stays valid after the transaction. Isolation means concurrent transactions do not corrupt one another. Durability means once a transaction commits, the change persists even after a crash."),
        new InterviewQuestion("Technical", "What is a deadlock and how do you prevent it?",
            "Mention circular wait and practical prevention strategies.",
            "A deadlock happens when multiple threads or processes each wait for resources held by the others, so none can continue. Prevention techniques include consistent lock ordering, timeouts, reducing shared state, and avoiding circular wait conditions."),
        new InterviewQuestion("Technical", "Explain REST vs SOAP.",
            "Contrast protocol style, message format, flexibility, and common use cases.",
            "REST is an architectural style usually built on HTTP with lightweight payloads such as JSON. SOAP is a stricter protocol with XML messaging and formal standards. REST is simpler and more flexible for web APIs, while SOAP is often used where strong contract rules and enterprise standards matter."),
        new InterviewQuestion("Technical", "What is the difference between HashMap and Hashtable?",
            "Cover synchronization, null handling, and practical usage.",
            "HashMap is not synchronized and allows one null key and multiple null values, so it is generally faster in single-threaded use. Hashtable is synchronized and does not allow null keys or values, but in modern Java other concurrent map options are usually preferred."),
        new InterviewQuestion("Technical", "What is microservices architecture?",
            "Compare it with a monolith and mention both benefits and tradeoffs.",
            "Microservices architecture splits an application into small independently deployable services, each focused on a business capability. This helps independent scaling and deployment, but it adds operational complexity, network communication overhead, and more distributed debugging."),

        new InterviewQuestion("Behavioral", "Tell me about a time you worked in a team under pressure.",
            "Use STAR and emphasize your personal contribution, not just the team outcome.",
            "In a final-year project we had a compressed deadline after requirements changed late. I reorganized the remaining work, handled the authentication flow myself, and set short check-ins so blockers were cleared quickly. We delivered on time and the demo was stable."),
        new InterviewQuestion("Behavioral", "Describe a situation where you had to learn something quickly.",
            "Show how you learn under pressure and how you validated your understanding.",
            "I once had to debug a React codebase during my first week in an internship. I used official docs, traced the component tree, and paired briefly with a teammate to confirm my understanding. I fixed the issue in two days and documented the root cause for the team."),
        new InterviewQuestion("Behavioral", "How do you handle disagreements with teammates?",
            "Show listening, clarity, and a bias toward shared goals instead of winning the argument.",
            "I first make sure I understand the other person's reasoning completely. Then I share my view with examples or data, and we compare against the project goal. If needed, I suggest a small experiment or ask for a tie-break from the lead so we can move forward constructively."),
        new InterviewQuestion("Behavioral", "Give an example of when you went above and beyond.",
            "Show initiative that made the team better, not just extra hours.",
            "I noticed our build pipeline was much slower than it needed to be, so I researched the bottleneck, adjusted caching in the CI setup, and cut build time significantly. That change helped the whole team iterate faster every day."),
        new InterviewQuestion("Behavioral", "How do you prioritize tasks when everything feels urgent?",
            "Talk about impact, deadlines, dependencies, and communication.",
            "I list the tasks, clarify deadlines and blockers, then rank them by impact and dependency. I finish the work that unblocks others or affects delivery first, and I communicate tradeoffs early so expectations stay realistic.")
    };

    /* ============================================================ */
    /*  DATA STRUCTURES                                            */
    /* ============================================================ */
    private final LinkedList<InterviewQuestion> sessionQueue = new LinkedList<>();
    private final Stack<InterviewQuestion> historyStack = new Stack<>();
    private final List<InterviewQuestion> sessionOrder = new ArrayList<>();

    /* ============================================================ */
    /*  SESSION STATE                                              */
    /* ============================================================ */
    private SessionState state = SessionState.HOME;
    private InterviewQuestion currentQuestion;
    private String selectedCategory = "All";
    private String selectedInterviewMode = "Classic";
    private String selectedAnswerGoal = "Balanced";
    private int selectedRating = 0;
    private boolean showSampleAnswers = true;
    private JPanel resultPanel;

    /* ============================================================ */
    /*  UI                                                         */
    /* ============================================================ */
    private final CardLayout cards = new CardLayout();
    private final JPanel mainCards = new JPanel(cards);

    private JLabel sessionsLabel;
    private JLabel selectedSummaryLabel;
    private JLabel qCountLabel;
    private JLabel qCategoryBadge;
    private JLabel progressLabel;
    private JLabel answeredLabel;
    private JLabel avgRatingLabel;
    private JLabel qualityLabel;
    private JLabel wordCountLabel;
    private JLabel questionLabel;
    private JLabel hintPreviewLabel;
    private JLabel samplePreviewLabel;
    private JTextArea answerArea;
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton[] ratingButtons = new JButton[5];
    private JSlider questionCountSlider;
    private JComboBox<String> interviewModeCombo;
    private JComboBox<String> answerGoalCombo;
    private JCheckBox sampleAnswerCheck;

    public MockInterviewPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        mainCards.setOpaque(false);
        mainCards.add(buildHomePanel(), "home");
        mainCards.add(buildPlayPanel(), "play");

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
                    new Color(INDIGO.getRed(), INDIGO.getGreen(), INDIGO.getBlue(), Theme.isDark() ? 68 : 36),
                    getWidth(), 0,
                    new Color(TEAL.getRed(), TEAL.getGreen(), TEAL.getBlue(), Theme.isDark() ? 40 : 22)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
            new EmptyBorder(16, 22, 16, 22)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = new JLabel("Mock Interview Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        JLabel sub = new JLabel("HR, technical, and behavioral practice with guided reflection and cleaner review.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(makePillLabel(ALL_QUESTIONS.length + " Prompts", TEAL, false));
        sessionsLabel = makePillLabel("Sessions " + UserProfileCache.getMockInterviews(), INDIGO, false);
        right.add(sessionsLabel);

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
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0;
        gc.weightx = 0.56;
        gc.insets = new Insets(0, 0, 0, 14);
        center.add(buildCategoryPanel(), gc);

        gc.gridx = 1;
        gc.weightx = 0.44;
        gc.insets = new Insets(0, 0, 0, 0);
        center.add(buildPrepPanel(), gc);

        root.add(center, BorderLayout.CENTER);
        root.add(buildStartBand(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeroPanel() {
        GradientCard hero = new GradientCard(new BorderLayout(18, 0), INDIGO, TEAL);
        hero.setBorder(new EmptyBorder(24, 24, 24, 24));
        hero.setPreferredSize(new Dimension(0, 140));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Practice the answer, not just the question");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Choose a track, set session size, speak your answer into text, rate it, and review your own patterns.");
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
        stats.add(heroStat("Tracks", "3"));
        stats.add(heroStat("Prompts", String.valueOf(ALL_QUESTIONS.length)));
        stats.add(heroStat("Your Sessions", String.valueOf(UserProfileCache.getMockInterviews())));

        hero.add(copy, BorderLayout.CENTER);
        hero.add(stats, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildCategoryPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Interview Track"), BorderLayout.WEST);
        header.add(makeTinyLabel("Choose the style of questions to rehearse."), BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        ButtonGroup group = new ButtonGroup();

        addCategoryTile(grid, group, "All", "Mixed set", INDIGO);
        addCategoryTile(grid, group, "HR", "Motivation and fit", GREEN);
        addCategoryTile(grid, group, "Technical", "Core engineering", TEAL);
        addCategoryTile(grid, group, "Behavioral", "STAR storytelling", ORANGE);

        panel.add(header, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPrepPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Session Design"), BorderLayout.WEST);
        header.add(makeTinyLabel("Tune the length and prep lens."), BorderLayout.EAST);

        JLabel countLabel = new JLabel("Prompts this session: 5");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        countLabel.setForeground(Theme.PRIMARY_TEXT);

        questionCountSlider = new JSlider(3, 10, 5);
        questionCountSlider.setOpaque(false);
        questionCountSlider.addChangeListener(e -> {
            countLabel.setText("Prompts this session: " + questionCountSlider.getValue());
            updateSelectedSummary();
        });

        JPanel cardsWrap = new JPanel(new GridLayout(5, 1, 0, 12));
        cardsWrap.setOpaque(false);
        cardsWrap.add(buildInterviewModeControl());
        cardsWrap.add(buildAnswerGoalControl());
        cardsWrap.add(buildSampleVisibilityControl());
        cardsWrap.add(infoCard("Answer shape", "Start clear, add one concrete example, and finish with the result or lesson.", TEAL));
        cardsWrap.add(infoCard("Self review", "Use the rating to judge clarity, confidence, and relevance, not perfection.", ORANGE));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(countLabel, BorderLayout.NORTH);
        body.add(questionCountSlider, BorderLayout.CENTER);
        body.add(cardsWrap, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInterviewModeControl() {
        SurfacePanel card = new SurfacePanel(new BorderLayout(12, 0));
        card.setAccent(INDIGO);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));

        JLabel label = new JLabel("Interview mode");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.PRIMARY_TEXT);

        interviewModeCombo = new JComboBox<>(new String[]{"Classic", "Rapid Fire", "STAR Drill", "Technical Deep Dive"});
        interviewModeCombo.setSelectedItem(selectedInterviewMode);
        interviewModeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        interviewModeCombo.addActionListener(e -> {
            Object selected = interviewModeCombo.getSelectedItem();
            if (selected != null) {
                selectedInterviewMode = selected.toString();
                applyModeDefaults();
                updateSelectedSummary();
            }
        });

        card.add(label, BorderLayout.WEST);
        card.add(interviewModeCombo, BorderLayout.EAST);
        return card;
    }

    private JPanel buildAnswerGoalControl() {
        SurfacePanel card = new SurfacePanel(new BorderLayout(12, 0));
        card.setAccent(TEAL);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));

        JLabel label = new JLabel("Answer goal");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.PRIMARY_TEXT);

        answerGoalCombo = new JComboBox<>(new String[]{"Concise", "Balanced", "Detailed"});
        answerGoalCombo.setSelectedItem(selectedAnswerGoal);
        answerGoalCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        answerGoalCombo.addActionListener(e -> {
            Object selected = answerGoalCombo.getSelectedItem();
            if (selected != null) {
                selectedAnswerGoal = selected.toString();
                updateSelectedSummary();
            }
        });

        card.add(label, BorderLayout.WEST);
        card.add(answerGoalCombo, BorderLayout.EAST);
        return card;
    }

    private JPanel buildSampleVisibilityControl() {
        SurfacePanel card = new SurfacePanel(new BorderLayout(12, 0));
        card.setAccent(ORANGE);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));

        JLabel label = new JLabel("Show sample answer");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.PRIMARY_TEXT);

        sampleAnswerCheck = new JCheckBox();
        sampleAnswerCheck.setSelected(showSampleAnswers);
        sampleAnswerCheck.setOpaque(false);
        sampleAnswerCheck.setToolTipText("Toggle sample answer guidance during the session.");
        sampleAnswerCheck.addActionListener(e -> {
            showSampleAnswers = sampleAnswerCheck.isSelected();
            if (currentQuestion != null && samplePreviewLabel != null) {
                updateSamplePreview();
            }
            updateSelectedSummary();
        });

        card.add(label, BorderLayout.WEST);
        card.add(sampleAnswerCheck, BorderLayout.EAST);
        return card;
    }

    private JPanel buildStartBand() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(14, 0));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel copy = new JLabel("Keep answers concise first, then expand with stronger evidence on the second pass.");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copy.setForeground(Theme.MUTED_TEXT);

        JButton startBtn = createGradientButton("Start Mock Interview", 210, 44);
        startBtn.addActionListener(e -> startSession(questionCountSlider.getValue()));

        panel.add(copy, BorderLayout.CENTER);
        panel.add(startBtn, BorderLayout.EAST);
        return panel;
    }

    private void addCategoryTile(JPanel grid, ButtonGroup group, String category, String desc, Color accent) {
        JToggleButton btn = choiceTile(category, desc, accent);
        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                selectedCategory = category;
                updateSelectedSummary();
            }
        });
        if ("All".equals(category)) btn.setSelected(true);
        group.add(btn);
        grid.add(btn);
    }

    private void applyModeDefaults() {
        if (questionCountSlider == null) return;

        switch (selectedInterviewMode) {
            case "Rapid Fire" -> {
                questionCountSlider.setValue(Math.min(questionCountSlider.getMaximum(), 3));
                selectedAnswerGoal = "Concise";
                showSampleAnswers = false;
            }
            case "STAR Drill" -> {
                questionCountSlider.setValue(Math.min(questionCountSlider.getMaximum(), 5));
                selectedAnswerGoal = "Balanced";
                showSampleAnswers = true;
            }
            case "Technical Deep Dive" -> {
                questionCountSlider.setValue(Math.min(questionCountSlider.getMaximum(), 6));
                selectedAnswerGoal = "Detailed";
                showSampleAnswers = true;
            }
            default -> {
                selectedAnswerGoal = "Balanced";
                showSampleAnswers = true;
            }
        }

        if (answerGoalCombo != null) {
            answerGoalCombo.setSelectedItem(selectedAnswerGoal);
        }
        if (sampleAnswerCheck != null) {
            sampleAnswerCheck.setSelected(showSampleAnswers);
        }
    }

    /* ============================================================ */
    /*  PLAY PANEL                                                 */
    /* ============================================================ */
    private JPanel buildPlayPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 24, 24));

        root.add(buildSessionRail(), BorderLayout.WEST);
        root.add(buildInterviewWorkspace(), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSessionRail() {
        SurfacePanel rail = new SurfacePanel(new BorderLayout(0, 14));
        rail.setPreferredSize(new Dimension(270, 0));
        rail.setBorder(new EmptyBorder(18, 16, 18, 16));

        JPanel stats = new JPanel(new GridLayout(5, 1, 0, 10));
        stats.setOpaque(false);

        progressLabel = new JLabel();
        answeredLabel = new JLabel();
        avgRatingLabel = new JLabel();
        qualityLabel = new JLabel();
        wordCountLabel = new JLabel();

        stats.add(statRow("Progress", progressLabel, INDIGO));
        stats.add(statRow("Answered", answeredLabel, TEAL));
        stats.add(statRow("Avg Rating", avgRatingLabel, ORANGE));
        stats.add(statRow("Words", wordCountLabel, SLATE));
        stats.add(statRow("Answer Quality", qualityLabel, GREEN));

        JTextArea coach = new JTextArea("Use the selected mode as your lens. A strong answer is specific, structured, and easy to follow.");
        coach.setEditable(false);
        coach.setLineWrap(true);
        coach.setWrapStyleWord(true);
        coach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coach.setForeground(Theme.MUTED_TEXT);
        coach.setBackground(Theme.PANEL_BG);
        coach.setBorder(new EmptyBorder(8, 0, 0, 0));

        rail.add(sectionLabel("Session"), BorderLayout.NORTH);
        rail.add(stats, BorderLayout.CENTER);
        rail.add(coach, BorderLayout.SOUTH);
        return rail;
    }

    private JPanel buildInterviewWorkspace() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        qCountLabel = new JLabel("Prompt 1 of 5");
        qCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        qCountLabel.setForeground(Theme.PRIMARY_TEXT);
        qCategoryBadge = makePillLabel("HR", INDIGO, true);
        left.add(qCountLabel);
        left.add(Box.createVerticalStrut(6));
        left.add(qCategoryBadge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton homeBtn = createOutlineButton("End Session", ROSE);
        homeBtn.addActionListener(e -> endSession());
        right.add(homeBtn);

        header.add(left, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        SurfacePanel promptCard = new SurfacePanel(new BorderLayout(0, 16));
        promptCard.setBorder(new EmptyBorder(22, 24, 20, 24));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        questionLabel.setForeground(Theme.PRIMARY_TEXT);

        answerArea = new JTextArea(9, 50);
        answerArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        answerArea.setForeground(Theme.PRIMARY_TEXT);
        answerArea.setBackground(Theme.isDark() ? new Color(28, 32, 44) : new Color(250, 251, 253));
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setBorder(new EmptyBorder(14, 16, 14, 16));
        answerArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onAnswerChanged(); }
            public void removeUpdate(DocumentEvent e) { onAnswerChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        JScrollPane answerScroll = new JScrollPane(answerArea);
        answerScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));

        JPanel rightCards = new JPanel(new GridLayout(2, 1, 0, 12));
        rightCards.setOpaque(false);
        hintPreviewLabel = new JLabel();
        samplePreviewLabel = new JLabel();
        rightCards.add(contentCard("Coach Hint", hintPreviewLabel, ORANGE));
        rightCards.add(contentCard("Sample Direction", samplePreviewLabel, TEAL));
        rightCards.setPreferredSize(new Dimension(280, 0));

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.add(answerScroll, BorderLayout.CENTER);
        body.add(rightCards, BorderLayout.EAST);

        JPanel ratingRow = new JPanel(new BorderLayout());
        ratingRow.setOpaque(false);

        JPanel leftRate = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftRate.setOpaque(false);
        JLabel rateText = new JLabel("Rate this answer");
        rateText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rateText.setForeground(Theme.PRIMARY_TEXT);
        leftRate.add(rateText);
        for (int i = 0; i < ratingButtons.length; i++) {
            int rating = i + 1;
            ratingButtons[i] = createRatingButton(rating);
            ratingButtons[i].addActionListener(e -> setRating(rating));
            leftRate.add(ratingButtons[i]);
        }

        JButton outlineBtn = createOutlineButton("Insert Outline", TEAL);
        outlineBtn.addActionListener(e -> insertAnswerOutline());
        prevBtn = createOutlineButton("Previous", SLATE);
        prevBtn.addActionListener(e -> previousQuestion());
        nextBtn = createGradientButton("Next Prompt", 150, 40);
        nextBtn.addActionListener(e -> nextQuestion());
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        nav.add(outlineBtn);
        nav.add(prevBtn);
        nav.add(nextBtn);

        ratingRow.add(leftRate, BorderLayout.WEST);
        ratingRow.add(nav, BorderLayout.EAST);

        promptCard.add(questionLabel, BorderLayout.NORTH);
        promptCard.add(body, BorderLayout.CENTER);
        promptCard.add(ratingRow, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(promptCard, BorderLayout.CENTER);
        return panel;
    }

    /* ============================================================ */
    /*  SESSION LOGIC                                              */
    /* ============================================================ */
    private void startSession(int count) {
        state = SessionState.PLAYING;
        historyStack.clear();
        sessionQueue.clear();
        sessionOrder.clear();
        currentQuestion = null;
        selectedRating = 0;

        List<InterviewQuestion> pool = new ArrayList<>();
        for (InterviewQuestion q : ALL_QUESTIONS) {
            if ("All".equals(selectedCategory) || q.category.equals(selectedCategory)) {
                pool.add(cloneQuestion(q));
            }
        }
        Collections.shuffle(pool);

        int target = Math.min(count, pool.size());
        for (int i = 0; i < target; i++) {
            InterviewQuestion q = pool.get(i);
            sessionQueue.add(q);
            sessionOrder.add(q);
        }

        if (sessionQueue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No prompts available for that category.", "Empty Category", JOptionPane.WARNING_MESSAGE);
            state = SessionState.HOME;
            return;
        }

        cards.show(mainCards, "play");
        loadCurrentQuestion();
    }

    private InterviewQuestion cloneQuestion(InterviewQuestion q) {
        return new InterviewQuestion(q.category, q.question, q.hint, q.sampleAnswer);
    }

    private void loadCurrentQuestion() {
        if (sessionQueue.isEmpty()) {
            endSession();
            return;
        }

        currentQuestion = sessionQueue.peekFirst();
        selectedRating = currentQuestion.rating;

        int answered = completedCount();
        int total = sessionOrder.size();

        qCountLabel.setText("Prompt " + (historyStack.size() + 1) + " of " + total);
        qCategoryBadge.setText(currentQuestion.category);
        qCategoryBadge.setBackground(categoryColor(currentQuestion.category));

        questionLabel.setText("<html><body style='width:760px;font-family:Segoe UI;font-size:14pt;line-height:1.35'>"
            + escapeHtml(currentQuestion.question) + "</body></html>");
        answerArea.setText(currentQuestion.userAnswer);
        hintPreviewLabel.setText(htmlText(currentQuestion.hint, 220));
        updateSamplePreview();

        updateRatingButtons();
        prevBtn.setEnabled(!historyStack.isEmpty());
        nextBtn.setText(sessionQueue.size() == 1 ? "Finish Session" : "Next Prompt");
        syncSessionStats();
        onAnswerChanged();
    }

    private void updateSamplePreview() {
        if (samplePreviewLabel == null || currentQuestion == null) return;
        String text = showSampleAnswers
            ? currentQuestion.sampleAnswer
            : "Sample answer hidden for a more realistic interview round. Turn it back on from Session Design.";
        samplePreviewLabel.setText(htmlText(text, 220));
    }

    private void nextQuestion() {
        if (currentQuestion == null) return;

        captureCurrentInput();

        if (currentQuestion.userAnswer.isBlank()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Move on without writing an answer for this prompt?",
                "Skip Answer", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        currentQuestion.completed = true;
        InterviewQuestion answered = sessionQueue.pollFirst();
        historyStack.push(answered);

        if (sessionQueue.isEmpty()) {
            endSession();
        } else {
            loadCurrentQuestion();
        }
    }

    private void previousQuestion() {
        if (historyStack.isEmpty()) return;

        captureCurrentInput();

        InterviewQuestion previous = historyStack.pop();
        sessionQueue.addFirst(previous);
        loadCurrentQuestion();
    }

    private void captureCurrentInput() {
        if (currentQuestion == null) return;
        currentQuestion.userAnswer = answerArea.getText().trim();
        currentQuestion.rating = selectedRating;
    }

    private void insertAnswerOutline() {
        if (answerArea == null || currentQuestion == null) return;

        String existing = answerArea.getText().trim();
        String outline;
        if ("Behavioral".equals(currentQuestion.category) || "STAR Drill".equals(selectedInterviewMode)) {
            outline = "Situation: \nTask: \nAction: \nResult: \nLearning: ";
        } else if ("Technical".equals(currentQuestion.category) || "Technical Deep Dive".equals(selectedInterviewMode)) {
            outline = "Definition: \nKey idea: \nExample: \nTradeoff: \nConclusion: ";
        } else {
            outline = "Opening: \nExample: \nImpact: \nClose: ";
        }

        if (existing.isEmpty()) {
            answerArea.setText(outline);
        } else {
            answerArea.append("\n\n" + outline);
        }
        answerArea.requestFocusInWindow();
    }

    private void endSession() {
        captureCurrentInput();

        if (!hasMeaningfulSessionWork()) {
            state = SessionState.HOME;
            cards.show(mainCards, "home");
            return;
        }

        state = SessionState.RESULT;
        int sessions = UserProfileCache.getMockInterviews() + 1;
        UserProfileCache.setMockInterviews(sessions);
        int userId = UserProfileCache.getCurrentUserId();
        if (userId > 0) {
            StatsService.saveMockInterviews(userId, sessions);
        }
        refreshSessionCount();
        showResult();
    }

    private boolean hasMeaningfulSessionWork() {
        for (InterviewQuestion q : sessionOrder) {
            if (q.completed || q.rating > 0 || !q.userAnswer.isBlank()) return true;
        }
        return false;
    }

    private void showResult() {
        if (resultPanel != null) {
            mainCards.remove(resultPanel);
        }
        resultPanel = buildResultPanel();
        mainCards.add(resultPanel, "result");
        cards.show(mainCards, "result");
    }

    private JPanel buildResultPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 24, 24));

        GradientCard summary = new GradientCard(new BorderLayout(18, 0), INDIGO, TEAL);
        summary.setBorder(new EmptyBorder(24, 26, 24, 26));
        summary.setPreferredSize(new Dimension(0, 150));

        double avgRating = averageRating();
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Interview Session Complete");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(selectedInterviewMode + " complete. Average self-rating " + formatRating(avgRating) + " / 5 across " + ratedCount() + " rated responses.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 220));
        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(sub);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(360, 0));
        stats.add(heroStat("Answered", String.valueOf(answeredCount())));
        stats.add(heroStat("Avg Rating", formatRating(avgRating)));
        stats.add(heroStat("Goal", selectedAnswerGoal));

        summary.add(copy, BorderLayout.CENTER);
        summary.add(stats, BorderLayout.EAST);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        gc.gridx = 0;
        gc.weightx = 0.34;
        gc.insets = new Insets(0, 0, 0, 14);
        body.add(buildResultStatsPanel(), gc);

        gc.gridx = 1;
        gc.weightx = 0.66;
        gc.insets = new Insets(0, 0, 0, 0);
        body.add(buildReviewPanel(), gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton homeBtn = createOutlineButton("Back to Setup", SLATE);
        JButton againBtn = createGradientButton("New Session", 160, 42);
        homeBtn.addActionListener(e -> {
            state = SessionState.HOME;
            cards.show(mainCards, "home");
        });
        againBtn.addActionListener(e -> startSession(questionCountSlider.getValue()));
        actions.add(homeBtn);
        actions.add(againBtn);

        root.add(summary, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildResultStatsPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel stats = new JPanel(new GridLayout(4, 1, 0, 10));
        stats.setOpaque(false);
        stats.add(statRow("Answered", new JLabel(answeredCount() + "/" + sessionOrder.size()), INDIGO));
        stats.add(statRow("Rated", new JLabel(String.valueOf(ratedCount())), ORANGE));
        stats.add(statRow("Longest Answer", new JLabel(longestAnswerWords() + " words"), TEAL));
        stats.add(statRow("Top Track", new JLabel(strongestCategory()), GREEN));

        JPanel tips = new JPanel(new GridLayout(2, 1, 0, 12));
        tips.setOpaque(false);
        tips.add(infoCard("What to improve", "Low-rated answers usually need clearer structure, stronger examples, or tighter wording.", ORANGE));
        tips.add(infoCard("Next pass", "Repeat the same category and aim to improve one answer at a time, not all at once.", GREEN));

        panel.add(sectionLabel("Session Readout"), BorderLayout.NORTH);
        panel.add(stats, BorderLayout.CENTER);
        panel.add(tips, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildReviewPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.add(sectionLabel("Answer Review"), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (InterviewQuestion q : sessionOrder) {
            list.add(buildReviewCard(q));
            list.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReviewCard(InterviewQuestion q) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 10));
        card.setAccent(categoryColor(q.category));
        card.setBorder(new EmptyBorder(14, 16, 14, 14));

        JLabel title = new JLabel("<html><b>" + escapeHtml(q.question) + "</b></html>");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel meta = new JLabel(q.category + " / Rating " + (q.rating == 0 ? "Not set" : q.rating + " of 5"));
        meta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        meta.setForeground(categoryColor(q.category));

        JTextArea answer = new JTextArea(q.userAnswer.isBlank() ? "(No answer written)" : q.userAnswer);
        answer.setEditable(false);
        answer.setLineWrap(true);
        answer.setWrapStyleWord(true);
        answer.setOpaque(false);
        answer.setBorder(null);
        answer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        answer.setForeground(Theme.MUTED_TEXT);

        card.add(title, BorderLayout.NORTH);
        card.add(answer, BorderLayout.CENTER);
        card.add(meta, BorderLayout.SOUTH);
        return card;
    }

    /* ============================================================ */
    /*  RATING + LIVE STATS                                        */
    /* ============================================================ */
    private JButton createRatingButton(int rating) {
        JButton btn = new JButton(String.valueOf(rating));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(36, 32));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setRating(int rating) {
        selectedRating = rating;
        updateRatingButtons();
        syncSessionStats();
    }

    private void updateRatingButtons() {
        for (int i = 0; i < ratingButtons.length; i++) {
            boolean selected = i < selectedRating;
            JButton btn = ratingButtons[i];
            btn.setForeground(selected ? Color.WHITE : ORANGE);
            btn.setBackground(selected ? ORANGE : new Color(ORANGE.getRed(), ORANGE.getGreen(), ORANGE.getBlue(), Theme.isDark() ? 28 : 16));
            btn.setBorder(BorderFactory.createLineBorder(ORANGE, 1, true));
            btn.setOpaque(true);
        }
    }

    private void onAnswerChanged() {
        if (currentQuestion != null) {
            currentQuestion.userAnswer = answerArea.getText().trim();
        }
        syncSessionStats();
    }

    private void syncSessionStats() {
        int answered = answeredCount();
        int total = sessionOrder.size();
        String answer = answerArea != null ? answerArea.getText().trim() : "";
        progressLabel.setText(answered + " / " + total);
        answeredLabel.setText(String.valueOf(answered));
        avgRatingLabel.setText(ratedCount() == 0 ? "-" : formatRating(averageRating()));
        wordCountLabel.setText(String.valueOf(wordCount(answer)));
        qualityLabel.setText(answerQualityLabel(answer));
    }

    private int answeredCount() {
        int count = 0;
        for (InterviewQuestion q : sessionOrder) {
            if (!q.userAnswer.isBlank()) count++;
        }
        return count;
    }

    private int completedCount() {
        int count = 0;
        for (InterviewQuestion q : sessionOrder) {
            if (q.completed) count++;
        }
        return count;
    }

    private int ratedCount() {
        int count = 0;
        for (InterviewQuestion q : sessionOrder) {
            if (q.rating > 0) count++;
        }
        return count;
    }

    private double averageRating() {
        int sum = 0;
        int count = 0;
        for (InterviewQuestion q : sessionOrder) {
            if (q.rating > 0) {
                sum += q.rating;
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) sum / count;
    }

    private String strongestCategory() {
        Map<String, int[]> stats = new LinkedHashMap<>();
        for (InterviewQuestion q : sessionOrder) {
            if (q.rating <= 0) continue;
            int[] row = stats.computeIfAbsent(q.category, k -> new int[2]);
            row[0] += q.rating;
            row[1]++;
        }
        String best = "None yet";
        double bestAvg = -1;
        for (Map.Entry<String, int[]> entry : stats.entrySet()) {
            double avg = (double) entry.getValue()[0] / entry.getValue()[1];
            if (avg > bestAvg) {
                bestAvg = avg;
                best = entry.getKey();
            }
        }
        return best;
    }

    private int longestAnswerLength() {
        int longest = 0;
        for (InterviewQuestion q : sessionOrder) {
            longest = Math.max(longest, q.userAnswer.length());
        }
        return longest;
    }

    private int longestAnswerWords() {
        int longest = 0;
        for (InterviewQuestion q : sessionOrder) {
            longest = Math.max(longest, wordCount(q.userAnswer));
        }
        return longest;
    }

    private String answerQualityLabel(String answer) {
        int words = wordCount(answer);
        if (words == 0) return "Empty";

        int[] range = targetWordRange();
        if (words < range[0]) return "Needs more detail";
        if (words <= range[1]) return "On target";
        return "Trim it down";
    }

    private int wordCount(String answer) {
        if (answer == null || answer.isBlank()) return 0;
        return answer.trim().split("\\s+").length;
    }

    private int[] targetWordRange() {
        return switch (selectedAnswerGoal) {
            case "Concise" -> new int[]{45, 90};
            case "Detailed" -> new int[]{140, 260};
            default -> new int[]{80, 160};
        };
    }

    private String formatRating(double rating) {
        return String.format("%.1f", rating);
    }

    /* ============================================================ */
    /*  THEME + HELPERS                                            */
    /* ============================================================ */
    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        refreshSessionCount();
        if (answerArea != null) {
            answerArea.setBackground(Theme.isDark() ? new Color(28, 32, 44) : new Color(250, 251, 253));
            answerArea.setForeground(Theme.PRIMARY_TEXT);
        }
        repaint();
    }

    private void refreshSessionCount() {
        if (sessionsLabel != null) {
            sessionsLabel.setText("Sessions " + UserProfileCache.getMockInterviews());
        }
        if (selectedSummaryLabel != null) {
            selectedSummaryLabel.setText(summaryText());
        }
    }

    private void updateSelectedSummary() {
        if (selectedSummaryLabel != null) {
            selectedSummaryLabel.setText(summaryText());
        }
    }

    private String summaryText() {
        int desired = questionCountSlider == null ? 5 : questionCountSlider.getValue();
        int[] range = targetWordRange();
        return selectedCategory + " / " + selectedInterviewMode + " / "
            + Math.min(desired, countQuestions(selectedCategory)) + " prompts / "
            + selectedAnswerGoal + " " + range[0] + "-" + range[1] + " words";
    }

    private int countQuestions(String category) {
        if ("All".equals(category)) return ALL_QUESTIONS.length;
        int count = 0;
        for (InterviewQuestion q : ALL_QUESTIONS) {
            if (q.category.equals(category)) count++;
        }
        return count;
    }

    private Color categoryColor(String category) {
        return switch (category) {
            case "HR" -> GREEN;
            case "Technical" -> TEAL;
            case "Behavioral" -> ORANGE;
            default -> INDIGO;
        };
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String htmlText(String text, int width) {
        return "<html><body style='width:" + width + "px'>" + escapeHtml(text) + "</body></html>";
    }

    /* ============================================================ */
    /*  VISUAL HELPERS                                             */
    /* ============================================================ */
    private JToggleButton choiceTile(String title, String desc, Color accent) {
        JToggleButton btn = new JToggleButton("<html><b>" + title + "</b><br><span style='font-size:9pt'>" + desc + "</span></html>") {
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
                    ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Theme.isDark() ? 88 : 34)
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
        btn.setPreferredSize(new Dimension(0, 90));
        return btn;
    }

    private JPanel heroStat(String label, String value) {
        JPanel stat = new JPanel(new BorderLayout(0, 4));
        stat.setOpaque(false);
        stat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1, true),
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

    private JPanel infoCard(String title, String body, Color accent) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 6));
        card.setAccent(accent);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Theme.PRIMARY_TEXT);
        JLabel bodyLbl = new JLabel(htmlText(body, 230));
        bodyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bodyLbl.setForeground(Theme.MUTED_TEXT);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bodyLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel contentCard(String title, JLabel bodyLabel, Color accent) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setAccent(accent);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Theme.PRIMARY_TEXT);
        bodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bodyLabel.setForeground(Theme.MUTED_TEXT);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bodyLabel, BorderLayout.CENTER);
        return card;
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
        label.setBackground(filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 34 : 16));
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
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 28 : 16));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 38));
        return btn;
    }

    /* ============================================================ */
    /*  CUSTOM PANELS                                              */
    /* ============================================================ */
    private static class GradientCard extends JPanel {
        private final Color start;
        private final Color end;

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
}
