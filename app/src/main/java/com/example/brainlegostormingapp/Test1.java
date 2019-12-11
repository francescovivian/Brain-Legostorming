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

    public void alignToOrigin(){ //
        robot.autoMove90Left();
        //processa cella fino all'origine
    }

    public void processNextCell(){ //processa la cella seguente
        if(ballNextCell()){
            storeBall();
        }
        forwardOnce();
    }


    public boolean amIStraight(){
        //funzione che mi dice se sono dritto
        //metodo che ottiene tutte le linee
        //roba che elabora quelle linee e fa qualcosa
        return false;
    }

    public void storeBall(){
        //codice per portare la pallina nella zona sicura
        backToLastMinePos();
    }

    public void backToLastMinePos(){
        //torna alla posizione in cui ha trovato l'ultima mina
    }

    public void straightMe(){
        //funzione che mi raddrizza
    }

    public void forwardOnce(){
        //funzione che mi fa avanzare di una cella
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
