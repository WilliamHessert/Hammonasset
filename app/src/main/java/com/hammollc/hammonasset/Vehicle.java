package com.hammollc.hammonasset;

public class Vehicle {

    private String id, name, plates;

    public Vehicle(String id, String name, String plates) {
        this.id = id;
        this.name = name;
        this.plates = plates;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlates() {
        return plates;
    }
}
