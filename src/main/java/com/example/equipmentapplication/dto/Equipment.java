package com.example.equipmentapplication.dto;

public class Equipment {
    private int id;
    private String name;
    private String model;
    private String snNumber;
    private String note;
    private int officeId;
    private String status;
    private String qrCode;
    private int equipmentTypeId;

    public Equipment(int id, String name, String model, String snNumber, String note, String status, int officeId, int equipmentTypeId, String qrCode) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.snNumber = snNumber;
        this.note = note;
        this.officeId = officeId;
        this.status = status;
        this.equipmentTypeId = equipmentTypeId;
        this.qrCode = qrCode;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getSnNumber() {
        return snNumber;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getNote() {
        return note;
    }

    public int getOfficeId() {
        return officeId;
    }

    public String getStatus() {
        return status;
    }

    public int getEquipmentTypeId() {
        return equipmentTypeId;
    }
}