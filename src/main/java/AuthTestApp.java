import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.talentforge.db.DatabaseConnection;
import com.talentforge.ui.LoginSignupPanel;
import com.talentforge.ui.Theme;

import javax.swing.*;
import java.awt.*;

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
            frame.getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, false);

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