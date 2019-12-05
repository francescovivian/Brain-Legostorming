package com.example.brainlegostormingapp;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Motor extends TachoMotor
{
    public Motor(EV3.Api api, EV3.OutputPort op)
    {
        super(api, op);
    }

    public void startEngine(int speed, char direction)
    {
        try
        {
            if (direction == 'b')
            {
                this.setPolarity(TachoMotor.Polarity.FORWARD);
                this.setSpeed(speed);
                this.start();
            }
            if (direction == 'f')
            {
                this.setPolarity(TachoMotor.Polarity.BACKWARDS);
                this.setSpeed(speed);
                this.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopEngine()
    {
        try
        {
            this.stop();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void autoMoveHand(int speed, char direction)
    {
        try
        {
            if (direction == 'o')
            {
                this.setPolarity(TachoMotor.Polarity.FORWARD);
                this.setTimeSpeed(speed,0,3000,0,true);
            }
            if (direction == 'c')
            {
                this.setPolarity(TachoMotor.Polarity.BACKWARDS);
                this.setTimeSpeed(speed,0,3000,0,true);
            }
            this.waitUntilReady();
        }
        catch (IOException | InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }
}
