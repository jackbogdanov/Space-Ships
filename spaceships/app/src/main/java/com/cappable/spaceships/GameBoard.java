package com.cappable.spaceships;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

/**
 * Created by Николай on 28.07.2015.
 */
public class GameBoard extends SurfaceView implements Runnable{

    String sp_h = "sp_height";
    String sp_w = "sp_width";
    SharedPreferences sp;
    Thread thread, spawnThread;//main thread

    int screenWidth, screenHeight;
    float xSpeed;//for turns
    float ySpeed, yStart;//for start accelerating
    int score_per_frame = 1, score = 0;//y score_per_frame
    int x, y;//x coordinate
    int mapY = 0;//
//    boolean isAccelerating = true;//while rocket starting
    boolean turnLeft = false, turnRight = false;
    Bitmap bitmap, bitmap_no, bitmap_start, background, background_start
            , background_gameplay, background_gameplay_1, bmp_plane_lower,
    bmp_hot_air_baloon, bmp_hot_air_baloon_1, bmp_sattelite,//texture of rocket
    background_gameplay_space, bmp_explotion;
    int rocket_texture_height, rocket_texture_width;
    Canvas canvas;
    Paint rocketPaint = new Paint(), paint = new Paint();
    SurfaceHolder holder;
    Matrix matrix = new Matrix();
    float degree = 0f; //for rocket turns. 0 == up rocket
    float turn_degree = 2f, turn_degree_faster = 6f;
    int[] colors = {R.color.color_first};
    float frames_for_passing_height = 120f;
    float difference_between_spawn_baloons = 70, difference_between_spawn_plane_lower = 150,
            difference_between_spawn_sattelites = 60;

    boolean isRun = true, isOK = false;
    long last_time, new_time, fps = 1000/60, last_time_spawn, new_time_spawn;

    boolean isPause = true, wasFrameUpdate = false;
    boolean isStarting = true;

    //objects to draw
    FlyingObject[] flyingObjects = new FlyingObject[10];
    PlaneLower planeLower = null;//cucurunik

    Random r = new Random();

    //drawing of bg
    Rect rectSrc = new Rect(), rectDst = new Rect(), rectGameplaySpace = new Rect();
    boolean spaceLevel = false;
    int bgLeft, bgBot, bgTop, bgRight, bgSpaceBot, bgSpaceTop;
    float rectY = 0, rectYGameplaySpace = 0;//rectScr y to draw moving bg
    float topSpaceGameplay, startTopSpaceGameplay;
    private boolean f = true;

    boolean isDied = false;
    int frames_for_death = 0;
    float explotionY;

    public GameBoard(Context context) {
        super(context);
        init();
    }

    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.space_ship_1);
        bitmap_no = BitmapFactory.decodeResource(getResources(), R.mipmap.space_ship_1_no);
        background = BitmapFactory.decodeResource(getResources(), R.mipmap.background);
        background_gameplay_1 = BitmapFactory.decodeResource(getResources(), R.mipmap.background_gameplay);
        bmp_plane_lower = BitmapFactory.decodeResource(getResources(), R.mipmap.plane_lower);
        bmp_hot_air_baloon = BitmapFactory.decodeResource(getResources(), R.mipmap.baloon_1);
        bmp_hot_air_baloon_1 = BitmapFactory.decodeResource(getResources(), R.mipmap.baloon_2);
        bmp_sattelite = BitmapFactory.decodeResource(getResources(), R.mipmap.satellite_1                                     );
        bmp_explotion = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion);

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        screenHeight = sp.getInt(sp_h, 1000);
        screenWidth = sp.getInt(sp_w, 1000);
        rocket_texture_height = bitmap.getHeight();
        rocket_texture_width = bitmap.getWidth();

        float scrW = screenWidth;
        float scrH = screenHeight;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            background_gameplay_space = (BitmapDrawable)
//                    getContext().getResources().getDrawable(R.mipmap.background_gameplay_space, null);
//        else background_gameplay_space = (BitmapDrawable) getResources().getDrawable(R.mipmap.background_gameplay_space);
//        try {
//            bgSpaceTop = background_gameplay_space.getIntrinsicHeight() * (-1);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
        background_gameplay_space = BitmapFactory.decodeResource(getResources(), R.mipmap.background_gameplay_space);
        bgSpaceTop = background_gameplay_space.getHeight() * (-1);
        bgSpaceBot = 0;
        rectGameplaySpace.set(0, bgSpaceTop, screenWidth, bgSpaceBot);
