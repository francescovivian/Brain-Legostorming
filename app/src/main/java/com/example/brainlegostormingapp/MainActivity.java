package com.example.brainlegostormingapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private TextView textView;

    private TextView testoCronometro;
    private Button start;
    private Button stop;
    private Button left;
    private Button right;
    private Button retro;
    private Button open;
    private Button close;
    private Button conn;
    private Button cancel;

    private Context contesto;
    private Cronometro cronometro;
    private Thread threadCronometro;

    private TachoMotor rm;
    private TachoMotor lm;
    private TachoMotor hand;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contesto = this;

        testoCronometro = findViewById(R.id.cronometro);
        start = findViewById(R.id.startButton);
        stop = findViewById(R.id.stopButton);
        left = findViewById(R.id.leftButton);
        right = findViewById(R.id.rightButton);
        retro = findViewById(R.id.retroButton);
        open = findViewById(R.id.openButton);
        close = findViewById(R.id.closeButton);
        conn = findViewById(R.id.connButton);
        cancel = findViewById(R.id.cancelButton);

        try
        {
            //BluetoothConnection.BluetoothChannel conn = new BluetoothConnection("EV3BL").connect();
            BluetoothConnection blueconn = new BluetoothConnection("EV3BL");
            BluetoothConnection.BluetoothChannel bluechan = blueconn.connect();
            EV3 ev3 = new EV3(bluechan);
            Prelude.trap(() -> ev3.run(this::legoMain));
            //ev3.cancel();

            start.setOnClickListener(v ->
            {
                /*if (cronometro == null)
                {
                    cronometro = new Cronometro(contesto);
                    threadCronometro = new Thread(cronometro);
                    threadCronometro.start();
                    cronometro.start();
                }*/
                try
                {
                    rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                    lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Fatal error: Cannot connect to EV3");
                    e.printStackTrace();
                }
                startEngine(rm, 50);
                startEngine(lm, 50);
            });

            stop.setOnClickListener(v ->
            {
                stopEngine(rm);
                stopEngine(lm);
                stopEngine(hand);
            });

            retro.setOnClickListener(v ->
            {
                try
                {
                    rm.setPolarity(TachoMotor.Polarity.FORWARD);
                    lm.setPolarity(TachoMotor.Polarity.FORWARD);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Fatal error: Cannot connect to EV3");
                    e.printStackTrace();
                }
                startEngine(rm, 40);
                startEngine(lm, 40);
            });

            left.setOnClickListener(v ->
            {
                try
                {
                    rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                    lm.setPolarity(TachoMotor.Polarity.FORWARD);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Fatal error: Cannot connect to EV3");
                    e.printStackTrace();
                }
                startEngine(rm, 40);
                startEngine(lm, 40);
            });

            right.setOnClickListener(v ->
            {
                try
                {
                    rm.setPolarity(TachoMotor.Polarity.FORWARD);
                    lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Fatal error: Cannot connect to EV3");
                    e.printStackTrace();
                }
                startEngine(rm, 40);
                startEngine(lm, 40);
            });

            open.setOnClickListener(v ->
            {
                try
                {
                    hand.setPolarity(TachoMotor.Polarity.FORWARD);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Fatal error: Cannot connect to EV3");
                    e.printStackTrace();
                }
                startEngine(hand,10);
            });

            close.setOnClickListener(v ->
            {
                try
                {
                    hand.setPolarity(TachoMotor.Polarity.BACKWARDS);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Fatal error: Cannot connect to EV3");
                    e.printStackTrace();
                }
                startEngine(hand,10);
            });

            conn.setOnClickListener(v ->
            {
                if(cronometro != null)
                {
                    cronometro.stop();
                    threadCronometro.interrupt();
                    threadCronometro = null;
                    cronometro = null;
                }
                System.out.println("La connessione è già stata effettuata e pure il lancio del LegoMain");
            });

            cancel.setOnClickListener(v ->
            {
                /*if(cronometro != null)
                {
                    cronometro.stop();
                    threadCronometro.interrupt();
                    threadCronometro = null;
                    cronometro = null;
                }*/
                bluechan.close();
            });
        }
        catch (IOException e)
        {
            Log.e(TAG, "Fatal error: Cannot connect to EV3");
            e.printStackTrace();
        }
    }

    private void legoMain(EV3.Api api)
    {
        final String TAG = Prelude.ReTAG("legoMain");

        //final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
        final UltrasonicSensor us = api.getUltrasonicSensor(EV3.InputPort._2);
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

        while (!api.ev3.isCancelled())
        {
            try
            {
                Future<Float> distance = us.getDistance();
                /*Future<Float> posdx = rm.getPosition();
                Future<Float> possx = lm.getPosition();
                Future<Float> speeddx = rm.getSpeed();
                Future<Float> speedsx = lm.getSpeed();
                //System.out.println(su.getDistance());
                //System.out.println(testoCronometro.getText());*/
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void startEngine(TachoMotor m, int i)
    {
        try
        {
            m.setSpeed(i);
            m.start();
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

    public void aggiornaTimer(String tempo)
    {
        runOnUiThread(() -> testoCronometro.setText(tempo));
    }
}
