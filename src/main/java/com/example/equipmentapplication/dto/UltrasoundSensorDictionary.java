package com.example.equipmentapplication.dto;

public class UltrasoundSensorDictionary {
    private int id;
    private String name;
    private String type;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public UltrasoundSensorDictionary(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    @Override
    public String toString() {
        return name;
    }
}
