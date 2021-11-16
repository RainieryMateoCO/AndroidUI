package com.mateonet.mar.marandroidcliente.Code;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.mateonet.mar.marandroidcliente.MainActivity;

import static com.mateonet.mar.marandroidcliente.MainActivity.GPS_PERMISSION_REQUEST;
import static com.mateonet.mar.marandroidcliente.MainActivity.PHONE_STATUS_PERMISSION_REQUEST;

/**
 * Created by Domingo on 2/8/2016.
 */
public class MarSettings {

    public static final String APK_VERSION = "1.6";
    private static Context mContext;
    private static Activity mActivity;

    private static SharedPreferences getSettings() {
        return mContext.getSharedPreferences("MAR.Client.Settings", Context.MODE_PRIVATE);
    }

    public static String getServerURL() {
        return getSettings().getString("marServerURL", null);
    }

    public static void setServerURL(String pUrl) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString("marServerURL", pUrl);
        editor.commit();
        MarSession.setServerInvalid();
    }

    public static String getDefaultPrinter() {
        return getSettings().getString("marDefaultPrinter", null);
    }

    public static void setDefaultPrinter(String pPrinter) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString("marDefaultPrinter", pPrinter);
        editor.commit();
    }

    public static String getConfiguration() {
        String theConfig = "";
        int idx = 0;
        SharedPreferences theSettings = getSettings();
        while (true) {
            String iStr = theSettings.getString("marConfig" + String.valueOf(idx), "**EndOfValueList**");
            if (iStr == "**EndOfValueList**") {
                break;
            } else {
                if (idx > 0) theConfig += "*|*";
                theConfig += iStr;
                idx++;
                if (idx > 500) break;
            }
        }
        return theConfig;
    }

    public static void setConfiguration(String[] pConfig) {
        SharedPreferences.Editor editor = getSettings().edit();
        for (int i = 0; i <= pConfig.length; i++) {
            if (i < pConfig.length) {
                editor.putString("marConfig" + String.valueOf(i), pConfig[i]);
            } else {
                editor.putString("marConfig" + String.valueOf(i), "**EndOfValueList**");
            }
        }
        editor.commit();
    }

    public static void setContext(Context pContext, Activity pActivity) {
        mContext = pContext;
        mActivity = pActivity;

        String apkVersion = "APK Version: " + MarSettings.APK_VERSION;
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString("marVersion0", apkVersion);
        editor.commit();

        // Run in separate thread to Request Permissions
        (new android.os.Handler()).postDelayed(
                new Runnable() {
                    public void run() {
                        int permissionPhone = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE);
                        if (permissionPhone!= PackageManager.PERMISSION_GRANTED) {
//                            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_PHONE_STATE)) {
//                                // Prompt user with explanation
//                            } else {
                                // Prompt user with permission request
                                ActivityCompat.requestPermissions(mActivity,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        PHONE_STATUS_PERMISSION_REQUEST);
//                            }
                        } else {
                            gatherPhoneDetails();
                        }

                        int permissionGPS = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionGPS!= PackageManager.PERMISSION_GRANTED) {
//                            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_PHONE_STATE)) {
//                                // Prompt user with explanation
//                            } else {
                            // Prompt user with permission request
                            ActivityCompat.requestPermissions(mActivity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    GPS_PERMISSION_REQUEST);
//                            }
                        }

                    }
                },1000);

    }

    @SuppressLint("MissingPermission")
    public static void gatherPhoneDetails() {
        try {
            String simSerial = "";
            String simNumber = "";

            TelephonyManager telemamanger = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            simSerial = telemamanger.getSimSerialNumber();
            simNumber = telemamanger.getLine1Number();

            SharedPreferences.Editor editor = getSettings().edit();
            if (simSerial!=null && simSerial.trim().length()>0) {
                editor.putString("marVersion1", "SIM Serial: " + simSerial);
            } else {
                editor.remove("marVersion1");
            }
            if (simNumber!=null && simNumber.trim().length()>0) {
                editor.putString("marVersion2", "SIM Number: " + simNumber);
            } else {
                editor.remove("marVersion2");
            }
            editor.commit();
        } catch (Exception ex) {
            String err=ex.getMessage();
            err.toLowerCase();
            // debug here
        }
    }
    public static void deletePhoneDetails() {
        SharedPreferences lector=getSettings();

        String simSerial = lector.getString("marVersion1",null);
        String simNumber = lector.getString("marVersion2",null);;

        SharedPreferences.Editor editor = lector.edit();
        if (simSerial!=null && simSerial.trim().length()>0) {
            editor.putString("marVersion1", simSerial.replace(" (viejo)","")+" (viejo)");
        } else {
            editor.remove("marVersion1");
        }
        if (simNumber!=null && simNumber.trim().length()>0) {
            editor.putString("marVersion2", simNumber.replace(" (viejo)","")+" (viejo)");
        } else {
            editor.remove("marVersion2");
        }

        editor.commit();
    }

    public static String getGPSCoordinates() {
        String result="||No tiene permiso a GPS";
        int permissionGPS = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionGPS == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        result = String.valueOf(loc.getLatitude())+"|"+String.valueOf(loc.getLongitude())+"|OK";
                    } else {
                        loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        if (loc!=null) {
                            result = String.valueOf(loc.getLatitude()) + "|" + String.valueOf(loc.getLongitude()) + "|OK";
                        } else {
                            result="||Ubicacion desconocida";;
                        }
                    }
                } else {
                    result="||GPS Desactivado";;
                }
            } catch (Exception ex) {
                result="||ERROR:" + ex.getMessage();
            }
        }
        return result;
    }

    public static String ReadVersions() {
        String versions="";
        SharedPreferences theSettings = getSettings();
        String apk=theSettings.getString("marVersion0",null);
        String simS=theSettings.getString("marVersion1", null);
        String simN=theSettings.getString("marVersion2",null);
        if (apk!=null && apk.length()>0) {
            versions = versions + "|" + apk;
        }
        if (simS!=null && apk.length()>0) {
            versions = versions + "|" + simS;
        }
        if (simN!=null && apk.length()>0) {
            versions = versions + "|" + simN;
        }
        return  versions;
    }
}
