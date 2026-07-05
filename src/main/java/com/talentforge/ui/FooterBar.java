package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Bottom footer bar: policy/support links on the left, a real working
 * dark-mode toggle on the right.
 */
public class FooterBar extends JPanel {

    public FooterBar() {
        setLayout(new BorderLayout());
        rebuild();
        Theme.addListener(this::rebuild);
    }

    /** Rebuilds the footer's contents/colors to match the current theme. */
    private void rebuild() {
        removeAll();
        setBackground(Theme.FOOTER_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.FOOTER_BORDER),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        left.setOpaque(false);
        left.add(footerLink("Privacy Policy"));
        left.add(footerLink("Terms & Conditions"));
        left.add(footerLink("Help Center"));
        left.add(footerLink("Contact Support"));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel darkLabel = new JLabel("Dark Mode");
        darkLabel.setFont(Theme.FONT_LINK.deriveFont(11f));
        darkLabel.setForeground(Theme.MUTED_TEXT);
        right.add(darkLabel);
        right.add(buildToggle());

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        revalidate();
        repaint();
    }

    private JButton footerLink(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_LINK.deriveFont(11f));
        btn.setForeground(Theme.MUTED_TEXT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this, text + " page coming soon."));
        return btn;
    }

    private JComponent buildToggle() {
        JPanel toggle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean on = Theme.isDark();
                g2.setColor(on ? Theme.PRIMARY_START : new Color(209, 213, 219));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                int knobSize = getHeight() - 4;
                int knobX = on ? getWidth() - knobSize - 2 : 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(knobX, 2, knobSize, knobSize);
                g2.dispose();
            }

            {
                setPreferredSize(new Dimension(38, 20));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        Theme.toggleDarkMode();
                    }
                });
            }
        };
        return toggle;
    }
}