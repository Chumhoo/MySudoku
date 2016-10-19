package com.example.chumhoo.mysudoku;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.MotionEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.IOException;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener,
        GestureDetector.OnGestureListener {
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;
    MyAbstractRenderer renderer = null;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] sensorStartValue = {0, 0, 0};

    MySurfaceView view;
    GestureDetectorCompat mDetectorCompat;
    NavigationView navigationView;
    DrawerLayout drawer;
    Toolbar toolbar;

    long backPressTime = 0;

    boolean GRAVITY = false;
    boolean PLAYMUSIC = false, PLAYSOUND = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawer.isDrawerOpen(Gravity.LEFT))
                {
                    drawer.openDrawer(Gravity.LEFT);
                }
                vibrate();
            }
        });
        FloatingActionButton refreshBtn = (FloatingActionButton) findViewById(R.id.refersh);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshDialog(renderer);
                vibrate();
            }
        });
        FloatingActionButton help = (FloatingActionButton) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderer.getHelp();
                if (PLAYSOUND)
                {
                    helpM.seekTo(0);
                    helpM.start();
                }
                Toast.makeText(MainActivity.this, "提示不要用太多哟", Toast.LENGTH_SHORT).show();
                vibrate();
            }
        });
        FloatingActionButton redo = (FloatingActionButton) findViewById(R.id.redo);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderer.redo();
                Toast.makeText(MainActivity.this, "重做", Toast.LENGTH_SHORT).show();
                vibrate();
            }
        });
        FloatingActionButton undo = (FloatingActionButton) findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderer.undo();
                Toast.makeText(MainActivity.this, "撤销", Toast.LENGTH_SHORT).show();
                vibrate();
            }
        });
        FloatingActionButton previousStage = (FloatingActionButton) findViewById(R.id.stage_previous);
        previousStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (renderer.stage > 1) {
                    renderer.stage -= 1;
                    newGameStart(renderer);
                }
                Toast.makeText(MainActivity.this, "上一关", Toast.LENGTH_SHORT).show();
                vibrate();
            }
        });
        FloatingActionButton nextStage = (FloatingActionButton) findViewById(R.id.stage_next);
        nextStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (renderer.stage < renderer.MAX_LEVELS) {
                    renderer.stage += 1;
                    newGameStart(renderer);
                }
                Toast.makeText(MainActivity.this, "下一关", Toast.LENGTH_SHORT).show();
                vibrate();
            }
        });

        //set screen: no title、full screen. keep screen on
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //get the screen's width and height
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        int height = metric.heightPixels;

        view = (MySurfaceView) findViewById(R.id.mySurfaceView);
//       view = new MySurfaceView(this);
        renderer = new MyColorCubeRenderer(width, height, this.getResources());
        view.setRenderer(renderer);


        //重力传感器监听
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        if (null == mSensorManager) {
            Log.d(TAG, "deveice not support SensorManager");
        }
        // 参数三,检测的精准度
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);// SENSOR_DELAY_GAME

        //        view.setOnTouchListener(this);
//        mDetectorCompat = new GestureDetectorCompat(this, this);

        musicPrepare();
        newGameStart(renderer);
    }

    //分发ouchEvent，防止抽屉将其拦截并消耗
    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (!drawer.isDrawerOpen(Gravity.LEFT)) this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    //继承了Activity的onTouchEvent方法，直接监听点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当手指按下的时候
                x1 = event.getX();
                y1 = event.getY();
                renderer.setZoomMode(true, x1, y1);
                if (renderer.pickCube(x1, y1)) {
                    if (PLAYSOUND)
                    {
//                        if (pick.isPlaying())
//                        pick.seekTo(0);
                        pick.start();
                    }
                    VibratorUtil.Vibrate(this, 50);   //震动100ms
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //当手指移动的时候
                x2 = event.getX();
                y2 = event.getY();
                renderer.setMouse(x2, y2);
                if (renderer.pickNumber(x2, y2))
                    VibratorUtil.Vibrate(this, 50);   //震动100ms

                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                renderer.setZoomMode(false, event.getX(), event.getY());
                int droppedPitch = renderer.dropCube();
                if (PLAYSOUND && droppedPitch > 0)
                {
                    switch (droppedPitch)
                    {
                        case 1: p1.start(); break;
                        case 2: p2.start(); break;
                        case 3: p3.start(); break;
                        case 4: p4.start(); break;
                        case 5: p5.start(); break;
                        case 6: p6.start(); break;
                        case 7: p7.start(); break;
                        case 8: p8.start(); break;
                        case 9: p9.start(); break;
                    }

                }
                if (renderer.checkPass()) {
                    if (PLAYSOUND) pass.start();
                    passDialog(renderer);
                }
                break;
        }

        // event reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

