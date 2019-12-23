package com.example.brainlegostormingapp;

import android.view.View;

public class GameField {
    private int row;
    private int column;
    private int[][] grid;
    private char orientation;
    private Position startPosition;
    private Position lastMinePosition;
    private Position robotPosition;
    private PixelGridView pixelGrid;

    public GameField(int row, int column, char orientation, int startX, int startY, PixelGridView pixelGrid) {
        this.pixelGrid = pixelGrid;
        this.row=row;
        this.column=column;
        this.orientation = orientation;
        if (this.orientation == 'e' || this.orientation == 'o')
        {
            this.row=column;
            this.column=row;
        }
        this.grid = new int[this.row][this.column];
        startPosition = new Position(startX, startY);
        lastMinePosition = new Position(0,0);
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
        pixelGrid.cellUncheck(robotPosition.getX(),robotPosition.getY(),"ROBOT");
        robotPosition.setX(x);
        robotPosition.setY(y);
        pixelGrid.cellCheck(x,y,"ROBOT");
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }
}
