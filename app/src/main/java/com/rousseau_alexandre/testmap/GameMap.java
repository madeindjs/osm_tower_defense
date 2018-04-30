package com.rousseau_alexandre.testmap;

import android.content.Context;
import android.location.Location;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

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

        for (int i = 0;i < quantity; i++) {
            double newLatitude = this.getDoubleBetween(latitudeMin, latitudeMax);
            double newLongitude = this.getDoubleBetween(longitudeMin, longitudeMax);;
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


}
