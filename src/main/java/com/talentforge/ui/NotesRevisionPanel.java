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
import java.util.LinkedList;
import java.util.List;

/**
 * Notes & Revision Module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>LinkedList&lt;Note&gt;</b> - Ordered revision queue. Since revision schedules
 *       are sequence-driven, a LinkedList represents this timeline and lets revised
 *       items cycle from the front to the back efficiently.</li>
 * </ul>
 */
public class NotesRevisionPanel extends JPanel {

    private static class Note {
        String title;
        String category;
        String content;
        int revisionCount = 0;

        Note(String title, String category, String content) {
            this.title = title;
            this.category = category;
            this.content = content;
        }
    }

    private static final Color SKY = new Color(56, 189, 248);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color ROSE = new Color(244, 63, 94);
    private static final Color SLATE = new Color(100, 116, 139);

    /* ============================================================ */
    /*  DATA STRUCTURES                                            */
    /* ============================================================ */
    private final LinkedList<Note> noteQueue = new LinkedList<>();

    /* ============================================================ */
    /*  UI STATE                                                   */
    /* ============================================================ */
    private final JPanel queuePanel = new JPanel();
    private final JTextField titleField = new JTextField();
    private final JTextField catField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JTextArea contentArea = new JTextArea(7, 24);
    private final JLabel statusLabel = new JLabel();
    private final JLabel queueCountLabel = new JLabel();
    private final JLabel revisionCountLabel = new JLabel();
    private final JLabel currentTargetLabel = new JLabel();
    private final JLabel detailCategoryLabel = new JLabel();
    private final JLabel detailTitleLabel = new JLabel();
    private final JLabel detailBodyLabel = new JLabel();
    private final JLabel detailMetaLabel = new JLabel();
    private final JLabel editorStateLabel = new JLabel();
    private final JProgressBar revisionProgressBar = new JProgressBar();
    private JButton addOrUpdateButton;
    private JButton reviseButton;
    private JButton deleteButton;

    private Note selectedNote;
    private String searchQuery = "";
    private boolean editingExisting = false;

