package com.ugprojects.couriertracerdpd.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.ugprojects.couriertracerdpd.model.Courier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsService {
    private Geocoder geocoder;
    private List<Address> addresses;

    public MapsService(Context context) {
        geocoder = new Geocoder(context, Locale.forLanguageTag("pl"));
        addresses = new ArrayList<>();
    }

    /**
     * This method converts the courier's coordinates to an address using Geocoder
     *
     * @param courier is the courier object with coordinates
     * @return address in plain text
     */
    public String getAddressOfCourierLocalization(Courier courier) {
        String address;
        try {
            addresses = geocoder.getFromLocation(courier.getLatitude(), courier.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            address = addresses.get(0).getAddressLine(0);
        } else {
            address = "Nieznany adres";
        }

        return address;
    }

    /**
     * This method gets coordinates of address from plain text and returns LatLng object with coordinates
     * If address was not found by Geocoder then returns null
     *
     * @param address is the address
     * @return object LatLng with coordinates or null if address was not found by Geocoder
     */
    public LatLng getLocationFromAddress(String address) {
        LatLng p1 = null;
        try {
            addresses = geocoder.getFromLocationName(address, 5);
            if (address == null) {
                return null;
            }
            Address location = addresses.get(0);

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p1;
    }

    /**
     * This method gets coordinates of courier localization
     *
     * @param courier is the courier object with coordinates
     * @return new LatLng object with couriers coordinates
     */
    public LatLng getCourierLocalization(Courier courier) {
        return new LatLng(courier.getLatitude(), courier.getLongitude());
    }

    /**
     * This method gets coordinates of location object
     *
     * @param location is the object with coordinates
     * @return new LatLng object with coordinates from location object
     */
    public LatLng getClientLocalization(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
