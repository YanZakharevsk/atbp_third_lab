package com.atbp.lab3.model;

public class WaterRequest {
    private double weight;
    private int activityMinutes;
    private String city;

    public WaterRequest() {}

    public WaterRequest(double weight, int activityMinutes, String city) {
        this.weight = weight;
        this.activityMinutes = activityMinutes;
        this.city = city;
    }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getActivityMinutes() { return activityMinutes; }
    public void setActivityMinutes(int activityMinutes) { this.activityMinutes = activityMinutes; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
