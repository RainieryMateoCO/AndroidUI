package com.mateonet.mar.marandroidcliente.Code;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.mateonet.mar.marandroidcliente.AboutActivity;
import com.mateonet.mar.marandroidcliente.utils.SunmiPrintHelper;

import java.util.ArrayList;

/**
 * Created by Domingo on 2/8/2016.
 */
public class MarWebInterface {
    Context mContext;
    WebView mWebView;
    PrintInterface mPRN;
    AboutActivity aboutActivity;


    public  MarWebInterface(Context pContext, WebView pWebView, PrintInterface pPRN) {
        mContext = pContext;
        mWebView = pWebView;
        mPRN=pPRN;
        aboutActivity = new AboutActivity();
    }
    @JavascriptInterface
    public String ReadVersion() {
        return "MAR Android" + MarSettings.ReadVersions();
    }

    @JavascriptInterface
    public String ReadGPS() {
        return MarSettings.getGPSCoordinates();
    }

    @JavascriptInterface
    public void alert(String pMessage) {
        MarSession.showToast(pMessage, Toast.LENGTH_LONG);
    }

    @JavascriptInterface
    public boolean signDevice(String pKey) {
        int seed = Integer.valueOf(pKey.substring(0,4));
        String tail =Integer.toString((seed * 43231) % 100000);
        if (pKey.equals(Integer.toString(seed)+tail)) {
            MarSession.setServerValid();
            return true;
        } else {
            return false;
        }
    }

    @JavascriptInterface
    public String ReadConfig() {
        return  MarSettings.getConfiguration();
    }

    @JavascriptInterface
    public void WriteConfig(String[] pConfig) {
        MarSettings.setConfiguration(pConfig);
    }

    @JavascriptInterface
    public void ChangeServerURL(String pServerURL) {
        MarSettings.setServerURL(pServerURL);
    }

    @JavascriptInterface
    public void PrintOut(String pContent, String pOptions) {
        aboutActivity = new AboutActivity();
        if (pContent==null) return;
        String[] theLineas=pContent.split("~i~");
        ArrayList<PrintContentLine> theContent=new ArrayList<PrintContentLine>();
        for (int i=0;i<theLineas.length;i++) {
            String[] theComp=theLineas[i].split("~s~");
            theContent.add(new PrintContentLine(theComp[0], (theComp.length==1 ? "1" : theComp[1])));
        }
        String theSelectedPrinter="1";
        String theQRCode="";
        String theCompo="";
        boolean ImprimeLogo=false;
        ArrayList<PrintOption> theOptions=null;

        shareTicket shareticket = new shareTicket();
        try{
            if (pOptions!=null) {
                String[] theOpts=pOptions.split("~i~");
                theOptions=new ArrayList<PrintOption>();

                if(theOpts.length > 1) {
                    for (int i = 0; i < theOpts.length; i++) {
                        String[] theComp = theOpts[i].split("~s~");

                        if (theComp != null || theComp.length > 0) {
                            if (theComp[0] != null || theComp[0] != "") {
                                theCompo = theComp[0];

                                if (theCompo.equalsIgnoreCase("Prn") == true) {
                                    if (theComp[1] != null && theComp[1] != "") {
                                        theSelectedPrinter = theComp[1];
                                    }
                                }
                                if (theCompo.equalsIgnoreCase("QRCode64") == true) {
                                    if (theComp[1] != null && theComp[1] != "") {
                                        theQRCode = theComp[1];
                                    }
                                }
                                if (theCompo.equalsIgnoreCase("ImprimeLogo")) {
                                    if (theComp[1] != null && theComp[1] != "") {
                                        if (theComp[1].equalsIgnoreCase("true") || theComp[1].equalsIgnoreCase("false")) {
                                            ImprimeLogo = true;
                                        }
                                    }
                                }
                                if (theCompo.equalsIgnoreCase("PrintWhatsapp")) {
                                    if (theComp[1].equalsIgnoreCase("TRUE")) {
                                        shareticket.shareTicketGenerated(mContext, theContent, theQRCode);
                                        return;
                                    }
                                }
                            }
                        } else if (theOpts.length == 1) {
                            theComp = theOpts[0].split("~s~");
                            theSelectedPrinter = theComp[1];
                        }
                    }
                }
                /**
                 String[] theComp=theOpts[i].split("~s~");
                 if (i==0) {
                 theSelectedPrinter=theComp[1];
                 if(theComp.length >= 2){theQRCode = theComp[2];}
                 }
                 else {
                 theOptions.add(new PrintOption(theComp[0], theComp[1]));
                 }
                 }**/
            }
        }catch (Exception e){
            Toast.makeText(aboutActivity.getBaseContext(),e.getMessage().toString(),Toast.LENGTH_LONG);
        }

        if (theSelectedPrinter.equals("1") || theSelectedPrinter.equals("7")) {
            try {
                if(aboutActivity.VeficarExistenciaLogo()==true && ImprimeLogo == true){
                    aboutActivity.ImprimirLogo(null);
                    while (aboutActivity.ImprimioLogo==false){}
                    try {Thread.sleep(2000);}catch (Exception e){}
                }
                mPRN.BT_PrintOut1(theContent, theOptions);
                while(mPRN.mImprimio==false){}
                try {Thread.sleep(1000);}catch (Exception e){}
                if(theQRCode.length() > 0){ aboutActivity.ImprimirQRCode(theQRCode);}
                else{SunmiPrintHelper.getInstance().feedPaper();}
            }catch (Exception e){
                Toast.makeText(aboutActivity.getBaseContext(),e.getMessage().toString(),Toast.LENGTH_LONG);
            }
        } else  if (theSelectedPrinter.equals("2")) {
            try {
                if(aboutActivity.VeficarExistenciaLogo()==true && ImprimeLogo == true){
                    aboutActivity.ImprimirLogo(null);
                    while (aboutActivity.ImprimioLogo==false){}
                    try {Thread.sleep(2000);}catch (Exception e){}
                }
                mPRN.TelpoMEGA_PrintOut1(theContent, theOptions);
                while(mPRN.mImprimio==false){}
                try {Thread.sleep(1000);}catch (Exception e){}
                if(theQRCode.length() > 0){ aboutActivity.ImprimirQRCode(theQRCode);}
                else{SunmiPrintHelper.getInstance().feedPaper();}
            }catch (Exception e){
                Toast.makeText(aboutActivity.getBaseContext(),e.getMessage().toString(),Toast.LENGTH_LONG);
            }
        } else  if (theSelectedPrinter.equals("3")) {
            try {
                if(aboutActivity.VeficarExistenciaLogo()==true && ImprimeLogo == true){
                    aboutActivity.ImprimirLogo(null);
                    while (aboutActivity.ImprimioLogo==false){}
                    try {Thread.sleep(2000);}catch (Exception e){}
                }
                mPRN.TelpoUsbMEGA_PrintOut1(theContent, theOptions);
                while(mPRN.mImprimio==false){}
                try {Thread.sleep(1000);}catch (Exception e){}
                if(theQRCode.length() > 0){ aboutActivity.ImprimirQRCode(theQRCode);}
                else{SunmiPrintHelper.getInstance().feedPaper();}
            }catch (Exception e){
                Toast.makeText(aboutActivity.getBaseContext(),e.getMessage().toString(),Toast.LENGTH_LONG);
            }
        } else {
            alert("Impresora configurada no es valida.");
        }

    }

}