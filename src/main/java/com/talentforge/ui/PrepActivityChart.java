package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Calendar;

/**
 * A beautiful, premium custom chart component showing weekly activity.
 * Draws a smooth curved line with a vertical gradient fill, gridlines,
 * and highlights days with interactive tooltips on mouse hover.
 */
public class PrepActivityChart extends JComponent {

    private final int[] data = new int[]{3, 5, 2, 8, 4, 9, 6}; // Mon-Sun solved count
    private final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private int hoveredIndex = -1;
    private Point mousePoint = null;

    public PrepActivityChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(500, 220));

        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                calculateHoverIndex(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                mousePoint = null;
                repaint();
            }
        };

        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    public void setWeeklyData(int[] newData) {
        if (newData != null && newData.length == 7) {
            System.arraycopy(newData, 0, this.data, 0, 7);
            repaint();
        }
    }

    private int getTodayIndex() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Monday = 2, ...
        if (dayOfWeek == Calendar.SUNDAY) {
            return 6;
        } else {
            return dayOfWeek - 2; // Monday is 0
        }
    }

    /** Increment today's count and return new value. */
    public int incrementTodayActivity() {
        int index = getTodayIndex();
        if (index >= 0 && index < 7) {
            data[index]++;
            repaint();
            return data[index];
        }
        return 0;
    }

    /** Decrement today's count and return new value. */
    public int decrementTodayActivity() {
        int index = getTodayIndex();
        if (index >= 0 && index < 7) {
            if (data[index] > 0) {
                data[index]--;
                repaint();
            }
            return data[index];
        }
        return 0;
    }

    private void calculateHoverIndex(Point p) {
        int width = getWidth();
        int paddingLeft = 40;
        int paddingRight = 30;
        int chartWidth = width - paddingLeft - paddingRight;

        int stepX = chartWidth / 6;
        int bestIndex = -1;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < 7; i++) {
            int x = paddingLeft + i * stepX;
            double dist = Math.abs(p.x - x);
            if (dist < stepX / 2.0 && dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
            }
        }

        if (bestIndex != hoveredIndex) {
            hoveredIndex = bestIndex;
            mousePoint = p;
            repaint();
        } else if (bestIndex != -1) {
            mousePoint = p;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int paddingTop = 30;
        int paddingBottom = 40;
        int paddingLeft = 40;
        int paddingRight = 30;

        int chartWidth = w - paddingLeft - paddingRight;
        int chartHeight = h - paddingTop - paddingBottom;

        if (chartWidth <= 0 || chartHeight <= 0) {
            g2.dispose();
            return;
        }

        // 1. Draw Gridlines and Y Ticks
        int maxVal = 10;
        for (int val : data) {
            if (val > maxVal) maxVal = val + 2;
        }

        g2.setColor(Theme.isDark() ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 15));
        g2.setStroke(new BasicStroke(1.0f));

        int yLinesCount = 4;
        for (int i = 0; i <= yLinesCount; i++) {
            int y = paddingTop + chartHeight - (i * chartHeight / yLinesCount);
            g2.drawLine(paddingLeft, y, w - paddingRight, y);

            // Draw Y-axis labels
            g2.setColor(Theme.MUTED_TEXT);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int valLabel = i * maxVal / yLinesCount;
            g2.drawString(String.valueOf(valLabel), 10, y + 4);
            g2.setColor(Theme.isDark() ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 15));
        }

        // Mon-Sun column points calculation
        int stepX = chartWidth / 6;
        Point[] points = new Point[7];
        for (int i = 0; i < 7; i++) {
            int x = paddingLeft + i * stepX;
            int y = paddingTop + chartHeight - (data[i] * chartHeight / maxVal);
            points[i] = new Point(x, y);
        }

        // 2. Draw Hover Guideline (Vertical line)
        if (hoveredIndex >= 0 && hoveredIndex < 7) {
            g2.setColor(Theme.PRIMARY_START);
            float[] dash = {4f, 4f};
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2.drawLine(points[hoveredIndex].x, paddingTop, points[hoveredIndex].x, paddingTop + chartHeight);
        }

        // 3. Draw Gradient Filled Area
        Path2D.Double areaPath = new Path2D.Double();
        areaPath.moveTo(points[0].x, paddingTop + chartHeight);
        areaPath.lineTo(points[0].x, points[0].y);

        // Render smooth curve (Bezier control points)
        for (int i = 0; i < 6; i++) {
            Point p1 = points[i];
            Point p2 = points[i + 1];
            double ctrlX1 = p1.x + (p2.x - p1.x) / 2.0;
            double ctrlY1 = p1.y;
            double ctrlX2 = p1.x + (p2.x - p1.x) / 2.0;
            double ctrlY2 = p2.y;
            areaPath.curveTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, p2.x, p2.y);
        }
        areaPath.lineTo(points[6].x, paddingTop + chartHeight);
        areaPath.closePath();

        Color startGrad = new Color(Theme.PRIMARY_START.getRed(), Theme.PRIMARY_START.getGreen(), Theme.PRIMARY_START.getBlue(), 70);
        Color endGrad = new Color(Theme.PRIMARY_START.getRed(), Theme.PRIMARY_START.getGreen(), Theme.PRIMARY_START.getBlue(), 0);
        GradientPaint fillGradient = new GradientPaint(0, paddingTop, startGrad, 0, paddingTop + chartHeight, endGrad);
        g2.setPaint(fillGradient);
        g2.fill(areaPath);

        // 4. Draw Main Curved Line
        Path2D.Double linePath = new Path2D.Double();
        linePath.moveTo(points[0].x, points[0].y);
        for (int i = 0; i < 6; i++) {
            Point p1 = points[i];
            Point p2 = points[i + 1];
            double ctrlX1 = p1.x + (p2.x - p1.x) / 2.0;
            double ctrlY1 = p1.y;
            double ctrlX2 = p1.x + (p2.x - p1.x) / 2.0;
            double ctrlY2 = p2.y;
            linePath.curveTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, p2.x, p2.y);
        }
        g2.setColor(Theme.PRIMARY_START);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linePath);

        // 5. Draw labels on X Axis and Data Points
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        for (int i = 0; i < 7; i++) {
            Point p = points[i];

            // Highlight if hovered
            if (i == hoveredIndex) {
                // outer glow
                g2.setColor(new Color(Theme.PRIMARY_START.getRed(), Theme.PRIMARY_START.getGreen(), Theme.PRIMARY_START.getBlue(), 50));
                g2.fill(new Ellipse2D.Double(p.x - 7, p.y - 7, 14, 14));

                // inner solid
                g2.setColor(Theme.PRIMARY_START);
                g2.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));

                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(p.x - 2, p.y - 2, 4, 4));
            } else {
                g2.setColor(Theme.PRIMARY_START);
                g2.fill(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
            }

            // X-label
            g2.setColor(Theme.MUTED_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            String dayStr = days[i];
            int tx = p.x - fm.stringWidth(dayStr) / 2;
            int ty = paddingTop + chartHeight + 18;
            g2.drawString(dayStr, tx, ty);
        }

        // 6. Draw tooltip box
        if (hoveredIndex >= 0 && hoveredIndex < 7 && mousePoint != null) {
            String label = days[hoveredIndex] + ": " + data[hoveredIndex] + " solved";
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(label) + 16;
            int th = 26;

            int tx = points[hoveredIndex].x - tw / 2;
            int ty = points[hoveredIndex].y - th - 10;

            // Constrain tooltip coordinates within bounds
            if (tx < paddingLeft) tx = paddingLeft;
            if (tx + tw > w - paddingRight) tx = w - paddingRight - tw;
            if (ty < 5) ty = points[hoveredIndex].y + 10;

            // Draw Tooltip Container with rounded corners
            g2.setColor(Theme.PANEL_BG);
            g2.fillRoundRect(tx, ty, tw, th, 8, 8);

            g2.setColor(Theme.PRIMARY_START);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(tx, ty, tw, th, 8, 8);

            // Draw text inside
            g2.setColor(Theme.PRIMARY_TEXT);
            int textX = tx + 8;
            int textY = ty + (th + fm.getAscent()) / 2 - 2;
            g2.drawString(label, textX, textY);
        }

        g2.dispose();
    }
}
