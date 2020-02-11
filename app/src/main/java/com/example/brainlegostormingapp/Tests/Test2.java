package com.example.brainlegostormingapp.Tests;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.brainlegostormingapp.Position;
import com.example.brainlegostormingapp.Utility.Constant;
import com.example.brainlegostormingapp.Utility.Utility;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;

import static com.example.brainlegostormingapp.Utility.Constant.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Random;

public class Test2 extends Test {

    private Context context;
    private String testoRicevuto;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private int IDRobot;
    Position positionReceived;

    int[][] mainField;


    //Variabili algoritmo
    int robotOrientation;
    Position finish; //servirà a contenere la posizione da raggiungere in quel momento
    Position target;
    int nRO;

    private ArrayList<String> movements=new ArrayList<String>();
    private ArrayList<Position> positionList=new ArrayList<Position>();
    private boolean minaRaccolta = false;
    private ArrayList<Position> cronologiaMovimenti;
    //Fine

    public Test2(Robot robot, GameField field, char cRO, int mine, Context context) {
        super(robot, field);
        this.context = context;
        this.totMine = mine;
        this.securedMine = 0;
        nRO=0;
        if(cRO=='s')
            nRO=0;
        else if(cRO=='o')
            nRO=1;
        else if(cRO=='n')
            nRO=2;
        else if(cRO=='e')
            nRO=3;
        this.robotOrientation=nRO;
        this.finish=null;
        this.mainField=new int[field.getRow()][field.getColumn()];
    }

    public void addNewPosition(Position p)
    {
        this.positionList.add(p);
    }

    @Override
    public void movement(){
        robot.openHand(15);
        field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
        printPlayerPositionInMatrix();
        /*while(securedMine<totMine){
            if(positionList.isEmpty()){
                //codice per attendere l'arrivo di una posizione
            }
            else{
                Position actualMine =positionList.remove(0); //tolgo la prima mina che c'è in coda e shifto la coda a sinistra
                goTo(actualMine);
            }
        }*/
        algorithm();
    }

    /*
    * Il robot è nella posizione iniziale e rivolto verso il centro del campo
    * Arriva alla mina richiesta e la raccoglie
    * Salva le varie mosse per arrivare alla mina nello stack movements
    * Per tornare a start e fare il percorso a ritroso, le mosse di rotazione dovranno essere effettuate al contrario
    * Le mosse di avanzamento/indietreggiamento dovranno essere effettuate come scritte nello stack
    * Esempio:  90LEFT nello stack      ->      devo effettuare 90RIGHT
    *           FW nello stack          ->      devo effettuare FW
    *           HBW nello stack         ->      devo effettuare HBW
    *
    * LEGENDA MOSSE:
    *   90LEFT:     rotazione di 90° a sinistra
    *   90RIGHT:    rotazione di 90° a destra
    *   180LEFT:    rotazione di 180° a sinistra
    *   180RIGHT:   rotazione di 180° a destra
    *   FW:         avanzamento di una cella
    *   HFW:        avanzamento di mezza cella
    *   BW:         inidietreggiamento di una cella
    *   HBW:        inidietreggiamento di mezza cella
    * */


    //Nel caso andando alla mina si ripassasse per la posizione iniziale, svuoto lo stack delle mosse precedenti
    public void emptyStack(){
        this.movements=new ArrayList<String>(); // ;) ;)
    }

    private boolean rightBallAhead() {
        return true; //controlla che davanti a se ci sia la cella con la mina da raccogliere
    }

    public void sorpassaSX(){

    }

    public boolean sorpassaDX(){
        robot.autoMove90Right();
        Utility.sleep(5000);
        if(!robot.identifyBall()){
            robot.forwardOnce();
            Utility.sleep(5000);
            robot.autoMove90Left();
            Utility.sleep(5000);
            int mosse=0;
            while(robot.identifyBall()){
                robot.autoMove90Right();
                Utility.sleep(5000);
                robot.forwardOnce();
                Utility.sleep(5000);
                robot.autoMove90Left();
                Utility.sleep(5000);
                mosse++;
            }
            robot.forwardOnce();
        }
        return false;
    }



    /*Algoritmo Vivian*/


