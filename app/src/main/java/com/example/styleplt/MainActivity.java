package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.styleplt.adapter.ContentsPagerAdapter;
import com.example.styleplt.fragment.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.type.Date;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private TextView test_id, test_pass, test_age;
    private TextView tv_main_title;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    private Button btn_logout;

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private ContentsPagerAdapter mContentPagerAdapter;


    public void wrapTabIndicatorToTitle(TabLayout tabLayout, int externalMargin, int internalMargin) {
        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            int childCount = ((ViewGroup) tabStrip).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View tabView = tabStripGroup.getChildAt(i);
                //set minimum width to 0 for instead for small texts, indicator is not wrapped as expected
                tabView.setMinimumWidth(0);
                // set padding to 0 for wrapping indicator as title
                tabView.setPadding(0, tabView.getPaddingTop(), 0, tabView.getPaddingBottom());
                // setting custom margin between tabs
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
                    if (i == 0) {
                        // left
                        settingMargin(layoutParams, externalMargin, internalMargin);
                    } else if (i == childCount - 1) {
                        // right
                        settingMargin(layoutParams, internalMargin, externalMargin);
                    } else {
                        // internal
                        settingMargin(layoutParams, internalMargin, internalMargin);
                    }
                }
            }

            tabLayout.requestLayout();
        }
    }

    private void settingMargin(ViewGroup.MarginLayoutParams layoutParams, int start, int end) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.setMarginStart(start);
            layoutParams.setMarginEnd(end);
            layoutParams.leftMargin = start;
            layoutParams.rightMargin = end;
        } else {
            layoutParams.leftMargin = start;
            layoutParams.rightMargin = end;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_main_title = findViewById(R.id.tv_main_title);
        btn_logout = findViewById(R.id.btn_logout);
        test_id = findViewById(R.id.test_id);
        test_pass = findViewById(R.id.test_password);
        test_age = findViewById(R.id.test_age);

        //패플 TextView 클릭시 홈으로 이동하게 설정
        tv_main_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(0);
                mTabLayout.getTabAt(0).setIcon(R.drawable.home_hilight);
                mTabLayout.getTabAt(1).setIcon(R.drawable.feed);
                mTabLayout.getTabAt(2).setIcon(R.drawable.profile);            }
        });

        //로그아웃 버튼
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                finish();

                //Intent를 새로 만든 후 LoginActivity로 화면전환
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mTabLayout = findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.home_hilight));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.feed));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.profile));
//        mTabLayout.addTab(mTabLayout.newTab().setText("홈"));

        mViewPager = findViewById(R.id.viewPager);

        //프레그먼트 이동 구현
        ContentsPagerAdapter contentsPagerAdapter = new ContentsPagerAdapter(this);
        mViewPager.setAdapter(contentsPagerAdapter);


        //tabLayout - ViewPager 연결
        new TabLayoutMediator(mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {

                // 각 탭에 이미지 배치
                mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        mViewPager.setCurrentItem(tab.getPosition());
                        switch (tab.getPosition()) {
                            case 0 :
                                mTabLayout.getTabAt(0).setIcon(R.drawable.home_hilight);
                                mTabLayout.getTabAt(1).setIcon(R.drawable.feed);
                                mTabLayout.getTabAt(2).setIcon(R.drawable.profile);
                                break;
                            case 1:
                                mTabLayout.getTabAt(0).setIcon(R.drawable.home);
                                mTabLayout.getTabAt(1).setIcon(R.drawable.feed_hilight);
                                mTabLayout.getTabAt(2).setIcon(R.drawable.profile);
                                break;
                            case  2:
                                mTabLayout.getTabAt(0).setIcon(R.drawable.home);
                                mTabLayout.getTabAt(1).setIcon(R.drawable.feed);
                                mTabLayout.getTabAt(2).setIcon(R.drawable.profile_hilight);
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

            }
        }).attach();

    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            //어플 완전종료
            finishAffinity();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

    }


}
