package com.talentforge.ui;

import com.talentforge.db.ProfileService;
import com.talentforge.db.ProfileService.ProfileData;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Rich user profile page for identity, placement readiness, and career links.
 */
public class ProfilePanel extends JPanel {

    private static final Preferences PREFS = Preferences.userNodeForPackage(ProfilePanel.class);

    private static final Color SKY = new Color(14, 165, 233);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color ROSE = new Color(244, 63, 94);
    private static final Color INDIGO = new Color(99, 102, 241);

    private final int userId;
    private final String userFullName;

    private String email;
    private String phone;
    private String education;
    private String college;
    private String cgpa;
    private String skills;
    private String location;
    private String bio;

    private String headline;
    private String targetRole;
    private String availability;
    private String portfolioUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String preferredLocation;
    private String profileVisibility;

    private JPanel contentPanel;
    private BufferedImage profileImage;
    private JPanel avatarContainer;

    public ProfilePanel(int userId, String userFullName) {
        this.userId = userId;
        this.userFullName = userFullName == null || userFullName.isBlank() ? "TalentForge Learner" : userFullName.trim();
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        loadProfileData();
        loadProfileOptions();
        loadProfileImage();
        buildUI();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::refreshProfileDisplay));
    }

    private void loadProfileData() {
        ProfileData profile = ProfileService.loadProfile(userId);
        this.email = clean(profile.email);
        this.phone = clean(profile.phone);
        this.education = clean(profile.education);
        this.college = clean(profile.college);
        this.cgpa = clean(profile.cgpa);
        this.skills = clean(profile.skills);
        this.location = clean(profile.location);
        this.bio = clean(profile.bio);
    }

    private void loadProfileOptions() {
        headline = pref("headline", "Final Year Student");
        targetRole = pref("targetRole", "Software Developer");
        availability = pref("availability", "Open to internships");
        portfolioUrl = pref("portfolioUrl", "");
        linkedinUrl = pref("linkedinUrl", "");
        githubUrl = pref("githubUrl", "");
        preferredLocation = pref("preferredLocation", location.isBlank() ? "Flexible" : location);
        profileVisibility = pref("profileVisibility", "Placement team only");
    }

    private void loadProfileImage() {
        if (userId > 0) {
            profileImage = ProfileService.loadProfileImage(userId);
            if (profileImage != null) {
                UserProfileCache.setProfileImage(makeAvatarImage(profileImage, 36));
            }
        }
    }

    private void buildUI() {
        removeAll();
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        contentPanel.add(buildHeroSection());
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(buildStatsStrip());
        contentPanel.add(Box.createVerticalStrut(18));

        JPanel grid = new JPanel(new GridLayout(1, 2, 18, 0));
        grid.setOpaque(false);

        JPanel left = verticalPanel();
        left.add(buildPersonalSection());
        left.add(Box.createVerticalStrut(16));
        left.add(buildCareerSection());

        JPanel right = verticalPanel();
        right.add(buildReadinessSection());
        right.add(Box.createVerticalStrut(16));
        right.add(buildLinksSection());
        right.add(Box.createVerticalStrut(16));
        right.add(buildBioSection());

        grid.add(left);
        grid.add(right);
        contentPanel.add(grid);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent buildHeroSection() {
        GradientPanel hero = new GradientPanel(new BorderLayout(24, 0), new Color(37, 99, 235), new Color(20, 184, 166));
        hero.setBorder(new EmptyBorder(24, 26, 24, 26));
        hero.setPreferredSize(new Dimension(0, 190));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        avatarContainer = createAvatar(112);

        JPanel copy = verticalPanel();
        JLabel name = new JLabel(userFullName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 30));
        name.setForeground(Color.WHITE);

        JLabel role = new JLabel(headline + "  |  " + targetRole);
        role.setFont(new Font("Segoe UI", Font.BOLD, 13));
        role.setForeground(new Color(229, 246, 255));

        JLabel meta = new JLabel(emptyFallback(location, "Location not added") + "  |  " + availability);
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(new Color(220, 245, 242));

        JLabel bioPreview = new JLabel("<html><body style='width:520px'>" + escapeHtml(shortText(bio, 145)) + "</body></html>");
        bioPreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bioPreview.setForeground(new Color(239, 250, 255));

        copy.add(name);
        copy.add(Box.createVerticalStrut(7));
        copy.add(role);
        copy.add(Box.createVerticalStrut(6));
        copy.add(meta);
        copy.add(Box.createVerticalStrut(14));
        copy.add(bioPreview);

        JPanel left = new JPanel(new BorderLayout(20, 0));
        left.setOpaque(false);
        left.add(avatarContainer, BorderLayout.WEST);
        left.add(copy, BorderLayout.CENTER);

        JPanel actions = verticalPanel();
        actions.setPreferredSize(new Dimension(190, 0));
        JButton editButton = glassButton("Edit Profile");
        editButton.addActionListener(e -> editProfile());
        JButton photoButton = glassButton("Change Photo");
        photoButton.addActionListener(e -> changeProfilePicture());

        actions.add(editButton);
        actions.add(Box.createVerticalStrut(10));
        actions.add(photoButton);
        actions.add(Box.createVerticalStrut(18));
        actions.add(heroMiniStat("Profile", profileCompletionPercent() + "%"));

        hero.add(left, BorderLayout.CENTER);
        hero.add(actions, BorderLayout.EAST);
        return hero;
    }

    private JComponent buildStatsStrip() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        grid.add(metricCard("Profile completion", profileCompletionPercent() + "%", "Identity and career fields", TEAL));
        grid.add(metricCard("Readiness score", readinessScore() + "%", "Resume, practice, and interviews", INDIGO));
        grid.add(metricCard("Skills listed", String.valueOf(skillCount()), "Parsed from skills field", AMBER));
        grid.add(metricCard("Visibility", profileVisibility, "Profile sharing preference", ROSE));
        return grid;
    }

    private JComponent buildPersonalSection() {
        JPanel card = sectionCard("Personal Information", "Core contact and education details.");
        JPanel body = verticalPanel();
        body.add(detailRow("Email", email, Icons.envelope(Theme.MUTED_TEXT), SKY));
        body.add(separator());
        body.add(detailRow("Phone", phone, Icons.bell(Theme.MUTED_TEXT), TEAL));
        body.add(separator());
        body.add(detailRow("Education", education, Icons.document(Theme.MUTED_TEXT), INDIGO));
        body.add(separator());
        body.add(detailRow("College", college, Icons.building(Theme.MUTED_TEXT), AMBER));
        body.add(separator());
        body.add(detailRow("CGPA", cgpa, Icons.trophy(Theme.MUTED_TEXT), ROSE));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildCareerSection() {
        JPanel card = sectionCard("Career Preferences", "Placement-facing profile options.");
        JPanel body = verticalPanel();
        body.add(detailRow("Target Role", targetRole, Icons.code(Theme.MUTED_TEXT), INDIGO));
        body.add(separator());
        body.add(detailRow("Availability", availability, Icons.calendar(Theme.MUTED_TEXT), TEAL));
        body.add(separator());
        body.add(detailRow("Preferred Location", preferredLocation, Icons.building(Theme.MUTED_TEXT), AMBER));
        body.add(separator());
        body.add(detailRow("Profile Visibility", profileVisibility, Icons.eye(Theme.MUTED_TEXT, true), ROSE));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildReadinessSection() {
        JPanel card = sectionCard("Placement Readiness", "Live snapshot from your practice activity.");
        JPanel body = verticalPanel();
        body.add(progressRow("Resume", UserProfileCache.getResumeScore(), ROSE));
        body.add(Box.createVerticalStrut(12));
        body.add(progressRow("Aptitude", UserProfileCache.getAptitudeScore(), INDIGO));
        body.add(Box.createVerticalStrut(12));
        body.add(progressRow("Coding", Math.min(100, UserProfileCache.getProblemsSolved() * 4), SKY));
        body.add(Box.createVerticalStrut(12));
        body.add(progressRow("Interviews", Math.min(100, UserProfileCache.getMockInterviews() * 10), TEAL));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildLinksSection() {
        JPanel card = sectionCard("Career Links", "Useful links for recruiters and mentors.");
        JPanel body = verticalPanel();
        body.add(detailRow("Portfolio", portfolioUrl, Icons.document(Theme.MUTED_TEXT), SKY));
        body.add(separator());
        body.add(detailRow("LinkedIn", linkedinUrl, Icons.peopleGroup(Theme.MUTED_TEXT), INDIGO));
        body.add(separator());
        body.add(detailRow("GitHub", githubUrl, Icons.code(Theme.MUTED_TEXT), TEAL));
        body.add(separator());
        body.add(detailRow("Skills", skills, Icons.brain(Theme.MUTED_TEXT), AMBER));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildBioSection() {
        JPanel card = sectionCard("Profile Summary", "Short introduction shown on your profile.");
        JTextArea bioArea = new JTextArea(emptyFallback(bio, "No profile summary added yet."));
        bioArea.setEditable(false);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setOpaque(false);
        bioArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bioArea.setForeground(Theme.PRIMARY_TEXT);
        bioArea.setBorder(new EmptyBorder(2, 0, 0, 0));
        card.add(bioArea, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAvatar(int size) {
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));

                if (profileImage != null) {
                    g2.drawImage(makeAvatarImage(profileImage, size), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, INDIGO, getWidth(), getHeight(), TEAL));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
                    String initials = getInitials(userFullName);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(initials)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(initials, x, y);
                }

                if (getMousePosition() != null) {
                    g2.setColor(new Color(0, 0, 0, 105));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    Icons.document(Color.WHITE).paintIcon(this, g2, getWidth() / 2 - 9, getHeight() / 2 - 9);
                }
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(size, size));
        avatar.setMaximumSize(new Dimension(size, size));
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { avatar.repaint(); }
            @Override public void mouseExited(MouseEvent e) { avatar.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { changeProfilePicture(); }
        });
        return avatar;
    }

    private void editProfile() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(680, 720);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.PAGE_BG);
        root.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Edit Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        JLabel subtitle = new JLabel("Update contact, career, and recruiter-facing profile options.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);
        JPanel copy = verticalPanel();
        copy.add(title);
        copy.add(Box.createVerticalStrut(3));
        copy.add(subtitle);
        header.add(copy, BorderLayout.CENTER);

        JPanel fields = new JPanel(new GridLayout(0, 2, 14, 12));
        fields.setOpaque(false);

        JTextField emailField = field(email);
        JTextField phoneField = field(phone);
        JTextField educationField = field(education);
        JTextField collegeField = field(college);
        JTextField cgpaField = field(cgpa);
        JTextField skillsField = field(skills);
        JTextField locationField = field(location);
        JTextField headlineField = field(headline);
        JTextField portfolioField = field(portfolioUrl);
        JTextField linkedinField = field(linkedinUrl);
        JTextField githubField = field(githubUrl);
        JTextField preferredLocationField = field(preferredLocation);

        JComboBox<String> roleCombo = combo(new String[]{"Software Developer", "Data Analyst", "Product Analyst", "QA Engineer", "Cloud Engineer", "UI/UX Designer"}, targetRole);
        JComboBox<String> availabilityCombo = combo(new String[]{"Open to internships", "Open to full-time", "Open to both", "Not looking right now"}, availability);
        JComboBox<String> visibilityCombo = combo(new String[]{"Placement team only", "Mentors and placement team", "Public inside TalentForge"}, profileVisibility);

        fields.add(fieldBlock("Headline", headlineField));
        fields.add(fieldBlock("Target Role", roleCombo));
        fields.add(fieldBlock("Email", emailField));
        fields.add(fieldBlock("Phone", phoneField));
        fields.add(fieldBlock("Education", educationField));
        fields.add(fieldBlock("College", collegeField));
        fields.add(fieldBlock("CGPA", cgpaField));
        fields.add(fieldBlock("Location", locationField));
        fields.add(fieldBlock("Availability", availabilityCombo));
        fields.add(fieldBlock("Preferred Location", preferredLocationField));
        fields.add(fieldBlock("Portfolio URL", portfolioField));
        fields.add(fieldBlock("LinkedIn URL", linkedinField));
        fields.add(fieldBlock("GitHub URL", githubField));
        fields.add(fieldBlock("Visibility", visibilityCombo));
        fields.add(fieldBlock("Skills", skillsField));

        JTextArea bioArea = new JTextArea(bio, 5, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bioArea.setForeground(Theme.PRIMARY_TEXT);
        bioArea.setBackground(Theme.FIELD_BG);
        bioArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true));

        JPanel form = verticalPanel();
        form.add(fields);
        form.add(Box.createVerticalStrut(14));
        form.add(fieldBlock("Bio", bioScroll));

        JButton cancelButton = outlineButton("Cancel", Theme.MUTED_TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        JButton saveButton = gradientButton("Save Changes", 150, 40);
        saveButton.addActionListener(e -> {
            email = emailField.getText().trim();
            phone = phoneField.getText().trim();
            education = educationField.getText().trim();
            college = collegeField.getText().trim();
            cgpa = cgpaField.getText().trim();
            skills = skillsField.getText().trim();
            location = locationField.getText().trim();
            bio = bioArea.getText().trim();
            headline = headlineField.getText().trim();
            targetRole = selectedText(roleCombo);
            availability = selectedText(availabilityCombo);
            portfolioUrl = portfolioField.getText().trim();
            linkedinUrl = linkedinField.getText().trim();
            githubUrl = githubField.getText().trim();
            preferredLocation = preferredLocationField.getText().trim();
            profileVisibility = selectedText(visibilityCombo);

            ProfileData profileData = new ProfileData(email, phone, education, college, cgpa, skills, location, bio);
            if (userId > 0 && ProfileService.saveProfile(userId, profileData)) {
                saveProfileOptions();
                refreshProfileDisplay();
                JOptionPane.showMessageDialog(dialog, "Profile updated successfully.", "Profile Saved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else if (userId <= 0) {
                JOptionPane.showMessageDialog(dialog, "Please log in before saving profile changes.", "Profile Not Saved", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancelButton);
        actions.add(saveButton);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        root.add(header, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        dialog.add(root);
        dialog.setVisible(true);
    }

    private void refreshProfileDisplay() {
        setBackground(Theme.PAGE_BG);
        buildUI();
    }

    private void changeProfilePicture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Profile Picture");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));

        int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) {
                JOptionPane.showMessageDialog(this, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            profileImage = makeAvatarImage(image, 160);
            BufferedImage topBarImage = makeAvatarImage(image, 36);
            UserProfileCache.setProfileImage(topBarImage);

            if (userId > 0 && !ProfileService.saveProfileImage(userId, profileImage)) {
                JOptionPane.showMessageDialog(this, "Image updated locally, but could not be saved.", "Image Save Warning", JOptionPane.WARNING_MESSAGE);
            }

            if (avatarContainer != null) {
                avatarContainer.repaint();
            }
            JOptionPane.showMessageDialog(this, "Profile picture updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProfileOptions() {
        putPref("headline", headline);
        putPref("targetRole", targetRole);
        putPref("availability", availability);
        putPref("portfolioUrl", portfolioUrl);
        putPref("linkedinUrl", linkedinUrl);
        putPref("githubUrl", githubUrl);
        putPref("preferredLocation", preferredLocation);
        putPref("profileVisibility", profileVisibility);
    }

    private JPanel sectionCard(String title, String subtitle) {
        JPanel card = new SurfacePanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel header = verticalPanel();
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Theme.PRIMARY_TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Theme.MUTED_TEXT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JComponent detailRow(String label, String value, Icon icon, Color accent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setPreferredSize(new Dimension(28, 28));
        row.add(iconLabel, BorderLayout.WEST);

        JPanel text = verticalPanel();
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Theme.MUTED_TEXT);
        JLabel body = new JLabel("<html><body style='width:360px'>" + escapeHtml(emptyFallback(value, "Not added")) + "</body></html>");
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        body.setForeground(value == null || value.isBlank() ? Theme.MUTED_TEXT : Theme.PRIMARY_TEXT);
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(body);

        JPanel marker = new JPanel();
        marker.setBackground(accent);
        marker.setPreferredSize(new Dimension(4, 36));
        row.add(text, BorderLayout.CENTER);
        row.add(marker, BorderLayout.EAST);
        return row;
    }

    private JComponent progressRow(String label, int value, Color color) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(Theme.PRIMARY_TEXT);
        name.setPreferredSize(new Dimension(84, 24));
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(Math.max(0, Math.min(100, value)));
        bar.setStringPainted(true);
        bar.setString(Math.max(0, Math.min(100, value)) + "%");
        bar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bar.setForeground(color);
        bar.setBackground(Theme.isDark() ? new Color(48, 50, 64) : new Color(229, 231, 235));
        bar.setBorderPainted(false);
        row.add(name, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    private JPanel metricCard(String title, String value, String caption, Color accent) {
        JPanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Theme.MUTED_TEXT);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        captionLabel.setForeground(Theme.MUTED_TEXT);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLabel, BorderLayout.SOUTH);
        return card;
    }

    private JComponent heroMiniStat(String label, String value) {
        JPanel stat = new JPanel(new BorderLayout(0, 4));
        stat.setOpaque(false);
        stat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLabel.setForeground(new Color(230, 250, 255));
        stat.add(valueLabel, BorderLayout.CENTER);
        stat.add(labelLabel, BorderLayout.SOUTH);
        return stat;
    }

    private JPanel fieldBlock(String label, JComponent field) {
        JPanel panel = verticalPanel();
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Theme.PRIMARY_TEXT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private JTextField field(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(Theme.PRIMARY_TEXT);
        field.setBackground(Theme.FIELD_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER),
                new EmptyBorder(9, 11, 9, 11)));
        return field;
    }

    private JComboBox<String> combo(String[] values, String selectedValue) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setSelectedItem(selectedValue);
        return combo;
    }

    private JButton gradientButton(String text, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton outlineButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(color);
        button.setBackground(Theme.PANEL_BG);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER),
                new EmptyBorder(9, 16, 9, 16)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton glassButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 44));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JComponent separator() {
        JPanel line = new JPanel();
        line.setBackground(Theme.isDark() ? new Color(58, 61, 77) : new Color(232, 235, 242));
        line.setPreferredSize(new Dimension(1, 1));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 0, 12, 0));
        wrapper.add(line, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private int profileCompletionPercent() {
        List<String> values = new ArrayList<>();
        values.add(email);
        values.add(phone);
        values.add(education);
        values.add(college);
        values.add(cgpa);
        values.add(skills);
        values.add(location);
        values.add(bio);
        values.add(headline);
        values.add(targetRole);
        values.add(availability);
        values.add(portfolioUrl);
        values.add(linkedinUrl);
        values.add(githubUrl);
        int filled = 0;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                filled++;
            }
        }
        return Math.round(filled * 100f / values.size());
    }

    private int readinessScore() {
        int resume = UserProfileCache.getResumeScore();
        int aptitude = UserProfileCache.getAptitudeScore();
        int coding = Math.min(100, UserProfileCache.getProblemsSolved() * 4);
        int interviews = Math.min(100, UserProfileCache.getMockInterviews() * 10);
        return Math.round((resume + aptitude + coding + interviews) / 4f);
    }

    private int skillCount() {
        if (skills == null || skills.isBlank()) return 0;
        return skills.split("\\s*,\\s*").length;
    }

    private BufferedImage makeAvatarImage(BufferedImage source, int size) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        int imageWidth = source.getWidth();
        int imageHeight = source.getHeight();
        float scale = Math.max((float) size / imageWidth, (float) size / imageHeight);
        int scaledWidth = Math.round(imageWidth * scale);
        int scaledHeight = Math.round(imageHeight * scale);
        int x = (size - scaledWidth) / 2;
        int y = (size - scaledHeight) / 2;
        g2.drawImage(source, x, y, scaledWidth, scaledHeight, null);
        g2.dispose();
        return output;
    }

    private String pref(String key, String fallback) {
        return PREFS.get("profile." + userId + "." + key, fallback);
    }

    private void putPref(String key, String value) {
        PREFS.put("profile." + userId + "." + key, value == null ? "" : value);
    }

    private String selectedText(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "U";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                initials.append(part.charAt(0));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.toString().toUpperCase();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String shortText(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "Add a short profile summary so mentors and placement teams understand your goals.";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class SurfacePanel extends JPanel {
        SurfacePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(Theme.isDark() ? new Color(58, 61, 77) : new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(LayoutManager layout, Color start, Color end) {
            super(layout);
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
