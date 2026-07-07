package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The main application shell shown after login: sidebar on the left,
 * top bar across the top, and a swappable content area for whichever
 * module is currently selected.
 */
public class MainAppPanel extends JPanel {

    private final Sidebar sidebar;
    private final TopBar topBar;
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentContainer = new JPanel(contentLayout);
    private final Map<String, String> moduleLabels = new HashMap<>();
    private DashboardPanel dashboard;

    public interface OnLogout {
        void onLogout();
    }

    private OnLogout onLogout;

    public MainAppPanel(int userId, String userFullName) {
        // Initialize user cache and load saved stats
        UserProfileCache.setCurrentUserId(userId);
        UserProfileCache.setResumeScore(StatsService.loadResumeScore(userId));

        setLayout(new BorderLayout());

        moduleLabels.put("coding", "Coding Practice");
        moduleLabels.put("aptitude", "Aptitude Practice");
        moduleLabels.put("resume", "Resume Checker");
        moduleLabels.put("interview", "Mock Interview");
        moduleLabels.put("companies", "Company Prep");
        moduleLabels.put("notes", "Notes & Revision");
        moduleLabels.put("planner", "Study Planner");
        moduleLabels.put("skills", "Skill Tracker");
        moduleLabels.put("analytics", "Analytics");
        moduleLabels.put("leaderboard", "Leaderboard");
        moduleLabels.put("settings", "Settings");

        sidebar = new Sidebar("dashboard");
        topBar = new TopBar(userFullName);
        topBar.setOnProfileAction(action -> {
            switch (action) {
                case "logout" -> handleLogout();
                case "settings" -> navigateTo("settings");
                case "profile" -> navigateTo("profile");
            }
        });

        dashboard = new DashboardPanel(userFullName);
        dashboard.setOnModuleClick(this::navigateTo);
        contentContainer.add(dashboard, "dashboard");
        contentContainer.add(new PlaceholderPanel("My Profile"), "profile");

        // Resume Checker — wired with score callback → live dashboard update
        ResumeCheckerPanel resumeChecker = new ResumeCheckerPanel();
        resumeChecker.setOnScoreUpdate(score -> SwingUtilities.invokeLater(() ->
                dashboard.updateResumeScore(score)));
        contentContainer.add(resumeChecker, "resume");
        moduleLabels.remove("resume");

        for (Map.Entry<String, String> entry : moduleLabels.entrySet()) {
            contentContainer.add(new PlaceholderPanel(entry.getValue()), entry.getKey());
        }

        sidebar.setOnNavigate(key -> {
            if (key.equals("logout")) {
                handleLogout();
            } else {
                navigateTo(key);
            }
        });

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.add(topBar, BorderLayout.NORTH);
        rightSide.add(contentContainer, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);
    }

    public void setOnLogout(OnLogout callback) {
        this.onLogout = callback;
    }

    private void handleLogout() {
        UserProfileCache.clearAll();
        if (onLogout != null) onLogout.onLogout();
    }

    private void navigateTo(String key) {
        sidebar.setActive(key);
        contentLayout.show(contentContainer, key);
    }
}