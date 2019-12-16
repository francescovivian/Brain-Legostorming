package com.example.brainlegostormingapp;

import com.example.brainlegostormingapp.ObjectOfInterest.Ball;
import com.example.brainlegostormingapp.ObjectOfInterest.Line;
import com.example.brainlegostormingapp.ObjectOfInterest.ObjectFind;
import com.example.brainlegostormingapp.Utility.Utility;

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

    private final UltrasonicSensor us;
    private final LightSensor ls;

    private ArrayList<Ball> balls;
    private ArrayList<Line> lines;

    Camera camera;
    Mat frame;

    public Robot(EV3.Api api) {
        //camera = new Camera();
        frame = new Mat();

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
            lm.setPolarity(TachoMotor.Polarity.FORWARD);
            lm.setTimeSpeed(SPEED, step1, step2, step3, true);
            rm.setPolarity(TachoMotor.Polarity.FORWARD);
            rm.setTimeSpeed(SPEED, step1, step2, step3, true);
            identifyBall();
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

        frame = camera.getFrame();
        ObjectFind objectFind = new ObjectFinder(frame).findObject("l", "b");
        balls = objectFind.getBalls();
        lines = objectFind.getLines();

        frame.release();

        //elabora le linee per qualche frame
        //controlla che tutte le linee siano angolate correttamente
        //controlla che tutte le linee finiscano con l'angolazione corretta per il lato dello schermo
        //potrebbe ritornare la direzione in cui dovrebbe muoversi per raddrizzarsi
        return skew;
    }

    public void identifyBall()
    {
        try {
            if (this.getDistance().get() < 35) {
                Utility.sleep(2000);
                this.closeHand(25);
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }
}
