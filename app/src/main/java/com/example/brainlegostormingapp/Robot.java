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
import it.unive.dais.legodroid.lib.plugs.GyroSensor;

import static com.example.brainlegostormingapp.Utility.Constant.*;

public class Robot {
    private Motor rm, lm, hand;

    private boolean minePickedUp;

    private final UltrasonicSensor us;
    private final LightSensor ls;
    private final GyroSensor gs;
    private Float scartogs = null;


    private ArrayList<Ball> balls;
    private ArrayList<Line> lines;

    //CameraBridgeViewBase camera;
    //Camera myCamera;
    //Mat frame;

    //Activity activity;
    //TextView txtDistance;

    private PixelGridView pixelGrid;

    private double skew, maxAcceptedSkew;

    public Robot(EV3.Api api) {
        rm = new Motor(api, EV3.OutputPort.D);
        lm = new Motor(api, EV3.OutputPort.A);
        hand = new Motor(api, EV3.OutputPort.C);

        us = api.getUltrasonicSensor(EV3.InputPort._1);
        ls = api.getLightSensor(EV3.InputPort._4);
        gs = api.getGyroSensor(EV3.InputPort._2);

        try {
            rm.setType(TachoMotor.Type.LARGE);
            lm.setType(TachoMotor.Type.LARGE);
            hand.setType(TachoMotor.Type.MEDIUM);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Robot(EV3.Api api, PixelGridView pixelGrid, Activity activity, TextView txtDistance) {
        /*this.activity = activity;
        this.txtDistance = txtDistance;
        this.pixelGrid = pixelGrid;

        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setMaxFrameSize(640, 480);
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(myCamera);
        frame = new Mat();*/

        minePickedUp=false;

        rm = new Motor(api, EV3.OutputPort.D);
        lm = new Motor(api, EV3.OutputPort.A);
        hand = new Motor(api, EV3.OutputPort.C);

        us = api.getUltrasonicSensor(EV3.InputPort._1);
        ls = api.getLightSensor(EV3.InputPort._4);
        gs = api.getGyroSensor(EV3.InputPort._2);

        skew = 0;
        maxAcceptedSkew = 300;

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
        fixOrientationGS();
        autoMove90('r');
    }

    public void autoMove90Left() {
        fixOrientationGS();
        autoMove90('l');
    }

    public void autoMove180Right() {
        fixOrientationGS();
        autoMove180('r');
    }

    public void autoMove180Left() {
        fixOrientationGS();
        autoMove180('l');
    }

    public void autoMove90(char direction) {
        int step1 = 0, step2 = 1000, step3 = 0;
        try {
            fixOrientationGS();
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
            fixOrientationGS();
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
            fixOrientationGS();
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

    public void forwardHalf() {
        int step1 = 0, step2 = 1500, step3 = 0;
        try {
            fixOrientationGS();
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
            fixOrientationGS();
            boolean isPresent = identifyBall();
            lm.setPolarity(TachoMotor.Polarity.FORWARD);
            lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.setPolarity(TachoMotor.Polarity.FORWARD);
            rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            if (isPresent)
                catchBall();
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backwardOnce() {
        int step1 = 0, step2 = 3100, step3 = 0;
        try {
            fixOrientationGS();
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

    public void backwardHalf() {
        int step1 = 0, step2 = 1500, step3 = 0;
        try {
            fixOrientationGS();
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

    public Future<Float> getAngle() {
        try {
            return gs.getAngle();
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

    public boolean identifyBall()
    {
        try {
            int count = 0;
            for (int i = 0; i < 10; i ++){
                Float dist = this.getDistance().get();
                if (dist<35)
                    count++;
            }
            return !this.minePickedUp && count >= 8;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false; // Non dovrebbe mai arrivare qua
    }

    public Float identifyOrientation() {
        try {
            Float angle = this.getAngle().get();
            if (scartogs == null)
                scartogs = angle;
            return angle - scartogs;
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return 0.f;
    }

    public void catchBall() {
        Utility.sleep(2000);
        this.closeHand(25);
        this.setMinePickedUp(true);
    }

    public void minaCheck(int c, int r) {
        pixelGrid.cellCheck(c,r,"MINA");
    }
    public void straightenMeGS(float angle) {
        int step1 = 0, step2 = 400, step3 = 0;
        try {//Destra
            if (angle > 0) {
                step2 = -step2; //ricavo il valore postivo
                lm.setPolarity(TachoMotor.Polarity.FORWARD);
                lm.setTimeSpeed(4, step1, step2, step3, true);
                rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                rm.setTimeSpeed(4, step1, step2, step3, true);
            }//Sinistra
            else if (angle < 0) {
                rm.setPolarity(TachoMotor.Polarity.FORWARD);
                rm.setTimeSpeed(4, step1, step2, step3, true);
                lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                lm.setTimeSpeed(4, step1, step2, step3, true);
            }
            rm.waitCompletion();
            lm.waitCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fixOrientationGS() {
        Float angle = 0.f;
        do {
            angle = identifyOrientation() % 90;
            if (angle > 45)
                angle -= 90;
            else if (angle < -45)
                angle += 90;

            if (angle <= -2 || angle >= 2) {
                Utility.sleep(500);
                straightenMeGS(angle);
                Utility.sleep(250);
            }
        }while (angle <= -2 || angle >= 2);
    }

    //region Vecchio Raddrizzamento
/*
    //funzione che mi dice se sono dritto
    public void amIStraight() {
        skew = 0;
//        double dx,dy, weight;
        int linesConsidered = 0;
//        double totWeight = 0;

        for (int i = 0; i < 10; i++) {
            frame = myCamera.getFrame();
            //metodo che ottiene tutte le lines
            ObjectFind objectFind = new ObjectFinder(frame).findObject("l", "b");
//            balls = objectFind.getBalls();
            lines = objectFind.getLines();

            for (Line line : lines) {
                //Sono nella metà destra
                if (line.p1.y > 240 && line.p2.y <= 240) {
//                    dx = Math.abs(line.p1.x - line.p2.x);
//                    dy = Math.abs(line.p1.y - line.p2.y);
//                    weight = dx + dy;
//                    totWeight += weight;
                    skew += ((frame.height()/2 - line.p2.y) * weight);
                    linesConsidered++;
                }

                //Sono nella metà sinistra
                if (line.p1.y < 240 && line.p2.y >= 240) {
//                    dx = Math.abs(line.p1.x - line.p2.x);
//                    dy = Math.abs(line.p1.y - line.p2.y);
//                    weight = dx + dy;
//                    totWeight += weight;
                    skew += ((frame.height()/2 - line.p2.y) * weight);
                    linesConsidered++;
                }
            }

            //elabora le linee per qualche frame
            //controlla che tutte le linee siano angolate correttamente
            //controlla che tutte le linee finiscano con l'angolazione corretta per il lato dello schermo
            //potrebbe ritornare la direzione in cui dovrebbe muoversi per raddrizzarsi
            frame.release();
            Utility.sleep(2000);
        }
//        skew /= totWeight;
        skew /= linesConsidered;
        skew *= 20;
    }

    public void slightestMove()
    {
        int step1 = 0, step2 = (int) skew, step3 = 0;
        try {//Destra
            if (skew < 0) {
                step2 = -step2; //ricavo il valore postivo
                lm.setPolarity(TachoMotor.Polarity.FORWARD);
                lm.setTimeSpeed(2, step1, step2, step3, true);
                rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                rm.setTimeSpeed(2, step1, step2, step3, true);
            }//Sinistra
            else if (skew > 0) {
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

    public void straightenMe(){
        //usa valore di amIstraight per raddrizzarsi molto lentamente per un istante
        //poi continua a luppare amIStraight finche non sono sufficentemente dritto
        if (skew != 0) this.slightestMove();
    }

    //si raddrizza sulla singola cella
    public void fixOrientation(){
        this.amIStraight();
        while(Math.abs(skew) > maxAcceptedSkew) {
            this.straightenMe();
            this.amIStraight();
        }
    }
*/
    //endregion
}