    public void algorithm(){

        //int md_best=md(field.getStartPosition(),finish);

        //ArrayList<Position> mosse=new ArrayList<Position>();


        //simulo il consumatore di posizioni ricevute
        /*target = new Position(6,6);
        positionList.add(target);
        target = new Position(1,6);
        positionList.add(target);
        target = new Position(1,1);
        positionList.add(target);
        */
        while(this.securedMine<this.totMine){
            while(positionList.size()<1) {
                Utility.sleep(100);
            }
            target = positionList.remove(0);

            inizializzaTutto();
            vaiA(target);
            vaiA(field.getStartPosition());
            setOrientation((nRO+2)%4);
            while (robotOrientation != ((nRO + 2) % 4))
                Utility.sleep(100);
            mollaMina();
        }
    }

    private void inizializzaTutto() {
        this.movements = new ArrayList<>();
        this.mainField = new int[field.getRow()][field.getColumn()];

    }

    private void vaiA(Position target) {
        finish = target;
        while(finish != null && (field.getRobotPosition().getX()!=finish.getX() || field.getRobotPosition().getY()!=finish.getY())){
            if (!minaRaccolta && devoRaccogliere()) {
                raccogliMina();
            }
            else if (minaRaccolta && sonoSuStart()){
                setOrientation((nRO+2)%4);
            }
            else if (minaRaccolta){
                //se ho raccolto la mina devo tornare indietro seguendo il percorso inverso
                reverseMove(movements.remove(movements.size()-1));
            }
            else {
                int best_dir = productivePath();
                if (best_dir == -1) {                   //non esiste un percorso migliore, prendo una direzione random
                    best_dir = randomDirection();
                }
                this.setOrientation(best_dir);
                if(!devoRaccogliere()) {
                    robot.forwardOnce();
                    movements.add("FW");
                    Utility.sleep(5000);
                    Position p = getNextPosition(best_dir);
                    //
                    field.setRobotPosition(p.getX(), p.getY());
                }
                else{
                    raccogliMina();
                }
                //cronologiaMovimenti.add(new Position(p.getX(), p.getY()));
            }
            /*mosse.add(new Position(field.now.x,field.now.y));
            for(int i=0;i<mosse.size();i++){
                System.out.print("("+mosse.get(i).x+";"+mosse.get(i).y+")->");
            }
            System.out.println();
            */
            printPlayerPositionInMatrix();
            //field.printField();
        }
    }

    private void reverseMove(String nextMove) {
        if (nextMove == "FW") {
            robot.forwardOnce();
            Position p = getNextPosition(robotOrientation);
            field.setRobotPosition(p.getX(), p.getY());
        }
        else if (nextMove == "90LEFT") {
            robot.autoMove90Right();
            robotOrientation = (robotOrientation + 1)%4;
        }
        else if (nextMove == "90RIGHT") {
            robot.autoMove90Left();
            robotOrientation = (robotOrientation - 1)%4;
            if(robotOrientation<0)
                robotOrientation+=4;
        }
        Utility.sleep(5000);
    }

    private void mollaMina() {
        //this.target = new Position(0,0);
        robot.setMinePickedUp(false);
        this.securedMine++;
        this.minaRaccolta = false;
        robot.openHand(15);       //apre la mano
        robot.forwardHalf();            //avanza un poco
        Utility.sleep(5000);
        robot.backwardHalf();           //torna indietro
        Utility.sleep(5000);
        robot.autoMove180Right();       //si gira di 180°
        robotOrientation = nRO;
        Utility.sleep(5000);
    }

    private boolean sonoSuStart() {
        return (field.getRobotPosition().getX() == field.getStartPosition().getX() && field.getRobotPosition().getY() == field.getStartPosition().getY());
    }

    private void raccogliMina() {
        //setto la flag a true per i cicli successivi
        minaRaccolta = true;
        //muovo effettivamente il robot per racccogliere la mina
        robot.forwardOnceSearch();
        movements.add("FW");
        Utility.sleep(5000);
        //setto la posizione della mina che ho appena raccolto nella matrice
        Position p = getNextPosition(robotOrientation);
        robot.minaCheck(p.getX(), p.getY());
        //aggiorno la posizione del robot
        field.setRobotPosition(p.getX(), p.getY());
        robot.autoMove180Right();
        Utility.sleep(5000);
        robotOrientation = (robotOrientation + 2)%4;
    }

    private boolean devoRaccogliere() {
        if(robotOrientation == 0){
            return (field.getRobotPosition().getX() == finish.getX() && field.getRobotPosition().getY() + 1 == finish.getY());
        }
        else if(robotOrientation == 1){
            return (field.getRobotPosition().getX() + 1 == finish.getX() && field.getRobotPosition().getY()  == finish.getY());
        }
        else if(robotOrientation == 2){
            return (field.getRobotPosition().getX() == finish.getX() && field.getRobotPosition().getY() -1 == finish.getY());
        }
        else if (robotOrientation == 3){
            return (field.getRobotPosition().getX() - 1 == finish.getX() && field.getRobotPosition().getY()  == finish.getY());
        }
        else
            return false;
    }

