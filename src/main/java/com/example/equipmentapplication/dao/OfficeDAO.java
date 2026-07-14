package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.dto.Office;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class OfficeDAO {
    public static ObservableList<Office> getAllOffice() {
        ObservableList<Office> office = FXCollections.observableArrayList();
        String sql = "SELECT * FROM office ORDER BY number_office ASC";
        try (Connection connection = DatabaseHelper.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String numberOffice = resultSet.getString("number_office");
                String nameOffice = resultSet.getString("name_office");
                int departmentId = resultSet.getInt("Department_id");
                office.add(new Office(id, numberOffice, nameOffice, departmentId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return office;
    }

    public static boolean addOffice(String numberOffice, String nameOffice, int departmentId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO office (number_office,name_office, Department_id) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, numberOffice);
            statement.setString(2, nameOffice);
            statement.setInt(3, departmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateOffice(int id, String numberOffice, String nameOffice, int departmentId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "UPDATE office SET number_office = ?, name_office = ?, Department_id = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, numberOffice);
            statement.setString(2, nameOffice);
            statement.setInt(3, departmentId);
            statement.setInt(4, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteOffice(int id) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "DELETE FROM office WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static int getDepartmentIdByOfficeId(int officeId) {

        String sql = "SELECT Department_id FROM office WHERE id = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, officeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Department_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Отдел не найден для офиса id = " + officeId);
    }
    public static String getOfficeNumberById(int officeId) {
        String officeNumber = "";
        String sql = "SELECT number_office FROM office WHERE id = ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, officeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                officeNumber = rs.getString("number_office");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return officeNumber;
    }
    public static Integer getOfficeIdByNumber(String officeNumber) {

        String sql =
                "SELECT id FROM office WHERE number_office = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, officeNumber);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}