package com.example.brainlegostormingapp;

public class Test implements TestInterface{

    public Robot robot;
    public GameField field;

    public Test(Robot robot, GameField field){
        this.robot=robot;
        this.field=field;
    }

    public void movement(){
        robot.autoMove90Left();
    }

    public void start(){
        movement();
    }
}
