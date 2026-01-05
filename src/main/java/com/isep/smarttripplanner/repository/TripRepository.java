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
                "FOREIGN KEY (trip_id) REFERENCES trips(id)" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
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
        trip.setStatus(TripStatus.valueOf(rs.getString("status")));
        trip.setDestinations(findDestinationsByTripId(trip.getId()));
    }

    public void insertTrip(Trip trip) {
        String sql = "INSERT INTO trips (id, title, startDate, endDate, budget, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trip.getId());
            stmt.setString(2, trip.getTitle());
            stmt.setString(3, trip.getStartDate().toString());
            stmt.setString(4, trip.getTripEndDate().toString());
            stmt.setDouble(5, trip.getBudget());
            stmt.setString(6, trip.getStatus().name());
            stmt.execute();
            if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                insertDestinations(trip);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting trip", e);
        }
    }

    private void insertDestinations(Trip trip) throws SQLException {
        String sql = "INSERT INTO destinations (trip_id, name, latitude, longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Destination d : trip.getDestinations()) {
                stmt.setString(1, trip.getId());
                stmt.setString(2, d.getName());
                stmt.setDouble(3, d.getLatitude());
                stmt.setDouble(4, d.getLongitude());
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
        String sql = "INSERT INTO destinations (trip_id, name, latitude, longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tripId);
            stmt.setString(2, destination.getName());
            stmt.setDouble(3, destination.getLatitude());
            stmt.setDouble(4, destination.getLongitude());
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding destination", e);
        }
    }
}
