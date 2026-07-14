package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.dto.EquipmentRepair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.equipmentapplication.DatabaseHelper;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class EquipmentRepairDAO {
    public static ObservableList<EquipmentRepair> getRepairsByEquipmentId(int equipmentId) {

        ObservableList<EquipmentRepair> repairs =
                FXCollections.observableArrayList();

        try (Connection connection =
                     DatabaseHelper.getConnection()) {

            String sql =
                    "SELECT * FROM equipment_repair " +
                            "WHERE equipment_id = ? " +
                            "ORDER BY repair_date DESC";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, equipmentId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {
                repairs.add(mapRowToRepair(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return repairs;
    }

    public static EquipmentRepair getRepairById(int id) {

        try (Connection connection =
                     DatabaseHelper.getConnection()) {

            String sql =
                    "SELECT * FROM equipment_repair WHERE id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, id);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {
                return mapRowToRepair(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean addRepair(
            int equipmentId,
            LocalDate repairDate,
            String malfunction,
            String workDone,
            BigDecimal cost
    ) {

        try (Connection connection =
                     DatabaseHelper.getConnection()) {

            String sql =
                    "INSERT INTO equipment_repair " +
                            "(equipment_id, repair_date, malfunction, work_done, cost) " +
                            "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, equipmentId);
            statement.setDate(2, Date.valueOf(repairDate));
            statement.setString(3, malfunction);
            statement.setString(4, workDone);

            if (cost == null) {
                statement.setNull(5, Types.DECIMAL);
            } else {
                statement.setBigDecimal(5, cost);
            }

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateRepair(
            int id,
            LocalDate repairDate,
            String malfunction,
            String workDone,
            BigDecimal cost
    ) {

        try (Connection connection =
                     DatabaseHelper.getConnection()) {

            String sql =
                    "UPDATE equipment_repair " +
                            "SET repair_date = ?, " +
                            "malfunction = ?, " +
                            "work_done = ?, " +
                            "cost = ? " +
                            "WHERE id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setDate(1, Date.valueOf(repairDate));
            statement.setString(2, malfunction);
            statement.setString(3, workDone);

            if (cost == null) {
                statement.setNull(4, Types.DECIMAL);
            } else {
                statement.setBigDecimal(4, cost);
            }

            statement.setInt(5, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRepair(int id) {

        try (Connection connection =
                     DatabaseHelper.getConnection()) {

            String sql =
                    "DELETE FROM equipment_repair WHERE id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static EquipmentRepair mapRowToRepair(ResultSet rs)
            throws SQLException {

        return new EquipmentRepair(
                rs.getInt("id"),
                rs.getInt("equipment_id"),
                rs.getDate("repair_date").toLocalDate(),
                rs.getString("malfunction"),
                rs.getString("work_done"),
                rs.getBigDecimal("cost")
        );
    }
}
