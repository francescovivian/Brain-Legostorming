package com.example.brainlegostormingapp.Tests;

import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;
import com.example.brainlegostormingapp.Tests.Test;
import com.example.brainlegostormingapp.Utility.Utility;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Test1 extends Test {


    private boolean testEnded;

     public Test1(Robot robot, GameField field, int mine){
        super(robot, field);
        this.totMine=mine;
        this.testEnded=false;
    }

    @Override
    public void movement(){
        //effettuo movimenti solo se ci sono effettivamente mine da raccogliere
        if (totMine > 0) {
            initialize();
            scan();
        }
    }

    public void initialize(){
        securedMine = 0;
        alignToOrigin();  //raggiungo l'origine
    }

    //processa celle dalla posizione di partenza fino all'origine della matrice (posizione [0][0])
    public void alignToOrigin(){
        robot.openHand(15);
        Utility.sleep(5000);
        robot.autoMove90Left();
        Utility.sleep(5000);
        for (int i = field.getStartPosition().getX(); i > 0; i--) {
            if(!testEnded) {
                robot.forwardOnceSearch();
                field.setRobotPosition(field.getRobotPosition().getX() - 1, field.getRobotPosition().getY());
                Utility.sleep(5000);
                if (robot.getMinePickedUp() == true) { //ho raccolto una mina nell'ultimo avanzamento
                    field.setLastMinePosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
                    robot.changeCellChecked(field.getRobotPosition().getX(),field.getRobotPosition().getY(),1);
                    robot.autoMove90Left();
                    Utility.sleep(5000);
                    storeBall();        //la deposito nella zona sicura
                    robot.autoMove90Left();
                    Utility.sleep(5000);
                }
            }
        }
        if(!testEnded) {
            robot.autoMove180Right();       //mi giro pronto per scansionare tutto il campo
            Utility.sleep(5000);
        }
    }

    //processa la cella seguente
    public void processNextCell(){
        if(ballNextCell())
            storeBall();
        robot.forwardOnceSearch();
    }

    //metodo per portare la pallina nella zona sicura
    public void storeBall(){
        backToStart();                  //torna alla posizione di partenza
        secureMine();                   //deposita la mina
        if(securedMine>=totMine)        //se ho finito di raccogliere mine
            testEnded=true;             //TODO ma e se invece facessimo terminare la prova senza dover usare una miriade di if ovunque? (ditemi se e' fattibile)
        else                            //altrimenti torno alla posizione in cui ero
            backToLastMinePos();
    }

    public void secureMine(){
        robot.openHand(15);       //apre la mano
        robot.forwardHalf();            //avanza un poco
        Utility.sleep(5000);
        this.securedMine++;
        robot.setMinePickedUp(false);
        robot.backwardHalf();           //torna indietro
        Utility.sleep(5000);
        robot.autoMove180Right();       //si gira di 180°
    }

    //boolean se nella prossima cella c'è una pallina
    public boolean ballNextCell(){
        return false;
    }

    //scansiona il campo
    public void scan(){
        for(int i=0;i<field.getRow();i++){
            if(testEnded)
                break;
            if(i%2==0)
                scanRight();
            else
                scanLeft();
        }
        backToStart();
    }

    //scansiona una riga andando verso sinistra
    public void scanLeft(){
        for(int i=0;i<field.getColumn()-1;i++){ //scorro tutta la riga andando verso sinistra
            if(!testEnded) {            //se il test non è finito
                robot.forwardOnceSearch();
                field.setRobotPosition(field.getRobotPosition().getX() - 1, field.getRobotPosition().getY());
                Utility.sleep(5000);
                if (robot.getMinePickedUp()) { //ho raccolto una mina nell'ultimo avanzamento
                    field.setLastMinePosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
                    robot.changeCellChecked(field.getRobotPosition().getX(),field.getRobotPosition().getY(),1);
                    robot.autoMove90Left();
                    Utility.sleep(5000);
                    storeBall();
                    robot.autoMove90Left();
                    Utility.sleep(5000);
                }
            }
        }
        if(field.getRobotPosition().getY()<field.getRow()-1  && !testEnded) { // se ho una riga sopra ed il test non è finito
            robot.autoMove90Right(); //arrivato infondo alla riga giro a destra per salire
            Utility.sleep(5000);
            robot.forwardOnceSearch(); //salgo nella riga sopra
            field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY() + 1);
            Utility.sleep(5000);
            if(robot.getMinePickedUp()){ //ho raccolto una mina nell'ultimo avanzamento
                field.setLastMinePosition(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                robot.changeCellChecked(field.getRobotPosition().getX(),field.getRobotPosition().getY(),1);
                robot.autoMove180Left();
                Utility.sleep(5000);
                storeBall();
            }
            robot.autoMove90Right(); //mi giro pronto per scorrere la nuova riga
            Utility.sleep(5000);
        }
        else if(!testEnded) //non dovrebbe mai entrare qua, forse si può togliere
        {
            robot.autoMove90Left();
            Utility.sleep(5000);
        }
    }

    //scansiona una riga andando verso destra
    public void scanRight() {
        for (int i = 0; i < field.getColumn() - 1; i++) { //scorro tutta la riga andando verso destra
            if(!testEnded) {
                robot.forwardOnceSearch();
                field.setRobotPosition(field.getRobotPosition().getX() + 1, field.getRobotPosition().getY());
                Utility.sleep(5000);
                if (robot.getMinePickedUp()) { //ho raccolto una mina nell'ultimo avanzamento
                    field.setLastMinePosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
                    robot.changeCellChecked(field.getRobotPosition().getX(),field.getRobotPosition().getY(),1);
                    robot.autoMove90Right();
                    Utility.sleep(5000);
                    storeBall();
                    robot.autoMove90Right();
                    Utility.sleep(5000);
                }
            }
        }
        if (field.getRobotPosition().getY() < field.getRow() - 1  && !testEnded) { // se ho una riga sopra ed il test non è finito
            robot.autoMove90Left(); //arrivato infondo alla riga giro a sinistra per salire
            Utility.sleep(5000);
            robot.forwardOnceSearch(); //salgo nella riga sopra
            field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY() + 1);
            Utility.sleep(5000);
            if(robot.getMinePickedUp()){ //ho raccolto una mina nell'ultimo avanzamento
                field.setLastMinePosition(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                robot.changeCellChecked(field.getRobotPosition().getX(),field.getRobotPosition().getY(),1);
                robot.autoMove180Right();
                Utility.sleep(5000);
                storeBall();
            }
            robot.autoMove90Left(); //mi giro pronto per scorrere la nuova riga
            Utility.sleep(5000);
        }
        else if(!testEnded)  //non dovrebbe mai entrare qui, forse si può togliere
        {
            robot.autoMove90Right();
            Utility.sleep(5000);
        }
    }

    //ritorno alla posizione di partenza
    public void backToStart(){  // si presuppone che il robot sia già girato verso il "basso"
        for(int i=field.getRobotPosition().getY();i>0;i--){ //scendo fino alla base della griglia
            robot.forwardOnce();
            Utility.sleep(5000);
            field.setRobotPosition(field.getRobotPosition().getX(),field.getRobotPosition().getY()-1);
        }
        if(field.getRobotPosition().getX()>field.getStartPosition().getX()){ // sono a destra rispetto allo start
            robot.autoMove90Right();
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()>field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            }
            robot.autoMove90Left();
            Utility.sleep(5000);
        }
        else if(field.getRobotPosition().getX()<field.getStartPosition().getX()){ //sono a sinistra rispetto allo start
            robot.autoMove90Left();
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()<field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
            }
            robot.autoMove90Right();
            Utility.sleep(5000);
        }
    }

    //ritorna alla posizione in cui a raccolto l'ultima mina, si aspetta che il robot sia rivolto verso l'alto
    public void backToLastMinePos(){ //il robot arriva rivolto verso "l'alto"
        if(field.getRobotPosition().getX()>field.getLastMinePosition().getX()){ // sono a destra rispetto alla posizione dell'ultima mina
            robot.autoMove90Left();
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()>field.getLastMinePosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            }
            robot.autoMove90Right();
            Utility.sleep(5000);
        }
        else if(field.getRobotPosition().getX()<field.getLastMinePosition().getX()){ //sono a sinistra rispetto alla posizione dell'ultima mina
            robot.autoMove90Right();
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()<field.getLastMinePosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
            }
            robot.autoMove90Left();
            Utility.sleep(5000);
        }
        for(int i=0;i<field.getLastMinePosition().getY();i++){ //salgo fino alla posizione dell'ultima mina
            robot.forwardOnce();
            Utility.sleep(5000);
            field.setRobotPosition(field.getRobotPosition().getX(),field.getRobotPosition().getY()+1);
        }
    }
}
