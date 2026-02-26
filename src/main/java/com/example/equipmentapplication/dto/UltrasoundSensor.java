package com.example.equipmentapplication.dto;

public class UltrasoundSensor {
    private int id;
    private int sensorDictionaryId;
    private String sensorName;
    private String sensorType;
    private String snNumber;
    private String note;
    private int equipmentId;

    public int getId() {
        return id;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getSensorType() {
        return sensorType;
    }

    public String getSnNumber() {
        return snNumber;
    }

    public String getNote() {
        return note;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public int getSensorDictionaryId() {
        return sensorDictionaryId;
    }

    public UltrasoundSensor(int id, int sensorDictionaryId, String sensorName, String sensorType, String snNumber, String note, int equipmentId) {
        this.id = id;
        this.sensorDictionaryId = sensorDictionaryId;
        this.sensorName = sensorName;
        this.sensorType = sensorType;
        this.snNumber = snNumber;
        this.note = note;
        this.equipmentId = equipmentId;
    }
}
