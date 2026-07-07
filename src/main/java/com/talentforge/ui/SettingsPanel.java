package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Settings Panel allowing users to adjust placement preferences, toggle theme,
 * and view app configurations.
 */
public class SettingsPanel extends JPanel {

    private final JSlider offerSlider = new JSlider(1, 10, 1);
    private final JCheckBox darkModeCheck = new JCheckBox("Enable Cosmic Dark Mode");

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        buildUI();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  UI ASSEMBLY                                                 */
    /* ============================================================ */
    private void buildUI() {
        // TOP Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JLabel title = new JLabel("⚙️ Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitle = new JLabel("Manage your placement preferences and application configurations.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        header.add(left, BorderLayout.CENTER);

        // Core Layout
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Card 1: Placement Preferences
        mainContent.add(buildPlacementPreferencesCard());
        mainContent.add(Box.createVerticalStrut(16));

        // Card 2: Theme Settings
        mainContent.add(buildThemeSettingsCard());
        mainContent.add(Box.createVerticalStrut(16));

        // Card 3: App Info
        mainContent.add(buildAppInfoCard());

        add(header, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private ElevatedCard buildPlacementPreferencesCard() {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        card.setAccentColor(Theme.PRIMARY_START);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("💼 Placement Preferences");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Theme.PRIMARY_TEXT);

        int savedOffers = UserProfileCache.getOffersTarget();
        JLabel sliderLbl = new JLabel("Target Placement Offers: " + savedOffers);
        sliderLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sliderLbl.setForeground(Theme.MUTED_TEXT);
        sliderLbl.setBorder(new EmptyBorder(12, 0, 4, 0));

        offerSlider.setOpaque(false);
        offerSlider.setValue(savedOffers);
        offerSlider.addChangeListener(e -> {
            int val = offerSlider.getValue();
            sliderLbl.setText("Target Placement Offers: " + val);
            UserProfileCache.setOffersTarget(val);
            StatsService.saveOffersTarget(UserProfileCache.getCurrentUserId(), val);
        });

        inner.add(title);
        inner.add(sliderLbl);
        inner.add(offerSlider);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private ElevatedCard buildThemeSettingsCard() {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        card.setAccentColor(Theme.PRIMARY_END);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("🎨 Display Options");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Theme.PRIMARY_TEXT);

        darkModeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        darkModeCheck.setForeground(Theme.PRIMARY_TEXT);
        darkModeCheck.setSelected(Theme.isDark());
        darkModeCheck.setOpaque(false);
        darkModeCheck.setBorder(new EmptyBorder(12, 0, 0, 0));
        darkModeCheck.addActionListener(e -> Theme.setDarkMode(darkModeCheck.isSelected()));

        inner.add(title);
        inner.add(darkModeCheck);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private ElevatedCard buildAppInfoCard() {
        ElevatedCard card = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("ℹ️ Application Info");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel version = new JLabel("Version: 1.0.0 (Release Build)");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        version.setForeground(Theme.MUTED_TEXT);
        version.setBorder(new EmptyBorder(12, 0, 2, 0));

        JLabel team = new JLabel("Developed by: Y Varshith & Shabd Jain");
        team.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        team.setForeground(Theme.MUTED_TEXT);

        inner.add(title);
        inner.add(version);
        inner.add(team);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        darkModeCheck.setSelected(Theme.isDark());
        repaint();
    }
}
