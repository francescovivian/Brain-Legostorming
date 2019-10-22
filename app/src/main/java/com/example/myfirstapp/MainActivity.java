package com.example.myfirstapp;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        TextView numeri = findViewById(R.id.numeri);
        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v ->
        {
            textView.setText("Ho premuto start");
            numeri.setText("");
            for (Integer i = 0; i < 11; i++)
            {

                numeri.setText(numeri.getText() + " " + i.toString());
            }
        });
        stopButton.setOnClickListener(v ->
        {
            textView.setText("Ho premuto stop");
            numeri.setText("Ho stoppato");
        });
    }
}
