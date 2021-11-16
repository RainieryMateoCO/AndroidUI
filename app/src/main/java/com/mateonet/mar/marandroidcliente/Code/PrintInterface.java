package com.mateonet.mar.marandroidcliente.Code;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.mateonet.mar.marandroidcliente.PrintBTDeviceListActivity;
import com.mateonet.mar.marandroidcliente.TelpoPrinterEngine;
import com.mateonet.mar.marandroidcliente.TelpoUsbPrinterEngine;
import com.telpo.tps550.api.printer.ThermalPrinter;
import com.telpo.tps550.api.printer.UsbThermalPrinter;
import com.zj.btsdk.BluetoothService;

import java.util.ArrayList;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by Domingo on 3/9/2016.
 */
public  class  PrintInterface {
    Context mContext;
    Activity mActivity;
    ArrayList<PrintContentLine> mPendingContentLines;
    ArrayList<PrintOption> mPendingPrintOptions;
    public  PrintInterface(Activity pActivity) {
        mActivity=pActivity;
        mContext=pActivity.getApplicationContext();
        mBTService1PairedFlag=false;
    }

    /*** BLUETOOTH PRINTING PARA PRINTER TIPO 1 ***/
    BluetoothService mBTService1=null;
    boolean mBTService1PairedFlag=false;
    public static boolean mImprimio=false;
    public static final int REQUEST_ENABLE_BT=1001;
    public static final int REQUEST_CONNECT_BTDEVICE=1002;
    public void  BT_PrintOut1(ArrayList<PrintContentLine> pContentLines,ArrayList<PrintOption> pPrintOptions) {
        if (pContentLines==null && pPrintOptions==null) {
            //Reintenta imprimir luego de conectar bluetooth
            if (mPendingContentLines==null && mPendingPrintOptions==null) return;
            pContentLines=mPendingContentLines;
            pPrintOptions=mPendingPrintOptions;
        } else {
            //Mantener en memoria en caso de que bluetooth no este habilitado
            mPendingContentLines = pContentLines;
            mPendingPrintOptions = pPrintOptions;
        }

        if (mBTService1==null) mBTService1=new BluetoothService(mContext,mBTServiceHandler1);

        if(mBTService1.isBTopen() == false)
        {
            //Habilita bluetooth y vuelve a llamar BT_PrintOut desde MainActivity
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mActivity, enableIntent, REQUEST_ENABLE_BT, null);
        } else {
            if (mBTService1PairedFlag==false) {
                //Conecta a Printer
                Intent serverIntent = new Intent(mActivity, PrintBTDeviceListActivity.class);
                startActivityForResult(mActivity, serverIntent, REQUEST_CONNECT_BTDEVICE, null);
            } else {
                mPendingContentLines=null;
                mPendingPrintOptions=null;
                String theLastSize="";
                String thePrintText="";

                byte[] theSizeCmd1 = new byte[3];
                byte[] theSizeCmd2 = new byte[3];
                theSizeCmd1[0] = 0x1b;theSizeCmd1[1] = 0x21;theSizeCmd1[2] &= 0xEF; //Regular
                theSizeCmd2[0] = 0x1b;theSizeCmd2[1] = 0x21;theSizeCmd2[2] |= 0x10; //Grande

                try {
                    for (int i=0;i<pContentLines.size();i++) {
                        PrintContentLine theLine=pContentLines.get(i);
                        if (!theLastSize.equals(theLine.Size())) {
                            if (thePrintText.length()>0) {
                                mBTService1.sendMessage(thePrintText,"GBK");
                                thePrintText="";
                            }
                            theLastSize=theLine.Size();
                            if (theLastSize.equals("1")) {
                                mBTService1.write(theSizeCmd1);
                            } else {
                                mBTService1.write(theSizeCmd2);
                            }
                        }
                        thePrintText=thePrintText+"\n"+theLine.Content();
                    }
                    if (thePrintText.length()>0) {mBTService1.sendMessage(thePrintText+"","GBK");mImprimio=true;};
                } catch (Exception ex) {
                    MarSession.showToast("Error: " + ex.getMessage().toString());
                }
            }
        }
    }

    public BluetoothService getBTService1() {
        return  mBTService1;
    }

    private final Handler mBTServiceHandler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            MarSession.showToast("Printer Conectado");
                            mBTService1PairedFlag=true;
                            BT_PrintOut1(null, null);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    MarSession.showToast("Printer Desconectado");
                    mBTService1PairedFlag=false;
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    MarSession.showToast( "Fallo Conexion a Printer");
                    mBTService1PairedFlag=false;
                    break;
            }
        }

    };


    /*** Printer MEGA Telpo ***/
    TelpoPrinterEngine mTelpoEngine;
    public void TelpoMEGA_PrintOut1(ArrayList<PrintContentLine> pContentLines, ArrayList<PrintOption> pPrintOptions) {
        if (mTelpoEngine ==null) mTelpoEngine =new TelpoPrinterEngine(mActivity);
        mTelpoEngine.Imprime(pContentLines,pPrintOptions);
    }

    /*** Printer USB MEGA Telpo ***/
    TelpoUsbPrinterEngine mUsbThermalPrinter;
    public void TelpoUsbMEGA_PrintOut1(ArrayList<PrintContentLine> pContentLines, ArrayList<PrintOption> pPrintOptions) {
        if (mUsbThermalPrinter ==null) mUsbThermalPrinter =new TelpoUsbPrinterEngine(mActivity);
        mUsbThermalPrinter.Imprime(pContentLines,pPrintOptions);
    }


}
