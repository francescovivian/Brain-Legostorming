package com.example.brainlegostormingapp.Tests;

public class Recovery {
    private int x;
    private int y;
    private Long timestamp;

    public Recovery(int x, int y, Long timestamp){
        this.x=x;
        this.y=y;
        this.timestamp=timestamp;
    }

    public Recovery(){
        super();
    }


    //getters

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    //setters
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void reset(){
        this.x=-1;
        this.y=-1;
        this.timestamp=null;
    }
}
