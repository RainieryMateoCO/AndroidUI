package com.mateonet.mar.marandroidcliente.Code;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Domingo on 2/9/2016.
 */
public class MarSession {
    public static final String BROWSER_LOADING_URL = "Session.Browser.LoadingUrl";
    public static final String BROWSER_LAST_ERROR = "Session.Browser.LastErrorUrl";
    private static Context mContext;
    private static boolean mIsAboutOpen=false;
    private static ImageView mLoaderImage=null;
    private static RotateAnimation mLoaderAnim;
    private static Activity mActivity;

    public static void setContext(Context ctx, Activity pActivity) {
        mContext = ctx;
        mActivity = pActivity;
    }
    public static void setupAnimation(ImageView imagen, RotateAnimation animacion) {
        stopAnimation();
        mLoaderImage=imagen;
        mLoaderAnim=animacion;
    }
    public static void startAnimation() {
        stopAnimation();
        if (mLoaderImage!=null) {
            mLoaderImage.setAnimation(mLoaderAnim);
            mLoaderImage.setVisibility(View.VISIBLE);
        }
    }
    public static void stopAnimation() {
        if (mLoaderImage!=null) {
            mLoaderImage.setVisibility(View.INVISIBLE);
            mLoaderImage.setAnimation(null);
        }
    }
    public static Context getContext() {
        return mContext;
    }
    public static boolean isAboutOpen() {
        return  mIsAboutOpen;
    }
    public static void isAboutOpen(boolean newValue) {
       mIsAboutOpen=newValue;
    }
    public static String getValue(String pKey) {
        return getSettings().getString(pKey,null);
    }
    public  static Boolean getServerValid() {
        return getSettings().getBoolean("marServerValid", false);
    }
    public  static void setServerValid() {
        SharedPreferences.Editor editor=getSettings().edit();
        editor.putBoolean("marServerValid", true);
        editor.commit();
    }
    public  static void setServerInvalid() {
        SharedPreferences.Editor editor=getSettings().edit();
        editor.putBoolean("marServerValid", false);
        editor.commit();
    }
    public static void setValue(String pKey,String pValue) {
        SharedPreferences.Editor editor=getSettings().edit();
        editor.putString(pKey,pValue);
        editor.commit();
    }

    public static void clear() {
        SharedPreferences.Editor editor=getSettings().edit();
        editor.clear();
        editor.commit();
    }

    private static SharedPreferences getSettings() {
        return mContext.getSharedPreferences("MAR.Client.Session", Context.MODE_PRIVATE);
    }

    private static Toast previousText;
    public static boolean IsToastEnabled;
    public static void showToast(String pMensaje) {
        showToast(pMensaje,Toast.LENGTH_SHORT);
    }
    public static void showToast(String pMensaje, int pDuracion) {
        if (IsToastEnabled) {
            cancelToast();
            Toast currentText=Toast.makeText(mContext, pMensaje, pDuracion);
            currentText.show();
            previousText=currentText;
        }
    }
    public static void cancelToast() {
        if (previousText!=null) {
            previousText.cancel();
            previousText=null;
        }
    }
}
