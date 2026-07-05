package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Small hand-drawn vector icons (envelope, lock, eye) used inside input fields.
 * Drawn directly with Graphics2D so there's no dependency on icon fonts or image files.
 */
public class Icons {

    public static Icon envelope(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(x, y + 2, 16, 12, 3, 3);
                g2.drawLine(x, y + 3, x + 8, y + 10);
                g2.drawLine(x + 16, y + 3, x + 8, y + 10);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon lock(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(x + 2, y + 6, 12, 9, 3, 3);
                g2.drawArc(x + 4, y, 8, 10, 0, 180);
                g2.fillOval(x + 7, y + 9, 2, 2);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon user(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawOval(x + 5, y, 6, 6);
                g2.drawArc(x, y + 6, 16, 12, 0, 180);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static class SpinnerIcon implements Icon {
        private double angle = 0;
        private final Color color;

        public SpinnerIcon(Color color) {
            this.color = color;
        }

        public void advance() {
            angle += 28;
            if (angle >= 360) angle -= 360;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.rotate(Math.toRadians(angle), x + 8, y + 8);
            g2.drawArc(x, y, 16, 16, 0, 270);
            g2.dispose();
        }

        public int getIconWidth() { return 18; }
        public int getIconHeight() { return 16; }
    }

    public static Icon eye(Color color, boolean open) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                if (open) {
                    g2.drawArc(x, y + 1, 18, 12, 0, 180);
                    g2.drawArc(x, y - 5, 18, 12, 180, 180);
                    g2.fillOval(x + 7, y + 3, 4, 4);
                } else {
                    g2.drawLine(x + 1, y + 8, x + 17, y + 8);
                    g2.drawArc(x + 2, y + 2, 14, 10, 20, 140);
                }
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }
}