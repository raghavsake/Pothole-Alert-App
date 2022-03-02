package com.raghav.pospe_detector.data;


import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class LocationData {


    public String id;
    double longitude;
    double latitude;

    public LocationData() {}

    public LocationData(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationData that = (LocationData) o;
        return Objects.equals(id, that.id);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
