package com.raghav.pospe_detector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.tabs.TabLayout;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mmi.services.api.directions.DirectionsCriteria;
import com.mmi.services.api.directions.MapmyIndiaDirections;
import com.mmi.services.api.directions.models.DirectionsResponse;
import com.mmi.services.api.directions.models.DirectionsRoute;
import com.raghav.pospe_detector.data.LocationData;
import com.raghav.pospe_detector.extras.DirectionPolylinePlugin;
import com.raghav.pospe_detector.extras.Storage;
import com.raghav.pospe_detector.extras.TransparentProgressDialog;
import com.raghav.pospe_detector.retrofit.IUploadAPI;
import com.raghav.pospe_detector.retrofit.RetrofitClient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictedActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener {

    private MapboxMap mapmyIndiaMap;
    private LocationEngine locationEngine;
    private MapView mapView;
    private TransparentProgressDialog transparentProgressDialog;
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    private TabLayout profileTabLayout;
    private String resource = DirectionsCriteria.RESOURCE_ROUTE;
    private LinearLayout directionDetailsLayout;
    private TextView tvDistance, tvDuration;
    private DirectionPolylinePlugin directionPolylinePlugin;

    IUploadAPI mService;
    private IUploadAPI getAPIUpload(){
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_layout);



        mapView = findViewById(R.id.map_view);


        profileTabLayout = findViewById(R.id.tab_layout_profile);
        RadioGroup rgResource = findViewById(R.id.rg_resource_type);

        directionDetailsLayout = findViewById(R.id.direction_details_layout);
        tvDistance = findViewById(R.id.tv_distance);
        tvDuration = findViewById(R.id.tv_duration);



//        Button back=findViewById(R.id.back_predict);
//        back.setOnClickListener(v -> {
//            Intent i=new Intent(PredictedActivity.this,HomeActivity.class);
//            startActivity(i);
//        });


        profileTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (mapmyIndiaMap == null) {
                    if (profileTabLayout.getTabAt(0) != null) {
                        Objects.requireNonNull(profileTabLayout.getTabAt(0)).select();
                        return;
                    }
                }
                switch (tab.getPosition()) {
                    case 0:
                        profile = DirectionsCriteria.PROFILE_DRIVING;
                        rgResource.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        profile = DirectionsCriteria.PROFILE_BIKING;
                        rgResource.check(R.id.rb_without_traffic);
                        rgResource.setVisibility(View.GONE);
                        break;

                    case 2:
                        profile = DirectionsCriteria.PROFILE_WALKING;
                        rgResource.check(R.id.rb_without_traffic);
                        rgResource.setVisibility(View.GONE);
                        break;

                    default:
                        break;
                }

                getDirections();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        rgResource.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rb_without_traffic:
                    resource = DirectionsCriteria.RESOURCE_ROUTE;
                    break;

                case R.id.rb_with_traffic:
                    resource = DirectionsCriteria.RESOURCE_ROUTE_TRAFFIC;
                    break;

                case R.id.rb_with_route_eta:
                    resource = DirectionsCriteria.RESOURCE_ROUTE_ETA;
                    break;

                default:
                    break;
            }

            getDirections();
        });
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        transparentProgressDialog = new TransparentProgressDialog(this, R.drawable.circle_loader, "");

    }

    @Override
    public void onMapReady(MapboxMap mapmyIndiaMap) {
        this.mapmyIndiaMap = mapmyIndiaMap;


        mapmyIndiaMap.setPadding(20, 20, 20, 20);
//        profileTabLayout.setVisibility(View.VISIBLE);

        mapmyIndiaMap.setCameraPosition(setCameraAndTilt());
        Toast.makeText(PredictedActivity.this,"The potholes detected using the ML models are in the map",Toast.LENGTH_LONG).show();

        getDirections();
    }


    protected CameraPosition setCameraAndTilt() {
        return new CameraPosition.Builder().target(new LatLng(
                28.551087, 77.257373)).zoom(14).tilt(0).build();
    }


    private void progressDialogShow() {
        transparentProgressDialog.show();
    }

    private void progressDialogHide() {
        transparentProgressDialog.dismiss();
    }

    void addMarker()
    {
        Intent intent = this.getIntent();
        String filename = intent.getStringExtra("filename");
        mService=getAPIUpload();
        List<LocationData> potholeData=new ArrayList<>();
        mService.processData(RequestBody.create(MediaType.parse("multipart/form-data"), filename)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                potholeData.add(new LocationData(Double.parseDouble(response.body()),Double.parseDouble(response.body())));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
        for(LocationData l:potholeData) {
            // Toast.makeText(DirectionActivity.this,l.getLatitude()+""+l.getLongitude(),Toast.LENGTH_LONG).show();
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())).icon(IconFactory.getInstance(PredictedActivity.this).fromResource(R.drawable.pospemarker));
            markerOptions.setTitle("Pothole");
            Marker marker = mapmyIndiaMap.addMarker(markerOptions);
            /*GeoFence here!!*/
            mapmyIndiaMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    return false;
                }
            });
        }


        for(LocationData l:Storage.speedBreakerData) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(l.getLatitude(),l.getLongitude())).icon(IconFactory.getInstance(PredictedActivity.this).fromResource(R.drawable.pospemarker));
            markerOptions.setTitle("Pothole");
            Marker marker = mapmyIndiaMap.addMarker(markerOptions);
            /*GeoFence here!!*/
            mapmyIndiaMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    return false;
                }
            });
        }
    }
    private void getDirections() {
        progressDialogShow();
        MapmyIndiaDirections.builder()
                .origin(Point.fromLngLat(Storage.start_long, Storage.start_lat))
                .destination(Point.fromLngLat(Storage.end_long, Storage.end_lat))
                .profile(profile)
                .resource(resource)
                .steps(true)
                .alternatives(false)
                .overview(DirectionsCriteria.OVERVIEW_FULL).build().enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        DirectionsResponse directionsResponse = response.body();
                        List<DirectionsRoute> results = directionsResponse.routes();

                        if (results.size() > 0) {
                            mapmyIndiaMap.clear();
                            addMarker();
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(Storage.end_lat,Storage.end_long))
                                    .icon(IconFactory.getInstance(PredictedActivity.this).fromResource(R.drawable.placeholder));
                            markerOptions.setTitle("End Point");
                            markerOptions.setSnippet("Destination");
                            Marker marker = mapmyIndiaMap.addMarker(markerOptions);

                            LocationComponentOptions options = LocationComponentOptions.builder(PredictedActivity.this)
                                    .trackingGesturesManagement(true)
                                    .accuracyColor(ContextCompat.getColor(PredictedActivity.this, R.color.purple_500))
                                    .build();
                            LocationComponent locationComponent = mapmyIndiaMap.getLocationComponent();
                            if (ActivityCompat.checkSelfPermission(PredictedActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PredictedActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationComponent.activateLocationComponent(PredictedActivity.this, options);
                            locationComponent.setLocationComponentEnabled(true);
                            locationEngine = locationComponent.getLocationEngine();

                            locationEngine.addLocationEngineListener(PredictedActivity.this);
                            locationComponent.setCameraMode(CameraMode.TRACKING);
                            locationComponent.setRenderMode(RenderMode.COMPASS);
                            DirectionsRoute directionsRoute = results.get(0);
                            if (directionsRoute != null && directionsRoute.geometry() != null) {
                                drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6));
                                updateData(directionsRoute);
                            }
                        }
                    }
                } else {
                    Toast.makeText(PredictedActivity.this, response.message() + response.code(), Toast.LENGTH_LONG).show();
                }
                progressDialogHide();

            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                progressDialogHide();
                t.printStackTrace();

            }
        });

    }


    private void updateData(@NonNull DirectionsRoute directionsRoute) {
        if (directionsRoute.distance() != null && directionsRoute.distance() != null) {
            directionDetailsLayout.setVisibility(View.VISIBLE);
            tvDuration.setText("ETA -(" + getFormattedDuration(directionsRoute.duration()) + ")");
            tvDistance.setText("Distance -"+getFormattedDistance(directionsRoute.distance()));
        }
    }


    private String getFormattedDistance(double distance) {

        if ((distance / 1000) < 1) {
            return distance + "mtr.";
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(distance / 1000) + "Km.";
    }


    private String getFormattedDuration(double duration) {
        long min = (long) (duration % 3600 / 60);
        long hours = (long) (duration % 86400 / 3600);
        long days = (long) (duration / 86400);
        if (days > 0L) {
            return days + " " + (days > 1L ? "Days" : "Day") + " " + hours + " " + "hr" + (min > 0L ? " " + min + " " + "min." : "");
        } else {
            return hours > 0L ? hours + " " + "hr" + (min > 0L ? " " + min + " " + "min" : "") : min + " " + "min.";
        }
    }


    private void drawPath(@NonNull List<Point> waypoints) {
        ArrayList<LatLng> listOfLatLng = new ArrayList<>();
        for (Point point : waypoints) {
            listOfLatLng.add(new LatLng(point.latitude(), point.longitude()));
        }

        if(directionPolylinePlugin == null) {
            directionPolylinePlugin = new DirectionPolylinePlugin(mapmyIndiaMap, mapView, profile);
            directionPolylinePlugin.createPolyline(listOfLatLng);
        } else {
            directionPolylinePlugin.updatePolyline(profile, listOfLatLng);

        }
//        mapmyIndiaMap.addPolyline(new PolylineOptions().addAll(listOfLatLng).color(Color.parseColor("#3bb2d0")).width(4));
        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatLng).build();
        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 30));
    }

    @Override
    public void onMapError(int i, String s) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
    }
}
