package com.thenewboston.backendless_test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.Geo;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;

public class ViewRequests extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    ListView listView;
    ArrayList<String> arrayList, usernames;
    //ArrayList<Requests> request;
    ArrayList<Double> latitudes,longitudes, dlatitudes, dlongitudes;
    ArrayAdapter<String> adapter;
    LocationManager locationManager;
    String provider;
    int permissionCheck, permissionCheck1;
    GoogleApiClient mGoogleApiClient = null;
    Location mLastLocation;
    Location requesterLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        listView = (ListView)findViewById(R.id.listView);
        arrayList = new ArrayList<>();
        //request = new ArrayList<>();
        usernames = new ArrayList<>();
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
        dlatitudes = new ArrayList<>();
        dlongitudes = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).build();
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
//        if(mLastLocation != null){
//            Log.i("Location", "Null nahi re");
//            updateLocation(mLastLocation);
//        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewRequests.this, Navigation.class);
                i.putExtra("username", usernames.get(position));
                i.putExtra("latitude", latitudes.get(position));
                i.putExtra("longitude", longitudes.get(position));
                i.putExtra("dlatitude", dlatitudes.get(position));
                i.putExtra("dlongitude", dlongitudes.get(position));
                Log.i("Intent", latitudes.get(position) + " " + longitudes.get(position));
                startActivity(i);
            }
        });



    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(ViewRequests.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
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

    public void updateLocation(final Location location) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        final GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        user.setProperty("location", geoPoint);
        Backendless.Data.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser backendlessUser) {
                Log.i("Driver Location", "Saved");
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.i("Driver Location", "Not saved");
            }
        });
        Log.i("Driver Location", String.valueOf(location.getLatitude() * 1E6) + " " + String.valueOf(location.getLongitude()*1E6));
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        //QueryOptions queryOptions = new QueryOptions();
        //queryOptions.addRelated("mtlocation");
        //dataQuery.setQueryOptions(queryOptions);
        dataQuery.setWhereClause("driverusername=''");
        //dataQuery.setWhereClause("requesterUsername='xyz123@gmail.com");
        Backendless.Data.of(Requests.class).find(dataQuery, new AsyncCallback<BackendlessCollection<Requests>>() {
            @Override
            public void handleResponse(BackendlessCollection<Requests> requestsBackendlessCollection) {
                if (requestsBackendlessCollection != null) {
                    Iterator<Requests> iterator = requestsBackendlessCollection.getCurrentPage().iterator();
                    arrayList.clear();
                    usernames.clear();
                    latitudes.clear();
                    longitudes.clear();
//        BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
//        geoQuery.addCategory("mylocation");
//        BackendlessCollection<GeoPoint> points = Backendless.Geo.getPoints(geoQuery);
//        int totalPoints = points.getTotalObjects();
//        int pointsLoaded = points.getCurrentPage().size();
//        arrayList.clear();
//        System.out.println("Total points in category " + points.getTotalObjects());
//
//        while (pointsLoaded < totalPoints) {
//
//            Iterator<GeoPoint> iterator = points.getCurrentPage().iterator();
//
//            while (iterator.hasNext()) {
//                GeoPoint driverGeopoint = iterator.next();
//                latitudes.add(driverGeopoint.getLatitude());
//                longitudes.add(driverGeopoint.getLongitude());
//
//                Float distance = getDistanceInMiles(driverGeopoint, geoPoint);
//                arrayList.add(distance.toString());
//
//                points = points.nextPage();
//                pointsLoaded += points.getCurrentPage().size();
//            }

                    //request.clear();
                    while (iterator.hasNext()) {
                        Requests requests = iterator.next();
                        //arrayList.add(requests.requesterUsername);


                        GeoPoint requesterGeopoint = requests.mylocation;
                        requesterLocation = new Location("");
                        if (requesterGeopoint != null) {
//                            double latitude = requesterGeopoint.getLatitude()/1E6;
//                            double longitude = requesterGeopoint.getLongitude()/1E6;
//                            LatLng latLng = new LatLng(latitude, longitude);
//                            requesterLocation.setLongitude(latLng.latitude);
//                            requesterLocation.setLongitude(latLng.longitude);
//                            latitudes.add(latitude);
//                            longitudes.add(longitude);
                            Log.i("User Location", "Retreived");

                        } else {
                            Log.i("User Location", "Not retrieved");
                        }
                        float distance = getDistanceInMiles(requesterGeopoint, geoPoint);
                        latitudes.add(requesterGeopoint.getLatitudeE6() / 1E6);
                        longitudes.add(requesterGeopoint.getLongitudeE6() / 1E6);
                        dlatitudes.add(location.getLatitude());
                        dlongitudes.add(location.getLongitude());
                        arrayList.add(String.valueOf(distance) + " miles");
                        usernames.add(requests.requesterUsername);
                        //request.add(requests);
                    }
                } else {
                    Log.i("Request retrieval", "null");
                }
                Log.i("Query", "Successful");

            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.i("Query", "Failed");
                Log.i("Error", backendlessFault.toString());
            }
        });

            adapter.notifyDataSetChanged();


        }


    public float getDistanceInMiles(GeoPoint p1, GeoPoint p2) {
        double lat1 = ((double)p1.getLatitudeE6()) / 1e6;
        double lng1 = ((double)p1.getLongitudeE6()) / 1e6;
        double lat2 = ((double)p2.getLatitudeE6()) / 1e6;
        double lng2 = ((double)p2.getLongitudeE6()) / 1e6;
        float [] dist = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
        return dist[0] * 0.000621371192f;
    }

    @Override
    public void onLocationChanged(Location location) {

        requestUpdates();
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
    protected void onPause() {
        super.onPause();
        stopUpdates();
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
            updateLocation(mLastLocation);

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
