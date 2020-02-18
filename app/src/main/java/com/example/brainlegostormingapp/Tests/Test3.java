package com.example.brainlegostormingapp.Tests;

import com.example.brainlegostormingapp.Activity.ConnectionsActivity;
import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

public class Test3 extends ConnectionsActivity {
    private boolean testEnded;

    public Test3(Robot robot, GameField field, int mine){
        String x = "Test 3 riesce a fare la send";
        byte[] bytes = x.getBytes();
        send(Payload.fromBytes(bytes));
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
