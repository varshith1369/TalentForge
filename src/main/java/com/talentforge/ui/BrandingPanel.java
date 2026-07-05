package com.talentforge.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * The left-side branding panel: gradient background, logo, tagline,
 * floating decorative icon chips, a 5-stat row, and a "Why TalentForge?"
 * two-column checklist.
 */
public class BrandingPanel extends JPanel {

    private final BufferedImage logoImage;

    public BrandingPanel() {
        setPreferredSize(new Dimension(430, 0));
        setLayout(new GridBagLayout());
        logoImage = loadLogo();
    }

    private BufferedImage loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/images/logo.png")) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(30, 27, 75),
                getWidth(), getHeight(), new Color(88, 28, 135)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(255, 255, 255, 15));
        int spacing = 28;
        for (int x = spacing; x < getWidth(); x += spacing) {
            for (int y = spacing; y < getHeight(); y += spacing) {
                g2.fillOval(x, y, 2, 2);
            }
        }

        g2.dispose();
    }

    public JPanel buildContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        if (logoImage != null) {
            JLabel logoLabel = new JLabel(new ImageIcon(
                    logoImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            content.add(logoLabel);
        }

        JLabel appName = new JLabel("TalentForge");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("AI Powered Placement Platform");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(216, 210, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 10, 0));

        content.add(appName);
        content.add(subtitle);
        content.add(buildTaglineRow());
        content.add(Box.createVerticalStrut(16));
        content.add(buildFloatingBadges());
        content.add(Box.createVerticalStrut(16));
        content.add(buildStatRow());
        content.add(Box.createVerticalStrut(16));
        content.add(buildWhySection());

        return content;
    }

    private JPanel buildTaglineRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        row.add(taglineWord("Prepare"));
        row.add(dot(new Color(56, 189, 248)));
        row.add(taglineWord("Practice"));
        row.add(dot(new Color(196, 181, 253)));
        row.add(taglineWord("Get Hired"));

        return row;
    }

    private JLabel taglineWord(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(240, 240, 255));
        return label;
    }

    private JLabel dot(Color color) {
        JLabel label = new JLabel("\u25CF");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 7));
        label.setForeground(color);
        return label;
    }

    /** Small floating glass icon chips standing in for the illustration. */
    private JPanel buildFloatingBadges() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        row.add(floatingChip(Icons.code(Color.WHITE), new Color(56, 189, 248)));
        row.add(floatingChip(Icons.document(Color.WHITE), new Color(167, 139, 250)));
        row.add(floatingChip(Icons.chart(Color.WHITE), new Color(244, 114, 182)));
        row.add(floatingTextChip("AI"));

        return row;
    }

    private JComponent floatingChip(Icon icon, Color tint) {
        JPanel chip = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setPreferredSize(new Dimension(42, 42));
        chip.add(new JLabel(icon));
        return chip;
    }

    private JComponent floatingTextChip(String text) {
        JPanel chip = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setPreferredSize(new Dimension(42, 42));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(88, 28, 135));
        chip.add(label);
        return chip;
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(400, 80));

        row.add(statItem(Icons.peopleGroup(Color.WHITE), new Color(56, 189, 248), "1,200+", "Students"));
        row.add(statItem(Icons.code(Color.WHITE), new Color(139, 92, 246), "3,500+", "Problems"));
        row.add(statItem(Icons.trophy(Color.WHITE), new Color(251, 191, 36), "95%", "Success"));
        row.add(statItem(Icons.video(Color.WHITE), new Color(244, 114, 182), "150+", "Interviews"));
        row.add(statItem(Icons.building(Color.WHITE), new Color(52, 211, 153), "75+", "Companies"));

        return row;
    }

    private JPanel statItem(Icon icon, Color color, String number, String label) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        IconBadge badge = new IconBadge(icon, color, color.darker(), true, 34);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        numberLabel.setForeground(Color.WHITE);
        numberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        numberLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        textLabel.setForeground(new Color(216, 210, 255));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(badge);
        col.add(numberLabel);
        col.add(textLabel);
        return col;
    }

    private JPanel buildWhySection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.setMaximumSize(new Dimension(400, 200));

        JLabel heading = new JLabel("Why TalentForge?");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));

        String[] left = {"Coding & Aptitude Practice", "AI Resume Analysis",
                "Mock Interviews & Analytics", "Company-wise Preparation"};
        String[] right = {"Skill Tracker & Analytics", "Study Planner",
                "Notes & Revision", "Leaderboard & Rewards"};

        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0));
        columns.setOpaque(false);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);

        columns.add(checklistColumn(left));
        columns.add(checklistColumn(right));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(heading);
        wrapper.add(columns);

        section.add(wrapper);
        return section;
    }

    private JPanel checklistColumn(String[] items) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        for (String item : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel check = new JLabel(Icons.checkSimple(new Color(52, 211, 153)));
            JLabel text = new JLabel(item);
            text.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            text.setForeground(new Color(230, 225, 255));
            text.setOpaque(false);

            row.add(check);
            row.add(text);
            col.add(row);
        }
        return col;
    }
}