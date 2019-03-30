package com.hammollc.hammonasset;

import java.util.ArrayList;

/**
 * Created by williamhessert on 3/23/19.
 */

public class HourDataBlock {

    String date, time;
    ArrayList<String[]> blocks;
    String foreman, sTime, eTime, tHours;
    boolean approved, unclaimed, unfinished;

    public HourDataBlock(String date, String time) {
        this.date = date;
        this.time = time;

        blocks = new ArrayList<>();
        foreman = "";
        sTime = "";
        eTime = "";
        tHours = "";
    }

    public ArrayList<String[]> getBlocks() {
        return blocks;
    }

    public String geteTime() {
        return eTime;
    }

    public String getForeman() {
        return foreman;
    }

    public String getsTime() {
        return sTime;
    }

    public String gettHours() {
        return tHours;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isUnclaimed() {
        return unclaimed;
    }

    public boolean isUnfinished() {
        return unfinished;
    }

    public void setBlocks(ArrayList<String[]> blocks) {
        this.blocks = blocks;
    }

    public void seteTime(String eTime) {
        this.eTime = eTime;
    }

    public void setForeman(String foreman) {
        this.foreman = foreman;
    }

    public void setsTime(String sTime) {
        this.sTime = sTime;
    }

    public void settHours(String tHours) {
        this.tHours = tHours;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setUnclaimed(boolean unclaimed) {
        this.unclaimed = unclaimed;
    }

    public void setUnfinished(boolean unfinished) {
        this.unfinished = unfinished;
    }
}
