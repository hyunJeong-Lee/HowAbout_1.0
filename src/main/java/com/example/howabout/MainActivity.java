package com.example.howabout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.howabout.Fragment.FragMyAdapter;
import com.example.howabout.functions.HowAboutThere;
import com.example.howabout.popular.PopularActivity;

import me.relex.circleindicator.CircleIndicator3;


public class MainActivity extends AppCompatActivity {

    HowAboutThere FUNC = new HowAboutThere();
    //Button
    Button btn_login;
    ImageButton btn_mypage, btn_mycource;
    ImageButton img_main1, img_main2;
    //login
//    final static int REQUEST_CODE_START_INPUT = 1;
    //viewpager
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;
    private int num_page = 4;
    private CircleIndicator3 mIndicator;
    SharedPreferences preferences;
    TextView helloId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FUNC.sideBar(MainActivity.this); //side bar

        img_main1 = findViewById(R.id.main_img_main1);
        img_main1.setClipToOutline(true);

        img_main2 = findViewById(R.id.main_img_main2);
        img_main2.setClipToOutline(true);

        helloId = findViewById(R.id.helloId);

        btn_login = findViewById(R.id.main_btn_login);
//        btn_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intentl = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intentl);
////                startActivityForResult(intentl, REQUEST_CODE_START_INPUT);
//            }
//        });
        btn_mypage = findViewById(R.id.main_btn_mypage);
//        btn_mypage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intentmp = new Intent(MainActivity.this, MyPageActivity.class);
//                startActivity(intentmp);
//            }
//        });
        btn_mycource = findViewById(R.id.main_btn_mycource);
//        btn_mycource1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intentmc = new Intent(MainActivity.this, MyCourseActivity.class);
//                startActivity(intentmc);
//            }
//        });
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("로그아웃");
//                builder.setMessage("정말로 로그아웃 하시겠습니까?");
//                builder.setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(MainActivity.this, "logout", Toast.LENGTH_SHORT).show();
//                        SharedPreferences.Editor editor = preferences.edit();
//                        String pp1 = preferences.getAll().toString();
//                        Log.i("subin", "onDestroy: " + pp1);
//                        editor.putString("u_id", null);
//                        editor.putString("u_nick", null);
//                        editor.putBoolean("auto_ck", false);
//                        editor.commit();
//                        editor.apply();
//                        btn_login.setVisibility(View.VISIBLE);
//                        btn_mycource1.setVisibility(View.INVISIBLE);
//                        btn_mypage.setVisibility(View.INVISIBLE);
//                        drawerLayout.closeDrawers();
//                        Log.i("subin", "user정보 : " + preferences.getAll());
//                        helloId = findViewById(R.id.helloId);
//                        helloId.setText("저기어때에 오신걸 환영합니다.");
//
//                    }
//                });
//                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.cancel();
//                    }
//                });
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            }
//        });
//        if (u_nick == null || u_id == null) {
//            Log.i("subin", "null 값이 오나요? ");
//        } else {
//            btn_login.setVisibility(View.INVISIBLE);
//            btn_mycource1.setVisibility(View.VISIBLE);
//            btn_mypage.setVisibility(View.VISIBLE);
//            Log.i("subin", "user 정보가 오나요?: " + u_nick);
//            helloId.setText(u_nick + "님, 환영합니다");
//        }
        //ViewPager2
        mPager = findViewById(R.id.main_viewpager);
        //Adapter
        pagerAdapter = new FragMyAdapter(this, num_page);
        mPager.setAdapter(pagerAdapter);
        //Indicator
        mIndicator = findViewById(R.id.main_indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.createIndicators(num_page, 0);
        //ViewPager Setting
        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mPager.setCurrentItem(1000);
        mPager.setOffscreenPageLimit(3);

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (positionOffsetPixels == 0) {
                    mPager.setCurrentItem(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIndicator.animatePageSelected(position % num_page);
            }
        });
        //intent button
        Button btn_popular = findViewById(R.id.main_btn_popular);
        Button btn_find = findViewById(R.id.main_btn_find);

        btn_popular.setClipToOutline(true);
        btn_popular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentpo = new Intent(MainActivity.this, PopularActivity.class);
                startActivity(intentpo);
            }
        });
        btn_find.setClipToOutline(true);
        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentf = new Intent(MainActivity.this, FindActivity.class);
                startActivity(intentf);
            }
        });

        img_main1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "안녕", Toast.LENGTH_SHORT).show();
            }
        });

        img_main2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "하세요", Toast.LENGTH_SHORT).show();
            }
        });
    }
}