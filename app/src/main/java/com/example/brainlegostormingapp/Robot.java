package com.example.brainlegostormingapp;

import android.app.Activity;
import android.content.Context;
import android.view.SurfaceView;
import android.widget.TextView;

import com.example.brainlegostormingapp.ObjectOfInterest.Ball;
import com.example.brainlegostormingapp.ObjectOfInterest.Line;
import com.example.brainlegostormingapp.ObjectOfInterest.ObjectFind;
import com.example.brainlegostormingapp.Utility.Utility;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

import static com.example.brainlegostormingapp.Utility.Constant.*;

public class Robot {
    private Motor rm, lm, hand;

    private boolean minePickedUp;

    private final UltrasonicSensor us;
    private final LightSensor ls;

    private ArrayList<Ball> balls;
    private ArrayList<Line> lines;

    CameraBridgeViewBase camera;
    Camera myCamera;
    Mat frame;

    TextView distanza;
    Activity activity;

    double skew, maxAcceptedSkew;

    public Robot(EV3.Api api) {
        rm = new Motor(api, EV3.OutputPort.D);
        lm = new Motor(api, EV3.OutputPort.A);
        hand = new Motor(api, EV3.OutputPort.C);

        us = api.getUltrasonicSensor(EV3.InputPort._1);
        ls = api.getLightSensor(EV3.InputPort._4);

        try {
            rm.setType(TachoMotor.Type.LARGE);
            lm.setType(TachoMotor.Type.LARGE);
            hand.setType(TachoMotor.Type.MEDIUM);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Robot(EV3.Api api, CameraBridgeViewBase camera, TextView distanza, Activity activity) {
        this.distanza = distanza;
        this.activity = activity;
        this.camera = camera;
        myCamera = new Camera();
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setMaxFrameSize(640, 480);
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(myCamera);
        frame = new Mat();

        minePickedUp=false;

        rm = new Motor(api, EV3.OutputPort.D);
        lm = new Motor(api, EV3.OutputPort.A);
        hand = new Motor(api, EV3.OutputPort.C);

        us = api.getUltrasonicSensor(EV3.InputPort._1);
        ls = api.getLightSensor(EV3.InputPort._4);

        skew = 0;
        maxAcceptedSkew = 30;

        try {
            rm.setType(TachoMotor.Type.LARGE);
            lm.setType(TachoMotor.Type.LARGE);
            hand.setType(TachoMotor.Type.MEDIUM);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getMinePickedUp(){
        return this.minePickedUp;
    }

    public void setMinePickedUp(boolean mineStatus){
        this.minePickedUp=mineStatus;
    }

    public void autoMove90Right() {
        autoMove90('r');
    }

    public void autoMove90Left() {
        autoMove90('l');
    }

    public void autoMove180Right() {
        autoMove180('r');
    }

    public void autoMove180Left() {
        autoMove180('l');
    }

    public void autoMove90(char direction) {
        int step1 = 0, step2 = 1000, step3 = 0;
        try {
            if (direction == 'r') {
                lm.setPolarity(TachoMotor.Polarity.FORWARD);
                lm.setTimeSpeed(SPEED, step1, step2, step3, true);
                rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            } else if (direction == 'l') {
                rm.setPolarity(TachoMotor.Polarity.FORWARD);
                rm.setTimeSpeed(SPEED, step1, step2, step3, true);
                lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            }
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void autoMove180(char direction) {
        int step1 = 0, step2 = 2000, step3 = 0;
        try {
            if (direction == 'r') {
                lm.setPolarity(TachoMotor.Polarity.FORWARD);
                lm.setTimeSpeed(SPEED, step1, step2, step3, true);
                rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            } else if (direction == 'l') {
                rm.setPolarity(TachoMotor.Polarity.FORWARD);
                rm.setTimeSpeed(SPEED, step1, step2, step3, true);
                lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            }
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startEngine(char motor, char direction) {
        if (motor == 'r') rm.startEngine(SPEED, direction);
        if (motor == 'l') lm.startEngine(SPEED, direction);
        if (motor == 'h') hand.startEngine(SPEED, direction);
    }

    public void startRLEngines(char direction) {
        rm.startEngine(SPEED, direction);
        lm.startEngine(SPEED, direction);
    }

    public void startRLEngines(int speed, char direction) {
        rm.startEngine(speed, direction);
        lm.startEngine(speed, direction);
    }

    public void forwardOnce() {
        int step1 = 0, step2 = 3100, step3 = 0;
        try {
            //fixOrientation();
            lm.setPolarity(TachoMotor.Polarity.FORWARD);
            lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.setPolarity(TachoMotor.Polarity.FORWARD);
            rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void forwardOnceSearch() {
        int step1 = 0, step2 = 3100, step3 = 0;
        try {
            //fixOrientation();
            boolean isPresent = identifyBall();
            lm.setPolarity(TachoMotor.Polarity.FORWARD);
            lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.setPolarity(TachoMotor.Polarity.FORWARD);
            rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            if (isPresent)
            {
                catchBall();
            }
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backwardOnce() {
        int step1 = 0, step2 = 3100, step3 = 0;
        try {
            lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
            lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
            rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnRight(int speed) {
        rm.startEngine(speed, 'b');
        lm.startEngine(speed, 'f');
    }

    public void turnLeft(int speed) {
        rm.startEngine(speed, 'f');
        lm.startEngine(speed, 'b');
    }

    public void stopEngine(char motor) {
        if (motor == 'r') rm.stopEngine();
        else if (motor == 'l') lm.stopEngine();
        else if (motor == 'h') hand.stopEngine();
    }

    public void stopRLEngines() {
        rm.stopEngine();
        lm.stopEngine();
    }

    public void stopAllEngines() {
        rm.stopEngine();
        lm.stopEngine();
        hand.stopEngine();
    }

    public void setMotorSpeed(char motor, int speed) {
        try {
            if (motor == 'r') rm.setSpeed(speed);
            if (motor == 'l') lm.setSpeed(speed);
            if (motor == 'h') hand.setSpeed(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openHand(int speed) {
        hand.autoMoveHand(speed, 'o');
    }

    public void closeHand(int speed) {
        hand.autoMoveHand(speed, 'c');
    }

    public Future<Float> getDistance() {
        try {
            return us.getDistance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; //Questo non era previsto, potrebbe causare problemi
    }

    public Future<LightSensor.Color> getColor() {
        try {
            return ls.getColor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; //Questo non era previsto, potrebbe causare problemi
    }

    //funzione che mi dice se sono dritto
    public double amIStraight() {
        double skew = 0;
        //metodo che ottiene tutte le lines

        frame = myCamera.getFrame();
        ObjectFind objectFind = new ObjectFinder(frame).findObject("l", "b");
        balls = objectFind.getBalls();
        lines = objectFind.getLines();

        double dx,dy, weight;
        int ballsConsidered = 0;
        double totWeight = 0;

        for (Line line : lines) {
            //Sono nella metà destra
            if (line.p2.y < 240 && line.p1.y >= 240)
            {
                dx = Math.abs(line.p1.x - line.p2.x);
                dy = Math.abs(line.p1.y - line.p2.y);
                weight = dx+dy;
                totWeight += weight;
                skew += ((frame.height() - line.p2.y) * weight);
                ballsConsidered++;
            }

            //Sono nella metà sinistra
            if (line.p1.y > 240 && line.p2.y <= 240)
            {
                dx = Math.abs(line.p1.x - line.p2.x);
                dy = Math.abs(line.p1.y - line.p2.y);
                weight = dx+dy;
                totWeight += weight;
                skew += ((frame.height() - line.p2.y) * weight);
                ballsConsidered++;
            }
        }

        //elabora le linee per qualche frame
        //controlla che tutte le linee siano angolate correttamente
        //controlla che tutte le linee finiscano con l'angolazione corretta per il lato dello schermo
        //potrebbe ritornare la direzione in cui dovrebbe muoversi per raddrizzarsi
        frame.release();
        //skew /= ballsConsidered;
        skew /= totWeight;
        return skew;
    }

    public boolean identifyBall()
    {
        try {
            int count=0;
            for(int i=0;i<10;i++){
                Float dist=this.getDistance().get();
                if(dist<35)
                    count++;
                System.out.println(dist);
            }
            Float distance = this.getDistance().get();
            activity.runOnUiThread(() -> distanza.setText(distance.toString()));
            return !this.minePickedUp && count >=8;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false; // Non dovrebbe mai arrivare qua
    }

    public void catchBall()
    {
        Utility.sleep(2000);
        this.closeHand(25);
        this.setMinePickedUp(true);
    }

    public void slightestMove(double skew)
    {
        int step1 = 0, step2 = (int) skew, step3 = 0;
        try {//Destra
            if (skew > 0) {
                lm.setPolarity(TachoMotor.Polarity.FORWARD);
                lm.setTimeSpeed(2, step1, step2, step3, true);
                rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                rm.setTimeSpeed(2, step1, step2, step3, true);
            }//Sinistra
            else if (skew < 0) {
                rm.setPolarity(TachoMotor.Polarity.FORWARD);
                rm.setTimeSpeed(2, step1, step2, step3, true);
                lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                lm.setTimeSpeed(2, step1, step2, step3, true);
            }
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void straightenMe(double skew){
        //usa valore di amIstraight per raddrizzarsi molto lentamente per un istante
        //poi continua a luppare amIStraight finche non sono sufficentemente dritto
        if (skew != 0) this.slightestMove(skew);
    }

    //si raddrizza sulla singola cella
    public void fixOrientation(){
        skew = this.amIStraight();
        while(skew > maxAcceptedSkew) {
            straightenMe(skew);
            skew = this.amIStraight();
        }
    }
}
