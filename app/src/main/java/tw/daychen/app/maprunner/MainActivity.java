package tw.daychen.app.maprunner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tw.daychen.app.maprunner.data.MapRunnerContract;
import tw.daychen.app.maprunner.utilities.JsonUtils;
import tw.daychen.app.maprunner.utilities.NetUtils;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private TextView mTextMessage;
    private static final String LOG_TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 102;

    private static int addSiteCODE = 3;
    private static int loginCODE = 2;

    private static final String CONTENT_AUTHORITY = "tw.daychen.app.maprunner";
    private static final String PATH_Setting = "setting";
    private static final String PATH_Site = "site";
    private static final String PATH_SiteN2M = "siten2m";

    private CheckSiteNetworkTask mSiteTask = null;
    private static String username = null;


    // Google API用戶端物件
    private GoogleApiClient googleApiClient;


    // Location請求物件
    private LocationRequest locationRequest;

    // 記錄目前最新的位置
    private Location currentLocation;

    // 顯示目前與儲存位置的標記物件
    private Marker currentMarker, itemMarker;
    private Map<Marker, String> marker_id = new HashMap<>();
    private Map<Marker, String> marker_title = new HashMap<>();
    private Map<Marker, String> marker_content = new HashMap<>();
    private ArrayList<Marker> nearbyMarker = new ArrayList<>();
    private ArrayList<HashMap<String, String>> cacheNearbyMarker;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_logout:
                    getContentResolver().delete(Uri.parse("content://tw.daychen.app.maprunner/setting/"),"key='username'", null);
                    getContentResolver().delete(Uri.parse("content://tw.daychen.app.maprunner/setting/"),"key='server_access_token'", null);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, loginCODE);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, loginCODE);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton myFab = (FloatingActionButton) this.findViewById(R.id.fab_add);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addSite();
            }
        });


        // 建立Google API用戶端物件
        configGoogleApiClient();

        // 建立Location請求物件
        configLocationRequest();

        // 連線到Google API用戶端
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }


        Log.d(LOG_TAG, String.valueOf(currentLocation == null));

        Log.d(LOG_TAG, "onCreate");
    }

    private void addSite() {
        if (currentLocation == null) {
            Toast.makeText(this, "尚未取得GPS訊號。", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Intent intent = new Intent(this, AddSiteActivity.class);
        intent.putExtra("lat", currentLocation.getLatitude());
        intent.putExtra("long", currentLocation.getLongitude());
        intent.putExtra("username", username);
        startActivityForResult(intent, addSiteCODE);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        Log.d(LOG_TAG, "onConnected");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "not have Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            Log.d(LOG_TAG, "havePermission");

            // TODO 有時候地點會載入失敗
            // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, MainActivity.this);
            Log.d(LOG_TAG, LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient).toString());

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            currentLocation = location;


            Log.d(LOG_TAG, "current Location" + currentLocation.toString());

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        Log.d(LOG_TAG, "onConnectionSuspended:" + String.valueOf(i));
        // int參數是連線中斷的代號
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        Log.d(LOG_TAG, "onConnectionFailed");
        int errorCode = connectionResult.getErrorCode();

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        Log.d(LOG_TAG, "onLocationChanged");
        currentLocation = location;

        LatLng latLng = new LatLng(
                location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        } else {
            currentMarker.setPosition(latLng);
        }

        // 移動地圖到目前的位置
        moveMap(latLng);
        addOtherSite();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        // 讀取記事儲存的座標
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat", 24.95);
        double lng = intent.getDoubleExtra("lng", 121.23);
        Log.d(LOG_TAG, "lat:" + String.valueOf(lat) + ",lng" + String.valueOf(lng));
        if (lat != 0.0 && lng != 0.0) {
            // 建立座標物件
            LatLng itemPlace = new LatLng(lat, lng);
//            // 加入地圖標記
//            addMarker(itemPlace, intent.getStringExtra("title"),
//                    intent.getStringExtra("datetime"));
            // 移動地圖
            moveMap(itemPlace);
        }
        addOwnSite();

        Log.d(LOG_TAG, "onMapReady");
    }

    // 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        Log.d(LOG_TAG, "moveMap");
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // 在地圖加入指定位置與標題的標記
    private void addMarker(LatLng place, String title, String content, String id, String category) {
        Log.d(LOG_TAG, "addMarker");


        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        if (category.equals("探索世界")) {
            Log.d(LOG_TAG, "EXPLORE");
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_explore);
        } else if (category.equals("資訊")) {
            Log.d(LOG_TAG, "INFO");
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_info);
        } else if (category.equals("紅利")) {
            Log.d(LOG_TAG, "BOUNUS");
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_bonus);
        }


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title)
                .icon(icon);


        Marker t = mMap.addMarker(markerOptions);
        mMap.setOnMarkerClickListener(this);
        marker_id.put(t, id);
        marker_title.put(t, title);
        marker_content.put(t, content);
    }
    private void addNearbyMarker(LatLng place, String title, String content, String id, String category) {
        Log.d(LOG_TAG, "addNearbyarker");
        BitmapDescriptor icon =
                BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        if (category.equals("探索世界")) {
            Log.d(LOG_TAG, "EXPLORE");
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_explore);
        } else if (category.equals("資訊")) {
            Log.d(LOG_TAG, "INFO");
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_info);
        } else if (category.equals("紅利")) {
            Log.d(LOG_TAG, "BOUNUS");
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_bonus);
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title)
                .snippet(content)
                .icon(icon);
        Log.d(LOG_TAG, place.toString());
        Log.d(LOG_TAG, markerOptions.toString());

        Marker t = mMap.addMarker(markerOptions);
        nearbyMarker.add(t);

        mMap.setOnMarkerClickListener(this);
        marker_id.put(t, id);
        marker_title.put(t, title);
        marker_content.put(t, content);

    }



    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // 建立Location請求物件
    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void clearMarker() {
        try {
            for (Map.Entry<Marker, String> entry : marker_id.entrySet()) {
                try {
                    marker_id.remove(entry.getKey());
                    marker_title.remove(entry.getKey());
                    marker_content.remove(entry.getKey());
                    entry.getKey().remove();
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        addOwnSite();
    }

    private void addOwnSite() {

        if (username != null) {
            Log.d(LOG_TAG, "addOwnSite");
            Uri contacts_uri = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + PATH_Site + "/");
            String[] selectionArgs = new String[1];
            selectionArgs[0] = username;
            try (Cursor cursor = getContentResolver().query(contacts_uri, null, "username=?", selectionArgs, null)) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MapRunnerContract.SiteEntry._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MapRunnerContract.SiteEntry.COLUMN_TITLE));
                    String content = cursor.getString(cursor.getColumnIndex(MapRunnerContract.SiteEntry.COLUMN_CONTENT));
                    String category = cursor.getString(cursor.getColumnIndex(MapRunnerContract.SiteEntry.COLUMN_CLASS));
                    String[] latlngStr = cursor.getString(cursor.getColumnIndex(MapRunnerContract.SiteEntry.COLUMN_LATLNG)).split(",");
                    LatLng latLng = new LatLng(
                            Double.parseDouble(latlngStr[0]), Double.parseDouble(latlngStr[1]));
                    addMarker(latLng, title, content, String.valueOf(id), category);
                }
            }
        }
    }

    private void addOtherSite() {
        String urlQuery = "maprunner/site/get_other_site/";
        URL Url = NetUtils.buildUrl(urlQuery, this);
        mSiteTask = new CheckSiteNetworkTask(currentLocation.getLongitude(), currentLocation.getLatitude(), Url);
        mSiteTask.execute((Void) null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        // 連線到Google API用戶端
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }

        // 檢查是否有登入
        try (Cursor cursor = getContentResolver().query(Uri.parse("content://tw.daychen.app.maprunner/setting/"), null, "key='username'", null, null)) {
            Log.d(LOG_TAG, String.valueOf(cursor.getCount()));
            if (cursor.getCount() == 0) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, loginCODE);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        // 移除位置請求服務
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        // 移除Google API用戶端連線
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == addSiteCODE) {
            clearMarker();
        }
        if (resultCode == loginCODE) {
            Log.d(LOG_TAG, "Success Login");
            try (Cursor cursor = getContentResolver().query(Uri.parse("content://tw.daychen.app.maprunner/setting/"), null, "key='username'", null, null)) {
                cursor.moveToFirst();
                username = cursor.getString(cursor.getColumnIndex(MapRunnerContract.SettingEntry.COLUMN_VALUE));
            } catch (Exception e){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, loginCODE);
            }
            clearMarker();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        try {
            Log.d(LOG_TAG, marker_id.get(marker));

            Log.d(LOG_TAG, "marker clicked!");
            if (marker.equals(itemMarker)) {
                // open site detail
                return false;
            }
            Intent detailIntent = new Intent(MainActivity.this, SiteDetailActivity.class);
            detailIntent.putExtra("id", marker_id.get(marker));
            detailIntent.putExtra("title", marker_title.get(marker));
            detailIntent.putExtra("content", marker_content.get(marker));

            startActivity(detailIntent);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    private class CheckSiteNetworkTask extends AsyncTask<Void, Void, Boolean> {

        private final double mLongitude;
        private final double mLatitude;
        private final URL mUrl;


        CheckSiteNetworkTask(double longitude, double latitude, URL url) {
            mLongitude = longitude;
            mLatitude = latitude;
            mUrl = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String searchResult;
            try {
                cacheNearbyMarker = null;
                HashMap<String, String> postData = new HashMap<>();
                postData.put("lat", String.valueOf(mLatitude));
                postData.put("long", String.valueOf(mLongitude));
                searchResult = NetUtils.getResponseFromAccessCode(mUrl, "POST", postData);

                try {

                    cacheNearbyMarker = JsonUtils.getNearbySiteFromJson(searchResult);


                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSiteTask = null;
            if (success) {
                for (Marker m: nearbyMarker){
                    try {
                        marker_id.remove(m);
                        marker_content.remove(m);
                        marker_title.remove(m);
                        m.remove();
                    } catch (Exception e){
                        continue;
                    }
                }
                nearbyMarker.clear();
                for (HashMap<String, String> data : cacheNearbyMarker) {
                    Log.d(LOG_TAG, data.toString());
                    LatLng location = new LatLng(Double.parseDouble(data.get("lat")),
                            Double.parseDouble(data.get("long")));

                    addNearbyMarker(location, data.get("title"), data.get("content"), data.get("id"), data.get("class"));
                }
            }
            cacheNearbyMarker = null;
        }

        @Override
        protected void onCancelled() {
            mSiteTask = null;
        }
    }
}
