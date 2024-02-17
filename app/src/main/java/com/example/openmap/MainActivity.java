package com.example.openmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;

    Double Latitude_Location = 0.0;
    Double Longitude_Location = 0.0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

//        IMapController mapController = map.getController();
//        mapController.setZoom(9.5);
//        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
//        mapController.setCenter(startPoint);
//
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

        this.mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map);
        this.mCompassOverlay.enableCompass();
        map.getOverlays().add(this.mCompassOverlay);

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Title", "Description", new GeoPoint(0.0d,0.0d))); // Lat/Lon decimal degrees

//the overlay
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, this);
        mOverlay.setFocusItemsOnTap(true);

        map.getOverlays().add(mOverlay);



//        GeoPoint startPoint = new GeoPoint(21.028511, 105.804817);
//
//        Marker startMarker = new Marker(map);
//        startMarker.setPosition(startPoint);
//        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
//        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_launcher_background));
//        startMarker.setTitle("Start point");
//        map.getOverlays().add(startMarker);
//


        Marker m = new Marker(map);
        m.setPosition(new GeoPoint(0d,0d));
        m.setTextLabelBackgroundColor(
                Color.TRANSPARENT
        );
        m.setTextLabelForegroundColor(
                Color.RED
        );
        m.setTextLabelFontSize(40);
        m.setTextIcon("text");
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
        map.getOverlays()
                .add(m);



        Marker marker = new Marker(map);
        Overlay mOverlay1 = new Overlay() {

            @Override
            public boolean onScroll(MotionEvent pEvent1, MotionEvent pEvent2, float pDistanceX, float pDistanceY, MapView pMapView) {

                marker.setPosition(new GeoPoint((float) pMapView.getMapCenter().getLatitude(),
                        (float) pMapView.getMapCenter().getLongitude()));

                return super.onScroll(pEvent1, pEvent2, pDistanceX, pDistanceY, pMapView);
            }
        };

        map.getOverlays().add(mOverlay1);


        List<GeoPoint> geoPoints = new ArrayList<>();


        // Hà Nội - Gia Lâm
        // Tạo một Marker và đặt vị trí cho nó




        //
        // Đặt vị trí mặc định và thu phóng
        IMapController mapController = map.getController();
        mapController.setZoom(16.0);

        double latitude = 21.025491;
        double longitude = 105.841329;
        mapController.setCenter(new GeoPoint(latitude, longitude));


        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference locationRef = database.getReference("location");

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Lấy giá trị từ cả hai nhánh
                Double latitudeValue = dataSnapshot.child("latitude").getValue(Double.class);
                Double longitudeValue = dataSnapshot.child("longitude").getValue(Double.class);

                Double latitudeValue_Old = 0.0;
                Double longitudeValue_Old = 0.0;
                if (latitudeValue != null) {
                    // Xử lý dữ liệu latitude kiểu Double ở đây
                    Latitude_Location = latitudeValue;
                    Log.d("Latitude value", latitudeValue + " ");
                }

                if (longitudeValue != null) {
                    // Xử lý dữ liệu longitude kiểu Double ở đây
                    Longitude_Location = longitudeValue;
                    Log.d("Longitude value", longitudeValue + " ");
                }

                // Thêm biểu tượng ga tàu
                Marker trainStationMarker_person = new Marker(map);
                trainStationMarker_person.setPosition(new GeoPoint(latitudeValue, longitudeValue));
                trainStationMarker_person.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                trainStationMarker_person.setIcon(getResources().getDrawable(R.drawable.baseline_person_pin_circle_24));
                map.getOverlays().add(trainStationMarker_person);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi xảy ra
                Log.e("FirebaseError", "Error getting data", error.toException());
            }
        });


        // Thêm biểu tượng ga tàu
        Marker trainStationMarker_person = new Marker(map);
        trainStationMarker_person.setPosition(new GeoPoint(Latitude_Location, Longitude_Location));
        trainStationMarker_person.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        trainStationMarker_person.setIcon(getResources().getDrawable(R.drawable.baseline_person_pin_circle_24));
        map.getOverlays().add(trainStationMarker_person);

        // Thêm biểu tượng ga tàu
        Marker trainStationMarker = new Marker(map);
        trainStationMarker.setPosition(new GeoPoint(latitude, longitude));
        trainStationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        trainStationMarker.setIcon(getResources().getDrawable(R.drawable.baseline_train_24));
        map.getOverlays().add(trainStationMarker);

        // Thêm biểu tượng ga tàu
        Marker trainStationMarker1 = new Marker(map);
        trainStationMarker1.setPosition(new GeoPoint( 21.051487, 105.878703));
        trainStationMarker1.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        trainStationMarker1.setIcon(getResources().getDrawable(R.drawable.baseline_train_24));
        map.getOverlays().add(trainStationMarker1);

        geoPoints.add(new GeoPoint(21.025491, 105.841329)); // Ga Hà Nội (ví dụ)
        geoPoints.add(new GeoPoint(21.023281, 105.844485)); // Ga Hà Nội (ví dụ)
        geoPoints.add(new GeoPoint(21.031117, 105.845365)); // Điểm trung gian
        geoPoints.add(new GeoPoint(21.036951, 105.846094)); // Điểm trung gian
        geoPoints.add(new GeoPoint(21.039727, 105.846640)); // Điểm trung gian
        geoPoints.add(new GeoPoint(21.040747, 105.849796)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.035648, 105.854105)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.041540, 105.869703)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.043805, 105.872191)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.046751, 105.878017)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.048563, 105.874801)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.049866, 105.877532)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.04778, 105.878442)); // Ga Hải Phòng (ví dụ)

//
        geoPoints.add(new GeoPoint(21.049250, 105.883311)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.052486, 105.886078)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.054168, 105.887462)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.054369, 105.887870)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.054379, 105.888085)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.054218, 105.888653)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.047730, 105.893267)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.046809, 105.893835)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.042794, 105.896475)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.039404, 105.897943)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.037741, 105.898672)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.036820, 105.899702)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.030171, 105.913328)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.022159, 105.935365)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.021138, 105.9379182)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.006415, 105.958453)); // Ga Hải Phòng (ví dụ)
        geoPoints.add(new GeoPoint(21.004252, 105.962466)); // Ga Hải Phòng (ví dụ)


        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setWidth(3.0F);
        line.setColor(Color.RED);
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        map.getOverlayManager().add(line);


        requestPermissionsIfNecessary(new String[]{
                // if you need to show the current location, uncomment the line below
                // Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}