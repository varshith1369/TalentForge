package com.talentforge.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Password version of RoundedTextField (same look, hides input).
 */
public class RoundedPasswordField extends JPasswordField {

    private boolean focused = false;

    public RoundedPasswordField() {
        setFont(Theme.FONT_FIELD);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setOpaque(false);
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) { focused = true; repaint(); }
            public void focusLost(FocusEvent e) { focused = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.FIELD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(focused ? Theme.FIELD_FOCUS : Theme.FIELD_BORDER);
        g2.setStroke(new BasicStroke(focused ? 2f : 1f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 280), 42);
    }
}