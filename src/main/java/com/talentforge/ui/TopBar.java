package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The top bar shown above the main content area: a welcome message,
 * a notification bell, and a clickable user avatar with a dropdown menu.
 */
public class TopBar extends JPanel implements UserProfileCache.ProfileUpdateListener {

    public interface OnProfileAction {
        void onProfileAction(String action); // "profile", "settings", or "logout"
    }

    private OnProfileAction onProfileAction;
    private JLabel avatar;

    public TopBar(String userFullName) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        String firstName = userFullName == null || userFullName.isBlank()
                ? "there" : userFullName.trim().split("\\s+")[0];

        JPanel welcomeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        welcomeRow.setOpaque(false);
        JLabel welcomeText = new JLabel("Welcome back, " + firstName + "!");
        welcomeText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeText.setForeground(Theme.PRIMARY_TEXT);
        JLabel waveIcon = new JLabel(Icons.wave(new Color(251, 191, 36)));
        welcomeRow.add(waveIcon);
        welcomeRow.add(welcomeText);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JLabel bell = new JLabel(Icons.bell(Theme.MUTED_TEXT));
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        avatar = buildAvatar(firstName);
        JPopupMenu menu = buildProfileMenu(userFullName == null ? "there" : userFullName);

        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                menu.show(avatar, -menu.getPreferredSize().width + avatar.getWidth(), avatar.getHeight() + 6);
            }
        });

        right.add(bell);
        right.add(avatar);

        add(welcomeRow, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        // Register as listener for profile picture updates
        UserProfileCache.addProfileUpdateListener(this);
    }

    public void setOnProfileAction(OnProfileAction callback) {
        this.onProfileAction = callback;
    }

    @Override
    public void onProfilePictureChanged(BufferedImage image) {
        // Refresh the avatar display when profile picture changes
        SwingUtilities.invokeLater(() -> {
            avatar.repaint();
        });
    }

    private JLabel buildAvatar(String firstName) {
        String initial = firstName.isEmpty() ? "?" : firstName.substring(0, 1).toUpperCase();

        JLabel avatarLabel = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                BufferedImage profileImage = UserProfileCache.getProfileImage();
                if (profileImage != null) {
                    // Draw profile image
                    g2.drawImage(profileImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Draw gradient background with initial
                    GradientPaint gradient = new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), getHeight(), Theme.PRIMARY_END);
                    g2.setPaint(gradient);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            }
        };
        avatarLabel.setPreferredSize(new Dimension(36, 36));
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return avatarLabel;
    }

    private JPopupMenu buildProfileMenu(String fullName) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(8, 14, 10, 14));
        JLabel nameLabel = new JLabel(fullName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(Theme.PRIMARY_TEXT);
        JLabel roleLabel = new JLabel("Final Year Student");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(Theme.MUTED_TEXT);
        header.add(nameLabel);
        header.add(roleLabel);

        menu.add(header);
        menu.addSeparator();
        menu.add(menuItem("My Profile", Icons.user(Theme.MUTED_TEXT), "profile"));
        menu.add(menuItem("Settings", Icons.gear(Theme.MUTED_TEXT), "settings"));
        menu.addSeparator();
        menu.add(menuItem("Logout", Icons.logout(new Color(220, 38, 38)), "logout"));

        return menu;
    }

    private JMenuItem menuItem(String text, Icon icon, String action) {
        JMenuItem item = new JMenuItem(text, icon);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setIconTextGap(10);
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 20));
        item.addActionListener(e -> {
            if (onProfileAction != null) onProfileAction.onProfileAction(action);
        });
        return item;
    }
}
