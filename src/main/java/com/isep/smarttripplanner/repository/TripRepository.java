package com.isep.smarttripplanner.repository;

import com.isep.smarttripplanner.model.Destination;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.model.TripStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    private Connection conn;

    public TripRepository() {
        try {
            conn = DatabaseManager.getDb().getConnection();
            createTripTable();
            createDestinationTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTripTable() {
        String sql = "CREATE TABLE IF NOT EXISTS trips (" +
                "id TEXT PRIMARY KEY," +
                "title TEXT," +
                "startDate TEXT," +
                "endDate TEXT," +
                "budget REAL," +
                "status TEXT" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            try {
                stmt.execute("ALTER TABLE trips ADD COLUMN currency TEXT DEFAULT 'USD'");
            } catch (SQLException ignored) {
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDestinationTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS destinations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trip_id TEXT," +
                "name TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "destination_start_date TEXT," +
                "destination_end_date TEXT," +
                "FOREIGN KEY (trip_id) REFERENCES trips(id)" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            try {
                stmt.execute("ALTER TABLE destinations ADD COLUMN destination_start_date TEXT");
            } catch (SQLException ignored) {
            }

            try {
                stmt.execute("ALTER TABLE destinations ADD COLUMN destination_end_date TEXT");
            } catch (SQLException ignored) {
            }
        }
    }

    public Trip findActiveTrip() {
        String sql = "SELECT * FROM trips WHERE status = 'PLANNED' OR status = 'ONGOING' ORDER BY startDate ASC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Trip trip = new Trip();
                    tripFetchHelper(trip, rs);
                    return trip;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void tripFetchHelper(Trip trip, ResultSet rs) throws SQLException {
        trip.setId(rs.getString("id"));
        trip.setTitle(rs.getString("title"));
        trip.setStartDate(LocalDate.parse(rs.getString("startDate")));
        trip.setTripEndDate(LocalDate.parse(rs.getString("endDate")));
        trip.setBudget(rs.getDouble("budget"));
        String curr = rs.getString("currency");
        if (curr != null)
            trip.setCurrency(curr);
        trip.setStatus(TripStatus.valueOf(rs.getString("status")));
        trip.setDestinations(findDestinationsByTripId(trip.getId()));
    }

    public void insertTrip(Trip trip) {
        String sql = "INSERT INTO trips (id, title, startDate, endDate, budget, status, currency) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trip.getId());
            stmt.setString(2, trip.getTitle());
            stmt.setString(3, trip.getStartDate().toString());
            stmt.setString(4, trip.getTripEndDate().toString());
            stmt.setDouble(5, trip.getBudget());
            stmt.setString(6, trip.getStatus().name());
            stmt.setString(7, trip.getCurrency());
            stmt.execute();
            if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                insertDestinations(trip);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting trip", e);
        }
    }

    private void insertDestinations(Trip trip) throws SQLException {
        String sql = "INSERT INTO destinations (trip_id, name, latitude, longitude, destination_start_date, destination_end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Destination d : trip.getDestinations()) {
                stmt.setString(1, trip.getId());
                stmt.setString(2, d.getName());
                stmt.setDouble(3, d.getLatitude());
                stmt.setDouble(4, d.getLongitude());
                stmt.setObject(5, d.getDestinationStartDate());
                stmt.setObject(6, d.getDestinationEndDate());
                stmt.execute();
            }
        }
    }

    public List<Trip> findAllTrips() {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT * FROM trips";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trip trip = new Trip();
                tripFetchHelper(trip, rs);
                trips.add(trip);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding trips", e);
        }
        return trips;
    }

    private List<Destination> findDestinationsByTripId(String tripId) {
        List<Destination> destinations = new ArrayList<>();
        String sql = "SELECT * FROM destinations WHERE trip_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tripId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Destination d = new Destination();
                    d.setId(rs.getInt("id"));
                    d.setName(rs.getString("name"));
                    d.setLatitude(rs.getDouble("latitude"));
                    d.setLongitude(rs.getDouble("longitude"));

                    String startStr = rs.getString("destination_start_date");
                    if (startStr != null)
                        d.setDestinationStartDate(LocalDate.parse(startStr));

                    String endStr = rs.getString("destination_end_date");
                    if (endStr != null)
                        d.setDestinationEndDate(LocalDate.parse(endStr));

                    destinations.add(d);
                }
                return destinations;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTrip(String id) {
        String deleteDestinations = "DELETE FROM destinations WHERE trip_id = ?";
        String deleteTrip = "DELETE FROM trips WHERE id = ?";

        try {
            try (PreparedStatement stmtD = conn.prepareStatement(deleteDestinations)) {
                stmtD.setString(1, id);
                stmtD.execute();
            }

            try (PreparedStatement stmtT = conn.prepareStatement(deleteTrip)) {
                stmtT.setString(1, id);
                stmtT.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting trip", e);
        }
    }

    public void deleteDestination(String id) {
        String deleteDestinations = "DELETE FROM destinations WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteDestinations)) {
            stmt.setString(1, id);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting destination", e);
        }
    }

    public void addDestination(String tripId, Destination destination) {
        String sql = "INSERT INTO destinations (trip_id, name, latitude, longitude, destination_start_date, destination_end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tripId);
            stmt.setString(2, destination.getName());
            stmt.setDouble(3, destination.getLatitude());
            stmt.setDouble(4, destination.getLongitude());
            stmt.setObject(5, destination.getDestinationStartDate());
            stmt.setObject(6, destination.getDestinationEndDate());
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding destination", e);
        }
    }

    public void updateTrip(Trip trip) {
        String sql = "UPDATE trips SET title = ?, startDate = ?, endDate = ?, budget = ?, status = ?, currency = ? WHERE id = ?";
        String deleteDestSql = "DELETE FROM destinations WHERE trip_id = ?";
        String insertDestSql = "INSERT INTO destinations(trip_id, name, latitude, longitude, destination_start_date, destination_end_date) VALUES(?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, trip.getTitle());
                pstmt.setObject(2, trip.getStartDate());
                pstmt.setObject(3, trip.getTripEndDate());
                pstmt.setDouble(4, trip.getBudget());
                pstmt.setString(5, trip.getStatus().name());
                pstmt.setString(6, trip.getCurrency());
                pstmt.setString(7, trip.getId());
                pstmt.executeUpdate();

                try (java.sql.PreparedStatement delStmt = conn.prepareStatement(deleteDestSql)) {
                    delStmt.setString(1, trip.getId());
                    delStmt.executeUpdate();
                }

                try (java.sql.PreparedStatement destStmt = conn.prepareStatement(insertDestSql)) {
                    for (com.isep.smarttripplanner.model.Destination d : trip.getDestinations()) {
                        destStmt.setString(1, trip.getId());
                        destStmt.setString(2, d.getName());
                        destStmt.setDouble(3, d.getLatitude());
                        destStmt.setDouble(4, d.getLongitude());
                        destStmt.setObject(5, d.getDestinationStartDate());
                        destStmt.setObject(6, d.getDestinationEndDate());
                        destStmt.addBatch();
                    }
                    destStmt.executeBatch();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (java.sql.SQLException e) {
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (java.sql.SQLException e) {
            }
        }
    }

    public int countTotalTrips() {
        String sql = "SELECT COUNT(*) FROM trips";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting trips", e);
        }
        return 0;
    }

    public int countCompletedTrips() {
        String sql = "SELECT COUNT(*) FROM trips WHERE status = 'COMPLETED'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting completed trips", e);
        }
        return 0;
    }

    public int countPlannedTrips() {
        String sql = "SELECT COUNT(*) FROM trips WHERE status = 'PLANNED' OR status = 'ONGOING'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting planned trips", e);
        }
        return 0;
    }

    public double calculateTotalBudget() {
        String sql = "SELECT SUM(budget) FROM trips";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating total budget", e);
        }
        return 0.0;
    }

    public int countTotalDestinations() {
        String sql = "SELECT COUNT(*) FROM destinations";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting destinations", e);
        }
        return 0;
    }
}
