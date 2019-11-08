package com.example.brainlegostormingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class AutoActivity extends AppCompatActivity
{
    private Thread cronometro;
    boolean conta;

    BluetoothConnection.BluetoothChannel bluechan;
    EV3 ev3;
    private TachoMotor rm;
    private TachoMotor lm;
    private TachoMotor hand;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

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
            start.setEnabled(false);
            stop.setEnabled(true);
            main.setEnabled(false);
            manual.setEnabled(false);
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
        });

        stop.setOnClickListener(v ->
        {
            stop.setEnabled(false);
            if(cronometro != null)
            {
                conta = false;
                cronometro.interrupt();
                cronometro = null;
            }
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
                Future<LightSensor.Color> col = ls.getColor();
                //System.out.println(distance.get());
                //System.out.println(ambient.get());
                //System.out.println(reflected.get());
                //System.out.println(col.get());
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

    public void aggiornaTimer(TextView tv, String tempo)
    {
        runOnUiThread(() -> tv.setText(tempo));
    }
}
