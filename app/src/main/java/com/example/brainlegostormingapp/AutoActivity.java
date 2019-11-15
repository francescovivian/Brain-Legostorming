package com.example.brainlegostormingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.CameraBridgeViewBase;
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

    private Thread cronometro;
    boolean conta;

    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;

    private TachoMotor rm;
    private TachoMotor lm;
    private TachoMotor hand;

    private CameraBridgeViewBase camera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!OpenCVLoader.initDebug()) Log.e(TAG, "Unable to load OpenCV");
        else Log.d(TAG, "OpenCV loaded");

        camera = findViewById(R.id.cameraView);
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setMaxFrameSize(320, 240);
        //camera.disableFpsMeter();
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
                Mat frame = inputFrame.rgba();
                Mat frameT = frame.t();
                Core.flip(frameT, frameT, 1);
                Imgproc.resize(frameT, frameT, frame.size());
                return frameT;
            }
        });

        camera.enableView();

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
                /*if (cronometro == null)
                {
                    cronometro = new Thread(() ->
                    {
                        long attuale, MINUTO = 60000, ORA = 3600000, tempoInizio = System.currentTimeMillis();
                        int secondi, minuti, ore, millisecondi;
                        conta = true;

                        while(conta)
                        {
                            attuale = System.currentTimeMillis() - tempoInizio;

                            secondi = (int) (attuale/1000) % 60;
                            minuti = (int) (attuale/MINUTO) % 60;
                            ore = (int) (attuale/ORA) % 24;
                            millisecondi = (int) attuale % 1000;

                            aggiornaTimer(testoCronometro, String.format("%02d:%02d:%02d:%03d", ore, minuti, secondi, millisecondi));
                        }
                    });
                    cronometro.start();
                }*/
            }
            catch (IOException e)
            {
                e.printStackTrace();
                new AlertDialog.Builder(this).setMessage("Connessione non stabilita").show();
            }
        });

        stop.setOnClickListener(v ->
        {
            //autoMoveHand(hand,25,'o');
            stopEngine(rm);
            stopEngine(lm);
            stop.setEnabled(false);
            /*if(cronometro != null)
            {
                conta = false;
                cronometro.interrupt();
                cronometro = null;
            }*/
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

        autoMoveHand(hand,15,'o');

        try
        {
            hand.waitUntilReady();
        }
        catch (IOException | InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        Future<Float> Fdistance;
        Float distance;

        boolean isRunning = false;
        boolean isMiddle = false;

        while (!api.ev3.isCancelled())
        {
            try
            {
                Fdistance = us.getDistance();
                distance = Fdistance.get(1000, TimeUnit.MILLISECONDS);

                System.out.println(distance);

                if (distance > 20 && distance <= 40 && !isRunning)
                {
                    startEngine(rm, 50, 'b');
                    startEngine(lm, 50, 'b');
                    isRunning = true;
                    isMiddle = true;
                }

                if (distance > 8 && distance <= 20 && isMiddle)
                {
                    startEngine(rm, 30, 'b');
                    startEngine(lm, 30, 'b');
                    isMiddle = false;
                }

                if (distance <= 8 && isRunning)
                {
                    //stopEngine(rm);
                    //stopEngine(lm);
                    autoMoveHand(hand,25,'c');
                    isRunning = false;
                    isMiddle = false;
                }

                /*Future<Short> Fambient = ls.getAmbient();
                Short ambient = Fambient.get();

                Future<LightSensor.Color> Fcol = ls.getColor();
                LightSensor.Color col = Fcol.get();*/
            }
            catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
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
                hand.setPolarity(TachoMotor.Polarity.BACKWARDS);
                m.setTimeSpeed(i,0,3000,0,true);
            }
            if (c== 'c')
            {
                hand.setPolarity(TachoMotor.Polarity.FORWARD);
                m.setTimeSpeed(i,0,3000,0,true);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void aggiornaTimer(TextView tv, String tempo)
    {
        runOnUiThread(() -> tv.setText(tempo));
    }
}