    public NotesRevisionPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        seedNotes();
        buildUI();
        selectedNote = noteQueue.isEmpty() ? null : noteQueue.getFirst();
        refreshAll();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    private void seedNotes() {
        noteQueue.add(new Note("Kadane's Algorithm", "DSA",
            "Finds the maximum contiguous subarray sum in O(n). Track a running best and a global best."));
        noteQueue.add(new Note("Database Normalization", "DBMS",
            "Organize tables to reduce redundancy. Revise 1NF, 2NF, 3NF, and when BCNF matters."));
        noteQueue.add(new Note("STAR Interview Method", "Behavioral",
            "Situation, Task, Action, Result. Use it to keep answers structured and outcome-focused."));
        noteQueue.add(new Note("TCP vs UDP", "Networking",
            "TCP is connection-oriented and reliable. UDP is connectionless, lower-latency, and best when occasional loss is acceptable."));
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
                    new Color(SKY.getRed(), SKY.getGreen(), SKY.getBlue(), Theme.isDark() ? 70 : 38),
                    getWidth(), 0,
                    new Color(TEAL.getRed(), TEAL.getGreen(), TEAL.getBlue(), Theme.isDark() ? 46 : 24)));
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
        JLabel title = new JLabel("Notes & Revision Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        JLabel sub = new JLabel("Capture concise study notes, then cycle them through a revision queue with a clearer daily workflow.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(makePillLabel("Queue " + noteQueue.size(), SKY, false));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(pillBorder(TEAL));
        right.add(statusLabel);

        bar.add(left, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildHeroPanel() {
        GradientCard hero = new GradientCard(new BorderLayout(18, 0), SKY, TEAL);
        hero.setBorder(new EmptyBorder(24, 24, 24, 24));
        hero.setPreferredSize(new Dimension(0, 140));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Turn scattered notes into a usable revision loop");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Write small, high-signal notes, keep the next revision visible, and push completed topics to the back of the queue.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 220));

        currentTargetLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        currentTargetLabel.setForeground(Color.WHITE);

        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(sub);
        copy.add(Box.createVerticalStrut(12));
        copy.add(currentTargetLabel);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(360, 0));
        stats.add(heroStat("In Queue", queueCountLabel));
        stats.add(heroStat("Total Revisions", revisionCountLabel));
        stats.add(heroStat("Current Focus", new JLabel("Daily loop")));

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
        header.add(sectionLabel("Compose Note"), BorderLayout.WEST);
        editorStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        editorStateLabel.setForeground(Theme.MUTED_TEXT);
        header.add(editorStateLabel, BorderLayout.EAST);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(fieldLabel("Topic title"));
        form.add(spacer(4));
        styleField(titleField, "Example: Binary Search");
        form.add(titleField);

        form.add(spacer(12));
        form.add(fieldLabel("Category"));
        form.add(spacer(4));
        styleField(catField, "Example: DSA, DBMS, OS");
        form.add(catField);

        form.add(spacer(12));
        form.add(fieldLabel("Revision note"));
        form.add(spacer(4));
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(Theme.isDark() ? new Color(28, 32, 44) : new Color(250, 251, 253));
        contentArea.setForeground(Theme.PRIMARY_TEXT);
        contentArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        contentScroll.setPreferredSize(new Dimension(0, 180));

        titleField.getDocument().addDocumentListener(editorListener());
        catField.getDocument().addDocumentListener(editorListener());
        contentArea.getDocument().addDocumentListener(editorListener());

        JPanel tips = new JPanel(new GridLayout(2, 1, 0, 10));
        tips.setOpaque(false);
        tips.add(infoCard("Make it brief", "A strong revision note is a compressed memory trigger, not a chapter rewrite.", TEAL));
        tips.add(infoCard("Write recall hooks", "Prefer formulas, comparisons, edge cases, and one example you can reconstruct quickly.", ORANGE));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton resetBtn = createOutlineButton("Reset", SLATE);
        resetBtn.addActionListener(e -> resetEditor());
        addOrUpdateButton = createGradientButton("Add to Queue", 150, 40);
        addOrUpdateButton.addActionListener(e -> saveNote());
        actions.add(resetBtn);
        actions.add(addOrUpdateButton);

        form.add(contentScroll);
        form.add(spacer(14));
        form.add(tips);

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
        header.add(sectionLabel("Revision Queue"), BorderLayout.WEST);
        header.add(makeTinyLabel("Search notes by title or category"), BorderLayout.EAST);

        styleField(searchField, "Search notes...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
        queuePanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(queuePanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDetailPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Revision Detail"), BorderLayout.WEST);
        header.add(makeTinyLabel("Study the front item first"), BorderLayout.EAST);

        detailCategoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        detailCategoryLabel.setForeground(TEAL);
        detailTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        detailTitleLabel.setForeground(Theme.PRIMARY_TEXT);
        detailBodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailBodyLabel.setForeground(Theme.MUTED_TEXT);
        detailMetaLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        detailMetaLabel.setForeground(SLATE);

        JPanel detailCopy = new JPanel();
        detailCopy.setOpaque(false);
        detailCopy.setLayout(new BoxLayout(detailCopy, BoxLayout.Y_AXIS));
        detailCopy.add(detailCategoryLabel);
        detailCopy.add(spacer(6));
        detailCopy.add(detailTitleLabel);
        detailCopy.add(spacer(10));
        detailCopy.add(detailBodyLabel);
        detailCopy.add(spacer(12));
        detailCopy.add(detailMetaLabel);

        revisionProgressBar.setBorderPainted(false);
        revisionProgressBar.setStringPainted(true);
        revisionProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        revisionProgressBar.setForeground(TEAL);
        revisionProgressBar.setBackground(Theme.isDark() ? new Color(45, 45, 60) : new Color(229, 231, 235));

        reviseButton = createGradientButton("Mark Revised", 150, 40);
        reviseButton.addActionListener(e -> reviseSelectedNote());
        deleteButton = createOutlineButton("Delete", ROSE);
        deleteButton.addActionListener(e -> deleteSelectedNote());
        JButton editButton = createOutlineButton("Edit in Composer", SKY);
        editButton.addActionListener(e -> loadSelectedIntoEditor());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(reviseButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(detailCopy, BorderLayout.CENTER);
        panel.add(revisionProgressBar, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(actions, BorderLayout.SOUTH);
        return wrapper;
    }

    /* ============================================================ */
    /*  ACTIONS                                                    */
    /* ============================================================ */
    private void saveNote() {
        String title = titleField.getText().trim();
        String category = catField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || category.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields first.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (editingExisting && selectedNote != null) {
            selectedNote.title = title;
            selectedNote.category = category;
            selectedNote.content = content;
        } else {
            Note newNote = new Note(title, category, content);
            noteQueue.addLast(newNote);
            selectedNote = newNote;
        }

        editingExisting = false;
        resetEditorFields();
        refreshAll();
    }

    private void reviseSelectedNote() {
        if (selectedNote == null || noteQueue.isEmpty()) return;

        if (selectedNote != noteQueue.getFirst()) {
            JOptionPane.showMessageDialog(this,
                "Only the note at the front of the revision queue can be marked revised.",
                "Queue Order", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Note note = noteQueue.removeFirst();
        note.revisionCount++;
        noteQueue.addLast(note);
        selectedNote = noteQueue.isEmpty() ? null : noteQueue.getFirst();
        refreshAll();
    }

    private void deleteSelectedNote() {
        if (selectedNote == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
            "Delete \"" + selectedNote.title + "\" from the revision queue?",
            "Delete Note", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        boolean deletedFront = !noteQueue.isEmpty() && selectedNote == noteQueue.getFirst();
        noteQueue.remove(selectedNote);
        selectedNote = noteQueue.isEmpty() ? null : deletedFront ? noteQueue.getFirst() : nearestVisibleSelection();
        editingExisting = false;
        resetEditorFields();
        refreshAll();
    }

    private Note nearestVisibleSelection() {
        List<Note> visible = filteredNotes();
        return visible.isEmpty() ? (noteQueue.isEmpty() ? null : noteQueue.getFirst()) : visible.get(0);
    }

    private void loadSelectedIntoEditor() {
        if (selectedNote == null) return;
        editingExisting = true;
        titleField.setText(selectedNote.title);
        catField.setText(selectedNote.category);
        contentArea.setText(selectedNote.content);
        updateEditorState();
    }

    private void resetEditor() {
        editingExisting = false;
        resetEditorFields();
        updateEditorState();
    }

    private void resetEditorFields() {
        titleField.setText("");
        catField.setText("");
        contentArea.setText("");
    }

    private void onSearchChanged() {
        searchQuery = searchField.getText().trim().toLowerCase();
        List<Note> visible = filteredNotes();
        if (selectedNote != null && !visible.contains(selectedNote)) {
            selectedNote = visible.isEmpty() ? (noteQueue.isEmpty() ? null : noteQueue.getFirst()) : visible.get(0);
        }
        rebuildQueueList();
        updateDetailPanel();
    }

    /* ============================================================ */
    /*  REFRESH                                                    */
    /* ============================================================ */
    private void refreshAll() {
        if (selectedNote == null && !noteQueue.isEmpty()) {
            selectedNote = noteQueue.getFirst();
        }
        rebuildQueueList();
        updateHeaderStats();
        updateDetailPanel();
        updateEditorState();
    }

    private void rebuildQueueList() {
        queuePanel.removeAll();

        List<Note> visible = filteredNotes();
        if (visible.isEmpty()) {
            queuePanel.add(emptyLabel(noteQueue.isEmpty()
                ? "No notes in the queue yet. Add one from the composer."
                : "No notes match this search."));
        } else {
            for (int i = 0; i < visible.size(); i++) {
                Note note = visible.get(i);
                queuePanel.add(buildQueueCard(note, note == selectedNote, note == noteQueue.peekFirst()));
                queuePanel.add(spacer(10));
            }
        }

        queuePanel.revalidate();
        queuePanel.repaint();
    }

    private List<Note> filteredNotes() {
        if (searchQuery.isBlank()) return new ArrayList<>(noteQueue);

        List<Note> visible = new ArrayList<>();
        for (Note note : noteQueue) {
            String haystack = (note.title + " " + note.category + " " + note.content).toLowerCase();
            if (haystack.contains(searchQuery)) {
                visible.add(note);
            }
        }
        return visible;
    }

    private JComponent buildQueueCard(Note note, boolean selected, boolean isFront) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setAccent(isFront ? TEAL : selected ? SKY : null);
        card.setBorder(new EmptyBorder(14, 16, 14, 14));

        JLabel meta = new JLabel((isFront ? "NEXT / " : "") + note.category.toUpperCase());
        meta.setFont(new Font("Segoe UI", Font.BOLD, 10));
        meta.setForeground(isFront ? TEAL : SKY);

        JLabel title = new JLabel(note.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel body = new JLabel(htmlText(truncate(note.content, 120), 240));
        body.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        body.setForeground(Theme.MUTED_TEXT);

        JLabel count = new JLabel("Revised " + note.revisionCount + " times");
        count.setFont(new Font("Segoe UI", Font.BOLD, 11));
        count.setForeground(SLATE);

        card.add(meta, BorderLayout.NORTH);
        card.add(title, BorderLayout.CENTER);
        card.add(body, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.add(count, BorderLayout.SOUTH);
        wrap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wrap.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedNote = note;
                refreshAll();
            }
        });
        return wrap;
    }

    private void updateHeaderStats() {
        int totalRevisions = 0;
        for (Note note : noteQueue) totalRevisions += note.revisionCount;

        queueCountLabel.setText(String.valueOf(noteQueue.size()));
        revisionCountLabel.setText(String.valueOf(totalRevisions));
        currentTargetLabel.setText(noteQueue.isEmpty()
            ? "Queue is empty right now."
            : "Current target: " + noteQueue.getFirst().title + " / " + noteQueue.getFirst().category);

        statusLabel.setText(noteQueue.isEmpty() ? "Queue Empty" : "Ready to Revise");
    }

    private void updateDetailPanel() {
        if (selectedNote == null) {
            detailCategoryLabel.setText("NO NOTE SELECTED");
            detailTitleLabel.setText("Build a note to start the cycle");
            detailBodyLabel.setText(htmlText("The detail panel will show the note body, revision metadata, and whether the current note is eligible to be marked revised.", 320));
            detailMetaLabel.setText("");
            revisionProgressBar.setMaximum(1);
            revisionProgressBar.setValue(0);
            revisionProgressBar.setString("No queue");
            reviseButton.setEnabled(false);
            deleteButton.setEnabled(false);
            return;
        }

        detailCategoryLabel.setText(selectedNote.category.toUpperCase());
        detailTitleLabel.setText(selectedNote.title);
        detailBodyLabel.setText(htmlText(selectedNote.content, 320));
        detailMetaLabel.setText(selectedNote == noteQueue.peekFirst()
            ? "This note is at the front of the queue and can be revised now."
            : "This note is in the queue, but not at the front yet.");

        int maxRevisions = Math.max(1, maxRevisionCount());
        revisionProgressBar.setMaximum(maxRevisions);
        revisionProgressBar.setValue(Math.min(selectedNote.revisionCount, maxRevisions));
        revisionProgressBar.setString("Revision count " + selectedNote.revisionCount);
        reviseButton.setEnabled(selectedNote == noteQueue.peekFirst());
        deleteButton.setEnabled(true);
    }

    private int maxRevisionCount() {
        int max = 0;
        for (Note note : noteQueue) max = Math.max(max, note.revisionCount);
        return max;
    }

    private void updateEditorState() {
        editorStateLabel.setText(editingExisting
            ? "Editing selected note"
            : "New note mode");
        addOrUpdateButton.setText(editingExisting ? "Update Note" : "Add to Queue");
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
        catField.setBackground(Theme.FIELD_BG);
        searchField.setBackground(Theme.FIELD_BG);
        contentArea.setBackground(Theme.isDark() ? new Color(28, 32, 44) : new Color(250, 251, 253));
        refreshAll();
        repaint();
    }

    /* ============================================================ */
    /*  VISUAL HELPERS                                             */
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

    private JLabel makePillLabel(String text, Color color, boolean filled) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(filled ? Color.WHITE : color);
        label.setOpaque(true);
        label.setBackground(filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 34 : 16));
        label.setBorder(pillBorder(color));
        return label;
    }

    private EmptyBorder pillPadding() {
        return new EmptyBorder(6, 12, 6, 12);
    }

    private javax.swing.border.Border pillBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            pillPadding());
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
        btn.setPreferredSize(new Dimension(120, 38));
        return btn;
    }

    private JLabel emptyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private String truncate(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }

    private String htmlText(String text, int width) {
        return "<html><body style='width:" + width + "px'>" + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</body></html>";
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
