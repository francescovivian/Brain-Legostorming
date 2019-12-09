package com.example.brainlegostormingapp;

public class Campo {
    private int[][] campo;
    private char orientation;
    private int startR, startC;

    public Campo(int row, int coloumn, char orientation, int startR, int startC) {
        this.campo = new int[row][coloumn];
        this.orientation = orientation;
        this.startR = startR;
        this.startC = startC;
    }

    public int[][] getCampo() {
        return campo;
    }

    public char getOrientation() {
        return orientation;
    }

    public int getStartR() {
        return startR;
    }

    public int getStartC() {
        return startC;
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }
}
