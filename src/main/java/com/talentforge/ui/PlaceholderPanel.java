package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/** Shown for sidebar destinations that aren't built yet, so navigation never dead-ends. */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String moduleName) {
        setLayout(new GridBagLayout());
        setBackground(new Color(249, 250, 251));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(Icons.gear(Theme.MUTED_TEXT));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(moduleName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        JLabel subtitle = new JLabel("This module is under construction");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(icon);
        content.add(title);
        content.add(subtitle);

        add(content);
    }
}