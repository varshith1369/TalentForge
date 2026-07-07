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

    public static Icon chart(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{x, x + 5, x + 10, x + 16}, new int[]{y + 12, y + 7, y + 9, y + 2}, 4);
                g2.fillOval(x - 1, y + 10, 4, 4);
                g2.fillOval(x + 4, y + 5, 4, 4);
                g2.fillOval(x + 9, y + 7, 4, 4);
                g2.fillOval(x + 15, y, 4, 4);
                g2.dispose();
            }
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 18; }
        };
    }

    public static Icon code(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{x + 6, x + 1, x + 6}, new int[]{y + 2, y + 8, y + 14}, 3);
                g2.drawPolyline(new int[]{x + 12, x + 17, x + 12}, new int[]{y + 2, y + 8, y + 14}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon document(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, y, 11, 15, 3, 3);
                g2.drawLine(x + 4, y + 4, x + 9, y + 4);
                g2.drawLine(x + 4, y + 7, x + 9, y + 7);
                g2.drawOval(x + 8, y + 9, 6, 6);
                g2.drawLine(x + 13, y + 14, x + 16, y + 17);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 18; }
        };
    }

    public static Icon video(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, y + 2, 12, 10, 3, 3);
                int[] xs = {x + 13, x + 18, x + 18, x + 13};
                int[] ys = {y + 5, y + 2, y + 12, y + 9};
                g2.drawPolyline(xs, ys, 4);
                g2.dispose();
            }
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 14; }
        };
    }

    public static Icon chevronRight(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{x, x + 5, x}, new int[]{y, y + 5, y + 10}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 10; }
            public int getIconHeight() { return 10; }
        };
    }

    public static Icon dashboardGrid(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(x, y, 7, 7, 2, 2);
                g2.fillRoundRect(x + 9, y, 7, 7, 2, 2);
                g2.fillRoundRect(x, y + 9, 7, 7, 2, 2);
                g2.fillRoundRect(x + 9, y + 9, 7, 7, 2, 2);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon gear(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawOval(x + 4, y + 4, 8, 8);
                g2.fillOval(x + 7, y + 7, 2, 2);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int cx = x + 8, cy = y + 8;
                    int x1 = (int) (cx + Math.cos(angle) * 6);
                    int y1 = (int) (cy + Math.sin(angle) * 6);
                    int x2 = (int) (cx + Math.cos(angle) * 8);
                    int y2 = (int) (cy + Math.sin(angle) * 8);
                    g2.drawLine(x1, y1, x2, y2);
                }
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 18; }
        };
    }

    public static Icon calendar(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(x, y + 2, 16, 13, 3, 3);
                g2.drawLine(x, y + 6, x + 16, y + 6);
                g2.drawLine(x + 4, y, x + 4, y + 4);
                g2.drawLine(x + 12, y, x + 12, y + 4);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon brain(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawArc(x, y, 9, 14, 90, 180);
                g2.drawArc(x + 7, y, 9, 14, -90, 180);
                g2.drawLine(x + 8, y + 2, x + 8, y + 12);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon logout(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, y, 9, 14, 2, 2);
                g2.drawLine(x + 6, y + 7, x + 16, y + 7);
                g2.drawPolyline(new int[]{x + 12, x + 16, x + 12}, new int[]{y + 3, y + 7, y + 11}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 14; }
        };
    }

    public static Icon bell(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawArc(x + 2, y, 12, 12, 0, 180);
                g2.drawLine(x + 2, y + 6, x + 2, y + 10);
                g2.drawLine(x + 14, y + 6, x + 14, y + 10);
                g2.drawLine(x, y + 10, x + 16, y + 10);
                g2.drawArc(x + 6, y + 10, 4, 4, 180, 180);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon googleG() {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d = 16;
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(new Color(66, 133, 244));
                g2.drawArc(x, y, d, d, -10, 100);
                g2.setColor(new Color(52, 168, 83));
                g2.drawArc(x, y, d, d, 90, 90);
                g2.setColor(new Color(251, 188, 5));
                g2.drawArc(x, y, d, d, 180, 80);
                g2.setColor(new Color(234, 67, 53));
                g2.drawArc(x, y, d, d, 260, 90);
                g2.setColor(new Color(66, 133, 244));
                g2.fillRect(x + 8, y + 6, 8, 4);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon checkSimple(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{x, x + 4, x + 11}, new int[]{y + 5, y + 9, y}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 14; }
            public int getIconHeight() { return 12; }
        };
    }

    public static Icon checkCircleFilled(Color bg, Color check) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(x, y, 18, 18);
                g2.setColor(check);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{x + 4, x + 8, x + 14}, new int[]{y + 9, y + 13, y + 5}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 18; }
        };
    }

    public static Icon trophy(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 4, y, 10, 9, 3, 3);
                g2.drawArc(x, y, 6, 8, -90, 180);
                g2.drawArc(x + 12, y, 6, 8, 90, 180);
                g2.drawLine(x + 9, y + 9, x + 9, y + 13);
                g2.drawLine(x + 5, y + 15, x + 13, y + 15);
                g2.drawLine(x + 6, y + 13, x + 12, y + 13);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon building(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRect(x + 2, y, 12, 16);
                g2.fillRect(x + 5, y + 3, 2, 2);
                g2.fillRect(x + 9, y + 3, 2, 2);
                g2.fillRect(x + 5, y + 7, 2, 2);
                g2.fillRect(x + 9, y + 7, 2, 2);
                g2.fillRect(x + 5, y + 11, 2, 2);
                g2.fillRect(x + 9, y + 11, 2, 2);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon peopleGroup(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x + 4, y, 6, 6);
                g2.drawArc(x, y + 6, 14, 10, 0, 180);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon userPlus(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawOval(x + 2, y, 6, 6);
                g2.drawArc(x - 1, y + 6, 14, 10, 0, 180);
                g2.drawLine(x + 13, y + 2, x + 13, y + 8);
                g2.drawLine(x + 10, y + 5, x + 16, y + 5);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon wave(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(x + 2, y, 14, 14, 20, 180);
                g2.drawLine(x + 9, y + 7, x + 9, y + 15);
                g2.drawLine(x + 6, y + 15, x + 15, y + 15);
                g2.dispose();
            }
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 18; }
        };
    }

    public static Icon loginArrow(Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, y, 9, 14, 2, 2);
                g2.drawLine(x + 9, y + 7, x + 17, y + 7);
                g2.drawPolyline(new int[]{x + 13, x + 17, x + 13}, new int[]{y + 3, y + 7, y + 11}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 14; }
        };
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