package com.bitwize10.korona;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import au.com.bytecode.opencsv.CSVReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static public String DATA_URL = "https://covid.ourworldindata.org/data/total_cases.csv";

    private GoogleMap mMap;

    private List<String[]> mAllData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);



        requestData();


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this/*context*/, R.raw.mapstyle_korona));
        mMap.setMaxZoomPreference(12.0f);
        mMap.setPadding(0, 0, 0, dp2px(75)); // left, top, right, bottom


        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */


    }



    private void requestData() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, DATA_URL,
                response -> {
                    //log("CSV data: " +response);
                    CSVReader reader = new CSVReader(new StringReader(response));
                    try {
                        mAllData = reader.readAll();
                        showData(1); // show world data by default

                        /*
                        // TESTING
                        // read the header: date, world, countries ...
                        String[] header = mAllData.get(0);
                        log("header: " + TextUtils.join(", ", header));
                        // other lines
                        for (String[] line : mAllData) {
                            log(line[0]+ ": " +line[1]); // date, world
                        }
                        */

                    } catch (IOException e) {
                        e.printStackTrace();
                        log("ERROR reading data");
                        TextView tv1 = findViewById(R.id.tv_text1);
                        tv1.setText(getString(R.string.error_reading_data));
                    }
                },
                error -> {
                    log("ERROR requesting data");
                    TextView tv1 = findViewById(R.id.tv_text1);
                    tv1.setText(getString(R.string.error_requesting_data));
                }
        );
        final RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }


    // column 0 is date, column 1 is world, column 1+ is country
    private void showData(int column) {

        if (column < 1) return;
        if (column > mAllData.get(0).length-1) return;

        TextView tv1 = findViewById(R.id.tv_text1);
        TextView tv2 = findViewById(R.id.tv_text2);
        TextView tv3 = findViewById(R.id.tv_text3);

        String country = mAllData.get(0)[column];
        String date = mAllData.get(mAllData.size()-1)[0]; // last date in data
        String today = mAllData.get(mAllData.size()-1)[column]; // number of cases today
        String yesterday; // number of cases yesterday

        int today_i = Integer.parseInt(today);
        int yesterday_i = 0;
        int change = 0;
        String sign = "+";

        if (mAllData.size() > 1) {
            yesterday = mAllData.get(mAllData.size()-2)[column];
            yesterday_i = Integer.parseInt(yesterday);
            change = today_i - yesterday_i;
            if (change < 0) sign = "-";
        }

        // today's data and change
        today = NumberFormat.getInstance().format(today_i);
        today += " ("+sign+NumberFormat.getInstance().format(change)+")";

        tv1.setText(country); // world or country
        tv2.setText(formatDate(date)); // last date
        tv3.setText(today);

    }


    @SuppressLint("SimpleDateFormat")
    private String formatDate(String date_string) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(date_string);
        } catch (ParseException e) {
            log("ERROR formatting date: " +e.toString());
        }

        if (date != null) return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        else return date_string;

    }



    private static void log(String msg) {
        Log.i("MapsActivity", msg);
    }

    static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


}
