package com.example.brainlegostormingapp;

public class GameField {
    private int[][] grid;
    private char orientation;
    private Position startPosition;
    private Position lastMinePosition;

    public GameField(int row, int column, char orientation, int startX, int startY) {
        this.grid = new int[row][column];
        this.orientation = orientation;
        startPosition=new Position(startX,startY);
        lastMinePosition=null;
    }

    public int[][] getGrid() {
        return grid;
    }

    public char getOrientation()  {
        return orientation;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getLastMinePosition(){
        return lastMinePosition;
    }

    public void setStartPosition(int x, int y){
        startPosition.setX(x);
        startPosition.setY(y);
    }

    public void setLastMinePosition(int x, int y){
        lastMinePosition.setX(x);
        lastMinePosition.setY(y);
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }
}
