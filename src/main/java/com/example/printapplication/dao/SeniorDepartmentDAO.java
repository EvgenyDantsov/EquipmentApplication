package com.example.printapplication.dao;

import com.example.printapplication.DatabaseHelper;
import com.example.printapplication.dto.SeniorDepartment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SeniorDepartmentDAO {
    public static ObservableList<SeniorDepartment> getAllSeniorDepartment() {
        ObservableList<SeniorDepartment> seniorDepartmentList = FXCollections.observableArrayList();
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM seniordepartment";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fio = resultSet.getString("fio");
                int departmentId = resultSet.getInt("Department_id");
                seniorDepartmentList.add(new SeniorDepartment(id, fio, departmentId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seniorDepartmentList;
    }

    public static boolean addSeniorDepartment(String fio, int departmentId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO seniordepartment (fio, Department_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, fio);
            statement.setInt(2, departmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateSeniorDepartment(int id, String fio, int departmentId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "UPDATE seniordepartment SET fio = ?, Department_id = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, fio);
            statement.setInt(2, departmentId);
            statement.setInt(3, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSeniorDepartment(int id) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "DELETE FROM seniordepartment WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fioExists(String fio, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM seniordepartment WHERE fio = ? AND id != ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fio);
            statement.setInt(2,excludeId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Если count > 0, отделение существует
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}