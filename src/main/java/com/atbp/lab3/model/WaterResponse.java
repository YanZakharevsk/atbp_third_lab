package com.atbp.lab3.model;

public class WaterResponse {
    private String status;
    private double waterNorm;
    private double temperature;
    private String message;

    public WaterResponse() {}

    public WaterResponse(String status, double waterNorm, double temperature, String message) {
        this.status = status;
        this.waterNorm = waterNorm;
        this.temperature = temperature;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getWaterNorm() { return waterNorm; }
    public void setWaterNorm(double waterNorm) { this.waterNorm = waterNorm; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
