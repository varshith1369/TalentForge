package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A white rounded card with a soft drop shadow, used to make the
 * login/signup form look like it's floating above the page background.
 */
public class ShadowPanel extends JPanel {

    private static final int SHADOW_SIZE = 18;
    private static final int ARC = 24;

    public ShadowPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
                SHADOW_SIZE - 6, SHADOW_SIZE, SHADOW_SIZE + 6, SHADOW_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - SHADOW_SIZE * 2;
        int h = getHeight() - SHADOW_SIZE * 2;
        int x = SHADOW_SIZE;
        int y = SHADOW_SIZE - 6;

        for (int i = SHADOW_SIZE; i > 0; i--) {
            float alpha = (1f - (float) i / SHADOW_SIZE) * 0.10f;
            g2.setColor(new Color(30, 27, 46, (int) (alpha * 255)));
            g2.fillRoundRect(x - i / 2, y - i / 2 + 4, w + i, h + i, ARC + i / 2, ARC + i / 2);
        }

        g2.setColor(Theme.PANEL_BG);
        g2.fillRoundRect(x, y, w, h, ARC, ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}