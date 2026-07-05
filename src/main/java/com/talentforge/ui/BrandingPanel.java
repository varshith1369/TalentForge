package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The left-side branding panel: gradient background with a subtle dot-grid
 * pattern, decorative translucent circles, app identity, and a floating
 * stat badge for a modern SaaS-landing-page feel.
 */
public class BrandingPanel extends JPanel {

    private double parallaxX = 0;
    private double parallaxY = 0;

    public BrandingPanel() {
        setPreferredSize(new Dimension(380, 0));
        setLayout(new GridBagLayout());

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                double targetX = (e.getX() - getWidth() / 2.0) / (getWidth() / 2.0);
                double targetY = (e.getY() - getHeight() / 2.0) / (getHeight() / 2.0);
                parallaxX = targetX * 14;
                parallaxY = targetY * 14;
                repaint();
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                parallaxX = 0;
                parallaxY = 0;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, Theme.PRIMARY_START,
                getWidth(), getHeight(), Theme.PRIMARY_END
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Subtle dot-grid pattern for texture
        g2.setColor(new Color(255, 255, 255, 18));
        int spacing = 26;
        for (int x = spacing; x < getWidth(); x += spacing) {
            for (int y = spacing; y < getHeight(); y += spacing) {
                g2.fillOval(x, y, 2, 2);
            }
        }

        // Decorative translucent circles for depth (shift slightly with mouse for parallax)
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillOval((int) (-60 + parallaxX * 0.6), (int) (-60 + parallaxY * 0.6), 220, 220);
        g2.fillOval((int) (getWidth() - 120 - parallaxX * 0.8), (int) (getHeight() - 160 - parallaxY * 0.8), 260, 260);

        g2.setColor(new Color(255, 255, 255, 15));
        g2.fillOval((int) (getWidth() - 90 - parallaxX), (int) (40 + parallaxY), 140, 140);
        g2.fillOval((int) (-40 + parallaxX), (int) (getHeight() - 120 - parallaxY), 160, 160);

        g2.dispose();
    }

    public JPanel buildContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel logoBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(64, 64));
        logoBadge.setMaximumSize(new Dimension(64, 64));
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel logoText = new JLabel("TF");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoText.setForeground(Color.WHITE);
        logoBadge.add(logoText);

        JLabel appName = new JLabel("TalentForge");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        appName.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JLabel tagline = new JLabel("<html><div style='text-align:center; width:240px;'>"
                + "Your AI-powered placement<br>readiness partner</div></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(255, 255, 255, 220));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(10, 0, 24, 0));

        content.add(logoBadge);
        content.add(appName);
        content.add(tagline);
        content.add(buildStatBadge());
        content.add(Box.createVerticalStrut(20));
        content.add(buildFeatureList());

        return content;
    }

    private JPanel buildStatBadge() {
        JPanel badge = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        badge.setMaximumSize(new Dimension(250, 60));
        badge.setPreferredSize(new Dimension(250, 60));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emoji = new JLabel("\uD83D\uDCC8");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel statNumber = new JLabel("1,200+ students");
        statNumber.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statNumber.setForeground(Theme.PRIMARY_TEXT);

        JLabel statLabel = new JLabel("preparing with TalentForge");
        statLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statLabel.setForeground(Theme.MUTED_TEXT);

        textCol.add(statNumber);
        textCol.add(statLabel);

        badge.add(emoji, BorderLayout.WEST);
        badge.add(textCol, BorderLayout.CENTER);

        return badge;
    }

    private JLabel buildFeatureList() {
        JLabel features = new JLabel("<html><div style='text-align:center; width:230px; line-height:190%;'>"
                + "&#10003;&nbsp; Coding &amp; Aptitude Practice<br>"
                + "&#10003;&nbsp; AI Resume Analysis<br>"
                + "&#10003;&nbsp; Mock Interviews &amp; Analytics</div></html>");
        features.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        features.setForeground(new Color(255, 255, 255, 205));
        features.setAlignmentX(Component.CENTER_ALIGNMENT);
        return features;
    }
}