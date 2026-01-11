package com.isep.smarttripplanner.repository;

import com.isep.smarttripplanner.model.AppConfig;
import java.sql.*;

public class AppConfigRepository {
    private Connection conn;

    public AppConfigRepository() {
        try {
            conn = DatabaseManager.getDb().getConnection();
            createAppConfigTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAppConfigTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS app_config (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                "default_currency TEXT, " +
                "target_currency TEXT, " +
                "last_summary_date TEXT" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

        // Primitive migrations
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE app_config ADD COLUMN target_currency TEXT");
        } catch (SQLException e) {
            // Ignore if exists
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE app_config ADD COLUMN last_summary_date TEXT");
        } catch (SQLException e) {
            // Ignore if exists
        }
    }

    public AppConfig getConfig() {
        String sql = "SELECT * FROM app_config WHERE id = 1";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                AppConfig config = new AppConfig();
                config.setDefaultCurrency(rs.getString("default_currency"));

                String target = rs.getString("target_currency");
                config.setTargetCurrency(target != null ? target : "EUR");

                String dateStr = rs.getString("last_summary_date");
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        config.setLastSummaryDate(java.time.LocalDate.parse(dateStr));
                    } catch (Exception e) {
                    }
                }

                return config;
            } else {
                AppConfig defaultConfig = new AppConfig("USD", "EUR");
                saveInitialConfig(defaultConfig);
                return defaultConfig;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching app config", e);
        }
    }

    private void saveInitialConfig(AppConfig config) {
        String sql = "INSERT INTO app_config (id, default_currency, target_currency, last_summary_date) VALUES (1, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, config.getDefaultCurrency());
            stmt.setString(2, config.getTargetCurrency());
            stmt.setString(3, config.getLastSummaryDate() != null ? config.getLastSummaryDate().toString() : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving initial config", e);
        }
    }

    public void saveConfig(AppConfig config) {
        String sql = "UPDATE app_config SET default_currency = ?, target_currency = ?, last_summary_date = ? WHERE id = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, config.getDefaultCurrency());
            stmt.setString(2, config.getTargetCurrency());
            stmt.setString(3, config.getLastSummaryDate() != null ? config.getLastSummaryDate().toString() : null);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                saveInitialConfig(config);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating app config", e);
        }
    }
}
