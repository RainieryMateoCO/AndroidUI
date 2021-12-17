package com.mateonet.mar.marandroidcliente;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.mateonet.mar.marandroidcliente.Code.MarSession;
import com.mateonet.mar.marandroidcliente.Code.MarSettings;
import com.mateonet.mar.marandroidcliente.Code.MarWebInterface;
import com.mateonet.mar.marandroidcliente.Code.MarWebViewClient;
import com.mateonet.mar.marandroidcliente.Code.PrintInterface;
import com.mateonet.mar.marandroidcliente.Code.Reciever;
import com.mateonet.mar.marandroidcliente.utils.SunmiPrintHelper;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private MarWebInterface mWebInterface;
    private Reciever reciever;
    private PrintInterface mPrnInterface;
    public static final int PHONE_STATUS_PERMISSION_REQUEST = 1;
    public static final int GPS_PERMISSION_REQUEST = 2;

    /**
     * Connect print service through interface library
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mContext=getApplicationContext();
        MarSettings.setContext(mContext, this);
        MarSession.setContext(mContext, this);
        MarSession.IsToastEnabled=true;
        MarSession.clear();
        MarSession.isAboutOpen(false);

        RotateAnimation anim = new RotateAnimation(0f, 360f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(650);
        final ImageView loader = (ImageView) findViewById(R.id.loader);
        MarSession.setupAnimation(loader,anim);

        String theUrl=MarSettings.getServerURL();
        if (theUrl==null || theUrl.trim().equals("")) {
            MarSettings.setServerURL("http://www.google.com/");
        }

        WebView theBrowser = (WebView)this.findViewById(R.id.webView);
        theBrowser.getSettings().setJavaScriptEnabled(true);
        theBrowser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        theBrowser.setWebViewClient(new MarWebViewClient());
        mPrnInterface=new PrintInterface(this);
        mWebInterface=new MarWebInterface(mContext,theBrowser,mPrnInterface);
        theBrowser.addJavascriptInterface(mWebInterface, "DroidMAR");
        theBrowser.addJavascriptInterface(new JavascriptDownloadInterface(), "AndroidDownload");

    }

    private void init(){
        //Download log register
        reciever = new Reciever(MainActivity.this);
        reciever.register(reciever);

        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
    }

    public class JavascriptDownloadInterface {
        //Javascript interface to download the apk version
        @JavascriptInterface
        public void DownloadVersion(String url) {
            reciever.Download(url);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PHONE_STATUS_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MarSettings.gatherPhoneDetails();
                } else {
                    MarSettings.deletePhoneDetails();
                }
                return;
            }
            case GPS_PERMISSION_REQUEST: {

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        WebView theBrowser = (WebView)this.findViewById(R.id.webView);
        if (MarSession.getServerValid()) {
            if (MarSession.getValue(MarSession.BROWSER_LAST_ERROR)!=null) {
                theBrowser.loadUrl(MarSession.getValue(MarSession.BROWSER_LOADING_URL));
            }
        } else {
            theBrowser.loadUrl(MarSettings.getServerURL() + "/AndroidUI/Out/SignIn");
        }

        reciever.register(reciever);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reciever.wipeRegister(reciever);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PrintInterface.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    MarSession.showToast("Bluetooth Encendido", Toast.LENGTH_LONG);
                    mPrnInterface.BT_PrintOut1(null, null);
                } else {
                    MarSession.showToast("Habilite bluetooth para imprimir", Toast.LENGTH_LONG);
                }
                break;
            case PrintInterface.REQUEST_CONNECT_BTDEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(PrintBTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice con_dev = mPrnInterface.getBTService1().getDevByMac(address);
                    mPrnInterface.getBTService1().connect(con_dev);
                }
                break;
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    boolean tripleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (tripleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            tripleBackToExitPressedOnce = true;
            doubleBackToExitPressedOnce = false;
            MarSession.showToast("Presione tres veces para cerrar");
            WebView theBrowser = (WebView)this.findViewById(R.id.webView);
            theBrowser.loadUrl(MarSettings.getServerURL() + "/AndroidUI/Out/SignIn");
        } else {
            doubleBackToExitPressedOnce = true;
            MarSession.showToast("Presione dos veces para salir");
        }
        int delayTime = doubleBackToExitPressedOnce ? 3000 : 1500;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                tripleBackToExitPressedOnce = false;
                doubleBackToExitPressedOnce = false;
            }
        }, delayTime);
    }

    @Override
    public  void onDestroy() {
        MarSession.isAboutOpen(true); // evita que se vuelva a abrir el about
        MarSession.IsToastEnabled=false;
        MarSession.stopAnimation();
        super.onDestroy();
    }
}
