package com.thenewboston.backendless_test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.Persistence;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoCategory;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Iterator;

public class YourLocation extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    int permissionCheck, permissionCheck1;
    GoogleApiClient mGoogleApiClient = null;
    Location mLastLocation;
    TextView uberRequest;
    Requests requests;
    Button requestUber;
    String email;
    Double latitude,longitude;

    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(YourLocation.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    public void requestUpdates(){
        checkPermission();
        if (permissionCheck == PackageManager.PERMISSION_GRANTED && permissionCheck1 == PackageManager.PERMISSION_GRANTED) {


                locationManager.requestLocationUpdates(provider, 400, 1, this);


        }
    }

    public void stopUpdates(){
        checkPermission();
        if (permissionCheck == PackageManager.PERMISSION_GRANTED && permissionCheck1 == PackageManager.PERMISSION_GRANTED) {


                locationManager.removeUpdates(this);


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestUpdates();

    }

    public void driverLocation(){

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause("requesterusername'" + Backendless.UserService.CurrentUser().getEmail() + "'");
        Backendless.Data.of(Requests.class).find(dataQuery, new AsyncCallback<BackendlessCollection<Requests>>() {
            @Override
            public void handleResponse(BackendlessCollection<Requests> requestsBackendlessCollection) {
                Log.i("Query", "Successful");
                if(requestsBackendlessCollection != null){
                    Log.i("Collection", "Not Zero");
                    Iterator<Requests> iterator = requestsBackendlessCollection.getCurrentPage().iterator();

                    while (iterator.hasNext()) {
                        Requests requests = iterator.next();
                        email = requests.driverUsername;


                    }

                }else{
                    Log.i("Collection", "Zero");
                }
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {

            }
        });
        Backendless.Data.of(BackendlessUser.class).find(new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
            @Override
            public void handleResponse(BackendlessCollection<BackendlessUser> backendlessUserBackendlessCollection) {
                if (backendlessUserBackendlessCollection != null) {
                    Iterator<BackendlessUser> iterator = backendlessUserBackendlessCollection.getCurrentPage().iterator();

                    while (iterator.hasNext()) {
                        BackendlessUser user = iterator.next();
                        if(user.getEmail().equals(email)){
                            GeoPoint geoPoint = (GeoPoint)user.getProperty("location");
                            latitude = geoPoint.getLatitudeE6()/1E6;
                            longitude = geoPoint.getLongitudeE6()/1E6;
                        }

                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.i("Error", "Failed Retrieval");
            }
        });
    }

    public void requestUber(View view){
        Log.i("Uber", "Requested");
        requests = new Requests();
        requests.setDriverUsername("");
        requests.setRequesterUsername(Backendless.UserService.CurrentUser().getEmail());
        requests.setMylocation(new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        if(requestUber.getText().toString().equals("Request Uber")){
            Backendless.Persistence.of(Requests.class).save(requests, new AsyncCallback<Requests>() {
                @Override
                public void handleResponse(Requests requests) {
                    Log.i("Requests", "Saved");
                    uberRequest.setText("Finding Uber driver");
                    requestUber.setText("Stop Uber");


                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    Log.i("Error", backendlessFault.toString());
                    Log.i("Requests", "Not saved");
                }
            });
        }else if (requestUber.getText().toString().equals("Stop Uber")) {
            Backendless.Persistence.of(Requests.class).remove(requests);
        }
        driverLocation();
//        Location dlocation = new Location("");
//        dlocation.setLatitude(latitude);
//        dlocation.setLongitude(longitude);
//
//        if(dlocation != null){
//            float distance = dlocation.distanceTo(mLastLocation);
//            uberRequest.setText(String.valueOf(distance/1000) + "km");
//        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        uberRequest = (TextView)findViewById(R.id.uberRequest);
        requestUber = (Button)findViewById(R.id.requestUber);
        uberRequest.setText("Uber Service Inactive");
//        isGPSEnabled = locationManager.isProviderEnabled(provider);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).build();
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        requestUpdates();
//        if (mLastLocation != null){
//            Log.i("Google api", "Yaha aaya re");
//            mMap.clear();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 14));
//            mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Me"));
//        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Me"));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        checkPermission();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null){
            Log.i("Google Api", String.valueOf(mLastLocation.getLatitude()));
            Log.i("Google Api", String.valueOf(mLastLocation.getLongitude()));
            onLocationChanged(mLastLocation);

        }else{
            Log.i("Google Api", "Babaji ka thulu");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
