package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.dto.UltrasoundSensorDictionary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class UltrasoundSensorDictionaryDAO {
    // Получить все записи справочника
    public static ObservableList<UltrasoundSensorDictionary> getAllSensors() {
        ObservableList<UltrasoundSensorDictionary> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM ultrasoundsensor_dictionary ORDER BY name, type";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToSensor(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Получить модели по названию
    public static ObservableList<String> getModelsByName(String name) {
        ObservableList<String> models = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT model FROM ultrasoundsensor_dictionary WHERE name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                models.add(rs.getString("type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return models;
    }

    // Получить все уникальные имена датчиков
    public static ObservableList<String> getAllNames() {
        ObservableList<String> names = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT name FROM ultrasoundsensor_dictionary ORDER BY name";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    // Добавить запись в справочник
    public static boolean addSensor(String name, String type) {
        String sql = "INSERT INTO ultrasoundsensor_dictionary (name, type) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Обновить запись
    public static boolean updateSensor(UltrasoundSensorDictionary sensor) {
        String sql = "UPDATE ultrasoundsensor_dictionary SET name = ?, type = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sensor.getName());
            stmt.setString(2, sensor.getType());
            stmt.setInt(3, sensor.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Удалить запись
    public static boolean deleteSensor(int id) {
        String sql = "DELETE FROM ultrasoundsensor_dictionary WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить запись по ID
    public static UltrasoundSensorDictionary getSensorById(int id) {
        String sql = "SELECT * FROM ultrasoundsensor_dictionary WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToSensor(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean isModelUnique(String name, String model, int excludeId) {
        String sql = "SELECT COUNT(*) FROM ultrasoundsensor_dictionary " +
                "WHERE name = ? AND type = ? AND id <> ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, model);
            pstmt.setInt(3, excludeId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0; // true, если уникально
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Вспомогательный метод для маппинга
    private static UltrasoundSensorDictionary mapRowToSensor(ResultSet rs) throws SQLException {
        return new UltrasoundSensorDictionary(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("type")
        );
    }
}