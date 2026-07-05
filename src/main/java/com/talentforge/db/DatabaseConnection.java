package com.talentforge.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles the single shared connection to the SQLite database.
 * Adjust DB_PATH to match wherever your project keeps its .db file.
 */
public class DatabaseConnection {

    private static final String DB_PATH = "jdbc:sqlite:talentforge.db";
    private static Connection connection;

    // Prevent instantiation
    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_PATH);
                connection.createStatement().execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Call this once at application startup to make sure
     * the users table exists before Login/Signup is used.
     */
    public static void initializeUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT NOT NULL, "
                + "email TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "salt TEXT NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table ready.");
        } catch (SQLException e) {
            System.err.println("Failed to create users table: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }
}