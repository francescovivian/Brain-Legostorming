package com.example.brainlegostormingapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.Visibility;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainlegostormingapp.Camera;
import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.MatrixField.MyRecyclerViewAdapter;
import com.example.brainlegostormingapp.ObjectFinder;
import com.example.brainlegostormingapp.ObjectOfInterest.Ball;
import com.example.brainlegostormingapp.ObjectOfInterest.Line;
import com.example.brainlegostormingapp.ObjectOfInterest.ObjectFind;
import com.example.brainlegostormingapp.PixelGridView;
import com.example.brainlegostormingapp.R;
import com.example.brainlegostormingapp.Robot;
import com.example.brainlegostormingapp.Tests.Test1;
import com.example.brainlegostormingapp.Tests.Test2;
import com.example.brainlegostormingapp.Utility.Utility;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.util.Prelude;

public class AutoActivity extends AppCompatActivity /*implements MyRecyclerViewAdapter.ItemClickListener*/ {
    private static final String TAG = "AutoActivity";
    private long tempoInizio, attuale;
    private int secondi, minuti, ore, millisecondi;

    int choosen;

    private BluetoothConnection.BluetoothChannel bluechan;
    private EV3 ev3;
    private Robot robot;
    private Mat frame;
    private ArrayList<Ball> balls;
    private ArrayList<Line> lines;
    private GameField gameField;
    private Test1 test1;
    private Test2 test2;


    private int dimR, dimC, startX, startY, mine;
    private char orientation;

    private CameraBridgeViewBase camera;
    private Camera myCamera;
    LinearLayout matrixView;
    private TextView txtCronometro, txtDistance;
    private Button btnMain, btnManual, btnStart, btnStop, btnSetMatrix, btnResetMatrix;
    private EditText eTxtMatrixR, eTxtMatrixC, eTxtStartX, eTxtStartY, eTxtMine;
    private Spinner spnOrientation;
    PixelGridView pixelGrid;

    //MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region initiate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        getSupportActionBar().hide();

        choosen = getIntent().getIntExtra("choosen", 0);

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

        //endregion

        //region FINDVIEW
        txtCronometro = findViewById(R.id.cronometro);
        txtDistance = findViewById(R.id.distance);
        btnMain = findViewById(R.id.mainButton);
        btnManual = findViewById(R.id.manualButton);
        btnStart = findViewById(R.id.btnStartButton);
        btnStop = findViewById(R.id.btnStopButton);
        btnSetMatrix = findViewById(R.id.btnSetDimMatrix);
        btnResetMatrix = findViewById(R.id.btnResetDimMatrix);
        eTxtMatrixR = findViewById(R.id.eTxtDimR);
        eTxtMatrixC = findViewById(R.id.eTxtDimC);
        eTxtStartX = findViewById(R.id.eTxtStartX);
        eTxtStartY = findViewById(R.id.eTxtStartY);
        matrixView = findViewById(R.id.matrixView);
        spnOrientation = findViewById(R.id.direction_spinner);
        spnOrientation.setSelection(1);
        camera = findViewById(R.id.cameraView);
        eTxtMine = findViewById(R.id.eTxtMine);
        //endregion


        if (!OpenCVLoader.initDebug())
            Log.e(TAG, "Unable to load OpenCV");
        else
            Log.d(TAG, "OpenCV loaded");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else avviaFotocamera();

        btnMain.setOnClickListener(v -> {
            Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(mainIntent);
        });

        btnManual.setOnClickListener(v -> {
            Intent manualIntent = new Intent(getBaseContext(), ManualActivity.class);
            startActivity(manualIntent);
        });

        btnSetMatrix.setOnClickListener(v -> {
            txtCronometro.setVisibility(View.GONE);
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
                pixelGrid = new PixelGridView(this, Character.toLowerCase(orientation));
                pixelGrid.setNumRows(dimR);
                pixelGrid.setNumColumns(dimC);

                gameField = new GameField(dimR, dimC, orientation, startX, startY);

                //Per fare i quadrati rossi
                //pixelGrid.changeCellChecked(0,2,1);

                matrixView.addView(pixelGrid);
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        });

        btnResetMatrix.setOnClickListener(e -> {
            try {
                matrixView.removeAllViews();
                eTxtMatrixR.setText("0");
                eTxtMatrixC.setText("0");
                eTxtStartX.setText("0");
                eTxtStartY.setText("0");
                spnOrientation.setSelection(1);
                Utility.elementVisibilityToggle(btnResetMatrix,btnStart,btnSetMatrix);
                Utility.elementToggle(eTxtMatrixR, eTxtMatrixC, eTxtStartX, eTxtStartY, eTxtMine, spnOrientation);
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
                Utility.elementToggle(btnMain, btnManual);
                Utility.elementVisibilityToggle(btnStart,btnStop,btnResetMatrix);
                tempoInizio = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Connessione non stabilita", Toast.LENGTH_SHORT).show();
            }
            if(choosen == 2){
                test2.startDiscovery();
            }
        });

