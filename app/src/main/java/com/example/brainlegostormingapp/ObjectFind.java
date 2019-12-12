package com.example.brainlegostormingapp;

import java.util.ArrayList;

public class ObjectFind {
    private ArrayList<Ball> balls;
    private ArrayList<Line> lines;

    public ObjectFind setBalls(ArrayList<Ball> balls) {
        this.balls = balls;
        return this;
    }

    public ObjectFind setLines(ArrayList<Line> lines) {
        this.lines = lines;
        return this;
    }

    public ArrayList<Ball> getBalls() {
        return balls != null ? balls : new ArrayList<Ball>();
    }

    public ArrayList<Line> getLines() {
        return lines != null ? lines : new ArrayList<Line>();
    }
}
