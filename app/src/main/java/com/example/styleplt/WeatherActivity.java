package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.styleplt.adapter.bottomAdapter;
import com.example.styleplt.adapter.outerAdapter;
import com.example.styleplt.adapter.topAdapter;
import com.example.styleplt.models.Bottom;
import com.example.styleplt.models.Outer;
import com.example.styleplt.models.Top;
import com.example.styleplt.utility.GpsTracker;
import com.example.styleplt.utility.WeatherData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class WeatherActivity extends AppCompatActivity {

    // GPS 사용을 위한 선언
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private TextView test_gps;
    private TextView tv_current_temperature, tv_wind, tv_cloud, tv_rain, tv_humidity, tv_total_weather, tv_weather_comment;

    // 리사이클러뷰 선언
    private RecyclerView mOuterRecyclerview;
    private RecyclerView mTopRecyclerview;
    private RecyclerView mBottomRecyclerview;

    // 리사이클러뷰 어댑터 연결
    private outerAdapter outerAdapter;
    private topAdapter topAdapter;
    private bottomAdapter bottomAdapter;

    // 리사이클러뷰 데이터 리스트 연결
    private List<Outer> outerDatas;
    private List<Top> topDatas;
    private List<Bottom> bottomDatas;

    // 좌표값, 날짜, 일출시간, 일몰시간
    private String x = "", y = "", address = "";
    private String date = "", time = "";
    private int sunrise = 800;
    private int sunset = 1800;

    private String weather = "";
    private ImageView iv_weather_back;
    private ImageView weather_image;

    // 날씨에 따른 이미지뷰 선언
    private BitmapDrawable iv_sun, iv_sun_cloud, iv_night, iv_night_cloud, iv_cloud, iv_rain, iv_snow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // 네트워크를 별도의 스레드 구현 없이 사용 -> 네트워크작업을 메인 쓰레드에서 동작 = 네트워크 부하가 많을시 속도가 느려질 수 있음
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        tv_rain = findViewById(R.id.tv_rain);
        tv_wind = findViewById(R.id.tv_wind);
        tv_cloud = findViewById(R.id.tv_cloud);
        tv_total_weather = findViewById(R.id.tv_total_weather);
        tv_current_temperature = findViewById(R.id.tv_current_temperature);
        weather_image = findViewById(R.id.weather_image);
        tv_total_weather = findViewById(R.id.tv_total_weather);
        tv_humidity = findViewById(R.id.tv_humidity);

        // 이미지 경로
        iv_sun = (BitmapDrawable)getResources().getDrawable(R.drawable.sun);
        iv_sun_cloud = (BitmapDrawable)getResources().getDrawable(R.drawable.sun_cloud);
        iv_night = (BitmapDrawable)getResources().getDrawable(R.drawable.night);
        iv_night_cloud = (BitmapDrawable)getResources().getDrawable(R.drawable.night_cloud);
        iv_cloud = (BitmapDrawable)getResources().getDrawable(R.drawable.cloud);
        iv_rain = (BitmapDrawable)getResources().getDrawable(R.drawable.rain);
        iv_snow = (BitmapDrawable)getResources().getDrawable(R.drawable.snow);


        test_gps = findViewById(R.id.test_gps);
        iv_weather_back = findViewById(R.id.iv_weather_back);

        iv_weather_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        //GPS 사용 구문
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }
        else {
            checkRunTimePermission();
        }

        gpsTracker = new GpsTracker(WeatherActivity.this);

        double latitude = gpsTracker.getLatitude();  // x좌표
        double longitude = gpsTracker.getLongitude();// y좌표

        String address = getCurrentAddress(latitude, longitude);
        String[] local = address.split(" ");
        // 대한민국 전라북도 군산시 나운2동 -> 나운2동으로 지역 설정
        String localName = local[3];
        String currentaddress = local[1] + " " +  local[2] + " " + local[3];

        test_gps.setText(currentaddress);
        Toast.makeText(WeatherActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

        // assests에 저장된 local_name.xls에서 local[3]에 저장된 ( 나운2동 ) 데이터의 격자x,y 가져오기
        readExcel(localName);

        long now = System.currentTimeMillis();
        Date mDate = new Date(now);

        // 날짜, 시간의 형식 설정
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");

        // 현재 날짜를 받아오는 형식 설정 ex) 20221121
        String getDate = simpleDateFormat1.format(mDate);
        // 현재 시간를 받아오는 형식 설정, 시간만 가져오고 WeatherData의 timechange()를 사용하기 위해 시간만 가져오고 뒤에 00을 붙임 ex) 02 + "00"
        String getTime = simpleDateFormat2.format(mDate) + "00";
        String CurrentTime = simpleDateFormat2.format(mDate) + ":00";
        Log.d("date", getDate + getTime);
        // 현재 월 가져오기 봄 = 3월 ~ 5월 / 여름 = 6월 ~ 8월 / 가을 = 9월, 10월 / 겨울 = 11월 ~ 2월
        String getSeason = simpleDateFormat.format(mDate);

        // WeatherData에서 return한 데이터를 가져오는 구문
        WeatherData wd = new WeatherData();
        try {
            date = getDate;  // 새벽이 되면 하루가 지나 2시가 되기 전의 데이터를 써야되는데 날짜가 바뀌어서 그러지못해서 임시로 전날로 정해둠
            time = getTime;
            weather = wd.lookUpWeather(date, time, x, y);
        } catch (IOException e) {
            Log.i("THREE_ERROR1", e.getMessage());
        } catch (JSONException e) {
            Log.i("THREE_ERROR2", e.getMessage());
        }
        Log.d("현재날씨",weather);

        // return한 값을 " " 기준으로 자른 후 배열에 추가
        // array[0] = 구름의 양, array[1] = 강수 확률, array[2] = 기온, array[3] = 풍속, array[4] = 적설량, array[5] = 습도
        String[] weatherarray = weather.split(" ");
        for(int i = 0; i < weatherarray.length; i++) {
            Log.d("weather = ", i + " " + weatherarray[i]);
        }

        tv_cloud.setText("구름 양 : " + weatherarray[0]);
        tv_rain.setText("강수 확률 : " + weatherarray[1]);
        tv_current_temperature.setText(weatherarray[2]);
        tv_wind.setText("풍속 : " + weatherarray[3]);
        tv_humidity.setText("습도 : " + weatherarray[5]);


        tv_total_weather.setText("기준시간 " + CurrentTime);

        // 날씨에 따른 이미지 세팅
       if(sunrise <= Integer.valueOf(time) && Integer.valueOf(time) < sunset) {
            if((weatherarray[0].equals("맑음")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_sun);
            }
            else if((weatherarray[0].equals("비")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_rain);
            }
            else if((weatherarray[0].equals("구름많음")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_sun_cloud);
            }
            else if((weatherarray[0].equals("흐림")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_cloud);
            }
            else if(weatherarray[4] != "적설없음") {
                weather_image.setImageDrawable(iv_snow);
            }
        }
        else {
            if ((weatherarray[0].equals("맑음")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_night);
            } else if ((weatherarray[0].equals("비")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_rain);
            } else if ((weatherarray[0].equals("구름많음")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_night_cloud);
            } else if ((weatherarray[0].equals("흐림")) && (weatherarray[4].equals("적설없음"))) {
                weather_image.setImageDrawable(iv_cloud);
            } else if (weatherarray[4] != "적설없음") {
                weather_image.setImageDrawable(iv_snow);
            }
        }

        settingImage(getSeason, getTime, weatherarray[2], weatherarray[3]);

    }

    public void settingImage(String month, String time, String temperature, String wind) {
        String getMonth = month;
        String getTime = time;
        String getTemperature = temperature;
        String getWind = wind;

        // 각각의 계절의 밤,낮에 맞는 코멘트
        String spring = "추위가 완전히 가신 봄날씨이므로 상의는 맨투맨이나 셔츠를 하의는 슬렉스나 청바지를 입는것을 추천합니다.";
        String spring_night = "봄이지만 밤에는 비교적 쌀쌀할 수 있어 외투를 걸치는 것을 추천합니다.";
        String summer = "한여름의 더위를 주의하시기 바라며 자외선 차단제를 바르고 시원한 옷감의 반팔이나 반바자리를 입는 것을 추천합니다.";
        String summer_night = "밤에도 온도가 많이 떨어지지 않아 시원하게 입는것을 추천드리며 추위를 잘탄다면 가벼운 외투를 챙기는 것을 추천합니다.";
        String autumn = "시원한 가을날씨로 바람이 불면 다소 추울 수 있어\n 니트나 후드집업, 코트를 입는것을 추천합니다.";
        String autumn_night = "밤이 되면 쌀쌀하기 때문에 비교적 두꺼운 재질의 옷을 챙겨\n 따뜻하게 입는 것을 추천합니다.";
        String winter = "온도가 급격히 떨어지고 바람이 불어 춥기 때문에 후리스같은 따뜻한 옷감의 아우터나 패딩을 입는것을 추천합니다.";
        String winter_night = "한겨울의 밤은 해가 떨어져 체감온도가 낮아지기 때문에 낮보다 더 따뜻하게 옷을 입는 것을 추천합니다.";

        // 리사이클러뷰를 가로로 설정
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        // 코멘트 텍스트뷰 연결
        tv_weather_comment = findViewById(R.id.tv_weather_comment);

        // 아우터 리사이클러뷰 연결
        mOuterRecyclerview = findViewById(R.id.outer_Recyclerview);
        mOuterRecyclerview.setLayoutManager(layoutManager1);
        outerDatas = new ArrayList<>();

        // 상의 리사이클러뷰 연결
        mTopRecyclerview = findViewById(R.id.top_Recyclerview);
        mTopRecyclerview.setLayoutManager(layoutManager2);
        topDatas = new ArrayList<>();

        // 하의 리사이클러뷰 연결
        mBottomRecyclerview = findViewById(R.id.bottom_Recyclerview);
        mBottomRecyclerview.setLayoutManager(layoutManager3);
        bottomDatas = new ArrayList<>();

        // 봄 여름 가을 겨울에 맞는 텍스트, 이미지 세팅
        if(getMonth.equals("03") || getMonth.equals("04") || getMonth.equals("05")) {
            if(sunrise <= Integer.valueOf(time) && Integer.valueOf(time) < sunset) { // 봄 낮
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer1.png?alt=media&token=08b9d5a6-73ed-4317-8b23-caa3100f294d","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer1.png?alt=media&token=08b9d5a6-73ed-4317-8b23-caa3100f294d"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer2.png?alt=media&token=dd130239-a2de-494f-a60d-1b4a8255d2360","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer2.png?alt=media&token=dd130239-a2de-494f-a60d-1b4a8255d2360"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer3.png?alt=media&token=83b81358-39d7-4818-a738-c8dcf1b27887","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer3.png?alt=media&token=83b81358-39d7-4818-a738-c8dcf1b27887"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top1.png?alt=media&token=54d19253-9796-453d-a93b-152291919be9","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top1.png?alt=media&token=54d19253-9796-453d-a93b-152291919be9"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top2.png?alt=media&token=41749def-50ad-49a1-9f90-41b41e2ceaeb","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top2.png?alt=media&token=41749def-50ad-49a1-9f90-41b41e2ceaeb"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top3.png?alt=media&token=3f70f434-0b52-44e5-96f8-6e05e699be53","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top3.png?alt=media&token=3f70f434-0b52-44e5-96f8-6e05e699be53"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a"));
                tv_weather_comment.setText(spring);
            }
            else {// 봄 밤
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer1.png?alt=media&token=08b9d5a6-73ed-4317-8b23-caa3100f294d","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer1.png?alt=media&token=08b9d5a6-73ed-4317-8b23-caa3100f294d"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer2.png?alt=media&token=dd130239-a2de-494f-a60d-1b4a8255d2360","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer2.png?alt=media&token=dd130239-a2de-494f-a60d-1b4a8255d2360"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer3.png?alt=media&token=83b81358-39d7-4818-a738-c8dcf1b27887","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_outer3.png?alt=media&token=83b81358-39d7-4818-a738-c8dcf1b27887"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top1.png?alt=media&token=54d19253-9796-453d-a93b-152291919be9","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top1.png?alt=media&token=54d19253-9796-453d-a93b-152291919be9"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top2.png?alt=media&token=41749def-50ad-49a1-9f90-41b41e2ceaeb","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top2.png?alt=media&token=41749def-50ad-49a1-9f90-41b41e2ceaeb"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top3.png?alt=media&token=3f70f434-0b52-44e5-96f8-6e05e699be53","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_top3.png?alt=media&token=3f70f434-0b52-44e5-96f8-6e05e699be53"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a"));
                tv_weather_comment.setText(spring_night);
            }
        }
        else if (getMonth.equals("06") || getMonth.equals("07") || getMonth.equals("08")) {
            if(sunrise <= Integer.valueOf(time) && Integer.valueOf(time) < sunset) { // 여름 낮
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_outer1.png?alt=media&token=59a6df40-0cc1-4824-b4d3-2d424d5a121a", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_outer1.png?alt=media&token=59a6df40-0cc1-4824-b4d3-2d424d5a121a"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top1.png?alt=media&token=ea51f24f-cd8f-4dc1-9e7b-ef9ba4bf1352", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top1.png?alt=media&token=ea51f24f-cd8f-4dc1-9e7b-ef9ba4bf1352"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top2.png?alt=media&token=40f68e3c-891f-4670-9209-83eed7ba200e", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top2.png?alt=media&token=40f68e3c-891f-4670-9209-83eed7ba200e"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top3.png?alt=media&token=e136d9ff-fb7a-44eb-8288-bebe31e5a409", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top3.png?alt=media&token=e136d9ff-fb7a-44eb-8288-bebe31e5a409"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom1.png?alt=media&token=c6e6de21-3d42-4781-bbad-78541a9171fb", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom1.png?alt=media&token=c6e6de21-3d42-4781-bbad-78541a9171fb"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom2.png?alt=media&token=1a58016c-f0ae-4b2c-81dd-ce3ab836c2f8", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom2.png?alt=media&token=1a58016c-f0ae-4b2c-81dd-ce3ab836c2f8"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom3.png?alt=media&token=9e908b78-5df5-430b-88f5-19c99ba2bae5", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom3.png?alt=media&token=9e908b78-5df5-430b-88f5-19c99ba2bae5"));
                tv_weather_comment.setText(summer);
            }
            else { // 여름 밤
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_outer1.png?alt=media&token=59a6df40-0cc1-4824-b4d3-2d424d5a121a", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_outer1.png?alt=media&token=59a6df40-0cc1-4824-b4d3-2d424d5a121a"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top1.png?alt=media&token=ea51f24f-cd8f-4dc1-9e7b-ef9ba4bf1352", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top1.png?alt=media&token=ea51f24f-cd8f-4dc1-9e7b-ef9ba4bf1352"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top2.png?alt=media&token=40f68e3c-891f-4670-9209-83eed7ba200e", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top2.png?alt=media&token=40f68e3c-891f-4670-9209-83eed7ba200e"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top3.png?alt=media&token=e136d9ff-fb7a-44eb-8288-bebe31e5a409", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_top3.png?alt=media&token=e136d9ff-fb7a-44eb-8288-bebe31e5a409"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom1.png?alt=media&token=c6e6de21-3d42-4781-bbad-78541a9171fb", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom1.png?alt=media&token=c6e6de21-3d42-4781-bbad-78541a9171fb"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom2.png?alt=media&token=1a58016c-f0ae-4b2c-81dd-ce3ab836c2f8", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom2.png?alt=media&token=1a58016c-f0ae-4b2c-81dd-ce3ab836c2f8"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom3.png?alt=media&token=9e908b78-5df5-430b-88f5-19c99ba2bae5", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fsummer%2Fsummer_bottom3.png?alt=media&token=9e908b78-5df5-430b-88f5-19c99ba2bae5"));
                tv_weather_comment.setText(summer_night);
            }
        }
        else if (getMonth.equals("09") || getMonth.equals("10") || getMonth.equals("11")) {
            if(sunrise <= Integer.valueOf(time) && Integer.valueOf(time) < sunset) { // 가을 낮
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer1.png?alt=media&token=203a4865-47dc-4665-9bb5-6c56bbfe697a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer1.png?alt=media&token=203a4865-47dc-4665-9bb5-6c56bbfe697a"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer2.png?alt=media&token=8d9a7128-ba13-4143-8013-8bd3ea42c52e", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer2.png?alt=media&token=8d9a7128-ba13-4143-8013-8bd3ea42c52e"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer3.png?alt=media&token=6afe9c7a-38b7-4ae1-b767-6d657ad55a91","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer3.png?alt=media&token=6afe9c7a-38b7-4ae1-b767-6d657ad55a91"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top2.png?alt=media&token=fb84b99c-071e-4037-992b-8b08e313f22a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top2.png?alt=media&token=fb84b99c-071e-4037-992b-8b08e313f22a"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top3.png?alt=media&token=4ad7479a-9d00-4425-8820-6fcf32840ac6","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top3.png?alt=media&token=4ad7479a-9d00-4425-8820-6fcf32840ac6"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top4.png?alt=media&token=6d5dcf2c-0d86-407d-81b2-e49a6bb5bb5f","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top4.png?alt=media&token=6d5dcf2c-0d86-407d-81b2-e49a6bb5bb5f"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom1.png?alt=media&token=3bba8eab-5b05-4c06-b33a-2931ebba3ba2","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom1.png?alt=media&token=3bba8eab-5b05-4c06-b33a-2931ebba3ba2"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom2.png?alt=media&token=96456e8e-d774-4cd9-97fa-cee437d95834","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom2.png?alt=media&token=96456e8e-d774-4cd9-97fa-cee437d95834"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom3.png?alt=media&token=cb663cef-aac8-43c3-aa71-8dc1eb25ec58","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom3.png?alt=media&token=cb663cef-aac8-43c3-aa71-8dc1eb25ec58"));
                tv_weather_comment.setText(autumn);
            }
            else { // 가을 밤
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer1.png?alt=media&token=203a4865-47dc-4665-9bb5-6c56bbfe697a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer1.png?alt=media&token=203a4865-47dc-4665-9bb5-6c56bbfe697a"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer2.png?alt=media&token=8d9a7128-ba13-4143-8013-8bd3ea42c52e", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer2.png?alt=media&token=8d9a7128-ba13-4143-8013-8bd3ea42c52e"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer3.png?alt=media&token=6afe9c7a-38b7-4ae1-b767-6d657ad55a91","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_outer3.png?alt=media&token=6afe9c7a-38b7-4ae1-b767-6d657ad55a91"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top2.png?alt=media&token=fb84b99c-071e-4037-992b-8b08e313f22a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top2.png?alt=media&token=fb84b99c-071e-4037-992b-8b08e313f22a"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top3.png?alt=media&token=4ad7479a-9d00-4425-8820-6fcf32840ac6","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top3.png?alt=media&token=4ad7479a-9d00-4425-8820-6fcf32840ac6"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top4.png?alt=media&token=6d5dcf2c-0d86-407d-81b2-e49a6bb5bb5f","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_top4.png?alt=media&token=6d5dcf2c-0d86-407d-81b2-e49a6bb5bb5f"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom1.png?alt=media&token=3bba8eab-5b05-4c06-b33a-2931ebba3ba2","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom1.png?alt=media&token=3bba8eab-5b05-4c06-b33a-2931ebba3ba2"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom2.png?alt=media&token=96456e8e-d774-4cd9-97fa-cee437d95834","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom2.png?alt=media&token=96456e8e-d774-4cd9-97fa-cee437d95834"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom3.png?alt=media&token=cb663cef-aac8-43c3-aa71-8dc1eb25ec58","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fautumn%2Fautumn_bottom3.png?alt=media&token=cb663cef-aac8-43c3-aa71-8dc1eb25ec58"));
                tv_weather_comment.setText(autumn_night);
            }
        }
        else if (getMonth.equals("12") || getMonth.equals("01") || getMonth.equals("02")) {
            if(sunrise <= Integer.valueOf(time) && Integer.valueOf(time) < sunset) { // 겨울 낮
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom1.png?alt=media&token=2c4682b8-0604-4108-9677-1ea2de0812a6", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom1.png?alt=media&token=2c4682b8-0604-4108-9677-1ea2de0812a6"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom2.png?alt=media&token=906e4b19-8f16-44de-ac6d-6704a600da5a", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom2.png?alt=media&token=906e4b19-8f16-44de-ac6d-6704a600da5a"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom3.png?alt=media&token=d33a3a37-1d7d-48b8-9fa0-bbad2163f7fe", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom3.png?alt=media&token=d33a3a37-1d7d-48b8-9fa0-bbad2163f7fe"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top1.png?alt=media&token=4feb008f-bfe3-4279-ad58-e7262c6e5587", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top1.png?alt=media&token=4feb008f-bfe3-4279-ad58-e7262c6e5587"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top2.png?alt=media&token=bae745b7-46b0-48e9-a73b-0411993e1da2", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top2.png?alt=media&token=bae745b7-46b0-48e9-a73b-0411993e1da2"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top3.png?alt=media&token=679f433e-daeb-4e55-8e7a-4ecd099c8c8f", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top3.png?alt=media&token=679f433e-daeb-4e55-8e7a-4ecd099c8c8f"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a"));
                tv_weather_comment.setText(winter);
            }
            else { // 겨울 밤
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom1.png?alt=media&token=2c4682b8-0604-4108-9677-1ea2de0812a6", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom1.png?alt=media&token=2c4682b8-0604-4108-9677-1ea2de0812a6"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom2.png?alt=media&token=906e4b19-8f16-44de-ac6d-6704a600da5a", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom2.png?alt=media&token=906e4b19-8f16-44de-ac6d-6704a600da5a"));
                outerDatas.add(new Outer("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom3.png?alt=media&token=d33a3a37-1d7d-48b8-9fa0-bbad2163f7fe", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_bottom3.png?alt=media&token=d33a3a37-1d7d-48b8-9fa0-bbad2163f7fe"));

                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top1.png?alt=media&token=4feb008f-bfe3-4279-ad58-e7262c6e5587", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top1.png?alt=media&token=4feb008f-bfe3-4279-ad58-e7262c6e5587"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top2.png?alt=media&token=bae745b7-46b0-48e9-a73b-0411993e1da2", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top2.png?alt=media&token=bae745b7-46b0-48e9-a73b-0411993e1da2"));
                topDatas.add(new Top("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top3.png?alt=media&token=679f433e-daeb-4e55-8e7a-4ecd099c8c8f", "https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fwinter%2Fwinter_top3.png?alt=media&token=679f433e-daeb-4e55-8e7a-4ecd099c8c8f"));

                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom1.png?alt=media&token=ddb693ee-04a9-4d66-830e-8d2a295d79c2"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom2.png?alt=media&token=a0ba76ba-5673-4405-a32d-b8a0d0ef57f5"));
                bottomDatas.add(new Bottom("https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a","https://firebasestorage.googleapis.com/v0/b/styleplt.appspot.com/o/clothes%2Fspring%2Fspring_bottom3.png?alt=media&token=154aec84-2caf-4cb9-878b-7c58ebe0c86a"));
                tv_weather_comment.setText(winter_night);
            }
        }

        // 리사이클러뷰 + 어댑터 연결
        outerAdapter = new outerAdapter(outerDatas);
        mOuterRecyclerview.setAdapter(outerAdapter);

        topAdapter = new topAdapter(topDatas);
        mTopRecyclerview.setAdapter(topAdapter);

        bottomAdapter = new bottomAdapter(bottomDatas);
        mBottomRecyclerview.setAdapter(bottomAdapter);


    }

    // 저장한 엑셀파일 읽어오기
    public void readExcel(String localName) {
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String contents = sheet.getCell(0, row).getContents();
                        if (contents.contains(localName)) {
                            x = sheet.getCell(1, row).getContents();
                            y = sheet.getCell(2, row).getContents();
                            row = rowTotal;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        // x, y = String형 전역변수
        Log.i("격자값", "x = " + x + "  y = " + y);
    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료
                // 2 가지 경우
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(WeatherActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(WeatherActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(WeatherActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(WeatherActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(WeatherActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(WeatherActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(WeatherActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(WeatherActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public class weatherViewHolder {
    }
}
