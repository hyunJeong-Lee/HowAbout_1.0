package com.example.howabout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.howabout.API.KakaoAPIClient;
import com.example.howabout.API.RetrofitClient;
import com.example.howabout.Search.CategoryResult;
import com.example.howabout.Search.Document;
import com.example.howabout.Search.SearchAdapter;
import com.example.howabout.functions.HowAboutThere;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import org.json.simple.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FindActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener {

    SharedPreferences sharedPreferences;

    HowAboutThere FUNC = new HowAboutThere();

    Map<String, Double> first_marker_location; //????????? ?????? ?????? ??????
    Map<String, Document> saveCourse_data = new HashMap<>(); //?????? ?????? ???, ????????? ????????? document ?????? {"rest", "cafe"}
    Map<String, String> getLocationInfo_data; //?????? ?????? ?????? ?????? ???, ????????? ?????? ????????? ?????? {place_name, place_url, place_id}
    ArrayList<Object> result_list; //?????? ?????? ???, ????????? ?????? ????????? {u_id, rest document, cafe document}
    Map<String, String> saveMyCourse_data = new HashMap<>();
    JSONObject location; //????????? ?????? location ?????? ?????? (x, y, radius)
    Document rest, cafe;

    //Search keyword
    RecyclerView rl_search; //recycler view
    SearchAdapter searchAdapter; //recycler view??? ????????? adapter
    EditText ed_search; //????????? ???????????? Edit Text

    //map, marker
    private MapView mapView;
    MapPOIItem marker; //?????? ??????. (?????? ??????, ????????????)
    MapPOIItem custom_marker; //???????????? ????????? ??????, ?????? ??????.
    MapPolyline polyline; //?????? ??????
    Switch aSwitch; //????????? ?????? ?????????

    //FloatingActionButton, ???????????????
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab_currentLoca, fab2;

    //???????????? ????????????
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    //???????????????
    private boolean isTrackingMode = false;

    //??????
    int radius = 600;

    //?????? ?????? ?????? code
    static boolean CODE_1st = false;
    static boolean CODE_2nd = false;
    static boolean CODE_3rd = false;
    static String CODE_flag = "3.0";

    //CalloutBalloonAdapter ??????????????? ??????
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.find_dialog_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem mapPOIItem) { //?????? ?????? ??? ????????? ???(?????????)
            //???????????? ?????? ?????? ??????
            ((TextView) mCalloutBalloon.findViewById(R.id.tv_balloon_placename)).setText(mapPOIItem.getItemName());
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem mapPOIItem) { //????????? ?????? ??? ????????? ???
            return null;
        }
    } //CustomCalloutBalloonAdapter{}...


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find);

        FUNC.sideBar(FindActivity.this);

        //????????????.
        rl_search = (RecyclerView) findViewById(R.id.rl_search);
//        rl_search.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));
        ed_search = findViewById(R.id.ed_search);
        ed_search.addTextChangedListener(textWatcher);

        //FloatingActionButton. ?????? ?????? ?????? ??????. ??????, ????????? ??????.
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_currentLoca = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        fab.setOnClickListener(click_fab);
        fab_currentLoca.setOnClickListener(click_fab);
        fab2.setOnClickListener(click_fab);

        //mapview
        mapView = findViewById(R.id.map_view);
        mapView.setCurrentLocationEventListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.fitMapViewAreaToShowAllPOIItems();
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

        //save mycourse
        aSwitch = (Switch) findViewById(R.id.find_switch);
        aSwitch.setOnTouchListener(click_switch);
//        aSwitch.setOnCheckedChangeListener(check_switch);
    } //...onCreate()

    //???????????? ????????? ?????? ????????? ??????
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            rl_search.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String keyword = charSequence.toString();
            if (ed_search.getText().toString().length() > 0) {
                mapView.removeAllPolylines();
                mapView.removeAllPOIItems();
                aSwitch.setChecked(false);
                searchKeyword(keyword);
            }
