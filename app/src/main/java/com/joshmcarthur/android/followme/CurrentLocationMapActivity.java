package com.joshmcarthur.android.followme;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;

public class CurrentLocationMapActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    private Location mLastLocation;
    private String mLastUpdateTime;
    private WebView mRTCWebView;
    private LocationRequest mLocationRequest;
    private String mPeerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location_map);

        mRTCWebView = (WebView) findViewById(R.id.rtc_web_view);
        mRTCWebView.addJavascriptInterface(new JavascriptInterface(), "FollowMeAndroid");
        mRTCWebView.getSettings().setJavaScriptEnabled(true);

        String html = "<html>" + "<head>" + "<script src=\"peer.min.js\"></script>" +
                "<script src=\"followme.js\"></script>" + "</head>" + "</html>";
        mRTCWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.offOnFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPeerId == null) {
                    Toast.makeText(CurrentLocationMapActivity.this,
                            "Cannot share until connected to service!", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, "See my location live with Follow Me: ");
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.view_host) + "/#" + mPeerId);

                startActivity(Intent.createChooser(share, "Share link to view location"));
            }
        });


        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void updateMap() {
        if (mMap != null && mLastLocation != null) {
            LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if ( mMarker == null ) {
                mMarker = mMap.addMarker(new MarkerOptions().position(loc).title("Your Location"));
            }

            mMarker.setPosition(loc);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(loc)
                    .zoom(17)
                    .bearing(mLastLocation.getBearing())
                    .tilt(30)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mRTCWebView.loadUrl(
                    "javascript:FollowMe.sendLocation(" +
                            loc.latitude + ", " + loc.longitude + ");");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Could not connect to Google Play Services.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateMap();
    }

    private class JavascriptInterface {
        @android.webkit.JavascriptInterface
        public void didError(String errType) {
            Toast.makeText(CurrentLocationMapActivity.this, "Error communicating: " + errType, Toast.LENGTH_SHORT)
                    .show();
        }

        @android.webkit.JavascriptInterface
        public void didConnect(String peerId) {
            Log.d(getClass().getSimpleName(), "PEER ID: " + peerId);
            CurrentLocationMapActivity.this.mPeerId = peerId;
            Toast.makeText(CurrentLocationMapActivity.this, "Connected!", Toast.LENGTH_SHORT)
            .show();
        }

        @android.webkit.JavascriptInterface
        public void didDisconnect() {
            Toast.makeText(CurrentLocationMapActivity.this, "Disconnected!", Toast.LENGTH_SHORT).show();
        }

        @android.webkit.JavascriptInterface
        public void didSendLocation() {
            Toast.makeText(CurrentLocationMapActivity.this, "Sent location!", Toast.LENGTH_SHORT).show();
        }

        @android.webkit.JavascriptInterface
        public void clientDidConnect() {
            Toast.makeText(CurrentLocationMapActivity.this, "A client connected!", Toast.LENGTH_SHORT).show();
        }
    }

}
