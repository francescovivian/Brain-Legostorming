package com.example.brainlegostormingapp.Tests;

import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;
import com.example.brainlegostormingapp.Tests.Test;
import com.example.brainlegostormingapp.Utility.Utility;

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
        scan();
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

    public void alignToOrigin(){   //processa cella fino all'origine
        //robot.openHand(15);
        robot.autoMove90Left();
        Utility.sleep(5000);
        for (int i = field.getStartPosition().getX(); i > 0; i--) {
            robot.forwardOnce();
            field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            Utility.sleep(5000);
        }
        robot.autoMove180Right();
        Utility.sleep(5000);
        //robot.openHand(15);
    }

    public void processNextCell(){ //processa la cella seguente
        if(ballNextCell())
            storeBall();
        robot.forwardOnce();
    }

    //metodo per portare la pallina nella zona sicura
    public void storeBall(){
        backToStart();
        secureMine();
        backToLastMinePos();
    }

    public void secureMine(){

    }


    public void straightenMe(double spostamento){
        //usa valore di amIstraight per raddrizzarsi molto lentamente per un istante
        //poi continua a luppare amIStraight finche non sono sufficentemente dritto
    }

    //boolean se nella prossima cella c'è una pallina
    public boolean ballNextCell(){
        return false;
    }

    public void scan(){
        for(int i=0;i<field.getRow();i++){
            if(i%2==0)
                scanRight();
            else
                scanLeft();
        }
    }
    public void scanLeft(){
        for(int i=0;i<field.getColumn()-1;i++){ //scorro tutta la riga andando verso sinistra
            robot.forwardOnce();
            field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            Utility.sleep(5000);
        }
        if(field.getRobotPosition().getY()<field.getRow()-1) { // se ho una riga sopra
            robot.autoMove90Right(); //arrivato infondo alla riga giro a destra per salire
            Utility.sleep(5000);
            robot.forwardOnce(); //salgo nella riga sopra
            field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY() + 1);
            Utility.sleep(5000);
            robot.autoMove90Right(); //mi giro pronto per scorrere la nuova riga
            Utility.sleep(5000);
        }
    }

    public void scanRight() {
        for (int i = 0; i < field.getColumn() - 1; i++) { //scorro tutta la riga andando verso destra
            robot.forwardOnce();
            field.setRobotPosition(field.getRobotPosition().getX() + 1, field.getRobotPosition().getY());
            Utility.sleep(5000);
        }
        if (field.getRobotPosition().getY() < field.getRow() - 1) { // se ho una riga sopra
            robot.autoMove90Left(); //arrivato infondo alla riga giro a sinistra per salire
            Utility.sleep(5000);
            robot.forwardOnce(); //salgo nella riga sopra
            field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY() + 1);
            Utility.sleep(5000);
            robot.autoMove90Left(); //mi giro pronto per scorrere la nuova riga
            Utility.sleep(5000);
        }
    }

    public void backToStart(){  // si presuppone che il robot sia già girato verso il "basso"
        for(int i=field.getRobotPosition().getY();i>=0;i--){ //scendo fino alla base della griglia
            robot.forwardOnce();
            Utility.sleep(5000);
            field.setRobotPosition(field.getRobotPosition().getX(),field.getRobotPosition().getY()-1);
        }
        if(field.getRobotPosition().getX()>field.getStartPosition().getX()){ // sono a destra rispetto allo start
            robot.autoMove90Right();
            while(field.getRobotPosition().getX()>field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            }
            robot.autoMove90Left();
        }
        else{ //sono a sinistra rispetto allo start
            robot.autoMove90Left();
            while(field.getRobotPosition().getX()<field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
            }
            robot.autoMove90Right();
        }
    }

    //si aspetta che il robot sia rivolto verso l'alto
    public void backToLastMinePos(){ //il robot arriva rivolto verso "l'alto"
        if(field.getRobotPosition().getX()>field.getLastMinePosition().getX()){ // sono a destra rispetto alla posizione dell'ultima mina
            robot.autoMove90Left();
            while(field.getRobotPosition().getX()>field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            }
            robot.autoMove90Right();
        }
        else{ //sono a sinistra rispetto alla posizione dell'ultima mina
            robot.autoMove90Right();
            while(field.getRobotPosition().getX()<field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
            }
            robot.autoMove90Left();
        }
        for(int i=0;i<=field.getLastMinePosition().getY();i++){ //salgo fino alla posizione dell'ultima mina
            robot.forwardOnce();
            Utility.sleep(5000);
            field.setRobotPosition(field.getRobotPosition().getX(),field.getRobotPosition().getY()+1);
        }
    }
}