//            else {
//                searchAdapter.clear();
//                searchAdapter.notifyDataSetChanged();
//            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }; //...textWatcher

    //????????? ?????? ?????? ================================================================================
    public void searchKeyword(String keyword) {

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); //soft keyboard handling manager
//        String API_KEY = "KakaoAK f33950708cffc6664e99ac21489fd117"; //kakao app key
        String API_KEY = "KakaoAK 66be8808cdde00fd86beab9d744bdffc"; //kakao app key
        Log.e("searchKeyword", "?????? ????????????? : " + keyword);

        Call<CategoryResult> search = KakaoAPIClient.getApiService().getSearchKeword(API_KEY, keyword); //kakap rest api ????????????
        search.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(Call<CategoryResult> call, Response<CategoryResult> response) {
//                Log.e("searchKeyword", "post ??????!! ?????????: "+response.body().getDocuments().toString());

                List<Document> result = response.body().getDocuments();
                if (result.size() != 0) { //api ????????? 0??? ????????? adapter??? ??????
                    searchAdapter = new SearchAdapter(result);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(FindActivity.this, LinearLayoutManager.VERTICAL, false); //????????????????????? ??????
//                    rl_search.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));//???????????????
                    rl_search.setLayoutManager(layoutManager);
                    rl_search.setAdapter(searchAdapter);

                    searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClicked(int position, View view) {
                            rl_search.setVisibility(View.GONE);
                            aSwitch.setVisibility(View.GONE);
                            inputMethodManager.hideSoftInputFromWindow(ed_search.getWindowToken(), 0); //hide keyboard
                            Document document = result.get(position);
                            MapMarker(document.getPlaceName(), Double.parseDouble(document.getX()), Double.parseDouble(document.getY()));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    } //...searchKeyword()

    //?????? ????????? ?????? ?????? ?????? ????????? ??????
    View.OnClickListener click_fab = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.fab:
                    anim();
//                    Toast.makeText(FindActivity.this, "Floating Action Button", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.fab1:
                    anim();
                    currentLocation();
                    Toast.makeText(FindActivity.this, "??????????????? ???????????? ????????????. ????????? ?????????????????? ????", Toast.LENGTH_LONG).show();
                    isTrackingMode = false;
                    break;
                case R.id.fab2: //????????? ?????? ??????
                    anim();
                    Toast.makeText(FindActivity.this, "Button2", Toast.LENGTH_SHORT).show();
                    isFabOpen = true;
                    anim();
                    break;
            }
        }
    };

    //?????? ?????? ??????
    public void currentLocation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!checkLocationServicesStatus()) {
                    showDialogForLocationServiceSetting();
                } else {
                    checkRunTimePermission();
                }
            }
        }, 1000);
    }

    //floatingactionbutton animation
    public void anim() {
        if (isFabOpen) {
            fab_currentLoca.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab_currentLoca.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab_currentLoca.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab_currentLoca.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }

    //????????? ?????? ????????????
    public void draw_marker(String MarkerName, double x, double y, int image) {
        Log.i("leehj", "marker name, x, y : " + MarkerName + ", " + x + ", " + y);

        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(y, x);
        mapView.setMapCenterPoint(mapPoint, true);
        marker = new MapPOIItem();
        marker.setItemName(MarkerName);
        marker.setMapPoint(mapPoint);
        marker.setTag(80);
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
        marker.setCustomImageResourceId(image); // ?????? ?????????.
        marker.setCustomImageAutoscale(false); // hdpi, xhdpi ??? ??????????????? ???????????? ???????????? ????????? ?????? ?????? ?????????????????? ????????? ????????? ??????.
        marker.setCustomImageAnchor(0.5f, 1.0f); // ?????? ???????????? ????????? ?????? ??????(???????????????) ?????? - ?????? ????????? ?????? ?????? ?????? x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) ???.
        marker.setShowCalloutBalloonOnTouch(true);
        mapView.addPOIItem(marker);
    }

    //basic marker (????????????, ???????????? )===============================================================
    public void MapMarker(String MakerName, double x, double y) {
        CODE_1st = true;
        CODE_2nd = false;

        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(y, x);
        mapView.setMapCenterPoint(mapPoint, true);
        //true??? ??? ?????? ??? ??????????????? ????????? ????????? false??? ?????????????????? ???????????????.

        marker = new MapPOIItem();
        marker.setItemName(MakerName); // ?????? ?????? ??? ??????????????? ?????? ??????
        marker.setMapPoint(mapPoint);
        marker.setTag(60);
        // ???????????? ???????????? BluePin ?????? ??????.
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker.setCustomImageResourceId(R.drawable.location_red);
        marker.setCustomImageAutoscale(false); // hdpi, xhdpi ??? ??????????????? ???????????? ???????????? ????????? ?????? ?????? ?????????????????? ????????? ????????? ??????.
//        custom_marker.setCustomImageAnchor(0.5f, 1.0f); // ?????? ???????????? ????????? ?????? ??????(???????????????) ?????? - ?????? ????????? ?????? ?????? ?????? x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) ???.

        marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker.setCustomSelectedImageResourceId(R.drawable.location_blue);
        //?????? ????????? ???????????? ??????
        marker.setDraggable(true);
        mapView.addPOIItem(marker);
    }

    //custom marker ================================================================================
    public void customMarker(Document document, int image) {
        String place_name = document.getPlaceName();
        double x = Double.parseDouble(document.getX());
        double y = Double.parseDouble(document.getY());

        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(y, x);
        mapView.setMapCenterPoint(mapPoint, true);
        //true??? ??? ?????? ??? ??????????????? ????????? ????????? false??? ?????????????????? ???????????????.
        custom_marker = new MapPOIItem();
        custom_marker.setItemName(place_name); // ?????? ?????? ??? ??????????????? ?????? ??????
        custom_marker.setUserObject(document);
        custom_marker.setMapPoint(mapPoint);
        // ???????????? ???????????? BluePin ?????? ??????.
        custom_marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
        custom_marker.setCustomImageResourceId(image); // ?????? ?????????.
        custom_marker.setCustomImageAutoscale(false); // hdpi, xhdpi ??? ??????????????? ???????????? ???????????? ????????? ?????? ?????? ?????????????????? ????????? ????????? ??????.
        custom_marker.setCustomImageAnchor(0.5f, 1.0f); // ?????? ???????????? ????????? ?????? ??????(???????????????) ?????? - ?????? ????????? ?????? ?????? ?????? x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) ???.
        //?????? ????????? ???????????? ??????
        custom_marker.setDraggable(true);
        mapView.addPOIItem(custom_marker);
    } //..customMarker

    //???????????? ???????????? ===============================================================================
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();

        double mCurrentLat = mapPointGeo.latitude;
        double mCurrentLng = mapPointGeo.longitude;
        //????????? ??????
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);

        MapPoint currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);

        mapView.setMapCenterPoint(currentMapPoint, true);
        mapView.setCurrentLocationRadiusStrokeColor(Color.argb(128, 255, 87, 87));
        mapView.setCurrentLocationRadiusFillColor(Color.argb(0, 0, 0, 0));

        mapView.removeAllPOIItems();
        mapView.removeAllPolylines();
        aSwitch.setVisibility(View.GONE);
        MapMarker("????????????", mCurrentLng, mCurrentLat);

        if (!isTrackingMode) {
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    //????????? ?????? ????????? ??????, ?????? ?????? ??????
    void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(FindActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION); //?????? ????????? ??????

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) { //?????? ?????? ???????????? ????????? ?????????
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); //?????? ????????? ????????????
        } else {  // ???????????? ????????? ????????? ????????? ?????? ?????? ??????
            if (ActivityCompat.shouldShowRequestPermissionRationale(FindActivity.this, REQUIRED_PERMISSIONS[0])) { //???????????? ????????? ?????? ????????? ?????? ??????
                Toast.makeText(FindActivity.this, "??? ????????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show(); //?????? ?????? ??? ?????? ?????? ??????
                ActivityCompat.requestPermissions(FindActivity.this, REQUIRED_PERMISSIONS, //?????? ????????? ??????
                        PERMISSIONS_REQUEST_CODE);
            } else { //???????????? ????????? ?????? ????????? ?????? ??????
                ActivityCompat.requestPermissions(FindActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE); //?????? ????????? ??????
            }
        }
    }

    //GPS ???????????? ?????? ????????? ??????+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //???????????? gps????????? ??????????????? ????????? ?????? ???????????? ??????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ?????????????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    //?????? gps??? ???????????? ??? ?????? ???????????? ???????????????
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } //GPS ???????????? ?????? ????????? ??????+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    public void onMapViewInitialized(MapView mapView) {
        MapPoint mp = mapView.getMapCenterPoint();
        MapPoint.GeoCoordinate gc = mp.getMapPointGeoCoord();

        double gCurrentLat = gc.latitude;
        double gCurrentLog = gc.longitude;
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        MapPoint mp = mapView.getMapCenterPoint();
        MapPoint.GeoCoordinate gc = mp.getMapPointGeoCoord();

        double gCurrentLat = gc.latitude;
        double gCurrentLog = gc.longitude;

        Log.i("leehj", "?????? ????????? ?????? ??? ??????: " + gCurrentLat + "??????: " + gCurrentLog + "??????" + radius);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    //????????? ?????? ????????? ?????? ==========================================================================
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        String place_name = mapPOIItem.getItemName();
        double lat = mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude; //??????, y
        double lon = mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude; //??????, x
        Toast.makeText(this, "??????: " + mapPOIItem.getItemName(), Toast.LENGTH_SHORT).show();

        if (CODE_1st) { //?????? ????????? MapMarker balloon touched
            Dialog dialog = new Dialog(FindActivity.this);
            dialog.setContentView(R.layout.find_dialog_first_balloon_click);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            first_marker_location = new HashMap();
            first_marker_location.put("x", lon);
            first_marker_location.put("y", lat);

            //????????? ??????
            TextView rest = (TextView) dialog.findViewById(R.id.dialog_click_rest);
            rest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    mapView.removeAllPOIItems();
                    draw_marker("????????? ??????", first_marker_location.get("x"), first_marker_location.get("y"), R.drawable.location_blue);
                    SearchRestaurant(String.valueOf(lon), String.valueOf(lat), String.valueOf(radius));
                }
            });

            //?????? ??????
            TextView cafe = (TextView) dialog.findViewById(R.id.dialog_click_cafe);
            cafe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    mapView.removeAllPOIItems();
                    draw_marker("????????? ??????", first_marker_location.get("x"), first_marker_location.get("y"), R.drawable.location_blue);
                    SearchCafe(String.valueOf(lon), String.valueOf(lat), String.valueOf(radius));
                }
            });
            dialog.show();
        } else { //NOT ?????? ????????? MapMarker balloon touched
            Dialog dialog_2st = new Dialog(FindActivity.this);
            dialog_2st.setContentView(R.layout.find_dialog_balloon_click);
            dialog_2st.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ImageButton select = (ImageButton) dialog_2st.findViewById(R.id.balloon_click_btn_check);
            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog_2st.dismiss();
                    if (CODE_2nd && !CODE_3rd) { //????????? ?????? ????????? ??????
                        Document rest_document = (Document) mapPOIItem.getUserObject(); //?????? ?????? Dcument Object ????????????
                        saveCourse_data.put("rest", rest_document); // ?????? ?????? Map??? ??????
                        saveMyCourse_data.put("r_id", rest_document.getId());
                        mapView.removeAllPOIItems(); //??? ?????? ?????????
                        draw_marker("????????? ??????", first_marker_location.get("x"), first_marker_location.get("y"), R.drawable.location_blue); //?????? ????????? ?????? ?????? ??????
                        draw_marker(rest_document.getPlaceName(), Double.parseDouble(rest_document.getX()), Double.parseDouble(rest_document.getY()), R.drawable.location_rest_blue); //?????? ????????? ?????? ?????? ??????
                        SearchCafe(String.valueOf(lon), String.valueOf(lat), String.valueOf(radius)); //?????? ?????? ?????? ????????? ?????? ??????
                        Log.e("leehj", "?????? ?????? : " + place_name + ", ?????? ??????: " + lon + ", " + lat);

                    } else if (!CODE_2nd && CODE_3rd) { //????????? ?????? ????????? ??????
                        Document cafe_document = (Document) mapPOIItem.getUserObject(); //?????? ?????? Document Object ????????????
                        saveCourse_data.put("cafe", cafe_document); //?????? ?????? Map??? ??????
                        saveMyCourse_data.put("c_id", cafe_document.getId());
                        mapView.removeAllPOIItems(); //??? ?????? ?????????
                        draw_marker("????????? ??????", first_marker_location.get("x"), first_marker_location.get("y"), R.drawable.location_blue); //?????? ????????? ?????? ?????? ??????
                        draw_marker(cafe_document.getPlaceName(), Double.parseDouble(cafe_document.getX()), Double.parseDouble(cafe_document.getY()), R.drawable.location_cafe_blue); //?????? ????????? ?????? ?????? ??????
                        SearchRestaurant(String.valueOf(lon), String.valueOf(lat), String.valueOf(radius)); //?????? ?????? ?????? ????????? ?????? ??????
                        Log.e("leehj", "?????? ?????? : " + place_name + ", ?????? ??????: " + lon + ", " + lat);

                    } else if (CODE_2nd && CODE_3rd) { //??????, ?????? ?????? ??????
                        //CODE ?????????
                        CODE_1st = false;
                        CODE_2nd = false;
                        CODE_3rd = false;

                        mapView.removeAllPOIItems();
                        polyline = new MapPolyline();
                        polyline.setTag(1000);
//                        polyline.setLineColor();

                        if (saveCourse_data.containsKey("rest")) { //?????? ????????? ?????? ??????. ????????? ?????? ????????? ??????
                            Document cafe_document = (Document) mapPOIItem.getUserObject();
                            saveCourse_data.put("cafe", cafe_document);
                            saveMyCourse_data.put("c_id", cafe_document.getId());
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(first_marker_location.get("y"), first_marker_location.get("x")));
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(saveCourse_data.get("rest").getY()), Double.parseDouble(saveCourse_data.get("rest").getX())));
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(saveCourse_data.get("cafe").getY()), Double.parseDouble(saveCourse_data.get("cafe").getX())));
                        } else { //?????? ????????? ?????? ??????. ????????? ?????? ????????? ??????
                            Document rest_document = (Document) mapPOIItem.getUserObject();
                            saveCourse_data.put("rest", rest_document);
                            saveMyCourse_data.put("r_id", rest_document.getId());
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(first_marker_location.get("y"), first_marker_location.get("x")));
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(saveCourse_data.get("cafe").getY()), Double.parseDouble(saveCourse_data.get("cafe").getX())));
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(saveCourse_data.get("rest").getY()), Double.parseDouble(saveCourse_data.get("rest").getX())));
                        }

                        //??????, ??????, ?????? ?????? ??????
                        Document rest_marker = (Document) saveCourse_data.get("rest");
                        Document cafe_marker = (Document) saveCourse_data.get("cafe");

                        draw_marker("????????? ??????", first_marker_location.get("x"), first_marker_location.get("y"), R.drawable.location_blue);
                        draw_marker(rest_marker.getPlaceName(), Double.parseDouble(rest_marker.getX()), Double.parseDouble(rest_marker.getY()), R.drawable.location_rest_blue);
                        draw_marker(cafe_marker.getPlaceName(), Double.parseDouble(cafe_marker.getX()), Double.parseDouble(cafe_marker.getY()), R.drawable.location_cafe_blue);

                        //?????? ????????? ?????? ??????
                        mapView.addPolyline(polyline);
