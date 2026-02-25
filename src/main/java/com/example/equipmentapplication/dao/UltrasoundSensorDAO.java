package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.dto.UltrasoundSensor;
import com.example.equipmentapplication.DatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UltrasoundSensorDAO {
    // Получить все датчики, привязанные к любому аппарату
    public static List<UltrasoundSensor> getAllSensors() {
        List<UltrasoundSensor> sensors = new ArrayList<>();
        String sql = "SELECT id, sensor_dictionary_id,sensor_name, sensor_type, sn_number, note, equipment_id FROM ultrasoundsensors";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sensors.add(new UltrasoundSensor(
                        rs.getInt("id"),
                        rs.getInt("sensor_dictionary_id"),
                        rs.getString("sensor_name"),
                        rs.getString("sensor_type"),
                        rs.getString("sn_number"),
                        rs.getString("note"),
                        rs.getInt("equipment_id")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sensors;
    }
    // Получить все датчики по конкретному УЗИ аппарату
    public static List<UltrasoundSensor> getSensorsByEquipmentId(int equipmentId) {
        List<UltrasoundSensor> list = new ArrayList<>();
        String sql = "SELECT * FROM ultrasoundsensors WHERE equipment_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new UltrasoundSensor(
                        rs.getInt("id"),
                        rs.getInt("sensor_dictionary_id"),
                        rs.getString("sensor_name"),
                        rs.getString("sensor_type"),
                        rs.getString("sn_number"),
                        rs.getString("note"),
                        rs.getInt("equipment_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Добавить датчик
    public static boolean addSensor(int dictionaryId, String name, String type, String snNumber, String note, int equipmentId) {
        String sql = "INSERT INTO ultrasoundsensors (sensor_dictionary_id, sensor_name, sensor_type, sn_number, note, equipment_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dictionaryId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setString(4, snNumber);
            ps.setString(5, note);
            ps.setInt(6, equipmentId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Обновить датчик
    public static boolean updateSensor(int id, int dictionaryId, String name, String type, String snNumber, String note, int equipmentId) {
        String sql = "UPDATE ultrasoundsensors SET sensor_dictionary_id = ?, sensor_name = ?, sensor_type = ?, sn_number = ?, note = ?, equipment_id = ? WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,dictionaryId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setString(4, snNumber);
            ps.setString(5, note);
            ps.setInt(6, equipmentId);
            ps.setInt(7, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Удалить датчик
    public static boolean deleteSensor(int id) {
        String sql = "DELETE FROM ultrasoundsensors WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Проверка уникальности серийного номера
    public static boolean isSnNumberUnique(String snNumber, int excludeId) {
        String sql = "SELECT COUNT(*) FROM ultrasoundsensors WHERE sn_number = ? AND id != ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, snNumber);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // true если уникален
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
