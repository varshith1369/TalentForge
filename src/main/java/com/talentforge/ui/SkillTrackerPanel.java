package com.talentforge.ui;

import com.talentforge.db.SkillTrackerService;
import com.talentforge.db.SkillTrackerService.HistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Skill Tracker module — lets the user manage placement-related skills,
 * adjust levels, and review an ordered progress history.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>HashMap&lt;String, Integer&gt;</b> — skill name → current level (0–10).
 *       Provides O(1) lookups when updating individual skills.</li>
 *   <li><b>LinkedList&lt;HistoryEntry&gt;</b> — ordered progress history.
 *       New entries are prepended; the list is iterated to render
 *       the scrolling timeline in the UI.</li>
 * </ul>
 */
public class SkillTrackerPanel extends JPanel {

    // ====================== DATA STRUCTURES ======================
    /** HashMap — skill name → level (0-10). O(1) lookup for skill levels. */
    private final HashMap<String, Integer> skillMap = new HashMap<>();

    /** LinkedList — ordered progress history, newest first. */
    private LinkedList<HistoryEntry> historyList = new LinkedList<>();

    // ====================== UI REFERENCES ========================
    private final JPanel skillsGrid = new JPanel();
    private final JPanel historyPanel = new JPanel();
    private final JTextField addSkillField = new JTextField();
    private final JComboBox<Integer> levelCombo = new JComboBox<>();
    private final JLabel skillCountLabel = new JLabel();
    private final JLabel avgLevelLabel = new JLabel();
    private final JLabel historyCountLabel = new JLabel();

    // Default skills seeded on first use
    private static final String[] DEFAULT_SKILLS = {
            "Java", "Python", "SQL", "DSA", "Aptitude",
            "Communication", "System Design", "OOP"
    };

    // Accent colors for skill cards (cycled)
    private static final Color[] CARD_ACCENTS = {
            new Color(99, 102, 241),   // indigo
            new Color(139, 92, 246),   // violet
            new Color(236, 72, 153),   // pink
            new Color(14, 165, 233),   // sky
            new Color(34, 197, 94),    // green
            new Color(245, 158, 11),   // amber
            new Color(239, 68, 68),    // red
            new Color(168, 85, 247),   // purple
    };

    public SkillTrackerPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        // Load data from DB into our data structures
        loadData();

        // Build the full UI
        JScrollPane scrollPane = new JScrollPane(buildContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Re-theme on dark mode toggle
        Theme.addListener(this::onThemeChanged);
    }

    /* ================================================================ */
    /*  DATA LOADING                                                    */
    /* ================================================================ */

    private void loadData() {
        int userId = UserProfileCache.getCurrentUserId();

        // HashMap — load skills from DB
        skillMap.clear();
        skillMap.putAll(SkillTrackerService.loadSkills(userId));

        // Seed defaults on first use
        if (skillMap.isEmpty()) {
            for (String s : DEFAULT_SKILLS) {
                skillMap.put(s, 0);
                SkillTrackerService.saveSkill(userId, s, 0);
            }
        }

        // LinkedList — load history from DB
        historyList = SkillTrackerService.loadHistory(userId);
    }

