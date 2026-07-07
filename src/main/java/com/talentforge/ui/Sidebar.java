package com.talentforge.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The app-wide sidebar navigation, used on the Dashboard and (eventually)
 * every other module screen. Highlights whichever item is currently active
 * and calls back with the item name when the user clicks a different one.
 */
public class Sidebar extends JPanel {

    public interface OnNavigate {
        void onNavigate(String itemKey);
    }

    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private String activeKey;
    private OnNavigate onNavigate;

    public Sidebar(String initialActiveKey) {
        this.activeKey = initialActiveKey;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(230, 0));
        setBackground(new Color(30, 27, 75));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(20, 16, 10, 16));
        top.add(buildLogoRow());
        top.add(Box.createVerticalStrut(20));

        JPanel navList = new JPanel();
        navList.setOpaque(false);
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));

        addNavItem(navList, "dashboard", "Dashboard", Icons.dashboardGrid(Color.WHITE));
        addNavItem(navList, "coding", "Coding Practice", Icons.code(Color.WHITE));
        addNavItem(navList, "aptitude", "Aptitude Practice", Icons.brain(Color.WHITE));
        addNavItem(navList, "resume", "Resume Checker", Icons.document(Color.WHITE));
        addNavItem(navList, "interview", "Mock Interview", Icons.video(Color.WHITE));
        addNavItem(navList, "companies", "Company Prep", Icons.building(Color.WHITE));
        addNavItem(navList, "notes", "Notes & Revision", Icons.document(Color.WHITE));
        addNavItem(navList, "planner", "Study Planner", Icons.calendar(Color.WHITE));
        addNavItem(navList, "skills", "Skill Tracker", Icons.chart(Color.WHITE));
        addNavItem(navList, "analytics", "Analytics", Icons.chart(Color.WHITE));
        addNavItem(navList, "leaderboard", "Leaderboard", Icons.trophy(Color.WHITE));
        addNavItem(navList, "settings", "Settings", Icons.gear(Color.WHITE));

        JScrollPane scroll = new JScrollPane(navList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        bottom.add(buildLogoutButton());

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refreshActiveStyles();
    }

    public void setOnNavigate(OnNavigate callback) {
        this.onNavigate = callback;
    }

    public void setActive(String key) {
        this.activeKey = key;
        refreshActiveStyles();
    }

    private JPanel buildLogoRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        BufferedImage logo = loadLogo();
        if (logo != null) {
            JLabel logoLabel = new JLabel(new ImageIcon(logo.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
            row.add(logoLabel);
            row.add(Box.createHorizontalStrut(8));
        }

        JLabel name = new JLabel("TalentForge");
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Color.WHITE);
        row.add(name);

        return row;
    }

    private BufferedImage loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/images/logo.png")) {
            if (in != null) return ImageIO.read(in);
        } catch (IOException ignored) {
        }
        return null;
    }

    private void addNavItem(JPanel container, String key, String label, Icon icon) {
        JButton btn = new JButton(label, icon);
        btn.setName(key);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(500, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            setActive(key);
            if (onNavigate != null) onNavigate.onNavigate(key);
        });
        buttons.put(key, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private JButton buildLogoutButton() {
        JButton btn = new JButton("Logout", Icons.logout(new Color(252, 165, 165)));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(252, 165, 165));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setName("logout");
        btn.addActionListener(e -> {
            if (onNavigate != null) onNavigate.onNavigate("logout");
        });
        return btn;
    }

    private void refreshActiveStyles() {
        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
            JButton btn = entry.getValue();
            boolean active = entry.getKey().equals(activeKey);
            if (active) {
                btn.setBackground(new Color(99, 102, 241));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(30, 27, 75));
                btn.setForeground(new Color(199, 195, 232));
            }
        }
    }
}