package com.example.brainlegostormingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.util.Prelude;

public class AutoActivity extends AppCompatActivity {
    private static final String TAG = "AutoActivity";
    private long tempoInizio, attuale;
    private int secondi, minuti, ore, millisecondi;

    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;
    private Robot robot;
    private Mat frame;
    private BallFinder ballFinder;
    private LineFinder lineFinder;
    private ArrayList<Ball> balls;
    private ArrayList<Line> lines;
    private Ball ball;
    private GameField gameField;


    private int dimM, dimN;

    private CameraBridgeViewBase camera;
    private TextView txtCronometro;
    private Button btnMain, btnManual, btnStart, btnStop, btnSetMatrix, btnResetMatrix;
    private EditText eTxtMatrixM, eTxtMatrixN, eTxtStartX, eTxtStartY;
    private Spinner spnOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
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

        txtCronometro = findViewById(R.id.cronometro);
        btnMain = findViewById(R.id.mainButton);
        btnManual = findViewById(R.id.manualButton);
        btnStart = findViewById(R.id.btnStartButton);
        btnStop = findViewById(R.id.btnStopButton);
        btnSetMatrix = findViewById(R.id.btnSetDimMatrix);
        btnResetMatrix = findViewById(R.id.btnResetDimMatrix);
        eTxtMatrixM = findViewById(R.id.eTxtDimM);
        eTxtMatrixN = findViewById(R.id.eTxtDimN);
        eTxtStartX = findViewById(R.id.eTxtStartX);
        eTxtStartY = findViewById(R.id.eTxtStartY);
        spnOrientation = findViewById(R.id.direction_spinner);
        camera = findViewById(R.id.cameraView);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);

        if (!OpenCVLoader.initDebug()) Log.e(TAG, "Unable to load OpenCV");
        else Log.d(TAG, "OpenCV loaded");


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else avviaFotocamera();

        btnMain.setOnClickListener(v ->
        {
            Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(mainIntent);
        });

        btnManual.setOnClickListener(v ->
        {
            Intent manualIntent = new Intent(getBaseContext(), ManualActivity.class);
            startActivity(manualIntent);
        });
        //LinearLayout matrixView = findViewById(R.id.matrixView);


        btnSetMatrix.setOnClickListener(v ->
        {
            try {
                btnResetMatrix.setVisibility(LinearLayout.VISIBLE);
                btnSetMatrix.setVisibility(LinearLayout.GONE);
                dimM = Integer.parseInt(eTxtMatrixM.getText().toString());
                dimN = Integer.parseInt(eTxtMatrixN.getText().toString());
                int startX = Integer.parseInt(eTxtStartX.getText().toString()),
                        startY = Integer.parseInt(eTxtStartX.getText().toString());
                char orientation = String.valueOf(spnOrientation.getSelectedItem()).charAt(0);
                elementToggle(btnStart);
                elementToggle(btnSetMatrix);
                elementToggle(eTxtMatrixM);
                elementToggle(eTxtMatrixN);
                elementToggle(eTxtStartX);
                elementToggle(eTxtStartY);
                elementToggle(spnOrientation);
                PixelGridView pixelGrid = new PixelGridView(this);
                pixelGrid.setNumRows(dimM);
                pixelGrid.setNumColumns(dimN);

                gameField = new GameField(dimM, dimN, orientation, startX, startY);

                //Per fare i quadrati rossi
                //pixelGrid.changeCellChecked(2,3);

                //matrixView.addView(pixelGrid);
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        });

        btnResetMatrix.setOnClickListener(e -> {
            try {
                btnResetMatrix.setVisibility(LinearLayout.GONE);
                btnSetMatrix.setVisibility(LinearLayout.VISIBLE);
                elementToggle(btnStart);
                elementToggle(btnSetMatrix);
                elementToggle(eTxtMatrixM);
                elementToggle(eTxtMatrixN);
                elementToggle(eTxtStartX);
                elementToggle(eTxtStartY);
                elementToggle(spnOrientation);
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
                Prelude.trap(() -> ev3.run(this::legoMain));
                Toast.makeText(this, "Connessione stabilita con successo", Toast.LENGTH_SHORT).show();
                elementToggle(btnResetMatrix);
                elementToggle(btnStart);
                elementToggle(btnStop);
                elementToggle(btnMain);
                elementToggle(btnManual);
                tempoInizio = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Connessione non stabilita", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(v ->
        {
            robot.stopRLEngines();
            elementToggle(btnStop);
            attuale = System.currentTimeMillis() - tempoInizio;
            secondi = (int) (attuale / 1000) % 60;
            minuti = (int) (attuale / 60000) % 60;
            ore = (int) (attuale / 3600000) % 24;
            millisecondi = (int) attuale % 1000;
            aggiornaTimer(txtCronometro, String.format("%02d:%02d:%02d:%03d", ore, minuti, secondi, millisecondi));
            ev3.cancel();
            bluechan.close();
            elementToggle(btnStart);
            elementToggle(btnMain);
            elementToggle(btnManual);
        });
    }

    private void elementToggle(View v) {
        v.setEnabled(!v.isEnabled());
    }

    private void legoMain(EV3.Api api) {
        //final String TAG = Prelude.ReTAG("legoMain");
        robot = new Robot(api);
        while (!api.ev3.isCancelled()) {
        }


    }

    public void aggiornaTimer(TextView tv, String tempo) {
        runOnUiThread(() -> tv.setText(tempo));
    }

    public void avviaFotocamera() {
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setMaxFrameSize(640, 480);
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "Camera Started");
            }

            @Override
            public void onCameraViewStopped() {
                Log.d(TAG, "Camera Stopped");
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.gc();

                frame = inputFrame.rgba();
                ballFinder = new BallFinder(frame);
                lineFinder = new LineFinder(frame);
                //balls = ballFinder.findBalls();
                lines = lineFinder.findLines();

                /*for (Ball ball : balls) {
                    Point center = new Point(ball.center.x, ball.center.y);
                    int radius = (int) ball.radius;
                    Scalar color_rgb;

                    if (ball.color.equals("red")) color_rgb = new Scalar(255, 0, 0);
                    else if (ball.color.equals("blue")) color_rgb = new Scalar(0, 0, 255);
                    else if (ball.color.equals("yellow")) color_rgb = new Scalar(255, 255, 0);
                    else color_rgb = new Scalar(0, 0, 0);

                    Imgproc.circle(frame, center, radius, color_rgb, 2);

                    Imgproc.circle(frame, new Point(320, 240), 10, new Scalar(0, 0, 0), 2);
                    Imgproc.line(frame, new Point(310, 240), new Point(330, 240), new Scalar(0, 0, 0), 2);
                    Imgproc.line(frame, new Point(320, 230), new Point(320, 250), new Scalar(0, 0, 0), 2);

                    Log.e("ball center x ", String.valueOf(ball.center.x));
                    Log.e("ball center y ", String.valueOf(ball.center.y));
                    Log.e("ball radius ", String.valueOf(ball.radius));
                    Log.e("ball color ", ball.color);
                }*/

                for(Line line : lines){
                    Imgproc.line(frame, line.p1, line.p2, new Scalar(255, 0, 0), 3);
                }

                System.gc();

                return frame;
            }
        });

        camera.enableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        if (requestCode == 1 && grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED)
            avviaFotocamera();
    }
}

