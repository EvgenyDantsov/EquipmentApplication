package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.dto.Equipment;
import com.example.equipmentapplication.dto.SeniorDepartment;
import com.example.equipmentapplication.util.QRCodeGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Equipment> getByModel(String model) {

        List<Equipment> equipments = new ArrayList<>();

        String sql = "SELECT * FROM equipment WHERE model = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, model);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipments.add(mapRowToEquipment(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return equipments;
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
        String qrCode = rs.getString("qr_code");
        return new Equipment(id, name, model, snNumber, note, status, officeId, equipmentTypeId, qrCode);
    }

    public static Equipment getEquipmentById(int id) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT * FROM equipment WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToEquipment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addEquipment(String name, String model, String snNumber, String note, String status, int officeId, int equipmentTypeId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO equipment (name, model, sn_number, note, status, Office_id, equipmenttype_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, model);
            statement.setString(3, snNumber);
            statement.setString(4, note);
            statement.setString(5, status);
            statement.setInt(6, officeId);
            statement.setInt(7, equipmentTypeId);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                // Получаем ID вставленного оборудования
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int equipmentId = generatedKeys.getInt(1);
                    String qrCode = String.format("EQ-%06d", equipmentId);
                    String updateQrSql =
                            "UPDATE equipment SET qr_code = ? WHERE id = ?";

                    PreparedStatement qrStatement =
                            connection.prepareStatement(updateQrSql);

                    qrStatement.setString(1, qrCode);
                    qrStatement.setInt(2, equipmentId);

                    qrStatement.executeUpdate();
                    int responsibleId = getResponsibleIdByOfficeId(officeId);
                    // Создаём запись в истории
                    EquipmentHistoryDAO.addHistory(connection, equipmentId, officeId, responsibleId, status, "Добавление", "-");
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEquipment(int id, String name, String model, String snNumber, String note, String status, int officeId, int equipmentTypeId) {
        Equipment oldEq = getEquipmentById(id);
        Equipment newEq = new Equipment(id, name, model, snNumber, note, status, officeId, equipmentTypeId, oldEq.getQrCode());
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
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                int responsibleId = getResponsibleIdByOfficeId(officeId);
                String details = buildDetails(oldEq, newEq);
                // ------------------ Логируем изменения ------------------
                EquipmentHistoryDAO.addHistory(connection, id, officeId, responsibleId, status, "Обновление", details);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEquipment(int id) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            // 1. Удаляем историю
            String deleteHistorySql = "DELETE FROM equipment_history WHERE equipment_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteHistorySql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
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

    public static boolean moveEquipment(int equipmentId, int newOfficeId, String note, String newStatus) {
        // Сначала полностью получаем старое оборудование (отдельное соединение)
        Equipment oldEq = getEquipmentById(equipmentId);
        if (oldEq == null) return false;

        Equipment newEq = new Equipment(
                equipmentId,
                oldEq.getName(),
                oldEq.getModel(),
                oldEq.getSnNumber(),
                note,
                newStatus,
                newOfficeId,
                oldEq.getEquipmentTypeId(),
                oldEq.getQrCode()
        );
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "UPDATE equipment SET Office_id = ?, note = ?, status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newOfficeId);
            pstmt.setString(2, note);
            pstmt.setString(3, newStatus);
            pstmt.setInt(4, equipmentId);

            int affectedRows = pstmt.executeUpdate();
            int responsibleId = getResponsibleIdByOfficeId(newOfficeId);
            if (affectedRows > 0) {
                String details = buildDetails(oldEq, newEq);
                // ------------------ Логируем перемещение ------------------
                EquipmentHistoryDAO.addHistory(conn, equipmentId, newOfficeId, responsibleId, newStatus, "Перемещение", details);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Equipment getEquipmentByQr(String qr) {

        String sql = """
        SELECT *
        FROM equipment
        WHERE qr_code = ?
    """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, qr);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Equipment(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("model"),
                        rs.getString("sn_number"),
                        rs.getString("note"),
                        rs.getString("status"),
                        rs.getInt("Office_id"),
                        rs.getInt("equipmenttype_id"),
                        rs.getString("qr_code")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static boolean moveEquipmentByQr(String qr, int newOfficeId, String note, String newStatus) {

        // 1. Получаем старое оборудование
        Equipment oldEq = getEquipmentByQr(qr);
        if (oldEq == null) return false;
        // ================= KEEP ОБРАБОТКА =================
        if ("KEEP".equals(newStatus)) {
            newStatus = oldEq.getStatus(); // 🔥 ВАЖНО: берём старый статус
        }

        Equipment newEq = new Equipment(
                oldEq.getId(),
                oldEq.getName(),
                oldEq.getModel(),
                oldEq.getSnNumber(),
                note,
                newStatus,
                newOfficeId,
                oldEq.getEquipmentTypeId(),
                oldEq.getQrCode()
        );

        try (Connection conn = DatabaseHelper.getConnection()) {

            String sql = """
            UPDATE equipment
            SET Office_id = ?, note = ?, status = ?
            WHERE qr_code = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newOfficeId);
            ps.setString(2, note);
            ps.setString(3, newStatus);
            ps.setString(4, qr);

            int affectedRows = ps.executeUpdate();

            int responsibleId = getResponsibleIdByOfficeId(newOfficeId);

            if (affectedRows > 0) {

                String details = buildDetails(oldEq, newEq);

                EquipmentHistoryDAO.addHistory(
                        conn,
                        oldEq.getId(),
                        newOfficeId,
                        responsibleId,
                        newStatus,
                        "Перемещение",
                        details
                );

                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int getResponsibleIdByOfficeId(int officeId) {

        int departmentId = OfficeDAO.getDepartmentIdByOfficeId(officeId);

        SeniorDepartment senior =
                SeniorDepartmentDAO.getByDepartmentId(departmentId);

        if (senior == null) {
            throw new RuntimeException("Материально ответственный не найден");
        }

        return senior.getId();
    }

    private static String getResponsibleFioByOfficeId(int officeId) {
        int departmentId = OfficeDAO.getDepartmentIdByOfficeId(officeId);
        SeniorDepartment senior = SeniorDepartmentDAO.getByDepartmentId(departmentId);
        if (senior == null) {
            throw new RuntimeException("Материально ответственный не найден");
        }
        return senior.getFio();
    }

    // Универсальный метод для формирования details
    private static String buildDetails(Equipment oldEq, Equipment newEq) {
        StringBuilder details = new StringBuilder();
        // Кабинет
        String oldOfficeNumber = OfficeDAO.getOfficeNumberById(oldEq.getOfficeId());
        String newOfficeNumber = OfficeDAO.getOfficeNumberById(newEq.getOfficeId());
        if (!oldOfficeNumber.equals(newOfficeNumber)) {
            details.append("Кабинет: ").append(oldOfficeNumber)
                    .append(" → ").append(newOfficeNumber).append("\n");
        }

        // ФИО материально ответственного
        String oldFio = getResponsibleFioByOfficeId(oldEq.getOfficeId());
        String newFio = getResponsibleFioByOfficeId(newEq.getOfficeId());
        if (!oldFio.equals(newFio)) {
            details.append("Ответственный: ").append(oldFio)
                    .append(" → ").append(newFio).append("\n");
        }
        // Примечание
        if (!oldEq.getNote().equals(newEq.getNote())) {
            details.append("Примечание: '").append(oldEq.getNote())
                    .append("' → '").append(newEq.getNote()).append("\n");
        }
        // Статус
        if (!oldEq.getStatus().equals(newEq.getStatus())) {
            details.append("Статус: ").append(oldEq.getStatus())
                    .append(" → ").append(newEq.getStatus()).append("\n");
        }
        // Серийный номер
        if (!oldEq.getSnNumber().equals(newEq.getSnNumber())) {
            details.append("Серийный номер: ").append(oldEq.getSnNumber())
                    .append(" → ").append(newEq.getSnNumber()).append("\n");
        }
        // Убираем последний "; "
        if (!details.isEmpty()) {
            details.setLength(details.length() - 1);
        }
        return details.toString();
    }
}