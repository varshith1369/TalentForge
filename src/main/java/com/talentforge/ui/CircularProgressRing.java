package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/** A simple circular progress ring showing a percentage, used for score-style stats. */
public class CircularProgressRing extends JComponent {

    private int percent;
    private final Color color;
    private final int size;

    /** Updates the displayed percentage and triggers a repaint. */
    public void setPercent(int percent) {
        this.percent = Math.max(0, Math.min(100, percent));
        repaint();
    }

    public CircularProgressRing(int percent, Color color, int size) {
        this.percent = Math.max(0, Math.min(100, percent));
        this.color = color;
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int stroke = 5;
        int pad = stroke / 2;

        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(229, 231, 235));
        g2.drawOval(pad, pad, size - stroke, size - stroke);

        g2.setColor(color);
        double angle = 360.0 * percent / 100.0;
        g2.drawArc(pad, pad, size - stroke, size - stroke, 90, -(int) angle);

        g2.setColor(Theme.PRIMARY_TEXT);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 4));
        String text = percent + "%";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (size - fm.stringWidth(text)) / 2;
        int ty = (size + fm.getAscent()) / 2 - 3;
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}