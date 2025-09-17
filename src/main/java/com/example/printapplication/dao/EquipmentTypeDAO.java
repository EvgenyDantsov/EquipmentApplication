package com.example.printapplication.dao;

import com.example.printapplication.DatabaseHelper;
import com.example.printapplication.dto.EquipmentType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentTypeDAO {
    public static List<EquipmentType> getAllEquipmentTypes() {
        List<EquipmentType> list = new ArrayList<>();
        String sql = "SELECT id, name FROM equipmentType";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new EquipmentType(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static boolean addEquipmentType(String name) {
        String sql = "INSERT INTO equipmenttype (name) VALUES (?)";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
        public static boolean updateEquipmentType(int id, String newName) {
            String sql = "UPDATE equipmenttype SET name = ? WHERE id = ?";
            try (Connection connection = DatabaseHelper.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean deleteEquipmentType(int id) {
            String sql = "DELETE FROM equipmenttype WHERE id = ?";
            try (Connection connection = DatabaseHelper.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
}
