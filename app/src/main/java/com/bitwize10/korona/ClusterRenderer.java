package com.bitwize10.korona;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.text.NumberFormat;

import static com.bitwize10.korona.MapsActivity.dp2px;


public class ClusterRenderer extends DefaultClusterRenderer<ClusterItem> {

    private Context mCtx;
    private Typeface mTF;
    private NumberFormat mNF;
    private GoogleMap mMap;

    private int mFontSize;
    private int mFontColor;
    private int mBackgroundColor1r, mBackgroundColor2r;
    private int mBackgroundColor1o, mBackgroundColor2o;
    private int mBackgroundColor1y, mBackgroundColor2y;
    private int mBackgroundColor1g, mBackgroundColor2g;


    @SuppressWarnings("unchecked")
    ClusterRenderer(Context ctx, GoogleMap map, ClusterManager cm, Typeface tf, Resources res) {
        super(ctx, map, cm);

        mCtx = ctx;
        mTF = tf;
        mMap = map;

        mFontSize = dp2px(17f);
        mFontColor = res.getColor(R.color.darkRed1);

        mBackgroundColor1r = res.getColor(R.color.darkerRed);
        mBackgroundColor2r = res.getColor(R.color.red);
        mBackgroundColor1o = res.getColor(R.color.darkerOrange);
        mBackgroundColor2o = res.getColor(R.color.orange);
        mBackgroundColor1y = res.getColor(R.color.darkerYellow);
        mBackgroundColor2y = res.getColor(R.color.yellow);
        mBackgroundColor1g = res.getColor(R.color.darkerGreen);
        mBackgroundColor2g = res.getColor(R.color.green);

        mNF = NumberFormat.getInstance();

    }


    // Draw a single marker
    @Override
    protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {

        markerOptions
                .position(item.getPosition())
                .anchor(0.5f, 0.5f)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromBitmap(item.getIcon()))
                .title(item.getTitle())
                .zIndex(item.getCountry().getCasesToday())
                ;

        // custom font in info window
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout ll = new LinearLayout(mCtx);
                ll.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mCtx);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(mTF);
                title.setText(marker.getTitle());
                ll.addView(title);

                return ll;
            }
        });

    }


    // Draw multiple markers (a cluster)
    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterItem> cluster, MarkerOptions markerOptions) {

        int color1, color2;
        int clusterCasesToday = 0;
        int clusterCases2daysAgo = 0;
        int clusterCases6daysAgo = 0;
        int clusterCases10daysAgo = 0;

        for (ClusterItem ci : cluster.getItems()) {
            ci.setSelected(false);
            clusterCasesToday += ci.getCountry().getCasesToday();
            clusterCases2daysAgo += ci.getCountry().getCasesNdaysAgo(2);
            clusterCases6daysAgo += ci.getCountry().getCasesNdaysAgo(6);
            clusterCases10daysAgo += ci.getCountry().getCasesNdaysAgo(10);
        }

        if (clusterCasesToday == clusterCases10daysAgo) {
            color1 = mBackgroundColor1g;
            color2 = mBackgroundColor2g;
        } else if (clusterCasesToday == clusterCases6daysAgo) {
            color1 = mBackgroundColor1y;
            color2 = mBackgroundColor2y;
        } else if (clusterCasesToday == clusterCases2daysAgo) {
            color1 = mBackgroundColor1o;
            color2 = mBackgroundColor2o;
        } else {
            color1 = mBackgroundColor1r;
            color2 = mBackgroundColor2r;
        }

        String today = mNF.format(clusterCasesToday);
        Bitmap icon = MapsActivity.textAsBitmap(today, mFontSize, mFontColor, mTF, color1, color2);

        markerOptions
                .position(cluster.getPosition())
                .anchor(0.5f, 0.5f)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .zIndex(clusterCasesToday)
                ;

    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > 6;
    }


    @Override
    protected void onClusterItemUpdated(ClusterItem item, Marker marker) {
        item.setSelected(false);
    }


    @Override
    protected void onClusterItemRendered(ClusterItem item, Marker marker) {
        if (item.isSelected()) {
            marker.showInfoWindow();
        }
    }


}