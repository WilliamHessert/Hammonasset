package com.hammollc.hammonasset;

/**
 * Created by williamhessert on 11/7/18.
 */

public class Block {

    private String type, classification;
    private int hours;

    public Block(String type, String classification, int hours) {
        this.type = type;
        this.classification = classification;
        this.hours = hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getType() { return type; }

    public String getClassification() {
        return classification;
    }

    public int getHours() { return hours; }
}
