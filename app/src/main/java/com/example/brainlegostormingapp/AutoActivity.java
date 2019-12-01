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

    private TachoMotor rm;
    private TachoMotor lm;
    private TachoMotor hand;

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
                new AlertDialog.Builder(this).setMessage("Connessione stabilita con successo").show();
                start.setEnabled(false);
                stop.setEnabled(true);
                main.setEnabled(false);
                manual.setEnabled(false);
                tempoInizio = System.currentTimeMillis();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                new AlertDialog.Builder(this).setMessage("Connessione non stabilita").show();
            }
        });

        stop.setOnClickListener(v ->
        {
            stopEngine(rm);
            stopEngine(lm);
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

        rm = api.getTachoMotor(EV3.OutputPort.A);
        lm = api.getTachoMotor(EV3.OutputPort.D);
        hand = api.getTachoMotor(EV3.OutputPort.C);

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
        boolean isMiddle = false;
        boolean open = false;

        while (!api.ev3.isCancelled())
        {
            try
            {
                Fdistance = us.getDistance();
                Thread.sleep(100);
                distance = Fdistance.get();
                Log.e("distance", String.valueOf(distance));

                if (distance == 255 && !open)
                {
                    autoMoveHand(hand,15,'o');
                    open = true;
                }

                if (distance > 20 && distance <= 40 && !isRunning)
                {
                    startEngine(rm, 50, 'b');
                    startEngine(lm, 50, 'b');
                    isRunning = true;
                }

                if (distance > 10 && distance <= 20 && isRunning)
                {
                    startEngine(rm, 30, 'b');
                    startEngine(lm, 30, 'b');
                    isMiddle = true;
                }

                if (distance <= 10 && isRunning && isMiddle)
                {
                    Thread.sleep(500);
                    stopEngine(rm);
                    stopEngine(lm);
                    autoMoveHand(hand,25,'c');
                    isRunning = false;
                    isMiddle = false;
                    open = false;
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

    private void startEngine(TachoMotor m, int i, char c)
    {
        try
        {
            if (c == 'f')
            {
                m.setPolarity(TachoMotor.Polarity.FORWARD);
                m.setSpeed(i);
                m.start();
            }
            if (c == 'b')
            {
                m.setPolarity(TachoMotor.Polarity.BACKWARDS);
                m.setSpeed(i);
                m.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void stopEngine(TachoMotor m)
    {
        try
        {
            m.stop();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void autoMoveHand(TachoMotor m, int i, char c)
    {
        try
        {
            if (c == 'o')
            {
                m.setPolarity(TachoMotor.Polarity.BACKWARDS);
                m.setTimeSpeed(i,0,3000,0,true);
            }
            if (c== 'c')
            {
                m.setPolarity(TachoMotor.Polarity.FORWARD);
                m.setTimeSpeed(i,0,3000,0,true);
            }
            m.waitUntilReady();
        }
        catch (IOException | InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
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

                    balls.remove(ball);

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
