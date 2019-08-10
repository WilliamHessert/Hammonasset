package com.hammollc.hammonasset;

/**
 * Created by williamhessert on 8/8/19.
 */

public class HourOption {

    private String label, classification;
    private boolean standard;

    public HourOption(String label, String classification, String sString) {
        this.label = label;
        this.classification = classification;
        standard = Boolean.parseBoolean(sString);
    }

    public String getLabel() {
        return label;
    }

    public String getClassification() {
        return classification;
    }

    public boolean isStandard() {
        return standard;
    }
}
