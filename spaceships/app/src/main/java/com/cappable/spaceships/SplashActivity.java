package com.cappable.spaceships;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Николай on 28.07.2015.
 */
public class SplashActivity extends Activity {

    String sp_h = "sp_height";
    String sp_w = "sp_width";
    int measuredWidth, measuredHeight;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp  = PreferenceManager.getDefaultSharedPreferences(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!sp.contains(sp_h)) { //works only on the first launch
                    WindowManager w = getWindowManager();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                        Point size = new Point();
                        w.getDefaultDisplay().getSize(size);
                        measuredWidth = size.x;
                        measuredHeight = size.y;
                    } else {
                        Display d = w.getDefaultDisplay();
                        measuredWidth = d.getWidth();
                        measuredHeight = d.getHeight();
                    }
                    sp.edit().putInt(sp_h, measuredHeight).commit();
                    sp.edit().putInt(sp_w, measuredWidth).commit();
//                    Log.d("ohoh", "ahahah H" + measuredHeight);
//                    Log.d("ohoh", "ahahah W" + measuredWidth);
                }
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 2000);
    }
}
