package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.dto.EquipmentHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class EquipmentHistoryDAO {
    // Получаем всю историю по конкретному оборудованию
    public static ObservableList<EquipmentHistory> getHistoryByEquipmentId(int equipmentId) {
        ObservableList<EquipmentHistory> historyList = FXCollections.observableArrayList();
        String sql = " SELECT equipment_history.id,\n" +
                "               equipment_history.equipment_id,\n" +
                "               equipment_history.office_id,\n" +
                "               equipment_history.responsible_id,\n" +
                "               equipment_history.status,\n" +
                "               equipment_history.action,\n" +
                "               equipment_history.details,\n"+
                "               equipment_history.change_date,\n" +
                "               equipment.name AS equipment_name,\n" +
                "               equipment.model AS equipment_model,\n" +
                "               office.number_office AS office_number,\n" +
                "               office.name_office AS office_name,\n" +
                "               seniordepartment.fio AS responsible_fio,\n" +
                "               department.department_name AS department_name\n" +
                "        FROM equipment_history\n" +
                "        JOIN equipment ON equipment_history.equipment_id = equipment.id\n" +
                "        JOIN office ON equipment_history.office_id = office.id\n" +
                "        JOIN seniordepartment ON equipment_history.responsible_id = seniordepartment.id\n" +
                "        LEFT JOIN department ON seniordepartment.department_id = department.id\n" +
                "        WHERE equipment_history.equipment_id = ?\n" +
                "        ORDER BY equipment_history.change_date DESC";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, equipmentId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                historyList.add(mapRowToHistory(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    // Вставка новой записи в историю
    public static boolean addHistory(Connection connection, int equipmentId, int officeId, int responsibleId, String status, String action, String details) {
        String sql = "INSERT INTO equipment_history (equipment_id, office_id, responsible_id, status, action, details) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, equipmentId);
            statement.setInt(2, officeId);
            statement.setInt(3, responsibleId);
            statement.setString(4, status);
            statement.setString(5, action);
            statement.setString(6, details);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Вспомогательный метод для маппинга ResultSet в объект
    private static EquipmentHistory mapRowToHistory(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int equipmentId = rs.getInt("equipment_id");
        int officeId = rs.getInt("office_id");
        int responsibleId = rs.getInt("responsible_id");
        String status = rs.getString("status");
        String note = rs.getString("action");
        Timestamp timestamp = rs.getTimestamp("change_date");
        LocalDateTime changeDate = timestamp.toLocalDateTime();
        String equipmentName = rs.getString("equipment_name");    // JOIN с equipment.name
        String equipmentModel = rs.getString("equipment_model");  // JOIN с equipment.model
        String officeNumber = rs.getString("office_number");      // JOIN с office.number_office
        String officeName = rs.getString("office_name");          // JOIN с office.name_office
        String responsibleFio = rs.getString("responsible_fio"); // JOIN с seniordepartment.fio
        String departmentName = rs.getString("department_name");  // если отделение хранится отдельно
        String details=rs.getString("details");
        return new EquipmentHistory(id, equipmentId, officeId, responsibleId,
                status, note, changeDate,
                equipmentName, equipmentModel,
                officeNumber, officeName,
                responsibleFio, departmentName,details);
    }
}
