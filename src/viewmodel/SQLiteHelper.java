package viewmodel;

import model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper {
    private static final String DATABASE_URL = "jdbc:sqlite:pos_database.db";

    // Method to create database and table if not exist
    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS departments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "price REAL, " +
                    "kdv REAL, " +
                    "count INTEGER)";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to add a department to the database
    public static void addDepartmentToDatabase(Department department) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String insertQuery = "INSERT INTO departments (name, price, kdv, count) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                pstmt.setString(1, department.getDepName());
                pstmt.setDouble(2, department.getDepPrice());
                pstmt.setDouble(3, department.getDepKdv());
                pstmt.setInt(4, department.getDepCount());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to fetch departments from the database
    public static List<Department> getDepartmentsFromDatabase() {
        List<Department> departments = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String selectQuery = "SELECT * FROM departments";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(selectQuery)) {

                while (rs.next()) {
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    double kdv = rs.getDouble("kdv");
                    int count = rs.getInt("count");
                    departments.add(new Department(name, kdv, price, count));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }



}
