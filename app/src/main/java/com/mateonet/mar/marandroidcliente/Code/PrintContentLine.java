package com.mateonet.mar.marandroidcliente.Code;

/**
 * Created by Domingo on 3/9/2016.
 */
public class PrintContentLine {
    String mSize;
    String mContent;

    public PrintContentLine(String pContent, String pSize) {
        mContent=pContent;
        mSize=pSize;
    }

    public String Size() {
        return  mSize;
    }
    public  String Content() {
        return  mContent;
    }
}
