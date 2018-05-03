package com.rousseau_alexandre.testmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MainActivity extends Activity {


    private final LocationListener locationListener = new MyLocationListener();

    private static final long LOCATION_REFRESH_TIME = 5000;
    private static final float LOCATION_REFRESH_DISTANCE = 10;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2812;

    private GameMap gameMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for local environement
        // TODO: remove this (see: https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0#important-note-about-network-calls)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //handle permissions first, before map is created. not depicted here
        requestPermission();

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        IConfigurationProvider configuration = Configuration.getInstance();

        configuration.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string


        //inflate and create the map
        setContentView(R.layout.activity_main);
        View parentLayout = findViewById(android.R.id.content);
        MapView mapView = (MapView) findViewById(R.id.map);
        FloatingActionButton fab = findViewById(R.id.fab);

        Location location = getLocation();

        gameMap = new GameMap(ctx, mapView);
        gameMap.setMyLocation();
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            GeoPoint locationGeopoint = new GeoPoint(latitude, longitude);
            gameMap.setZoom(15.0, locationGeopoint);
            // gameMap.setZoom(17.0, locationGeopoint);
        }else{
            Snackbar.make(parentLayout, "Can't find your location", Snackbar.LENGTH_LONG)
                    .show();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = getLocation();

                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    GeoPoint locationGeopoint = new GeoPoint(latitude, longitude);
                    gameMap.createEnemiesAround(location, 5);

                    for (Enemy enemy : gameMap.getEnemies()) {
                        enemy.moveTo(locationGeopoint);
                    }
                }else{
                    Snackbar.make(view, "Can't find your location", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
    }


    /**
     * https://developer.android.com/training/permissions/requesting
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.d("position", "Position has already been granted");
        }
    }

    /**
     * https://developer.android.com/training/permissions/requesting
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private Location getLocation() {
        Location location = null;
        Context context = getApplicationContext();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // check location available for API >= 23
        if (
                Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("location", "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission not granted");
            return null;
        }

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (isGPSEnabled || isNetworkEnabled || isPassiveEnabled) {
            if (isGPSEnabled && location == null) {
                if (isGPSEnabled && location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                            LOCATION_REFRESH_DISTANCE, this.locationListener
                    );
                    Log.d("GPS", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
                if (isPassiveEnabled && location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.PASSIVE_PROVIDER,
                            LOCATION_REFRESH_TIME,
                            LOCATION_REFRESH_DISTANCE, this.locationListener);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }
                }

                if (isNetworkEnabled && location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            LOCATION_REFRESH_TIME,
                            LOCATION_REFRESH_DISTANCE, this.locationListener);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }
        }

        return location;
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        gameMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        gameMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}
