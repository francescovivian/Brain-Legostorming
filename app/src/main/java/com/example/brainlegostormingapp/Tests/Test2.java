package com.example.brainlegostormingapp.Tests;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.brainlegostormingapp.Position;
import com.example.brainlegostormingapp.Utility.Constant;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;

import static com.example.brainlegostormingapp.Utility.Constant.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;

public class Test2 extends Test {

    private Context context;
    private String testoRicevuto;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private int IDRobot;
    Position positionReceived;

    public Test2(Robot robot, GameField field, Context context) {
        super(robot, field);
        this.context = context;
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
}

