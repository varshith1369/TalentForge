package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * A premium stat card with a colorful icon badge, stat number, subtitle text,
 * and a smooth sparkline mini-chart in the bottom-right corner.
 */
public class SparklineStatCard extends ElevatedCard {

    private final String label;
    private String subtitle;
    private final Color accentCol;
    private final Icon icon;
    private int statValue;
    private final int maxValue;
    private int[] sparkData;
    private JLabel valueLabel;
    private JLabel subtitleLabel;

    public SparklineStatCard(String label, int statValue, int maxValue,
                             String subtitle, Color accent, Icon icon, int[] sparkData) {
        super(new BorderLayout());
        this.label = label;
        this.statValue = statValue;
        this.maxValue = maxValue;
        this.subtitle = subtitle;
        this.accentCol = accent;
        this.icon = icon;
        this.sparkData = sparkData;
        setAccentColor(accent);
        buildUI();
    }

    public void setStatValue(int value) {
        this.statValue = value;
        if (valueLabel != null) {
            if (maxValue > 0) {
                valueLabel.setText(value + " / " + maxValue);
            } else {
                valueLabel.setText(String.valueOf(value));
            }
        }
    }

    public void setSubtitle(String text) {
        this.subtitle = text;
        if (subtitleLabel != null) {
            subtitleLabel.setText(text);
        }
    }

    public void setSparkData(int[] data) {
        this.sparkData = data;
        repaint();
    }

    private void buildUI() {
        setBorder(BorderFactory.createCompoundBorder(
                getBorder(), BorderFactory.createEmptyBorder(14, 14, 10, 14)));

        // Top row: icon badge + label
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);

        JLabel iconBadge = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(accentCol.getRed(), accentCol.getGreen(), accentCol.getBlue(), 35);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setIcon(icon);
        iconBadge.setHorizontalAlignment(SwingConstants.CENTER);
        iconBadge.setPreferredSize(new Dimension(36, 36));
        iconBadge.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Theme.MUTED_TEXT);

        topRow.add(iconBadge, BorderLayout.WEST);
        topRow.add(lbl, BorderLayout.CENTER);

        // Middle: big stat value
        String valStr = maxValue > 0 ? statValue + " / " + maxValue : String.valueOf(statValue);
        valueLabel = new JLabel(valStr);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(Theme.PRIMARY_TEXT);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));

        // Bottom row: subtitle on left, sparkline on right
        JPanel bottomRow = new JPanel(new BorderLayout(8, 0));
        bottomRow.setOpaque(false);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitleLabel.setForeground(accentCol);

        JComponent sparkline = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (sparkData == null || sparkData.length < 2) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int n = sparkData.length;

                int maxD = 1;
                for (int d : sparkData) if (d > maxD) maxD = d;

                float stepX = (float) (w - 4) / (n - 1);
                Point[] pts = new Point[n];
                for (int i = 0; i < n; i++) {
                    int x = (int) (2 + i * stepX);
                    int y = (int) (h - 4 - ((float) sparkData[i] / maxD) * (h - 8));
                    pts[i] = new Point(x, y);
                }

                // Gradient fill
                Path2D area = new Path2D.Float();
                area.moveTo(pts[0].x, h);
                area.lineTo(pts[0].x, pts[0].y);
                for (int i = 0; i < n - 1; i++) {
                    double cx1 = pts[i].x + (pts[i+1].x - pts[i].x) / 2.0;
                    area.curveTo(cx1, pts[i].y, cx1, pts[i+1].y, pts[i+1].x, pts[i+1].y);
                }
                area.lineTo(pts[n-1].x, h);
                area.closePath();

                Color c1 = new Color(accentCol.getRed(), accentCol.getGreen(), accentCol.getBlue(), 55);
                Color c2 = new Color(accentCol.getRed(), accentCol.getGreen(), accentCol.getBlue(), 0);
                g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
                g2.fill(area);

                // Line
                Path2D line = new Path2D.Float();
                line.moveTo(pts[0].x, pts[0].y);
                for (int i = 0; i < n - 1; i++) {
                    double cx1 = pts[i].x + (pts[i+1].x - pts[i].x) / 2.0;
                    line.curveTo(cx1, pts[i].y, cx1, pts[i+1].y, pts[i+1].x, pts[i+1].y);
                }
                g2.setColor(accentCol);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(line);

                g2.dispose();
            }
        };
        sparkline.setOpaque(false);
        sparkline.setPreferredSize(new Dimension(80, 36));

        bottomRow.add(subtitleLabel, BorderLayout.CENTER);
        bottomRow.add(sparkline, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(topRow, BorderLayout.NORTH);
        body.add(valueLabel, BorderLayout.CENTER);
        body.add(bottomRow, BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);
    }
}
