package com.bitwize10.korona;

import com.google.android.gms.maps.model.LatLng;

public class Country {

    private String name = "";
    private int column = 0; // column in CSV data file

    private double latitude = 0.0;
    private double longitude = 0.0;

    private int cases_today = 0;
    private int cases_yesterday = 0;
    private int cases_2days_ago = 0;

    public Country(String name) {
        this.name = name;
    }

    public Country(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int casesToday() {
        return cases_today;
    }

    public int casesYesterday() {
        return cases_yesterday;
    }

    public int cases2daysAgo() {
        return cases_2days_ago;
    }

    public void setCasesToday(int cases_today) {
        this.cases_today = cases_today;
    }

    public void setCasesYesterday(int cases_yesterday) {
        this.cases_yesterday = cases_yesterday;
    }

    public void setCases2daysAgo(int cases_2days_ago) {
        this.cases_2days_ago = cases_2days_ago;
    }

    public String getName() {
        return name;
    }

    public int getColumn() {
        return column;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getCoords(){
        return new LatLng(latitude, longitude);
    }

}
