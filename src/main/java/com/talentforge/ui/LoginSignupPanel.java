package com.talentforge.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.talentforge.auth.AuthService;
import com.talentforge.auth.AuthService.AuthResult;
import com.talentforge.auth.GoogleAuthService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A pristine, ultra-premium split-screen Login/Signup interface.
 * Left side: Vibrant animated gradient branding.
 * Right side: Crisp, pure white form with modern light-gray inputs.
 */
public class LoginSignupPanel extends JPanel {

    private final CrossFadePanel crossFade = new CrossFadePanel();
    private final AuthService authService = new AuthService();

    private JButton loginTab, signupTab, forgotTab;

    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JLabel loginErrorLabel;
    private JButton loginSubmitBtn;

    private JTextField signupNameField;
    private JTextField signupEmailField;
    private JPasswordField signupPasswordField;
    private JLabel signupErrorLabel;
    private JButton signupSubmitBtn;

    private JPanel loginFormPanel;
    private JPanel signupFormPanel;

    // Forgot password inline state
    private CardLayout forgotStepLayout;
    private JPanel forgotStepContainer;
    private JTextField forgotEmailField;
    private JLabel forgotEmailError;
    private String forgotVerifiedEmail;
    private JPasswordField forgotNewPassword;
    private JPasswordField forgotConfirmPassword;
    private JLabel forgotResetError;

    private OnAuthSuccess onAuthSuccess;

    public interface OnAuthSuccess {
        void onSuccess(int userId, String fullName);
    }

    public void setOnAuthSuccess(OnAuthSuccess callback) {
        this.onAuthSuccess = callback;
    }

    public LoginSignupPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        // 1. Left Side - Animated Branding
        LeftBrandingPanel branding = new LeftBrandingPanel();
        add(branding, BorderLayout.WEST);

        // 2. Right Side - Pure White Form
        JPanel rightSide = new JPanel(new GridBagLayout());
        rightSide.setOpaque(true);
        rightSide.setBackground(Color.WHITE);

        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        formContainer.add(buildTabHeader());
        formContainer.add(Box.createVerticalStrut(32));

        crossFade.addCard(buildLoginCard(), "LOGIN");
        crossFade.addCard(buildSignupCard(), "SIGNUP");
        crossFade.addCard(buildForgotCard(), "FORGOT");
        crossFade.setPreferredSize(new Dimension(360, 460));
        crossFade.setOpaque(false);
        formContainer.add(crossFade);

