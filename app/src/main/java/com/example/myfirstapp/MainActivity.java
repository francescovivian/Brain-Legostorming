package com.example.myfirstapp;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private TextView textView;

    private TextView testoCronometro;
    private Button start;
    private Button stop;
    private Button reset;

    private Context contesto;
    private Cronometro cronometro;
    private Thread threadCronometro;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contesto = this;

        testoCronometro = findViewById(R.id.cronometro);
        start = findViewById(R.id.startButton);
        stop = findViewById(R.id.stopButton);
        reset = findViewById(R.id.resetButton);

        start.setOnClickListener(v ->
        {
            if (cronometro == null)
            {
                cronometro = new Cronometro(contesto);
                threadCronometro = new Thread(cronometro);
                threadCronometro.start();
                cronometro.start();
            }
        });

        stop.setOnClickListener(v ->
        {
            if(cronometro != null)
            {
                cronometro.stop();
                threadCronometro.interrupt();
                threadCronometro = null;
                cronometro = null;
            }
        });

        reset.setOnClickListener(v ->
        {
            if(cronometro == null)
            {
                testoCronometro.setText("00:00:00:000");
            }
        });
    }

    public void aggiornaTimer(String tempo)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                testoCronometro.setText(tempo);
            }
        });
    }
}
