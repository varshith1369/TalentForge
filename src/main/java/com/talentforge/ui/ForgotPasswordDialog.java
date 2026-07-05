package com.talentforge.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.talentforge.auth.AuthService;
import com.talentforge.auth.AuthService.AuthResult;

import javax.swing.*;
import java.awt.*;

/**
 * A modal dialog for resetting a forgotten password.
 * Step 1: enter email, verify an account exists.
 * Step 2: enter and confirm a new password, save it.
 */
public class ForgotPasswordDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private final CardLayout stepLayout = new CardLayout();
    private final JPanel stepContainer = new JPanel(stepLayout);

    private JTextField emailField;
    private JLabel emailErrorLabel;
    private String verifiedEmail;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel resetErrorLabel;

    public ForgotPasswordDialog(Frame owner) {
        super(owner, "Reset Password", true);
        setSize(400, 360);
        setLocationRelativeTo(owner);
        setResizable(false);

        stepContainer.add(buildEmailStep(), "EMAIL");
        stepContainer.add(buildResetStep(), "RESET");
        stepContainer.add(buildDoneStep(), "DONE");

        add(stepContainer);
        stepLayout.show(stepContainer, "EMAIL");
    }

    private JPanel buildEmailStep() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Forgot your password?");
        title.setFont(Theme.FONT_TITLE.deriveFont(20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your account email to continue");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = new JTextField();
        emailField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Email address");
        emailField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.envelope(Theme.MUTED_TEXT));
        emailField.putClientProperty(FlatClientProperties.STYLE, "arc:14; margin:10,10,10,10");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(300, 46));
        emailField.setPreferredSize(new Dimension(300, 46));

        emailErrorLabel = new JLabel(" ");
        emailErrorLabel.setFont(Theme.FONT_LINK);
        emailErrorLabel.setForeground(Theme.ERROR_COLOR);
        emailErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton continueBtn = new JButton("Continue");
        continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueBtn.setMaximumSize(new Dimension(300, 46));
        continueBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        continueBtn.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; background:#6366F1; foreground:#FFFFFF; focusWidth:0; borderWidth:0");
        continueBtn.addActionListener(e -> handleEmailStep());
        getRootPane().setDefaultButton(continueBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setForeground(Theme.MUTED_TEXT);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(22));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(emailErrorLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(continueBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cancelBtn);

        return panel;
    }

    private JPanel buildResetStep() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Set a new password");
        title.setFont(Theme.FONT_TITLE.deriveFont(20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose a password with at least 6 characters");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        newPasswordField = new JPasswordField();
        newPasswordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "New password");
        newPasswordField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.lock(Theme.MUTED_TEXT));
        newPasswordField.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; showRevealButton:true; margin:10,10,10,10");
        newPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        newPasswordField.setMaximumSize(new Dimension(300, 46));
        newPasswordField.setPreferredSize(new Dimension(300, 46));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Confirm password");
        confirmPasswordField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.lock(Theme.MUTED_TEXT));
        confirmPasswordField.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; showRevealButton:true; margin:10,10,10,10");
        confirmPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmPasswordField.setMaximumSize(new Dimension(300, 46));
        confirmPasswordField.setPreferredSize(new Dimension(300, 46));

        resetErrorLabel = new JLabel(" ");
        resetErrorLabel.setFont(Theme.FONT_LINK);
        resetErrorLabel.setForeground(Theme.ERROR_COLOR);
        resetErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton saveBtn = new JButton("Save New Password");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(300, 46));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; background:#6366F1; foreground:#FFFFFF; focusWidth:0; borderWidth:0");
        saveBtn.addActionListener(e -> handleResetStep());

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(22));
        panel.add(newPasswordField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(confirmPasswordField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(resetErrorLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(saveBtn);

        return panel;
    }

    private JPanel buildDoneStep() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 30, 30, 30));

        JLabel checkmark = new JLabel("\u2713");
        checkmark.setFont(new Font("Segoe UI", Font.BOLD, 48));
        checkmark.setForeground(Theme.SUCCESS_COLOR);
        checkmark.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Password updated!");
        title.setFont(Theme.FONT_TITLE.deriveFont(20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 6, 0));

        JLabel subtitle = new JLabel("You can now log in with your new password");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeBtn = new JButton("Back to Log In");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setMaximumSize(new Dimension(300, 46));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; background:#6366F1; foreground:#FFFFFF; focusWidth:0; borderWidth:0");
        closeBtn.addActionListener(e -> dispose());
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(checkmark);
        panel.add(title);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(24));
        panel.add(closeBtn);

        return panel;
    }

    private void handleEmailStep() {
        String email = emailField.getText().trim();
        if (email.isBlank()) {
            emailErrorLabel.setText("Please enter your email.");
            return;
        }
        AuthResult result = authService.emailExists(email);
        if (result.success) {
            verifiedEmail = email;
            emailErrorLabel.setText(" ");
            stepLayout.show(stepContainer, "RESET");
        } else {
            emailErrorLabel.setText(result.message);
        }
    }

    private void handleResetStep() {
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (!newPass.equals(confirmPass)) {
            resetErrorLabel.setText("Passwords do not match.");
            return;
        }

        AuthResult result = authService.resetPassword(verifiedEmail, newPass);
        if (result.success) {
            stepLayout.show(stepContainer, "DONE");
        } else {
            resetErrorLabel.setText(result.message);
        }
    }
}