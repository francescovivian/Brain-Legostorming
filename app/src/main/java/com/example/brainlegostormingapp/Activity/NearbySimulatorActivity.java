package com.example.brainlegostormingapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Position;
import com.example.brainlegostormingapp.R;
import com.example.brainlegostormingapp.Robot;
import com.example.brainlegostormingapp.Tests.Test2;
import com.example.brainlegostormingapp.Utility.Utility;


import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.util.Prelude;

public class NearbySimulatorActivity extends AppCompatActivity {
    private static final String TAG = "NearbySimulatorActivity";

    private long tempoInizio, attuale;
    private int secondi, minuti, ore, millisecondi;

    Robot robot;

    private int dimR, dimC, startX, startY, mine;
    private char orientation;
    private TextView txtCronometro, distanza;
    private EditText eTxtX, eTxtY,eTxtMatrixR, eTxtMatrixC, eTxtStartX, eTxtStartY, eTxtMine;
    private Button btnMain, btnManual, btnStart, btnStop, btnSetMatrix, btnResetMatrix, btnInvia;
    private Spinner spnOrientation;
    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;
    private GameField gameField;
    private Test2 test2;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_simulator);
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

        //region FINDVIEW
        txtCronometro = findViewById(R.id.cronometro);
        distanza = findViewById(R.id.distanza);
        btnMain = findViewById(R.id.mainButton);
        btnManual = findViewById(R.id.manualButton);
        btnStart = findViewById(R.id.btnStartButton);
        btnStop = findViewById(R.id.btnStopButton);
        btnSetMatrix = findViewById(R.id.btnSetDimMatrix);
        btnResetMatrix = findViewById(R.id.btnResetDimMatrix);
        btnInvia = findViewById(R.id.btnInvia);
        eTxtX = findViewById(R.id.eTxtX);
        eTxtY = findViewById(R.id.eTxtY);
        eTxtMatrixR = findViewById(R.id.eTxtDimR);
        eTxtMatrixC = findViewById(R.id.eTxtDimC);
        eTxtStartX = findViewById(R.id.eTxtStartX);
        eTxtStartY = findViewById(R.id.eTxtStartY);
        spnOrientation = findViewById(R.id.direction_spinner);
        eTxtMine = findViewById(R.id.eTxtMine);
        //endregion

        btnMain.setOnClickListener(v -> {
            Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(mainIntent);
        });

        btnManual.setOnClickListener(v -> {
            Intent manualIntent = new Intent(getBaseContext(), ManualActivity.class);
            startActivity(manualIntent);
        });

        btnSetMatrix.setOnClickListener(v -> {
            try {
                dimR = Integer.parseInt(eTxtMatrixR.getText().toString());
                dimC = Integer.parseInt(eTxtMatrixC.getText().toString());
                startX = Integer.parseInt(eTxtStartX.getText().toString());
                startY = Integer.parseInt(eTxtStartY.getText().toString());
                mine = Integer.parseInt(eTxtMine.getText().toString());
                orientation = String.valueOf(spnOrientation.getSelectedItem()).charAt(0);

                Utility.elementToggle(eTxtMatrixR, eTxtMatrixC, eTxtStartX, eTxtStartY, eTxtMine, spnOrientation);
                //compare btnStart e btnReset, scompare btnsetdim
                Utility.elementVisibilityToggle(btnStart,btnSetMatrix,btnResetMatrix);
                gameField = new GameField(dimR, dimC, orientation, startX, startY,null);

                //Per fare i quadrati rossi
                //pixelGrid.minaCheck(2,3);

                //matrixView.addView(pixelGrid);
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        });

        btnResetMatrix.setOnClickListener(e -> {
            try {
                //matrixView.removeAllViews();
                eTxtMatrixR.setText("0");
                eTxtMatrixC.setText("0");
                eTxtStartX.setText("0");
                eTxtStartY.setText("0");
                spnOrientation.setSelection(0);
                Utility.elementToggle(eTxtMatrixR, eTxtMatrixC, eTxtStartX, eTxtStartY, eTxtMine, spnOrientation);
                //compare btnStart e btnReset, scompare btnsetdim
                Utility.elementVisibilityToggle(btnStart,btnSetMatrix,btnResetMatrix);
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        });

        btnStart.setOnClickListener(v ->
        {

            try {
                BluetoothConnection blueconn = new BluetoothConnection("EV3BL");
                bluechan = blueconn.connect();
                ev3 = new EV3(bluechan);
                Utility.playMp3Audio(getApplicationContext(),"rally.mp3");
                Prelude.trap(() -> ev3.run(this::legoMain));
                Toast.makeText(this, "Connessione stabilita con successo", Toast.LENGTH_SHORT).show();
                Utility.elementVisibilityToggle(btnStart,btnStop,btnInvia);
                Utility.elementToggle(btnResetMatrix, btnStart, btnStop, btnMain, btnManual);
                tempoInizio = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Connessione non stabilita", Toast.LENGTH_SHORT).show();
            }

        });

        btnStop.setOnClickListener(v -> {
            robot.stopRLEngines();
            Utility.elementToggle(btnStop);
            //manca il timer
            ev3.cancel();
            bluechan.close();
            Utility.elementToggle(btnStart, btnMain, btnManual);
            Utility.elementVisibilityToggle(btnStop,btnStart,txtCronometro,btnSetMatrix);
        });
        btnInvia.setOnClickListener(v -> {
            int x,y;
            x = Integer.parseInt(eTxtX.getText().toString());
            y = Integer.parseInt(eTxtY.getText().toString());
            test2.sendPosition(new Position(x,y));
        });
    }
    private void legoMain(EV3.Api api) {
        //final String TAG = Prelude.ReTAG("legoMain");

        robot = new Robot(api);

        test2 = new Test2(robot,gameField,getApplicationContext());
        test2.start();
    }
}