        rightSide.add(formContainer);
        add(rightSide, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> showSection("LOGIN"));
    }

    // ---------------- FORM TABS ----------------

    private JPanel buildTabHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        header.setOpaque(false);

        loginTab = tabButton("Log In", Icons.loginArrow(Theme.MUTED_TEXT));
        signupTab = tabButton("Sign Up", Icons.userPlus(Theme.MUTED_TEXT));
        forgotTab = tabButton("Forgot", Icons.lock(Theme.MUTED_TEXT));

        loginTab.addActionListener(e -> showSection("LOGIN"));
        signupTab.addActionListener(e -> showSection("SIGNUP"));
        forgotTab.addActionListener(e -> showSection("FORGOT"));

        header.add(loginTab);
        header.add(signupTab);
        header.add(forgotTab);
        return header;
    }

    private JButton tabButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFont(Theme.FONT_BUTTON.deriveFont(13f));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(6);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(108, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showSection(String section) {
        styleTab(loginTab, section.equals("LOGIN"), Icons.loginArrow(section.equals("LOGIN") ? Color.WHITE : Theme.MUTED_TEXT));
        styleTab(signupTab, section.equals("SIGNUP"), Icons.userPlus(section.equals("SIGNUP") ? Color.WHITE : Theme.MUTED_TEXT));
        styleTab(forgotTab, section.equals("FORGOT"), Icons.lock(section.equals("FORGOT") ? Color.WHITE : Theme.MUTED_TEXT));
        crossFade.showCard(section);
    }

    private void styleTab(JButton btn, boolean active, Icon icon) {
        btn.setIcon(icon);
        if (active) {
            btn.setBackground(new Color(99, 102, 241)); // Indigo 500
            btn.setForeground(Color.WHITE);
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc:20; background:#6366F1; foreground:#FFFFFF; borderWidth:0; focusWidth:0");
        } else {
            btn.setBackground(new Color(243, 244, 246)); // Gray 100
            btn.setForeground(new Color(107, 114, 128)); // Gray 500
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc:20; background:#F3F4F6; foreground:#6B7280; borderWidth:0; focusWidth:0");
        }
        btn.repaint();
    }

    // ---------------- LOGIN ----------------

    private JPanel buildLoginCard() {
        JPanel form = new JPanel();
        loginFormPanel = form;
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome Back", Icons.wave(new Color(251, 191, 36)), SwingConstants.LEFT);
        title.setFont(Theme.FONT_TITLE.deriveFont(26f));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setIconTextGap(10);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Log in to continue your placement journey");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 32, 0));

        loginEmailField = styledField("Email address", Icons.envelope(Theme.MUTED_TEXT));
        loginEmailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        attachEmailValidation(loginEmailField);

        loginPasswordField = styledPasswordField();
        loginPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setFont(Theme.FONT_LINK);
        loginErrorLabel.setForeground(Theme.ERROR_COLOR);
        loginErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        optionsRow.setMaximumSize(new Dimension(320, 26));
        optionsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setOpaque(false);
        rememberMe.setFont(Theme.FONT_LINK);
        rememberMe.setForeground(Theme.MUTED_TEXT);
        rememberMe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton forgotPassword = linkButton("Forgot password?", Theme.PRIMARY_START);
        forgotPassword.addActionListener(e -> showSection("FORGOT"));
        optionsRow.add(rememberMe, BorderLayout.WEST);
        optionsRow.add(forgotPassword, BorderLayout.EAST);

        loginSubmitBtn = primaryButton("Log In");
        loginSubmitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginSubmitBtn.addActionListener(e -> handleLogin());
        loginEmailField.addActionListener(e -> handleLogin());
        loginPasswordField.addActionListener(e -> handleLogin());

        form.add(title);
        form.add(subtitle);
        form.add(loginEmailField);
        form.add(Box.createVerticalStrut(16));
        form.add(loginPasswordField);
        form.add(Box.createVerticalStrut(12));
        form.add(optionsRow);
        form.add(Box.createVerticalStrut(8));
        form.add(loginErrorLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(loginSubmitBtn);
        form.add(Box.createVerticalStrut(20));
        form.add(buildOrDivider());
        form.add(Box.createVerticalStrut(16));
        form.add(googleButton());

        return form;
    }

    // ---------------- SIGNUP ----------------

    private JPanel buildSignupCard() {
        JPanel form = new JPanel();
        signupFormPanel = form;
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create Account");
        title.setFont(Theme.FONT_TITLE.deriveFont(26f));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Start building your placement readiness");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 28, 0));

        signupNameField = styledField("Full name", Icons.user(Theme.MUTED_TEXT));
        signupNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupEmailField = styledField("Email address", Icons.envelope(Theme.MUTED_TEXT));
        signupEmailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        attachEmailValidation(signupEmailField);

        signupPasswordField = styledPasswordField();
        signupPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupErrorLabel = new JLabel(" ");
        signupErrorLabel.setFont(Theme.FONT_LINK);
        signupErrorLabel.setForeground(Theme.ERROR_COLOR);
        signupErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupSubmitBtn = primaryButton("Create Account");
        signupSubmitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupSubmitBtn.addActionListener(e -> handleSignup());

        JLabel terms = new JLabel("By signing up, you agree to our Terms & Privacy Policy");
        terms.setFont(Theme.FONT_LINK.deriveFont(11f));
        terms.setForeground(Theme.MUTED_TEXT);
        terms.setAlignmentX(Component.CENTER_ALIGNMENT);

        form.add(title);
        form.add(subtitle);
        form.add(signupNameField);
        form.add(Box.createVerticalStrut(14));
        form.add(signupEmailField);
        form.add(Box.createVerticalStrut(14));
        form.add(signupPasswordField);
        form.add(Box.createVerticalStrut(8));
        form.add(signupErrorLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(signupSubmitBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(terms);
        form.add(Box.createVerticalStrut(16));
        form.add(buildOrDivider());
        form.add(Box.createVerticalStrut(16));
        form.add(googleButton());

        return form;
    }

    // ---------------- FORGOT PASSWORD ----------------

    private JPanel buildForgotCard() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        forgotStepLayout = new CardLayout();
        forgotStepContainer = new JPanel(forgotStepLayout);
        forgotStepContainer.setOpaque(false);

        forgotStepContainer.add(buildForgotEmailStep(), "EMAIL");
        forgotStepContainer.add(buildForgotResetStep(), "RESET");
        forgotStepContainer.add(buildForgotDoneStep(), "DONE");

        outer.add(forgotStepContainer, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildForgotEmailStep() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Forgot password?");
        title.setFont(Theme.FONT_TITLE.deriveFont(24f));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your account email to continue");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 32, 0));

        forgotEmailField = styledField("Email address", Icons.envelope(Theme.MUTED_TEXT));
        forgotEmailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        forgotEmailError = new JLabel(" ");
        forgotEmailError.setFont(Theme.FONT_LINK);
        forgotEmailError.setForeground(Theme.ERROR_COLOR);
        forgotEmailError.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton continueBtn = primaryButton("Continue");
        continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueBtn.addActionListener(e -> handleForgotEmailStep());

        form.add(title);
        form.add(subtitle);
        form.add(forgotEmailField);
        form.add(Box.createVerticalStrut(8));
        form.add(forgotEmailError);
        form.add(Box.createVerticalStrut(16));
        form.add(continueBtn);

        return form;
    }

    private JPanel buildForgotResetStep() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Set new password");
        title.setFont(Theme.FONT_TITLE.deriveFont(24f));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("At least 6 characters");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 32, 0));

        forgotNewPassword = styledPasswordField();
        forgotNewPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "New password");
        forgotNewPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        forgotConfirmPassword = styledPasswordField();
        forgotConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Confirm password");
        forgotConfirmPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        forgotResetError = new JLabel(" ");
        forgotResetError.setFont(Theme.FONT_LINK);
        forgotResetError.setForeground(Theme.ERROR_COLOR);
        forgotResetError.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton saveBtn = primaryButton("Save Password");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> handleForgotResetStep());

        form.add(title);
        form.add(subtitle);
        form.add(forgotNewPassword);
        form.add(Box.createVerticalStrut(14));
        form.add(forgotConfirmPassword);
        form.add(Box.createVerticalStrut(8));
        form.add(forgotResetError);
        form.add(Box.createVerticalStrut(16));
        form.add(saveBtn);

        return form;
    }

    private JPanel buildForgotDoneStep() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JLabel checkmark = new JLabel(Icons.checkCircleFilled(Theme.SUCCESS_COLOR, Color.WHITE));
        checkmark.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Password updated!");
        title.setFont(Theme.FONT_TITLE.deriveFont(24f));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 6, 0));

        JLabel subtitle = new JLabel("You can now log in with your new password");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backBtn = primaryButton("Back to Log In");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        backBtn.addActionListener(e -> showSection("LOGIN"));

        form.add(checkmark);
        form.add(title);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(30));
        form.add(backBtn);

        return form;
    }

    private void handleForgotEmailStep() {
        String email = forgotEmailField.getText().trim();
        if (email.isBlank()) {
            forgotEmailError.setText("Please enter your email.");
            return;
        }
        AuthResult result = authService.emailExists(email);
        if (result.success) {
            forgotVerifiedEmail = email;
            forgotEmailError.setText(" ");
            forgotStepLayout.show(forgotStepContainer, "RESET");
        } else {
            forgotEmailError.setText(result.message);
        }
    }

    private void handleForgotResetStep() {
        String newPass = new String(forgotNewPassword.getPassword());
        String confirmPass = new String(forgotConfirmPassword.getPassword());

        if (!newPass.equals(confirmPass)) {
            forgotResetError.setText("Passwords do not match.");
            return;
        }
        AuthResult result = authService.resetPassword(forgotVerifiedEmail, newPass);
        if (result.success) {
            forgotStepLayout.show(forgotStepContainer, "DONE");
        } else {
            forgotResetError.setText(result.message);
        }
    }

    // ---------------- Shared UI builders ----------------

    private JPanel buildOrDivider() {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(320, 20));
        row.setPreferredSize(new Dimension(320, 20));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComponent lineLeft = thinLine();
        JComponent lineRight = thinLine();

        JLabel text = new JLabel("or continue with", SwingConstants.CENTER);
        text.setFont(Theme.FONT_LINK.deriveFont(11f));
        text.setForeground(Theme.MUTED_TEXT);

        row.add(lineLeft, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(lineRight, BorderLayout.EAST);
        return row;
    }

    private JComponent thinLine() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(229, 231, 235)); // Gray 200
                int midY = getHeight() / 2;
                g.drawLine(0, midY, getWidth(), midY);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(100, 20));
        return line;
    }

    private JButton googleButton() {
        JButton btn = new JButton("Continue with Google", Icons.googleG());
        btn.setFont(Theme.FONT_BUTTON.deriveFont(14f));
        btn.setIconTextGap(12);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc:18; background:#FFFFFF; foreground:#111827; borderColor:#D1D5DB; borderWidth:1; focusWidth:0; " +
                "hoverBackground:#F9FAFB; pressedBackground:#F3F4F6;");
        btn.addActionListener(e -> handleGoogleSignIn(btn));
        return btn;
    }

    private void handleGoogleSignIn(JButton btn) {
        String originalText = btn.getText();
        btn.setText("Waiting...");
        btn.setEnabled(false);

        GoogleAuthService googleAuth = new GoogleAuthService();
        googleAuth.signIn()
                .thenAccept(googleUser -> SwingUtilities.invokeLater(() -> {
                    btn.setText(originalText);
                    btn.setEnabled(true);
                    AuthResult result = authService.loginOrCreateGoogleUser(googleUser.email, googleUser.name);
                    if (result.success && onAuthSuccess != null) {
                        onAuthSuccess.onSuccess(result.userId, result.fullName);
                    } else if (!result.success) {
                        JOptionPane.showMessageDialog(this, "Google sign-in failed: " + result.message);
                    }
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        btn.setText(originalText);
                        btn.setEnabled(true);
                        JOptionPane.showMessageDialog(this, ex.getCause() != null
                                ? ex.getCause().getMessage() : ex.getMessage());
                    });
                    return null;
                });
    }

    private void attachEmailValidation(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { check(); }
            public void removeUpdate(DocumentEvent e) { check(); }
            public void changedUpdate(DocumentEvent e) { check(); }

            private void check() {
                String text = field.getText();
                boolean looksValid = text.contains("@") && text.indexOf('@') < text.lastIndexOf('.')
                        && text.lastIndexOf('.') < text.length() - 1;
                field.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                        looksValid ? Icons.checkCircleFilled(new Color(16, 185, 129), Color.WHITE) : null);
            }
        });
    }

    private JTextField styledField(String placeholder, Icon leadingIcon) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, leadingIcon);
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc:18; background:#F9FAFB; foreground:#111827; caretColor:#111827; " +
                "borderColor:#E5E7EB; focusedBorderColor:#6366F1; borderWidth:1; focusWidth:3; innerFocusWidth:0; margin:10,14,10,14");
        field.setFont(Theme.FONT_FIELD.deriveFont(14f));
        field.setMaximumSize(new Dimension(320, 48));
        field.setPreferredSize(new Dimension(320, 48));
        return field;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.lock(Theme.MUTED_TEXT));
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc:18; showRevealButton:true; background:#F9FAFB; foreground:#111827; caretColor:#111827; " +
                "borderColor:#E5E7EB; focusedBorderColor:#6366F1; borderWidth:1; focusWidth:3; innerFocusWidth:0; margin:10,14,10,14");
        field.setFont(Theme.FONT_FIELD.deriveFont(14f));
        field.setMaximumSize(new Dimension(320, 48));
        field.setPreferredSize(new Dimension(320, 48));
        return field;
    }

    private JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BUTTON.deriveFont(15f));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc:18; background:#6366F1; foreground:#FFFFFF; focusWidth:0; "
                        + "hoverBackground:#4F46E5; pressedBackground:#4338CA; borderWidth:0");
        return btn;
    }

    private JButton linkButton(String text, Color c) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_LINK);
        btn.setForeground(c);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    // ---------------- Auth handlers ----------------

    private Timer loginSpinnerTimer;
    private Timer signupSpinnerTimer;

    private void setLoading(JButton btn, boolean loading, String loadingText, String normalText) {
        if (loading) {
            Icons.SpinnerIcon spinner = new Icons.SpinnerIcon(Color.WHITE);
            btn.setIcon(spinner);
            btn.setText(loadingText);
            btn.setEnabled(false);
            Timer t = new Timer(45, e -> {
                spinner.advance();
                btn.repaint();
            });
            t.start();
            if (btn == loginSubmitBtn) loginSpinnerTimer = t; else signupSpinnerTimer = t;
        } else {
            Timer t = (btn == loginSubmitBtn) ? loginSpinnerTimer : signupSpinnerTimer;
            if (t != null) t.stop();
            btn.setIcon(null);
            btn.setText(normalText);
            btn.setEnabled(true);
        }
    }

    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        setLoading(loginSubmitBtn, true, "Logging in...", "Log In");
        Timer delay = new Timer(450, null);
        delay.setRepeats(false);
        delay.addActionListener(e -> {
            AuthResult result = authService.login(email, password);
            setLoading(loginSubmitBtn, false, "Logging in...", "Log In");
            if (result.success) {
                loginErrorLabel.setText(" ");
                if (onAuthSuccess != null) {
                    onAuthSuccess.onSuccess(result.userId, result.fullName);
                }
            } else {
                loginErrorLabel.setText(result.message);
                ShakeAnimator.shake(loginFormPanel);
            }
        });
        delay.start();
    }

    private void handleSignup() {
        String name = signupNameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = new String(signupPasswordField.getPassword());

        setLoading(signupSubmitBtn, true, "Creating account...", "Create Account");
        Timer delay = new Timer(450, null);
        delay.setRepeats(false);
        delay.addActionListener(e -> {
            AuthResult result = authService.signup(name, email, password);
            setLoading(signupSubmitBtn, false, "Creating account...", "Create Account");
            if (result.success) {
                signupErrorLabel.setForeground(new Color(16, 185, 129)); // Green 500
                signupErrorLabel.setText("Account created! You can now log in.");
                signupNameField.setText("");
                signupEmailField.setText("");
                signupPasswordField.setText("");
            } else {
                signupErrorLabel.setForeground(Theme.ERROR_COLOR);
                signupErrorLabel.setText(result.message);
                ShakeAnimator.shake(signupFormPanel);
            }
        });
        delay.start();
    }

    // ---------------- Left Branding Panel (Animated) ----------------

    private static class LeftBrandingPanel extends JPanel {
        private List<FloatingOrb> orbs;
        private Timer animTimer;

        public LeftBrandingPanel() {
            setPreferredSize(new Dimension(460, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(60, 50, 60, 50));
            initOrbs();

            // 1. Custom Drawn Logo
            LogoIcon logo = new LogoIcon();
            logo.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(logo);
            add(Box.createVerticalStrut(24));

            // 2. Title & Subtitle
            JLabel appName = new JLabel("TalentForge");
            appName.setFont(new Font("Segoe UI", Font.BOLD, 42));
            appName.setForeground(Color.WHITE);
            appName.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel subtitle = new JLabel("AI Powered Placement Platform");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            subtitle.setForeground(new Color(255, 255, 255, 200));
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 32, 0));

            add(appName);
            add(subtitle);

            // Tags
            JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
            tags.setOpaque(false);
            tags.setAlignmentX(Component.LEFT_ALIGNMENT);
            tags.add(tagLabel("Prepare"));
            tags.add(dotLabel());
            tags.add(tagLabel("Practice"));
            tags.add(dotLabel());
            tags.add(tagLabel("Get Hired"));
            
            add(tags);
            add(Box.createVerticalStrut(40));

            // 3. Feature Checklist (fills the middle gap)
            add(buildFeatureList());

            add(Box.createVerticalGlue());

            // 4. Stats Row
            JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
            stats.setOpaque(false);
            stats.setAlignmentX(Component.LEFT_ALIGNMENT);
            stats.setMaximumSize(new Dimension(400, 70));
            stats.add(miniStat("1,200+", "Students"));
            stats.add(miniStat("3,500+", "Problems"));
            stats.add(miniStat("95%", "Hired"));
            
            add(stats);

            animTimer = new Timer(30, e -> {
                for (FloatingOrb o : orbs) o.update(getWidth(), getHeight());
                repaint();
            });
            animTimer.start();
        }

        private JPanel buildFeatureList() {
            JPanel list = new JPanel();
            list.setOpaque(false);
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setAlignmentX(Component.LEFT_ALIGNMENT);

            list.add(featureItem(Icons.code(Color.WHITE), new Color(56, 189, 248), "Extensive Coding Practice", "Master data structures & algorithms"));
            list.add(Box.createVerticalStrut(20));
            list.add(featureItem(Icons.document(Color.WHITE), new Color(167, 139, 250), "AI Resume Analysis", "Get ATS-friendly feedback instantly"));
            list.add(Box.createVerticalStrut(20));
            list.add(featureItem(Icons.video(Color.WHITE), new Color(244, 114, 182), "Mock Interviews", "Practice with AI analytics"));
            
            return list;
        }

        private JPanel featureItem(Icon icon, Color badgeColor, String title, String sub) {
            JPanel row = new JPanel(new BorderLayout(16, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            IconBadge badge = new IconBadge(icon, badgeColor, badgeColor.darker(), false, 40);
            row.add(badge, BorderLayout.WEST);

            JPanel textCol = new JPanel();
            textCol.setOpaque(false);
            textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
            
            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.BOLD, 15));
            t.setForeground(Color.WHITE);
            t.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel s = new JLabel(sub);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            s.setForeground(new Color(255, 255, 255, 170));
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            s.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

            textCol.add(t);
            textCol.add(s);
            row.add(textCol, BorderLayout.CENTER);

            return row;
        }

        private void initOrbs() {
            orbs = new ArrayList<>();
            orbs.add(new FloatingOrb(0.1, 0.2, 250, new Color(139, 92, 246, 80), 0.6, 0.4)); // Purple
            orbs.add(new FloatingOrb(0.8, 0.8, 350, new Color(56, 189, 248, 60), -0.4, -0.7)); // Blue
            orbs.add(new FloatingOrb(0.7, 0.3, 200, new Color(236, 72, 153, 50), -0.5, 0.6)); // Pink
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bgGrad = new GradientPaint(
                0, 0, new Color(30, 27, 75), // Deep purple
                getWidth(), getHeight(), new Color(88, 28, 135) // Vibrant purple
            );
            g2.setPaint(bgGrad);
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (FloatingOrb orb : orbs) {
                RadialGradientPaint orbPaint = new RadialGradientPaint(
                    (float)orb.x, (float)orb.y, (float)orb.radius,
                    new float[]{0f, 1f},
                    new Color[]{orb.color, new Color(orb.color.getRed(), orb.color.getGreen(), orb.color.getBlue(), 0)}
                );
                g2.setPaint(orbPaint);
                g2.fill(new Ellipse2D.Double(orb.x - orb.radius, orb.y - orb.radius, orb.radius*2, orb.radius*2));
            }

            g2.setColor(new Color(255, 255, 255, 8));
            int spacing = 32;
            for (int x = spacing; x < getWidth(); x += spacing) {
                for (int y = spacing; y < getHeight(); y += spacing) {
                    g2.fillOval(x, y, 2, 2);
                }
            }

            g2.dispose();
        }

        private JLabel tagLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.BOLD, 17));
            l.setForeground(Color.WHITE);
            return l;
        }
        
        private JLabel dotLabel() {
            JLabel l = new JLabel("\u25CF");
            l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            l.setForeground(new Color(255, 255, 255, 120));
            return l;
        }

        private JPanel miniStat(String val, String lbl) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            JLabel v = new JLabel(val);
            v.setFont(new Font("Segoe UI", Font.BOLD, 22));
            v.setForeground(Color.WHITE);
            v.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel l = new JLabel(lbl);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setForeground(new Color(255, 255, 255, 180));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(v);
            p.add(l);
            return p;
        }
    }

    private static class LogoIcon extends JComponent {
        public LogoIcon() {
            setPreferredSize(new Dimension(56, 56));
            setMaximumSize(new Dimension(56, 56));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Base glowing gradient
            GradientPaint baseGrad = new GradientPaint(
                0, 0, new Color(56, 189, 248),
                getWidth(), getHeight(), new Color(139, 92, 246)
            );
            g2.setPaint(baseGrad);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            // Draw "TF" in white
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            FontMetrics fm = g2.getFontMetrics();
            String text = "TF";
            int tw = fm.stringWidth(text);
            int th = fm.getAscent();
            g2.drawString(text, (getWidth() - tw)/2, (getHeight() + th)/2 - 4);

            g2.dispose();
        }
    }

    private static class FloatingOrb {
        double px, py;
        int radius;
        Color color;
        double dx, dy;
        double x, y;

        public FloatingOrb(double px, double py, int r, Color c, double dx, double dy) {
            this.px = px; this.py = py;
            this.radius = r; this.color = c;
            this.dx = dx; this.dy = dy;
        }

        public void update(int w, int h) {
            px += (dx * 0.001);
            py += (dy * 0.001);
            if (px < -0.2) dx = Math.abs(dx);
            if (px > 1.2) dx = -Math.abs(dx);
            if (py < -0.2) dy = Math.abs(dy);
            if (py > 1.2) dy = -Math.abs(dy);
            x = px * w;
            y = py * h;
        }
    }
}