package com.thenewboston.backendless_test;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Iterator;

public class Navigation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double latitude;
    double longitude;
    double dlatitude, dlongitude;
    String rusername;
    RelativeLayout mapLayout;
    Requests requests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i = getIntent();
        latitude = i.getDoubleExtra("latitude", 0);
        longitude = i.getDoubleExtra("longitude", 0);
        dlatitude = i.getDoubleExtra("dlatitude", 0);
        dlongitude = i.getDoubleExtra("dlongitude", 0);
        rusername = i.getStringExtra("username");
        mapLayout = (RelativeLayout)findViewById(R.id.relativeLayout);

    }

    public void acceptRequest(View view){
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause("requesterusername='" + rusername + "'");
        Backendless.Data.of(Requests.class).find(dataQuery, new AsyncCallback<BackendlessCollection<Requests>>() {
            @Override
            public void handleResponse(BackendlessCollection<Requests> requestsBackendlessCollection) {
                if (requestsBackendlessCollection != null) {
                    Iterator<Requests> iterator = requestsBackendlessCollection.getCurrentPage().iterator();
//            }

                    //request.clear();
                    while (iterator.hasNext()) {
                        requests = iterator.next();
                        requests.setDriverUsername(Backendless.UserService.CurrentUser().getEmail());
                    }
                    Backendless.Data.of(Requests.class).save(requests, new AsyncCallback<Requests>() {
                        @Override
                        public void handleResponse(Requests requests) {
                            Log.i("Data", "Updated");
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            Log.i("Data", "Not updated");
                            Log.i("Error", backendlessFault.toString());
                        }
                    });
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
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + latitude + "," + longitude +
                        "&daddr=" + dlatitude + "," + dlongitude));
        startActivity(intent);
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


        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //and write code, which you can see in answer above
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                ArrayList<Marker> markers = new ArrayList<Marker>();
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Requester")));
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(dlatitude, dlongitude)).title("Me").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))
                );

                for (Marker marker: markers){
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                // Add a marker in Sydney and move the camera


                mMap.moveCamera(cu);
            }
        });

    }
}
