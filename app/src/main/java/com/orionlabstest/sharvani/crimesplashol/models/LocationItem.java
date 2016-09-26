
package com.orionlabstest.sharvani.crimesplashol.models;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class LocationItem implements ClusterItem {
    private final LatLng mPosition;
    int hue;
    String district;
    int color;

    public LocationItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }


    public void setHue(int hue) {
        this.hue=hue;
    }

    public int getHue() {

        return hue;
    }

    public void addDistrict(String district) {
        this.district=district;
    }

    public String getDistrict() {
        return district;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
