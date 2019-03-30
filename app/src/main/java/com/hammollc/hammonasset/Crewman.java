package com.hammollc.hammonasset;

import java.util.ArrayList;

/**
 * Created by williamhessert on 1/9/19.
 */

public class Crewman {

    private String name, pNum;
    private ArrayList<Block> blocks;

    public Crewman(String name, String pNum, ArrayList<Block> blocks) {
        this.name = name;
        this.pNum = pNum;
        this.blocks = blocks;
    }

    public String getName() {
        return name;
    }

    public String getNum() {
        return pNum;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }
}
