import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.talentforge.db.DatabaseConnection;
import com.talentforge.ui.LoginSignupPanel;
import com.talentforge.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class AuthTestApp {
    public static void main(String[] args) {
        // Enables FlatLaf's native unified title bar instead of the plain OS one
        JFrame.setDefaultLookAndFeelDecorated(true);
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 14);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("CheckBox.arc", 4);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.accentColor", Theme.PRIMARY_START);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

        DatabaseConnection.initializeUserTable();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TalentForge");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 640);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

                // Color the title bar to match the brand instead of default gray
                frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.PRIMARY_START);
                frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
                // Show the application icon in the title bar (not the Java coffee cup)
                frame.getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, true);

                // Try to load a real icon from resources: /assets/tf_icon.png
                Image loadedIcon = null;
                try (InputStream is = AuthTestApp.class.getResourceAsStream("/assets/tf_icon.png")) {
                    if (is != null) {
                        loadedIcon = ImageIO.read(is);
                    }
                } catch (IOException ignored) {
                }

                if (loadedIcon != null) {
                    frame.setIconImage(loadedIcon);
                } else {
                    // Fallback: Create a simple programmatic TF icon (rounded badge with "TF")
                    java.awt.image.BufferedImage iconImg = new java.awt.image.BufferedImage(64, 64,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics2D ig = iconImg.createGraphics();
                    ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, Theme.PRIMARY_START, 64, 64, Theme.PRIMARY_END);
                    ig.setPaint(gp);
                    ig.fillOval(0, 0, 64, 64);
                    ig.setColor(Color.WHITE);
                    Font f = new Font("Segoe UI", Font.BOLD, 28);
                    ig.setFont(f);
                    FontMetrics fm = ig.getFontMetrics();
                    String s = "TF";
                    int sw = fm.stringWidth(s);
                    int sh = fm.getAscent();
                    ig.drawString(s, (64 - sw) / 2, (64 + sh) / 2 - 2);
                    ig.dispose();
                    frame.setIconImage(iconImg);
                }

            LoginSignupPanel panel = new LoginSignupPanel();
            panel.setOnAuthSuccess((userId, fullName) -> {
                JOptionPane.showMessageDialog(frame,
                        "Logged in as " + fullName + " (user ID: " + userId + ")\n" +
                        "This is where you'd navigate to the Dashboard.");
            });

            frame.add(panel);

            // Show window immediately. Drop fade-in because it conflicts with
            // native OS decorations used for the colored title bar.
            frame.setVisible(true);
        });
    }
}