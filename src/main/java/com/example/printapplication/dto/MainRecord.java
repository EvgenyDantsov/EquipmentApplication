package com.example.printapplication.dto;

public class MainRecord {
    private String nameOffice;
    private String numberOffice;
    private String nameDepartment;
    private String nameEquipment;
    private String model;
    private String snNumber;
    private String note;
    private String status;
    private String fio;
    private int equipmentTypeId;

    private int equipmentId;

    public MainRecord(String nameOffice, String numberOffice, String nameDepartment,
                      String nameEquipment, String model, String snNumber,
                      String note, String status, String fio) {
        this.nameOffice = nameOffice;
        this.numberOffice = numberOffice;
        this.nameDepartment = nameDepartment;
        this.nameEquipment = nameEquipment;
        this.model = model;
        this.snNumber = snNumber;
        this.note = note;
        this.status=status;
        this.fio = fio;
    }

    // Геттеры
    public String getNameOffice() {
        return nameOffice;
    }

    public String getNumberOffice() {
        return numberOffice;
    }

    public String getNameDepartment() {
        return nameDepartment;
    }

    public String getNameEquipment() {
        return nameEquipment;
    }

    public String getModel() {
        return model;
    }

    public String getSnNumber() {
        return snNumber;
    }

    public String getNote() {
        return note;
    }
    public String getStatus() {
        return status;
    }

    public String getFio() {
        return fio;
    }
    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }
    public int getEquipmentTypeId() {
        return equipmentTypeId;
    }
    public void setEquipmentTypeId(int equipmentTypeId) {
        this.equipmentTypeId = equipmentTypeId;
    }
}