        //termina la prova, calcola e mostra il tempo finale e TODO mostra la matrice
        btnStop.setOnClickListener(v ->
        {
            robot.stopRLEngines();
            attuale = System.currentTimeMillis() - tempoInizio;
            secondi = (int) (attuale / 1000) % 60;
            minuti = (int) (attuale / 60000) % 60;
            ore = (int) (attuale / 3600000) % 24;
            millisecondi = (int) attuale % 1000;
            runOnUiThread(() -> txtCronometro.setText(String.format("%02d:%02d:%02d:%03d", ore, minuti, secondi, millisecondi)));
            //ev3.cancel();
            //bluechan.close();
            Utility.elementToggle(btnMain, btnManual);
            Utility.elementVisibilityToggle(btnStop,txtCronometro,btnSetMatrix);
        });

        /*
        //todo la matrice viene inizializzata qui. ma poi andrebbe aggiornata
        // (oppure inizializata in un onlick da qualche altra parte)
        //data to populate the RecyclerView with
        String[] data = {"0","0","0","MINA","0","0","0","ROBOT","0",};

        //TODO sto codice va spostato ma intanto teniamolo qui finche ci lavoriamo sopra
        //set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.matrixField);
        //numberOfColumns andrebbe cambiato con il valore di colonne della matrice
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new MyRecyclerViewAdapter(this, data);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);*/
    }

    /*
    //TODO: qui va messa la logica che si vuole implementare per un click sulla matrice
    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked " + adapter.getItem(position) + ", which is at cell position " + position);
    }*/

    private void legoMain(EV3.Api api) {
        //final String TAG = Prelude.ReTAG("legoMain");

        robot = new Robot(api, camera, pixelGrid, this, txtDistance);

        //selezione della prova
        if (choosen == 1) {
            test1 = new Test1(robot, gameField, mine);
            test1.start();
        }
        /*if (choosen == 2)
        {
            test2 = new Test2(robot, gameField,mine);
            test2.start();
        }
        if (choosen == 3)
        {
            test3 = new Test3(robot, gameField,mine);
            test3.start();
        }*/

        //suono di fine prova
        Utility.sleep(5000);
        Utility.playMp3Audio(getApplicationContext(),"mammamia.mp3");
        //Utility.sleep(5000);
        //btnStop.performClick();
    }

    public void avviaFotocamera() {
        myCamera = new Camera();
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setMaxFrameSize(640, 480);
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(myCamera);
        /*frame = myCamera.getFrame();
        ObjectFind objectFind = new ObjectFinder(frame).findObject("l", "b");
        balls = objectFind.getBalls();
        lines = objectFind.getLines();*/
        camera.enableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        if (requestCode == 1 && grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED)
            avviaFotocamera();
    }
}

/*
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
            Utility.sleep(100);

            frame = inputFrame.rgba();
            ObjectFind objectFind = new ObjectFinder(frame).findObject("l", "b");
            balls = objectFind.getBalls();
            lines = objectFind.getLines();

            Imgproc.circle(frame, new Point(320, 240), 10, new Scalar(0, 0, 0), 2);
            Imgproc.line(frame, new Point(310, 240), new Point(330, 240), new Scalar(0, 0, 0), 2);
            Imgproc.line(frame, new Point(320, 230), new Point(320, 250), new Scalar(0, 0, 0), 2);

            //Imgproc.line(frame, new Point(580, 0), new Point(580, 480), new Scalar(0, 255, 0), 2);
            //Imgproc.line(frame, new Point(620, 0), new Point(620, 480), new Scalar(0, 255, 0), 2);

            //Imgproc.line(frame, new Point(0, 180), new Point(640, 180), new Scalar(0, 255, 0), 2);
            //Imgproc.line(frame, new Point(0, 200), new Point(640, 200), new Scalar(0, 255, 0), 2);

            //Imgproc.line(frame, new Point(0, 280), new Point(640, 280), new Scalar(0, 255, 0), 2);
            //Imgproc.line(frame, new Point(0, 300), new Point(640, 300), new Scalar(0, 255, 0), 2);

            for (Ball ball : balls) {
                Point center = new Point(ball.center.x, ball.center.y);
                int radius = (int) ball.radius;
                Scalar color_rgb;

                if (ball.color.equals("red")) color_rgb = new Scalar(255, 0, 0);
                else if (ball.color.equals("blue")) color_rgb = new Scalar(0, 0, 255);
                else if (ball.color.equals("yellow")) color_rgb = new Scalar(255, 255, 0);
                else color_rgb = new Scalar(0, 0, 0);

                Imgproc.circle(frame, center, radius, color_rgb, 2);

                //Log.e("ball center x ", String.valueOf(ball.center.x));
                //Log.e("ball center y ", String.valueOf(ball.center.y));
                //Log.e("ball radius ", String.valueOf(ball.radius));
                //Log.e("ball color ", ball.color);
            }

            for (Line line : lines) {
                Imgproc.circle(frame, line.p1, 10, new Scalar(255, 200, 0), 2);
                Imgproc.circle(frame, line.p2, 10, new Scalar(0, 100, 255), 2);
                Imgproc.line(frame, line.p1, line.p2, new Scalar(255, 0, 0), 2);

                //Log.e("line p1 x ", String.valueOf(line.p1.x));
                //Log.e("line p1 y ", String.valueOf(line.p1.y));
                //Log.e("line p2 x ", String.valueOf(line.p2.x));
                //Log.e("line p2 y ", String.valueOf(line.p2.y));
            }

            return frame;
        }
    });

    camera.enableView();*/

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
