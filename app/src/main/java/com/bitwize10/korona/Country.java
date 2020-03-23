package com.bitwize10.korona;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Country implements Comparable<Country> {

    private String name = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private int[] data;
    private int daysNoChange = 0;

    Country(String name) {
        this.name = name;
    }

    Country(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    Country(String name, String latitude, String longitude) {
        this.name = name;
        try {
            this.latitude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
            log("ERROR parsing coordinates: " +e);
        }
    }


    void setData(int[] data) {
        this.data = data;
        this.daysNoChange = daysNoChange();
    }

    private int daysNoChange(){
        int daysNoChanges = 0;
        int today = getCasesToday();
        for (int i = data.length-2; i >= 0; i--) {
            if (data[i] == today) daysNoChanges++;
            else break;
        }
        return daysNoChanges;
    }


    int[] getData() {
        return data;
    }

    int getChange() {
        return getCasesToday() - getCasesYesterday();
    }

    int getCasesToday() {
        return getCasesNdaysAgo(0);
    }

    int getCasesYesterday() {
        return getCasesNdaysAgo(1);
    }

    int getCasesNdaysAgo(int n) {
        if (data == null || n > data.length-1) return 0;
        return data[data.length-1-n];
    }

    int getDaysNoChange() {
        return daysNoChange;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setCoords(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    LatLng getCoords(){
        return new LatLng(latitude, longitude);
    }

    private static void log(String msg) {
        Log.i("Country", msg);
    }

    @Override
    public int compareTo(Country country) {
        // sort by change descending
        return Integer.compare(country.getChange(), this.getChange());
    }
}
