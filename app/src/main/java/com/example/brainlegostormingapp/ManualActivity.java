package com.example.brainlegostormingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class ManualActivity extends AppCompatActivity {
    //private static final String TAG = "ManualActivity";

    private Thread cronometro;
    boolean conta;

    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;

    private Robot robot;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
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

        Button main = findViewById(R.id.mainButton);
        Button auto = findViewById(R.id.autoButton);

        main.setOnClickListener(v ->
        {
            Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(mainIntent);
        });

        auto.setOnClickListener(v ->
        {
            Intent autoIntent = new Intent(getBaseContext(), AutoActivity.class);
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
            robot.startRLEngines(50, 'f');
        });

        stop.setOnClickListener(v ->
        {
            robot.stopAllEngines();
        });

        retro.setOnClickListener(v ->
        {
            robot.startRLEngines(50, 'b');
        });

        left.setOnClickListener(v ->
        {
            robot.startEngine('r', 40, 'f');
            robot.startEngine('l', 40, 'b');
        });

        right.setOnClickListener(v ->
        {
            robot.startEngine('r', 40, 'b');
            robot.startEngine('l', 40, 'f');
        });

        open.setOnClickListener(v -> robot.startEngine('h', 15, 'b'));

        close.setOnClickListener(v -> robot.startEngine('h', 25, 'f'));

        conn.setOnClickListener(v ->
        {
            try {
                BluetoothConnection blueconn = new BluetoothConnection("EV3BL");
                bluechan = blueconn.connect();
                ev3 = new EV3(bluechan);
                Prelude.trap(() -> ev3.run(this::legoMain));
                Toast.makeText(this, "Connessione stabilita con successo", Toast.LENGTH_SHORT).show();
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
                if (cronometro == null) {
                    cronometro = new Thread(() ->
                    {
                        long attuale, MINUTO = 60000, ORA = 3600000, tempoInizio = System.currentTimeMillis();
                        int secondi, minuti, ore, millisecondi;
                        conta = true;

                        while (conta) {
                            try {
                                Thread.sleep(17);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            attuale = System.currentTimeMillis() - tempoInizio;
                            secondi = (int) (attuale / 1000) % 60;
                            minuti = (int) (attuale / MINUTO) % 60;
                            ore = (int) (attuale / ORA) % 24;
                            millisecondi = (int) attuale % 1000;

                            aggiornaTimer(testoCronometro, String.format("%02d:%02d:%02d:%03d", ore, minuti, secondi, millisecondi));
                        }
                    });
                    cronometro.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Connessione non stabilita", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(v ->
        {
            cancel.setEnabled(false);
            if (cronometro != null) {
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

    private void legoMain(EV3.Api api) {
        //final String TAG = Prelude.ReTAG("legoMain");

        robot = new Robot(api);

        while (!api.ev3.isCancelled()) {
            try {
                Future<LightSensor.Color> Fcol = robot.getColor();
                LightSensor.Color col = Fcol.get();

                runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(col.toARGB32()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(LightSensor.Color.WHITE.toARGB32()));
    }

    public void aggiornaTimer(TextView tv, String tempo) {
        runOnUiThread(() -> tv.setText(tempo));
    }
}
