package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Segmented donut ring progress chart matching the Placement Progress design.
 * Renders multiple colored arcs with an overall percentage in the center.
 */
public class DonutProgressChart extends JComponent {

    public static class Segment {
        final String label;
        final int percent;
        final Color color;
        public Segment(String label, int percent, Color color) {
            this.label = label;
            this.percent = percent;
            this.color = color;
        }
    }

    private Segment[] segments;
    private int overallPercent;

    public DonutProgressChart(Segment[] segments, int overallPercent) {
        this.segments = segments;
        this.overallPercent = overallPercent;
        setOpaque(false);
        setPreferredSize(new Dimension(180, 180));
    }

    public void setSegments(Segment[] segments, int overall) {
        this.segments = segments;
        this.overallPercent = overall;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radius = (int) (Math.min(cx, cy) * 0.85);
        int strokeW = 16;

        int x = cx - radius;
        int y = cy - radius;
        int d = radius * 2;

        // Background track
        g2.setColor(Theme.isDark() ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 10));
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(x, y, d, d, 0, 360, Arc2D.OPEN));

        // Gap angle between segments in degrees
        float gapDeg = 4.0f;
        float usableAngle = 360.0f - (gapDeg * segments.length);

        // Calculate total weight sum (we treat each segment equally by its own %)
        float startAngle = 90.0f; // start at top

        for (Segment seg : segments) {
            float sweep = (seg.percent / 100.0f) * (usableAngle / segments.length);
            g2.setColor(seg.color);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Double(x, y, d, d, startAngle, -sweep, Arc2D.OPEN));
            startAngle -= (sweep + gapDeg);
        }

        // Center text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(Theme.PRIMARY_TEXT);
        String pctStr = overallPercent + "%";
        FontMetrics fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth(pctStr) / 2;
        int ty = cy + fm.getAscent() / 2 - 4;
        g2.drawString(pctStr, tx, ty);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(Theme.MUTED_TEXT);
        String sub = "Overall";
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, ty + 18);

        g2.dispose();
    }
}
