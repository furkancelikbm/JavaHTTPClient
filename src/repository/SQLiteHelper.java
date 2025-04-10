package repository;

import model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper {
    private static final String DATABASE_URL = "jdbc:sqlite:pos_database.db";

    // Method to create the database and table if not exist
    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS departments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "kdv REAL NOT NULL, " +
                    "count INTEGER NOT NULL)";
            stmt.execute(createTableQuery);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    // Method to add a department to the database
    public static void addDepartmentToDatabase(Department department) {
        String insertQuery = "INSERT INTO departments (name, price, kdv, count) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {

            pstmt.setString(1, department.getDepName());
            pstmt.setDouble(2, department.getDepPrice());
            pstmt.setDouble(3, department.getDepKdv());
            pstmt.setInt(4, department.getDepCount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding department: " + e.getMessage());
        }
    }

    // Method to fetch departments from the database
    public static List<Department> getDepartmentsFromDatabase() {
        List<Department> departments = new ArrayList<>();
        String selectQuery = "SELECT name, price, kdv, count FROM departments";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = connection.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                double kdv = rs.getDouble("kdv");
                int count = rs.getInt("count");
                departments.add(new Department(name, kdv, price, count));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching departments: " + e.getMessage());
        }
        return departments;
    }

    // Method to remove a department from the database
    public static void removeDepartmentFromDatabase(Department dept) {
        String deleteQuery = "DELETE FROM departments WHERE name = ? AND price = ? AND kdv = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setString(1, dept.getDepName());
            stmt.setDouble(2, dept.getDepPrice());
            stmt.setDouble(3, dept.getDepKdv());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing department: " + e.getMessage());
        }
    }
}
