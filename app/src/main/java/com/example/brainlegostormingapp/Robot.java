package com.example.brainlegostormingapp;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

public class Robot {
    private Motor rm, lm, hand;

    private final UltrasonicSensor us;
    private final LightSensor ls;

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

    public void autoMove90Right() {
        autoMove90('r');
    }

    public void autoMove90Left() {
        autoMove90('l');
    }

    public void autoMove90(char direction) {
        try {
            if (direction == 'r') {
                lm.setPolarity(TachoMotor.Polarity.FORWARD);
                lm.setTimeSpeed(20, 0, 3000, 0, true);
                rm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                rm.setTimeSpeed(20, 0, 3000, 0, true);
            }
            if (direction == 'l') {
                rm.setPolarity(TachoMotor.Polarity.FORWARD);
                rm.setTimeSpeed(20, 0, 3000, 0, true);
                lm.setPolarity(TachoMotor.Polarity.BACKWARDS);
                lm.setTimeSpeed(20, 0, 3000, 0, true);
            }
            rm.waitUntilReady();
            lm.waitUntilReady();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void startEngine(char motor, int speed, char direction) {
        if (motor == 'r') rm.startEngine(speed, direction);
        if (motor == 'l') lm.startEngine(speed, direction);
        if (motor == 'h') hand.startEngine(speed, direction);
    }

    public void startRLEngines(int speed, char direction) {
        rm.startEngine(speed, direction);
        lm.startEngine(speed, direction);
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
}