//        mDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END))
        {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            long currentTime = System.currentTimeMillis();
            if (backPressTime == 0 || currentTime - backPressTime > 1000) Toast.makeText(MainActivity.this, "再按一次退出游戏", Toast.LENGTH_SHORT).show();
            else System.exit(1);
            backPressTime = System.currentTimeMillis();
//            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        VibratorUtil.Vibrate(this, 50);

        int id = item.getItemId();

        if (id == R.id.nav_gravity) {
            if (switch_gravity())
            {
                Toast.makeText(MainActivity.this, "重力感应开启", Toast.LENGTH_SHORT).show();
                item.setTitle("重力感应：开启");
            }
            else
            {
                Toast.makeText(MainActivity.this, "重力感应关闭", Toast.LENGTH_SHORT).show();
                item.setTitle("重力感应：关闭");
            }

        } else if (id == R.id.nav_music) {
           if (PLAYMUSIC)
           {
               PLAYMUSIC = false;
               bkgMusic.pause();
               item.setTitle("音乐：关闭");
           }
           else
           {
               PLAYMUSIC = true;
               bkgMusic.start();
               bkgMusic.setVolume(0.5f, 0.5f);
               item.setTitle("音乐：开启");
           }

        }
        else if (id == R.id.nav_sound) {
           if (PLAYSOUND)
           {
               Toast.makeText(MainActivity.this, "音效关闭", Toast.LENGTH_SHORT).show();
               PLAYSOUND = false;
               item.setTitle("音效：关闭");
           }
           else
           {
               Toast.makeText(MainActivity.this, "音效开启", Toast.LENGTH_SHORT).show();
               PLAYSOUND = true;
               item.setTitle("音效：开启");
           }

       }
        else if (id == R.id.nav_chocolate) {
           renderer.theme = 0;
        }
        else if (id == R.id.nav_ice) {
           renderer.theme = 1;
        }
       else if (id == R.id.nav_help) {
           showHelp(R.drawable.help_info);
       }
       else if (id == R.id.nav_about) {
            showHelp(R.drawable.about);
       }

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!GRAVITY) return;
        if (event.sensor == null) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (sensorStartValue[0] == 0 && sensorStartValue[1] == 0 && sensorStartValue[2] == 0) {
                sensorStartValue[0] = event.values[0];
                sensorStartValue[1] = event.values[1];
                sensorStartValue[2] = event.values[2];
                return;
            }
            float x = event.values[0] - sensorStartValue[0];
            float y = event.values[1] - sensorStartValue[1];
            float z = event.values[2] - sensorStartValue[2];
            renderer.eyeMove(0.05f * (x), 0.05f * y, 0.01f, false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public void vibrate()
    {
        VibratorUtil.Vibrate(this, 50);   //震动100ms
    }

    private void refreshDialog(final MyAbstractRenderer _renderer){
        //先new出一个监听器，设置好监听
        DialogInterface.OnClickListener refreshdialogOnclicListener=new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case Dialog.BUTTON_POSITIVE:
                        newGameStart(_renderer);
                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        break;
                    case Dialog.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("确认刷新谜题?"); //设置内容
        builder.setPositiveButton("是的",refreshdialogOnclicListener);
        builder.setNegativeButton("算了", refreshdialogOnclicListener);
//        builder.setNeutralButton("忽略", dialogOnclicListener);
        builder.create().show();

    }

    private void passDialog(final MyAbstractRenderer _renderer){
        //先new出一个监听器，设置好监听
        DialogInterface.OnClickListener passdialogOnclicListener=new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case Dialog.BUTTON_POSITIVE:
                        newGameStart(_renderer);
                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("恭喜通关！"); //设置标题
        builder.setMessage("本轮提示数：" + renderer.hintNumber + "\n" +
                "本轮用时：" + (_renderer.roundEndTime - _renderer.roundBeginTime)/1000.0f + "s"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("下一关！",passdialogOnclicListener);
        builder.setNegativeButton("再欣赏一会儿", passdialogOnclicListener);
        builder.create().show();
    }



    private void newGameStart(MyAbstractRenderer _renderer)
    {
        _renderer.newGame();
        ImageView img = (ImageView) findViewById(R.id.stageNum);
        switch(_renderer.stage)
        {
            case 1: img.setImageResource(R.drawable.stage_1); break;
            case 2: img.setImageResource(R.drawable.stage_2); break;
            case 3: img.setImageResource(R.drawable.stage_3); break;
            case 4: img.setImageResource(R.drawable.stage_4); break;
            case 5: img.setImageResource(R.drawable.stage_5); break;
            case 6: img.setImageResource(R.drawable.stage_6); break;
            case 7: img.setImageResource(R.drawable.stage_7); break;
            case 8: img.setImageResource(R.drawable.stage_8); break;
            case 9: img.setImageResource(R.drawable.stage_9); break;
            case 10: img.setImageResource(R.drawable.stage_10); break;
        }
    }

    private boolean switch_gravity()
    {
        if (GRAVITY)
        {
            renderer.eyeMove(0f, 0f, 0f, true);
            GRAVITY = false;
        }
        else
        {
            GRAVITY = true;
        }
        return GRAVITY;
    }

    private MediaPlayer bkgMusic;
    private MediaPlayer helpM, pass, pick;
    private MediaPlayer p1, p2, p3, p4, p5, p6, p7, p8, p9;
    private void musicPrepare()
    {
        bkgMusic = MediaPlayer.create(this, R.raw.bkgmusic);
        helpM =  MediaPlayer.create(this, R.raw.help);
        pass =  MediaPlayer.create(this, R.raw.pass);
        pick =  MediaPlayer.create(this, R.raw.pick);
        p1 =  MediaPlayer.create(this, R.raw.p1); p1.setVolume(0.3f, 0.3f);
        p2 =  MediaPlayer.create(this, R.raw.p2);p2.setVolume(0.3f, 0.3f);
        p3 =  MediaPlayer.create(this, R.raw.p3);p3.setVolume(0.3f, 0.3f);
        p4 =  MediaPlayer.create(this, R.raw.p4);p4.setVolume(0.3f, 0.3f);
        p5 =  MediaPlayer.create(this, R.raw.p5);p5.setVolume(0.3f, 0.3f);
        p6 =  MediaPlayer.create(this, R.raw.p6);p6.setVolume(0.3f, 0.3f);
        p7 =  MediaPlayer.create(this, R.raw.p7);p7.setVolume(0.3f, 0.3f);
        p8 =  MediaPlayer.create(this, R.raw.p8);p8.setVolume(0.3f, 0.3f);
        p9 =  MediaPlayer.create(this, R.raw.p9);p9.setVolume(0.3f, 0.3f);
        //播放工程res目录下的raw目录中的音乐文件in_call_alarm
        try {
            bkgMusic.prepare();
        } catch (IllegalStateException e) {
        } catch (IOException e) {

        }
        bkgMusic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                //播完了接着播或者关闭mMediaPlayer
                bkgMusic.start();
            }
        });
    }

    public void showHelp(int id)
    {
        //这里是获取图片Bitmap，也可以传入其他参数到Dialog中
        Bitmap  bitmap = BitmapFactory.decodeResource(this.getResources(), id);
        ImageDialog.Builder dialogBuild = new ImageDialog.Builder(this);
        dialogBuild.setImage(bitmap);
        ImageDialog dialog = dialogBuild.create();
        dialog.setCanceledOnTouchOutside(true);// 点击外部区域关闭
        dialog.show();
    }
}
