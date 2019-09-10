package com.hammollc.hammonasset;

public class AddressBlock {

    private String address, city, state, zip;

    public AddressBlock() {
        this.address = "";
        this.city = "";
        this.state = "";
        this.zip = "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getZip() {
        return zip;
    }
}
