package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Performance overview bar chart.
 * Only shows metrics the user actually tracks: Aptitude, Coding, Mock Interview.
 * No dummy/hardcoded columns.
 */
public class PerformanceBarChart extends JComponent {

    private static final String[] LABELS = {"Aptitude", "Coding", "Mock Int."};
    private int[] yourScores;

    public PerformanceBarChart(int aptitude, int coding, int mock) {
        this.yourScores = new int[]{aptitude, coding, mock};
        setOpaque(false);
    }

    public void updateScores(int aptitude, int coding, int mock) {
        this.yourScores = new int[]{aptitude, coding, mock};
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padLeft = 38, padRight = 10, padTop = 20, padBottom = 36;
        int chartW = w - padLeft - padRight;
        int chartH = h - padTop - padBottom;

        // Y axis grid lines + labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        for (int v = 0; v <= 100; v += 25) {
            int y = padTop + chartH - (int) ((v / 100.0) * chartH);
            g2.setColor(Theme.isDark() ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 12));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padLeft, y, padLeft + chartW, y);
            g2.setColor(Theme.MUTED_TEXT);
            String lbl = String.valueOf(v);
            g2.drawString(lbl, padLeft - 6 - g2.getFontMetrics().stringWidth(lbl), y + 4);
        }

        int n = LABELS.length;
        float colW = (float) chartW / n;
        float barW = colW * 0.52f;

        // "No data" hint if all zeros
        boolean allZero = true;
        for (int s : yourScores) if (s > 0) { allZero = false; break; }

        if (allZero) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(Theme.MUTED_TEXT);
            String msg = "Start practising to see your performance here!";
            int mw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (w - mw) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Draw bars
        for (int i = 0; i < n; i++) {
            float cx = padLeft + i * colW + colW / 2f;
            int bx = (int) (cx - barW / 2);

            int score = Math.min(100, Math.max(0, yourScores[i]));
            int bh = (int) ((score / 100.0) * chartH);
            int by = padTop + chartH - bh;

            if (score > 0) {
                // Gradient bar
                Color topC = getBarColor(i);
                Color botC = new Color(topC.getRed(), topC.getGreen(), topC.getBlue(), 80);
                GradientPaint gp = new GradientPaint(bx, by, topC, bx, padTop + chartH, botC);
                g2.setPaint(gp);
                g2.fillRoundRect(bx, by, (int) barW, bh, 8, 8);

                // Score text on top
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(Theme.PRIMARY_TEXT);
                String sv = String.valueOf(score);
                g2.drawString(sv, (int) cx - g2.getFontMetrics().stringWidth(sv) / 2, by - 4);
            } else {
                // Empty bar outline with "0" label
                g2.setColor(Theme.isDark() ? new Color(255,255,255,18) : new Color(0,0,0,12));
                g2.fillRoundRect(bx, padTop, (int) barW, chartH, 8, 8);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(Theme.MUTED_TEXT);
                g2.drawString("0", (int) cx - 3, padTop + chartH - 4);
            }

            // X label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(Theme.MUTED_TEXT);
            String xl = LABELS[i];
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(xl, (int) cx - fm.stringWidth(xl) / 2, padTop + chartH + 16);
        }

        // Legend dots
        int ly = h - 10;
        int lx = padLeft + 10;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(getBarColor(0));
        g2.fillOval(lx, ly - 6, 8, 8);
        g2.setColor(Theme.MUTED_TEXT);
        g2.drawString("Your Score", lx + 12, ly + 1);

        g2.dispose();
    }

    private Color getBarColor(int idx) {
        Color[] cols = {
            new Color(130, 80, 220),   // Aptitude – purple
            new Color(60, 190, 120),   // Coding – green
            new Color(60, 140, 220),   // Mock – blue
        };
        return cols[idx % cols.length];
    }
}
