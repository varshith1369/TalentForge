package com.talentforge.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;

/**
 * Notes & Revision Module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>LinkedList&lt;Note&gt;</b> — Ordered revision queue. Since revision schedules
 *       are sequence-driven (e.g. review oldest notes first, or cycle them through a queue),
 *       a LinkedList represents this ordered timeline where completed items are cycled.</li>
 * </ul>
 */
public class NotesRevisionPanel extends JPanel {

    private static class Note {
        final String title;
        final String category;
        final String content;
        int revisionCount = 0;

        Note(String title, String category, String content) {
            this.title = title;
            this.category = category;
            this.content = content;
        }
    }

    /* ============================================================ */
    /*  DATA STRUCTURES                                             */
    /* ============================================================ */
    private final LinkedList<Note> noteQueue = new LinkedList<>();

    /* ============================================================ */
    /*  UI REFERENCES                                               */
    /* ============================================================ */
    private final JPanel queuePanel = new JPanel();
    private final JTextField titleField = new JTextField();
    private final JTextField catField = new JTextField();
    private final JTextArea contentArea = new JTextArea(4, 20);

    public NotesRevisionPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        seedNotes();
        buildUI();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    private void seedNotes() {
        noteQueue.add(new Note("Kadane's Algorithm", "DSA", "Finds maximum contiguous subarray sum in O(N). Store current max and global max."));
        noteQueue.add(new Note("Database Normalization", "DBMS", "Process of organizing tables to minimize redundancy. 1NF, 2NF, 3NF, BCNF."));
        noteQueue.add(new Note("STAR Interview Method", "Behavioral", "Situation, Task, Action, Result. Crucial framework for structured HR answers."));
        noteQueue.add(new Note("TCP vs UDP", "Networking", "TCP is connection-oriented, reliable, slower. UDP is connectionless, fast, unreliable."));
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

        JLabel title = new JLabel("📚 Notes & Revision");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitle = new JLabel("Create notes and review them sequentially. Completing a revision cycles the note to the end of the queue.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        header.add(left, BorderLayout.CENTER);

        // Main split pane: left (create note form), right (scrollable revision queue)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left form
        gbc.gridx = 0; gbc.weightx = 0.4;
        contentPanel.add(buildFormPanel(), gbc);

        // Spacer
        gbc.gridx = 1; gbc.weightx = 0.05;
        contentPanel.add(Box.createHorizontalStrut(20), gbc);

        // Right list
        gbc.gridx = 2; gbc.weightx = 0.55;
        contentPanel.add(buildQueueSection(), gbc);

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.PANEL_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel sectionTitle = new JLabel("📝 Create Revision Note");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionTitle.setForeground(Theme.PRIMARY_TEXT);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tLbl = new JLabel("Topic Title:");
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLbl.setForeground(Theme.MUTED_TEXT);
        tLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        tLbl.setBorder(new EmptyBorder(12, 0, 4, 0));

        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cLbl = new JLabel("Category (e.g., DSA, OS, HR):");
        cLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cLbl.setForeground(Theme.MUTED_TEXT);
        cLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        cLbl.setBorder(new EmptyBorder(12, 0, 4, 0));

        catField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        catField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        catField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel("Notes & Summary:");
        descLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        descLbl.setForeground(Theme.MUTED_TEXT);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLbl.setBorder(new EmptyBorder(12, 0, 4, 0));

        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));
        JScrollPane descScroll = new JScrollPane(contentArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setBorder(null);

        JButton addBtn = createGradientButton("Add to Queue", 160, 36);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.addActionListener(e -> addNote());

        form.add(sectionTitle);
        form.add(tLbl);
        form.add(titleField);
        form.add(cLbl);
        form.add(catField);
        form.add(descLbl);
        form.add(descScroll);
        form.add(Box.createVerticalStrut(16));
        form.add(addBtn);

        return form;
    }

    private JPanel buildQueueSection() {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);

        JLabel listTitle = new JLabel("📋 Revision Queue");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        listTitle.setForeground(Theme.PRIMARY_TEXT);

        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
        queuePanel.setOpaque(false);

        rebuildQueueList();

        JScrollPane scroll = new JScrollPane(queuePanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        section.add(listTitle, BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);

        return section;
    }

    private void rebuildQueueList() {
        queuePanel.removeAll();

        if (noteQueue.isEmpty()) {
            JLabel empty = new JLabel("No notes to revise. Create one on the left!");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Theme.MUTED_TEXT);
            queuePanel.add(empty);
        } else {
            for (int i = 0; i < noteQueue.size(); i++) {
                queuePanel.add(buildNoteCard(noteQueue.get(i), i == 0));
                queuePanel.add(Box.createVerticalStrut(8));
            }
        }

        queuePanel.revalidate();
        queuePanel.repaint();
    }

    private ElevatedCard buildNoteCard(Note note, boolean isFront) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        if (isFront) {
            // Highlight the next note to revise
            card.setAccentColor(Theme.PRIMARY_START);
        }

        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel textSide = new JPanel();
        textSide.setLayout(new BoxLayout(textSide, BoxLayout.Y_AXIS));
        textSide.setOpaque(false);

        JLabel cat = new JLabel(note.category.toUpperCase());
        cat.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cat.setForeground(Theme.PRIMARY_END);

        JLabel title = new JLabel(note.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Theme.PRIMARY_TEXT);

        JTextArea body = new JTextArea(note.content);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        body.setForeground(Theme.MUTED_TEXT);
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel count = new JLabel("Revised: " + note.revisionCount + " times");
        count.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        count.setForeground(Theme.MUTED_TEXT);
        count.setBorder(new EmptyBorder(6, 0, 0, 0));

        textSide.add(cat);
        textSide.add(title);
        textSide.add(body);
        textSide.add(count);

        JPanel btnSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnSide.setOpaque(false);

        if (isFront) {
            JButton doneBtn = createSmallButton("Mark Revised ✓", new Color(34, 197, 94));
            doneBtn.addActionListener(e -> cycleNote(note));
            btnSide.add(doneBtn);
        }

        JButton delBtn = createSmallButton("✕", Theme.MUTED_TEXT);
        delBtn.addActionListener(e -> deleteNote(note));
        btnSide.add(delBtn);

        inner.add(textSide, BorderLayout.CENTER);
        inner.add(btnSide, BorderLayout.EAST);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /* ============================================================ */
    /*  ACTIONS                                                     */
    /* ============================================================ */
    private void addNote() {
        String title = titleField.getText().trim();
        String category = catField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || category.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields first!", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Note newNote = new Note(title, category, content);
        noteQueue.add(newNote); // Add to end of LinkedList queue

        titleField.setText("");
        catField.setText("");
        contentArea.setText("");

        rebuildQueueList();
    }

    private void cycleNote(Note note) {
        // Remove from front and add to the back of the LinkedList queue
        noteQueue.remove(note);
        note.revisionCount++;
        noteQueue.addLast(note);

        rebuildQueueList();
    }

    private void deleteNote(Note note) {
        noteQueue.remove(note);
        rebuildQueueList();
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        rebuildQueueList();
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
