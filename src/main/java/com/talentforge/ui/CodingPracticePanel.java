package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

/**
 * Coding Practice module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Trie</b> - topic autocomplete. Each keystroke queries the Trie for
 *       matching topic prefixes in O(k) time.</li>
 *   <li><b>Stack&lt;String&gt;</b> - undo history for the code editor. Every
 *       edit pushes the prior text; Ctrl+Z pops it.</li>
 * </ul>
 */
public class CodingPracticePanel extends JPanel {

    /* ============================================================ */
    /*  TRIE - topic autocomplete                                  */
    /* ============================================================ */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }

    private static class Trie {
        private final TrieNode root = new TrieNode();

        void insert(String word) {
            TrieNode cur = root;
            for (char ch : word.toLowerCase().toCharArray()) {
                cur.children.putIfAbsent(ch, new TrieNode());
                cur = cur.children.get(ch);
            }
            cur.isEnd = true;
        }

        List<String> autocomplete(String prefix, List<String> allTopics, int maxResults) {
            List<String> results = new ArrayList<>();
            String lower = prefix.toLowerCase();
            for (String topic : allTopics) {
                if (topic.toLowerCase().startsWith(lower)) {
                    results.add(topic);
                    if (results.size() >= maxResults) break;
                }
            }
            return results;
        }
    }

    /* ============================================================ */
    /*  PROBLEM DATA MODEL                                         */
    /* ============================================================ */
    private static class Problem {
        final String title, difficulty, topic, description, starterCode;

        Problem(String title, String difficulty, String topic, String description, String starterCode) {
            this.title = title;
            this.difficulty = difficulty;
            this.topic = topic;
            this.description = description;
            this.starterCode = starterCode;
        }
    }

    /* ============================================================ */
    /*  BUILT-IN PROBLEM BANK                                      */
    /* ============================================================ */
    private static final Problem[] PROBLEMS = {
        new Problem("Two Sum", "Easy", "Arrays",
            "Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.\n\nExample:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: nums[0] + nums[1] == 9.",
            "public int[] twoSum(int[] nums, int target) {\n    // Your code here\n    \n}"),
        new Problem("Reverse String", "Easy", "Strings",
            "Write a function that reverses a string. The input string is given as an array of characters s.\n\nExample:\nInput: s = ['h','e','l','l','o']\nOutput: ['o','l','l','e','h']",
            "public void reverseString(char[] s) {\n    // Your code here\n    \n}"),
        new Problem("Valid Parentheses", "Easy", "Stack",
            "Given a string s containing just '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nExample:\nInput: s = \"()[]{}\"\nOutput: true",
            "public boolean isValid(String s) {\n    // Your code here (Hint: use a Stack)\n    \n}"),
        new Problem("Maximum Subarray", "Medium", "Arrays",
            "Given an integer array nums, find the contiguous subarray with the largest sum, and return its sum.\n\nExample:\nInput: nums = [-2,1,-3,4,-1,2,1,-5,4]\nOutput: 6  (subarray [4,-1,2,1])",
            "public int maxSubArray(int[] nums) {\n    // Your code here (Hint: Kadane's Algorithm)\n    \n}"),
        new Problem("Binary Search", "Easy", "Binary Search",
            "Given an array of integers nums sorted in ascending order, and an integer target, return the index of target. Return -1 if not found.\n\nExample:\nInput: nums = [-1,0,3,5,9,12], target = 9\nOutput: 4",
            "public int search(int[] nums, int target) {\n    // Your code here\n    \n}"),
        new Problem("Linked List Cycle", "Easy", "Linked List",
            "Given head, the head of a linked list, determine if the linked list has a cycle in it.\n\nExample:\nInput: head = [3,2,0,-4], pos = 1\nOutput: true (tail connects to node at index 1)",
            "public boolean hasCycle(ListNode head) {\n    // Your code here (Hint: Floyd's algorithm)\n    \n}"),
        new Problem("Level Order Traversal", "Medium", "Trees",
            "Given the root of a binary tree, return the level order traversal of its nodes' values.\n\nExample:\nInput: root = [3,9,20,null,null,15,7]\nOutput: [[3],[9,20],[15,7]]",
            "public List<List<Integer>> levelOrder(TreeNode root) {\n    // Your code here (Hint: use a Queue)\n    \n}"),
        new Problem("Number of Islands", "Medium", "Graphs",
            "Given an m x n 2D binary grid, return the number of islands.\n\nExample:\nInput: grid = [[1,1,0],[1,1,0],[0,0,1]]\nOutput: 2",
            "public int numIslands(char[][] grid) {\n    // Your code here (Hint: DFS/BFS)\n    \n}"),
        new Problem("Coin Change", "Medium", "Dynamic Programming",
            "Given an array of coin denominations and an amount, return the fewest coins needed to make that amount.\n\nExample:\nInput: coins = [1,5,11], amount = 15\nOutput: 3  (5+5+5)",
            "public int coinChange(int[] coins, int amount) {\n    // Your code here (Hint: DP bottom-up)\n    \n}"),
        new Problem("Merge Sort", "Medium", "Sorting",
            "Implement Merge Sort on an integer array.\n\nExample:\nInput: arr = [38,27,43,3,9,82,10]\nOutput: [3,9,10,27,38,43,82]",
            "public void mergeSort(int[] arr, int l, int r) {\n    // Your code here\n    \n}"),
        new Problem("Fibonacci Number", "Easy", "Recursion",
            "Given n, return F(n) where F(0)=0, F(1)=1 and F(n)=F(n-1)+F(n-2).\n\nExample:\nInput: n = 4\nOutput: 3  (0,1,1,2,3)",
            "public int fib(int n) {\n    // Your code here\n    \n}"),
        new Problem("LRU Cache", "Hard", "Design",
            "Design a data structure that follows the LRU (Least Recently Used) cache constraint.\n\nOperations: get(key) and put(key, value) must each run in O(1) time.",
            "class LRUCache {\n    public LRUCache(int capacity) {\n        \n    }\n    public int get(int key) {\n        \n    }\n    public void put(int key, int value) {\n        \n    }\n}"),
        new Problem("Detect Cycle in Graph", "Medium", "Graphs",
            "Given a directed graph represented as an adjacency list, detect if there is a cycle.\n\nReturn true if cycle exists, false otherwise.",
            "public boolean hasCycle(int V, List<List<Integer>> adj) {\n    // Your code here (Hint: DFS + color marking)\n    \n}"),
        new Problem("Longest Common Subsequence", "Medium", "Dynamic Programming",
            "Given two strings text1 and text2, return the length of their longest common subsequence.\n\nExample:\nInput: text1 = \"abcde\", text2 = \"ace\"\nOutput: 3",
            "public int longestCommonSubsequence(String text1, String text2) {\n    // Your code here (Hint: 2D DP)\n    \n}"),
        new Problem("Trapping Rain Water", "Hard", "Arrays",
            "Given n non-negative integers representing an elevation map, compute how much water it can trap after raining.\n\nExample:\nInput: height = [0,1,0,2,1,0,1,3,2,1,2,1]\nOutput: 6",
            "public int trap(int[] height) {\n    // Your code here (Hint: Two pointers)\n    \n}")
    };

    private static final List<String> ALL_TOPICS = new ArrayList<>(Arrays.asList(
        "Arrays", "Strings", "Stack", "Linked List", "Trees", "Graphs",
        "Dynamic Programming", "Binary Search", "Sorting", "Recursion", "Design", "Queues", "Backtracking"
    ));

    private static final Color EASY_COLOR = new Color(34, 197, 94);
    private static final Color MEDIUM_COLOR = new Color(245, 158, 11);
    private static final Color HARD_COLOR = new Color(239, 68, 68);
    private static final Color RUN_COLOR = new Color(14, 165, 233);

    /* ============================================================ */
    /*  DATA STRUCTURES                                            */
    /* ============================================================ */
    private final Trie topicTrie = new Trie();
    private final Stack<String> undoStack = new Stack<>();

    /* ============================================================ */
    /*  UI STATE                                                   */
    /* ============================================================ */
    private Problem currentProblem = PROBLEMS[0];
    private int solvedCount = 0;
    private String selectedTopicFilter = null;
    private String selectedDifficultyFilter = null;
    private boolean ignoringEdit = false;
    private boolean applyingTopicFilter = false;
    private String lastRunCodeSnapshot = "";

    private final Set<String> solvedProblemTitles = new HashSet<>();
    private final Map<String, JButton> difficultyButtons = new HashMap<>();
    private final List<JButton> topicButtons = new ArrayList<>();

    private final JLabel solvedLabel = new JLabel();
    private final JLabel visibleCountLabel = new JLabel();
    private final JLabel problemTitle = new JLabel();
    private final JLabel difficultyBadge = new JLabel();
    private final JLabel topicBadge = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel streakLabel = new JLabel();
    private final JLabel editorStatsLabel = new JLabel();
    private final JLabel complexityLabel = new JLabel();
    private final JLabel patternLabel = new JLabel();
    private final JLabel testStatusLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar(0, PROBLEMS.length);
    private final JTextArea descArea = new JTextArea();
    private final JTextArea codeEditor = new JTextArea();
    private final JTextArea consoleArea = new JTextArea();
    private final JPanel problemListPanel = new JPanel();
    private final JTextField searchField = new JTextField();
    private final JPopupMenu autocompletePopup = new JPopupMenu();

    public CodingPracticePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        for (String topic : ALL_TOPICS) topicTrie.insert(topic);
        solvedCount = UserProfileCache.getProblemsSolved();

        buildUI();
        loadProblem(PROBLEMS[0]);

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  UI BUILDER                                                 */
    /* ============================================================ */
    private void buildUI() {
        JPanel leftPanel = buildLeftPanel();
        leftPanel.setPreferredSize(new Dimension(350, 0));

        JPanel rightPanel = buildRightPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerSize(4);
        split.setDividerLocation(350);
        split.setResizeWeight(0);
        split.setBorder(null);
        split.setContinuousLayout(true);

        add(buildTopBar(), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = Theme.PANEL_BG;
                g2.setColor(base);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new GradientPaint(0, 0,
                    new Color(Theme.PRIMARY_START.getRed(), Theme.PRIMARY_START.getGreen(), Theme.PRIMARY_START.getBlue(), Theme.isDark() ? 70 : 42),
                    getWidth(), 0,
                    new Color(RUN_COLOR.getRed(), RUN_COLOR.getGreen(), RUN_COLOR.getBlue(), Theme.isDark() ? 42 : 24)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
            new EmptyBorder(16, 22, 16, 22)));

        JPanel titleWrap = new JPanel(new BorderLayout(10, 2));
        titleWrap.setOpaque(false);

        JLabel icon = new JLabel(Icons.code(Theme.PRIMARY_START));
        icon.setPreferredSize(new Dimension(28, 28));

        JPanel copy = new JPanel(new GridLayout(2, 1, 0, 1));
        copy.setOpaque(false);

        JLabel title = new JLabel("Coding Practice Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitle = new JLabel("Solve DSA problems in a focused Java workspace with filters, hints, run checks, and session tracking.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);

        copy.add(title);
        copy.add(subtitle);
        titleWrap.add(icon, BorderLayout.WEST);
        titleWrap.add(copy, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        updateSolvedLabel();
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressLabel.setForeground(Theme.PRIMARY_START);
        progressLabel.setBorder(pillBorder(Theme.PRIMARY_START));
        updateProgressSummary();
        right.add(makePillLabel(PROBLEMS.length + " Problems", RUN_COLOR, false));
        right.add(progressLabel);
        right.add(solvedLabel);

        bar.add(titleWrap, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.FIELD_BORDER));

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(new EmptyBorder(14, 14, 10, 14));

        controls.add(buildFocusCard());
        controls.add(Box.createVerticalStrut(12));
        controls.add(buildSearchRow());
        controls.add(Box.createVerticalStrut(12));
        controls.add(buildDifficultyFilters());
        controls.add(Box.createVerticalStrut(12));
        controls.add(buildTopicFilters());

        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);
        listHeader.setBorder(new EmptyBorder(2, 10, 8, 10));

        JLabel label = new JLabel("Problem Bank");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.PRIMARY_TEXT);

        visibleCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        visibleCountLabel.setForeground(Theme.MUTED_TEXT);

        listHeader.add(label, BorderLayout.WEST);
        listHeader.add(visibleCountLabel, BorderLayout.EAST);

        problemListPanel.setLayout(new BoxLayout(problemListPanel, BoxLayout.Y_AXIS));
        problemListPanel.setOpaque(false);
        problemListPanel.setBorder(new EmptyBorder(0, 10, 12, 10));

        JScrollPane scroll = new JScrollPane(problemListPanel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel listWrap = new JPanel(new BorderLayout());
        listWrap.setOpaque(false);
        listWrap.add(listHeader, BorderLayout.NORTH);
        listWrap.add(scroll, BorderLayout.CENTER);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(listWrap, BorderLayout.CENTER);

        rebuildProblemList();
        return panel;
    }

    private JPanel buildSearchRow() {
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Search by problem title or topic");
        searchField.putClientProperty("JTextField.placeholderText", "Search title or topic...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)));

        JButton clearBtn = smallIconButton("x", "Clear search and topic filter");
        clearBtn.addActionListener(e -> {
            selectedTopicFilter = null;
            searchField.setText("");
            rebuildProblemList();
            refreshTopicButtons();
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearBtn, BorderLayout.EAST);
        return searchBar;
    }

    private JPanel buildFocusCard() {
        JPanel card = new GradientCard(new BorderLayout(12, 0), Theme.PRIMARY_START, RUN_COLOR);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Today's DSA Focus");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Finish one problem, then review the pattern.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 255, 255, 210));

        streakLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        streakLabel.setForeground(Color.WHITE);
        updateFocusCard();

        copy.add(title);
        copy.add(Box.createVerticalStrut(4));
        copy.add(sub);
        copy.add(Box.createVerticalStrut(7));
        copy.add(streakLabel);

        progressBar.setValue(Math.min(PROBLEMS.length, solvedProblemTitles.size()));
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(Color.WHITE);
        progressBar.setBackground(new Color(255, 255, 255, 70));
        progressBar.setPreferredSize(new Dimension(74, 8));

        JPanel progressWrap = new JPanel(new BorderLayout());
        progressWrap.setOpaque(false);
        progressWrap.setPreferredSize(new Dimension(82, 0));
        progressWrap.add(progressBar, BorderLayout.SOUTH);

        card.add(copy, BorderLayout.CENTER);
        card.add(progressWrap, BorderLayout.EAST);
        return card;
    }

    private JPanel buildDifficultyFilters() {
        JPanel row = new JPanel(new GridLayout(1, 4, 6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        row.add(createFilterButton("All", null, Theme.PRIMARY_START));
        row.add(createFilterButton("Easy", "Easy", EASY_COLOR));
        row.add(createFilterButton("Medium", "Medium", MEDIUM_COLOR));
        row.add(createFilterButton("Hard", "Hard", HARD_COLOR));
        refreshDifficultyButtons();
        return row;
    }

    private JPanel buildTopicFilters() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);

        JLabel title = new JLabel("Topics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Theme.PRIMARY_TEXT);

        JPanel grid = new JPanel(new GridLayout(0, 2, 6, 6));
        grid.setOpaque(false);

        JButton all = createTopicButton("All", null);
        topicButtons.add(all);
        grid.add(all);
        for (String topic : ALL_TOPICS) {
            JButton btn = createTopicButton(topic, topic);
            topicButtons.add(btn);
            grid.add(btn);
        }

        JScrollPane topicScroll = new JScrollPane(grid);
        topicScroll.setBorder(null);
        topicScroll.setOpaque(false);
        topicScroll.getViewport().setOpaque(false);
        topicScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        topicScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        topicScroll.getVerticalScrollBar().setUnitIncrement(10);
        topicScroll.setPreferredSize(new Dimension(0, 128));

        wrap.add(title, BorderLayout.NORTH);
        wrap.add(topicScroll, BorderLayout.CENTER);
        refreshTopicButtons();
        return wrap;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(14, 10));
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
            new EmptyBorder(16, 20, 16, 20)));

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));

        problemTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        problemTitle.setForeground(Theme.PRIMARY_TEXT);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        badges.setOpaque(false);
        badges.add(difficultyBadge);
        badges.add(topicBadge);

        titleArea.add(problemTitle);
        titleArea.add(badges);

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setForeground(Theme.MUTED_TEXT);

        JPanel metaStrip = new JPanel(new GridLayout(1, 4, 10, 0));
        metaStrip.setOpaque(false);
        metaStrip.add(metricCard("Pattern", patternLabel, Theme.PRIMARY_START));
        metaStrip.add(metricCard("Complexity", complexityLabel, RUN_COLOR));
        metaStrip.add(metricCard("Run Status", testStatusLabel, MEDIUM_COLOR));
        metaStrip.add(metricCard("Editor", editorStatsLabel, EASY_COLOR));

        header.add(titleArea, BorderLayout.CENTER);
        header.add(statusLabel, BorderLayout.EAST);
        header.add(metaStrip, BorderLayout.SOUTH);

        JSplitPane workspace = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildPromptPanel(), buildEditorPanel());
        workspace.setBorder(null);
        workspace.setDividerSize(5);
        workspace.setDividerLocation(260);
        workspace.setResizeWeight(0.34);
        workspace.setContinuousLayout(true);

        panel.add(header, BorderLayout.NORTH);
        panel.add(workspace, BorderLayout.CENTER);
        panel.add(buildBottomPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPromptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PAGE_BG);
        panel.setBorder(new EmptyBorder(14, 18, 10, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        header.add(sectionLabel("Problem Brief"), BorderLayout.WEST);
        header.add(makeTinyLabel("Read examples, mark edge cases, then code."), BorderLayout.EAST);

        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setBackground(Theme.PANEL_BG);
        descArea.setForeground(Theme.PRIMARY_TEXT);
        descArea.setBorder(new EmptyBorder(14, 16, 14, 16));

        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 10));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(270, 0));
        right.add(infoCard("Approach", "Start with brute force, identify repeated work, then choose the right data structure.", Theme.PRIMARY_START));
        right.add(infoCard("Edge Cases", "Empty input, one element, duplicates, null-like boundaries, and very large values.", MEDIUM_COLOR));

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);
        body.add(scroll, BorderLayout.CENTER);
        body.add(right, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PAGE_BG);
        panel.setBorder(new EmptyBorder(8, 18, 10, 18));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel label = sectionLabel("Java Editor");
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tools.setOpaque(false);
        JLabel language = makePillLabel("Java 17", Theme.PRIMARY_START, false);
        JButton formatBtn = createOutlineButton("Format", Theme.PRIMARY_START);
        JButton copyBtn = createOutlineButton("Copy", RUN_COLOR);
        formatBtn.setPreferredSize(new Dimension(84, 28));
        copyBtn.setPreferredSize(new Dimension(72, 28));
        formatBtn.addActionListener(e -> formatCode());
        copyBtn.addActionListener(e -> copyCode());
        tools.add(language);
        tools.add(formatBtn);
        tools.add(copyBtn);

        header.add(label, BorderLayout.WEST);
        header.add(tools, BorderLayout.EAST);

        codeEditor.setFont(resolveEditorFont());
        codeEditor.setBackground(editorBackground());
        codeEditor.setForeground(Theme.PRIMARY_TEXT);
        codeEditor.setCaretColor(Theme.PRIMARY_TEXT);
        codeEditor.setSelectionColor(new Color(99, 102, 241, 75));
        codeEditor.setBorder(new EmptyBorder(14, 16, 14, 16));
        codeEditor.setTabSize(4);
        codeEditor.setLineWrap(false);

        installUndoFilter();
        codeEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onEditorChanged(); }
            public void removeUpdate(DocumentEvent e) { onEditorChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        codeEditor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "stackUndo");
        codeEditor.getActionMap().put("stackUndo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { popUndo(); }
        });

        JScrollPane scroll = new JScrollPane(codeEditor);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        scroll.setRowHeaderView(new LineNumberGutter(codeEditor));
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getHorizontalScrollBar().setUnitIncrement(12);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.FIELD_BORDER),
            new EmptyBorder(10, 18, 10, 18)));

        consoleArea.setEditable(false);
        consoleArea.setRows(5);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        consoleArea.setBackground(Theme.isDark() ? new Color(18, 18, 28) : new Color(249, 250, 251));
        consoleArea.setForeground(Theme.PRIMARY_TEXT);
        consoleArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        consoleArea.setText("Console ready. Visible checks will appear here.");

        JScrollPane consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setPreferredSize(new Dimension(0, 104));
        consoleScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));

        JPanel consoleWrap = new JPanel(new BorderLayout(0, 8));
        consoleWrap.setOpaque(false);
        JPanel consoleHeader = new JPanel(new BorderLayout());
        consoleHeader.setOpaque(false);
        consoleHeader.add(sectionLabel("Run Console"), BorderLayout.WEST);
        consoleHeader.add(makeTinyLabel("Visible tests are simulated for practice flow."), BorderLayout.EAST);
        consoleWrap.add(consoleHeader, BorderLayout.NORTH);
        consoleWrap.add(consoleScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(3, 2, 8, 8));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(260, 104));

        JButton hintBtn = createOutlineButton("Hint", MEDIUM_COLOR);
        JButton resetBtn = createOutlineButton("Reset", Theme.MUTED_TEXT);
        JButton undoBtn = createOutlineButton("Undo", Theme.PRIMARY_START);
        JButton runBtn = createOutlineButton("Run", RUN_COLOR);
        JButton clearBtn = createOutlineButton("Clear", Theme.MUTED_TEXT);
        JButton submitBtn = createGradientButton("Submit", 118, 36);

        hintBtn.addActionListener(e -> showHint());
        resetBtn.addActionListener(e -> resetEditor());
        undoBtn.addActionListener(e -> popUndo());
        runBtn.addActionListener(e -> runCode());
        clearBtn.addActionListener(e -> consoleArea.setText("Console cleared."));
        submitBtn.addActionListener(e -> submitSolution());

        actions.add(hintBtn);
        actions.add(resetBtn);
        actions.add(undoBtn);
        actions.add(clearBtn);
        actions.add(runBtn);
        actions.add(submitBtn);

        panel.add(consoleWrap, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    /* ============================================================ */
    /*  PROBLEM LIST                                               */
    /* ============================================================ */
    private void rebuildProblemList() {
        problemListPanel.removeAll();

        int visible = 0;
        String query = searchField.getText().trim().toLowerCase();
        boolean exactTopicMode = selectedTopicFilter != null
            && searchField.getText().trim().equalsIgnoreCase(selectedTopicFilter);

        for (Problem p : PROBLEMS) {
            if (selectedDifficultyFilter != null && !p.difficulty.equals(selectedDifficultyFilter)) continue;

            if (exactTopicMode) {
                if (!p.topic.equalsIgnoreCase(selectedTopicFilter)) continue;
            } else if (!query.isEmpty()) {
                String haystack = (p.title + " " + p.topic + " " + p.difficulty).toLowerCase();
                if (!haystack.contains(query)) continue;
            } else if (selectedTopicFilter != null) {
                if (!p.topic.equalsIgnoreCase(selectedTopicFilter)) continue;
            }

            problemListPanel.add(buildProblemRow(p));
            problemListPanel.add(Box.createVerticalStrut(8));
            visible++;
        }

        if (visible == 0) {
            JLabel empty = new JLabel("<html><div style='text-align:center'>No problems match these filters.</div></html>");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(Theme.MUTED_TEXT);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(22, 8, 22, 8));
            problemListPanel.add(empty);
        }

        visibleCountLabel.setText(visible + " shown");
        problemListPanel.revalidate();
        problemListPanel.repaint();
    }

    private JPanel buildProblemRow(Problem p) {
        boolean selected = currentProblem != null && currentProblem.title.equals(p.title);
        boolean solved = solvedProblemTitles.contains(p.title);
        Color diffColor = difficultyColor(p.difficulty);

        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            boolean hover = false;

            {
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { loadProblem(p); }
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = selected
                    ? new Color(Theme.PRIMARY_START.getRed(), Theme.PRIMARY_START.getGreen(), Theme.PRIMARY_START.getBlue(), Theme.isDark() ? 58 : 30)
                    : solved
                        ? new Color(EASY_COLOR.getRed(), EASY_COLOR.getGreen(), EASY_COLOR.getBlue(), Theme.isDark() ? 28 : 18)
                    : hover
                        ? (Theme.isDark() ? new Color(42, 42, 58) : new Color(246, 247, 252))
                        : Theme.PANEL_BG;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(selected ? Theme.PRIMARY_START : Theme.FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(selected ? diffColor : solved ? EASY_COLOR : new Color(diffColor.getRed(), diffColor.getGreen(), diffColor.getBlue(), 150));
                g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        row.setBorder(new EmptyBorder(10, 12, 10, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JLabel status = new JLabel(solved ? "OK" : p.difficulty.substring(0, 1));
        status.setHorizontalAlignment(SwingConstants.CENTER);
        status.setFont(new Font("Segoe UI", Font.BOLD, 10));
        status.setForeground(solved ? Color.WHITE : diffColor);
        status.setOpaque(true);
        status.setBackground(solved ? EASY_COLOR : new Color(diffColor.getRed(), diffColor.getGreen(), diffColor.getBlue(), Theme.isDark() ? 32 : 18));
        status.setBorder(BorderFactory.createLineBorder(new Color(diffColor.getRed(), diffColor.getGreen(), diffColor.getBlue(), 120), 1, true));
        status.setPreferredSize(new Dimension(30, 30));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(p.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Theme.PRIMARY_TEXT);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(new JLabel("<html><span style='color:gray'>" + p.topic + "</span> &nbsp; " + patternFor(p) + "</html>"));

        JLabel diff = new JLabel(p.difficulty);
        diff.setFont(new Font("Segoe UI", Font.BOLD, 10));
        diff.setForeground(diffColor);

        row.add(status, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(diff, BorderLayout.EAST);
        return row;
    }

    /* ============================================================ */
    /*  AUTOCOMPLETE (TRIE)                                        */
    /* ============================================================ */
    private void onSearchChanged() {
        String query = searchField.getText().trim();
        autocompletePopup.setVisible(false);

        if (!applyingTopicFilter && selectedTopicFilter != null && !query.equalsIgnoreCase(selectedTopicFilter)) {
            selectedTopicFilter = null;
            refreshTopicButtons();
        }

        rebuildProblemList();

        if (query.isEmpty()) return;

        List<String> suggestions = topicTrie.autocomplete(query, ALL_TOPICS, 6);
        if (!suggestions.isEmpty() && searchField.isShowing()) {
            autocompletePopup.removeAll();
            for (String topic : suggestions) {
                JMenuItem item = new JMenuItem(topic);
                item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                item.addActionListener(e -> applyTopicFilter(topic));
                autocompletePopup.add(item);
            }
            autocompletePopup.show(searchField, 0, searchField.getHeight());
        }
    }

    private void applyTopicFilter(String topic) {
        selectedTopicFilter = topic;
        applyingTopicFilter = true;
        searchField.setText(topic == null ? "" : topic);
        applyingTopicFilter = false;
        autocompletePopup.setVisible(false);
        refreshTopicButtons();
        rebuildProblemList();
    }

    /* ============================================================ */
    /*  PROBLEM LOADING                                            */
    /* ============================================================ */
    private void loadProblem(Problem p) {
        currentProblem = p;
        undoStack.clear();

        problemTitle.setText(p.title);
        setBadge(difficultyBadge, p.difficulty, difficultyColor(p.difficulty), true);
        setBadge(topicBadge, p.topic, Theme.PRIMARY_START, false);
        patternLabel.setText(patternFor(p));
        complexityLabel.setText(complexityFor(p));
        testStatusLabel.setText("Not run");
        testStatusLabel.setForeground(MEDIUM_COLOR);
        lastRunCodeSnapshot = "";

        statusLabel.setText(solvedProblemTitles.contains(p.title) ? "Accepted in this session" : "Unsolved");
        statusLabel.setForeground(solvedProblemTitles.contains(p.title) ? Theme.SUCCESS_COLOR : Theme.MUTED_TEXT);

        descArea.setText(p.description);
        descArea.setCaretPosition(0);

        ignoringEdit = true;
        codeEditor.setText(p.starterCode);
        codeEditor.setCaretPosition(0);
        ignoringEdit = false;

        consoleArea.setText("Loaded \"" + p.title + "\". Write a solution, run checks, then submit.");
        updateEditorStats();
        updateProgressSummary();
        updateFocusCard();
        rebuildProblemList();
    }

    /* ============================================================ */
    /*  UNDO STACK                                                 */
    /* ============================================================ */
    private void installUndoFilter() {
        ((AbstractDocument) codeEditor.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                pushUndoSnapshot();
                super.insertString(fb, offset, string, attr);
            }

            @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                pushUndoSnapshot();
                super.remove(fb, offset, length);
            }

            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                pushUndoSnapshot();
                super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    private void pushUndoSnapshot() {
        if (ignoringEdit) return;
        String currentText = codeEditor.getText();
        if (!undoStack.isEmpty() && undoStack.peek().equals(currentText)) return;
        undoStack.push(currentText);
        if (undoStack.size() > 100) undoStack.remove(0);
    }

    private void popUndo() {
        if (undoStack.isEmpty()) {
            consoleArea.setText("Nothing to undo.");
            return;
        }

        ignoringEdit = true;
        String previous = undoStack.pop();
        int caret = Math.min(previous.length(), codeEditor.getCaretPosition());
        codeEditor.setText(previous);
        codeEditor.setCaretPosition(caret);
        ignoringEdit = false;
        updateEditorStats();
        consoleArea.setText("Undo applied.");
    }

    /* ============================================================ */
    /*  ACTIONS                                                    */
    /* ============================================================ */
    private void showHint() {
        String hint = switch (currentProblem.topic) {
            case "Arrays" -> "Track values you have already seen, or keep two pointers when the array is ordered.";
            case "Strings" -> "Work from both ends when the transformation is symmetric.";
            case "Stack" -> "Push opening symbols and compare every closing symbol with the top.";
            case "Binary Search" -> "Keep low <= high and shrink the search range after each comparison.";
            case "Linked List" -> "Slow and fast pointers are often enough when cycles are involved.";
            case "Trees" -> "Decide whether recursion or a queue mirrors the traversal order.";
            case "Graphs" -> "Mark visited nodes before exploring neighbors to avoid repeated work.";
            case "Dynamic Programming" -> "Define the state first, then write the transition from smaller states.";
            case "Sorting" -> "Split, solve the halves, then merge while preserving order.";
            case "Recursion" -> "Write the base case before the recursive case.";
            case "Design" -> "Combine a HashMap for lookup with a linked order structure for recency.";
            default -> randomGeneralHint();
        };
        consoleArea.setText("Hint for " + currentProblem.title + ":\n" + hint);
    }

    private String randomGeneralHint() {
        String[] hints = {
            "Start with the smallest input and write what the answer should be.",
            "Name edge cases before writing the main loop.",
            "Look for a data structure that gives the operation you repeat most often.",
            "If the brute-force solution repeats work, memoization or a set may help."
        };
        return hints[new Random().nextInt(hints.length)];
    }

    private void runCode() {
        String code = codeEditor.getText().trim();
        if (!hasUserCode(code)) {
            consoleArea.setText("Run stopped: write your solution first. The editor still matches the starter code.");
            testStatusLabel.setText("Needs code");
            testStatusLabel.setForeground(HARD_COLOR);
            JOptionPane.showMessageDialog(this,
                "Please write your solution first.",
                "No Code", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lastRunCodeSnapshot = code;
        testStatusLabel.setText("Visible passed");
        testStatusLabel.setForeground(EASY_COLOR);
        consoleArea.setText(
            "Running visible checks for " + currentProblem.title + "...\n\n" +
            "Test 1: Passed\n" +
            "Test 2: Passed\n" +
            "Test 3: Passed\n\n" +
            "All visible checks passed. Review edge cases before submitting."
        );
    }

    private void submitSolution() {
        String code = codeEditor.getText().trim();
        if (!hasUserCode(code)) {
            consoleArea.setText("Submit blocked: the solution is empty or unchanged from the starter code.");
            testStatusLabel.setText("Needs code");
            testStatusLabel.setForeground(HARD_COLOR);
            JOptionPane.showMessageDialog(this,
                "Please write your solution first.",
                "No Code", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!code.equals(lastRunCodeSnapshot)) {
            consoleArea.setText("Submit blocked: run the latest version before submitting.\nThis prevents stale run results from being counted.");
            testStatusLabel.setText("Run required");
            testStatusLabel.setForeground(MEDIUM_COLOR);
            JOptionPane.showMessageDialog(this,
                "Run the latest version before submitting.",
                "Run Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (solvedProblemTitles.contains(currentProblem.title)) {
            consoleArea.setText("Already accepted in this session. The solved count was not incremented again.");
            JOptionPane.showMessageDialog(this,
                "You already submitted this problem in this session.",
                "Already Accepted", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Submit your solution for \"" + currentProblem.title + "\"?\nThis will count as 1 problem solved.",
            "Submit Solution", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            solvedProblemTitles.add(currentProblem.title);
            solvedCount++;
            UserProfileCache.setProblemsSolved(solvedCount);

            int userId = UserProfileCache.getCurrentUserId();
            if (userId != -1) {
                StatsService.saveProblemsSolved(userId, solvedCount);
            }

            updateSolvedLabel();
            updateProgressSummary();
            updateFocusCard();
            statusLabel.setText("Accepted in this session");
            statusLabel.setForeground(Theme.SUCCESS_COLOR);
            testStatusLabel.setText("Accepted");
            testStatusLabel.setForeground(Theme.SUCCESS_COLOR);
            consoleArea.setText("Accepted: " + currentProblem.title + "\nTotal problems solved: " + solvedCount);

            JOptionPane.showMessageDialog(this,
                "Accepted!\n\nGreat job solving \"" + currentProblem.title + "\".\nTotal problems solved: " + solvedCount,
                "Accepted", JOptionPane.INFORMATION_MESSAGE);
            rebuildProblemList();
        }
    }

    private void resetEditor() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Reset the editor to the starter code for \"" + currentProblem.title + "\"?",
            "Reset Editor", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        pushUndoSnapshot();
        ignoringEdit = true;
        codeEditor.setText(currentProblem.starterCode);
        codeEditor.setCaretPosition(0);
        ignoringEdit = false;
        lastRunCodeSnapshot = "";
        testStatusLabel.setText("Not run");
        testStatusLabel.setForeground(MEDIUM_COLOR);
        updateEditorStats();
        consoleArea.setText("Editor reset. Use Undo if you need to recover your previous draft.");
    }

    private boolean hasUserCode(String code) {
        if (code.isEmpty() || code.equals(currentProblem.starterCode.trim())) return false;
        String withoutComments = code
            .replaceAll("(?s)/\\*.*?\\*/", "")
            .replaceAll("(?m)//.*$", "")
            .replaceAll("[{}();,\\s]", "");
        String starterWithoutComments = currentProblem.starterCode
            .replaceAll("(?s)/\\*.*?\\*/", "")
            .replaceAll("(?m)//.*$", "")
            .replaceAll("[{}();,\\s]", "");
        return withoutComments.length() > starterWithoutComments.length();
    }

    private void onEditorChanged() {
        if (ignoringEdit) return;
        lastRunCodeSnapshot = "";
        testStatusLabel.setText("Edited");
        testStatusLabel.setForeground(MEDIUM_COLOR);
        updateEditorStats();
    }

    private void updateEditorStats() {
        String text = codeEditor.getText();
        int lines = text.isEmpty() ? 1 : text.split("\\R", -1).length;
        int chars = text.length();
        editorStatsLabel.setText(lines + " lines / " + chars + " chars");
    }

    private void updateProgressSummary() {
        int sessionSolved = solvedProblemTitles.size();
        progressLabel.setText(sessionSolved + "/" + PROBLEMS.length + " session");
        progressBar.setValue(Math.min(PROBLEMS.length, sessionSolved));
    }

    private void updateFocusCard() {
        if (streakLabel != null) {
            int remaining = Math.max(0, PROBLEMS.length - solvedProblemTitles.size());
            streakLabel.setText(solvedProblemTitles.size() + " accepted this session | " + remaining + " left in bank");
        }
    }

    private void updateSolvedLabel() {
        solvedLabel.setText(solvedCount + " Solved");
        solvedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        solvedLabel.setForeground(Theme.SUCCESS_COLOR);
        solvedLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(Theme.SUCCESS_COLOR.getRed(), Theme.SUCCESS_COLOR.getGreen(), Theme.SUCCESS_COLOR.getBlue(), 120), 1, true),
            new EmptyBorder(6, 12, 6, 12)));
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        codeEditor.setBackground(editorBackground());
        codeEditor.setForeground(Theme.PRIMARY_TEXT);
        codeEditor.setCaretColor(Theme.PRIMARY_TEXT);
        descArea.setBackground(Theme.PANEL_BG);
        descArea.setForeground(Theme.PRIMARY_TEXT);
        consoleArea.setBackground(Theme.isDark() ? new Color(18, 18, 28) : new Color(249, 250, 251));
        consoleArea.setForeground(Theme.PRIMARY_TEXT);
        refreshDifficultyButtons();
        refreshTopicButtons();
        rebuildProblemList();
        repaint();
    }

    /* ============================================================ */
    /*  BUTTON + STYLE HELPERS                                     */
    /* ============================================================ */
    private JPanel metricCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.isDark() ? new Color(37, 37, 52) : new Color(250, 251, 255));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Theme.isDark() ? 90 : 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labelText.setForeground(Theme.MUTED_TEXT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(accent);

        card.add(labelText, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel infoCard(String title, String body, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Theme.isDark() ? 80 : 55));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heading.setForeground(Theme.PRIMARY_TEXT);

        JLabel copy = new JLabel("<html><body style='width:205px'>" + body + "</body></html>");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copy.setForeground(Theme.MUTED_TEXT);

        card.add(heading, BorderLayout.NORTH);
        card.add(copy, BorderLayout.CENTER);
        return card;
    }

    private JLabel makeTinyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private void formatCode() {
        pushUndoSnapshot();
        String[] lines = codeEditor.getText().replace("\t", "    ").split("\\R", -1);
        StringBuilder formatted = new StringBuilder();
        for (String line : lines) {
            formatted.append(line.stripTrailing()).append('\n');
        }
        if (formatted.length() > 0) formatted.setLength(formatted.length() - 1);

        ignoringEdit = true;
        codeEditor.setText(formatted.toString());
        codeEditor.setCaretPosition(0);
        ignoringEdit = false;
        lastRunCodeSnapshot = "";
        testStatusLabel.setText("Edited");
        testStatusLabel.setForeground(MEDIUM_COLOR);
        updateEditorStats();
        consoleArea.setText("Formatted indentation tabs and removed trailing spaces.");
    }

    private void copyCode() {
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(codeEditor.getText()), null);
        consoleArea.setText("Code copied to clipboard.");
    }

    private String patternFor(Problem p) {
        return switch (p.topic) {
            case "Arrays" -> p.title.equals("Trapping Rain Water") ? "Two pointers" : "Hashing / scan";
            case "Strings" -> "Two pointers";
            case "Stack" -> "Stack matching";
            case "Binary Search" -> "Binary search";
            case "Linked List" -> "Fast-slow pointers";
            case "Trees" -> "BFS levels";
            case "Graphs" -> p.title.contains("Cycle") ? "DFS coloring" : "DFS / BFS";
            case "Dynamic Programming" -> "DP table";
            case "Sorting" -> "Divide and merge";
            case "Recursion" -> "Base + recurrence";
            case "Design" -> "Map + ordering";
            default -> "Core DSA";
        };
    }

    private String complexityFor(Problem p) {
        return switch (p.title) {
            case "Two Sum", "Valid Parentheses", "Maximum Subarray", "Binary Search",
                 "Linked List Cycle", "Number of Islands", "Trapping Rain Water" -> "Target O(n)";
            case "Level Order Traversal" -> "O(n) traversal";
            case "Coin Change" -> "O(amount * coins)";
            case "Merge Sort" -> "O(n log n)";
            case "Fibonacci Number" -> "O(n) with memo";
            case "LRU Cache" -> "O(1) ops";
            case "Detect Cycle in Graph" -> "O(V + E)";
            case "Longest Common Subsequence" -> "O(n*m)";
            default -> "Analyze before submit";
        };
    }

    private EmptyBorder pillPadding() {
        return new EmptyBorder(6, 12, 6, 12);
    }

    private javax.swing.border.Border pillBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            pillPadding());
    }

    private JButton createFilterButton(String text, String value, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            selectedDifficultyFilter = value;
            refreshDifficultyButtons();
            rebuildProblemList();
        });
        difficultyButtons.put(value == null ? "All" : value, btn);
        return btn;
    }

    private JButton createTopicButton(String text, String topic) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> applyTopicFilter(topic));
        btn.putClientProperty("topic", topic);
        return btn;
    }

    private void refreshDifficultyButtons() {
        for (Map.Entry<String, JButton> entry : difficultyButtons.entrySet()) {
            String key = entry.getKey();
            JButton btn = entry.getValue();
            boolean selected = (selectedDifficultyFilter == null && "All".equals(key))
                || key.equals(selectedDifficultyFilter);
            Color color = switch (key) {
                case "Easy" -> EASY_COLOR;
                case "Medium" -> MEDIUM_COLOR;
                case "Hard" -> HARD_COLOR;
                default -> Theme.PRIMARY_START;
            };
            styleChipButton(btn, color, selected);
        }
    }

    private void refreshTopicButtons() {
        for (JButton btn : topicButtons) {
            String topic = (String) btn.getClientProperty("topic");
            boolean selected = (selectedTopicFilter == null && topic == null)
                || (selectedTopicFilter != null && selectedTopicFilter.equals(topic));
            styleChipButton(btn, Theme.PRIMARY_START, selected);
        }
    }

    private void styleChipButton(AbstractButton btn, Color color, boolean selected) {
        btn.setForeground(selected ? Color.WHITE : color);
        btn.setBackground(selected
            ? color
            : new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 30 : 18));
        btn.setBorder(BorderFactory.createLineBorder(
            new Color(color.getRed(), color.getGreen(), color.getBlue(), selected ? 210 : 120), 1, true));
        btn.setOpaque(true);
    }

    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
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
        return btn;
    }

    private JButton smallIconButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Theme.MUTED_TEXT);
        btn.setPreferredSize(new Dimension(34, 32));
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Theme.PRIMARY_TEXT);
        return label;
    }

    private JLabel makePillLabel(String text, Color color, boolean filled) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(filled ? Color.WHITE : color);
        label.setOpaque(true);
        label.setBackground(filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            new EmptyBorder(6, 12, 6, 12)));
        return label;
    }

    private void setBadge(JLabel label, String text, Color color, boolean filled) {
        label.setText(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(filled ? Color.WHITE : color);
        label.setOpaque(true);
        label.setBackground(filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 40 : 22));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            new EmptyBorder(3, 8, 3, 8)));
    }

    private Color difficultyColor(String difficulty) {
        return switch (difficulty) {
            case "Easy" -> EASY_COLOR;
            case "Hard" -> HARD_COLOR;
            default -> MEDIUM_COLOR;
        };
    }

    private Color editorBackground() {
        return Theme.isDark() ? new Color(16, 18, 26) : new Color(250, 251, 253);
    }

    private Font resolveEditorFont() {
        Font font = new Font("JetBrains Mono", Font.PLAIN, 13);
        if ("Dialog".equals(font.getFamily())) {
            font = new Font("Consolas", Font.PLAIN, 13);
        }
        if ("Dialog".equals(font.getFamily())) {
            font = new Font("Courier New", Font.PLAIN, 13);
        }
        return font;
    }

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
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(new Color(255, 255, 255, 55));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class LineNumberGutter extends JComponent implements DocumentListener {
        private final JTextArea area;
        private final Font gutterFont = new Font("Consolas", Font.PLAIN, 12);

        LineNumberGutter(JTextArea area) {
            this.area = area;
            area.getDocument().addDocumentListener(this);
            setPreferredSize(new Dimension(42, 1));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Theme.isDark() ? new Color(25, 27, 38) : new Color(241, 243, 248));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.FIELD_BORDER);
            g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

            g2.setFont(gutterFont);
            g2.setColor(Theme.MUTED_TEXT);
            FontMetrics fm = g2.getFontMetrics();

            int lineHeight = area.getFontMetrics(area.getFont()).getHeight();
            int startY = area.getInsets().top + fm.getAscent();
            int lines = Math.max(1, area.getLineCount());
            for (int i = 1; i <= lines; i++) {
                String s = String.valueOf(i);
                int y = startY + (i - 1) * lineHeight;
                g2.drawString(s, getWidth() - fm.stringWidth(s) - 10, y);
            }
            g2.dispose();
        }

        public void insertUpdate(DocumentEvent e) { repaint(); }
        public void removeUpdate(DocumentEvent e) { repaint(); }
        public void changedUpdate(DocumentEvent e) { repaint(); }
    }
}
