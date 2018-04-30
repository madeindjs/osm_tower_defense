package com.rousseau_alexandre.testmap;

import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

public class Enemy extends Marker {

    public int life = 5;

    public Enemy(MapView mapView) {
        super(mapView);
        //this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

    }

    public void updateTitle() {
        this.setTitle(Integer.toString(this.life));
    }

    @Override
    protected boolean onMarkerClickDefault(Marker marker, MapView mapView) {
        this.life = this.life - 1;
        this.updateTitle();

        if (this.life > 0) {
            return super.onMarkerClickDefault(marker, mapView);
        } else {
            return this.destroy(mapView);
        }
    }

    /**
     * Move a point
     * https://stackoverflow.com/questions/31337149/animating-markers-on-openstreet-maps-using-osmdroid
     *
     * @param mapView
     * @param toPosition
     */
    public void moveTo(final MapView mapView, final GeoPoint toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mapView.getProjection();
        Point startPoint = proj.toPixels(this.getPosition(), null);
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
                Enemy.this.setPosition(new GeoPoint(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 15);
                }
                mapView.postInvalidate();
            }
        });
    }

    public boolean destroy(MapView mapView) {
        mapView.getOverlayManager().remove(this);
        return true;
    }

}
