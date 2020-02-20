package com.example.brainlegostormingapp.Tests;

import com.example.brainlegostormingapp.Activity.ConnectionsActivity;
import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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


    public Test3(Robot robot, GameField field, int mine, ConnectionsClient connectionsClient, String idSend, String KEY){
        this.connectionsClient = connectionsClient;
        this.idSend = idSend;
        this.KEY = KEY;
    }

    public void sendCriptedMessage(int x, int y, Constant.OPERATION opType)
    {
        String s = "Operazione "+ opType.toString() +":"+x+";"+y+";";
        if (opType.toString().equals(Constant.OPERATION.COMPLETATA.toString())) sendMessage(x,y);
        Calendar calendar = Calendar.getInstance();
        Long time_long = calendar.getTimeInMillis();
        s = s+time_long.toString()+";";

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
}
