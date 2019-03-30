package com.hammollc.hammonasset;

import java.util.ArrayList;

/**
 * Created by williamhessert on 3/25/19.
 */

public class Crew {

    boolean report, approve;
    ArrayList<String[]> crewData;
    String week, date, time, poNumber;

    public Crew(String week, String date, String time, String poNumber) {
        this.week = week;
        this.date = date;
        this.time = time;
        this.poNumber = poNumber;
    }

    public String getWeek() {
        return week;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public boolean isReport() {
        return report;
    }

    public boolean isApprove() {
        return approve;
    }

    public ArrayList<String[]> getCrewData() {
        return crewData;
    }

    public void setReport(boolean report) {
        this.report = report;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public void setCrewData(ArrayList<String[]> crewData) {
        this.crewData = crewData;
    }
}
