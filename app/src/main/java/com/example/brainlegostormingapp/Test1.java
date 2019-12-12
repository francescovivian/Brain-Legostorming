package com.example.brainlegostormingapp;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Test1 extends Test {

    private int totMine;
    private int securedMine;

    public Test1(Robot robot, GameField field, int mine){
        super(robot, field);
        this.totMine=mine;
    }

    @Override
    public void movement(){
        initialize();
        //robot.autoMove180Left();  //il robot si gira di 180°
        //scanRight(); //inizia la scansione andando verso destra
        //while(securedMine < totMine){
            //fa i vari movimenti
        //}
    }

    public void initialize(){
        securedMine = 0;
        alignToOrigin();  //raggiungo l'origine
    }

    //si raddrizza sulla singola cella
    public void fixOrientation(){
        double skew;
        double maxAcceptedSkew = 30;
        skew = robot.amIStraight();
        while(skew > maxAcceptedSkew) {
            straightenMe(skew);
            skew = robot.amIStraight();
        }
    }

    public void sleep(int msec){
        try{
            Thread.sleep(msec);
        }catch(InterruptedException e ){
            e.printStackTrace();
        }
    }

    public void alignToOrigin(){   //processa cella fino all'origine
        Future<Float> fDistance;
        Float distance;

        try {
            robot.openHand(15);
            robot.autoMove90Left();
            sleep(5000);
            for (int i = field.getStartPosition().getX(); i > 0; i--) {
                robot.forwardOnce();
                fDistance = robot.getDistance();
                if (fDistance.get() < 35)
                {
                    sleep(1500);
                    robot.closeHand(25);
                }
                sleep(3500);
            }
            robot.autoMove180Right();
            sleep(2500);
            robot.openHand(15);
        }
        catch(ExecutionException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void processNextCell(){ //processa la cella seguente
        if(ballNextCell()){
            storeBall();
        }
        robot.forwardOnce();
    }

    public void storeBall(){
        //codice per portare la pallina nella zona sicura
        backToLastMinePos();
    }

    public void backToLastMinePos(){
        //torna alla posizione in cui ha trovato l'ultima mina
    }

    public void straightenMe(double spostamento){
        //usa valore di amIstraight per raddrizzarsi molto lentamente per un istante
        //poi continua a luppare amIStraight finche non sono sufficentemente dritto
    }

    public boolean ballNextCell(){
        //mi dice se nella prossima cella c'è una pallina
        return false;
    }

    public void scanLeft(){

    }

    public void scanRight(){

    }
}
