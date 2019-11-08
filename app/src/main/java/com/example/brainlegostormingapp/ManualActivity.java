package com.example.brainlegostormingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class ManualActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private Thread cronometro;
    boolean conta;

    BluetoothConnection.BluetoothChannel bluechan;
    EV3 ev3;
    private TachoMotor rm;
    private TachoMotor lm;
    private TachoMotor hand;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Button mainButton = findViewById(R.id.mainButton);
        Button autoButton = findViewById(R.id.autoButton);

        mainButton.setOnClickListener(v ->
        {
            Intent mainIntent = new Intent(getBaseContext(),MainActivity.class);
            startActivity(mainIntent);
        });

        autoButton.setOnClickListener(v ->
        {
            Intent autoIntent = new Intent(getBaseContext(),AutoActivity.class);
            startActivity(autoIntent);
        });

        TextView testoCronometro = findViewById(R.id.cronometro);
        Button start = findViewById(R.id.startButton);
        Button stop = findViewById(R.id.stopButton);
        Button left = findViewById(R.id.leftButton);
        Button right = findViewById(R.id.rightButton);
        Button retro = findViewById(R.id.retroButton);
        Button open = findViewById(R.id.openButton);
        Button close = findViewById(R.id.closeButton);
        Button conn = findViewById(R.id.connButton);
        Button cancel = findViewById(R.id.cancelButton);

        start.setEnabled(false);
        stop.setEnabled(false);
        retro.setEnabled(false);
        left.setEnabled(false);
        right.setEnabled(false);
        open.setEnabled(false);
        close.setEnabled(false);
        cancel.setEnabled(false);

        start.setOnClickListener(v ->
        {
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
                hand.setPolarity(TachoMotor.Polarity.BACKWARDS);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Fatal error: Cannot connect to EV3");
                e.printStackTrace();
            }
            startEngine(hand,15);
        });

        close.setOnClickListener(v ->
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
            startEngine(hand,15);
        });

        conn.setOnClickListener(v ->
        {
            conn.setEnabled(false);
            try
            {
                BluetoothConnection blueconn = new BluetoothConnection("EV3BL");
                bluechan = blueconn.connect();
                ev3 = new EV3(bluechan);
                Prelude.trap(() -> ev3.run(this::legoMain));
                if (cronometro == null)
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
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            start.setEnabled(true);
            stop.setEnabled(true);
            retro.setEnabled(true);
            left.setEnabled(true);
            right.setEnabled(true);
            open.setEnabled(true);
            close.setEnabled(true);
            cancel.setEnabled(true);
        });

        cancel.setOnClickListener(v ->
        {
            cancel.setEnabled(false);
            if(cronometro != null)
            {
                conta = false;
                cronometro.interrupt();
                cronometro = null;
            }
            ev3.cancel();
            bluechan.close();
            start.setEnabled(false);
            stop.setEnabled(false);
            retro.setEnabled(false);
            left.setEnabled(false);
            right.setEnabled(false);
            open.setEnabled(false);
            close.setEnabled(false);
            conn.setEnabled(true);
        });
    }

    private void legoMain(EV3.Api api)
    {
        //final String TAG = Prelude.ReTAG("legoMain");

        //final UltrasonicSensor us = api.getUltrasonicSensor(EV3.InputPort._1);
        final LightSensor ls = api.getLightSensor(EV3.InputPort._4);
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
                //Future<Float> distance = us.getDistance();
                //Future<Short> ambient = ls.getAmbient();
                Future<LightSensor.Color> tempcol = ls.getColor();
                LightSensor.Color col = tempcol.get();
                runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(col.toARGB32()));

                //System.out.println(distance.get());
                //System.out.println(ambient.get());
                //System.out.println(reflected.get());
                //System.out.println(col);
            }
            catch (IOException | InterruptedException | ExecutionException e)
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

    public void aggiornaTimer(TextView tv, String tempo)
    {
        runOnUiThread(() -> tv.setText(tempo));
    }
}
