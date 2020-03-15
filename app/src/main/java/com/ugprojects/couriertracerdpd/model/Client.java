package com.ugprojects.couriertracerdpd.model;

import com.ugprojects.couriertracerdpd.model.Package;

import java.util.Set;

public class Client {
    private String clientID;
    private String firstName;
    private String lastName;
    private long phoneNumber;
    private double latitude;
    private double longitude;

    public Client(String clientID, String firstName, String lastName, long phoneNumber, double latitude, double longitude) {
        this.clientID = clientID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
