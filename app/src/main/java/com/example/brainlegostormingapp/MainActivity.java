package com.example.brainlegostormingapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;

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

    private TachoMotor mdx;
    private TachoMotor msx;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contesto = this;

        testoCronometro = findViewById(R.id.cronometro);
        start = findViewById(R.id.startButton);
        stop = findViewById(R.id.stopButton);
        reset = findViewById(R.id.resetButton);

        try
        {
            BluetoothConnection.BluetoothChannel conn = new BluetoothConnection("EV3BL").connect();

            EV3 ev3 = new EV3(conn);

            start.setOnClickListener(v ->
            {
                if (cronometro == null)
                {
                    cronometro = new Cronometro(contesto);
                    threadCronometro = new Thread(cronometro);
                    threadCronometro.start();
                    cronometro.start();
                }
                Prelude.trap(() -> ev3.run(this::legoMain));
                try
                {
                    mdx.setPower(25);
                    mdx.start();
                    mdx.setPower(25);
                    msx.start();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
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
                try
                {
                    mdx.stop();
                    msx.stop();
                    /*mdx.brake();
                    msx.brake();*/
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                ev3.cancel();
            });

            reset.setOnClickListener(v ->
            {
                if(cronometro == null)
                {
                    testoCronometro.setText("00:00:00:000");
                }
            });
        }
        catch (IOException e)
        {
            Log.e(TAG, "Fatal error: Cannot connect to EV3");
            e.printStackTrace();
        }
    }

    private void legoMain(EV3.Api api)
    {
        final String TAG = Prelude.ReTAG("legoMain");

        //final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
        final UltrasonicSensor su = api.getUltrasonicSensor(EV3.InputPort._2);
        //final GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._4);

        mdx = api.getTachoMotor(EV3.OutputPort.A);
        msx = api.getTachoMotor(EV3.OutputPort.D);

        while (!api.ev3.isCancelled())
        {
            try
            {
                Future<Float> distance = su.getDistance();
                Future<Float> posdx = mdx.getPosition();
                Future<Float> possx = msx.getPosition();
                Future<Float> speeddx = mdx.getSpeed();
                Future<Float> speedsx = msx.getSpeed();
                //System.out.println(su.getDistance());
                System.out.println(testoCronometro.getText());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void aggiornaTimer(String tempo)
    {
        runOnUiThread(() -> testoCronometro.setText(tempo));
    }
}
