package com.talentforge.ui;

import com.talentforge.datastructures.ResumeKeywordBank;
import com.talentforge.datastructures.Trie;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.zip.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Resume Checker — upload a PDF / TXT / DOCX (or paste text), then receive a
 * multi-dimensional ATS score (0-100) broken down into:
 *   40 pts  Keyword match (Trie-based)
 *   20 pts  Section detection (Education, Experience, Projects, Skills, Summary)
 *   15 pts  Action verbs
 *   15 pts  Contact info (email, phone, LinkedIn/GitHub)
 *   10 pts  Resume length (ideal 300–700 words)
 *
 * After analysis the OnScoreUpdate callback fires so MainAppPanel can forward
 * the score to DashboardPanel for a real-time Resume Score card update.
 */
public class ResumeCheckerPanel extends JPanel {

    // ── Callback ─────────────────────────────────────────────────────────────
    public interface OnScoreUpdate {
        void onScoreUpdate(int score);
    }

    // ── Palette (pink accent matching the resume stat card) ──────────────────
    private static final Color ACCENT       = new Color(244, 114, 182);
    private static final Color ACCENT_DARK  = new Color(219,  39, 119);
    private static final Color ACCENT_LIGHT = new Color(253, 242, 248);

    // ── State ─────────────────────────────────────────────────────────────────
    private final ResumeKeywordBank keywordBank = new ResumeKeywordBank();
    private OnScoreUpdate onScoreUpdate;

    private boolean useFileMode = true;
    private File    selectedFile;

    // ── UI refs ───────────────────────────────────────────────────────────────
    private JLabel    fileNameLabel;
    private JTextArea pasteArea;
    private JPanel    fileUploadZone;
    private JPanel    pasteZone;
    private JButton   fileTabBtn;
    private JButton   pasteTabBtn;

    // Results
    private JPanel              resultsPanel;
    private CircularProgressRing scoreRing;
    private JLabel               scoreNumLabel;
    private JLabel               gradeLabel;
    private JPanel               breakdownPanel;
    private JPanel               keywordsPanel;
    private JPanel               tipsPanel;
    private JScrollPane          mainScroll;