//                        aSwitch.setChecked(false);
//                        aSwitch.setClickable(true);
                        aSwitch.setVisibility(View.VISIBLE);
                        aSwitch.setClickable(false);
                        aSwitch.setFocusable(false);

                        //?????? ?????? ??????
                        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                        int padding = 400; // px
//                        mapView.fitMapViewAreaToShowAllPolylines();
                        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));

                        sharedPreferences = getSharedPreferences("USER", Activity.MODE_PRIVATE);
                        String token = sharedPreferences.getString("token", null);
//                        String request_token = "Bearer "+token;
                        Log.i("leehj", "token: " + token);

                        if (token != null) { //???????????? ??? ??????
                            Log.i("leehj", "???????????? ??? ????????? ?????????. ?????? ???????????? ????????? ???????????????.");
                            //????????? ?????? ????????? ??????. 1. u_id list??? ?????? 2. rest api??? ????????? ????????? ?????? *****
                            result_list = new ArrayList<>(2);

                            rest = saveCourse_data.get("rest"); //????????? ??????????????? ????????? ????????? ???
                            cafe = saveCourse_data.get("cafe");

                            result_list.add(0, rest);
                            result_list.add(1, cafe);

                            Log.e("leehj", "result list rest index 1: " + result_list.get(0).toString()); //@@@
                            Log.e("leehj", "result list cafe index 2: " + result_list.get(1).toString()); //@@@
                            //**************************************************************************

//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
                            //????????? ????????? ?????? -- r_id, c_id
//                                sharedPreferences = getSharedPreferences("USER", Activity.MODE_PRIVATE);
//                                String token = "Bearer "+sharedPreferences.getString("token", null);
//                                Log.i("leehj", "token: " + token);
                            String request_token = "Bearer " + token;
                            Log.e("leehj", "\n?????? ?????? ????????? ???????????? =============================");
                            Call<Map<String, String>> saveCourse = RetrofitClient.getApiService().saveCourse(result_list, request_token);
                            saveCourse.enqueue(new Callback<Map<String, String>>() {
                                @Override
                                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                                    if (response.isSuccessful()) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.e("leehj", "????????? ???????????????,,!");
                                                Log.i("leehj", "save course response: " + response.body());
                                                Log.i("leehj", "save course response - flag: " + response.body().get("flag"));
                                                Log.i("leehj", "save course - token: " + request_token);
                                                CODE_flag = response.body().get("flag");

                                                if (CODE_flag.equals("1")) {
                                                    aSwitch.setClickable(true);
                                                    aSwitch.setFocusable(true);
                                                    aSwitch.setChecked(true);
                                                    Toast.makeText(FindActivity.this, "??? ????????? ???????????? ?????? ??????????????? ????", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    aSwitch.setChecked(false);
                                                    aSwitch.setClickable(true);
                                                    aSwitch.setFocusable(true);
                                                }
//                                                    if(response.body() != null) {
//                                                        Map<String, String> result = response.body();
//                                                        saveMyCourse_data.put("r_id", result.get("r_id")); //?????? ????????? ?????? ???????????????
//                                                        saveMyCourse_data.put("c_id", result.get("c_id"));
//                                                    }
                                            }
                                        }, 1000);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                                }
                            });
