package com.bitwize10.korona;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Country {

    private String name = "";

    private double latitude = 0.0;
    private double longitude = 0.0;


    int[] data;

    public Country(String name) {
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

    /*
    public void setColumn(int column) {
        this.column = column;
    }
    */


    public void setData(int[] data) {
        this.data = data;
    }


    public int[] getData() {
        return data;
    }

    public int casesToday() {
        return casesNdaysAgo(0);
    }

    public int casesYesterday() {
        return casesNdaysAgo(1);
    }

    public int casesNdaysAgo(int n) {
        if (data == null || n > data.length-1) return 0;
        return data[data.length-1-n];
    }

    public int daysNoChanges(){
        int daysNoChanges = 0;
        int today = casesToday();
        for (int i = data.length-2; i >= 0; i--) {
            if (data[i] == today) {
                daysNoChanges++;
            } else {
                break;
            }
        }
        return daysNoChanges;
    }

    /*
    public int cases2daysAgo() {
        return cases_2days_ago;
    }
    */

    /*
    public void setCasesToday(int cases_today) {
        this.cases_today = cases_today;
    }

    public void setCasesYesterday(int cases_yesterday) {
        this.cases_yesterday = cases_yesterday;
    }

    public void setCases2daysAgo(int cases_2days_ago) {
        this.cases_2days_ago = cases_2days_ago;
    }
    */

    public String getName() {
        return name;
    }

    /*
    public int getColumn() {
        return column;
    }
    */

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    LatLng getCoords(){
        return new LatLng(latitude, longitude);
    }

    private static void log(String msg) {
        Log.i("Country", msg);
    }

}