    // ── Constructor ───────────────────────────────────────────────────────────
    public ResumeCheckerPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(18));
        content.add(buildModeSelector());
        content.add(Box.createVerticalStrut(12));
        content.add(buildInputArea());
        content.add(Box.createVerticalStrut(12));
        content.add(buildAnalyzeButtonRow());
        content.add(Box.createVerticalStrut(16));

        resultsPanel = buildResultsPanel();
        resultsPanel.setVisible(false);
        content.add(resultsPanel);
        content.add(Box.createVerticalStrut(24));

        mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    public void setOnScoreUpdate(OnScoreUpdate cb) {
        this.onScoreUpdate = cb;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI BUILDING
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Resume Checker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Upload your resume or paste text to receive a full ATS score and improvement tips.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED_TEXT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        p.add(title);
        p.add(sub);
        return p;
    }

    // ── Mode selector tabs ────────────────────────────────────────────────────

    private JPanel buildModeSelector() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        fileTabBtn  = tabBtn("\uD83D\uDCC4  Upload File", true);
        pasteTabBtn = tabBtn("\u270F\uFE0F  Paste Text",  false);
        fileTabBtn .addActionListener(e -> switchMode(true));
        pasteTabBtn.addActionListener(e -> switchMode(false));

        p.add(fileTabBtn);
        p.add(Box.createHorizontalStrut(8));
        p.add(pasteTabBtn);
        return p;
    }

    private JButton tabBtn(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleTab(b, active);
        return b;
    }

    private void styleTab(JButton b, boolean active) {
        if (active) {
            b.setBackground(ACCENT);
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(Theme.MUTED_TEXT);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        }
    }

    private void switchMode(boolean fileMode) {
        useFileMode = fileMode;
        styleTab(fileTabBtn,  fileMode);
        styleTab(pasteTabBtn, !fileMode);
        fileUploadZone.setVisible(fileMode);
        pasteZone.setVisible(!fileMode);
        revalidate();
        repaint();
    }

    // ── Input area (file zone + paste zone stacked, one shown at a time) ─────

    private JPanel buildInputArea() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileUploadZone = buildFileUploadZone();
        pasteZone      = buildPasteZone();
        pasteZone.setVisible(false);

        p.add(fileUploadZone);
        p.add(pasteZone);
        return p;
    }

    private JPanel buildFileUploadZone() {
        JPanel zone = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // soft pink fill
                g2.setColor(ACCENT_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // dashed pink border
                g2.setColor(ACCENT);
                float[] dash = {10f, 6f};
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);
                g2.dispose();
            }
        };
        zone.setOpaque(false);
        zone.setAlignmentX(Component.LEFT_ALIGNMENT);
        zone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        zone.setPreferredSize(new Dimension(10, 210));

        // Inner content column
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("\uD83D\uDCC4", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dropHint = new JLabel("Drag & drop your resume here", SwingConstants.CENTER);
        dropHint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dropHint.setForeground(Theme.PRIMARY_TEXT);
        dropHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        dropHint.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

        JLabel fmts = new JLabel("Supports PDF \u00b7 TXT \u00b7 DOCX", SwingConstants.CENTER);
        fmts.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fmts.setForeground(Theme.MUTED_TEXT);
        fmts.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton browseBtn = new JButton("Browse Files") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        browseBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setOpaque(false);
        browseBtn.setContentAreaFilled(false);
        browseBtn.setBorderPainted(false);
        browseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        browseBtn.setPreferredSize(new Dimension(140, 36));
        browseBtn.setMaximumSize(new Dimension(140, 36));
        browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseBtn.addActionListener(e -> browseFile());

        fileNameLabel = new JLabel("No file selected", SwingConstants.CENTER);
        fileNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileNameLabel.setForeground(Theme.MUTED_TEXT);
        fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileNameLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        inner.add(icon);
        inner.add(dropHint);
        inner.add(fmts);
        inner.add(Box.createVerticalStrut(10));
        inner.add(browseBtn);
        inner.add(fileNameLabel);

        zone.add(inner);

        // Drag-and-drop
        new DropTarget(zone, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) evt.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) pickFile(files.get(0));
                } catch (Exception ex) { /* ignore */ }
            }
        });

        return zone;
    }

    private JPanel buildPasteZone() {
        JPanel zone = new JPanel(new BorderLayout());
        zone.setOpaque(false);
        zone.setAlignmentX(Component.LEFT_ALIGNMENT);
        zone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        zone.setPreferredSize(new Dimension(10, 210));

        pasteArea = new JTextArea();
        pasteArea.setLineWrap(true);
        pasteArea.setWrapStyleWord(true);
        pasteArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pasteArea.setForeground(Theme.MUTED_TEXT);
        pasteArea.setText("Paste your resume text here\u2026");
        pasteArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (pasteArea.getText().equals("Paste your resume text here\u2026")) {
                    pasteArea.setText("");
                    pasteArea.setForeground(Theme.PRIMARY_TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (pasteArea.getText().isBlank()) {
                    pasteArea.setForeground(Theme.MUTED_TEXT);
                    pasteArea.setText("Paste your resume text here\u2026");
                }
            }
        });

        JScrollPane sp = new JScrollPane(pasteArea);
        sp.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1));
        zone.add(sp, BorderLayout.CENTER);
        return zone;
    }

    private JPanel buildAnalyzeButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JButton btn = new JButton("\uD83D\uDD0D  Analyze Resume") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_DARK, getWidth(), getHeight(),
                        new Color(99, 102, 241));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(210, 44));
        btn.addActionListener(e -> analyzeResume());
        p.add(btn);
        return p;
    }

    // ── Results panel (hidden until first analysis) ───────────────────────────

    private JPanel buildResultsPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(Theme.FIELD_BORDER);
        p.add(sep);
        p.add(Box.createVerticalStrut(22));

        // Score overview card
        scoreRing     = new CircularProgressRing(0, ACCENT, 90);
        scoreNumLabel = new JLabel("0");
        gradeLabel    = new JLabel("\u2014");
        p.add(buildScoreCard());
        p.add(Box.createVerticalStrut(22));

        // Score breakdown
        p.add(sectionLabel("\uD83D\uDCCA Score Breakdown"));
        p.add(Box.createVerticalStrut(10));
        breakdownPanel = new JPanel();
        breakdownPanel.setOpaque(false);
        breakdownPanel.setLayout(new BoxLayout(breakdownPanel, BoxLayout.Y_AXIS));
        breakdownPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(breakdownPanel);
        p.add(Box.createVerticalStrut(22));

        // Keyword analysis
        p.add(sectionLabel("\uD83D\uDD11 Keyword Analysis"));
        p.add(Box.createVerticalStrut(10));
        keywordsPanel = new JPanel();
        keywordsPanel.setOpaque(false);
        keywordsPanel.setLayout(new BoxLayout(keywordsPanel, BoxLayout.Y_AXIS));
        keywordsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(keywordsPanel);
        p.add(Box.createVerticalStrut(22));

        // Recommendations
        p.add(sectionLabel("\uD83D\uDCA1 Recommendations"));
        p.add(Box.createVerticalStrut(10));
        tipsPanel = new JPanel();
        tipsPanel.setOpaque(false);
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(tipsPanel);

        return p;
    }

    private JPanel buildScoreCard() {
        ElevatedCard card = new ElevatedCard(null);
        card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setPreferredSize(new Dimension(10, 140));

        JPanel ringWrap = new JPanel(new GridBagLayout());
        ringWrap.setOpaque(false);
        ringWrap.setPreferredSize(new Dimension(130, 110));
        ringWrap.setMaximumSize(new Dimension(130, 110));
        ringWrap.add(scoreRing);

        JPanel textSide = new JPanel();
        textSide.setOpaque(false);
        textSide.setLayout(new BoxLayout(textSide, BoxLayout.Y_AXIS));

        scoreNumLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        scoreNumLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel outOf = new JLabel("out of 100");
        outOf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        outOf.setForeground(Theme.MUTED_TEXT);

        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gradeLabel.setForeground(ACCENT);
        gradeLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        textSide.add(Box.createVerticalGlue());
        textSide.add(scoreNumLabel);
        textSide.add(outOf);
        textSide.add(gradeLabel);
        textSide.add(Box.createVerticalGlue());

        card.add(ringWrap);
        card.add(Box.createHorizontalStrut(16));
        card.add(textSide);
        card.add(Box.createHorizontalGlue());
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(Theme.PRIMARY_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOGIC
    // ═════════════════════════════════════════════════════════════════════════

    private void browseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Your Resume");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Resume files (PDF, TXT, DOCX)", "pdf", "txt", "docx"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pickFile(fc.getSelectedFile());
        }
    }

    private void pickFile(File f) {
        selectedFile = f;
        fileNameLabel.setText("\u2713 " + f.getName());
        fileNameLabel.setForeground(new Color(22, 163, 74));
    }

    private void analyzeResume() {
        String text = getResumeText();
        if (text == null || text.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    useFileMode ? "Please select a resume file first."
                                : "Please paste your resume text first.",
                    "No Content", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Run analysis (fast, no need to background-thread for typical resumes)
        AnalysisResult res = analyze(text);
        showResults(res);

        if (onScoreUpdate != null) {
            onScoreUpdate.onScoreUpdate(res.totalScore);
        }
    }

    // ── File reading ──────────────────────────────────────────────────────────

    private String getResumeText() {
        if (useFileMode) {
            if (selectedFile == null) return null;
            String name = selectedFile.getName().toLowerCase();
            try {
                if (name.endsWith(".txt"))  return Files.readString(selectedFile.toPath());
                if (name.endsWith(".pdf"))  return readPdf(selectedFile);
                if (name.endsWith(".docx")) return readDocx(selectedFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Read Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        } else {
            String t = pasteArea.getText();
            return t.equals("Paste your resume text here\u2026") ? "" : t;
        }
    }

    private String readPdf(File f) throws IOException {
        // Suppress verbose PDFBox logging
        java.util.logging.Logger.getLogger("org.apache.pdfbox")
                .setLevel(java.util.logging.Level.OFF);
        try (PDDocument doc = PDDocument.load(f)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String readDocx(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (ZipFile zip = new ZipFile(f)) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) throw new IOException("Not a valid DOCX file.");
            try (InputStream is = zip.getInputStream(entry)) {
                // Read raw XML and extract <w:t> text nodes via regex.
                // Avoids namespace-awareness issues with the default DocumentBuilderFactory.
                String xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                Matcher wt = Pattern.compile("<w:t(?:[^>]*)>([^<]*)</w:t>").matcher(xml);
                while (wt.find()) {
                    sb.append(wt.group(1)).append(' ');
                }
            }
        }
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ANALYSIS ENGINE
    // ═════════════════════════════════════════════════════════════════════════

    private static class AnalysisResult {
        int totalScore, keywordScore, sectionScore, actionVerbScore, contactScore, lengthScore;
        int keywordsFound, keywordsTotal;
        Map<String, List<String>> foundByCategory  = new LinkedHashMap<>();
        Map<String, List<String>> missingByCategory = new LinkedHashMap<>();
        List<String> missingSections = new ArrayList<>();
        List<String> tips            = new ArrayList<>();
        int wordCount;
    }

    private AnalysisResult analyze(String text) {
        AnalysisResult r   = new AnalysisResult();
        String         lower = text.toLowerCase();

        // ── 1. Keyword match  (max 40) ──────────────────────────────────────
        Trie trie = new Trie();
        Matcher mat = Pattern.compile("[A-Za-z0-9+#.]+").matcher(text);
        while (mat.find()) trie.insert(mat.group());

        Map<String, String[]> cats = keywordBank.getCategorizedKeywords();
        int total = 0, found = 0;
        for (Map.Entry<String, String[]> e : cats.entrySet()) {
            String cat = e.getKey();
            r.foundByCategory .put(cat, new ArrayList<>());
            r.missingByCategory.put(cat, new ArrayList<>());
            for (String kw : e.getValue()) {
                total++;
                boolean present = kw.contains(" ")
                        ? lower.contains(kw.toLowerCase())
                        : trie.contains(kw);
                if (present) { found++; r.foundByCategory.get(cat).add(kw); }
                else           r.missingByCategory.get(cat).add(kw);
            }
        }
        r.keywordsFound  = found;
        r.keywordsTotal  = total;
        r.keywordScore   = (int) Math.round(40.0 * found / Math.max(1, total));

        // ── 2. Section detection  (max 20, 4 pts each) ─────────────────────
        String[][] sectionPatterns = {
            {"education"},
            {"experience|work history|employment"},
            {"project"},
            {"skill"},
            {"summary|objective|profile|about me"}
        };
        String[] sectionNames = { "Education", "Experience", "Projects", "Skills", "Summary/Objective" };
        int sScore = 0;
        for (int i = 0; i < sectionPatterns.length; i++) {
            if (Pattern.compile(sectionPatterns[i][0]).matcher(lower).find()) sScore += 4;
            else r.missingSections.add(sectionNames[i]);
        }
        r.sectionScore = sScore;

        // ── 3. Action verbs  (max 15) ───────────────────────────────────────
        String[] verbs = {
            "developed","implemented","designed","built","created","led","managed",
            "optimized","improved","reduced","increased","delivered","collaborated",
            "deployed","automated","analyzed","achieved","architected","engineered",
            "launched","spearheaded","coordinated"
        };
        int vc = 0;
        for (String v : verbs) if (lower.contains(v)) vc++;
        r.actionVerbScore = Math.min(15, (int) Math.round(vc * 15.0 / verbs.length * 2));

        // ── 4. Contact info  (max 15) ───────────────────────────────────────
        int cScore = 0;
        if (Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}").matcher(lower).find()) cScore += 5;
        if (Pattern.compile("\\+?[\\d][\\d\\-\\s]{8,}\\d").matcher(lower).find()) cScore += 5;
        if (Pattern.compile("linkedin\\.com|github\\.com|gitlab\\.com").matcher(lower).find()) cScore += 5;
        r.contactScore = cScore;

        // ── 5. Resume length  (max 10) ──────────────────────────────────────
        r.wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        if      (r.wordCount >= 300 && r.wordCount <= 700) r.lengthScore = 10;
        else if ((r.wordCount >= 200) || (r.wordCount <= 1000)) r.lengthScore = 7;
        else if  (r.wordCount >= 100) r.lengthScore = 4;
        else                          r.lengthScore = 0;

        r.totalScore = r.keywordScore + r.sectionScore + r.actionVerbScore
                     + r.contactScore + r.lengthScore;

        // ── Tips ────────────────────────────────────────────────────────────
        if (r.keywordScore < 20)
            r.tips.add("Add more technical keywords for your target role ("
                + found + " of " + total + " found).");
        if (!r.missingSections.isEmpty())
            r.tips.add("Add missing resume sections: <b>"
                + String.join(", ", r.missingSections) + "</b>.");
        if (r.actionVerbScore < 8)
            r.tips.add("Use stronger action verbs: <i>developed, implemented, led, optimized\u2026</i>");
        if (r.contactScore < 10)
            r.tips.add("Include your email address, phone number, and a LinkedIn or GitHub link.");
        if (r.wordCount < 200)
            r.tips.add("Resume is very short (" + r.wordCount + " words). Aim for 300\u2013700 words.");
        else if (r.wordCount > 1000)
            r.tips.add("Resume is long (" + r.wordCount + " words). Try to keep it under 700 words.");
        if (r.tips.isEmpty())
            r.tips.add("Great resume! Keep it tailored to each role and updated regularly.");

        return r;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RESULTS RENDERING
    // ═════════════════════════════════════════════════════════════════════════

    private void showResults(AnalysisResult res) {
        // Score ring + labels
        scoreRing.setPercent(res.totalScore);
        scoreNumLabel.setText(String.valueOf(res.totalScore));

        String grade; Color gc;
        if      (res.totalScore >= 85) { grade = "\u2B50 Excellent";                gc = new Color(22, 163, 74);  }
        else if (res.totalScore >= 70) { grade = "\u2705 Good";                     gc = new Color(37, 99, 235);  }
        else if (res.totalScore >= 50) { grade = "\u26A0\uFE0F Fair \u2014 Needs Improvement"; gc = new Color(202, 138, 4); }
        else                           { grade = "\u274C Needs Major Work";          gc = new Color(220, 38, 38);  }
        gradeLabel.setText(grade);
        gradeLabel.setForeground(gc);

        // Breakdown rows
        breakdownPanel.removeAll();
        breakdownPanel.add(breakdownRow("\uD83D\uDD11 Keyword Match",     res.keywordScore,    40, new Color(99, 102, 241)));
        breakdownPanel.add(Box.createVerticalStrut(8));
        breakdownPanel.add(breakdownRow("\uD83D\uDCCB Sections Detected", res.sectionScore,    20, new Color(52, 211, 153)));
        breakdownPanel.add(Box.createVerticalStrut(8));
        breakdownPanel.add(breakdownRow("\uD83D\uDCAA Action Verbs",      res.actionVerbScore, 15, new Color(56, 189, 248)));
        breakdownPanel.add(Box.createVerticalStrut(8));
        breakdownPanel.add(breakdownRow("\uD83D\uDCDE Contact Info",      res.contactScore,    15, ACCENT));
        breakdownPanel.add(Box.createVerticalStrut(8));
        breakdownPanel.add(breakdownRow("\uD83D\uDCCF Resume Length",     res.lengthScore,     10, new Color(251, 191, 36)));

        // Keyword cards
        keywordsPanel.removeAll();
        JLabel kwSummary = new JLabel(
                res.keywordsFound + " of " + res.keywordsTotal + " technical keywords found in your resume");
        kwSummary.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        kwSummary.setForeground(Theme.MUTED_TEXT);
        kwSummary.setAlignmentX(Component.LEFT_ALIGNMENT);
        kwSummary.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        keywordsPanel.add(kwSummary);

        for (String cat : res.foundByCategory.keySet()) {
            keywordsPanel.add(keywordCard(cat,
                    res.foundByCategory.get(cat), res.missingByCategory.get(cat)));
            keywordsPanel.add(Box.createVerticalStrut(8));
        }

        // Tips
        tipsPanel.removeAll();
        for (String tip : res.tips) {
            tipsPanel.add(tipRow(tip));
            tipsPanel.add(Box.createVerticalStrut(8));
        }

        resultsPanel.setVisible(true);
        resultsPanel.revalidate();
        resultsPanel.repaint();
        revalidate();
        repaint();

        // Scroll to reveal results
        SwingUtilities.invokeLater(() -> {
            JScrollBar vsb = mainScroll.getVerticalScrollBar();
            vsb.setValue(vsb.getMaximum());
        });
    }

    /** A card row with a label, score/max badge, and a custom progress bar. */
    private JPanel breakdownRow(String label, int score, int max, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setOpaque(true);
        card.setBackground(Theme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        card.setPreferredSize(new Dimension(10, 58));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Theme.PRIMARY_TEXT);

        JLabel scoreLbl = new JLabel(score + " / " + max);
        scoreLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        scoreLbl.setForeground(color);

        // Custom painted progress bar
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(229, 231, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int w = max == 0 ? 0 : (int)(getWidth() * (double) score / max);
                if (w > 0) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, 0, w, getHeight(), 6, 6);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(10, 9));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl, BorderLayout.WEST);
        top.add(scoreLbl, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    /** A card listing found (green) and missing (red) keywords for one category. */
    private JPanel keywordCard(String category, List<String> found, List<String> missing) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Theme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel catLbl = new JLabel(category
                + "  \u2014  " + found.size() + " / " + (found.size() + missing.size()) + " found");
        catLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        catLbl.setForeground(Theme.PRIMARY_TEXT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chips.setOpaque(false);
        for (String kw : found)   chips.add(chip(kw, new Color(220, 252, 231), new Color(22, 163, 74)));
        for (String kw : missing) chips.add(chip(kw, new Color(254, 226, 226), new Color(220, 38, 38)));

        inner.add(catLbl);
        inner.add(chips);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /** A recommendation row with a pink arrow and HTML-formatted tip text. */
    private JPanel tipRow(String tip) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel arrow = new JLabel("\u2192");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 15));
        arrow.setForeground(ACCENT);
        arrow.setVerticalAlignment(SwingConstants.TOP);

        JLabel text = new JLabel("<html><body style='width:600px'>" + tip + "</body></html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(Theme.PRIMARY_TEXT);

        row.add(arrow, BorderLayout.WEST);
        row.add(text,  BorderLayout.CENTER);
        return row;
    }

    private JLabel chip(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return l;
    }
}