//                                }
//                            }, 1000 * 2); //2??? ????????? ??? ??? ??????
                        }
                    }
                }
            }); //...?????? ?????? ?????? ?????? ?????????

            //?????? ?????? ?????? (?????????, ?????? ?????? ??????)
            TextView storeInfo = (TextView) dialog_2st.findViewById(R.id.balloon_click_placeInfo);
            storeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog loading_dialog = new Dialog(FindActivity.this);
                    loading_dialog.setContentView(R.layout.find_loading_dialog);
                    loading_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    loading_dialog.setCancelable(false);
                    loading_dialog.show();


//                    dialog_2st.cancel();
                    Document marker_document = (Document) mapPOIItem.getUserObject();
//                    Log.i("leehj", "?????? ?????? ?????? marker document: " + marker_document.toString()); //@@@

                    //?????? ?????? ?????????
                    getLocationInfo_data = new HashMap<>();
//                    getLocationInfo_data.put("place_name", marker_document.getPlaceName());
                    getLocationInfo_data.put("place_url", marker_document.getPlaceUrl());
//                    getLocationInfo_data.put("place_id", marker_document.getId());

                    Log.i("leehj", "?????? ?????? ?????? Map data"); //@@@
//                    Log.i("leehj", "?????? ?????? ?????? place_name: " + getLocationInfo_data.get("place_name")); //@@@
                    Log.i("leehj", "?????? ?????? ?????? place_url: " + getLocationInfo_data.get("place_url")); //@@@
