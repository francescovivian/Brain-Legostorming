package com.example.brainlegostormingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class ManualActivity extends AppCompatActivity
{
    //private static final String TAG = "ManualActivity";

    private Thread cronometro;
    boolean conta;

    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;

    private TachoMotor rm;
    private TachoMotor lm;
    private TachoMotor hand;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button main = findViewById(R.id.mainButton);
        Button auto = findViewById(R.id.autoButton);

        main.setOnClickListener(v ->
        {
            Intent mainIntent = new Intent(getBaseContext(),MainActivity.class);
            startActivity(mainIntent);
        });

        auto.setOnClickListener(v ->
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
            startEngine(rm, 50,'b');
            startEngine(lm, 50,'b');
        });

        stop.setOnClickListener(v ->
        {
            stopEngine(rm);
            stopEngine(lm);
            stopEngine(hand);
        });

        retro.setOnClickListener(v ->
        {
            startEngine(rm, 40,'f');
            startEngine(lm, 40,'f');
        });

        left.setOnClickListener(v ->
        {
            startEngine(rm, 40,'b');
            startEngine(lm, 40,'f');
        });

        right.setOnClickListener(v ->
        {
            startEngine(rm, 40,'f');
            startEngine(lm, 40,'b');
        });

        open.setOnClickListener(v -> startEngine(hand,15,'b'));

        close.setOnClickListener(v -> startEngine(hand,25,'f'));

        conn.setOnClickListener(v ->
        {
            try
            {
                BluetoothConnection blueconn = new BluetoothConnection("EV3BL");
                bluechan = blueconn.connect();
                ev3 = new EV3(bluechan);
                Prelude.trap(() -> ev3.run(this::legoMain));
                new AlertDialog.Builder(this).setMessage("Connessione stabilita con successo").show();
                conn.setEnabled(false);
                main.setEnabled(false);
                auto.setEnabled(false);
                start.setEnabled(true);
                stop.setEnabled(true);
                retro.setEnabled(true);
                left.setEnabled(true);
                right.setEnabled(true);
                open.setEnabled(true);
                close.setEnabled(true);
                cancel.setEnabled(true);
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

                            try
                            {
                                Thread.sleep(17);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
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
                new AlertDialog.Builder(this).setMessage("Connessione non stabilita").show();
            }
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
            main.setEnabled(true);
            auto.setEnabled(true);
        });
    }

    private void legoMain(EV3.Api api)
    {
        //final String TAG = Prelude.ReTAG("legoMain");

        final LightSensor ls = api.getLightSensor(EV3.InputPort._4);

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
                Future<LightSensor.Color> Fcol = ls.getColor();
                LightSensor.Color col = Fcol.get();

                runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(col.toARGB32()));
            }
            catch (IOException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(LightSensor.Color.WHITE.toARGB32()));
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

    public void aggiornaTimer(TextView tv, String tempo)
    {
        runOnUiThread(() -> tv.setText(tempo));
    }
}
