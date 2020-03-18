package com.example.brainlegostormingapp.Tests;

import com.example.brainlegostormingapp.Activity.ConnectionsActivity;
import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Position;
import com.example.brainlegostormingapp.Robot;
import com.example.brainlegostormingapp.Utility.Utility;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import com.example.brainlegostormingapp.Utility.Constant;

public class Test3 extends ConnectionsActivity {
    private boolean testEnded;
    private ConnectionsClient connectionsClient;
    private String idSend,KEY;

    public Robot robot;
    public GameField field;
    int orientation;

    public boolean myMsgReturned;

    public Recovery myRecovery;

    public int totMine;
    public int securedMine;

    private ArrayList<Recovery> onGoingRecovery;


    public Test3(Robot robot, GameField field, int mine, ConnectionsClient connectionsClient, String idSend, String KEY){
        this.connectionsClient = connectionsClient;
        this.idSend = idSend;
        this.KEY = KEY;

        this.robot=robot;
        this.field=field;
        this.totMine=mine;
        this.securedMine=0;
        this.myRecovery=new Recovery();
        this.myMsgReturned=false;
        this.orientation=0;
        this.onGoingRecovery = new ArrayList<>();
    }

    public void sendCriptedMessage(int x, int y, Long timestamp,  Constant.OPERATION opType)
    {
        String s = "Operazione "+ opType.toString() +":"+x+";"+y+";";
        if (opType.toString().equals(Constant.OPERATION.COMPLETATA.toString())) sendMessage(x,y);
        s = s+timestamp.toString()+";";

        byte[] bytes = s.getBytes();
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
            Cipher c = Cipher.getInstance("DES/ECB/ISO10126Padding");
            c.init(c.ENCRYPT_MODE, key);

            byte[] ciphertext = c.doFinal(bytes);
            //send(Payload.fromBytes(ciphertext));
            connectionsClient.sendPayload(idSend,Payload.fromBytes(ciphertext));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(int x, int y)
    {
        String s = "Coordinate recupero:"+x+";"+y+";";
        byte[] bytes = s.getBytes();

        connectionsClient.sendPayload(idSend,Payload.fromBytes(bytes));
    }

    public void addNewPosition(Position p)
    {
        // TODO
        /* Fare il check di questa casella in base alla coordinata ricevuta*/
    }

    public void addMessage(Recovery r)
    {
        if(r.getX()==myRecovery.getX() && r.getY()==myRecovery.getY() && r.getTimestamp()==myRecovery.getTimestamp())
            myMsgReturned=true;
        onGoingRecovery.add(r);
    }

    public void removeMessage(Recovery r)
    {
        /*int i = 0;
        boolean trovato = false;
        while(!trovato && i < onGoingRecovery.size())
        {
            Recovery rg = onGoingRecovery.get(i);
            if (rg.getX() == r.getX() && rg.getY() == r.getY())
                trovato = true;
            i++;
        }*/

        onGoingRecovery.remove(r);
    }

    @Override
    protected String getName() {
        return null;
    }

    @Override
    protected String getServiceId() {
        return null;
    }

    @Override
    protected Strategy getStrategy() {
        return null;
    }



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

    public void testRecovery(){
        if(robot.identifyBall()){
            Position ballPosition=getNextPosition(); //devo recuperare la posizione in cui si trova la pallina

            //calcolo il timestamp
            Calendar calendar = Calendar.getInstance();
            Long timestamp = calendar.getTimeInMillis();

            //salvo le variabili del mio recupero
            myRecovery.setX(ballPosition.getX());
            myRecovery.setY(ballPosition.getY());
            myRecovery.setTimestamp(timestamp);

            //comunico alla GS la posizione in cui voglio recuperare
            sendCriptedMessage(ballPosition.getX(),ballPosition.getY(),timestamp, Constant.OPERATION.INCORSO);
            while(!myMsgReturned){ //attendo che mi ritorni il mio messaggio
                Utility.sleep(100);
            }
            if(!recoveryOK()) {//controllo se qualcuno sta recuperando quella pallina prima di me
                //comunico alla GS che non vado più a recuperare quella pallina
                sendCriptedMessage(ballPosition.getX(),ballPosition.getY(), timestamp, Constant.OPERATION.ANNULLATA);
                //resetto la mia variabile del recupero
                myRecovery.reset();
                while(robot.identifyBall()){
                    Utility.sleep(1000);
                }
            }
        }
    }

    public Position getNextPosition(){       //immmagino di spostarmi dalla posizione nella direzione passata dal costruttore senza muovere il robot
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

    public boolean recoveryOK(){
        //controllo se qualcuno sta recuperando la mia stessa mina
        for(Recovery r : onGoingRecovery){
            if(r.getX() == myRecovery.getX() && r.getY() == myRecovery.getY() && r.getTimestamp()<myRecovery.getTimestamp())
                return false;
        }
        return true;
    }

    //processa celle dalla posizione di partenza fino all'origine della matrice (posizione [0][0])
    public void alignToOrigin(){
        robot.openHand(15);
        field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
        Utility.sleep(5000);
        robot.autoMove90Left();
        Utility.sleep(5000);
        this.orientation=3;
        for (int i = field.getStartPosition().getX(); i > 0; i--) {
            if(!testEnded) {
                testRecovery();
                robot.forwardOnceSearch();
                field.setRobotPosition(field.getRobotPosition().getX() - 1, field.getRobotPosition().getY());
                Utility.sleep(5000);
                if (robot.getMinePickedUp() == true) { //ho raccolto una mina nell'ultimo avanzamento
                    field.setLastMinePosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
                    robot.minaCheck(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                    robot.autoMove90Left();
                    this.orientation=2;
                    Utility.sleep(5000);
                    storeBall();        //la deposito nella zona sicura
                    robot.autoMove90Left();
                    this.orientation=3;
                    Utility.sleep(5000);
                }
            }
        }
        if(!testEnded) {
            robot.autoMove180Right();       //mi giro pronto per scansionare tutto il campo
            this.orientation=1;
            Utility.sleep(5000);
        }
    }

    //processa la cella seguente
    public void processNextCell(){
        if(ballNextCell())
            storeBall();
        testRecovery();
        robot.forwardOnceSearch();
    }

    //metodo per portare la pallina nella zona sicura
    public void storeBall(){
        backToStart();                  //torna alla posizione di partenza
        secureMine();                   //deposita la mina
        sendCriptedMessage(myRecovery.getX(),myRecovery.getY(),myRecovery.getTimestamp(), Constant.OPERATION.COMPLETATA);  //comunico di aver commpletato l'operazione
        myRecovery.reset(); //resetto i dati del mio recupero
        if(securedMine>=totMine)        //se ho finito di raccogliere mine
            testEnded=true;
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
        this.orientation=0;
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
                testRecovery();
                robot.forwardOnceSearch();
                field.setRobotPosition(field.getRobotPosition().getX() - 1, field.getRobotPosition().getY());
                Utility.sleep(5000);
                if (robot.getMinePickedUp()) { //ho raccolto una mina nell'ultimo avanzamento
                    field.setLastMinePosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
                    robot.minaCheck(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                    robot.autoMove90Left();
                    this.orientation=2;
                    Utility.sleep(5000);
                    storeBall();
                    robot.autoMove90Left();
                    this.orientation=3;
                    Utility.sleep(5000);
                }
            }
        }
        if(field.getRobotPosition().getY()<field.getRow()-1  && !testEnded) { // se ho una riga sopra ed il test non è finito
            robot.autoMove90Right(); //arrivato infondo alla riga giro a destra per salire
            this.orientation=0;
            Utility.sleep(5000);
            testRecovery();
            robot.forwardOnceSearch(); //salgo nella riga sopra
            field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY() + 1);
            Utility.sleep(5000);
            if(robot.getMinePickedUp()){ //ho raccolto una mina nell'ultimo avanzamento
                field.setLastMinePosition(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                robot.minaCheck(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                robot.autoMove180Left();
                this.orientation=2;
                Utility.sleep(5000);
                storeBall();
            }
            robot.autoMove90Right(); //mi giro pronto per scorrere la nuova riga
            this.orientation=1;
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
                testRecovery();
                robot.forwardOnceSearch();
                field.setRobotPosition(field.getRobotPosition().getX() + 1, field.getRobotPosition().getY());
                Utility.sleep(5000);
                if (robot.getMinePickedUp()) { //ho raccolto una mina nell'ultimo avanzamento
                    field.setLastMinePosition(field.getRobotPosition().getX(), field.getRobotPosition().getY());
                    robot.minaCheck(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                    robot.autoMove90Right();
                    this.orientation=2;
                    Utility.sleep(5000);
                    storeBall();
                    robot.autoMove90Right();
                    this.orientation=1;
                    Utility.sleep(5000);
                }
            }
        }
        if (field.getRobotPosition().getY() < field.getRow() - 1  && !testEnded) { // se ho una riga sopra ed il test non è finito
            robot.autoMove90Left(); //arrivato infondo alla riga giro a sinistra per salire
            this.orientation=0;
            Utility.sleep(5000);
            testRecovery();
            robot.forwardOnceSearch(); //salgo nella riga sopra
            field.setRobotPosition(field.getRobotPosition().getX(), field.getRobotPosition().getY() + 1);
            Utility.sleep(5000);
            if(robot.getMinePickedUp()){ //ho raccolto una mina nell'ultimo avanzamento
                field.setLastMinePosition(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                robot.minaCheck(field.getRobotPosition().getX(),field.getRobotPosition().getY());
                robot.autoMove180Right();
                this.orientation=2;
                Utility.sleep(5000);
                storeBall();
            }
            robot.autoMove90Left(); //mi giro pronto per scorrere la nuova riga
            this.orientation=3;
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
            this.orientation=3;
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()>field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            }
            robot.autoMove90Left();
            this.orientation=2;
            Utility.sleep(5000);
        }
        else if(field.getRobotPosition().getX()<field.getStartPosition().getX()){ //sono a sinistra rispetto allo start
            robot.autoMove90Left();
            this.orientation=1;
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()<field.getStartPosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
            }
            robot.autoMove90Right();
            this.orientation=2;
            Utility.sleep(5000);
        }
    }

    //ritorna alla posizione in cui a raccolto l'ultima mina, si aspetta che il robot sia rivolto verso l'alto
    public void backToLastMinePos(){ //il robot arriva rivolto verso "l'alto"
        if(field.getRobotPosition().getX()>field.getLastMinePosition().getX()){ // sono a destra rispetto alla posizione dell'ultima mina
            robot.autoMove90Left();
            this.orientation=3;
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()>field.getLastMinePosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()-1,field.getRobotPosition().getY());
            }
            robot.autoMove90Right();
            this.orientation=0;
            Utility.sleep(5000);
        }
        else if(field.getRobotPosition().getX()<field.getLastMinePosition().getX()){ //sono a sinistra rispetto alla posizione dell'ultima mina
            robot.autoMove90Right();
            this.orientation=1;
            Utility.sleep(5000);
            while(field.getRobotPosition().getX()<field.getLastMinePosition().getX()){
                robot.forwardOnce();
                Utility.sleep(5000);
                field.setRobotPosition(field.getRobotPosition().getX()+1,field.getRobotPosition().getY());
            }
            robot.autoMove90Left();
            this.orientation=0;
            Utility.sleep(5000);
        }
        for(int i=0;i<field.getLastMinePosition().getY();i++){ //salgo fino alla posizione dell'ultima mina
            robot.forwardOnce();
            Utility.sleep(5000);
            field.setRobotPosition(field.getRobotPosition().getX(),field.getRobotPosition().getY()+1);
        }
    }
}
