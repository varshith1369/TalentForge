package com.talentforge.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Study Planner Module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Stack&lt;Action&gt;</b> — Undo and Redo operations. Each add/delete/toggle
 *       pushes a state Action onto the undo stack, clearing the redo stack. Clicking
 *       Undo pops the undo stack, rolls back the change, and pushes onto redo. Redo does the opposite.</li>
 * </ul>
 */
public class StudyPlannerPanel extends JPanel {

    private static class Task {
        final String name;
        boolean completed = false;

        Task(String name) {
            this.name = name;
        }

        Task(String name, boolean completed) {
            this.name = name;
            this.completed = completed;
        }

        Task copy() {
            return new Task(name, completed);
        }
    }

    private static class PlannerState {
        final List<Task> tasks;

        PlannerState(List<Task> tasks) {
            this.tasks = new ArrayList<>();
            for (Task t : tasks) {
                this.tasks.add(t.copy());
            }
        }
    }

    /* ============================================================ */
    /*  DATA STRUCTURES (UNDO / REDO STACKS)                        */
    /* ============================================================ */
    private final Stack<PlannerState> undoStack = new Stack<>();
    private final Stack<PlannerState> redoStack = new Stack<>();

    /* ============================================================ */
    /*  PLANNER STATE                                               */
    /* ============================================================ */
    private List<Task> currentTasks = new ArrayList<>();

    /* ============================================================ */
    /*  UI REFERENCES                                               */
    /* ============================================================ */
    private final JPanel tasksContainer = new JPanel();
    private final JTextField taskInputField = new JTextField();
    private final JButton undoBtn = createOutlineButton("↶ Undo", Theme.PRIMARY_START);
    private final JButton redoBtn = createOutlineButton("↷ Redo", Theme.PRIMARY_END);

    public StudyPlannerPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        seedTasks();
        buildUI();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    private void seedTasks() {
        currentTasks.add(new Task("Complete 5 LeetCode Medium problems"));
        currentTasks.add(new Task("Revise DBMS normalization notes", true));
        currentTasks.add(new Task("Mock interview session on Behavioral questions"));
        currentTasks.add(new Task("Apply to SDE roles on job portal"));
    }

    /* ============================================================ */
    /*  UI SETUP                                                    */
    /* ============================================================ */
    private void buildUI() {
        // TOP Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JLabel title = new JLabel("📅 Study Planner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitle = new JLabel("Plan your daily tasks. Use Undo / Redo controls to roll back actions or re-apply them.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        // Undo & Redo buttons in the header
        JPanel historyControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        historyControls.setOpaque(false);

        undoBtn.addActionListener(e -> performUndo());
        redoBtn.addActionListener(e -> performRedo());
        updateHistoryButtons();

        historyControls.add(undoBtn);
        historyControls.add(redoBtn);

        header.add(left, BorderLayout.CENTER);
        header.add(historyControls, BorderLayout.EAST);

        // Center Panel: Input on top, task cards list below
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Form card
        ElevatedCard formCard = new ElevatedCard(new BorderLayout());
        formCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel formInner = new JPanel(new BorderLayout(12, 0));
        formInner.setOpaque(false);
        formInner.setBorder(new EmptyBorder(14, 18, 14, 18));

        taskInputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        taskInputField.putClientProperty("JTextField.placeholderText", "Enter new study task...");

        JButton addBtn = createGradientButton("Add Task", 120, 36);
        addBtn.addActionListener(e -> addTask());
        taskInputField.addActionListener(e -> addTask());

        formInner.add(taskInputField, BorderLayout.CENTER);
        formInner.add(addBtn, BorderLayout.EAST);
        formCard.add(formInner, BorderLayout.CENTER);

        // Tasks container
        tasksContainer.setLayout(new BoxLayout(tasksContainer, BoxLayout.Y_AXIS));
        tasksContainer.setOpaque(false);

        rebuildTaskList();

        JScrollPane scroll = new JScrollPane(tasksContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        body.add(formCard, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private void rebuildTaskList() {
        tasksContainer.removeAll();

        if (currentTasks.isEmpty()) {
            JLabel empty = new JLabel("No tasks added yet. Create one above!");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Theme.MUTED_TEXT);
            tasksContainer.add(empty);
        } else {
            for (Task t : currentTasks) {
                tasksContainer.add(buildTaskCard(t));
                tasksContainer.add(Box.createVerticalStrut(8));
            }
        }

        tasksContainer.revalidate();
        tasksContainer.repaint();
    }

    private ElevatedCard buildTaskCard(Task task) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        if (task.completed) {
            card.setAccentColor(new Color(34, 197, 94));
        }

        JPanel inner = new JPanel(new BorderLayout(12, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(14, 16, 14, 16));

        JCheckBox check = new JCheckBox(task.name);
        check.setFont(new Font("Segoe UI", task.completed ? Font.ITALIC : Font.BOLD, 13));
        check.setForeground(task.completed ? Theme.MUTED_TEXT : Theme.PRIMARY_TEXT);
        check.setSelected(task.completed);
        check.setOpaque(false);
        check.addActionListener(e -> toggleTaskCompletion(task, check.isSelected()));

        JButton delBtn = createSmallButton("✕", Theme.MUTED_TEXT);
        delBtn.addActionListener(e -> deleteTask(task));

        inner.add(check, BorderLayout.CENTER);
        inner.add(delBtn, BorderLayout.EAST);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /* ============================================================ */
    /*  UNDO / REDO LOGIC USING STACKS                              */
    /* ============================================================ */
    private void saveStateBeforeChange() {
        undoStack.push(new PlannerState(currentTasks));
        redoStack.clear(); // Clear redo stack on any new action
        updateHistoryButtons();
    }

    private void performUndo() {
        if (undoStack.isEmpty()) return;

        // Push current state to redo stack
        redoStack.push(new PlannerState(currentTasks));

        // Restore state from undo stack
        PlannerState previousState = undoStack.pop();
        currentTasks = previousState.tasks;

        updateHistoryButtons();
        rebuildTaskList();
    }

    private void performRedo() {
        if (redoStack.isEmpty()) return;

        // Push current state to undo stack
        undoStack.push(new PlannerState(currentTasks));

        // Restore state from redo stack
        PlannerState nextState = redoStack.pop();
        currentTasks = nextState.tasks;

        updateHistoryButtons();
        rebuildTaskList();
    }

    private void updateHistoryButtons() {
        undoBtn.setEnabled(!undoStack.isEmpty());
        redoBtn.setEnabled(!redoStack.isEmpty());
    }

    /* ============================================================ */
    /*  ACTIONS                                                     */
    /* ============================================================ */
    private void addTask() {
        String name = taskInputField.getText().trim();
        if (name.isEmpty()) return;

        saveStateBeforeChange();

        currentTasks.add(new Task(name));
        taskInputField.setText("");

        rebuildTaskList();
    }

    private void toggleTaskCompletion(Task task, boolean completed) {
        saveStateBeforeChange();

        task.completed = completed;

        rebuildTaskList();
    }

    private void deleteTask(Task task) {
        saveStateBeforeChange();

        currentTasks.remove(task);

        rebuildTaskList();
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        rebuildTaskList();
        repaint();
    }

    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setMaximumSize(new Dimension(w, h));
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
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        return btn;
    }

    private JButton createSmallButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(color);
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
