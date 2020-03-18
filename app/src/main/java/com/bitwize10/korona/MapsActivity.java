package com.bitwize10.korona;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

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

import au.com.bytecode.opencsv.CSVReader;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener {

    static public String DATA_URL = "https://covid.ourworldindata.org/data/total_cases.csv";

    private GoogleMap mMap;

    private List<String[]> mAllData;
    private HashMap<String, Country> mCountryCoords = new HashMap<>();

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        readCoordinates();
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

        // add listeners
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

    }

    private void showMap() {
        // hide map overlay and show map
        findViewById(R.id.map_overlay).setVisibility(View.GONE);
    }


    private void requestData() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, DATA_URL,
                response -> {
                    //log("CSV data: " +response);
                    CSVReader reader = new CSVReader(new StringReader(response));
                    try {
                        mAllData = reader.readAll();
                        fillCountries();
                        showData("World"); // show world data by default
                        showMap();
                        addMarkers();
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

    // shows data at the bottom of the screen
    private void showData(String countryName) {

        Country country = mCountryCoords.get(countryName);
        if (country == null) return;

        TextView tv1 = findViewById(R.id.tv_text1);
        TextView tv2 = findViewById(R.id.tv_text2);
        TextView tv3 = findViewById(R.id.tv_text3);

        String date = mAllData.get(mAllData.size()-1)[0]; // last date in data

        int change = country.casesToday() - country.casesYesterday();
        char sign = (change < 0)? '-' : '+'; // just in case it goes down somehow

        // today's data and change
        String today = NumberFormat.getInstance().format(country.casesToday());
        today += " ("+sign+NumberFormat.getInstance().format(change)+")";

        if (country.casesToday() == country.cases2daysAgo()) {
            tv1.setTextColor(getResources().getColor(R.color.green));
            tv2.setTextColor(getResources().getColor(R.color.green));
            tv3.setTextColor(getResources().getColor(R.color.green));
        } else {
            tv1.setTextColor(getResources().getColor(R.color.red));
            tv2.setTextColor(getResources().getColor(R.color.red));
            tv3.setTextColor(getResources().getColor(R.color.red));
        }

        tv1.setText(country.getName()); // world or country
        tv2.setText(formatDate(date)); // last date
        tv3.setText(today);

        updateChart(country.getColumn());

    }

    private void updateChart(int column) {

        int[] data = new int[mAllData.size()-1]; // skip 1st line
        int cases;
        String value;
        for (int i = 1; i < mAllData.size(); i++) {
            value = mAllData.get(i)[column];
            if (value.isEmpty()) {
                data[i-1] = 0;
            } else {
                try {
                    cases = Integer.parseInt(value);
                    data[i-1] = cases;
                } catch (NumberFormatException e) {
                    data[i-1] = 0;
                }
            }
        }

        ChartView chart = findViewById(R.id.chart);
        chart.setVisibility(View.VISIBLE);
        chart.setData(data);
        chart.invalidate(); // redraw

    }

    // fills countries with data
    private void fillCountries() {

        String[] header = mAllData.get(0); // read the header: date, world, country1, country2, ...
        String countryName, today, yesterday, twoDaysAgo;
        for (int column = 1; column < header.length; column++) { // skip first column
            countryName = header[column];

            if (mAllData.size() > 2) {
                today = mAllData.get(mAllData.size() - 1)[column]; // number of cases today
                yesterday = mAllData.get(mAllData.size() - 2)[column]; // number of cases yesterday
                twoDaysAgo = mAllData.get(mAllData.size() - 3)[column]; // number of cases 2 days ago

                int today_i, yesterday_i, twoDaysAgo_i;
                try {
                    twoDaysAgo_i = Integer.parseInt(twoDaysAgo);
                } catch (NumberFormatException e) {
                    twoDaysAgo_i = 0;
                }
                try {
                    yesterday_i = Integer.parseInt(yesterday);
                } catch (NumberFormatException e) {
                    yesterday_i = 0;
                }
                try {
                    today_i = Integer.parseInt(today);
                } catch (NumberFormatException e) {
                    today_i = yesterday_i;
                }

                Country country = mCountryCoords.get(countryName);
                if (country != null) {
                    country.setColumn(column);
                    country.setCasesToday(today_i);
                    country.setCasesYesterday(yesterday_i);
                    country.setCases2daysAgo(twoDaysAgo_i);
                } else { // country does not have coordinates (World also falls here)
                    Country c = new Country(countryName);
                    c.setColumn(column);
                    c.setCasesToday(today_i);
                    c.setCasesYesterday(yesterday_i);
                    c.setCases2daysAgo(twoDaysAgo_i);
                    mCountryCoords.put(countryName, c);
                }
            }

        }

    }

    // read country coordinates from CSV file
    private void readCoordinates() {

        Context ctx = getApplicationContext();
        String datafile = "countries"; // filename *without* extension!

        try {
            InputStream ins = ctx.getResources().openRawResource(
                    ctx.getResources().getIdentifier(datafile, "raw",
                            ctx.getPackageName())
            );
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                double lat = Double.parseDouble(data[0]);
                double lon = Double.parseDouble(data[1]);
                String countryName = data[2];
                mCountryCoords.put(countryName, new Country(countryName, lat, lon));
            }

            br.close();
        } catch (FileNotFoundException e) {
            log("ERROR file not found: " +e);
        } catch (IOException e) {
            log("ERROR reading file: " +e);
        } catch (NumberFormatException e) {
            log("ERROR parsing coordinates: " +e);
        }

    }


    private void addMarkers() {

        int fontSize = dp2px(15);
        int fontColor = getResources().getColor(R.color.darkRed1);
        int backgroundColor1r = getResources().getColor(R.color.colorPrimary);
        int backgroundColor2r = getResources().getColor(R.color.red);
        int backgroundColor1g = getResources().getColor(R.color.darkerGreen);
        int backgroundColor2g = getResources().getColor(R.color.green);
        int color1, color2;

        String today;
        Bitmap icon;

        for (Country country : mCountryCoords.values()) {
            if (country.casesToday() == 0) continue;
            LatLng coords = country.getCoords();
            if (coords.longitude != 0 && coords.longitude != 0) {

                if (country.casesToday() == country.cases2daysAgo()) {
                    color1 = backgroundColor1g;
                    color2 = backgroundColor2g;
                } else {
                    color1 = backgroundColor1r;
                    color2 = backgroundColor2r;
                }
                today = NumberFormat.getInstance().format(country.casesToday());
                icon = textAsBitmap(today, fontSize, fontColor, color1, color2);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(coords)
                        .anchor(0.5f, 0.5f)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))
                        .title(country.getName())
                        ;

                Marker marker = mMap.addMarker(markerOptions);
                marker.setTag(country);

            }
        }

    }


    @Override
    public boolean onMarkerClick(Marker marker){
        //log("clicked on marker: " +marker.getId());

        Country country = (Country) marker.getTag();
        if (country != null) {
            showData(country.getName());
        }

        return false; // consume event? (false by default - do not consume event and center camera)

    }

    @Override
    public void onMapClick(LatLng point) {
        //log("clicked on map: " +point.latitude+ ", "+point.longitude);
        showData("World");
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


    private static Bitmap textAsBitmap(final String text, final int fontSize, int fontColor, int bgColor1, int bgColor2) {

        // text paint
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(fontSize);
        paint.setColor(fontColor);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setElegantTextHeight(true);

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int width = rect.width();
        int height = rect.height();

        final int padding = dp2px(10f);
        width  += padding;
        height += padding;


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);


        // paint the background
        Paint rect_paint = new Paint(ANTI_ALIAS_FLAG);
        rect_paint.setStyle(Paint.Style.FILL);
        //rect_paint.setColor(backgroundColor);
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
        canvas.drawText(text, (padding/2f)-1, (height - padding/2f)-1, paint);

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