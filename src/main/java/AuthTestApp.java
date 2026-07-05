import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.talentforge.db.DatabaseConnection;
import com.talentforge.ui.LoginSignupPanel;
import com.talentforge.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class AuthTestApp {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Theme.applyGlobalUIDefaults();

        DatabaseConnection.initializeUserTable();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TalentForge");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(980, 760);
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
            frame.setVisible(true);
        });
    }
}