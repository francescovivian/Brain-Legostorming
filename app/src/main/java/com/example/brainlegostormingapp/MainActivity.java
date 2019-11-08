package com.example.brainlegostormingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button manualButton = findViewById(R.id.manualButton);
        Button autoButton = findViewById(R.id.autoButton);

        manualButton.setOnClickListener(v ->
        {
            Intent manualIntent = new Intent(getBaseContext(),ManualActivity.class);
            startActivity(manualIntent);
        });

        autoButton.setOnClickListener(v ->
        {
            Intent autoIntent = new Intent(getBaseContext(),AutoActivity.class);
            startActivity(autoIntent);
        });
    }
}