//        background_gameplay_space.setTileModeY(Shader.TileMode.REPEAT);
//        background_gameplay_space.setBounds(rectGameplaySpace);

//crop bitmap
//        background_start = Bitmap.createBitmap(background, 0, 0, screenWidth, screenHeight);
        background_start = Bitmap.createScaledBitmap(background, screenWidth, screenHeight, true);
        bitmap_start = Bitmap.createScaledBitmap(bitmap_no, (int) scrW / 12, (int) scrH / 20, true);
        background_gameplay = Bitmap.createBitmap(background_gameplay_1
                , (background_gameplay_1.getWidth() - screenWidth) / 2, 0
                , (background_gameplay_1.getWidth() + screenWidth) / 2, background_gameplay_1.getHeight());

        holder = getHolder();

        x = (screenWidth - bitmap.getWidth()) / 2;//ship x coordinate
        explotionY = y = screenHeight - bitmap.getHeight()
                - getResources().getDimensionPixelSize(R.dimen.mm_ten);

        matrix.setTranslate(x, y);
//        mxSpeed = screenWidth / 50;
        xSpeed = screenWidth / 30f;
        ySpeed = screenHeight / 160f;
        yStart = screenHeight - scrH * 0.244f - bitmap_start.getHeight();

        float blur = getResources().getDimensionPixelSize(R.dimen.mm_blur);
//        rocketPaint.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL));
        rocketPaint.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.INNER));
