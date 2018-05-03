package com.rousseau_alexandre.testmap;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.TileProvider;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameMap {

    private final MapView mapView;
    private final Context context;
    private final Random random;
    private final ArrayList<Enemy> enemies = new ArrayList<Enemy>();


    public GameMap(Context context, MapView mapView) {
        this.random = new Random();
        this.context = context;
        this.mapView = mapView;

        this.mapView.setTileSource(TileSourceFactory.MAPNIK);
        this.mapView.setMultiTouchControls(true);
        // Built in Zoom control is required for emulator
        // map.setBuiltInZoomControls(false);

        this.mapView.setOverlayManager(MyOverlayManager.create(mapView, context));
    }

    public void setMyLocation() {
        GpsMyLocationProvider myLocationProvider = new GpsMyLocationProvider(context);
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(myLocationProvider, this.mapView);
        mLocationOverlay.enableMyLocation();
        this.mapView.getOverlays().add(mLocationOverlay);
    }

    public MapView getMapView() {
        return mapView;
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
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

    public ArrayList<Enemy> createEnemiesAround(Location location, int quantity) {
        final double range = 0.01;
        //
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        //
        final double latitudeMin = latitude - range;
        final double latitudeMax = latitude + range;
        //
        final double longitudeMin = longitude - range;
        final double longitudeMax = longitude + range;

        ArrayList<Enemy> newEnemies = new ArrayList<Enemy>();

        for (int i = 0; i < quantity; i++) {
            Enemy enemy = new Enemy(this.mapView, this.context);

            double newLatitude = this.getDoubleBetween(latitudeMin, latitudeMax);
            double newLongitude = this.getDoubleBetween(longitudeMin, longitudeMax);
            enemy.setPosition(new GeoPoint(newLatitude, newLongitude));

            newEnemies.add(enemy);
            enemies.add(enemy);
        }

        this.mapView.getOverlays().addAll(enemies);

        return newEnemies;
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
     * Add marker on the map & return it
     *
     * @param location
     * @return
     */
    public Enemy createEnemy(GeoPoint location) {
        Enemy enemy = new Enemy(this.mapView, this.context);
        enemy.setPosition(location);
        enemy.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        this.mapView.getOverlays().add(enemy);
        this.enemies.add(enemy);

        return enemy;
    }


}
