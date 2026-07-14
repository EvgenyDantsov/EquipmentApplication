package com.example.equipmentapplication.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EquipmentRepair {
    private int id;
    private int equipmentId;

    private LocalDate repairDate;

    private String malfunction;
    private String workDone;

    private BigDecimal cost;

    public EquipmentRepair() {
    }

    public EquipmentRepair(
            int id,
            int equipmentId,
            LocalDate repairDate,
            String malfunction,
            String workDone,
            BigDecimal cost
    ) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.repairDate = repairDate;
        this.malfunction = malfunction;
        this.workDone = workDone;
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public LocalDate getRepairDate() {
        return repairDate;
    }

    public String getMalfunction() {
        return malfunction;
    }

    public String getWorkDone() {
        return workDone;
    }

    public BigDecimal getCost() {
        return cost;
    }

}
