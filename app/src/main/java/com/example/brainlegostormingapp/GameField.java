package com.example.brainlegostormingapp;

public class GameField {
    private int row;
    private int column;
    private int[][] grid;
    private char orientation;
    private Position startPosition;
    private Position lastMinePosition;
    private Position robotPosition;

    public GameField(int column, int row, char orientation, int startX, int startY) {
        this.column=column;
        this.row=row;
        this.grid = new int[row][column];
        this.orientation = orientation;
        startPosition = new Position(startX, startY);
        lastMinePosition = null;
        robotPosition = new Position(startX,startY);
    }

    public int getRow(){
        return this.row;
    }

    public int getColumn(){
        return this.column;
    }

    public void setRow(int row){
        this.row=row;
    }

    public void setColumn(int column){
        this.column=column;
    }
    public int[][] getGrid() {
        return grid;
    }

    public char getOrientation() {
        return orientation;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getLastMinePosition() {
        return lastMinePosition;
    }

    public Position getRobotPosition(){
        return robotPosition;
    }

    public void setStartPosition(int x, int y) {
        startPosition.setX(x);
        startPosition.setY(y);
    }

    public void setLastMinePosition(int x, int y) {
        lastMinePosition.setX(x);
        lastMinePosition.setY(y);
    }

    public void setRobotPosition(int x, int y){
        robotPosition.setX(x);
        robotPosition.setY(y);
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }
}
