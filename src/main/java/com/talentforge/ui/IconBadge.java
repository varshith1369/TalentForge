package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A small rounded gradient badge with a centered icon, used to lead
 * stat cards and feature rows (matches the "icon chip" pattern common
 * in modern app onboarding/landing screens).
 */
public class IconBadge extends JComponent {

    private final Icon icon;
    private final Color colorStart;
    private final Color colorEnd;
    private final boolean circular;
    private final int size;

    public IconBadge(Icon icon, Color colorStart, Color colorEnd, boolean circular, int size) {
        this.icon = icon;
        this.colorStart = colorStart;
        this.colorEnd = colorEnd;
        this.circular = circular;
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, colorStart, size, size, colorEnd);
        g2.setPaint(gradient);
        if (circular) {
            g2.fillOval(0, 0, size, size);
        } else {
            int arc = size / 3;
            g2.fillRoundRect(0, 0, size, size, arc, arc);
        }

        if (icon != null) {
            int iconX = (size - icon.getIconWidth()) / 2;
            int iconY = (size - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, iconX, iconY);
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }
}