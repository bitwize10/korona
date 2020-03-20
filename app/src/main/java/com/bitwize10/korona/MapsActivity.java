package com.bitwize10.korona;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.CheckedInputStream;

import au.com.bytecode.opencsv.CSVReader;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        ClusterManager.OnClusterClickListener<ClusterItem>,
        ClusterManager.OnClusterItemClickListener<ClusterItem> {

    private static String DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";

    private GoogleMap mMap;
    private ClusterManager<ClusterItem> mClusterManager;
    private Typeface mTF;

    private String mSelectedCountry = "World";
    private String mLastDate = "";

    private HashMap<String, Country> mCountries = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_maps);

        // transparent statusbar
        getWindow().getDecorView().setSystemUiVisibility(
                          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);


        // set custom font to TextView
        TextView tv = findViewById(R.id.map_overlay);
        mTF = ResourcesCompat.getFont(getApplicationContext(), R.font.audiowide_regular);
        tv.setTypeface(mTF);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);


        if (savedInstanceState != null) {
            mSelectedCountry = savedInstanceState.getString("mSelectedCountry", mSelectedCountry);
        }

        requestData();

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mSelectedCountry", mSelectedCountry);
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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.mapstyle_korona));
        mMap.setMaxZoomPreference(8.0f);
        mMap.setPadding(0, 0, 0, dp2px(75)); // left, top, right, bottom

        // disable the features we don't need
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setBuildingsEnabled(false);

        // add listener
        mMap.setOnMapClickListener(this);

        // cluster setup
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setAnimation(false);

        // custom cluster renderer
        mClusterManager.setRenderer(new ClusterRenderer(getApplicationContext(), mMap, mClusterManager, mTF, getResources()));

        // Point the map's listeners at the listeners implemented by the cluster manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

    }


    private void showMap() {
        // hide map overlay and show map
        findViewById(R.id.map_overlay).setVisibility(View.GONE);
    }


    private void requestData() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, DATA_URL,
                response -> {
                    CSVReader reader = new CSVReader(new StringReader(response));
                    try {
                        fillCountries(reader.readAll());
                        showData(mSelectedCountry); // show world data by default
                        addMarkers();
                    } catch (IOException e) {
                        e.printStackTrace();
                        log("ERROR reading data");
                        TextView tv1 = findViewById(R.id.tv_text1);
                        tv1.setText(getString(R.string.error_reading_data));
                    }
                    showMap();
                },
                error -> {
                    log("ERROR requesting data");
                    TextView tv1 = findViewById(R.id.tv_text1);
                    tv1.setText(getString(R.string.error_requesting_data));
                    showMap();
                }
        );
        final RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }


    // shows data at the bottom of the screen
    private void showData(String countryName) {

        Country country = mCountries.get(countryName);
        if (country == null) return;

        TextView tv1 = findViewById(R.id.tv_text1);
        TextView tv2 = findViewById(R.id.tv_text2);
        TextView tv3 = findViewById(R.id.tv_text3);
        tv1.setTypeface(mTF); tv2.setTypeface(mTF); tv3.setTypeface(mTF);

        int change = country.casesToday() - country.casesYesterday();
        char sign = (change < 0)? '-' : '+';

        String today = NumberFormat.getInstance().format(country.casesToday());
        today += " ("+sign+NumberFormat.getInstance().format(change)+")";

        switch (country.daysNoChanges()) {
            case 0:
            case 1:
                tv1.setTextColor(getResources().getColor(R.color.red));
                tv2.setTextColor(getResources().getColor(R.color.red));
                tv3.setTextColor(getResources().getColor(R.color.red));
                break;
            case 2:
            case 3:
            case 4:
                tv1.setTextColor(getResources().getColor(R.color.orange));
                tv2.setTextColor(getResources().getColor(R.color.orange));
                tv3.setTextColor(getResources().getColor(R.color.orange));
                break;
            case 5:
            case 6:
                tv1.setTextColor(getResources().getColor(R.color.yellow));
                tv2.setTextColor(getResources().getColor(R.color.yellow));
                tv3.setTextColor(getResources().getColor(R.color.yellow));
                break;
            default:
                tv1.setTextColor(getResources().getColor(R.color.green));
                tv2.setTextColor(getResources().getColor(R.color.green));
                tv3.setTextColor(getResources().getColor(R.color.green));
        }

        tv1.setText(country.getName()); // world or country
        tv2.setText(formatDate(mLastDate)); // last date
        tv3.setText(today);

        updateChart(country);

    }


    private void updateChart(Country country) {

        ChartView chart = findViewById(R.id.chart);
        chart.setVisibility(View.VISIBLE);
        chart.setCountry(country);
        chart.invalidate(); // redraw

    }


    // fills countries with data
    private void fillCountries(List<String[]> allData) {

        // header is: Province/State,Country/Region,Lat,Long,day1,day2,...
        String[] header = allData.get(0);
        mLastDate = header[header.length-1];

        String provinceName, countryName, lat, lon;
        Country country;
        int[] data;
        int[] worldData = new int[allData.get(0).length-4];

        for (int row = 1; row < allData.size(); row++) { // skip first row
            String[] rowData = allData.get(row);
            provinceName = rowData[0];
            countryName = rowData[1];
            lat = rowData[2];
            lon = rowData[3];

            if (!provinceName.isEmpty()) countryName = provinceName;
            country = new Country(countryName, lat, lon);

            // read the data
            data = new int[rowData.length-4];
            for (int column = 4; column < rowData.length; column++) { // skip first 4 columns
                data[column-4] = Integer.parseInt(rowData[column]);
                worldData[column-4] += data[column-4];
            }
            country.setData(data);

            // add country
            mCountries.put(countryName, country);

        }

        // add world
        country = new Country("World");
        country.setData(worldData);
        mCountries.put(country.getName(), country);

    }


    private void addMarkers() {

        int fontSize = dp2px(12f);
        int fontColor = getResources().getColor(R.color.darkRed1);

        int backgroundColor1r = getResources().getColor(R.color.darkerRed);
        int backgroundColor2r = getResources().getColor(R.color.red);
        int backgroundColor1o = getResources().getColor(R.color.darkerOrange);
        int backgroundColor2o = getResources().getColor(R.color.orange);
        int backgroundColor1y = getResources().getColor(R.color.darkerYellow);
        int backgroundColor2y = getResources().getColor(R.color.yellow);
        int backgroundColor1g = getResources().getColor(R.color.darkerGreen);
        int backgroundColor2g = getResources().getColor(R.color.green);
        int color1, color2;

        String today;
        Bitmap icon;

        for (Country country : mCountries.values()) {
            if (country.casesToday() == 0) continue;
            LatLng coords = country.getCoords();
            if (coords.longitude != 0 && coords.longitude != 0) {

                switch (country.daysNoChanges()) {
                    case 0:
                    case 1:
                        color1 = backgroundColor1r;
                        color2 = backgroundColor2r;
                        break;
                    case 2:
                    case 3:
                    case 4:
                        color1 = backgroundColor1o;
                        color2 = backgroundColor2o;
                        break;
                    case 5:
                    case 6:
                        color1 = backgroundColor1y;
                        color2 = backgroundColor2y;
                        break;
                    default:
                        color1 = backgroundColor1g;
                        color2 = backgroundColor2g;
                }

                today = NumberFormat.getInstance().format(country.casesToday());
                icon = textAsBitmap(today, fontSize, fontColor, mTF, color1, color2);

                // add to cluster
                ClusterItem clusterItem = new ClusterItem(country, icon);
                clusterItem.setSelected(mSelectedCountry.equals(country.getName()));
                mClusterManager.addItem(clusterItem);

            }
        }

        // force a re-cluster
        mClusterManager.cluster();

    }


    @Override
    public void onMapClick(LatLng point) {
        //log("clicked on map: " +point.latitude+ ", "+point.longitude);
        mSelectedCountry = "World";
        showData(mSelectedCountry);
    }


    @Override
    public boolean onClusterClick(Cluster<ClusterItem> cluster) {
        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (com.google.maps.android.clustering.ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    // individual item
    @Override
    public boolean onClusterItemClick(ClusterItem item) {
        Country country = item.getCountry();
        if (country != null) {
            showData(country.getName());
            mSelectedCountry = country.getName();
        }
        return false; // consume event? (false by default - do not consume event and center camera)
    }



    @SuppressLint("SimpleDateFormat")
    private String formatDate(String date_string) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy"); // example 1/28/20
        Date date = null;
        try {
            date = sdf.parse(date_string);
        } catch (ParseException e) {
            log("ERROR formatting date: " +e.toString());
        }
        if (date != null) return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        else return date_string;
    }


    public static Bitmap textAsBitmap(final String text, final int fontSize, int fontColor, Typeface typeface, int bgColor1, int bgColor2) {

        // text paint
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(fontSize);
        paint.setColor(fontColor);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(typeface);
        paint.setElegantTextHeight(true);

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int width = rect.width();
        int height = rect.height();

        final int paddingTB = dp2px(10f);
        final int paddingLR = dp2px(12f);
        width  += paddingLR;
        height += paddingTB;


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);


        // draw background
        Paint rect_paint = new Paint(ANTI_ALIAS_FLAG);
        rect_paint.setStyle(Paint.Style.FILL);
        LinearGradient gradient = new LinearGradient(
                0, 0,
                0, height,
                bgColor1, // start color
                bgColor2, // end color
                Shader.TileMode.CLAMP);
        rect_paint.setShader(gradient);
        int rounded = dp2px(4.5f);
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rectF, rounded, rounded, rect_paint);

        // draw background stroke
        int strokeWidth = dp2px(1.5f);
        rect_paint.reset();
        rect_paint.setStyle(Paint.Style.STROKE);
        rect_paint.setColor(fontColor);
        rect_paint.setStrokeWidth(strokeWidth);
        rectF = new RectF(strokeWidth/2f, strokeWidth/2f, width-strokeWidth/2f, height-strokeWidth/2f);
        rounded = dp2px(3.8f);
        canvas.drawRoundRect(rectF, rounded, rounded, rect_paint);


        // draw text
        canvas.getClipBounds(rect);
        canvas.drawText(text, (paddingLR/2f)-1, (height - paddingTB/2f)-1, paint);

        return bitmap;

    }


    private static void log(String msg) {
        Log.i("MapsActivity", msg);
    }

    private static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    static int dp2px(float dp) {
        return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
    }


}
