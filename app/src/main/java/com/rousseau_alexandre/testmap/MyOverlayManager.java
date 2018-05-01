package com.rousseau_alexandre.testmap;

import android.content.Context;
import android.view.MotionEvent;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DefaultOverlayManager;
import org.osmdroid.views.overlay.TilesOverlay;


public class MyOverlayManager extends DefaultOverlayManager{

    /**
     * Create MyOverlayManager
     */
    public static MyOverlayManager create(MapView mapView, Context context) {
        MapTileProviderBase mTileProvider = mapView.getTileProvider();
        TilesOverlay tilesOverlay = new TilesOverlay(mTileProvider, context);
        mapView.getTileProvider();
        mapView.setOverlayManager(new MyOverlayManager(tilesOverlay));

        return new MyOverlayManager(tilesOverlay);
    }

    /**
     * Default constructor
     */
    public MyOverlayManager(final TilesOverlay tilesOverlay) {
        super(tilesOverlay);
    }

    /**
     * Override event & do nothing
     */
    @Override
    public boolean onDoubleTap(MotionEvent e, MapView pMapView) {
        return true;
    }

    /**
     * Override event & do nothing
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e, MapView pMapView) {
        return true;
    }
}
