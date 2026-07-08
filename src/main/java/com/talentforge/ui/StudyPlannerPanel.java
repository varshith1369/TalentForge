package com.talentforge.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Study Planner Module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Stack&lt;PlannerState&gt;</b> - Undo and redo operations. Each add, edit,
 *       delete, or completion toggle pushes a full planner snapshot onto the undo stack,
 *       while redo stores the forward states.</li>
 * </ul>
 */
public class StudyPlannerPanel extends JPanel {

    private static class Task {
        String title;
        String category;
        String notes;
        int priority; // 1-3
        boolean completed;

        Task(String title, String category, String notes, int priority, boolean completed) {
            this.title = title;
            this.category = category;
            this.notes = notes;
            this.priority = priority;
            this.completed = completed;
        }

        Task copy() {
            return new Task(title, category, notes, priority, completed);
        }
    }

    private static class PlannerState {
        final List<Task> tasks;
        final String selectedTaskTitle;

        PlannerState(List<Task> tasks, Task selectedTask) {
            this.tasks = new ArrayList<>();
            for (Task task : tasks) {
                this.tasks.add(task.copy());
            }
            this.selectedTaskTitle = selectedTask == null ? null : selectedTask.title;
        }
    }

    private static final Color SKY = new Color(56, 189, 248);
    private static final Color INDIGO = new Color(79, 70, 229);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color ROSE = new Color(244, 63, 94);
    private static final Color SLATE = new Color(100, 116, 139);

    /* ============================================================ */
    /*  DATA STRUCTURES                                            */
    /* ============================================================ */
    private final Stack<PlannerState> undoStack = new Stack<>();
    private final Stack<PlannerState> redoStack = new Stack<>();

    /* ============================================================ */
    /*  STATE                                                      */
    /* ============================================================ */
    private List<Task> currentTasks = new ArrayList<>();
    private Task selectedTask;
    private boolean editingExisting = false;
    private String filterMode = "All";
    private String searchQuery = "";

    /* ============================================================ */
    /*  UI REFERENCES                                              */
    /* ============================================================ */
    private final JPanel tasksContainer = new JPanel();
    private final JTextField titleField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JTextArea notesArea = new JTextArea(6, 24);
    private final JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    private final JButton undoBtn = createOutlineButton("Undo", INDIGO);
    private final JButton redoBtn = createOutlineButton("Redo", SKY);
    private final JLabel statusLabel = new JLabel();
    private final JLabel plannerSummaryLabel = new JLabel();
    private final JLabel totalTasksLabel = new JLabel();
    private final JLabel doneTasksLabel = new JLabel();
    private final JLabel focusTasksLabel = new JLabel();
    private final JLabel detailCategoryLabel = new JLabel();
    private final JLabel detailTitleLabel = new JLabel();
    private final JLabel detailNotesLabel = new JLabel();
    private final JLabel detailMetaLabel = new JLabel();
    private final JLabel editorStateLabel = new JLabel();
    private final JProgressBar completionBar = new JProgressBar();
    private JButton saveTaskButton;
    private JButton markDoneButton;
    private JButton deleteTaskButton;

    public StudyPlannerPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        seedTasks();
        buildUI();
        selectedTask = currentTasks.isEmpty() ? null : currentTasks.get(0);
        refreshAll();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    private void seedTasks() {
        currentTasks.add(new Task(
            "Complete 5 LeetCode medium problems",
            "Coding",
            "Focus on arrays and binary search. Write the explanation after each solution.",
            1, false));
        currentTasks.add(new Task(
            "Revise DBMS normalization notes",
            "Core CS",
            "Quick pass through 1NF, 2NF, 3NF, and one BCNF example.",
            2, true));
        currentTasks.add(new Task(
            "Mock interview session on behavioral questions",
            "Interview",
            "Use STAR answers for teamwork, conflict, and ownership stories.",
            1, false));
        currentTasks.add(new Task(
            "Apply to SDE roles on job portal",
            "Placement",
            "Shortlist five roles with a backend or platform focus.",
            3, false));
    }

