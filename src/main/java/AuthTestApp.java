import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.talentforge.db.DatabaseConnection;
import com.talentforge.db.ProfileService;
import com.talentforge.db.StatsService;
import com.talentforge.ui.LoginSignupPanel;
import com.talentforge.ui.MainAppPanel;
import com.talentforge.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class AuthTestApp {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Theme.applyGlobalUIDefaults();

        DatabaseConnection.initializeUserTable();
        ProfileService.initializeProfileTable();
        StatsService.initializeStatsTable();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TalentForge");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(980, 760);
            frame.setLocationRelativeTo(null);

            // Color the title bar to match the brand instead of default gray
            frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.PRIMARY_START);
            frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
            frame.getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, false);

            showLoginScreen(frame);
            frame.setVisible(true);
        });
    }

    private static void showLoginScreen(JFrame frame) {
        frame.setResizable(false);
        frame.setSize(980, 760);
        frame.setLocationRelativeTo(null);

        LoginSignupPanel loginPanel = new LoginSignupPanel();
        loginPanel.setOnAuthSuccess((userId, fullName) -> showDashboard(frame, userId, fullName));

        frame.getContentPane().removeAll();
        frame.add(loginPanel);
        frame.revalidate();
        frame.repaint();
    }

    private static void showDashboard(JFrame frame, int userId, String fullName) {
        frame.setResizable(true);
        frame.setSize(1200, 780);
        frame.setLocationRelativeTo(null);

        MainAppPanel mainApp = new MainAppPanel(userId, fullName);
        mainApp.setOnLogout(() -> showLoginScreen(frame));

        frame.getContentPane().removeAll();
        frame.add(mainApp);
        frame.revalidate();
        frame.repaint();
    }
}