//        paint.setColor(Color.RED);

        bgLeft = (background_gameplay.getWidth() - screenWidth) / 2;
        bgBot = background_gameplay.getHeight();
        bgTop = bgBot - screenHeight;
        bgRight = (background_gameplay.getWidth() + screenWidth) / 2;

        spawnThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int spawn_baloon = 0, spawn_kukuruznik = 0, spawn_sattelite = 0;
                while(isRun)
                    while(isOK && wasFrameUpdate){
                        wasFrameUpdate = false;
                        last_time_spawn = System.currentTimeMillis();

                        if (!holder.getSurface().isValid()) continue;
                        if (!isPause) {

                            if (score <= 2000000000) {
                                score += score_per_frame;
                                mapY -= score_per_frame;
                            }

                            if (!spaceLevel) {
                                ++spawn_baloon;
                                ++spawn_kukuruznik;

                                if (spawn_baloon % difference_between_spawn_baloons == 0) {
                                    for (int i = 0; i < flyingObjects.length; ++i) {

                                        spawn_baloon = 0;

                                        if (flyingObjects[i] == null) {
                                            int dir = r.nextInt(2);
                                            float xSpeed = 0, x = 0;
                                            switch (dir) {
                                                case 0://right
                                                    x = r.nextInt(screenWidth / 2);
                                                    xSpeed = r.nextInt((int) ((screenWidth - x) / frames_for_passing_height));
                                                    break;
                                                case 1://left
                                                    x = r.nextInt((int) (screenWidth / 2f)) + screenWidth / 2f;
                                                    xSpeed = r.nextInt((int) (x / frames_for_passing_height)) * -1;
                                                    break;
                                            }
                                            flyingObjects[i]
                                                    = new FlyingObject(getContext(), x, 0
                                                    , xSpeed, 0, r.nextBoolean() ? bmp_hot_air_baloon : bmp_hot_air_baloon_1);

                                            i = flyingObjects.length + 1;

                                            difference_between_spawn_baloons
                                                    = (new Random().nextInt((int) (frames_for_passing_height / 6f))
                                                    + frames_for_passing_height * 2 / 6f);
                                        }
                                    }
                                }

                                //-------------------------------kukuruznik------------------------
                                if (spawn_kukuruznik % difference_between_spawn_plane_lower == 0) {
                                    spawn_kukuruznik = 0;

                                    if (planeLower == null) {
//                                    float x = screenWidth /4f * (-1);
                                        float x = screenWidth;
                                        float xSpeed = (r.nextInt((int) (x / frames_for_passing_height) / 2)
                                                + x / frames_for_passing_height / 8) * (-1);
                                        planeLower = new PlaneLower(getContext(), x, 0
                                                , xSpeed, screenHeight / frames_for_passing_height * (-0.54f), bmp_plane_lower);
                                        difference_between_spawn_plane_lower = r.nextInt((int) (frames_for_passing_height / 6f))
                                                + frames_for_passing_height * 3;
                                    }

                                }


                            } else {
                                //------------- space --------------------------------
                                ++spawn_sattelite;

                                if (spawn_sattelite % difference_between_spawn_sattelites == 0) {
                                    for (int i = 0; i < flyingObjects.length; ++i) {

                                        spawn_sattelite = 0;

                                        if (flyingObjects[i] == null) {
                                            int dir = r.nextInt(2);
                                            float xSpeed = 0, x = 0;
                                            switch (dir) {
                                                case 0://right
                                                    x = r.nextInt(screenWidth / 2);
                                                    xSpeed = r.nextInt((int) ((screenWidth - x) / frames_for_passing_height));
                                                    break;
                                                case 1://left
                                                    x = r.nextInt((int) (screenWidth / 2f)) + screenWidth / 2f;
                                                    xSpeed = r.nextInt((int) (x / frames_for_passing_height)) * -1;
                                                    break;
                                            }
                                            flyingObjects[i] = new FlyingObject(getContext(), x, 0, xSpeed, 0, bmp_sattelite);

                                            i = flyingObjects.length + 1;

                                            difference_between_spawn_sattelites
                                                    = (new Random().nextInt((int) (frames_for_passing_height / 6f))
                                                    + frames_for_passing_height * 2 / 6f);
                                        }
                                    }
                                }
                            }

                        }

                        new_time_spawn = System.currentTimeMillis();
                        if(new_time_spawn < last_time_spawn + fps)
                            try {
                                spawnThread.sleep(last_time_spawn + fps - new_time_spawn);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                    }
            }
        });
        spawnThread.start();
    }

    @Override
    public void run() {
        while (isRun) {
            while (isOK) {
                while (isStarting) {
                    if (!holder.getSurface().isValid()) continue;
                    last_time = System.currentTimeMillis();
                    canvas = holder.lockCanvas();

                    if (!isPause)
                        updateStarting();

                    try {
                        //draw background
                        canvas.drawBitmap(background_start, 0, 0, null);
//                drawBG();
                        //draw rocket
                        canvas.drawBitmap(bitmap_start, screenWidth / 2 - bitmap_start.getWidth() / 2, yStart, null);

                        holder.unlockCanvasAndPost(canvas);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    new_time = System.currentTimeMillis();
                    if (new_time < last_time + fps)
                        try {
                            Thread.sleep(last_time + fps - new_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }

                while (!isStarting) {
                    if (!holder.getSurface().isValid()) continue;
                    last_time = System.currentTimeMillis();
                    canvas = holder.lockCanvas();
                    //do drawings
                    //РИСОВАШКИ
                    if (!isPause)
                        update();

                    try {
                        //draw background
                        drawBG();
                        //draw rocket
                        if (!isDied)
                            canvas.drawBitmap(bitmap, matrix, isPause ? rocketPaint : null);
                        else {
                            canvas.drawBitmap(bmp_explotion, x + (bitmap.getWidth() - bmp_explotion.getWidth())/2
                                    , explotionY, null);
                            ++frames_for_death;
                            explotionY += screenHeight/(frames_for_passing_height/2);
                            if (frames_for_death == 60 || explotionY > screenHeight * 12 / 10) {
                                restartGame();
                                isPause = true;
                            }
                        }
                        //draw everything else
                        drawElse();

                        holder.unlockCanvasAndPost(canvas);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    new_time = System.currentTimeMillis();
                    if (new_time < last_time + fps)
                        try {
                            Thread.sleep(last_time + fps - new_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    wasFrameUpdate = true;
                }
            }
        }
    }

    private void drawBG() {
//        canvas.drawColor(getResources().getColor(colors[0]));

        if (spaceLevel) {
//            rectGameplaySpace.set(0, (int) (bgSpaceTop + rectYGameplaySpace)
//                    , screenWidth, (int) (bgSpaceBot + rectYGameplaySpace));
//            canvas.drawBitmap(background_gameplay_space, );

            for (int x = 0, width = screenWidth; x < width; x += background_gameplay_space.getWidth()) {
                for (int y = (int) topSpaceGameplay, height = (int) (screenHeight - topSpaceGameplay);
                     y < height; y += background_gameplay_space.getHeight()) {
                    canvas.drawBitmap(background_gameplay_space, x, y, null);
                }
            }
        }

        if (rectY > bgTop && f) {
            f = false;
            spaceLevel = true;
            startTopSpaceGameplay = topSpaceGameplay = bgTop - rectY - background_gameplay_space.getHeight();
        }
        rectSrc.set(bgLeft
                , (int) (bgTop - rectY)
                , bgRight
                , (int) (bgBot - rectY));
        rectDst.set(0, 0, screenWidth, screenHeight);

        canvas.drawBitmap(background_gameplay, rectSrc, rectDst, null);
    }

    private void update(){
//        y += score_per_frame;
        //update spawn in thread

        for (int i = 0; i < flyingObjects.length; ++i)
            if (flyingObjects[i] != null) {
                flyingObjects[i].update();
                if (flyingObjects[i].x > screenWidth || flyingObjects[i].x + flyingObjects[i].mBitmap.getWidth() < 0
                        || flyingObjects[i].y > screenHeight)
                    flyingObjects[i] = null;
            }

        if (planeLower != null) {
            planeLower.update();
            if (planeLower.x > screenWidth || planeLower.x + planeLower.mBitmap.getWidth() < 0
                    || planeLower.y > screenHeight)
                planeLower = null;
        }

        rectY += screenHeight / 400f;

        if (spaceLevel) {
//            rectYGameplaySpace += screenHeight/600;
            topSpaceGameplay += screenHeight / 400f;
            if (startTopSpaceGameplay <= topSpaceGameplay - background_gameplay_space.getHeight())
                topSpaceGameplay = startTopSpaceGameplay;
        }
        //            restartGame();
//            isPause = true;
        if (!isDied)
            updateTurnRocket();

        updateCollisions();
        //collisions are made in spawn thread
    }

    private void updateCollisions() {
        for (int i = 0; i < flyingObjects.length; ++i) {
            if (flyingObjects[i] != null &&
                    ((((x < flyingObjects[i].x + flyingObjects[i].mBitmap.getWidth() && x > flyingObjects[i].x)
                    || (x + rocket_texture_width > flyingObjects[i].x
                    && x + rocket_texture_width < flyingObjects[i].x + flyingObjects[i].mBitmap.getWidth()))
                    && (y + rocket_texture_height * 0.42 < flyingObjects[i].y + flyingObjects[i].mBitmap.getHeight()
                    && y + rocket_texture_height * 0.81 > flyingObjects[i].y)) ||
                    //upper - wings; lower - body;
                    (((x + rocket_texture_width * 0.34f < flyingObjects[i].x + flyingObjects[i].mBitmap.getWidth()
                            && x + rocket_texture_width * 0.34f > flyingObjects[i].x)
                    || (x + rocket_texture_width * 0.67f> flyingObjects[i].x
                    && x + rocket_texture_width * 0.67f < flyingObjects[i].x + flyingObjects[i].mBitmap.getWidth()))
                    && (y < flyingObjects[i].y + flyingObjects[i].mBitmap.getHeight()
                    && y + rocket_texture_height * 0.76 > flyingObjects[i].y)))
                    ) {
                onDie();
            }
        }
        //rect Wings, l,t,r,b
        //0, 0,42 * bmp.height,bmp.width, bmp.height * 0,81,
        //x + 0, y + 0,42 * bmp.height,x + bmp.width, y + bmp.height * 0,81,
        //rect main rocket
        //bmp.width * 0,34, 0, bmp.width * 0,67, bmp.height * 0,76
        //x + bmp.width * 0,34, y + 0, x + bmp.width * 0,67, y + bmp.height * 0,76

    }

    private void onDie() {
        isDied = true;
    }


    private void updateStarting() {//upd method for starting
        yStart -= ySpeed;
        if (yStart < screenHeight * 9 / 16)
            isStarting = false;

    }

    private void drawElse() {
        for (int i = 0; i < flyingObjects.length; ++i) {
            if (flyingObjects[i] != null)
                flyingObjects[i].draw(canvas);
        }

        if (planeLower != null)
            planeLower.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xTouch = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                turnRocket(xTouch);
                break;
            case MotionEvent.ACTION_MOVE:
                turnRocket(xTouch);
                break;
            case MotionEvent.ACTION_UP:
                turnLeft = false;
                turnRight = false;
                break;
        }
//        return super.onTouchEvent(event);
        return true;
    }

    private void turnRocket(float xTouch) {
        if (xTouch > screenWidth / 2) {//touch on right
            turnRight = true;
            turnLeft = false;
//            if (x < screenWidth - bitmap.getWidth() - mxSpeed) {
//                turnRight = true;
//                turnLeft = false;
//            } else
//                turnLeft = turnRight = false;
        } else {//left
            turnLeft = true;
            turnRight = false;
//            turnRight = false;
//            turnLeft = true;
//        } else
//            turnLeft = turnRight = false;
        }
    }

    private void updateTurnRocket(){
        if (turnLeft && x > xSpeed) {
            if (degree >= -45) {
                if (degree > 0) {//if you need to turn to left after right turn
                    degree -= 6;
                    matrix.postRotate(-turn_degree_faster, x + bitmap.getWidth() / 2, y + bitmap.getHeight() / 2);
                } else {//normal
                    degree -= 2;
                    matrix.postRotate(-turn_degree, x + bitmap.getWidth() / 2, y + bitmap.getHeight() / 2);
                }
            }
            x -= xSpeed;
            matrix.postTranslate(-xSpeed, 0);
        } else if (turnRight && x  < screenWidth - bitmap.getWidth() - xSpeed) {
            if (degree < 45) {
                if (degree < 0) {//if you have to turn to right after left turn
                    degree += 6;
                    matrix.postRotate(turn_degree_faster, x + bitmap.getWidth() / 2, y + bitmap.getHeight() / 2);
                } else {//normal
                    degree += 2;
                    matrix.postRotate(turn_degree, x + bitmap.getWidth() / 2, y + bitmap.getHeight() / 2);
                }
            }
            x += xSpeed;
            matrix.postTranslate(xSpeed, 0);
        } else {
            if (degree < 0) {
                matrix.postRotate(turn_degree, x + bitmap.getWidth() / 2, y + bitmap.getHeight() / 2);
                degree += 2;
            } else if (degree > 0) {
                matrix.postRotate(-turn_degree, x + bitmap.getWidth() / 2, y + bitmap.getHeight() / 2);
                degree -= 2;
            }
        }
    }

    public void restartGame(){
        isStarting = true;
        x = (screenWidth - bitmap.getWidth()) / 2;//ship x coordinate
        matrix = new Matrix();
        matrix.setTranslate(x, y);
        xSpeed = screenWidth / 36f;
        ySpeed = screenHeight / 40f;
        yStart = screenHeight - screenHeight * 0.244f - bitmap_start.getHeight();
        score = 0;
        degree = 0f;
        for (int i = 0; i < flyingObjects.length; ++i)
            flyingObjects[i] = null;
        planeLower = null;
        rectY = 0;
        f = true;
        spaceLevel = false;
        isDied = false;
        frames_for_death = 0;
        explotionY = y;
    }

    public void resume(){
        thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        isOK = false;
        bitmap = bitmap_no = bitmap_start = background = background_start = null;
        thread = null;
    }

    public void startGame() {
        isOK = true;
    }

    class FlyingObject extends View{
        float x, y;
        float mxSpeed, mySpeed;
        Paint p = new Paint();
        Bitmap mBitmap;

        public FlyingObject(Context context, float x, float yLower, float xSpeed, float ySpeed, Bitmap bitmap){
            //bitmap init
            super(context);
            this.x = x;
            this.y = yLower;
            this.mxSpeed = xSpeed;
            this.mySpeed = ySpeed;
            mBitmap = bitmap;
            y -= mBitmap.getHeight();
//            p.setColor(Color.RED);
        }

        @Override
        public void draw(Canvas canvas) {
//            super.onDraw(canvas);
//            canvas.drawRect(x, y, x + 200, y + 300, p);
            if ((x < screenWidth && x + mBitmap.getWidth() > 0)
                    && (y < screenHeight && y + screenHeight > 0))
                canvas.drawBitmap(mBitmap, x, y, null);
        }

        public void update(){
            x += mxSpeed;
            y += mySpeed + screenHeight/frames_for_passing_height;
        }
    }

    class HotAirBaloon extends FlyingObject{
        public HotAirBaloon(Context context, float x, float yLower, float xSpeed, float ySpeed, Bitmap bitmap) {
            super(context, x, yLower, xSpeed, ySpeed, bitmap);
        }
    }

    class PlaneLower extends FlyingObject{
        public PlaneLower(Context context, float x, float yLower, float xSpeed, float ySpeed, Bitmap bitmap) {
            super(context, x, yLower, xSpeed, ySpeed, bitmap);
        }
    }
}
