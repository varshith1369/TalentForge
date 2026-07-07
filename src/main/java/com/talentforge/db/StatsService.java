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
                + "problems_solved INTEGER DEFAULT 0, "
                + "aptitude_score INTEGER DEFAULT 0, "
                + "mock_interviews INTEGER DEFAULT 0, "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
            System.out.println("User stats table ready.");

            // Run migrations in case columns do not exist in an existing database
            try {
                stmt.execute("ALTER TABLE user_stats ADD COLUMN problems_solved INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE user_stats ADD COLUMN aptitude_score INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE user_stats ADD COLUMN mock_interviews INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE user_stats ADD COLUMN companies_prepared INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE user_stats ADD COLUMN offers_target INTEGER DEFAULT 1;");
            } catch (SQLException ignored) {}

            // Initialize the weekly activity table
            initializeActivityTable();

            // Initialize skill tracker tables
            SkillTrackerService.initializeSkillTables();
        } catch (SQLException e) {
            System.err.println("Failed to create user_stats table: " + e.getMessage());
        }
    }

    private static int loadStat(int userId, String column) {
        String sql = "SELECT " + column + " FROM user_stats WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(column);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load stat " + column + ": " + e.getMessage());
        }
        return 0;
    }

    private static void saveStat(int userId, String column, int value) {
        // Try update first
        String updateSql = "UPDATE user_stats SET " + column + " = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, value);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("Failed to update stat " + column + ": " + e.getMessage());
        }

        // If user doesn't exist in user_stats, insert
        String insertSql = "INSERT INTO user_stats (user_id, " + column + ") VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert stat " + column + ": " + e.getMessage());
        }
    }

    public static int loadResumeScore(int userId) {
        return loadStat(userId, "resume_score");
    }

    public static void saveResumeScore(int userId, int score) {
        saveStat(userId, "resume_score", score);
    }

    public static int loadProblemsSolved(int userId) {
        return loadStat(userId, "problems_solved");
    }

    public static void saveProblemsSolved(int userId, int count) {
        saveStat(userId, "problems_solved", count);
    }

    public static int loadAptitudeScore(int userId) {
        return loadStat(userId, "aptitude_score");
    }

    public static void saveAptitudeScore(int userId, int score) {
        saveStat(userId, "aptitude_score", score);
    }

    public static int loadMockInterviews(int userId) {
        return loadStat(userId, "mock_interviews");
    }

    public static void saveMockInterviews(int userId, int count) {
        saveStat(userId, "mock_interviews", count);
    }

    /** Initialize weekly activity table. */
    public static void initializeActivityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_weekly_activity ("
                + "user_id INTEGER, "
                + "day_index INTEGER, "
                + "solved_count INTEGER DEFAULT 0, "
                + "PRIMARY KEY(user_id, day_index), "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
            System.out.println("User weekly activity table ready.");
        } catch (SQLException e) {
            System.err.println("Failed to create user_weekly_activity table: " + e.getMessage());
        }
    }

    /** Load weekly activity solved count (index 0=Mon, ..., 6=Sun) */
    public static int[] loadWeeklyActivity(int userId) {
        int[] activity = new int[7];
        int[] defaultData = {3, 5, 2, 8, 4, 9, 6}; // Mon-Sun default solved counts
        System.arraycopy(defaultData, 0, activity, 0, 7);

        String sql = "SELECT day_index, solved_count FROM user_weekly_activity WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    int idx = rs.getInt("day_index");
                    int count = rs.getInt("solved_count");
                    if (idx >= 0 && idx < 7) {
                        activity[idx] = count;
                        hasData = true;
                    }
                }
                // Save defaults to DB on first load for this user
                if (!hasData) {
                    for (int i = 0; i < 7; i++) {
                        saveDailyActivity(userId, i, defaultData[i]);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load weekly activity: " + e.getMessage());
        }
        return activity;
    }

    /** Save daily solved count for the user. */
    public static void saveDailyActivity(int userId, int dayIndex, int count) {
        String sql = "INSERT OR REPLACE INTO user_weekly_activity (user_id, day_index, solved_count) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, dayIndex);
            ps.setInt(3, count);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save daily activity: " + e.getMessage());
        }
    }

    public static int loadCompaniesPrepared(int userId) {
        return loadStat(userId, "companies_prepared");
    }

    public static void saveCompaniesPrepared(int userId, int count) {
        saveStat(userId, "companies_prepared", count);
    }

    public static int loadOffersTarget(int userId) {
        int val = loadStat(userId, "offers_target");
        return val == 0 ? 1 : val; // Default to 1
    }

    public static void saveOffersTarget(int userId, int count) {
        saveStat(userId, "offers_target", count);
    }

    public static class UserRank {
        public final int userId;
        public final String name;
        public final int problemsSolved;
        public final int aptitudeScore;
        public final int mockInterviews;
        public final int resumeScore;

        public UserRank(int userId, String name, int solved, int aptitude, int interviews, int resume) {
            this.userId = userId;
            this.name = name;
            this.problemsSolved = solved;
            this.aptitudeScore = aptitude;
            this.mockInterviews = interviews;
            this.resumeScore = resume;
        }

        public int getScore() {
            // Overall score formula: problemsSolved*2 + aptitudeScore + mockInterviews*10 + resumeScore
            return problemsSolved * 2 + aptitudeScore + mockInterviews * 10 + resumeScore;
        }
    }

    public static java.util.List<UserRank> loadAllUserRanks() {
        java.util.List<UserRank> list = new java.util.ArrayList<>();
        String sql = "SELECT u.id, u.full_name, s.problems_solved, s.aptitude_score, s.mock_interviews, s.resume_score "
                   + "FROM users u LEFT JOIN user_stats s ON u.id = s.user_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new UserRank(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getInt("problems_solved"),
                    rs.getInt("aptitude_score"),
                    rs.getInt("mock_interviews"),
                    rs.getInt("resume_score")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user ranks: " + e.getMessage());
        }
        return list;
    }
}
