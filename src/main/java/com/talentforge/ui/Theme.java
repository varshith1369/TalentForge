package com.talentforge.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/**
 * Central place for colors and fonts. Fields are non-final so
 * {@link #setDarkMode(boolean)} can swap the whole palette at runtime.
 * Custom-painted components (ShadowPanel, GlassCard, etc.) read these
 * fields fresh on every repaint, so a simple window repaint after
 * toggling is enough to re-theme them.
 */
public class Theme {
    // Brand colors stay the same in both modes
    public static final Color PRIMARY_START = new Color(99, 102, 241);
    public static final Color PRIMARY_END   = new Color(139, 92, 246);
    public static final Color ERROR_COLOR   = new Color(220, 38, 38);
    public static final Color SUCCESS_COLOR = new Color(22, 163, 74);

    // Palette-dependent colors (swapped on toggle)
    public static Color PRIMARY_TEXT = new Color(30, 27, 46);
    public static Color MUTED_TEXT   = new Color(107, 114, 128);
    public static Color FIELD_BORDER = new Color(209, 213, 219);
    public static Color FIELD_FOCUS  = new Color(99, 102, 241);
    public static Color FIELD_BG     = Color.WHITE;
    public static Color PANEL_BG     = Color.WHITE;
    public static Color PAGE_BG      = new Color(243, 244, 248);
    public static Color FOOTER_BG    = new Color(249, 250, 251);
    public static Color FOOTER_BORDER = new Color(229, 231, 235);

    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_FIELD    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_LINK     = new Font("Segoe UI", Font.PLAIN, 13);

    private static boolean dark = false;
    private static final List<Runnable> listeners = new ArrayList<>();

    public static boolean isDark() {
        return dark;
    }

    /** Components can register to run extra logic (beyond a simple repaint) when the mode changes. */
    public static void addListener(Runnable r) {
        listeners.add(r);
    }

    public static void toggleDarkMode() {
        setDarkMode(!dark);
    }

    public static void applyGlobalUIDefaults() {
        UIManager.put("Component.arc", 14);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("CheckBox.arc", 4);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.accentColor", Theme.PRIMARY_START);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
    }

    public static void setDarkMode(boolean enabled) {
        dark = enabled;

        if (dark) {
            PRIMARY_TEXT   = new Color(237, 237, 245);
            MUTED_TEXT     = new Color(156, 163, 175);
            FIELD_BORDER   = new Color(75, 78, 95);
            FIELD_BG       = new Color(39, 39, 54);
            PANEL_BG       = new Color(28, 28, 40);
            PAGE_BG        = new Color(18, 18, 28);
            FOOTER_BG      = new Color(24, 24, 36);
            FOOTER_BORDER  = new Color(55, 55, 70);
        } else {
            PRIMARY_TEXT   = new Color(30, 27, 46);
            MUTED_TEXT     = new Color(107, 114, 128);
            FIELD_BORDER   = new Color(209, 213, 219);
            FIELD_BG       = Color.WHITE;
            PANEL_BG       = Color.WHITE;
            PAGE_BG        = new Color(243, 244, 248);
            FOOTER_BG      = new Color(249, 250, 251);
            FOOTER_BORDER  = new Color(229, 231, 235);
        }

        try {
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf());
            applyGlobalUIDefaults();
        } catch (Exception ignored) {
            // Falls back to whatever LAF was already active
        }

        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.repaint();
        }
        for (Runnable r : listeners) {
            r.run();
        }
    }
}