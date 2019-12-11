package com.example.brainlegostormingapp;

public class Test1 extends Test {

    private int totMine;

    public Test1(Robot robot, GameField field, int mine){
        super(robot, field);
        this.totMine=mine;
    }

    @Override
    public void movement(){
        alignToOrigin();  //raggiungo l'origine
        robot.autoMove180Left();  //il robot si gira di 180°
        scanRight(); //inizia la scansione andando verso destra
    }

    public void alignToOrigin(){
        robot.autoMove90Left();
    }

    public boolean amIStraight(){
        return false;
    }

    public void straightMe(){

    }

    public boolean ballVisible(){
        return false;
    }

    public void scanLeft(){

    }

    public void scanRight(){

    }
}