    public void updateRobotPosition(){}

    public int randomDirection(){
        int dir=-1;
        int initialOrientation=robotOrientation;

        //creo un vettore con le direzioni che non hanno 2
        ArrayList<Integer> newDirs=new ArrayList<Integer>();
        ArrayList<Integer> allDirs=new ArrayList<Integer>();


        for(int i=0;i<4;i++){                                   //per tutte le direzioni
            Position current=getNextPosition(i);                //prendo la posizione virtuale
            if (current != null){
                allDirs.add(i);
            }
            if( current != null && mainField[convertToR(current.getY())][convertToC(current.getX())]!=2)    //se non ci sono mai statp
                newDirs.add(i);                                 //la aggiungo al vettore
        }

        for(int i=0;i<newDirs.size();i++){                      //controllo se nelle posizioni che ho nel vettore ci sono mine o no
            setOrientation(newDirs.get(i));                     //mi giro nella posizione corrente
            if(!robot.identifyBall())                           //se posso andarci
                return newDirs.get(i);                          //ritorno tale direzione
            else {
                for (int j = 0; j < allDirs.size(); j++){       //rimuovo le posizioni non valide per il random successivo
                    if(allDirs.get(j) == newDirs.get(i))
                        allDirs.remove(j);
                }
            }
        }

                                                                //non ho trovato una direzione in cui non sono mai stato ed in cui non ci sono mine
        int random = ( int )(Math.random()*(allDirs.size()-1));
        setOrientation(random);
        return allDirs.get(random);

        //return dir;                                             //se arriva qui, qualcosa è andato storto
    }

    private void setOrientation(int newOrientation) {
        int NewOrientationAux;
        if (newOrientation == 0){
            NewOrientationAux = 4;
        }
        else{
            NewOrientationAux = newOrientation;
        }
        int movement = (NewOrientationAux - robotOrientation)%4;
        if(movement<0)
            movement+=4;
        if (movement == 1){
            robot.autoMove90Right();
            if(!minaRaccolta)
                movements.add("90RIGHT");
        }
        else if (movement == 2){
            robot.autoMove180Right();
            if(!minaRaccolta)
                movements.add("180RIGHT");
        }
        else if (movement == 3){
            robot.autoMove90Left();
            if(!minaRaccolta)
               movements.add("90LEFT");
        }
        //aggiorno l'orientamento scritto del robot
        //robotOrientation = (robotOrientation + movement)%4;
        Utility.sleep(5000);
        robotOrientation = newOrientation;
    }

    public int productivePath(){

        int best_dir=-1;                                    //se ritorno -1 non ho trovato una direzione migliore, dovrò muovermi random

        ArrayList<Integer> betterDirections=betterMdDirections();

        for(int i=0;i<betterDirections.size();i++){         //per tutte le direzioni
            int direction=betterDirections.get(i);          //prendo quella corrente
            Position p=getNextPosition(direction);          //fake posizione andando in quella direzione
            if( p != null && mainField[convertToR(p.getY())][convertToC(p.getX())]!=2){           //se non ci sono mai passato
                setOrientation(direction);                  //mi giro da quella parte
                Utility.sleep(3000);
                if(devoRaccogliere() || !robot.identifyBall())                   //se non c'è una mina oppure ho davanti quella che devo raccogliere
                    return direction;                       //ritorno la direzione corrente che userò per muovermi
            }
        }
        return best_dir;
    }

    public boolean isAllowedForward(){
        if(robotOrientation==0){
            return (field.getRobotPosition().getY() < field.getRow()-1) && (!robot.identifyBall());
        }
        else if(robotOrientation==1){
            return (field.getRobotPosition().getX() < field.getColumn()-1) && (!robot.identifyBall());
        }
        else if(robotOrientation==2){
            return (field.getRobotPosition().getY() > 0) && (!robot.identifyBall());
        }else{
            return (field.getRobotPosition().getX() > 0) && (!robot.identifyBall());
        }
    }

    public int md(Position a, Position b){
        return abs(a.getX()-b.getX())+abs(a.getY()-b.getY());
    }

    public int abs(int x){
        if (x<0)
            x=-x;
        return x;
    }

