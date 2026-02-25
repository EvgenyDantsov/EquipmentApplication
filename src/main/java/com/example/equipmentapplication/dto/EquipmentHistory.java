package com.example.equipmentapplication.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EquipmentHistory {
    private int id;
    private int equipmentId;
    private int officeId;
    private int responsibleId;
    private String status;
    private String action;
    private LocalDateTime changeDate;
    // Дополнительные поля для отображения в таблице
    private String equipmentName;
    private String equipmentModel;
    private String officeNumber;
    private String officeName;
    private String responsibleFio;
    private String departmentName;
    private String details;

    public String getDetails() {
        return details;
    }

    public EquipmentHistory(int id, int equipmentId, int officeId, int responsibleId, String status, String action, LocalDateTime changeDate, String equipmentName, String equipmentModel, String officeNumber, String officeName, String responsibleFio, String departmentName, String details) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.officeId = officeId;
        this.responsibleId = responsibleId;
        this.status = status;
        this.action = action;
        this.changeDate = changeDate;
        this.equipmentName = equipmentName;
        this.equipmentModel = equipmentModel;
        this.officeNumber = officeNumber;
        this.officeName = officeName;
        this.responsibleFio = responsibleFio;
        this.departmentName = departmentName;
        this.details=details;
    }

    public int getId() {
        return id;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public int getOfficeId() {
        return officeId;
    }

    public int getResponsibleId() {
        return responsibleId;
    }

    public String getStatus() {
        return status;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getEquipmentModel() {
        return equipmentModel;
    }

    public String getOfficeNumber() {
        return officeNumber;
    }

    public String getOfficeName() {
        return officeName;
    }

    public String getResponsibleFio() {
        return responsibleFio;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getFormattedChangeDate() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
        return changeDate.format(formatter);
    }
}
