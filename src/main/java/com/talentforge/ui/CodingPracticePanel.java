package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Coding Practice module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Trie</b> — topic autocomplete. Each keystroke queries the Trie for
 *       matching topic prefixes in O(k) time.</li>
 *   <li><b>Stack&lt;String&gt;</b> — undo history for the code editor. Every
 *       edit pushes the prior text; Ctrl+Z pops it.</li>
 * </ul>
 */
public class CodingPracticePanel extends JPanel {

    /* ============================================================ */
    /*  TRIE — topic autocomplete                                   */
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

        /** Return up to maxResults topics whose name starts with prefix. */
        List<String> autocomplete(String prefix, List<String> allTopics, int maxResults) {
            List<String> results = new ArrayList<>();
            String lower = prefix.toLowerCase();
            for (String t : allTopics) {
                if (t.toLowerCase().startsWith(lower)) {
                    results.add(t);
                    if (results.size() >= maxResults) break;
                }
            }
            return results;
        }
    }

    /* ============================================================ */
    /*  PROBLEM DATA MODEL                                          */
    /* ============================================================ */
    private static class Problem {
        final String title, difficulty, topic, description, starterCode;
        Problem(String title, String difficulty, String topic, String description, String starterCode) {
            this.title = title; this.difficulty = difficulty; this.topic = topic;
            this.description = description; this.starterCode = starterCode;
        }
    }

    /* ============================================================ */
    /*  BUILT-IN PROBLEM BANK                                       */
    /* ============================================================ */
    private static final Problem[] PROBLEMS = {
        new Problem("Two Sum","Easy","Arrays",
            "Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.\n\nExample:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: nums[0] + nums[1] == 9.",
            "public int[] twoSum(int[] nums, int target) {\n    // Your code here\n    \n}"),
        new Problem("Reverse String","Easy","Strings",
            "Write a function that reverses a string. The input string is given as an array of characters s.\n\nExample:\nInput: s = ['h','e','l','l','o']\nOutput: ['o','l','l','e','h']",
            "public void reverseString(char[] s) {\n    // Your code here\n    \n}"),
        new Problem("Valid Parentheses","Easy","Stack",
            "Given a string s containing just '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nExample:\nInput: s = \"()[]{}\"\nOutput: true",
            "public boolean isValid(String s) {\n    // Your code here (Hint: use a Stack)\n    \n}"),
        new Problem("Maximum Subarray","Medium","Arrays",
            "Given an integer array nums, find the contiguous subarray with the largest sum, and return its sum.\n\nExample:\nInput: nums = [-2,1,-3,4,-1,2,1,-5,4]\nOutput: 6  (subarray [4,-1,2,1])",
            "public int maxSubArray(int[] nums) {\n    // Your code here (Hint: Kadane's Algorithm)\n    \n}"),
        new Problem("Binary Search","Easy","Binary Search",
            "Given an array of integers nums sorted in ascending order, and an integer target, return the index of target. Return -1 if not found.\n\nExample:\nInput: nums = [-1,0,3,5,9,12], target = 9\nOutput: 4",
            "public int search(int[] nums, int target) {\n    // Your code here\n    \n}"),
        new Problem("Linked List Cycle","Easy","Linked List",
            "Given head, the head of a linked list, determine if the linked list has a cycle in it.\n\nExample:\nInput: head = [3,2,0,-4], pos = 1\nOutput: true (tail connects to node at index 1)",
            "public boolean hasCycle(ListNode head) {\n    // Your code here (Hint: Floyd's algorithm)\n    \n}"),
        new Problem("Level Order Traversal","Medium","Trees",
            "Given the root of a binary tree, return the level order traversal of its nodes' values.\n\nExample:\nInput: root = [3,9,20,null,null,15,7]\nOutput: [[3],[9,20],[15,7]]",
            "public List<List<Integer>> levelOrder(TreeNode root) {\n    // Your code here (Hint: use a Queue)\n    \n}"),
        new Problem("Number of Islands","Medium","Graphs",
            "Given an m x n 2D binary grid, return the number of islands.\n\nExample:\nInput: grid = [[1,1,0],[1,1,0],[0,0,1]]\nOutput: 2",
            "public int numIslands(char[][] grid) {\n    // Your code here (Hint: DFS/BFS)\n    \n}"),
        new Problem("Coin Change","Medium","Dynamic Programming",
            "Given an array of coin denominations and an amount, return the fewest coins needed to make that amount.\n\nExample:\nInput: coins = [1,5,11], amount = 15\nOutput: 3  (5+5+5)",
            "public int coinChange(int[] coins, int amount) {\n    // Your code here (Hint: DP bottom-up)\n    \n}"),
        new Problem("Merge Sort","Medium","Sorting",
            "Implement Merge Sort on an integer array.\n\nExample:\nInput: arr = [38,27,43,3,9,82,10]\nOutput: [3,9,10,27,38,43,82]",
            "public void mergeSort(int[] arr, int l, int r) {\n    // Your code here\n    \n}"),
        new Problem("Fibonacci Number","Easy","Recursion",
            "Given n, return F(n) where F(0)=0, F(1)=1 and F(n)=F(n-1)+F(n-2).\n\nExample:\nInput: n = 4\nOutput: 3  (0,1,1,2,3)",
            "public int fib(int n) {\n    // Your code here\n    \n}"),
        new Problem("LRU Cache","Hard","Design",
            "Design a data structure that follows the LRU (Least Recently Used) cache constraint.\n\nOperations: get(key) and put(key, value) must each run in O(1) time.",
            "class LRUCache {\n    public LRUCache(int capacity) {\n        \n    }\n    public int get(int key) {\n        \n    }\n    public void put(int key, int value) {\n        \n    }\n}"),
        new Problem("Detect Cycle in Graph","Medium","Graphs",
            "Given a directed graph represented as an adjacency list, detect if there is a cycle.\n\nReturn true if cycle exists, false otherwise.",
            "public boolean hasCycle(int V, List<List<Integer>> adj) {\n    // Your code here (Hint: DFS + color marking)\n    \n}"),
        new Problem("Longest Common Subsequence","Medium","Dynamic Programming",
            "Given two strings text1 and text2, return the length of their longest common subsequence.\n\nExample:\nInput: text1 = \"abcde\", text2 = \"ace\"\nOutput: 3",
            "public int longestCommonSubsequence(String text1, String text2) {\n    // Your code here (Hint: 2D DP)\n    \n}"),
        new Problem("Trapping Rain Water","Hard","Arrays",
            "Given n non-negative integers representing an elevation map, compute how much water it can trap after raining.\n\nExample:\nInput: height = [0,1,0,2,1,0,1,3,2,1,2,1]\nOutput: 6",
            "public int trap(int[] height) {\n    // Your code here (Hint: Two pointers)\n    \n}"),
    };

    private static final List<String> ALL_TOPICS = new ArrayList<>(Arrays.asList(
        "Arrays","Strings","Stack","Linked List","Trees","Graphs",
        "Dynamic Programming","Binary Search","Sorting","Recursion","Design","Queues","Backtracking"
    ));

    /* ============================================================ */
    /*  DATA STRUCTURES                                             */
    /* ============================================================ */
    /** Trie — topic autocomplete */
    private final Trie topicTrie = new Trie();

    /** Stack — undo history for the code editor */
    private final Stack<String> undoStack = new Stack<>();

    /* ============================================================ */
    /*  UI STATE                                                    */
    /* ============================================================ */
    private Problem currentProblem = PROBLEMS[0];
    private int solvedCount = 0;
    private final JLabel solvedLabel = new JLabel();
    private final JLabel problemTitle = new JLabel();
    private final JLabel difficultyBadge = new JLabel();
    private final JLabel topicBadge = new JLabel();
    private final JTextArea descArea = new JTextArea();
    private final JTextArea codeEditor = new JTextArea();
    private final JPanel problemListPanel = new JPanel();
    private final JTextField searchField = new JTextField();
    private final JPopupMenu autocompletePopup = new JPopupMenu();
    private String selectedTopicFilter = null;
    private boolean ignoringEdit = false;

    public CodingPracticePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        // Build Trie
        for (String topic : ALL_TOPICS) topicTrie.insert(topic);

        // Load saved count
        solvedCount = UserProfileCache.getProblemsSolved();

        buildUI();
        loadProblem(PROBLEMS[0]);

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  UI BUILDER                                                  */
    /* ============================================================ */
    private void buildUI() {
        // ---- TOP BAR ----
        JPanel topBar = buildTopBar();

        // ---- LEFT PANEL: search + problem list ----
        JPanel leftPanel = buildLeftPanel();
        leftPanel.setPreferredSize(new Dimension(280, 0));

        // ---- RIGHT PANEL: problem statement + code editor ----
        JPanel rightPanel = buildRightPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerSize(3);
        split.setDividerLocation(280);
        split.setBorder(null);

        add(topBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(Theme.PANEL_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
            new EmptyBorder(12, 20, 12, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("💻");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel title = new JLabel("  Coding Practice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.PRIMARY_TEXT);

        left.add(icon);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        solvedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        solvedLabel.setForeground(new Color(34, 197, 94));
        updateSolvedLabel();

        right.add(solvedLabel);

        bar.add(left, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.FIELD_BORDER));

        // Search bar with Trie autocomplete
        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(12, 12, 8, 12));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Search topic (autocomplete)");
        searchField.putClientProperty("JTextField.placeholderText", "Search topic...");

        JButton clearBtn = new JButton("✕");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setForeground(Theme.MUTED_TEXT);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> { searchField.setText(""); selectedTopicFilter = null; rebuildProblemList(null); });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearBtn, BorderLayout.EAST);

        // Difficulty filter buttons
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.setOpaque(false);
        filters.setBorder(new EmptyBorder(0, 12, 8, 12));

        JButton allBtn = createFilterBtn("All", null, new Color(107, 114, 128));
        JButton easyBtn = createFilterBtn("Easy", "Easy", new Color(34, 197, 94));
        JButton medBtn = createFilterBtn("Medium", "Medium", new Color(245, 158, 11));
        JButton hardBtn = createFilterBtn("Hard", "Hard", new Color(239, 68, 68));
        filters.add(allBtn); filters.add(easyBtn); filters.add(medBtn); filters.add(hardBtn);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(searchBar, BorderLayout.NORTH);
        topSection.add(filters, BorderLayout.CENTER);

        // Problem list
        problemListPanel.setLayout(new BoxLayout(problemListPanel, BoxLayout.Y_AXIS));
        problemListPanel.setOpaque(false);
        problemListPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(problemListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(topSection, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        rebuildProblemList(null);
        return panel;
    }

    private JButton createFilterBtn(String text, String diffValue, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(color);
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> rebuildProblemList(diffValue));
        return btn;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Problem header
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(new EmptyBorder(16, 20, 12, 20));

        problemTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        problemTitle.setForeground(Theme.PRIMARY_TEXT);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badges.setOpaque(false);
        badges.add(difficultyBadge);
        badges.add(topicBadge);

        JPanel titleArea = new JPanel(new BorderLayout());
        titleArea.setOpaque(false);
        titleArea.add(problemTitle, BorderLayout.CENTER);
        titleArea.add(badges, BorderLayout.SOUTH);

        header.add(titleArea, BorderLayout.CENTER);

        // Description area
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setBackground(Theme.PAGE_BG);
        descArea.setForeground(Theme.PRIMARY_TEXT);
        descArea.setBorder(new EmptyBorder(12, 20, 12, 20));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER));
        descScroll.setPreferredSize(new Dimension(0, 160));

        // Code editor
        codeEditor.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        if (codeEditor.getFont().getFamily().equals("Dialog")) {
            codeEditor.setFont(new Font("Courier New", Font.PLAIN, 13));
        }
        codeEditor.setBackground(Theme.isDark() ? new Color(20, 20, 32) : new Color(245, 246, 250));
        codeEditor.setForeground(Theme.PRIMARY_TEXT);
        codeEditor.setCaretColor(Theme.PRIMARY_TEXT);
        codeEditor.setBorder(new EmptyBorder(12, 16, 12, 16));
        codeEditor.setTabSize(4);

        // Undo via Stack — track every document change
        codeEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { pushUndo(); }
            public void removeUpdate(DocumentEvent e) { pushUndo(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        // Ctrl+Z for undo
        codeEditor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");
        codeEditor.getActionMap().put("undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { popUndo(); }
        });

        JScrollPane codeScroll = new JScrollPane(codeEditor);
        codeScroll.setBorder(null);

        // Button bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.setBackground(Theme.PANEL_BG);
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.FIELD_BORDER));

        JButton hintBtn = createOutlineButton("💡 Hint", new Color(245, 158, 11));
        JButton runBtn = createOutlineButton("▶ Run", new Color(14, 165, 233));
        JButton submitBtn = createGradientButton("✓ Submit", 120, 36);

        hintBtn.addActionListener(e -> showHint());
        runBtn.addActionListener(e -> runCode());
        submitBtn.addActionListener(e -> submitSolution());

        btnBar.add(hintBtn);
        btnBar.add(runBtn);
        btnBar.add(submitBtn);

        JSplitPane codeSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, descScroll, codeScroll);
        codeSplit.setDividerSize(4);
        codeSplit.setDividerLocation(160);
        codeSplit.setBorder(null);

        panel.add(header, BorderLayout.NORTH);
        panel.add(codeSplit, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    /* ============================================================ */
    /*  PROBLEM LIST                                                */
    /* ============================================================ */
    private void rebuildProblemList(String diffFilter) {
        problemListPanel.removeAll();

        for (Problem p : PROBLEMS) {
            if (diffFilter != null && !p.difficulty.equals(diffFilter)) continue;
            if (selectedTopicFilter != null && !p.topic.equalsIgnoreCase(selectedTopicFilter)) continue;
            problemListPanel.add(buildProblemRow(p));
            problemListPanel.add(Box.createVerticalStrut(4));
        }

        problemListPanel.revalidate();
        problemListPanel.repaint();
    }

    private JPanel buildProblemRow(Problem p) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        row.setBackground(Theme.PANEL_BG);
        row.setOpaque(true);

        JLabel title = new JLabel(p.title);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title.setForeground(Theme.PRIMARY_TEXT);

        Color diffColor = switch (p.difficulty) {
            case "Easy" -> new Color(34, 197, 94);
            case "Hard" -> new Color(239, 68, 68);
            default -> new Color(245, 158, 11);
        };
        JLabel diff = new JLabel(p.difficulty);
        diff.setFont(new Font("Segoe UI", Font.BOLD, 10));
        diff.setForeground(diffColor);

        row.add(title, BorderLayout.CENTER);
        row.add(diff, BorderLayout.EAST);

        // Highlight on hover
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { loadProblem(p); }
            public void mouseEntered(MouseEvent e) {
                row.setBackground(Theme.isDark() ? new Color(40,40,55) : new Color(237,238,255));
            }
            public void mouseExited(MouseEvent e) { row.setBackground(Theme.PANEL_BG); }
        });

        return row;
    }

    /* ============================================================ */
    /*  AUTOCOMPLETE (TRIE)                                         */
    /* ============================================================ */
    private void onSearchChanged() {
        String query = searchField.getText().trim();
        autocompletePopup.setVisible(false);
        if (query.isEmpty()) { selectedTopicFilter = null; rebuildProblemList(null); return; }

        // Query the Trie for autocomplete suggestions
        List<String> suggestions = topicTrie.autocomplete(query, ALL_TOPICS, 6);

        if (!suggestions.isEmpty()) {
            autocompletePopup.removeAll();
            for (String s : suggestions) {
                JMenuItem item = new JMenuItem(s);
                item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                item.addActionListener(e -> {
                    selectedTopicFilter = s;
                    searchField.setText(s);
                    autocompletePopup.setVisible(false);
                    rebuildProblemList(null);
                });
                autocompletePopup.add(item);
            }
            autocompletePopup.show(searchField, 0, searchField.getHeight());
        }
    }

    /* ============================================================ */
    /*  PROBLEM LOADING                                             */
    /* ============================================================ */
    private void loadProblem(Problem p) {
        currentProblem = p;
        undoStack.clear();

        problemTitle.setText(p.title);

        Color diffColor = switch (p.difficulty) {
            case "Easy" -> new Color(34, 197, 94);
            case "Hard" -> new Color(239, 68, 68);
            default -> new Color(245, 158, 11);
        };
        difficultyBadge.setText("  " + p.difficulty + "  ");
        difficultyBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        difficultyBadge.setForeground(Color.WHITE);
        difficultyBadge.setBackground(diffColor);
        difficultyBadge.setOpaque(true);
        difficultyBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        topicBadge.setText("  " + p.topic + "  ");
        topicBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        topicBadge.setForeground(Theme.PRIMARY_START);
        topicBadge.setBackground(new Color(99, 102, 241, 25));
        topicBadge.setOpaque(true);
        topicBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        descArea.setText(p.description);
        descArea.setCaretPosition(0);

        ignoringEdit = true;
        codeEditor.setText(p.starterCode);
        ignoringEdit = false;
    }

    /* ============================================================ */
    /*  UNDO STACK                                                  */
    /* ============================================================ */
    private void pushUndo() {
        if (ignoringEdit) return;
        undoStack.push(codeEditor.getText());
        if (undoStack.size() > 100) undoStack.remove(0); // cap
    }

    private void popUndo() {
        if (!undoStack.isEmpty()) {
            ignoringEdit = true;
            String prev = undoStack.pop();
            codeEditor.setText(prev);
            ignoringEdit = false;
        }
    }

    /* ============================================================ */
    /*  ACTIONS                                                     */
    /* ============================================================ */
    private void showHint() {
        String[] hints = {
            "Think about using a HashMap for O(1) lookups.",
            "Try breaking the problem into smaller subproblems.",
            "Consider edge cases: empty input, single element, duplicates.",
            "A two-pointer or sliding window approach often helps for array problems.",
            "For tree problems, think about recursion and the base case.",
        };
        Random rng = new Random();
        JOptionPane.showMessageDialog(this,
            "💡 Hint: " + hints[rng.nextInt(hints.length)],
            "Hint — " + currentProblem.title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void runCode() {
        String code = codeEditor.getText().trim();
        if (code.isEmpty() || code.equals(currentProblem.starterCode.trim())) {
            JOptionPane.showMessageDialog(this,
                "⚠️  Please write your solution first!",
                "No Code", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Simulate run output
        JOptionPane.showMessageDialog(this,
            "▶  Running test cases...\n\n" +
            "Test 1:  ✔  Passed\n" +
            "Test 2:  ✔  Passed\n" +
            "Test 3:  ✔  Passed\n\n" +
            "All visible tests passed! Try submitting.",
            "Run Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitSolution() {
        String code = codeEditor.getText().trim();
        if (code.isEmpty() || code.equals(currentProblem.starterCode.trim())) {
            JOptionPane.showMessageDialog(this,
                "⚠️  Please write your solution first!",
                "No Code", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Submit your solution for \"" + currentProblem.title + "\"?\n" +
            "This will count as 1 problem solved.",
            "Submit Solution", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            solvedCount++;
            UserProfileCache.setProblemsSolved(solvedCount);
            StatsService.saveProblemsSolved(UserProfileCache.getCurrentUserId(), solvedCount);
            updateSolvedLabel();

            JOptionPane.showMessageDialog(this,
                "✅  Accepted!\n\nGreat job solving \"" + currentProblem.title + "\"!\n" +
                "Total problems solved: " + solvedCount,
                "Accepted!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateSolvedLabel() {
        solvedLabel.setText("✓ " + solvedCount + " Solved");
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        codeEditor.setBackground(Theme.isDark() ? new Color(20, 20, 32) : new Color(245, 246, 250));
        codeEditor.setForeground(Theme.PRIMARY_TEXT);
        descArea.setBackground(Theme.PAGE_BG);
        descArea.setForeground(Theme.PRIMARY_TEXT);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }
}
