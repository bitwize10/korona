package com.bitwize10.korona;

public class Country {

    private String name = "";
    private int column = 0; // column in CSV data file
    private float latitude = 0.0f;
    private float longitude = 0.0f;
    private int cases_today = 0;
    private int cases_yesterday = 0;

    public Country(String name, float latitude, float longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getCases_today() {
        return cases_today;
    }

    public void setCases_today(int cases_today) {
        this.cases_today = cases_today;
    }

    public int getCases_yesterday() {
        return cases_yesterday;
    }

    public void setCases_yesterday(int cases_yesterday) {
        this.cases_yesterday = cases_yesterday;
    }

    public String getName() {
        return name;
    }

    public int getColumn() {
        return column;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

}
