package com.talentforge.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Plays a quick horizontal "shake" animation on a component,
 * used to draw attention to a failed login/signup attempt.
 */
public class ShakeAnimator {

    public static void shake(JComponent component) {
        Point original = component.getLocation();
        int amplitude = 8;
        int[] offsets = {amplitude, -amplitude, amplitude * 2 / 3, -amplitude * 2 / 3, amplitude / 3, -amplitude / 3, 0};
        int[] index = {0};

        Timer timer = new Timer(35, null);
        timer.addActionListener(e -> {
            if (index[0] >= offsets.length) {
                timer.stop();
                component.setLocation(original);
                return;
            }
            component.setLocation(original.x + offsets[index[0]], original.y);
            index[0]++;
        });
        timer.start();
    }
}