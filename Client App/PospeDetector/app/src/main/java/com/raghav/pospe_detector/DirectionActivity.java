package com.raghav.pospe_detector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.raghav.pospe_detector.extras.LocationUploadAdapter;
import com.raghav.pospe_detector.extras.Storage;
import com.raghav.pospe_detector.extras.TransparentProgressDialog;
import com.raghav.pospe_detector.retrofit.IUploadAPI;
import com.raghav.pospe_detector.retrofit.RetrofitClient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, SensorEventListener {

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
    final double[] accData=new double[3];
    final double[] gyroData=new double[3];
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_layout);

        mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(!LocationUploadAdapter.findPotholes()){
            Toast.makeText(DirectionActivity.this, "Could not get the potholes", Toast.LENGTH_SHORT).show();
        }
        if(!LocationUploadAdapter.findSpeedBreakers()){
            Toast.makeText(DirectionActivity.this, "Could not get the speedbreakers", Toast.LENGTH_SHORT).show();
        }

        mapView = findViewById(R.id.map_view);

        profileTabLayout = findViewById(R.id.tab_layout_profile);
        RadioGroup rgResource = findViewById(R.id.rg_resource_type);

        directionDetailsLayout = findViewById(R.id.direction_details_layout);
        tvDistance = findViewById(R.id.tv_distance);
        tvDuration = findViewById(R.id.tv_duration);

        Button endJourney=findViewById(R.id.end_journey);
        endJourney.setVisibility(View.INVISIBLE);
        final String filename[]={"None"};
        Button startJourney=findViewById(R.id.start_journey);
        final boolean[] shouldStopLoop = {false};
        startJourney.setOnClickListener(v -> {
            startJourney.setVisibility(View.INVISIBLE);
            endJourney.setVisibility(View.VISIBLE);

            Handler mHandler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final String[] lat={""};
                    final String[] lon={""};
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(DirectionActivity.this);
                    if (ActivityCompat.checkSelfPermission(DirectionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DirectionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(DirectionActivity.this, location -> {
                                if (location != null) {
                                  lat[0]=""+location.getLatitude();
                                  lon[0]=""+location.getLongitude();
                                }
                            });
                    Date currentTime = Calendar.getInstance().getTime();
                    RequestBody fileName= RequestBody.create(MediaType.parse("multipart/form-data"), filename[0]);
                    RequestBody dateTime= RequestBody.create(MediaType.parse("multipart/form-data"), ""+currentTime);
                    RequestBody latitude = RequestBody.create(MediaType.parse("multipart/form-data"), lat[0]);
                    RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"), lon[0]);
                    RequestBody Ax = RequestBody.create(MediaType.parse("multipart/form-data"), ""+accData[0]);
                    RequestBody Ay = RequestBody.create(MediaType.parse("multipart/form-data"), ""+accData[1]);
                    RequestBody Az = RequestBody.create(MediaType.parse("multipart/form-data"), ""+accData[2]);
                    RequestBody Gx = RequestBody.create(MediaType.parse("multipart/form-data"), ""+gyroData[0]);
                    RequestBody Gy = RequestBody.create(MediaType.parse("multipart/form-data"), ""+gyroData[1]);
                    RequestBody Gz = RequestBody.create(MediaType.parse("multipart/form-data"), ""+gyroData[2]);
                    IUploadAPI mService= RetrofitClient.getClient().create(IUploadAPI.class);
                    mService.recordData(fileName,latitude,longitude,dateTime,Ax,Ay,Az,Gx,Gy,Gz).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            filename[0]=response.body();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                    if (!shouldStopLoop[0]) {
                        mHandler.postDelayed(this, 200);
                    }
                }
            };
            new Thread(runnable).start();
        });

        endJourney.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shouldStopLoop[0] =true;
                        Intent i=new Intent(DirectionActivity.this,PredictedActivity.class);
                        i.putExtra("filename",filename[0]);
                        startActivity(i);
                    }
                }
        );

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
        for(LocationData l:Storage.potholeData) {
           // Toast.makeText(DirectionActivity.this,l.getLatitude()+""+l.getLongitude(),Toast.LENGTH_LONG).show();
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(l.getLatitude(),l.getLongitude())).icon(IconFactory.getInstance(DirectionActivity.this).fromResource(R.drawable.pospemarker));
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
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(l.getLatitude(),l.getLongitude())).icon(IconFactory.getInstance(DirectionActivity.this).fromResource(R.drawable.pospemarker));
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
                                           .icon(IconFactory.getInstance(DirectionActivity.this).fromResource(R.drawable.placeholder));
                            markerOptions.setTitle("End Point");
                            markerOptions.setSnippet("Destination");
                            Marker marker = mapmyIndiaMap.addMarker(markerOptions);

                            LocationComponentOptions options = LocationComponentOptions.builder(DirectionActivity.this)
                                    .trackingGesturesManagement(true)
                                    .accuracyColor(ContextCompat.getColor(DirectionActivity.this, R.color.purple_500))
                                    .build();
                            LocationComponent locationComponent = mapmyIndiaMap.getLocationComponent();
                            if (ActivityCompat.checkSelfPermission(DirectionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DirectionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationComponent.activateLocationComponent(DirectionActivity.this, options);
                            locationComponent.setLocationComponentEnabled(true);
                            locationEngine = locationComponent.getLocationEngine();

                            locationEngine.addLocationEngineListener(DirectionActivity.this);
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
                    Toast.makeText(DirectionActivity.this, response.message() + response.code(), Toast.LENGTH_LONG).show();
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
        super.onPause();
        mSensorManager.unregisterListener(this);
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            accData[0]=sensorEvent.values[0];
            accData[1]=sensorEvent.values[1];
            accData[2]=sensorEvent.values[2];
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            gyroData[0]=sensorEvent.values[0];
            gyroData[1]=sensorEvent.values[1];
            gyroData[2]=sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