    /* ============================================================ */
    /*  UI SETUP                                                   */
    /* ============================================================ */
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 24, 24));

        root.add(buildHeroPanel(), BorderLayout.NORTH);
        root.add(buildWorkspace(), BorderLayout.CENTER);

        add(buildTopBar(), BorderLayout.NORTH);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.PANEL_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new GradientPaint(0, 0,
                    new Color(SKY.getRed(), SKY.getGreen(), SKY.getBlue(), Theme.isDark() ? 70 : 40),
                    getWidth(), 0,
                    new Color(INDIGO.getRed(), INDIGO.getGreen(), INDIGO.getBlue(), Theme.isDark() ? 44 : 24)));
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
        JLabel title = new JLabel("Study Planner Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        JLabel sub = new JLabel("Plan focused daily work, keep the next task visible, and use undo or redo without losing control.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        undoBtn.addActionListener(e -> performUndo());
        redoBtn.addActionListener(e -> performRedo());
        right.add(undoBtn);
        right.add(redoBtn);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(pillBorder(TEAL()));
        right.add(statusLabel);

        bar.add(left, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildHeroPanel() {
        GradientCard hero = new GradientCard(new BorderLayout(18, 0), SKY, INDIGO);
        hero.setBorder(new EmptyBorder(24, 24, 24, 24));
        hero.setPreferredSize(new Dimension(0, 140));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Move from scattered intentions to a real plan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Capture the task, assign a lane, pick urgency, and keep the current focus visible while you revise the plan.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 220));

        plannerSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        plannerSummaryLabel.setForeground(Color.WHITE);

        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(sub);
        copy.add(Box.createVerticalStrut(12));
        copy.add(plannerSummaryLabel);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(360, 0));
        stats.add(heroStat("Total Tasks", totalTasksLabel));
        stats.add(heroStat("Completed", doneTasksLabel));
        stats.add(heroStat("High Focus", focusTasksLabel));

        hero.add(copy, BorderLayout.CENTER);
        hero.add(stats, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0;
        gc.weightx = 0.34;
        gc.insets = new Insets(0, 0, 0, 14);
        workspace.add(buildComposerPanel(), gc);

        gc.gridx = 1;
        gc.weightx = 0.30;
        gc.insets = new Insets(0, 0, 0, 14);
        workspace.add(buildQueuePanel(), gc);

        gc.gridx = 2;
        gc.weightx = 0.36;
        gc.insets = new Insets(0, 0, 0, 0);
        workspace.add(buildDetailPanel(), gc);

        return workspace;
    }

    private JPanel buildComposerPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Task Composer"), BorderLayout.WEST);
        editorStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        editorStateLabel.setForeground(Theme.MUTED_TEXT);
        header.add(editorStateLabel, BorderLayout.EAST);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(fieldLabel("Task title"));
        form.add(spacer(4));
        styleField(titleField, "Example: Revise graphs");
        form.add(titleField);

        form.add(spacer(12));
        form.add(fieldLabel("Category"));
        form.add(spacer(4));
        styleField(categoryField, "Example: Coding, Core CS, HR");
        form.add(categoryField);

        form.add(spacer(12));
        form.add(fieldLabel("Priority"));
        form.add(spacer(4));
        priorityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priorityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(priorityCombo);

        form.add(spacer(12));
        form.add(fieldLabel("Notes"));
        form.add(spacer(4));
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(Theme.isDark() ? new Color(28, 32, 44) : new Color(250, 251, 253));
        notesArea.setForeground(Theme.PRIMARY_TEXT);
        notesArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        notesScroll.setPreferredSize(new Dimension(0, 180));

        titleField.getDocument().addDocumentListener(editorListener());
        categoryField.getDocument().addDocumentListener(editorListener());
        notesArea.getDocument().addDocumentListener(editorListener());

        JPanel helperCards = new JPanel(new GridLayout(2, 1, 0, 10));
        helperCards.setOpaque(false);
        helperCards.add(infoCard("Write action-first tasks", "Good tasks start with a verb and describe one clear outcome instead of a vague intention.", TEAL()));
        helperCards.add(infoCard("Keep the note useful", "Include the next concrete step, constraints, or what done should look like.", ORANGE));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton resetBtn = createOutlineButton("Reset", SLATE);
        resetBtn.addActionListener(e -> resetEditor());
        saveTaskButton = createGradientButton("Add Task", 140, 40);
        saveTaskButton.addActionListener(e -> saveTask());
        actions.add(resetBtn);
        actions.add(saveTaskButton);

        form.add(notesScroll);
        form.add(spacer(14));
        form.add(helperCards);

        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildQueuePanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Planner Lanes"), BorderLayout.WEST);
        header.add(makeTinyLabel("Search and filter your tasks"), BorderLayout.EAST);

        styleField(searchField, "Search tasks...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        JPanel filters = new JPanel(new GridLayout(1, 3, 8, 0));
        filters.setOpaque(false);
        filters.add(createFilterButton("All"));
        filters.add(createFilterButton("Pending"));
        filters.add(createFilterButton("Done"));

        tasksContainer.setLayout(new BoxLayout(tasksContainer, BoxLayout.Y_AXIS));
        tasksContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(tasksContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.CENTER);
        top.add(filters, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDetailPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Task Detail"), BorderLayout.WEST);
        header.add(makeTinyLabel("Keep the next move visible"), BorderLayout.EAST);

        detailCategoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        detailCategoryLabel.setForeground(SKY);
        detailTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        detailTitleLabel.setForeground(Theme.PRIMARY_TEXT);
        detailNotesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailNotesLabel.setForeground(Theme.MUTED_TEXT);
        detailMetaLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        detailMetaLabel.setForeground(SLATE);

        JPanel detailCopy = new JPanel();
        detailCopy.setOpaque(false);
        detailCopy.setLayout(new BoxLayout(detailCopy, BoxLayout.Y_AXIS));
        detailCopy.add(detailCategoryLabel);
        detailCopy.add(spacer(6));
        detailCopy.add(detailTitleLabel);
        detailCopy.add(spacer(10));
        detailCopy.add(detailNotesLabel);
        detailCopy.add(spacer(12));
        detailCopy.add(detailMetaLabel);

        completionBar.setBorderPainted(false);
        completionBar.setStringPainted(true);
        completionBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        completionBar.setForeground(TEAL());
        completionBar.setBackground(Theme.isDark() ? new Color(45, 45, 60) : new Color(229, 231, 235));

        JButton editButton = createOutlineButton("Edit", SKY);
        editButton.addActionListener(e -> loadSelectedIntoEditor());
        markDoneButton = createGradientButton("Mark Complete", 160, 40);
        markDoneButton.addActionListener(e -> toggleSelectedTaskCompletion());
        deleteTaskButton = createOutlineButton("Delete", ROSE);
        deleteTaskButton.addActionListener(e -> deleteSelectedTask());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(editButton);
        actions.add(deleteTaskButton);
        actions.add(markDoneButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(detailCopy, BorderLayout.CENTER);
        panel.add(completionBar, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(actions, BorderLayout.SOUTH);
        return wrapper;
    }

    /* ============================================================ */
    /*  REFRESH                                                    */
    /* ============================================================ */
    private void refreshAll() {
        if (selectedTask == null && !currentTasks.isEmpty()) {
            selectedTask = currentTasks.get(0);
        }
        rebuildTaskList();
        updateHeaderStats();
        updateDetailPanel();
        updateEditorState();
        updateHistoryButtons();
    }

    private void rebuildTaskList() {
        tasksContainer.removeAll();

        List<Task> visible = filteredTasks();
        if (visible.isEmpty()) {
            tasksContainer.add(emptyLabel(currentTasks.isEmpty()
                ? "No tasks yet. Add one from the composer."
                : "No tasks match this search or filter."));
        } else {
            for (Task task : visible) {
                tasksContainer.add(buildTaskCard(task, task == selectedTask));
                tasksContainer.add(spacer(10));
            }
        }

        tasksContainer.revalidate();
        tasksContainer.repaint();
    }

    private List<Task> filteredTasks() {
        List<Task> visible = new ArrayList<>();
        for (Task task : currentTasks) {
            if (!matchesFilter(task)) continue;
            String haystack = (task.title + " " + task.category + " " + task.notes).toLowerCase();
            if (!searchQuery.isBlank() && !haystack.contains(searchQuery)) continue;
            visible.add(task);
        }
        return visible;
    }

    private boolean matchesFilter(Task task) {
        return switch (filterMode) {
            case "Pending" -> !task.completed;
            case "Done" -> task.completed;
            default -> true;
        };
    }

    private JComponent buildTaskCard(Task task, boolean selected) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setAccent(task.completed ? GREEN : selected ? SKY : null);
        card.setBorder(new EmptyBorder(14, 16, 14, 14));

        JLabel top = new JLabel((task.completed ? "DONE / " : priorityLabel(task.priority) + " / ") + task.category.toUpperCase());
        top.setFont(new Font("Segoe UI", Font.BOLD, 10));
        top.setForeground(task.completed ? GREEN : priorityColor(task.priority));

        JLabel title = new JLabel(task.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel body = new JLabel(htmlText(truncate(task.notes, 120), 240));
        body.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        body.setForeground(Theme.MUTED_TEXT);

        JCheckBox done = new JCheckBox("Completed");
        done.setOpaque(false);
        done.setSelected(task.completed);
        done.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        done.setForeground(Theme.MUTED_TEXT);
        done.addActionListener(e -> toggleTaskCompletion(task, done.isSelected()));

        card.add(top, BorderLayout.NORTH);
        card.add(title, BorderLayout.CENTER);
        card.add(body, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.add(done, BorderLayout.SOUTH);
        wrap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wrap.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedTask = task;
                refreshAll();
            }
        });
        return wrap;
    }

    private void updateHeaderStats() {
        int total = currentTasks.size();
        int done = 0;
        int high = 0;
        for (Task task : currentTasks) {
            if (task.completed) done++;
            if (!task.completed && task.priority == 1) high++;
        }

        totalTasksLabel.setText(String.valueOf(total));
        doneTasksLabel.setText(String.valueOf(done));
        focusTasksLabel.setText(String.valueOf(high));
        plannerSummaryLabel.setText(total == 0
            ? "No tasks in the planner yet."
            : (currentTasks.get(0).completed ? "Top task is already complete. Review the next pending one."
                : "Current top focus: " + currentTasks.get(0).title));
        statusLabel.setText(done == total && total > 0 ? "All Done" : "Planner Active");
    }

    private void updateDetailPanel() {
        if (selectedTask == null) {
            detailCategoryLabel.setText("NO TASK SELECTED");
            detailTitleLabel.setText("Build a task to start the planner");
            detailNotesLabel.setText(htmlText("The detail panel will show category, priority, notes, and whether the selected task is complete.", 320));
            detailMetaLabel.setText("");
            completionBar.setMaximum(1);
            completionBar.setValue(0);
            completionBar.setString("No tasks");
            markDoneButton.setEnabled(false);
            deleteTaskButton.setEnabled(false);
            return;
        }

        detailCategoryLabel.setText(selectedTask.category.toUpperCase());
        detailTitleLabel.setText(selectedTask.title);
        detailNotesLabel.setText(htmlText(selectedTask.notes, 320));
        detailMetaLabel.setText((selectedTask.completed ? "Completed / " : "Pending / ")
            + priorityLabel(selectedTask.priority) + " priority");
        completionBar.setMaximum(Math.max(1, currentTasks.size()));
        completionBar.setValue(completedCount());
        completionBar.setString(completedCount() + " / " + currentTasks.size() + " complete");
        markDoneButton.setEnabled(true);
        markDoneButton.setText(selectedTask.completed ? "Mark Pending" : "Mark Complete");
        deleteTaskButton.setEnabled(true);
    }

    private int completedCount() {
        int count = 0;
        for (Task task : currentTasks) {
            if (task.completed) count++;
        }
        return count;
    }

    /* ============================================================ */
    /*  UNDO / REDO LOGIC                                          */
    /* ============================================================ */
    private void saveStateBeforeChange() {
        undoStack.push(new PlannerState(currentTasks, selectedTask));
        redoStack.clear();
        updateHistoryButtons();
    }

    private void performUndo() {
        if (undoStack.isEmpty()) return;

        redoStack.push(new PlannerState(currentTasks, selectedTask));
        PlannerState state = undoStack.pop();
        restoreState(state);
    }

    private void performRedo() {
        if (redoStack.isEmpty()) return;

        undoStack.push(new PlannerState(currentTasks, selectedTask));
        PlannerState state = redoStack.pop();
        restoreState(state);
    }

    private void restoreState(PlannerState state) {
        currentTasks = state.tasks;
        selectedTask = findTaskByTitle(state.selectedTaskTitle);
        if (selectedTask == null && !currentTasks.isEmpty()) {
            selectedTask = currentTasks.get(0);
        }
        editingExisting = false;
        resetEditorFields();
        refreshAll();
    }

    private Task findTaskByTitle(String title) {
        if (title == null) return null;
        for (Task task : currentTasks) {
            if (title.equals(task.title)) return task;
        }
        return null;
    }

    private void updateHistoryButtons() {
        undoBtn.setEnabled(!undoStack.isEmpty());
        redoBtn.setEnabled(!redoStack.isEmpty());
    }

    /* ============================================================ */
    /*  ACTIONS                                                    */
    /* ============================================================ */
    private void saveTask() {
        String title = titleField.getText().trim();
        String category = categoryField.getText().trim();
        String notes = notesArea.getText().trim();
        int priority = comboPriority();

        if (title.isEmpty() || category.isEmpty() || notes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all task fields first.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveStateBeforeChange();

        if (editingExisting && selectedTask != null) {
            selectedTask.title = title;
            selectedTask.category = category;
            selectedTask.notes = notes;
            selectedTask.priority = priority;
        } else {
            Task task = new Task(title, category, notes, priority, false);
            currentTasks.add(task);
            selectedTask = task;
        }

        editingExisting = false;
        resetEditorFields();
        refreshAll();
    }

    private void toggleTaskCompletion(Task task, boolean completed) {
        if (task.completed == completed) return;
        saveStateBeforeChange();
        task.completed = completed;
        selectedTask = task;
        refreshAll();
    }

    private void toggleSelectedTaskCompletion() {
        if (selectedTask == null) return;
        toggleTaskCompletion(selectedTask, !selectedTask.completed);
    }

    private void deleteSelectedTask() {
        if (selectedTask == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
            "Delete \"" + selectedTask.title + "\" from the planner?",
            "Delete Task", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        saveStateBeforeChange();
        int index = currentTasks.indexOf(selectedTask);
        currentTasks.remove(selectedTask);
        selectedTask = currentTasks.isEmpty() ? null : currentTasks.get(Math.max(0, Math.min(index, currentTasks.size() - 1)));
        editingExisting = false;
        resetEditorFields();
        refreshAll();
    }

    private void loadSelectedIntoEditor() {
        if (selectedTask == null) return;
        editingExisting = true;
        titleField.setText(selectedTask.title);
        categoryField.setText(selectedTask.category);
        notesArea.setText(selectedTask.notes);
        priorityCombo.setSelectedIndex(selectedTask.priority - 1);
        updateEditorState();
    }

    private void resetEditor() {
        editingExisting = false;
        resetEditorFields();
        updateEditorState();
    }

    private void resetEditorFields() {
        titleField.setText("");
        categoryField.setText("");
        notesArea.setText("");
        priorityCombo.setSelectedIndex(1);
    }

    private void onSearchChanged() {
        searchQuery = searchField.getText().trim().toLowerCase();
        List<Task> visible = filteredTasks();
        if (selectedTask != null && !visible.contains(selectedTask)) {
            selectedTask = visible.isEmpty() ? (currentTasks.isEmpty() ? null : currentTasks.get(0)) : visible.get(0);
        }
        rebuildTaskList();
        updateDetailPanel();
    }

    private JButton createFilterButton(String text) {
        JButton btn = createOutlineButton(text, "All".equals(text) ? SKY : SLATE);
        btn.addActionListener(e -> {
            filterMode = text;
            rebuildTaskList();
            updateDetailPanel();
        });
        return btn;
    }

    private int comboPriority() {
        int idx = priorityCombo.getSelectedIndex();
        return idx == 0 ? 1 : idx == 1 ? 2 : 3;
    }

    private void updateEditorState() {
        editorStateLabel.setText(editingExisting ? "Editing selected task" : "New task mode");
        if (saveTaskButton != null) {
            saveTaskButton.setText(editingExisting ? "Update Task" : "Add Task");
        }
    }

    private DocumentListener editorListener() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateEditorState(); }
            public void removeUpdate(DocumentEvent e) { updateEditorState(); }
            public void changedUpdate(DocumentEvent e) {}
        };
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        titleField.setBackground(Theme.FIELD_BG);
        categoryField.setBackground(Theme.FIELD_BG);
        searchField.setBackground(Theme.FIELD_BG);
        notesArea.setBackground(Theme.isDark() ? new Color(28, 32, 44) : new Color(250, 251, 253));
        refreshAll();
        repaint();
    }

    /* ============================================================ */
    /*  HELPERS                                                    */
    /* ============================================================ */
    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBackground(Theme.FIELD_BG);
        field.setForeground(Theme.PRIMARY_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private Component spacer(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private JPanel heroStat(String label, JLabel valueLabel) {
        JPanel stat = new JPanel(new BorderLayout(0, 4));
        stat.setOpaque(false);
        stat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1, true),
            new EmptyBorder(12, 12, 12, 12)));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLbl.setForeground(new Color(255, 255, 255, 210));
        stat.add(valueLabel, BorderLayout.CENTER);
        stat.add(labelLbl, BorderLayout.SOUTH);
        return stat;
    }

    private JPanel infoCard(String title, String body, Color accent) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 6));
        card.setAccent(accent);
        card.setBorder(new EmptyBorder(12, 14, 12, 12));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Theme.PRIMARY_TEXT);
        JLabel bodyLbl = new JLabel(htmlText(body, 220));
        bodyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bodyLbl.setForeground(Theme.MUTED_TEXT);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bodyLbl, BorderLayout.CENTER);
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

    private javax.swing.border.Border pillBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            new EmptyBorder(6, 12, 6, 12));
    }

    private String htmlText(String text, int width) {
        return "<html><body style='width:" + width + "px'>" + escapeHtml(text) + "</body></html>";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String truncate(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }

    private String priorityLabel(int priority) {
        return switch (priority) {
            case 1 -> "HIGH";
            case 2 -> "MEDIUM";
            default -> "LOW";
        };
    }

    private Color priorityColor(int priority) {
        return switch (priority) {
            case 1 -> ROSE;
            case 2 -> ORANGE;
            default -> GREEN;
        };
    }

    private JLabel emptyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private Color TEAL() {
        return new Color(20, 184, 166);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        btn.setPreferredSize(new Dimension(110, 38));
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
