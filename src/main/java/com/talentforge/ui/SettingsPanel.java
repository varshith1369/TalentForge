package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.prefs.Preferences;

/**
 * Settings Panel for app appearance, learning goals, reminders, and data preferences.
 */
public class SettingsPanel extends JPanel {

    private static final Preferences PREFS = Preferences.userNodeForPackage(SettingsPanel.class);

    private static final String KEY_DAILY_MINUTES = "settings.dailyMinutes";
    private static final String KEY_WEEKLY_CODING_TARGET = "settings.weeklyCodingTarget";
    private static final String KEY_INTERVIEW_TARGET = "settings.interviewTarget";
    private static final String KEY_TRACK = "settings.track";
    private static final String KEY_REMINDERS = "settings.reminders";
    private static final String KEY_REMINDER_TIME = "settings.reminderTime";
    private static final String KEY_WEEKLY_SUMMARY = "settings.weeklySummary";
    private static final String KEY_STREAK_RESCUE = "settings.streakRescue";
    private static final String KEY_CHANNEL = "settings.channel";
    private static final String KEY_COMPACT_MODE = "settings.compactMode";
    private static final String KEY_REDUCE_MOTION = "settings.reduceMotion";
    private static final String KEY_FONT_SCALE = "settings.fontScale";
    private static final String KEY_AUTO_SAVE = "settings.autoSave";
    private static final String KEY_CONFIRM_ACTIONS = "settings.confirmActions";
    private static final String KEY_SHOW_DEMO_RANKS = "settings.showDemoRanks";

    private static final Color SKY = new Color(14, 165, 233);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color ROSE = new Color(244, 63, 94);
    private static final Color VIOLET = new Color(124, 58, 237);

    private final JSlider offerSlider = new JSlider(1, 10, 1);
    private final JLabel offerValueLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Preferences are saved automatically.");

    private boolean rebuilding;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        buildUI();
        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    private void buildUI() {
        rebuilding = true;
        removeAll();
        setBackground(Theme.PAGE_BG);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(18));
        content.add(buildStatsStrip());
        content.add(Box.createVerticalStrut(18));

        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        mainGrid.setOpaque(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.add(buildPlacementCard());
        leftColumn.add(Box.createVerticalStrut(16));
        leftColumn.add(buildDisplayCard());
        leftColumn.add(Box.createVerticalStrut(16));
        leftColumn.add(buildDataCard());

        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.add(buildLearningCard());
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(buildReminderCard());
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(buildAboutCard());

        mainGrid.add(leftColumn);
        mainGrid.add(rightColumn);
        content.add(mainGrid);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(scrollPane, BorderLayout.CENTER);
        rebuilding = false;
        revalidate();
        repaint();
    }

