package com.isep.smarttripplanner.repository;

import com.isep.smarttripplanner.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {
    private Connection conn;

    public ExpenseRepository() {
        try {
            conn = DatabaseManager.getDb().getConnection();
            createExpenseTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createExpenseTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trip_id TEXT," +
                "description TEXT," +
                "amount REAL," +
                "category TEXT," +
                "date TEXT," +
                "FOREIGN KEY (trip_id) REFERENCES trips(id)" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertExpense(Expense expense) {
        String sql = "INSERT INTO expenses (trip_id, description, amount, category, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, expense.getTripId());
            stmt.setString(2, expense.getDescription());
            stmt.setDouble(3, expense.getAmount());
            stmt.setString(4, expense.getCategory());
            stmt.setString(5, expense.getDate().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting expense", e);
        }
    }

    public List<Expense> findExpensesByTripId(String tripId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE trip_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tripId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense e = new Expense();
                    e.setId(rs.getInt("id"));
                    e.setTripId(rs.getString("trip_id"));
                    e.setDescription(rs.getString("description"));
                    e.setAmount(rs.getDouble("amount"));
                    e.setCategory(rs.getString("category"));
                    e.setDate(LocalDate.parse(rs.getString("date")));
                    expenses.add(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding expenses", e);
        }
        return expenses;
    }

    public void deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting expense", e);
        }
    }
}
