package com.talentforge.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles persistence for user stats (e.g. resume score, problems solved) in the database.
 */
public class StatsService {

    /** Initialize user_stats table if it doesn't exist. */
    public static void initializeStatsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_stats ("
                + "user_id INTEGER PRIMARY KEY, "
                + "resume_score INTEGER DEFAULT 0, "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
            System.out.println("User stats table ready.");
        } catch (SQLException e) {
            System.err.println("Failed to create user_stats table: " + e.getMessage());
        }
    }

    /** Load user stats from database. Returns 0 for resume score if not found. */
    public static int loadResumeScore(int userId) {
        String sql = "SELECT resume_score FROM user_stats WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("resume_score");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user stats: " + e.getMessage());
        }

        return 0; // Default score
    }

    /** Save or update resume score in database. */
    public static void saveResumeScore(int userId, int score) {
        // Upsert logic (Insert or Replace)
        String sql = "INSERT OR REPLACE INTO user_stats (user_id, resume_score) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, score);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save resume score: " + e.getMessage());
        }
    }
}