    private JComponent buildHeader() {
        GradientPanel header = new GradientPanel(new BorderLayout(24, 0), new Color(37, 99, 235), new Color(20, 184, 166));
        header.setBorder(new EmptyBorder(22, 24, 22, 24));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 164));
        header.setPreferredSize(new Dimension(0, 164));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("TalentForge control center");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eyebrow.setForeground(new Color(220, 250, 245));

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("<html>Personalize your practice flow, reminders, display mode, and placement goals from one polished workspace.</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(232, 247, 255));

        text.add(eyebrow);
        text.add(Box.createVerticalStrut(8));
        text.add(title);
        text.add(Box.createVerticalStrut(8));
        text.add(subtitle);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton resetButton = glassButton("Reset local options");
        resetButton.setToolTipText("Reset display, learning, reminder, and data preferences.");
        resetButton.addActionListener(e -> resetLocalPreferences());
        resetButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        actions.add(statusLabel);
        actions.add(Box.createVerticalStrut(14));
        actions.add(resetButton);

        header.add(text, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent buildStatsStrip() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));

        int overallScore = UserProfileCache.getProblemsSolved() * 2
                + UserProfileCache.getAptitudeScore()
                + UserProfileCache.getMockInterviews() * 10
                + UserProfileCache.getResumeScore();

        grid.add(metricCard("Current score", overallScore + " pts", "Leaderboard formula", SKY));
        grid.add(metricCard("Offer target", UserProfileCache.getOffersTarget() + " offers", "Placement goal", TEAL));
        grid.add(metricCard("Daily focus", PREFS.getInt(KEY_DAILY_MINUTES, 60) + " min", "Preferred study time", AMBER));
        grid.add(metricCard("Reminder", PREFS.get(KEY_REMINDER_TIME, "07:30 PM"), "Daily check-in", ROSE));
        return grid;
    }

    private JComponent buildPlacementCard() {
        JPanel card = surfaceCard("Placement Goals", "Tune the targets shown across your dashboard and analytics.");
        JPanel body = verticalPanel();

        int savedOffers = Math.max(1, Math.min(10, UserProfileCache.getOffersTarget()));
        offerSlider.setMinimum(1);
        offerSlider.setMaximum(10);
        offerSlider.setValue(savedOffers);
        offerSlider.setMajorTickSpacing(1);
        offerSlider.setPaintTicks(true);
        offerSlider.setSnapToTicks(true);
        offerSlider.setOpaque(false);
        offerSlider.setForeground(Theme.PRIMARY_START);
        offerSlider.setBackground(Theme.PANEL_BG);

        offerValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        offerValueLabel.setForeground(Theme.PRIMARY_TEXT);
        updateOfferLabel(savedOffers);

        offerSlider.addChangeListener(e -> {
            int value = offerSlider.getValue();
            updateOfferLabel(value);
            if (!offerSlider.getValueIsAdjusting()) {
                saveOfferTarget(value);
            }
        });

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(labelBlock("Target placement offers", "How many offers you want to aim for this season."), BorderLayout.CENTER);
        topRow.add(offerValueLabel, BorderLayout.EAST);

        body.add(topRow);
        body.add(Box.createVerticalStrut(12));
        body.add(offerSlider);
        body.add(Box.createVerticalStrut(12));
        body.add(progressPreview("Prepared companies", UserProfileCache.getCompaniesPrepared(), Math.max(savedOffers * 4, 4), TEAL));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildDisplayCard() {
        JPanel card = surfaceCard("Display", "Adjust the app's visual behavior and density.");
        JPanel body = verticalPanel();

        body.add(toggleRow("Dark mode", "Switch TalentForge between light and dark palettes.", Theme.isDark(),
                selected -> Theme.setDarkMode(selected), Icons.gear(Theme.PRIMARY_START)));
        body.add(separator());
        body.add(toggleRow("Compact module spacing", "Use denser panels for dashboards and repeated practice.", getBool(KEY_COMPACT_MODE, false),
                selected -> putBool(KEY_COMPACT_MODE, selected), Icons.dashboardGrid(TEAL)));
        body.add(separator());
        body.add(toggleRow("Reduce motion", "Prefer calmer transitions and fewer animated details.", getBool(KEY_REDUCE_MOTION, false),
                selected -> putBool(KEY_REDUCE_MOTION, selected), Icons.eye(AMBER, true)));
        body.add(separator());
        body.add(comboRow("Interface scale", "Choose a comfortable reading size.", new String[]{"Comfortable", "Compact", "Large"},
                PREFS.get(KEY_FONT_SCALE, "Comfortable"), value -> put(KEY_FONT_SCALE, value), VIOLET));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildLearningCard() {
        JPanel card = surfaceCard("Learning Preferences", "Shape the practice plan around your goals.");
        JPanel body = verticalPanel();

        body.add(spinnerRow("Daily practice minutes", "Focus time you want to protect each day.", KEY_DAILY_MINUTES, 60, 15, 240, 15, SKY));
        body.add(separator());
        body.add(spinnerRow("Weekly coding target", "Problems you want to solve this week.", KEY_WEEKLY_CODING_TARGET, 18, 1, 100, 1, TEAL));
        body.add(separator());
        body.add(spinnerRow("Mock interview target", "Interview sessions to complete this month.", KEY_INTERVIEW_TARGET, 4, 0, 30, 1, AMBER));
        body.add(separator());
        body.add(comboRow("Preferred track", "Sets your default preparation lens.", new String[]{"Software Developer", "Data Analyst", "Product", "Core Engineering", "Consulting"},
                PREFS.get(KEY_TRACK, "Software Developer"), value -> put(KEY_TRACK, value), ROSE));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildReminderCard() {
        JPanel card = surfaceCard("Reminders", "Decide how TalentForge nudges you back into practice.");
        JPanel body = verticalPanel();

        body.add(toggleRow("Daily reminders", "Show a practice reminder at your preferred time.", getBool(KEY_REMINDERS, true),
                selected -> putBool(KEY_REMINDERS, selected), Icons.bell(SKY)));
        body.add(separator());
        body.add(comboRow("Reminder time", "Pick the daily check-in window.", new String[]{"07:00 AM", "08:30 AM", "12:30 PM", "05:30 PM", "07:30 PM", "09:00 PM"},
                PREFS.get(KEY_REMINDER_TIME, "07:30 PM"), value -> put(KEY_REMINDER_TIME, value), TEAL));
        body.add(separator());
        body.add(toggleRow("Weekly summary", "Keep a weekly progress recap available.", getBool(KEY_WEEKLY_SUMMARY, true),
                selected -> putBool(KEY_WEEKLY_SUMMARY, selected), Icons.chart(AMBER)));
        body.add(separator());
        body.add(toggleRow("Streak rescue alerts", "Nudge before a day ends without activity.", getBool(KEY_STREAK_RESCUE, true),
                selected -> putBool(KEY_STREAK_RESCUE, selected), Icons.calendar(ROSE)));
        body.add(separator());
        body.add(comboRow("Notification channel", "Choose where reminders should appear.", new String[]{"In-app only", "Desktop", "Email digest"},
                PREFS.get(KEY_CHANNEL, "In-app only"), value -> put(KEY_CHANNEL, value), VIOLET));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildDataCard() {
        JPanel card = surfaceCard("Data & Safety", "Control how practice data is handled inside the app.");
        JPanel body = verticalPanel();

        body.add(toggleRow("Auto-save progress", "Save completed practice updates as soon as they happen.", getBool(KEY_AUTO_SAVE, true),
                selected -> putBool(KEY_AUTO_SAVE, selected), Icons.checkCircleFilled(Theme.SUCCESS_COLOR, Color.WHITE)));
        body.add(separator());
        body.add(toggleRow("Confirm major actions", "Ask before destructive or high-impact changes.", getBool(KEY_CONFIRM_ACTIONS, true),
                selected -> putBool(KEY_CONFIRM_ACTIONS, selected), Icons.lock(AMBER)));
        body.add(separator());
        body.add(toggleRow("Show demo leaderboard entries", "Keep sample competitors visible when the database is small.", getBool(KEY_SHOW_DEMO_RANKS, true),
                selected -> putBool(KEY_SHOW_DEMO_RANKS, selected), Icons.trophy(ROSE)));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildAboutCard() {
        JPanel card = surfaceCard("Application", "Build details and current account snapshot.");
        JPanel body = verticalPanel();

        body.add(infoRow("Version", "1.0.0 Release Build"));
        body.add(separator());
        body.add(infoRow("User ID", UserProfileCache.getCurrentUserId() > 0 ? String.valueOf(UserProfileCache.getCurrentUserId()) : "Guest"));
        body.add(separator());
        body.add(infoRow("Resume score", UserProfileCache.getResumeScore() + "%"));
        body.add(separator());
        body.add(infoRow("Prepared companies", String.valueOf(UserProfileCache.getCompaniesPrepared())));
        body.add(separator());
        body.add(infoRow("Developed by", "Y Varshith and Shabd Jain"));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel surfaceCard(String title, String subtitle) {
        JPanel card = new SurfacePanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JPanel text = verticalPanel();
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Theme.MUTED_TEXT);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitleLabel);

        header.add(text, BorderLayout.CENTER);
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JComponent toggleRow(String title, String subtitle, boolean selected, ToggleHandler handler, Icon icon) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setPreferredSize(new Dimension(28, 28));
        row.add(iconLabel, BorderLayout.WEST);
        row.add(labelBlock(title, subtitle), BorderLayout.CENTER);

        JToggleButton toggle = new JToggleButton(selected ? "On" : "Off");
        styleToggle(toggle, selected);
        toggle.setSelected(selected);
        toggle.addActionListener(e -> {
            boolean value = toggle.isSelected();
            toggle.setText(value ? "On" : "Off");
            styleToggle(toggle, value);
            handler.onChange(value);
            announceSaved();
        });
        row.add(toggle, BorderLayout.EAST);
        return row;
    }

    private JComponent spinnerRow(String title, String subtitle, String key, int defaultValue, int min, int max, int step, Color accent) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.add(accentBadge(accent), BorderLayout.WEST);
        row.add(labelBlock(title, subtitle), BorderLayout.CENTER);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(PREFS.getInt(key, defaultValue), min, max, step));
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spinner.setPreferredSize(new Dimension(82, 36));
        spinner.addChangeListener(e -> {
            PREFS.putInt(key, (Integer) spinner.getValue());
            announceSaved();
        });
        row.add(spinner, BorderLayout.EAST);
        return row;
    }

    private JComponent comboRow(String title, String subtitle, String[] options, String selected, ValueHandler handler, Color accent) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.add(accentBadge(accent), BorderLayout.WEST);
        row.add(labelBlock(title, subtitle), BorderLayout.CENTER);

        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setSelectedItem(selected);
        combo.setPreferredSize(new Dimension(170, 36));
        combo.addActionListener(e -> {
            Object value = combo.getSelectedItem();
            if (value != null) {
                handler.onChange(value.toString());
                announceSaved();
            }
        });
        row.add(combo, BorderLayout.EAST);
        return row;
    }

    private JComponent infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel left = new JLabel(label);
        left.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        left.setForeground(Theme.MUTED_TEXT);

        JLabel right = new JLabel(value);
        right.setFont(new Font("Segoe UI", Font.BOLD, 13));
        right.setForeground(Theme.PRIMARY_TEXT);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent progressPreview(String label, int value, int max, Color color) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title.setForeground(Theme.MUTED_TEXT);
        title.setPreferredSize(new Dimension(128, 26));

        JProgressBar progress = new JProgressBar(0, Math.max(1, max));
        progress.setValue(Math.min(value, Math.max(1, max)));
        progress.setStringPainted(true);
        progress.setString(value + " / " + Math.max(1, max));
        progress.setFont(new Font("Segoe UI", Font.BOLD, 11));
        progress.setForeground(color);
        progress.setBackground(Theme.isDark() ? new Color(48, 50, 64) : new Color(229, 231, 235));
        progress.setBorderPainted(false);

        panel.add(title, BorderLayout.WEST);
        panel.add(progress, BorderLayout.CENTER);
        return panel;
    }

    private JPanel metricCard(String title, String value, String caption, Color accent) {
        JPanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(accentBadge(accent), BorderLayout.WEST);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.MUTED_TEXT);
        top.add(label, BorderLayout.CENTER);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        captionLabel.setForeground(Theme.MUTED_TEXT);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel labelBlock(String title, String subtitle) {
        JPanel panel = verticalPanel();

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Theme.MUTED_TEXT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitleLabel);
        return panel;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JComponent separator() {
        JPanel line = new JPanel();
        line.setOpaque(true);
        line.setBackground(Theme.isDark() ? new Color(55, 58, 73) : new Color(232, 235, 242));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(13, 0, 13, 0));
        wrapper.add(line, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent accentBadge(Color accent) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mix(accent, Theme.PANEL_BG, 0.20f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillOval(getWidth() / 2 - 4, getHeight() / 2 - 4, 8, 8);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(28, 28));
        return badge;
    }

    private JButton glassButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 46));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleToggle(JToggleButton toggle, boolean selected) {
        toggle.setFocusPainted(false);
        toggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setPreferredSize(new Dimension(64, 34));
        toggle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? Theme.PRIMARY_START : Theme.FIELD_BORDER),
                new EmptyBorder(7, 12, 7, 12)));
        toggle.setBackground(selected ? Theme.PRIMARY_START : Theme.FIELD_BG);
        toggle.setForeground(selected ? Color.WHITE : Theme.PRIMARY_TEXT);
        toggle.setOpaque(true);
    }

    private void updateOfferLabel(int value) {
        offerValueLabel.setText(value + (value == 1 ? " offer" : " offers"));
    }

    private void saveOfferTarget(int value) {
        UserProfileCache.setOffersTarget(value);
        int userId = UserProfileCache.getCurrentUserId();
        if (userId > 0) {
            StatsService.saveOffersTarget(userId, value);
        }
        announceSaved();
    }

    private void resetLocalPreferences() {
        PREFS.putInt(KEY_DAILY_MINUTES, 60);
        PREFS.putInt(KEY_WEEKLY_CODING_TARGET, 18);
        PREFS.putInt(KEY_INTERVIEW_TARGET, 4);
        PREFS.put(KEY_TRACK, "Software Developer");
        PREFS.putBoolean(KEY_REMINDERS, true);
        PREFS.put(KEY_REMINDER_TIME, "07:30 PM");
        PREFS.putBoolean(KEY_WEEKLY_SUMMARY, true);
        PREFS.putBoolean(KEY_STREAK_RESCUE, true);
        PREFS.put(KEY_CHANNEL, "In-app only");
        PREFS.putBoolean(KEY_COMPACT_MODE, false);
        PREFS.putBoolean(KEY_REDUCE_MOTION, false);
        PREFS.put(KEY_FONT_SCALE, "Comfortable");
        PREFS.putBoolean(KEY_AUTO_SAVE, true);
        PREFS.putBoolean(KEY_CONFIRM_ACTIONS, true);
        PREFS.putBoolean(KEY_SHOW_DEMO_RANKS, true);
        statusLabel.setText("Local options reset.");
        buildUI();
    }

    private boolean getBool(String key, boolean fallback) {
        return PREFS.getBoolean(key, fallback);
    }

    private void putBool(String key, boolean value) {
        PREFS.putBoolean(key, value);
    }

    private void put(String key, String value) {
        PREFS.put(key, value);
    }

    private void announceSaved() {
        if (!rebuilding) {
            statusLabel.setText("Saved just now.");
        }
    }

    private void onThemeChange() {
        buildUI();
    }

    private static Color mix(Color a, Color b, float ratioFromA) {
        float clamped = Math.min(1f, Math.max(0f, ratioFromA));
        float other = 1f - clamped;
        return new Color(
                Math.round(a.getRed() * clamped + b.getRed() * other),
                Math.round(a.getGreen() * clamped + b.getGreen() * other),
                Math.round(a.getBlue() * clamped + b.getBlue() * other)
        );
    }

    private interface ToggleHandler {
        void onChange(boolean selected);
    }

    private interface ValueHandler {
        void onChange(String value);
    }

    private static class SurfacePanel extends JPanel {
        SurfacePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(Theme.isDark() ? new Color(58, 61, 77) : new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(LayoutManager layout, Color start, Color end) {
            super(layout);
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