/*robot = new Robot(api);

    Future<Float> fDistance;
    Float distance;
    Future<LightSensor.Color> fCol;
    LightSensor.Color col;

    boolean isRunning = false;
    boolean isFind = false;
    boolean isSearching = false;
    boolean isApproached = false;
    boolean isStraightening = false;

    while (!api.ev3.isCancelled())
    {
        try
        {
            for (int i = 0; i < balls.size(); i++)
            {
                ball = balls.get(i);

                if (!isSearching)
                {
                    robot.startEngine('r',10, 'b');
                    robot.startEngine('l',10, 'f');
                    isSearching = true;
                }

                if (ball.center.y > 220 && ball.center.y < 260 && !ball.color.equals("yellow") && isSearching && !isFind)
                {
                    if (!isStraightening)
                    {
                        robot.startEngine('r',5, 'f');
                        robot.startEngine('l',5, 'b');
                        isStraightening = true;
                    }

                    if (ball.center.y > 230 && ball.center.y < 250)
                    {
                        robot.startEngine('r',10, 'f');
                        robot.startEngine('l',10, 'f');
                        robot.openHand(15);
                        isFind = true;
                    }
                }

                if (isFind && !isApproached)
                {
                    if (ball.center.y >= 220 && ball.center.y < 240)
                    {
                        robot.setMotorSpeed('r',5);
                        robot.setMotorSpeed('l',10);
                        //(int)(10 + (240 - ball.center.y + 240)/40)
                    }

                    if (ball.center.y == 240)
                    {
                        robot.setMotorSpeed('r',10);
                        robot.setMotorSpeed('l',10);
                    }

                    if (ball.center.y > 240 && ball.center.y <= 260)
                    {
                        robot.setMotorSpeed('l',5);
                        robot.setMotorSpeed('r',10);
                        //(int)(10 + (240 - ball.center.y)/40)
                    }

                    if (ball.radius >= 30)
                    {
                        robot.setMotorSpeed('r',10);
                        robot.setMotorSpeed('l',10);
                        isApproached = true;
                    }
                }

                if (isApproached)
                {
                    fDistance = robot.getDistance();
                    Thread.sleep(100);
                    distance = fDistance.get();
                    //Log.e("distance", String.valueOf(distance));

                    if (distance > 15 && distance <= 30 && !isRunning)
                    {
                        robot.setMotorSpeed('r',10);
                        robot.setMotorSpeed('l',10);
                        isRunning = true;
                    }

                    if (distance <= 15)
                    {
                        Thread.sleep(1000);
                        robot.stopRLEngine();
                        robot.closeHand(25);
                    }
                }

                balls.remove(ball);
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }*/
