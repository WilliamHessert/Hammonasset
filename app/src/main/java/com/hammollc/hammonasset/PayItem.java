package com.hammollc.hammonasset;

import android.widget.EditText;

/**
 * Created by williamhessert on 1/11/19.
 */

public class PayItem {

    private EditText editText;
    private String code, name, unit, value;

    public PayItem(String code, String name, String unit) {
        this.code = code;
        this.name = name;
        this.unit = unit;
        value = "";
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getSimpleValue() {
        int index = value.indexOf("~");
        return value.substring(index+1);
    }

    public void setEditText(EditText editText) { this.editText = editText; }

    public EditText getEditText() { return editText; }
}
