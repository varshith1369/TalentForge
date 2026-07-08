package com.talentforge.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/**
 * Handles user profile data persistence in the database.
 * Stores and retrieves profile information for each user.
 */
public class ProfileService {

    /** Initialize user_profiles table if it doesn't exist. */
    public static void initializeProfileTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_profiles ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL UNIQUE, "
                + "email TEXT, "
                + "phone TEXT, "
                + "education TEXT, "
                + "college TEXT, "
                + "cgpa TEXT, "
                + "skills TEXT, "
                + "location TEXT, "
                + "bio TEXT, "
                + "profile_image BLOB, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
            System.out.println("User profiles table ready.");
        } catch (SQLException e) {
            System.err.println("Failed to create user_profiles table: " + e.getMessage());
        }
    }

    /** Load user profile from database. */
    public static ProfileData loadProfile(int userId) {
        String sql = "SELECT email, phone, education, college, cgpa, skills, location, bio FROM user_profiles WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ProfileData(
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("education"),
                        rs.getString("college"),
                        rs.getString("cgpa"),
                        rs.getString("skills"),
                        rs.getString("location"),
                        rs.getString("bio")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
        }

        // Return default profile if not found
        return new ProfileData(
            "user@example.com",
            "+91 98765 43210",
            "B.Tech - Computer Science",
            "Institute of Technology",
            "8.5 / 10",
            "Java, Python, DSA, Web Dev",
            "Mumbai, Maharashtra",
            "Passionate learner, always ready to explore new technologies"
        );
    }

    /** Save or update user profile in database. */
    public static boolean saveProfile(int userId, ProfileData profile) {
        // First check if profile exists
        String checkSql = "SELECT id FROM user_profiles WHERE user_id = ?";
        boolean profileExists = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                profileExists = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Failed to check profile existence: " + e.getMessage());
        }

        String sql;
        if (profileExists) {
            sql = "UPDATE user_profiles SET email = ?, phone = ?, education = ?, college = ?, "
                    + "cgpa = ?, skills = ?, location = ?, bio = ?, updated_at = CURRENT_TIMESTAMP "
                    + "WHERE user_id = ?";
        } else {
            sql = "INSERT INTO user_profiles (user_id, email, phone, education, college, cgpa, skills, location, bio) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (profileExists) {
                ps.setString(1, profile.email);
                ps.setString(2, profile.phone);
                ps.setString(3, profile.education);
                ps.setString(4, profile.college);
                ps.setString(5, profile.cgpa);
                ps.setString(6, profile.skills);
                ps.setString(7, profile.location);
                ps.setString(8, profile.bio);
                ps.setInt(9, userId);
            } else {
                ps.setInt(1, userId);
                ps.setString(2, profile.email);
                ps.setString(3, profile.phone);
                ps.setString(4, profile.education);
                ps.setString(5, profile.college);
                ps.setString(6, profile.cgpa);
                ps.setString(7, profile.skills);
                ps.setString(8, profile.location);
                ps.setString(9, profile.bio);
            }

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Failed to save profile: " + e.getMessage());
            return false;
        }
    }

    /** Load stored profile image, if one exists. */
    public static BufferedImage loadProfileImage(int userId) {
        String sql = "SELECT profile_image FROM user_profiles WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] imageBytes = rs.getBytes("profile_image");
                    if (imageBytes != null && imageBytes.length > 0) {
                        return ImageIO.read(new ByteArrayInputStream(imageBytes));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load profile image: " + e.getMessage());
        }
        return null;
    }

    /** Save or replace the user's profile image. */
    public static boolean saveProfileImage(int userId, BufferedImage image) {
        if (image == null) {
            return false;
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            byte[] imageBytes = out.toByteArray();

            String sql = "INSERT INTO user_profiles (user_id, profile_image) VALUES (?, ?) "
                    + "ON CONFLICT(user_id) DO UPDATE SET profile_image = excluded.profile_image, "
                    + "updated_at = CURRENT_TIMESTAMP";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setBytes(2, imageBytes);
                ps.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to save profile image: " + e.getMessage());
            return false;
        }
    }

    /** Data class to hold profile information. */
    public static class ProfileData {
        public final String email;
        public final String phone;
        public final String education;
        public final String college;
        public final String cgpa;
        public final String skills;
        public final String location;
        public final String bio;

        public ProfileData(String email, String phone, String education, String college,
                          String cgpa, String skills, String location, String bio) {
            this.email = email;
            this.phone = phone;
            this.education = education;
            this.college = college;
            this.cgpa = cgpa;
            this.skills = skills;
            this.location = location;
            this.bio = bio;
        }
    }
}
