package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A rounded button with a gradient fill and a hover effect.
 * Reuse this anywhere in the app that needs a primary action button.
 */
public class RoundedButton extends JButton {

    private boolean hover = false;

    public RoundedButton(String text) {
        super(text);
        setFont(Theme.FONT_BUTTON);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hover = true;
                repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 14;
        GradientPaint gradient = hover
                ? new GradientPaint(0, 0, Theme.PRIMARY_END, getWidth(), 0, Theme.PRIMARY_START)
                : new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END);

        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();

        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 220), 44);
    }
}