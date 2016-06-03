package com.example.falli_000.flashbackv2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private static final int REQUEST_MESSAGE_CODE = 001;
    private static final int REQUEST_IMAGE_CODE = 002;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CAMERA = 2;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Firebase myFirebaseRef = new Firebase("https://torrid-heat-4805.firebaseio.com");

    private double currentlat;
    private double currentlon;

    public static final String TAG = MapsActivity.class.getSimpleName();
    private String messageInput;
    private String mCurrentPhotoPath;

    //Creates custom window adapter
    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());

            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

//            ImageView mImageView = (ImageView) findViewById(R.id.flashImage);
//
//            if(mCurrentPhotoPath != null) {
//
//            mImageView.setImageBitmap((BitmapFactory.decodeFile(mCurrentPhotoPath)));
//
//            }


            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }
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

        mGoogleApiClient.connect();



    }





    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        //Customize the info window
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Brandon's a fuckboy").visible(false));


        //TODO make firebase place Flashes every time a new one is added to the database.

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            popUpError(R.string.noPermissionText, R.string.signUpErrorTitle);

            return;
        }
        mMap.setMyLocationEnabled(true);
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

            if (currentlat != 0 && currentlon != 0) {

                LatLng myLocation = new LatLng(currentlat, currentlon);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 25));
            }
            else{
                makeQuickPopup(MapsActivity.this, "Your location cannot be determined");
            }
        }


    public void onMessage(View view) {

        if(currentlat != 0 && currentlon!= 0){

            startActivityForResult(new Intent(MapsActivity.this, MessageActivity.class), REQUEST_MESSAGE_CODE);
            this.centerMapOnMyLocation();

        }
        else{
            makeQuickPopup(MapsActivity.this, "Your location can't be found");
        }
    }


    public void onPicture(View view) {

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_ACCESS_CAMERA);

        }
        else if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    makeQuickPopup(MapsActivity.this, "File error");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CODE);

                    this.centerMapOnMyLocation();
                }

                startActivityForResult(new Intent(MapsActivity.this, MessageActivity.class), REQUEST_MESSAGE_CODE);


            }
        }
        else {

            makeQuickPopup(MapsActivity.this, "You fucked up lil nigga");

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MESSAGE_CODE && resultCode == RESULT_OK) {

            messageInput= data.getStringExtra("message");


            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentlat, currentlon))
                    .title("Insert Username here pls").snippet(messageInput)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.powered_by_google_light)))
                    .showInfoWindow();
        }
       else if (requestCode == REQUEST_IMAGE_CODE && resultCode == RESULT_OK) {

//            Bundle extras = data.getExtras();




//            mImageView.setImageBitmap((BitmapFactory.decodeFile(mCurrentPhotoPath)));





            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentlat, currentlon))
                    .title("Insert Username here pls")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.powered_by_google_light)));
//                        .showInfoWindow();

        }

        else if( resultCode == RESULT_CANCELED){
            makeQuickPopup(MapsActivity.this, "Cancelled");
        }


    }

    private File createImageFile() throws IOException {
        // Create an image file name
       /* String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "1mind_" + timeStamp + ".jpg";
        File image = new File(Environment.getExternalStorageDirectory(),  imageFileName);
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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
            case MY_PERMISSIONS_REQUEST_ACCESS_CAMERA:
                if (grantResults.length!=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                           makeQuickPopup(MapsActivity.this, "File error");
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));

                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CODE);
                        }

                    }

                }
                else if(grantResults.length == 0){

                }
                else {
                    // Permission Denied
                    makeQuickPopup(MapsActivity.this, "ACCESS_CAMERA Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void makeQuickPopup(Activity currentActivity, String error){
        Toast.makeText(currentActivity, error, Toast.LENGTH_SHORT)
                .show();
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

        currentlat= location.getLatitude();
        currentlon= location.getLongitude();
        LatLng latlon= new LatLng(currentlat,currentlon);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlon));

    }

    @Override
    protected void onResume(){
        super.onResume();
//        setUpMapIfNeeded();
        if(!mGoogleApiClient.isConnected()) {

            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            onConnectionSuspended(1);
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