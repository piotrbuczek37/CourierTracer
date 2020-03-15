package com.ugprojects.couriertracerdpd.model;

import android.graphics.Color;

import java.util.Set;

public class Courier {
    private String courierID;
    private String firstName;
    private String lastName;
    private long phoneNumber;
    private String startTime;
    private String endTime;
    private String car;
    private int hhPin;
    private double latitude;
    private double longitude;

    public Courier(String courierID, String startTime, String endTime, String car, int hhPin) {
        this.courierID = courierID;
        this.firstName = null;
        this.lastName = null;
        this.phoneNumber = 0;
        this.startTime = startTime;
        this.endTime = endTime;
        this.car = car;
        this.hhPin = hhPin;
        this.latitude = 0;
        this.longitude = 0;
    }

    public String getCourierID() {
        return courierID;
    }

    public void setCourierID(String courierID) {
        this.courierID = courierID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public int getHhPin() {
        return hhPin;
    }

    public void setHhPin(int hhPin) {
        this.hhPin = hhPin;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
