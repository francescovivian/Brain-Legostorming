package com.example.brainlegostormingapp.Activity;

import android.content.Intent; //questo a cosa ti serve Denny? Perche il prof non lo usa

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainlegostormingapp.R;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    Button btnManual, btnTest1, btnTest2, btnTest3,btnFakeNearby;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        btnManual = findViewById(R.id.btnManual);
        btnTest1 = findViewById(R.id.btnTest1);
        btnTest2 = findViewById(R.id.btnTest2);
        btnTest3 = findViewById(R.id.btnTest3);
        btnFakeNearby = findViewById(R.id.btnNearby);

        btnManual.setOnClickListener(v ->
        {
            Intent manualIntent = new Intent(getBaseContext(),ManualActivity.class);
            startActivity(manualIntent);
        });

        btnTest1.setOnClickListener(v ->
        {
            Intent autoIntent = new Intent(getBaseContext(), AutoActivity.class);
            autoIntent.putExtra("choosen",1);
            startActivity(autoIntent);
        });

        btnTest2.setOnClickListener(v ->
        {
            Intent autoIntent = new Intent(getBaseContext(),AutoActivity.class);
            autoIntent.putExtra("choosen",2);
            startActivity(autoIntent);
        });

        btnTest3.setOnClickListener(v ->
        {
            Intent autoIntent = new Intent(getBaseContext(),AutoActivity.class);
            autoIntent.putExtra("choosen",3);
            startActivity(autoIntent);
        });
        btnFakeNearby.setOnClickListener(v -> {
            Intent nearbyIntent = new Intent(getBaseContext(), NearbySimulatorActivity.class);
            startActivity(nearbyIntent);
        });
    }
}
