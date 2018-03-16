package com.cappable.spaceships;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements View.OnClickListener{
    Button btnSound, btnSettings, btnScores, btnShop;
    LinearLayout menuLayout;
    GameBoard gb;
    int skin;//for skin of rocket
    boolean gamePlay;
    Animation menuGoUp, menuGoDown, alphaToMin, alphaToMax;
    TextView tvScore, tvMoney, tvPressStart;
    ShopFragment shopFragment;
    ScoreFragment scoreFragment;
    SettingsFragment settingsFragment;
    FragmentTransaction ft;

    private boolean isOK = true;//for upd the score
    private boolean isShop = false, isScore = false, isSettings = false;
    private boolean doubleBackPress = false, wasStarted = false;
    private Handler scoreHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ls", "onCreate");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setVisibility(View.GONE);

        gb = (GameBoard) findViewById(R.id.gb);
        btnScores = (Button) findViewById(R.id.btn_scores);
        btnSettings = (Button) findViewById(R.id.btn_settings);
        btnSound = (Button) findViewById(R.id.btn_sound);
        btnShop = (Button) findViewById(R.id.btn_shop);
        menuLayout = (LinearLayout) findViewById(R.id.layout_menu);
        tvScore = (TextView) findViewById(R.id.tv_score);
        tvMoney = (TextView) findViewById(R.id.tv_money);
        tvPressStart = (TextView) findViewById(R.id.tv_press_to_start);
        shopFragment = (ShopFragment) getFragmentManager().findFragmentById(R.id.fragmentShop);
        scoreFragment = (ScoreFragment) getFragmentManager().findFragmentById(R.id.fragmentScore);
        settingsFragment = (SettingsFragment) getFragmentManager().findFragmentById(R.id.fragmentSettings);

        btnShop.setOnClickListener(this);
        btnScores.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        gb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gb.isPause && !(isScore || isShop || isSettings) && event.getAction() == MotionEvent.ACTION_UP) {
                    menuLayout.startAnimation(menuGoUp);
//                    btnPlay.startAnimation(alphaToMin);
                    gb.isPause = false;
                    tvScore.setVisibility(View.VISIBLE);
//                gb.startGame();
//                threadUpdate.start();
                    tvPressStart.setVisibility(View.GONE);
                    if (!wasStarted) {
                        final Timer scoreTimer = new Timer();
                        scoreTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                scoreHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvScore.setText(gb.score + " m");
                                    }
                                });
                            }
                        }, 0L, 1000L / 30);

                        wasStarted = true;
                    }
                }
                return false;
            }
        });

        menuGoUp = AnimationUtils.loadAnimation(this, R.anim.menu_go_up);
        menuGoDown = AnimationUtils.loadAnimation(this, R.anim.menu_go_down);
//        alphaToMin = AnimationUtils.loadAnimation(this, R.anim.alpha_to_min);
//        alphaToMax = AnimationUtils.loadAnimation(this, R.anim.alpha_to_max);

        Animation.AnimationListener menu_out = new Animation.AnimationListener() {//menu going away
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnShop.setVisibility(View.GONE);
                btnScores.setVisibility(View.GONE);
                btnSettings.setVisibility(View.GONE);
                btnSound.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        Animation.AnimationListener menu_in = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                btnShop.setVisibility(View.VISIBLE);
                btnScores.setVisibility(View.VISIBLE);
                btnSettings.setVisibility(View.VISIBLE);
                btnSound.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        menuGoUp.setAnimationListener(menu_out);
        menuGoDown.setAnimationListener(menu_in);
//        alphaToMin.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                btnPlay.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        alphaToMax.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                btnPlay.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });

        //money and score to invisble
        tvScore.setVisibility(View.GONE);
        tvMoney.setVisibility(View.GONE);

        //make fragments of menu invisible
        ft = getFragmentManager().beginTransaction();
        ft.hide(scoreFragment);
        ft.hide(settingsFragment);
        ft.hide(shopFragment).commit();

        //start drawing
        gb.isOK = true;
    }

    @Override
    public void onClick(View v) {
        if (gb.isPause) {
            switch (v.getId()) {
                case R.id.btn_shop:
                    if (isScore) {
                        ft = getFragmentManager().beginTransaction();
                        ft.hide(scoreFragment).commit();
                    } else if (isSettings) {
                        ft = getFragmentManager().beginTransaction();
                        ft.hide(settingsFragment).commit();
                    }
                    isShop = true;
                    isScore = false;
                    isSettings = false;
                    ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    ft.show(shopFragment).commit();
//                    btnPlay.setVisibility(View.GONE);
                    break;
                case R.id.btn_scores:
                    if (isShop) {
                        ft = getFragmentManager().beginTransaction();
                        ft.hide(shopFragment).commit();
                    } else if (isSettings) {
                        ft = getFragmentManager().beginTransaction();
                        ft.hide(settingsFragment).commit();
                    }
                    isScore = true;
                    isShop = false;
                    isSettings = false;
                    ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    ft.show(scoreFragment).commit();
//                    btnPlay.setVisibility(View.GONE);
                    break;
                case R.id.btn_settings:
                    if (isShop) {
                        ft = getFragmentManager().beginTransaction();
                        ft.hide(shopFragment).commit();
                    } else if (isScore) {
                        ft = getFragmentManager().beginTransaction();
                        ft.hide(scoreFragment).commit();
                    }
                    isScore = false;
                    isShop = false;
                    isSettings = true;
                    ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    ft.show(settingsFragment).commit();
//                    btnPlay.setVisibility(View.GONE);
                    break;
            }
            tvPressStart.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (!gb.isPause) {
            gb.isPause = true;
            menuLayout.startAnimation(menuGoDown);
//            tvPressStart.setVisibility(View.VISIBLE);
//            btnPlay.startAnimation(alphaToMax);
        } else if (isShop) {
            ft = getFragmentManager().beginTransaction();
            ft.hide(shopFragment).commit();
            isShop = false;
            tvPressStart.setVisibility(View.VISIBLE);
//            btnPlay.setVisibility(View.VISIBLE);
        } else if (isScore) {
            ft = getFragmentManager().beginTransaction();
            ft.hide(scoreFragment).commit();
            isScore = false;
            tvPressStart.setVisibility(View.VISIBLE);
//            btnPlay.setVisibility(View.VISIBLE);
        } else if (isSettings) {
            ft = getFragmentManager().beginTransaction();
            ft.hide(settingsFragment).commit();
            isSettings = false;
            tvPressStart.setVisibility(View.VISIBLE);
//            btnPlay.setVisibility(View.VISIBLE);
        } else {
            if (doubleBackPress) {
                super.onBackPressed();
                return;
            }
            doubleBackPress = true;
            Toast.makeText(this, "Press again", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackPress = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onResume() {
        Log.d("ls", "onResume");
        super.onResume();
        gb.resume();
        isOK = true;
    }

    @Override
    protected void onStop() {
        isOK = false;
        gb.stop();
        super.onStop();
    }
}
