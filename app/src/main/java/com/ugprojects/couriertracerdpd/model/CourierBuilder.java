package com.ugprojects.couriertracerdpd.model;

public class CourierBuilder {
    private String courierID;
    private String firstName;
    private String lastName;
    private String carInfo;
    private String phoneNumber;
    private int hhPin;
    private String startTime;
    private String endTime;
    private double latitude;
    private double longitude;

    public CourierBuilder withCourierID(String courierID){
        this.courierID = courierID;
        return this;
    }

    public CourierBuilder withFirstName(String firstName){
        this.firstName = firstName;
        return this;
    }

    public CourierBuilder withLastName(String lastName){
        this.lastName = lastName;
        return this;
    }

    public CourierBuilder withCarInfo(String carInfo){
        this.carInfo = carInfo;
        return this;
    }

    public CourierBuilder withPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
        return this;
    }

    public CourierBuilder withHhPin(int hhPin){
        this.hhPin = hhPin;
        return this;
    }

    public CourierBuilder withStartTime(String startTime){
        this.startTime = startTime;
        return this;
    }

    public CourierBuilder withEndTime(String endTime){
        this.endTime = endTime;
        return this;
    }

    public CourierBuilder withLatitude(double latitude){
        this.latitude = latitude;
        return this;
    }

    public CourierBuilder withLongitude(double longitude){
        this.longitude = longitude;
        return this;
    }

    public Courier build(){
        Courier courier = new Courier();
        courier.setCourierID(this.courierID);
        courier.setFirstName(this.firstName);
        courier.setLastName(this.lastName);
        courier.setCarInfo(this.carInfo);
        courier.setHhPin(this.hhPin);
        courier.setStartTime(this.startTime);
        courier.setEndTime(this.endTime);
        courier.setPhoneNumber(this.phoneNumber);
        courier.setLatitude(this.latitude);
        courier.setLongitude(this.longitude);

        return courier;
    }
}
