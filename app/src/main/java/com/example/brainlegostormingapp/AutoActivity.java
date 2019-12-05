package com.example.brainlegostormingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.GenEV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.Plug;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class AutoActivity extends AppCompatActivity
{
    private static final String TAG = "AutoActivity";

    private long tempoInizio, attuale;
    private int secondi, minuti, ore, millisecondi;

    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;

    private Motor rm;
    private Motor lm;
    private Motor hand;

    private CameraBridgeViewBase camera;
    Mat frame;
    BallFinder ballFinder;
    ArrayList<Ball> balls;
    Ball ball;

    private int dimM, dimN;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        getSupportActionBar().hide();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        decorView.setOnSystemUiVisibilityChangeListener((v) ->
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN));

        if (!OpenCVLoader.initDebug()) Log.e(TAG, "Unable to load OpenCV");
        else Log.d(TAG, "OpenCV loaded");

        camera = findViewById(R.id.cameraView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        else avviaFotocamera();

        Button main = findViewById(R.id.mainButton);
        Button manual = findViewById(R.id.manualButton);

        main.setOnClickListener(v ->
        {
            Intent mainIntent = new Intent(getBaseContext(),MainActivity.class);
            startActivity(mainIntent);
        });

        manual.setOnClickListener(v ->
        {
            Intent manualIntent = new Intent(getBaseContext(),ManualActivity.class);
            startActivity(manualIntent);
        });

        TextView testoCronometro = findViewById(R.id.cronometro);
        Button start = findViewById(R.id.startButton);
        Button stop = findViewById(R.id.stopButton);
        Button setMatrix = findViewById(R.id.setDimMatrix);
        EditText matrixM = findViewById(R.id.dimM);
        EditText matrixN = findViewById(R.id.dimN);
        //LinearLayout matrixView = findViewById(R.id.matrixView);

        setMatrix.setOnClickListener(v ->
        {
            try
            {
                dimM = Integer.parseInt(matrixM.getText().toString());
                dimN = Integer.parseInt(matrixN.getText().toString());
                setMatrix.setEnabled(false);
                matrixM.setEnabled(false);
                matrixN.setEnabled(false);
                start.setEnabled(true);

                PixelGridView pixelGrid = new PixelGridView(this);
                pixelGrid.setNumRows(dimM);
                pixelGrid.setNumColumns(dimN);

                //Per fare i quadrati rossi
                //pixelGrid.changeCellChecked(2,3);

                //matrixView.addView(pixelGrid);
            }
            catch (NumberFormatException ignored)
            {
                ignored.printStackTrace();
            }
        });

        start.setEnabled(false);
        stop.setEnabled(false);

        start.setOnClickListener(v ->
        {
            try
            {
                BluetoothConnection blueconn = new BluetoothConnection("EV3BL");
                bluechan = blueconn.connect();
                ev3 = new EV3(bluechan);
                Prelude.trap(() -> ev3.run(this::legoMain));
                Toast.makeText(this,"Connessione stabilita con successo",Toast.LENGTH_SHORT).show();
                start.setEnabled(false);
                stop.setEnabled(true);
                main.setEnabled(false);
                manual.setEnabled(false);
                tempoInizio = System.currentTimeMillis();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(this,"Connessione non stabilita",Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(v ->
        {
            rm.stopEngine();
            lm.stopEngine();
            stop.setEnabled(false);
            attuale = System.currentTimeMillis() - tempoInizio;
            secondi = (int) (attuale/1000) % 60;
            minuti = (int) (attuale/60000) % 60;
            ore = (int) (attuale/3600000) % 24;
            millisecondi = (int) attuale % 1000;
            aggiornaTimer(testoCronometro, String.format("%02d:%02d:%02d:%03d", ore, minuti, secondi, millisecondi));
            ev3.cancel();
            bluechan.close();
            start.setEnabled(true);
            main.setEnabled(true);
            manual.setEnabled(true);
        });
    }

    private void legoMain(EV3.Api api)
    {
        //final String TAG = Prelude.ReTAG("legoMain");

        final UltrasonicSensor us = api.getUltrasonicSensor(EV3.InputPort._1);
        //final LightSensor ls = api.getLightSensor(EV3.InputPort._4);
        //final GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._4);

        rm = new Motor(api, EV3.OutputPort.A);
        lm = new Motor(api, EV3.OutputPort.D);
        hand = new Motor(api, EV3.OutputPort.C);

        try
        {
            rm.setType(TachoMotor.Type.LARGE);
            lm.setType(TachoMotor.Type.LARGE);
            hand.setType(TachoMotor.Type.MEDIUM);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Future<Float> Fdistance;
        Float distance;

        boolean isRunning = false;
        boolean isFind = false;
        boolean isSearching = false;
        boolean isApproached = false;
        boolean isCentered = false;
        boolean isStraightening = false;

        while (!api.ev3.isCancelled())
        {
            try
            {
                for (int i = 0; i < balls.size(); i++)
                {
                    ball = balls.get(i);

                    if (!isSearching)
                    {
                        rm.startEngine(20, 'b');
                        lm.startEngine(20, 'f');
                        isSearching = true;
                    }

                    if (ball.center.y > 220 && ball.center.y < 260 && !ball.color.equals("yellow") && isSearching && !isFind)
                    {
                        if (!isStraightening)
                        {
                            rm.startEngine(10, 'f');
                            lm.startEngine(10, 'b');
                            isStraightening = true;
                        }

                        if (ball.center.y > 230 && ball.center.y < 250)
                        {
                            rm.startEngine(20, 'f');
                            lm.startEngine(20, 'f');
                            hand.autoMoveHand(15, 'o');
                            isFind = true;
                        }
                    }

                    if (isFind && !isApproached)
                    {
                        if (ball.center.y > 150 && ball.center.y < 240)
                        {
                            rm.setSpeed(10);
                            lm.setSpeed(30);
                            //(int)(10 + (240 - ball.center.y + 240)/40)
                        }

                        if (ball.center.y == 241)
                        {
                            rm.setSpeed(10);
                            lm.setSpeed(10);
                        }

                        if (ball.center.y > 240 && ball.center.y < 300)
                        {
                            lm.setSpeed(10);
                            rm.setSpeed(30);
                            //(int)(10 + (240 - ball.center.y)/40)
                        }

                        if (ball.radius >= 35)
                        {
                            rm.setSpeed(20);
                            lm.setSpeed(20);
                            isApproached = true;
                        }
                    }

                    if (isApproached)
                    {
                        Fdistance = us.getDistance();
                        Thread.sleep(100);
                        distance = Fdistance.get();
                        //Log.e("distance", String.valueOf(distance));

                        if (distance > 15 && distance <= 30 && !isRunning)
                        {
                            rm.setSpeed(20);
                            lm.setSpeed(20);
                            isRunning = true;
                        }

                        if (distance <= 15 && isRunning)
                        {
                            Thread.sleep(1000);
                            rm.stopEngine();
                            lm.stopEngine();
                            hand.autoMoveHand(25, 'c');
                        }
                    }

                    balls.remove(ball);
                }

                /*Future<Short> Fambient = ls.getAmbient();
                Short ambient = Fambient.get();

                Future<LightSensor.Color> Fcol = ls.getColor();
                LightSensor.Color col = Fcol.get();*/
            }
            catch (IOException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void aggiornaTimer(TextView tv, String tempo)
    {
        runOnUiThread(() -> tv.setText(tempo));
    }

    public void avviaFotocamera()
    {
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setMaxFrameSize(640, 480);
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2()
        {
            @Override
            public void onCameraViewStarted(int width, int height)
            {
                Log.d(TAG, "Camera Started");
            }

            @Override
            public void onCameraViewStopped()
            {
                Log.d(TAG, "Camera Stopped");
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                System.gc();

                frame = inputFrame.rgba();
                ballFinder = new BallFinder(frame);
                balls = ballFinder.findBalls();

                for (int i = 0; i < balls.size(); i++)
                {
                    ball = balls.get(i);
                    Point center = new Point(ball.center.x,ball.center.y);
                    int radius = (int) ball.radius;
                    Scalar color_rgb;

                    if (ball.color.equals("red")) color_rgb = new Scalar(255, 0, 0);
                    else if (ball.color.equals("blue")) color_rgb = new Scalar(0, 0, 255);
                    else if (ball.color.equals("yellow")) color_rgb = new Scalar(255, 255, 0);
                    else color_rgb = new Scalar(0, 0, 0);

                    Imgproc.circle(frame, center,radius,color_rgb,2);

                    Imgproc.circle(frame, new Point(320,240),10,new Scalar(0,0,0),2);
                    Imgproc.line(frame, new Point(310,240), new Point(330,240),new Scalar(0,0,0),2);
                    Imgproc.line(frame, new Point(320,230), new Point(320,250),new Scalar(0,0,0),2);

                    /*Log.e("ball center x ", String.valueOf(ball.center.x));
                    Log.e("ball center y ", String.valueOf(ball.center.y));
                    Log.e("ball radius ", String.valueOf(ball.radius));
                    Log.e("ball color ", ball.color);*/
                }

                System.gc();

                return frame;
            }
        });

        camera.enableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult)
    {
        if (requestCode == 1 && grantResult.length > 0 && grantResult[0]==PackageManager.PERMISSION_GRANTED) avviaFotocamera();
    }
}
