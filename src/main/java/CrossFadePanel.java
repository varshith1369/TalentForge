package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Wraps a CardLayout panel and animates a smooth cross-dissolve
 * whenever the visible card changes, instead of an instant swap.
 */
public class CrossFadePanel extends JLayeredPane {

    /** A simple panel that draws a static snapshot image at a given alpha. */
    private static class FadeOverlay extends JPanel {
        private float alpha = 1f;
        private final BufferedImage snapshot;

        FadeOverlay(BufferedImage snapshot) {
            this.snapshot = snapshot;
            setOpaque(false);
        }

        void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            g2.drawImage(snapshot, 0, 0, null);
            g2.dispose();
        }
    }

    private final JPanel cards = new JPanel(new CardLayout());
    private FadeOverlay overlay;
    private Timer timer;

    public CrossFadePanel() {
        setLayout(null);
        add(cards, JLayeredPane.DEFAULT_LAYER);
    }

    public void addCard(JComponent component, String name) {
        cards.add(component, name);
    }

    @Override
    public void doLayout() {
        cards.setBounds(0, 0, getWidth(), getHeight());
        if (overlay != null) {
            overlay.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return cards.getPreferredSize();
    }

    /** Switches to the given card with a smooth ~250ms cross-dissolve. */
    public void showCard(String name) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            ((CardLayout) cards.getLayout()).show(cards, name);
            return;
        }

        BufferedImage snapshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = snapshot.createGraphics();
        cards.paint(g2);
        g2.dispose();

        ((CardLayout) cards.getLayout()).show(cards, name);

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        if (overlay != null) {
            remove(overlay);
            overlay = null;
        }

        overlay = new FadeOverlay(snapshot);
        overlay.setBounds(0, 0, getWidth(), getHeight());
        add(overlay, JLayeredPane.PALETTE_LAYER);

        final int totalSteps = 16;
        final int[] step = {0};
        timer = new Timer(14, e -> {
            step[0]++;
            float remaining = 1f - (step[0] / (float) totalSteps);
            if (overlay != null) {
                overlay.setAlpha(remaining);
            }
            if (step[0] >= totalSteps) {
                ((Timer) e.getSource()).stop();
                if (overlay != null) {
                    remove(overlay);
                    overlay = null;
                }
                revalidate();
                repaint();
            }
        });
        timer.start();
    }
}