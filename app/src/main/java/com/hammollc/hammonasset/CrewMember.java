package com.hammollc.hammonasset;

import java.util.ArrayList;

/**
 * Created by williamhessert on 3/27/19.
 */

public class CrewMember {

    ArrayList<Block> blocks;
    boolean reported, approved;
    String id, name, phone, email, sTime, eTime, tHours;

    public CrewMember(String name, ArrayList<Block> blocks, boolean reported, boolean approved) {
        this.name = name;
        this.blocks = blocks;
        this.reported = reported;
        this.approved = approved;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public boolean isReported() {
        return reported;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setsTime(String sTime) {
        this.sTime = sTime;
    }

    public String getsTime() {
        return sTime;
    }

    public void seteTime(String eTime) {
        this.eTime = eTime;
    }

    public String geteTime() {
        return eTime;
    }

    public void settHours(String tHours) {
        this.tHours = tHours;
    }

    public String gettHours() {
        return tHours;
    }
}
