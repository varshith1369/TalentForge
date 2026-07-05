package com.talentforge.auth;

import com.talentforge.db.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * Handles user signup and login.
 * Uses SHA-256 + per-user salt for password hashing
 * (no external libraries needed, sticks to core Java + JDBC).
 */
public class AuthService {

    /** Result object returned after a signup/login attempt. */
    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final int userId;
        public final String fullName;

        public AuthResult(boolean success, String message, int userId, String fullName) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.fullName = fullName;
        }
    }

    public AuthResult signup(String fullName, String email, String password) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            return new AuthResult(false, "All fields are required.", -1, null);
        }
        if (password.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters.", -1, null);
        }

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        String sql = "INSERT INTO users (full_name, email, password_hash, salt) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, fullName);
            ps.setString(2, email.toLowerCase());
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int newId = keys.next() ? keys.getInt(1) : -1;
                return new AuthResult(true, "Account created successfully.", newId, fullName);
            }

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return new AuthResult(false, "An account with this email already exists.", -1, null);
            }
            return new AuthResult(false, "Signup failed: " + e.getMessage(), -1, null);
        }
    }

    public AuthResult login(String email, String password) {
        String sql = "SELECT id, full_name, password_hash, salt FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new AuthResult(false, "No account found with this email.", -1, null);
                }

                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String attemptHash = hashPassword(password, salt);

                if (attemptHash.equals(storedHash)) {
                    return new AuthResult(true, "Login successful.", rs.getInt("id"), rs.getString("full_name"));
                } else {
                    return new AuthResult(false, "Incorrect password.", -1, null);
                }
            }

        } catch (SQLException e) {
            return new AuthResult(false, "Login failed: " + e.getMessage(), -1, null);
        }
    }

    /** Checks whether an account exists for the given email (used by Forgot Password step 1). */
    public AuthResult emailExists(String email) {
        String sql = "SELECT id, full_name FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthResult(true, "Account found.", rs.getInt("id"), rs.getString("full_name"));
                }
                return new AuthResult(false, "No account found with this email.", -1, null);
            }

        } catch (SQLException e) {
            return new AuthResult(false, "Lookup failed: " + e.getMessage(), -1, null);
        }
    }

    /** Sets a new password for the given email (used by Forgot Password step 2). */
    public AuthResult resetPassword(String email, String newPassword) {
        if (newPassword.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters.", -1, null);
        }

        String salt = generateSalt();
        String hash = hashPassword(newPassword, salt);
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setString(3, email.toLowerCase());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                return new AuthResult(true, "Password updated successfully.", -1, null);
            }
            return new AuthResult(false, "No account found with this email.", -1, null);

        } catch (SQLException e) {
            return new AuthResult(false, "Reset failed: " + e.getMessage(), -1, null);
        }
    }

    /** Logs in a user by email if they exist, otherwise creates an account for them (used by Google Sign-In). */
    public AuthResult loginOrCreateGoogleUser(String email, String fullName) {
        AuthResult existing = emailExists(email);
        if (existing.success) {
            return existing;
        }
        // No local password needed for Google users; store a random unusable hash.
        String randomPassword = java.util.UUID.randomUUID().toString();
        return signup(fullName, email, randomPassword);
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }
}