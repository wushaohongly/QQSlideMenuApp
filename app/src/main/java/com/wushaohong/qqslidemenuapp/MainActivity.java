package com.wushaohong.qqslidemenuapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SlideMenu.OnSlideListener{

    private SlideMenu mSlideMenu;
    private TextView mTitleName;
    private View mViewHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    private void initView() {
        mSlideMenu = findViewById(R.id.sm_main);
        mTitleName = findViewById(R.id.tv_title_name);
        mViewHelp = findViewById(R.id.v_help);
    }

    private void initEvent() {
        mSlideMenu.setOnSlideListener(this);
        mTitleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlideMenu.toggleMenu();
            }
        });
        mViewHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlideMenu.closeMenu();
            }
        });
    }

    @Override
    public void onOpen() {
        mViewHelp.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClose() {
        mViewHelp.setVisibility(View.GONE);
    }

    @Override
    public void onSliding(float fraction) {
        Log.e("my_info", fraction+"");
    }
}
