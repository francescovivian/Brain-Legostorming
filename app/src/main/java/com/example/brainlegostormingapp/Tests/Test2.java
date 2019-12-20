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
import java.util.ArrayList;

public class Test2 extends Test {

    private Context context;
    private String testoRicevuto;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private int IDRobot;
    Position positionReceived;

    //Variabili aggiunte da TheExpert
    ArrayList<Position> positionList= new ArrayList<Position>();
    ArrayList<String> movements=new ArrayList<String>();
    //Fine

    public Test2(Robot robot, GameField field, Context context) {
        super(robot, field);
        this.context = context;
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
        while(securedMine<totMine){
            if(positionList.isEmpty()){
                //codice per attendere l'arrivo di una posizione
            }
            else{
                Position actualMine =positionList.remove(0); //tolgo la prima mina che c'è in coda e shifto la coda a sinistra
                goTo(actualMine);
            }
        }
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
    public void goTo(Position actualMine){
        while(field.getRobotPosition().getX()!=actualMine.getX()){

        }
    }


    //Nel caso andando alla mina si ripassasse per la posizione iniziale, svuoto lo stack delle mosse precedenti
    public void emptyStack(){
        this.movements=new ArrayList<String>(); // ;) ;)
    }

    /*
    * Right90, FW, Left90 se può
    * se non ha una mina davanti, termina
    * altrimenti se ha una cella a destra prova dodgeRight
    * */
    public void dodgeRight(){
        robot.autoMove90Right();
        Utility.sleep(5000);
        if(robot.identifyBall()){ //trovo una pallina a destra
            robot.autoMove90Left();
            Utility.sleep(5000);
            dodgeLeft();
        }
    }

    public void dodgeLeft(){

    }

    public void sorpassaSX(){

    }

    public boolean sorpassaDX(){
        robot.autoMove90Right();
        if(!robot.identifyBall()){
            robot.forwardOnce();
            robot.autoMove90Left();
            int mosse=0;
            while(robot.identifyBall()){
                robot.autoMove90Right();
                robot.forwardOnce();
                robot.autoMove90Left();
                mosse++;
            }
            robot.forwardOnce();
        }
        return false;
    }
}

