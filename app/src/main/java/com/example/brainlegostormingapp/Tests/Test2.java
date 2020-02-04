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
    Position finish; //todo: servirà a contenere la posizione da raggiungere in quel momento

    private ArrayList<String> movements=new ArrayList<String>();
    private ArrayList<Position> positionList=new ArrayList<Position>();
    private boolean minaRaccolta = false;
    private ArrayList<Position> cronologiaMovimenti;
    //Fine

    public Test2(Robot robot, GameField field, char cRO, Context context) {
        super(robot, field);
        this.context = context;
        //todo cambiare con il valore definitivo
        totMine = 9;
        securedMine = 0;
        int nRO=0;
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

    public void sendPosition(Position position){
        //TODO implement the movement
    }

    //start discovery è chiamata al click del btn Start nella AutoActivity
    public void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(context)
                .startDiscovery(
                        SERVICE_ID,/*com.example.brainlegostormingapp";*/
                        endpointDiscoveryCallback,
                        discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're discovering!
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We're unable to start discovering.
                        });
    }

    /*Quando trova dei dispositivi, il discoverer inizializza la connessione*/
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // An endpoint was found. We request a connection to it.
                    Nearby.getConnectionsClient(context)
                            .requestConnection(
                                    NOME_GRUPPO,
                                    endpointId,
                                    mConnectionLifecycleCallback /*Is the callback that will be invoked when discoverers request to connect to the advertiser*/
                            )
                            .addOnSuccessListener((Void unused) -> {
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                    })
                            .addOnFailureListener((Exception e) -> {
                                        // Nearby Connections failed to request the connection.
                                    });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                }
            };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Accetta in automatico la connesione per entrambe le parti
                    Nearby.getConnectionsClient(context).acceptConnection(endpointId, mPayloadCallback);
                    //ricavo il nome dell'endpoint acui mi sto connettendo
                    String endpoint = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    /*endpointId -> id della stazione base*/
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            //Ora siamo connessi, si possono spedire dati
                            Nearby.getConnectionsClient(context).stopDiscovery();
                            String firstMessage = "Benvenuto sono " + NOME_GRUPPO;
                            Nearby.getConnectionsClient(context).sendPayload(
                                    endpointId, Payload.fromBytes(firstMessage.getBytes(UTF_8)));
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // La connessione è stata rifiutata da una delle 2 parti
                            break;
                        default:
                            // The connection was broken before it was accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };

    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                    testoRicevuto = new String(payload.asBytes(), UTF_8);
                    if (testoRicevuto.contains("Coordinate obiettivo")) {
                        testoRicevuto = testoRicevuto.replace("Coordinate obiettivo:", "");
                        testoRicevuto = testoRicevuto.substring(0,testoRicevuto.length()-1);
                        String coordinata[]= testoRicevuto.split(";");
                        positionReceived = new Position(Integer.parseInt(coordinata[0]),Integer.parseInt(coordinata[1]));
                    }

                    if(testoRicevuto == IDRobot + "STOP"){
                        //TODO stop the motors
                    }
                    else if(testoRicevuto == IDRobot + "RESUME"){
                        //TODO resume the game
                    }
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                    //Quando ho finito il trasferimento dei dati spediti entra in questo metodo
                }

            };



    /*Inizio funzioni dell'algoritmo della seconda prova*/

    @Override
    public void movement(){
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
        return true; //todo controlla che davanti a se ci sia la cella con la mina da raccogliere
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


        //todo: while che "consuma" tutte le posizioni ricevute tramite nearby
        //int md_best=md(field.getStartPosition(),finish);

        ArrayList<Position> mosse=new ArrayList<Position>();

        while(this.securedMine<this.totMine){
            //todo: togliere il commento sotto qua una volta che implementato il riempimento del vettore di posizioni
            /*while(positionList.size()<1){
                Utility.sleep(100);
            }
            Position target=positionList.remove(0);*/
            //Position target = new Position(4,4);
            Position target = new Position(1,2);
            vaiA(target);
            vaiA(field.getStartPosition());
        }
    }

    private void vaiA(Position target) {
        finish = target;
        while(field.getRobotPosition().getX()!=finish.getX() || field.getRobotPosition().getY()!=finish.getY()){
            if (!minaRaccolta && devoRaccogliere()) {
                raccogliMina();
            }
            else if (minaRaccolta && sonoSuStart()){
                setOrientation(2);
                mollaMina();
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
                robot.forwardOnce();
                movements.add("FW");
                Utility.sleep(5000);

                Position p = getNextPosition(best_dir);
                field.setRobotPosition(p.getX(), p.getY());
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
        }
        Utility.sleep(5000);
    }

    private void mollaMina() {
        robot.openHand(15);       //apre la mano
        robot.forwardHalf();            //avanza un poco
        Utility.sleep(5000);
        this.securedMine++;
        minaRaccolta = false;
        robot.backwardHalf();           //torna indietro
        Utility.sleep(5000);
        robot.autoMove180Right();       //si gira di 180°
    }

    private boolean sonoSuStart() {
        return (field.getRobotPosition() == field.getStartPosition());
    }

    private void raccogliMina() {
        //setto la flag a true per i cicli successivi
        minaRaccolta = true;
        //setto la posizione della mina che ho appena raccolto nella matrice
        Position p = getNextPosition(robotOrientation);
        robot.minaCheck(p.getX(), p.getY());
        //muovo effettivamente il robot per racccogliere la mina
        robot.forwardOnceSearch();
        movements.add("FW");
        Utility.sleep(5000);
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

        for(int i=0;i<4;i++){                                   //per tutte le direzioni
            Position current=getNextPosition(i);                //prendo la posizione virtuale
            if(mainField[current.getX()][current.getY()]!=2)    //se non ci sono mai statp
                newDirs.add(i);                                 //la aggiungo al vettore
        }

        for(int i=0;i<newDirs.size();i++){                      //controllo se nelle posizioni che ho nel vettore ci sono mine o no
            setOrientation(newDirs.get(i));                     //mi giro nella posizione corrente
            if(!robot.identifyBall())                           //se posso andarci
                return newDirs.get(i);                          //ritorno tale direzione
        }

                                                                //non ho trovato una direzione in cui non sono mai stato ed in cui non ci sono mine
        for(int i=0;i<4;i++){                                   //per le 4 direzioni
            dir=(robotOrientation+1)%4;
            setOrientation(dir);                                //mi giro nella direzione corrente
            if(!robot.identifyBall())                           //se non ci sono mine
                return dir;                                     //restituisco tale direzione
        }

        return dir;                                             //se arriva qui, qualcosa è andato storto
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
        if (movement == 1){
            robot.autoMove90Right();
            movements.add("90RIGHT");
            Utility.sleep(5000);
        }
        else if (movement == 2){
            robot.autoMove180Right();
            movements.add("180RIGHT");
            Utility.sleep(5000);
        }
        else if (movement == 3){
            robot.autoMove90Left();
            movements.add("90LEFT");
            Utility.sleep(5000);
        }
        //aggiorno l'orientamento scritto del robot
        //robotOrientation = (robotOrientation + movement)%4;
        robotOrientation = newOrientation;
    }

    public int productivePath(){

        int best_dir=-1;                                    //se ritorno -1 non ho trovato una direzione migliore, dovrò muovermi random

        ArrayList<Integer> betterDirections=betterMdDirections();

        for(int i=0;i<betterDirections.size();i++){         //per tutte le direzioni
            int direction=betterDirections.get(i);          //prendo quella corrente
            Position p=getNextPosition(direction);          //fake posizione andando in quella direzione
            if(mainField[p.getX()][p.getY()]!=2){           //se non ci sono mai passato
                setOrientation(direction);                  //mi giro da quella parte
                Utility.sleep(3000);
                if(!robot.identifyBall())                   //se non c'è una mina
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
        else if (true) {                        //TODO ho muro a destra e voglio andare verso l'alto
            robot.autoMove90Left();             //mi raddrizzo verso l'alto
            Utility.sleep(5000);
            dodgeLeft();                        //tento il dodge verso sinistra
        }
        else {
            robot.forwardOnce();                //mi sposto nella cella a destra
            //TODO aggiorna posizione robot
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
            //TODO tornare indietro e provare a passare a sinistra da riga piu in basso
            // o a destra da riga piu in basso
        }
        else if (true) {                        //TODO ho muro a sinistra e voglio andare verso l'alto
            robot.autoMove90Left();
            Utility.sleep(5000);
            dodgeLeft();
        }
        else {
            robot.forwardOnce();                //mi sposto nella cella a sinistra
            //TODO aggiorna posizione robot
            Utility.sleep(5000);
            robot.autoMove90Left();             //mi raddrizzo verso l'alto
            Utility.sleep(5000);
        }
    }

    public void alignToBall(){
        if(true){ //todo posizione robot diversa da riga della mina
            if(robot.identifyBall() && !rightBallAhead()){               //trovo una pallina davanti
                dodgeRight();
            }
            else if(rightBallAhead()){
                //todo raccoglie la mina
            }
            else {
                robot.forwardOnce();
                alignToBall();
            }
        }
    }*/
    //end region
}