    public int convertToR(int y){
        return this.field.getRow() - 1 - y;
    }

    public int convertToC(int x){
        return x;
    }

    public int convertToX(String pos){
        if(pos=="NOW"){
            return (field.getRow()-(field.getRobotPosition().getY()))-1;
        }else if(pos=="START"){
            return field.getRow()-(field.getStartPosition().getY())-1;
        }else{
            return field.getRow()-(finish.getY())-1;
        }
    }

    public int convertToY(String pos){
        if(pos=="NOW"){
            return field.getRobotPosition().getX();
        }else if(pos=="START"){
            return field.getStartPosition().getX();
        }else {
            return finish.getX();
        }
    }

    public ArrayList<Integer> betterMdDirections(){
        ArrayList<Integer> directions=new ArrayList<Integer>();

        int currentMD=md(field.getRobotPosition(),finish);  //md dalla posizione del robot

        for(int i=0;i<4;i++){                               //per ogni direzione
            Position fakePosition=getNextPosition(i);       //immagino di muovermi  in quella direzione

            if(fakePosition!=null) {
                int md = md(fakePosition, finish);          //mi calcolo la md
                if (md < currentMD)                         //se è migliore di quella dalla posizione corrente
                    directions.add(i);                      //la aggiungo al vettore
            }
        }
        return directions;                                  //ritorno il vettore con le direzioni migliori in cui posso muovermi
    }

    public Position getNextPosition(int orientation){       //immmagino di spostarmi dalla posizione nella direzione passata dal costruttore senza muovere il robot
        if(orientation==0 && field.getRobotPosition().getY()<field.getRow()-1)
            return new Position(field.getRobotPosition().getX(),field.getRobotPosition().getY()+1);
        else if(orientation==1 && field.getRobotPosition().getX()<field.getColumn()-1)
            return new Position(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
        else if(orientation==2 && field.getRobotPosition().getY()>0)
            return new Position(field.getRobotPosition().getX(),field.getRobotPosition().getY()-1);
        else if(orientation==3 && field.getRobotPosition().getX()>0)
            return new Position(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
        else
            return null;
    }

    public void printPlayerPositionInMatrix(){
        mainField[convertToX("NOW")][convertToY("NOW")]=2;
    }


    //region legacy code
    /*
     * Right90, FW, Left90 se può
     * se non ha una mina davanti, termina
     * altrimenti se ha una cella a destra prova dodgeRight
     * */
    /*public void dodgeRight(){
        robot.autoMove90Right();
        Utility.sleep(5000);
        if(robot.identifyBall()){               //trovo una pallina a destra
            robot.autoMove90Left();             //mi raddrizzo verso l'alto
            Utility.sleep(5000);
            dodgeLeft();                        //tento il dodge verso sinistra
        }
        else if (true) {                        //ho muro a destra e voglio andare verso l'alto
            robot.autoMove90Left();             //mi raddrizzo verso l'alto
            Utility.sleep(5000);
            dodgeLeft();                        //tento il dodge verso sinistra
        }
        else {
            robot.forwardOnce();                //mi sposto nella cella a destra
            //aggiorna posizione robot
            Utility.sleep(5000);
            robot.autoMove90Left();             //mi raddrizzo verso l'alto
            Utility.sleep(5000);
        }
    }

    public void dodgeLeft(){
        robot.autoMove90Left();
        Utility.sleep(5000);
        if(robot.identifyBall()){               //trovo una pallina a sinistra
            robot.autoMove90Right();
            Utility.sleep(5000);
            //tornare indietro e provare a passare a sinistra da riga piu in basso
            // o a destra da riga piu in basso
        }
        else if (true) {                        //ho muro a sinistra e voglio andare verso l'alto
            robot.autoMove90Left();
            Utility.sleep(5000);
            dodgeLeft();
        }
        else {
            robot.forwardOnce();                //mi sposto nella cella a sinistra
            //aggiorna posizione robot
            Utility.sleep(5000);
            robot.autoMove90Left();             //mi raddrizzo verso l'alto
            Utility.sleep(5000);
        }
    }

    public void alignToBall(){
        if(true){ //posizione robot diversa da riga della mina
            if(robot.identifyBall() && !rightBallAhead()){               //trovo una pallina davanti
                dodgeRight();
            }
            else if(rightBallAhead()){
                //raccoglie la mina
            }
            else {
                robot.forwardOnce();
                alignToBall();
            }
        }
    }*/
    //end region
}

