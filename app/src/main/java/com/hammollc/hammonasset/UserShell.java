package com.hammollc.hammonasset;

/**
 * Created by williamhessert on 9/26/18.
 */

public class UserShell {

    private String fName, lName, hDate, ssn;

    public UserShell(String fName, String lName, String hDate, String ssn) {
        this.fName = fName;
        this.lName = lName;
        this.hDate = hDate;
        this.ssn = ssn;
    }

    public String getfName() { return fName; }

    public String getlName() { return lName; }

    public String gethDate() { return hDate; }

    public String getSsn() { return ssn; }
}
