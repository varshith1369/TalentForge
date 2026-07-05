package com.talentforge.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.talentforge.auth.AuthService;
import com.talentforge.auth.AuthService.AuthResult;

import javax.swing.*;
import java.awt.*;

/**
 * Advanced Login/Signup screen: gradient + dot-grid branding panel with a
 * floating stat badge, a drop-shadow card containing a segmented Login/Sign Up
 * toggle, icon-adorned fields with placeholders, a built-in password reveal
 * button, and a smooth cross-dissolve animation when switching sections.
 */
public class LoginSignupPanel extends JPanel {

    private final CrossFadePanel crossFade = new CrossFadePanel();
    private final AuthService authService = new AuthService();

    private JButton loginToggleBtn;
    private JButton signupToggleBtn;

    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JLabel loginErrorLabel;

    private JTextField signupNameField;
    private JTextField signupEmailField;
    private JPasswordField signupPasswordField;
    private JLabel signupErrorLabel;

    private OnAuthSuccess onAuthSuccess;

    public interface OnAuthSuccess {
        void onSuccess(int userId, String fullName);
    }

    public void setOnAuthSuccess(OnAuthSuccess callback) {
        this.onAuthSuccess = callback;
    }

    public LoginSignupPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(243, 244, 248));

        BrandingPanel branding = new BrandingPanel();
        branding.add(branding.buildContent());

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 26, 40));
        wrapper.add(buildSegmentedToggle());
        wrapper.add(Box.createVerticalStrut(26));

        crossFade.addCard(buildLoginCard(), "LOGIN");
        crossFade.addCard(buildSignupCard(), "SIGNUP");
        crossFade.setPreferredSize(new Dimension(320, 380));
        wrapper.add(crossFade);

        ShadowPanel shadowCard = new ShadowPanel(new BorderLayout());
        shadowCard.add(wrapper, BorderLayout.CENTER);
        outer.add(shadowCard);

        add(branding, BorderLayout.WEST);
        add(outer, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> showSection("LOGIN"));
    }

    private JPanel buildSegmentedToggle() {
        JPanel toggle = new JPanel(new GridLayout(1, 2, 4, 0));
        toggle.setOpaque(true);
        toggle.setBackground(new Color(243, 244, 246));
        toggle.putClientProperty(FlatClientProperties.STYLE, "arc:20");
        toggle.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        toggle.setMaximumSize(new Dimension(320, 44));
        toggle.setPreferredSize(new Dimension(320, 44));

        loginToggleBtn = new JButton("Log In");
        signupToggleBtn = new JButton("Sign Up");

        for (JButton b : new JButton[]{loginToggleBtn, signupToggleBtn}) {
            b.setFocusPainted(false);
            b.setFont(Theme.FONT_BUTTON.deriveFont(14f));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        loginToggleBtn.addActionListener(e -> showSection("LOGIN"));
        signupToggleBtn.addActionListener(e -> showSection("SIGNUP"));

        toggle.add(loginToggleBtn);
        toggle.add(signupToggleBtn);
        return toggle;
    }

    private void showSection(String section) {
        boolean loginActive = section.equals("LOGIN");
        styleToggleButton(loginToggleBtn, loginActive);
        styleToggleButton(signupToggleBtn, !loginActive);
        crossFade.showCard(section);
    }

    private void styleToggleButton(JButton btn, boolean active) {
        if (active) {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc:16; background:#6366F1; foreground:#FFFFFF; borderWidth:0; focusWidth:0");
        } else {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc:16; background:#F3F4F6; foreground:#6B7280; borderWidth:0; focusWidth:0");
        }
    }

    private JPanel loginFormPanel;
    private JPanel signupFormPanel;

    private JPanel buildLoginCard() {
        JPanel form = new JPanel();
        this.loginFormPanel = form;
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Log in to continue your prep journey");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginEmailField = styledField("Email address", Icons.envelope(Theme.MUTED_TEXT));
        loginEmailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginPasswordField = styledPasswordField();
        loginPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setFont(Theme.FONT_LINK);
        loginErrorLabel.setForeground(Theme.ERROR_COLOR);
        loginErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        optionsRow.setMaximumSize(new Dimension(300, 26));
        optionsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setOpaque(false);
        rememberMe.setFont(Theme.FONT_LINK);
        rememberMe.setForeground(Theme.MUTED_TEXT);
        rememberMe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton forgotPassword = linkButton("Forgot password?");
        forgotPassword.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            ForgotPasswordDialog dialog = new ForgotPasswordDialog(
                    owner instanceof Frame ? (Frame) owner : null);
            dialog.setVisible(true);
        });
        optionsRow.add(rememberMe, BorderLayout.WEST);
        optionsRow.add(forgotPassword, BorderLayout.EAST);

        loginSubmitBtn = primaryButton("Log In");
        JButton submitBtn = loginSubmitBtn;
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> handleLogin());
        loginEmailField.addActionListener(e -> handleLogin());
        loginPasswordField.addActionListener(e -> handleLogin());

        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(26));
        form.add(loginEmailField);
        form.add(Box.createVerticalStrut(14));
        form.add(loginPasswordField);
        form.add(Box.createVerticalStrut(10));
        form.add(optionsRow);
        form.add(Box.createVerticalStrut(6));
        form.add(loginErrorLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(submitBtn);

        return form;
    }

    private JPanel buildSignupCard() {
        JPanel form = new JPanel();
        this.signupFormPanel = form;
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create Account");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Start building your placement readiness");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupNameField = styledField("Full name", Icons.user(Theme.MUTED_TEXT));
        signupNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupEmailField = styledField("Email address", Icons.envelope(Theme.MUTED_TEXT));
        signupEmailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupPasswordField = styledPasswordField();
        signupPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupErrorLabel = new JLabel(" ");
        signupErrorLabel.setFont(Theme.FONT_LINK);
        signupErrorLabel.setForeground(Theme.ERROR_COLOR);
        signupErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupSubmitBtn = primaryButton("Create Account");
        JButton submitBtn = signupSubmitBtn;
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> handleSignup());
        signupNameField.addActionListener(e -> handleSignup());
        signupEmailField.addActionListener(e -> handleSignup());
        signupPasswordField.addActionListener(e -> handleSignup());

        JLabel terms = new JLabel("By signing up, you agree to our Terms & Privacy Policy");
        terms.setFont(Theme.FONT_LINK.deriveFont(11f));
        terms.setForeground(Theme.MUTED_TEXT);
        terms.setAlignmentX(Component.CENTER_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(22));
        form.add(signupNameField);
        form.add(Box.createVerticalStrut(12));
        form.add(signupEmailField);
        form.add(Box.createVerticalStrut(12));
        form.add(signupPasswordField);
        form.add(Box.createVerticalStrut(8));
        form.add(signupErrorLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(submitBtn);
        form.add(Box.createVerticalStrut(14));
        form.add(terms);

        return form;
    }

    private JTextField styledField(String placeholder, Icon leadingIcon) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, leadingIcon);
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; showClearButton:true; iconTextGap:8; margin:10,10,10,10");
        field.setFont(Theme.FONT_FIELD);
        field.setMaximumSize(new Dimension(300, 46));
        field.setPreferredSize(new Dimension(300, 46));
        return field;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.lock(Theme.MUTED_TEXT));
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; showRevealButton:true; iconTextGap:8; margin:10,10,10,10");
        field.setFont(Theme.FONT_FIELD);
        field.setMaximumSize(new Dimension(300, 46));
        field.setPreferredSize(new Dimension(300, 46));
        return field;
    }

    private JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BUTTON);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(300, 46));
        btn.setPreferredSize(new Dimension(300, 46));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; background:#6366F1; foreground:#FFFFFF; focusWidth:0; "
                        + "hoverBackground:#4F46E5; pressedBackground:#4338CA; borderWidth:0");
        return btn;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_LINK);
        btn.setForeground(Theme.PRIMARY_START);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    private JButton loginSubmitBtn;
    private JButton signupSubmitBtn;
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
                loginErrorLabel.setForeground(Theme.ERROR_COLOR);
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
                signupErrorLabel.setForeground(Theme.SUCCESS_COLOR);
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
}