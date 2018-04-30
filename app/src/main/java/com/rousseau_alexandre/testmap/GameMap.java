package com.rousseau_alexandre.testmap;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.Random;

public class GameMap {

    private final MapView mapView;
    private final Context context;

    private final Random random;


    public GameMap(Context context, MapView mapView) {
        this.random = new Random();
        this.context = context;
        this.mapView = mapView;

        this.mapView.setTileSource(TileSourceFactory.MAPNIK);
        this.mapView.setMultiTouchControls(true);
        // Built in Zoom control is required for emulator
        // map.setBuiltInZoomControls(false);
    }

    public void setMyLocation() {
        GpsMyLocationProvider myLocationProvider = new GpsMyLocationProvider(context);
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(myLocationProvider, this.mapView);
        mLocationOverlay.enableMyLocation();
        this.mapView.getOverlays().add(mLocationOverlay);
    }

    public void setZoom(double zoomLevel, IGeoPoint point) {
        IMapController mapController = mapView.getController();
        mapController.setZoom(zoomLevel);
        mapController.setCenter(point);
    }

    public void addPoint(String title, String description, double latitude, double longitude) {
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem(title, description, new GeoPoint(latitude, longitude))); // Lat/Lon decimal degrees

        this.addPoints(items);
    }

    public void addPoints(ArrayList<OverlayItem> items) {
        //the overlay
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
                context,
                items,
                new ItemizedIconOverlay.OnItemGestureListener() {
                    @Override
                    public boolean onItemSingleTapUp(int index, Object item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, Object item) {
                        return false;
                    }
                }
        );
        mOverlay.setFocusItemsOnTap(true);

        this.mapView.getOverlays().add(mOverlay);

    }

    public void addPointsAround(Location location, int quantity) {
        final double range = 0.001;
        //
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        //
        final double latitudeMin = latitude - range;
        final double latitudeMax = latitude + range;
        //
        final double longitudeMin = longitude - range;
        final double longitudeMax = longitude + range;

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        for (int i = 0; i < quantity; i++) {
            double newLatitude = this.getDoubleBetween(latitudeMin, latitudeMax);
            double newLongitude = this.getDoubleBetween(longitudeMin, longitudeMax);
            ;
            items.add(new OverlayItem("Hello", "I'm a point", new GeoPoint(newLatitude, newLongitude))); // Lat/Lon decimal degrees
        }

        this.addPoints(items);
    }

    public double getDoubleBetween(double rangeMin, double rangeMax) {
        return rangeMin + (rangeMax - rangeMin) * this.random.nextDouble();
    }

    public void onResume() {
        this.mapView.onResume();
    }

    public void onPause() {
        this.mapView.onPause();
    }

    /**
     * Move a point
     * https://stackoverflow.com/questions/31337149/animating-markers-on-openstreet-maps-using-osmdroid
     *
     * @param marker
     * @param toPosition
     */
    public void animateMarker(final Marker marker, final GeoPoint toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = this.mapView.getProjection();
        Point startPoint = proj.toPixels(marker.getPosition(), null);
        final IGeoPoint startGeoPoint = proj.fromPixels(startPoint.x, startPoint.y);
        final long duration = 10000;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.getLongitude() + (1 - t) * startGeoPoint.getLongitude();
                double lat = t * toPosition.getLatitude() + (1 - t) * startGeoPoint.getLatitude();
                marker.setPosition(new GeoPoint(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 15);
                }
                GameMap.this.mapView.postInvalidate();
            }
        });
    }

    /**
     * Add marker on the map & return it
     *
     * @param location
     * @return
     */
    public Marker createMarker(GeoPoint location) {
        Marker marker = new Marker(this.mapView);
        marker.setPosition(location);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        this.mapView.getOverlays().add(marker);

        return marker;
    }


}