    /* ================================================================ */
    /*  MAIN CONTENT BUILDER                                            */
    /* ================================================================ */

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));

        root.add(buildHeader());
        root.add(Box.createVerticalStrut(20));
        root.add(buildSummaryCards());
        root.add(Box.createVerticalStrut(20));
        root.add(buildAddSkillBar());
        root.add(Box.createVerticalStrut(20));
        root.add(buildSkillsSection());
        root.add(Box.createVerticalStrut(24));
        root.add(buildHistorySection());

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

        JLabel title = new JLabel("📊  Skill Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel sub = new JLabel("Track your placement skills and measure progress over time");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED_TEXT);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        // Refresh button
        JButton refreshBtn = createGradientButton("↻  Refresh", 100, 34);
        refreshBtn.addActionListener(e -> refreshAll());

        header.add(left, BorderLayout.CENTER);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    /* ================================================================ */
    /*  SUMMARY STAT CARDS                                              */
    /* ================================================================ */

    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        row.add(buildMiniStat("Total Skills", String.valueOf(skillMap.size()),
                "📚", new Color(99, 102, 241), skillCountLabel));
        row.add(buildMiniStat("Average Level", calcAverageLevel(),
                "📈", new Color(34, 197, 94), avgLevelLabel));
        row.add(buildMiniStat("History Entries", String.valueOf(historyList.size()),
                "📝", new Color(245, 158, 11), historyCountLabel));

        return row;
    }

    private ElevatedCard buildMiniStat(String label, String value, String emoji,
                                       Color accent, JLabel valueRef) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        card.setAccentColor(accent);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel valLbl = valueRef != null ? valueRef : new JLabel();
        valLbl.setText(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(Theme.PRIMARY_TEXT);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(Theme.MUTED_TEXT);

        inner.add(emojiLbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(valLbl);
        inner.add(Box.createVerticalStrut(2));
        inner.add(nameLbl);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private String calcAverageLevel() {
        if (skillMap.isEmpty()) return "0.0";
        double avg = skillMap.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        return String.format("%.1f / 10", avg);
    }

    /* ================================================================ */
    /*  ADD SKILL BAR                                                   */
    /* ================================================================ */

    private JPanel buildAddSkillBar() {
        ElevatedCard bar = new ElevatedCard(new BorderLayout());
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel inner = new JPanel(new BorderLayout(12, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Skill name field
        addSkillField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addSkillField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        addSkillField.setToolTipText("Enter a new skill name");

        // Level combo
        for (int i = 0; i <= 10; i++) levelCombo.addItem(i);
        levelCombo.setSelectedIndex(1);
        levelCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        levelCombo.setPreferredSize(new Dimension(70, 36));

        JPanel leftFields = new JPanel(new BorderLayout(8, 0));
        leftFields.setOpaque(false);
        leftFields.add(addSkillField, BorderLayout.CENTER);

        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        levelPanel.setOpaque(false);
        JLabel lvlLabel = new JLabel("Level:");
        lvlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lvlLabel.setForeground(Theme.MUTED_TEXT);
        levelPanel.add(lvlLabel);
        levelPanel.add(levelCombo);

        leftFields.add(levelPanel, BorderLayout.EAST);

        // Add button
        JButton addBtn = createGradientButton("＋ Add Skill", 130, 36);
        addBtn.addActionListener(e -> addSkill());

        // Enter key shortcut
        addSkillField.addActionListener(e -> addSkill());

        inner.add(leftFields, BorderLayout.CENTER);
        inner.add(addBtn, BorderLayout.EAST);
        bar.add(inner, BorderLayout.CENTER);
        return bar;
    }

    /* ================================================================ */
    /*  SKILLS GRID                                                     */
    /* ================================================================ */

    private JPanel buildSkillsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel sectionTitle = new JLabel("💡 Your Skills");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(Theme.PRIMARY_TEXT);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        section.add(sectionTitle, BorderLayout.NORTH);

        skillsGrid.setLayout(new GridLayout(0, 2, 14, 14));
        skillsGrid.setOpaque(false);

        rebuildSkillsGrid();

        section.add(skillsGrid, BorderLayout.CENTER);
        return section;
    }

    private void rebuildSkillsGrid() {
        skillsGrid.removeAll();

        // Sort skills alphabetically from the HashMap for consistent display
        java.util.List<String> sortedSkills = new ArrayList<>(skillMap.keySet());
        Collections.sort(sortedSkills);

        int idx = 0;
        for (String skillName : sortedSkills) {
            int level = skillMap.get(skillName);  // HashMap O(1) lookup
            Color accent = CARD_ACCENTS[idx % CARD_ACCENTS.length];
            skillsGrid.add(buildSkillCard(skillName, level, accent));
            idx++;
        }

        skillsGrid.revalidate();
        skillsGrid.repaint();
    }

    private ElevatedCard buildSkillCard(String skillName, int level, Color accent) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        card.setAccentColor(accent);
        card.setPreferredSize(new Dimension(0, 110));

        JPanel inner = new JPanel(new BorderLayout(12, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Left side — name + level text + buttons
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JLabel nameLbl = new JLabel(skillName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(Theme.PRIMARY_TEXT);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel levelLbl = new JLabel("Level " + level + " / 10");
        levelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        levelLbl.setForeground(Theme.MUTED_TEXT);
        levelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // +/- buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton minusBtn = createSmallButton("−", new Color(239, 68, 68));
        JButton plusBtn = createSmallButton("+", new Color(34, 197, 94));
        JButton deleteBtn = createSmallButton("✕", Theme.MUTED_TEXT);

        minusBtn.addActionListener(e -> updateSkillLevel(skillName, -1));
        plusBtn.addActionListener(e -> updateSkillLevel(skillName, +1));
        deleteBtn.addActionListener(e -> removeSkill(skillName));

        btnRow.add(minusBtn);
        btnRow.add(plusBtn);
        btnRow.add(Box.createHorizontalStrut(8));
        btnRow.add(deleteBtn);

        leftCol.add(nameLbl);
        leftCol.add(Box.createVerticalStrut(4));
        leftCol.add(levelLbl);
        leftCol.add(Box.createVerticalStrut(8));
        leftCol.add(btnRow);

        // Right side — circular progress ring
        JPanel ringPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 8;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Background arc
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Theme.isDark()
                        ? new Color(50, 50, 65)
                        : new Color(230, 232, 240));
                g2.drawArc(x, y, size, size, 0, 360);

                // Progress arc
                int angle = (int) (360.0 * level / 10);
                g2.setColor(accent);
                g2.drawArc(x, y, size, size, 90, -angle);

                // Center text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(Theme.PRIMARY_TEXT);
                String text = String.valueOf(level);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,
                        (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                g2.dispose();
            }
        };
        ringPanel.setOpaque(false);
        ringPanel.setPreferredSize(new Dimension(70, 70));

        inner.add(leftCol, BorderLayout.CENTER);
        inner.add(ringPanel, BorderLayout.EAST);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /* ================================================================ */
    /*  HISTORY SECTION                                                 */
    /* ================================================================ */

    private JPanel buildHistorySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);

        JLabel sectionTitle = new JLabel("📋 Progress History");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(Theme.PRIMARY_TEXT);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        section.add(sectionTitle, BorderLayout.NORTH);

        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setOpaque(false);

        rebuildHistoryPanel();

        section.add(historyPanel, BorderLayout.CENTER);
        return section;
    }

    private void rebuildHistoryPanel() {
        historyPanel.removeAll();

        if (historyList.isEmpty()) {
            JLabel empty = new JLabel("No history yet — update a skill to create your first entry.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Theme.MUTED_TEXT);
            empty.setBorder(new EmptyBorder(20, 0, 20, 0));
            historyPanel.add(empty);
        } else {
            // Iterate the LinkedList — this is the ordered traversal
            int shown = 0;
            for (HistoryEntry entry : historyList) {
                if (shown >= 50) break;  // cap display
                historyPanel.add(buildHistoryRow(entry));
                historyPanel.add(Box.createVerticalStrut(6));
                shown++;
            }
        }

        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private ElevatedCard buildHistoryRow(HistoryEntry entry) {
        ElevatedCard row = new ElevatedCard(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        boolean isIncrease = entry.newLevel > entry.oldLevel;
        Color changeColor = isIncrease ? new Color(34, 197, 94) : new Color(239, 68, 68);

        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Left: arrow icon + skill name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel arrow = new JLabel(isIncrease ? "▲" : "▼");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        arrow.setForeground(changeColor);

        JLabel name = new JLabel(entry.skillName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Theme.PRIMARY_TEXT);

        JLabel change = new JLabel("Level " + entry.oldLevel + " → " + entry.newLevel);
        change.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        change.setForeground(changeColor);

        left.add(arrow);
        left.add(name);
        left.add(change);

        // Right: timestamp + delete
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        String displayTime = entry.timestamp;
        if (displayTime != null && displayTime.length() > 16) {
            displayTime = displayTime.substring(0, 16);
        }
        JLabel timeLbl = new JLabel(displayTime != null ? displayTime : "");
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(Theme.MUTED_TEXT);

        JButton delBtn = createSmallButton("✕", Theme.MUTED_TEXT);
        delBtn.addActionListener(e -> deleteHistoryEntry(entry));

        right.add(timeLbl);
        right.add(delBtn);

        inner.add(left, BorderLayout.CENTER);
        inner.add(right, BorderLayout.EAST);
        row.add(inner, BorderLayout.CENTER);
        return row;
    }

    /* ================================================================ */
    /*  ACTIONS                                                         */
    /* ================================================================ */

    private void addSkill() {
        String name = addSkillField.getText().trim();
        if (name.isEmpty()) return;

        // Check if already in HashMap
        if (skillMap.containsKey(name)) {
            JOptionPane.showMessageDialog(this,
                    "Skill \"" + name + "\" already exists!",
                    "Duplicate Skill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int level = (Integer) levelCombo.getSelectedItem();
        int userId = UserProfileCache.getCurrentUserId();

        // Update HashMap
        skillMap.put(name, level);

        // Persist
        SkillTrackerService.saveSkill(userId, name, level);
        SkillTrackerService.addHistoryEntry(userId, name, 0, level);

        // Reload LinkedList from DB so ordering is correct
        historyList = SkillTrackerService.loadHistory(userId);

        // Clear field
        addSkillField.setText("");
        levelCombo.setSelectedIndex(1);

        // Rebuild UI
        refreshUI();
    }

    private void updateSkillLevel(String skillName, int delta) {
        int oldLevel = skillMap.getOrDefault(skillName, 0);  // HashMap O(1) get
        int newLevel = Math.max(0, Math.min(10, oldLevel + delta));
        if (newLevel == oldLevel) return;

        int userId = UserProfileCache.getCurrentUserId();

        // Update HashMap
        skillMap.put(skillName, newLevel);

        // Persist
        SkillTrackerService.saveSkill(userId, skillName, newLevel);
        SkillTrackerService.addHistoryEntry(userId, skillName, oldLevel, newLevel);

        // Prepend to LinkedList (newest first)
        historyList = SkillTrackerService.loadHistory(userId);

        refreshUI();
    }

    private void removeSkill(String skillName) {
        int result = JOptionPane.showConfirmDialog(this,
                "Remove skill \"" + skillName + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        int userId = UserProfileCache.getCurrentUserId();

        // Remove from HashMap
        skillMap.remove(skillName);

        // Delete from DB
        SkillTrackerService.deleteSkill(userId, skillName);

        // Reload history
        historyList = SkillTrackerService.loadHistory(userId);

        refreshUI();
    }

    private void deleteHistoryEntry(HistoryEntry entry) {
        int userId = UserProfileCache.getCurrentUserId();
        SkillTrackerService.deleteHistoryEntry(userId, entry.id);

        // Remove from LinkedList
        historyList.removeIf(e -> e.id == entry.id);

        refreshUI();
    }

    private void refreshAll() {
        loadData();
        refreshUI();
    }

    private void refreshUI() {
        // Update summary labels
        skillCountLabel.setText(String.valueOf(skillMap.size()));
        avgLevelLabel.setText(calcAverageLevel());
        historyCountLabel.setText(String.valueOf(historyList.size()));

        rebuildSkillsGrid();
        rebuildHistoryPanel();
    }

    private void onThemeChanged() {
        setBackground(Theme.PAGE_BG);
        SwingUtilities.invokeLater(() -> {
            rebuildSkillsGrid();
            rebuildHistoryPanel();
            revalidate();
            repaint();
        });
    }

    /* ================================================================ */
    /*  UTILITY — styled buttons                                        */
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

    private JButton createSmallButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(color.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(color);
            }
        });
        return btn;
    }
}
