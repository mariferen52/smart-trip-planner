package com.isep.smarttripplanner.repository;

import com.isep.smarttripplanner.model.TodoItem;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TodoRepository {
    private Connection conn;

    public TodoRepository() {
        try {
            conn = DatabaseManager.getDb().getConnection();
            createTodoTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTodoTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS todos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trip_id TEXT," +
                "description TEXT," +
                "is_completed INTEGER," +
                "due_date TEXT," +
                "FOREIGN KEY (trip_id) REFERENCES trips(id)" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertTodo(TodoItem item) {
        String sql = "INSERT INTO todos (trip_id, description, is_completed, due_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getTripId());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.isCompleted() ? 1 : 0);
            stmt.setObject(4, item.getDueDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting todo", e);
        }
    }

    public List<TodoItem> findTodosByTripId(String tripId) {
        List<TodoItem> todos = new ArrayList<>();
        String sql = "SELECT * FROM todos WHERE trip_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tripId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    boolean isCompleted = rs.getInt("is_completed") == 1;
                    String dueDateStr = rs.getString("due_date");
                    LocalDate dueDate = (dueDateStr != null) ? LocalDate.parse(dueDateStr) : null;

                    todos.add(new TodoItem(id, tripId, description, isCompleted, dueDate));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding todos", e);
        }
        return todos;
    }

    public void updateTodo(TodoItem item) {
        String sql = "UPDATE todos SET is_completed = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.isCompleted() ? 1 : 0);
            stmt.setInt(2, item.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating todo", e);
        }
    }
}
