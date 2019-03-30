package com.hammollc.hammonasset;

import android.graphics.drawable.Drawable;

/**
 * Created by williamhessert on 10/3/18.
 */

public class Item {

    String title, subtitle, code;
    int pic;

    public Item(String title, String subtitle, String code, int pic) {
        this.title = title;
        this.subtitle = subtitle;
        this.code = code;
        this.pic = pic;
    }

    public String getTitle() { return title; }

    public String getSub() { return subtitle; }

    public String getCode() { return code; }

    public int getPic() { return pic; }

    public void setSub(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setPic(int pic) {
        this.pic = pic;
    }
}
