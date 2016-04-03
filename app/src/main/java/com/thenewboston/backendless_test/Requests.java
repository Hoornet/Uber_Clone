package com.thenewboston.backendless_test;

import android.util.Log;

import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ASHUTOSH on 3/13/2016.
 */
public class Requests{
    String driverUsername;
    String requesterUsername;
    GeoPoint mylocation;

    public GeoPoint getMylocation() {
        return mylocation;
    }

    public void setMylocation(GeoPoint mylocation) {
        this.mylocation = mylocation;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getDriverUsername() {
        return driverUsername;
    }

    public void setDriverUsername(String driverUsername) {
        this.driverUsername = driverUsername;
    }
}
