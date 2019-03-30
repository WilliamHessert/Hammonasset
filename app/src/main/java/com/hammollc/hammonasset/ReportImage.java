package com.hammollc.hammonasset;

/**
 * Created by williamhessert on 3/16/19.
 */

public class ReportImage {

    String name, desc, file;

    public ReportImage(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getFile() {
        return file;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
