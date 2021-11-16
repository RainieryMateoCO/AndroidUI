package com.mateonet.mar.marandroidcliente.Code;

/**
 * Created by Domingo on 3/9/2016.
 */

public class PrintOption {
    String mKey;
    String mValue;
    public  PrintOption(String pKey, String pValue) {
        mKey=pKey;
        mValue=pValue;
    }

    public String Key() {
        return  mKey;
    }

    public String Value() {
        return  mValue;
    }

}
