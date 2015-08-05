package com.gmail.pjmdopheide.glassglobe;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerDragListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // TODO Prevent destruction when device tilted

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mMarker;
    private Marker mMarker2;
    private static LatLng fromPosition;
    private static LatLng toPosition;
    private static int latAnti;
    private static int lngAnti;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setMyLocationEnabled(true);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
@Override
public void onMapClick(LatLng point){
        mMarker2.setPosition(point);
                toPosition = point;
            }
        });

        final Button seeThrough = (Button) findViewById(R.id.seeTroughButton);
        seeThrough.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                calculateAntipodalPoints();

                Toast.makeText(
                        getApplicationContext(),
                        "Original: " + toPosition.toString() + "\r" + "Anti: " + latAnti + " "
                                + lngAnti, Toast.LENGTH_LONG).show();

                // If there is already a marker remove it
                if (mMarker != null) {
                    mMarker.remove();
                }
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latAnti, lngAnti))
                        .title("Marker2")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                // Go to newly created marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 1));
            }
        });

        final Button startLocation = (Button)findViewById(R.id.startLocationButton);
        startLocation.setOnClickListener(new View.OnClickListener(){
public void onClick(View v){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker2.getPosition(), 1));
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMarker2 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker")
                .draggable(true));
        toPosition = mMarker2.getPosition();
    }

    private void calculateAntipodalPoints() {
        latAnti = (int)toPosition.latitude;
        lngAnti = (int)toPosition.longitude;

        // Source: http://boards.straightdope.com/sdmb/showthread.php?t=437906
        // Change latitude to opposite i.e. 76 becomes -76
        latAnti *= -1;

        // Longitude:
        // if positive, 180 - longitude, if negative 180 + longitude
        // switch E and W i.e. 76 becomes -76
        if (Math.signum(lngAnti) == 1) {
            lngAnti = 180 - lngAnti;
            lngAnti *= -1;
        } else if (Math.signum(lngAnti) == -1) {
            lngAnti = 180 + lngAnti;
            lngAnti *= -1;
        }
    }

    // Source: http://javapapers.com/android/markersgoogle-maps-android-api-v2/
    @Override
    public void onMarkerDrag(Marker marker) {
        // do nothing during drag
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        toPosition = marker.getPosition();
        Toast.makeText(
                getApplicationContext(),
                "Marker " + marker.getTitle() + " dragged from " + fromPosition
                        + " to " + toPosition, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        fromPosition = marker.getPosition();
        Log.d(getClass().getSimpleName(), "Drag start at: " + fromPosition);
    }

    // Source: http://blog.teamtreehouse.com/beginners-guide-location-android
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        };
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!");
//        mMap.addMarker(options);
        mMarker2.setPosition(latLng);
        toPosition = mMarker2.getPosition();
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}
