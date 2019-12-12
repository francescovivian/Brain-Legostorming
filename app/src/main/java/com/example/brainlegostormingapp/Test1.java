package com.example.brainlegostormingapp;

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
        robot.autoMove180Left();  //il robot si gira di 180°
        scanRight(); //inizia la scansione andando verso destra
        while(securedMine < totMine){
            //fa i vari movimenti
        }
    }

    public void initialize(){
        securedMine = 0;
        alignToOrigin();  //raggiungo l'origine
    }

    //si raddrizza sulla singola cella
    public void fixOrientation(){
        double skew;
        double maxAcceptedSkew = 30;
        skew = amIStraight();
        while(skew > maxAcceptedSkew) {
            straightenMe(skew);
            skew = amIStraight();
        }
    }

    public void alignToOrigin(){ //
        robot.autoMove90Left();
        //processa cella fino all'origine
    }

    public void processNextCell(){ //processa la cella seguente
        if(ballNextCell()){
            storeBall();
        }
        robot.forwardOnce();
    }

    //funzione che mi dice se sono dritto
    public double amIStraight(){
        double skew = 0;
        //metodo che ottiene tutte le lines

        //elabora le linee per qualche frame
        //controlla che tutte le linee siano angolate correttamente
        //controlla che tutte le linee finiscano con l'angolazione corretta per il lato dello schermo
        //potrebbe ritornare la direzione in cui dovrebbe muoversi per raddrizzarsi
        return skew;
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
