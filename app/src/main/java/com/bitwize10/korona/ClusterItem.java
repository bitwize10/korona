package com.bitwize10.korona;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class ClusterItem implements com.google.maps.android.clustering.ClusterItem {

    private String mSnippet = "Snippet";
    private Country mCountry;
    private Bitmap mIcon;
    private boolean mSelected = false;


    ClusterItem(Country country, Bitmap icon) {
        mCountry = country;
        mIcon = icon;
    }


    @Override
    public LatLng getPosition() {
        return mCountry.getCoords();
    }

    @Override
    public String getTitle() {
        return mCountry.getName();
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }


    Country getCountry() {
        return mCountry;
    }


    Bitmap getIcon() {
        return mIcon;
    }

    void setSelected(boolean selected) {
        mSelected = selected;
    }

    boolean isSelected(){
        return mSelected;
    }


}
