package com.example.falli_000.flashbackv2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

//import android.location.LocationListener;

//I'm sorry for how messy this code, but I'm a physicist not a programmer
//TODO:clean up this mess that parallels the mess of my life

/*
Actual TODO
Implement message flashes so it is on server
Implement voting on flashes
picture flashes
video flashes
customize markers
maybe customize map?
Graphic overhaul
remember accounts
forgot password activity

 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int MESSAGE_REQUEST_CODE = 001;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Firebase myFirebaseRef = new Firebase("https://torrid-heat-4805.firebaseio.com");

    public static final String TAG = MapsActivity.class.getSimpleName();
    private String messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                onMapReady(mMap);
            }
        }
    }


//    private void putFlashesOnMap(){
//        Firebase flashes = myFirebaseRef.child("Flashes");
//        flashes.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                for (DataSnapshot child : snapshot.getChildren()) {
//                    if (child.toString().equals("Messages")) {
//                        for (int i =(int)child.getChildrenCount(); i>0; i--){
//                            MessageFlash flash = (MessageFlash) child.getValue();
//
//                        }
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                //idk
//            }
//        });
//
//    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(final GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Brandon's a fuckboy"));


        //TODO make firebase place Flashes every time a new one is added to the database.
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                map.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getValue().equals("Messages")) {
                        for (DataSnapshot snap : child.getChildren()) {
                            MessageFlash flash = (MessageFlash) snap.getValue();
                            map.addMarker(new MarkerOptions().position(new LatLng(flash.getLat(), flash.getLon())).title(flash.getMessage()));
                        }

                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            popUpError(R.string.noPermissionText, R.string.signUpErrorTitle);

            return;
        }
        map.setMyLocationEnabled(true);
    }
    private void popUpError(int errortext, int errorTitle){
        AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
        alert.setMessage(errortext)
                .setTitle(errorTitle)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = alert.create();
        dialog.show();
    }
    private void centerMapOnMyLocation() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                popUpError(R.string.noPermissionText,R.string.signUpErrorTitle);

                return;
            }
            mMap.setMyLocationEnabled(true);
            Location location = mMap.getMyLocation();
            if (location != null) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 25));
            }
        }
    }

    public void onMessage(View view) {

        this.centerMapOnMyLocation();

        //Get message to send to firebase
        startActivityForResult(new Intent(MapsActivity.this, MessageActivity.class), MESSAGE_REQUEST_CODE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            popUpError(R.string.noPermissionText,R.string.signUpErrorText);

            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(location == null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        }
        else {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            //Put lat lon and message into MessageFlash object and then into tree
            myFirebaseRef.child("Flashes").child("Messages").setValue(new MessageFlash(lat, lon, messageInput));

            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(messageInput));
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MESSAGE_REQUEST_CODE && resultCode == RESULT_OK) {

            messageInput= data.getStringExtra("message");
        }
    }

    public class MessageFlash {
        public double lat = 0;
        public double lon = 0;
        public String message = "";
        //constructor
        public MessageFlash(double lat,double lon, String message) {
            this.lat = lat;
            this.lon = lon;
            this.message = message;
        }
        public double getLat(){
            return lat;
        }
        public double getLon(){
            return lon;
        }
        public String getMessage(){
            return message;
        }
    }

    public void onVideo(View view) {
        //TODO: UPLOAD FUCKING VIDEOS

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length!=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                }
                else if(grantResults.length == 0){

                }
                else {
                    // Permission Denied
                    Toast.makeText(MapsActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            popUpError(R.string.noPermissionText,R.string.signUpErrorTitle);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location == null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        }
        else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG,location.toString());
        double currentlat= location.getLatitude();
        double currentlon= location.getLongitude();
        LatLng latlon= new LatLng(currentlat,currentlon);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlon));
    }

    @Override
    protected void onResume(){
        super.onResume();
        setUpMapIfNeeded();
        if(!mGoogleApiClient.isConnected()) {

            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"Location services suspended. Please reconnect.");
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


//I hate the sound of people
//I think I just hate the sound of my sister
//She's super abrasive to my ears