//                    Log.i("leehj", "?????? ?????? ?????? place_id: " + getLocationInfo_data.get("place_id")); //@@@

                    //???????????? ?????? ????????????
                    Call<Map<String, String>> getLocationInfo = RetrofitClient.getApiService().getLocationInfo(getLocationInfo_data);
                    getLocationInfo.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            Map<String, String> result = response.body();
                            Log.e("leehj", "?????? ?????? Map data: " + result.toString());

                            //store info dialog
                            Dialog storeInfo_dialog = new Dialog(FindActivity.this);
                            storeInfo_dialog.setContentView(R.layout.store_info);
                            storeInfo_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            //data =================================================================
                            String placeName = marker_document.getPlaceName();
                            String address = marker_document.getAddressName();
                            String category = marker_document.getCategoryName();
                            String phone = marker_document.getPhone();
                            String storeUrl = marker_document.getPlaceUrl();

                            String url = "http:" + result.get("imgUrl"); //response data img Url ????????????
                            Log.e("leehj", "img url : " + url);
                            String review1 = result.get("review_1");
                            String review2 = result.get("review_2");
                            String review3 = result.get("review_3");
                            String storeTime = result.get("storeTime");
                            String starpoint = result.get("startpoint");

                            //?????? & ?????? ============================================================
