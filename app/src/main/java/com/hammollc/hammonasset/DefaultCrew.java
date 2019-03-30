package com.hammollc.hammonasset;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by williamhessert on 3/1/19.
 */

public class DefaultCrew {

    private String date, name;
    private ArrayList<String> cIds;

    public DefaultCrew(String date, DataSnapshot data) {
        this.date = date;
        handleData(data);
    }

    private void handleData(DataSnapshot data) {
        name = data.child("name").getValue(String.class);
        boolean cont = true;
        int i = 0;

        while(cont) {
            String id = data.child(""+i).getValue(String.class);

            if(id.equals(null) || id.equals(""))
                cont = false;
            else {
                cIds.add(id);
                i++;
            }
        }
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getcIds() {
        return cIds;
    }
}
