package com.talentforge.ui;

import com.talentforge.db.ProfileService;
import com.talentforge.db.ProfileService.ProfileData;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * User Profile page showing personal information and profile details
 */
public class ProfilePanel extends JPanel {

    private int userId;
    private String userFullName;
    private String email;
    private String phone;
    private String education;
    private String college;
    private String cgpa;
    private String skills;
    private String location;
    private String bio;

    private JPanel contentPanel;
    private BufferedImage profileImage;
    private JPanel avatarContainer;

    public ProfilePanel(int userId, String userFullName) {
        this.userId = userId;
        this.userFullName = userFullName;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Load profile data from database
        loadProfileData();

        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Profile Header Section
        contentPanel.add(buildProfileHeader(userFullName));
        contentPanel.add(Box.createVerticalStrut(32));

        // Profile Details Section
        contentPanel.add(buildProfileDetailsSection());

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    private void loadProfileData() {
        ProfileData profile = ProfileService.loadProfile(userId);
        this.email = profile.email;
        this.phone = profile.phone;
        this.education = profile.education;
        this.college = profile.college;
        this.cgpa = profile.cgpa;
        this.skills = profile.skills;
        this.location = profile.location;
        this.bio = profile.bio;
    }

    private JPanel buildProfileHeader(String userName) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatarSection = new JPanel();
        avatarSection.setOpaque(false);
        avatarSection.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        avatarSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar with clickable profile picture change
        avatarContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (profileImage != null) {
                    // Draw profile image
                    g2.drawImage(profileImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Draw initials background
                    g2.setColor(Theme.PRIMARY_START);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                    String initials = getInitials(userName);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(initials)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(initials, x, y);
                }

                // Draw camera icon on hover
                boolean isHovered = getMousePosition() != null;
                if (isHovered) {
                    g2.setColor(new Color(0, 0, 0, 100));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    Icon camera = Icons.document(Color.WHITE);
                    camera.paintIcon(this, g2, getWidth() / 2 - 8, getHeight() / 2 - 8);
                }
                g2.dispose();
            }
        };
        avatarContainer.setPreferredSize(new Dimension(80, 80));
        avatarContainer.setMaximumSize(new Dimension(80, 80));
        avatarContainer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add mouse listener for avatar
        avatarContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                avatarContainer.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                avatarContainer.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                changeProfilePicture();
            }
        });

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel statusLabel = new JLabel("Final Year Student");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(Theme.MUTED_TEXT);

        userInfo.add(nameLabel);
        userInfo.add(Box.createVerticalStrut(4));
        userInfo.add(statusLabel);

        avatarSection.add(avatarContainer);
        avatarSection.add(userInfo);

        header.add(avatarSection);
        header.add(Box.createVerticalStrut(20));

        JButton editBtn = new JButton("Edit Profile");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editBtn.setForeground(Color.WHITE);
        editBtn.setBackground(Theme.PRIMARY_START);
        editBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        editBtn.setFocusPainted(false);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        editBtn.addActionListener(e -> editProfile());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.add(editBtn);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(buttonPanel);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        return header;
    }

    private JPanel buildProfileDetailsSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section Title
        JLabel sectionTitle = new JLabel("Personal Information");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setForeground(Theme.PRIMARY_TEXT);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(12));

        // Create profile cards with 8 options
        section.add(createProfileCard("Email", email, Icons.envelope(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("Phone", phone, Icons.bell(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("Education", education, Icons.document(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("College", college, Icons.building(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("CGPA", cgpa, Icons.trophy(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("Skills", skills, Icons.code(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("Location", location, Icons.building(Theme.MUTED_TEXT)));
        section.add(Box.createVerticalStrut(8));

        section.add(createProfileCard("Bio", bio, Icons.document(Theme.MUTED_TEXT)));

        return section;
    }

    private JPanel createProfileCard(String label, String value, Icon icon) {
        ElevatedCard card = new ElevatedCard(null);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setPreferredSize(new Dimension(10, 70));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconPanel.add(iconLabel);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelText.setForeground(Theme.MUTED_TEXT);

        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueText.setForeground(Theme.PRIMARY_TEXT);

        textPanel.add(labelText);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueText);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void editProfile() {
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        editDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        editDialog.setSize(450, 600);
        editDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Edit Your Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(15));

        // Email
        mainPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(email);
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(10));

        // Phone
        mainPanel.add(new JLabel("Phone:"));
        JTextField phoneField = new JTextField(phone);
        mainPanel.add(phoneField);
        mainPanel.add(Box.createVerticalStrut(10));

        // Education
        mainPanel.add(new JLabel("Education:"));
        JTextField educationField = new JTextField(education);
        mainPanel.add(educationField);
        mainPanel.add(Box.createVerticalStrut(10));

        // College
        mainPanel.add(new JLabel("College:"));
        JTextField collegeField = new JTextField(college);
        mainPanel.add(collegeField);
        mainPanel.add(Box.createVerticalStrut(10));

        // CGPA
        mainPanel.add(new JLabel("CGPA:"));
        JTextField cgpaField = new JTextField(cgpa);
        mainPanel.add(cgpaField);
        mainPanel.add(Box.createVerticalStrut(10));

        // Skills
        mainPanel.add(new JLabel("Skills:"));
        JTextField skillsField = new JTextField(skills);
        mainPanel.add(skillsField);
        mainPanel.add(Box.createVerticalStrut(10));

        // Location
        mainPanel.add(new JLabel("Location:"));
        JTextField locationField = new JTextField(location);
        mainPanel.add(locationField);
        mainPanel.add(Box.createVerticalStrut(10));

        // Bio
        mainPanel.add(new JLabel("Bio:"));
        JTextArea bioArea = new JTextArea(bio, 3, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        JScrollPane bioScroll = new JScrollPane(bioArea);
        mainPanel.add(bioScroll);
        mainPanel.add(Box.createVerticalStrut(15));

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(Theme.PRIMARY_START);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            // Update profile data
            email = emailField.getText();
            phone = phoneField.getText();
            education = educationField.getText();
            college = collegeField.getText();
            cgpa = cgpaField.getText();
            skills = skillsField.getText();
            location = locationField.getText();
            bio = bioArea.getText();

            // Save to database
            ProfileData profileData = new ProfileData(email, phone, education, college, cgpa, skills, location, bio);
            if (ProfileService.saveProfile(userId, profileData)) {
                // Refresh the profile display
                refreshProfileDisplay();
                JOptionPane.showMessageDialog(editDialog, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                editDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(editDialog, "Failed to save profile", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveBtn);
        mainPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editDialog.add(scrollPane);
        editDialog.setVisible(true);
    }

    private void refreshProfileDisplay() {
        // Remove old content
        contentPanel.removeAll();

        // Add updated content
        contentPanel.add(buildProfileHeader(userFullName));
        contentPanel.add(Box.createVerticalStrut(32));
        contentPanel.add(buildProfileDetailsSection());

        // Refresh the UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void changeProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Profile Picture");
        
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
            "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(imageFilter);

        int result = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                if (image != null) {
                    // Resize image to fit avatar size (80x80 for profile panel)
                    profileImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = profileImage.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw circular clipped image
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 80, 80));
                    
                    // Calculate dimensions to maintain aspect ratio
                    int imgWidth = image.getWidth();
                    int imgHeight = image.getHeight();
                    float scale = Math.max(80.0f / imgWidth, 80.0f / imgHeight);
                    int scaledWidth = Math.round(imgWidth * scale);
                    int scaledHeight = Math.round(imgHeight * scale);
                    int x = (80 - scaledWidth) / 2;
                    int y = (80 - scaledHeight) / 2;
                    
                    g2.drawImage(image, x, y, scaledWidth, scaledHeight, null);
                    g2.dispose();
                    
                    // Also create a smaller version for the top bar (36x36)
                    BufferedImage topBarImage = new BufferedImage(36, 36, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2TopBar = topBarImage.createGraphics();
                    g2TopBar.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2TopBar.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 36, 36));
                    
                    float scaleTopBar = Math.max(36.0f / imgWidth, 36.0f / imgHeight);
                    int scaledWidthTopBar = Math.round(imgWidth * scaleTopBar);
                    int scaledHeightTopBar = Math.round(imgHeight * scaleTopBar);
                    int xTopBar = (36 - scaledWidthTopBar) / 2;
                    int yTopBar = (36 - scaledHeightTopBar) / 2;
                    
                    g2TopBar.drawImage(image, xTopBar, yTopBar, scaledWidthTopBar, scaledHeightTopBar, null);
                    g2TopBar.dispose();
                    
                    // Update the shared profile cache for top bar
                    UserProfileCache.setProfileImage(topBarImage);
                    
                    // Refresh avatar display in profile panel
                    avatarContainer.repaint();
                    
                    JOptionPane.showMessageDialog(this, "Profile picture updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to load image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "U";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
}
