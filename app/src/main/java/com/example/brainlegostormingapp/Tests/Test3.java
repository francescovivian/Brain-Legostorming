package com.example.brainlegostormingapp.Tests;

import com.example.brainlegostormingapp.GameField;
import com.example.brainlegostormingapp.Robot;

public class Test3 extends Test1 {
    private boolean testEnded;

    public Test3(Robot robot, GameField field, int mine){
        super(robot, field, mine);
        this.totMine=mine;
        this.testEnded=false;
    }
}
