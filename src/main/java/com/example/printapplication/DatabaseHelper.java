package com.example.printapplication;

import com.example.printapplication.config.Config;
import com.example.printapplication.dto.MainRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.List;

public class DatabaseHelper {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = Config.get("URL_SQL");
            String user = Config.get("USER_NAME_SQL");
            String password = Config.get("PASSWORD_SQL");
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }

    public static List<MainRecord> getAllView() {
        ObservableList<MainRecord> records = FXCollections.observableArrayList();
        String sql = "SELECT \n" +
                "    Office.name_office, \n" +
                "    Office.number_office, \n" +
                "    Department.department_name, \n" +
                "    equipment.name, \n" +
                "    equipment.model, \n" +
                "    equipment.sn_number, \n" +
                "    equipment.note, \n" +
                "    equipment.status, \n" +
                "    SeniorDepartment.fio,\n" +
                "    equipment.id, \n" +
                "    equipment.equipmenttype_id \n" +
                "FROM \n" +
                "    Office\n" +
                "INNER JOIN \n" +
                "    Department ON Office.Department_id = Department.id\n" +
                "INNER JOIN \n" +
                "    equipment ON Office.id = equipment.Office_id\n" +
                "INNER JOIN \n" +
                "    SeniorDepartment ON Department.id = SeniorDepartment.Department_id";
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                MainRecord record = new MainRecord(
                        resultSet.getString("name_office"),
                        resultSet.getString("number_office"),
                        resultSet.getString("department_name"),
                        resultSet.getString("name"),
                        resultSet.getString("model"),
                        resultSet.getString("sn_number"),
                        resultSet.getString("note"),
                        resultSet.getString("status"),
                        resultSet.getString("fio")
                );
                record.setEquipmentId(resultSet.getInt("id")); // Устанавливаем ID принтера
                record.setEquipmentTypeId(resultSet.getInt("equipmenttype_id"));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    public static List<MainRecord> getAllViewByType(int equipmentTypeId) {
        ObservableList<MainRecord> records = FXCollections.observableArrayList();
        String sql =
                "SELECT " +
                        "  Office.name_office, " +
                        "  Office.number_office, " +
                        "  Department.department_name, " +
                        "  equipment.name, " +
                        "  equipment.model, " +
                        "  equipment.sn_number, " +
                        "  equipment.note, " +
                        "  equipment.status, " +
                        "  SeniorDepartment.fio, " +
                        "  equipment.id, " +
                        "  equipment.equipmenttype_id " +
                        "FROM Office " +
                        "INNER JOIN Department ON Office.Department_id = Department.id " +
                        "INNER JOIN equipment  ON Office.id = equipment.Office_id " +
                        "INNER JOIN SeniorDepartment ON Department.id = SeniorDepartment.Department_id " +
                        "WHERE equipment.equipmentType_id = ?";

        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setInt(1, equipmentTypeId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    MainRecord r = new MainRecord(
                            rs.getString("name_office"),
                            rs.getString("number_office"),
                            rs.getString("department_name"),
                            rs.getString("name"),
                            rs.getString("model"),
                            rs.getString("sn_number"),
                            rs.getString("note"),
                            rs.getString("status"),
                            rs.getString("fio")
                    );
                    r.setEquipmentId(rs.getInt("id")); // лучше переименовать в setEquipmentId
                    r.setEquipmentTypeId(rs.getInt("equipmentType_id"));
                    records.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}