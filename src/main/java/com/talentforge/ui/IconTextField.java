package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A rounded "pill" container holding a leading icon + a borderless text field inside.
 * Looks like a modern Material/SaaS style input instead of a plain boxed field.
 */
public class IconTextField extends JPanel {

    private boolean focused = false;
    private final JTextField field = new JTextField();

    public IconTextField(Icon icon) {
        setOpaque(false);
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

        JLabel iconLabel = new JLabel(icon);

        field.setFont(Theme.FONT_FIELD);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setOpaque(false);
        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) { focused = true; repaint(); }
            public void focusLost(FocusEvent e) { focused = false; repaint(); }
        });

        add(iconLabel, BorderLayout.WEST);
        add(field, BorderLayout.CENTER);
    }

    public String getText() { return field.getText(); }
    public void setText(String text) { field.setText(text); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.FIELD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
        g2.setColor(focused ? Theme.FIELD_FOCUS : Theme.FIELD_BORDER);
        g2.setStroke(new BasicStroke(focused ? 2f : 1f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 24, 24);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 46);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}