package com.example.printapplication.dao;

import com.example.printapplication.DatabaseHelper;
import com.example.printapplication.dto.Equipment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class EquipmentDAO {
    public static ObservableList<Equipment> getAllEquipment() {
        ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM equipment";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                equipmentList.add(mapRowToEquipment(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }

    // Загружает оборудование по конкретному типу
    public static ObservableList<Equipment> getEquipmentByType(int equipmentTypeId) {
        ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM equipment WHERE equipmentType_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, equipmentTypeId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                equipmentList.add(mapRowToEquipment(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }

    // Вспомогательный метод для уменьшения дублирования кода
    private static Equipment mapRowToEquipment(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String model = rs.getString("model");
        String snNumber = rs.getString("sn_number");
        String note = rs.getString("note");
        int officeId = rs.getInt("Office_id");
        String status = rs.getString("status");
        int equipmentTypeId = rs.getInt("equipmentType_id");
        return new Equipment(id, name, model, snNumber, note, status, officeId, equipmentTypeId);
    }

    public static boolean addEquipment(String name, String model, String snNumber, String note, String status, int officeId, int equipmentTypeId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO equipment (name, model, sn_number, note, status, Office_id, equipmenttype_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, model);
            statement.setString(3, snNumber);
            statement.setString(4, note);
            statement.setString(5,status);
            statement.setInt(6, officeId);
            statement.setInt(7, equipmentTypeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEquipment(int id, String name, String model, String snNumber, String note, String status, int officeId, int equipmentTypeId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "UPDATE equipment SET name = ?, model = ?, sn_number = ?, note = ?, status = ?, Office_id = ?, equipmenttype_id = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, model);
            statement.setString(3, snNumber);
            statement.setString(4, note);
            statement.setString(5, status);
            statement.setInt(6, officeId);
            statement.setInt(7, equipmentTypeId);
            statement.setInt(8, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEquipment(int id) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "DELETE FROM equipment WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Проверка уникальности snNumber
    public static boolean isSnNumberUnique(String snNumber, int excludeId) {
        String sql = "SELECT COUNT(*) FROM equipment WHERE sn_number = ? AND id != ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, snNumber);
            statement.setInt(2, excludeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) != 0; // Если count == 0, snNumber уникален
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean moveEquipment(int printerId, int newOfficeId, String note, String newStatus) {
        String sql = "UPDATE equipment SET Office_id = ?, note = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newOfficeId);
            pstmt.setString(2, note);
            pstmt.setString(3,newStatus);
            pstmt.setInt(4, printerId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}