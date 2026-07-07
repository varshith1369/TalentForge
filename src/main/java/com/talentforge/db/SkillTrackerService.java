package com.talentforge.db;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Persistence layer for the Skill Tracker module.
 *
 * Data structures exposed:
 *   • HashMap<String, Integer>  — skill name → current level (0–10)
 *   • LinkedList<HistoryEntry>  — ordered progress-change log
 *
 * Two SQLite tables are managed:
 *   user_skills   (user_id, skill_name, level)
 *   skill_history (id, user_id, skill_name, old_level, new_level, timestamp)
 */
public class SkillTrackerService {

    /* ------------------------------------------------------------------ */
    /*  Data-transfer objects                                              */
    /* ------------------------------------------------------------------ */

    /** One row from the skill_history table. */
    public static class HistoryEntry {
        public final int id;
        public final String skillName;
        public final int oldLevel;
        public final int newLevel;
        public final String timestamp;

        public HistoryEntry(int id, String skillName, int oldLevel, int newLevel, String timestamp) {
            this.id = id;
            this.skillName = skillName;
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.timestamp = timestamp;
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Table initialisation                                               */
    /* ------------------------------------------------------------------ */

    /** Create both tables if they don't exist yet. */
    public static void initializeSkillTables() {
        String skillsSql = "CREATE TABLE IF NOT EXISTS user_skills ("
                + "user_id INTEGER NOT NULL, "
                + "skill_name TEXT NOT NULL, "
                + "level INTEGER DEFAULT 0, "
                + "PRIMARY KEY(user_id, skill_name), "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

        String historySql = "CREATE TABLE IF NOT EXISTS skill_history ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "skill_name TEXT NOT NULL, "
                + "old_level INTEGER DEFAULT 0, "
                + "new_level INTEGER DEFAULT 0, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(skillsSql);
            stmt.execute(historySql);
            System.out.println("Skill tracker tables ready.");
        } catch (SQLException e) {
            System.err.println("Failed to create skill tracker tables: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  HashMap<String, Integer>  —  skill → level  lookups               */
    /* ------------------------------------------------------------------ */

    /**
     * Load every skill for the given user into a HashMap for O(1) lookups.
     */
    public static HashMap<String, Integer> loadSkills(int userId) {
        HashMap<String, Integer> skills = new HashMap<>();
        String sql = "SELECT skill_name, level FROM user_skills WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skills.put(rs.getString("skill_name"), rs.getInt("level"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load skills: " + e.getMessage());
        }
        return skills;
    }

    /**
     * Upsert a single skill level (INSERT OR REPLACE).
     */
    public static void saveSkill(int userId, String skillName, int level) {
        String sql = "INSERT OR REPLACE INTO user_skills (user_id, skill_name, level) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, skillName);
            ps.setInt(3, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save skill: " + e.getMessage());
        }
    }

    /**
     * Delete a skill entirely from the user's skill set.
     */
    public static void deleteSkill(int userId, String skillName) {
        String sql = "DELETE FROM user_skills WHERE user_id = ? AND skill_name = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, skillName);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete skill: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  LinkedList<HistoryEntry>  —  ordered progress history              */
    /* ------------------------------------------------------------------ */

    /**
     * Append a history entry (the DB auto-timestamps it).
     */
    public static void addHistoryEntry(int userId, String skillName, int oldLevel, int newLevel) {
        String sql = "INSERT INTO skill_history (user_id, skill_name, old_level, new_level) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, skillName);
            ps.setInt(3, oldLevel);
            ps.setInt(4, newLevel);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add history entry: " + e.getMessage());
        }
    }

    /**
     * Load all history entries for a user into a LinkedList, newest first.
     * LinkedList is chosen to preserve insertion order and allow efficient
     * prepend / iteration for the UI's scrolling timeline.
     */
    public static LinkedList<HistoryEntry> loadHistory(int userId) {
        LinkedList<HistoryEntry> history = new LinkedList<>();
        String sql = "SELECT id, skill_name, old_level, new_level, timestamp "
                   + "FROM skill_history WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new HistoryEntry(
                            rs.getInt("id"),
                            rs.getString("skill_name"),
                            rs.getInt("old_level"),
                            rs.getInt("new_level"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load skill history: " + e.getMessage());
        }
        return history;
    }

    /**
     * Load history entries for a single skill (used for mini-sparklines).
     */
    public static LinkedList<HistoryEntry> loadSkillHistory(int userId, String skillName) {
        LinkedList<HistoryEntry> history = new LinkedList<>();
        String sql = "SELECT id, skill_name, old_level, new_level, timestamp "
                   + "FROM skill_history WHERE user_id = ? AND skill_name = ? "
                   + "ORDER BY timestamp ASC, id ASC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, skillName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new HistoryEntry(
                            rs.getInt("id"),
                            rs.getString("skill_name"),
                            rs.getInt("old_level"),
                            rs.getInt("new_level"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load skill history: " + e.getMessage());
        }
        return history;
    }

    /**
     * Delete a specific history entry by id.
     */
    public static void deleteHistoryEntry(int userId, int entryId) {
        String sql = "DELETE FROM skill_history WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, entryId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete history entry: " + e.getMessage());
        }
    }
}