//=================================================================================================================================================================================
                            Log.i("leehj", "marker document print : " + marker_document.toString());
                            Log.i("leehj", "response print: " + result.toString());

                            loading_dialog.dismiss();

                            ImageView img = (ImageView) storeInfo_dialog.findViewById(R.id.storeInfo_img);
//                            Glide.with(storeInfo_dialog.getContext()).load(url).into(img);
                            Glide.with(storeInfo_dialog.getContext()).load(url).placeholder(R.drawable.rabbit_and_bear).override(Target.SIZE_ORIGINAL).apply(new RequestOptions().transforms(new CenterCrop(),
                                    new RoundedCorners(25))).into(img);

                            TextView tv_place_name = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_placeName);
                            TextView tv_category = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_cat);
                            TextView tv_address = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_address);
                            TextView tv_tel = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_phone);
                            TextView tv_store_url = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_url);
                            TextView tv_time = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_time);
                            TextView tv_review1 = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_reivew1);
                            TextView tv_review2 = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_reivew2);
                            TextView tv_review3 = (TextView) storeInfo_dialog.findViewById(R.id.storeInfo_tv_reivew3);
                            Button storeInfo_btn_phone = storeInfo_dialog.findViewById(R.id.storeInfo_btn_phone);
                            Button storeInfo_btn_road = storeInfo_dialog.findViewById(R.id.storeInfo_btn_road);

                            tv_place_name.setText(placeName);
                            tv_category.setText(category);
                            tv_address.setText(" " + address);
                            tv_tel.setText(" " + phone);
                            tv_store_url.setText(" " + storeUrl);
                            tv_time.setText(" " + storeTime);
                            tv_review1.setText(review1);
                            tv_review2.setText(review2);
                            tv_review3.setText(review3);

                            //????????? ?????? ?????? ????????? ==================================================
                            ImageButton cancel = (ImageButton) storeInfo_dialog.findViewById(R.id.storeInfo_cancel);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    storeInfo_dialog.dismiss();
                                }
                            });

                            //?????? ?????????
                            storeInfo_btn_phone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent telintent = new Intent(Intent.ACTION_DIAL);
                                    telintent.setData(Uri.parse("tel:" + phone));
                                    startActivity(telintent);
                                }
                            });

                            storeInfo_btn_road.setVisibility(View.GONE);
                            storeInfo_dialog.show();
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {

                        }
                    });
                }
            });
            dialog_2st.show();
        } //...??????, ?????? ?????? ????????? ?????? ????????? else{}

    } //...onCalloutBalloonOfPOIItemTouched()

    //==============================================================================================
    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        double mSearchLng = mapPointGeo.longitude;
        double mSearchLat = mapPointGeo.latitude;
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng), true);
        mapView.removeAllPOIItems();
        mapView.removeAllPolylines();
        aSwitch.setVisibility(View.GONE);
        MapMarker("???????????? ??????", mSearchLng, mSearchLat);
    }

    //?????? ????????? ???????????? =============================================================================
    public void SearchRestaurant(String x, String y, String radius) {
        CODE_1st = false;
        CODE_2nd = true;

        location = new JSONObject();
        try {
            location.put("x", x);
            location.put("y", y);
            location.put("radius", radius);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        arrayList.add(location);
        Call<ArrayList<CategoryResult>> cafe = RetrofitClient.getApiService().rest(arrayList);
        cafe.enqueue(new Callback<ArrayList<CategoryResult>>() {
            @Override
            public void onResponse(Call<ArrayList<CategoryResult>> call, Response<ArrayList<CategoryResult>> response) {

                List<Document> documents = response.body().get(0).getDocuments();//document??? ?????????

                if (documents.size() != 0) {
                    Log.e("leehj", "?????? ????????? ????????? ????????? : " + documents.get(0).toString());
                    for (int i = 0; i < documents.size(); i++) {
                        customMarker(documents.get(i), R.drawable.location_rest);
                    }
                    mapView.setCurrentLocationRadius(Integer.parseInt(radius));
                    mapView.setCurrentLocationRadiusStrokeColor(Color.argb(128, 255, 87, 87));
                    mapView.setCurrentLocationRadiusFillColor(Color.argb(0, 0, 0, 0));
                } else {
                    Toast.makeText(FindActivity.this, "????????? ????????? ???????????? ????????????. ?????? ????????? ??????????????? ????", Toast.LENGTH_LONG).show();
                    CODE_1st = true;
                    CODE_2nd = false;
                    CODE_3rd = false;
                }
            }

            @Override
            public void onFailure(Call<ArrayList<CategoryResult>> call, Throwable t) {
                Log.i("leehj", "rest list data loading failed!! : " + t.getMessage());
            }
        });
    }

    //?????? ????????? ???????????? =============================================================================
    public void SearchCafe(String x, String y, String radius) {
        CODE_1st = false;
        CODE_3rd = true;

        location = new JSONObject();
        try {
            location.put("x", x);
            location.put("y", y);
            location.put("radius", radius);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        arrayList.add(location);
        Call<ArrayList<CategoryResult>> cafe = RetrofitClient.getApiService().cafe(arrayList);
        cafe.enqueue(new Callback<ArrayList<CategoryResult>>() {
            @Override
            public void onResponse(Call<ArrayList<CategoryResult>> call, Response<ArrayList<CategoryResult>> response) {
                List<Document> documents = response.body().get(0).getDocuments();//document??? ?????????

                if (documents.size() != 0) {
                    Log.e("leehj", "?????? ????????? ????????? ????????? : " + documents.get(0).toString());
                    for (int i = 0; i < documents.size(); i++) {
                        customMarker(documents.get(i), R.drawable.location_cafe);
                    }
                    mapView.setCurrentLocationRadius(Integer.parseInt(radius));
                    mapView.setCurrentLocationRadiusStrokeColor(Color.argb(128, 255, 87, 87));
                    mapView.setCurrentLocationRadiusFillColor(Color.argb(0, 0, 0, 0));
                } else {
                    Toast.makeText(FindActivity.this, "????????? ????????? ???????????? ????????????. ?????? ????????? ??????????????? ????", Toast.LENGTH_LONG).show();
                    CODE_1st = true;
                    CODE_2nd = false;
                    CODE_3rd = false;
                }
            }

            @Override
            public void onFailure(Call<ArrayList<CategoryResult>> call, Throwable t) {
                Log.i("leehj", "cafe list data loading failed!! : " + t.getMessage());
            }
        });
    } //...SearchCafe()

    View.OnTouchListener click_switch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            sharedPreferences = getSharedPreferences("USER", Activity.MODE_PRIVATE);
            String token = sharedPreferences.getString("token", null);
            if (token != null) {
                if(aSwitch.isFocusable() == false){
                    Toast.makeText(FindActivity.this, "????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                }else {
                    Log.i("leehj", "token: " + token);
                    aSwitch.setOnCheckedChangeListener(check_switch);
                }
                    return false;
            } else {
                Toast.makeText(FindActivity.this, "????????? ??? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    };


    //save myCourse event
    Switch.OnCheckedChangeListener check_switch = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            sharedPreferences = getSharedPreferences("USER", Activity.MODE_PRIVATE);
            String token = sharedPreferences.getString("token", null);
            String request_token = "Bearer " + token;
//            if(token != null){
//                Log.i("leehj", "token: " + token);

            if (b) { //switch on!!
                if (CODE_flag.equals("0")) {//???????????? ?????? ????????????
                    Log.e("leehj", "????????? on ????????? ????????? ?????? ??????!!");
//                saveMyCourse_data.put("u_id", "leehj");
                    Call<Integer> save_myCourse = RetrofitClient.getApiService().courseDibs(saveMyCourse_data, request_token);
                    save_myCourse.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            Log.i("leehj", "?????? ??? return value??? 1: " + response.body());
                            Toast.makeText(FindActivity.this, "??? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
//                            compoundButton.setClickable(false);
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {

                        }
                    });
                }
            } else { //switch off!!
                CODE_flag = "0";
                Log.e("leehj", "????????? off!!! ????????? ????????? ????????????");
                //??? ?????? ??????
                Call<Integer> save_myCourse = RetrofitClient.getApiService().courseDibs(saveMyCourse_data, request_token);
                save_myCourse.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.i("leehj", "?????? ??? return value??? 1: " + response.body());
                        Toast.makeText(FindActivity.this, "??? ???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {

                    }
                });
            }
        }
    };
}
