package com.hammollc.hammonasset;

/**
 * Created by williamhessert on 11/7/18.
 */

public class Block {

    private String type;
    private int hours;

    public Block(String type, int hours) {
        this.type = type;
        this.hours = hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getType() { return type; }

    public int getHours() { return hours; }
}
