package com.arc.machine.dto;

public class MachineRecordDto {
    private Long id;
    private String serialNumber;
    private String partNumber;
    private String status;

    public MachineRecordDto() {
    }

    public MachineRecordDto(Long id, String serialNumber, String partNumber, String status) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.partNumber = partNumber;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
