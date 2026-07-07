package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A white card with a subtle drop shadow — a lighter-weight version of
 * ShadowPanel, sized for use inside grids (stat cards, module cards)
 * rather than as one big centerpiece card.
 */
public class ElevatedCard extends JPanel {

    private static final int SHADOW_SIZE = 8;
    private static final int ARC = 14;

    public ElevatedCard(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - SHADOW_SIZE * 2;
        int h = getHeight() - SHADOW_SIZE * 2;
        int x = SHADOW_SIZE;
        int y = SHADOW_SIZE;

        for (int i = SHADOW_SIZE; i > 0; i--) {
            float alpha = (1f - (float) i / SHADOW_SIZE) * 0.06f;
            g2.setColor(new Color(15, 15, 30, (int) (alpha * 255)));
            g2.fillRoundRect(x - i / 2, y - i / 2, w + i, h + i, ARC + i / 2, ARC + i / 2);
        }

        g2.setColor(Theme.PANEL_BG);
        g2.fillRoundRect(x, y, w, h, ARC, ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}