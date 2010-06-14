package com.steelebit.proximityalerter;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

public class ProximityAlerterActivity extends MapActivity {
	
	MapView mapView;
	MapController mapController;
	OverlayManager overlayManager;
	LocationManager locManager;
	
	SharedPreferences settings;
	int counter = 0;
	
	private String proximityIntentAction = new String ("com.steelebit.quicktest.PROXIMITY_ALERT");
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		
		settings = getPreferences(MODE_WORLD_READABLE);
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		overlayManager = new OverlayManager(this, mapView);
		
		IntentFilter intentFilter = new IntentFilter(proximityIntentAction);
		registerReceiver(new ProximityAlert(), intentFilter);
        
        createOverlayWithListener();
        initProximityAlerts();
    }
    
	private void initProximityAlerts() {
		for (GeoPoint gp : getStoredGeoPoints())
		{
			setProximityAlert(gp.getLatitudeE6() / 1E6,
					gp.getLongitudeE6() / 1E6, 0, counter);
			counter++;
		}
	}

	public void createOverlayWithListener() {
		ManagedOverlay managedOverlay = createOverlay();
		
		managedOverlay.setOnOverlayGestureListener(new ManagedOverlayGestureDetector.OnOverlayGestureListener() {
			@Override
			public boolean onZoom(ZoomEvent zoom, ManagedOverlay overlay) {
				Toast.makeText(getApplicationContext(), "Zoom yeah!", Toast.LENGTH_SHORT).show();
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				mapController.animateTo(point);
				mapController.zoomIn();
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e, ManagedOverlay overlay) {
				//Toast.makeText(getApplicationContext(), "LongPress incoming...!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onLongPressFinished(MotionEvent e, final ManagedOverlay overlay, final GeoPoint point, final ManagedOverlayItem item) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());
				if (item == null)
				{
					builder.setTitle("Add Point?");
					
					builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							overlay.createItem(point);
							overlayManager.populate();
							
							int lat = point.getLatitudeE6();
							int lon = point.getLongitudeE6();
							
							setProximityAlert(lat / 1E6, lon / 1E6, 0, counter);
							counter++;
							
							String key = lat + ";" + lon;
							
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(key, key);
							editor.commit();
						}
					});
				}
				else
				{
					builder.setTitle("Remove Point?");
					
					builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							overlay.remove(item);
							overlayManager.populate();
							
							SharedPreferences.Editor editor = settings.edit();
							editor.remove(item.getPoint().getLatitudeE6() + ";" + item.getPoint().getLongitudeE6());
							editor.commit();
							
							dialog.dismiss();
						}
					});
				}
				
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				
				AlertDialog alert = builder.create();
				alert.show();
			}

			@Override
			public boolean onScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, ManagedOverlay overlay) {
				return false;
			}

			@Override
			public boolean onSingleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				return false;
			}
		});
		overlayManager.populate();
	}
    
	private ManagedOverlay createOverlay()
	{
		ManagedOverlay managedOverlay = overlayManager.createOverlay("listenerOverlay", getResources().getDrawable(R.drawable.down));
        		
		for (GeoPoint gp : getStoredGeoPoints())
		{
			managedOverlay.createItem(gp);
		}
		
		if (managedOverlay.getOverlayItems().isEmpty())
		{
			managedOverlay.createItem(new GeoPoint(37410375, -122059773), "Item");
		}
		
		return managedOverlay;
	}

	private ArrayList<GeoPoint> getStoredGeoPoints() {
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		for (String key : settings.getAll().keySet())
		{
			if (key.split(";").length == 2)
			{
				int lat = Integer.parseInt(key.split(";")[0]);
				int lon = Integer.parseInt(key.split(";")[1]);
				geoPoints.add(new GeoPoint(lat, lon));
			}
		}
		
		return geoPoints;
	}
	
	private void setProximityAlert(double lat, double lon, final long eventID, int requestCode)
	{
		// 100 meter radius
		float radius = 10f;
		// Expiration is 10 Minutes
		long expiration = 600000;
		
		Intent intent = new Intent(proximityIntentAction);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		locManager.addProximityAlert(lat, lon, radius, expiration, pendingIntent);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}