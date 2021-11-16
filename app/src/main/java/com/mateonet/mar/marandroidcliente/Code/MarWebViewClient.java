package com.mateonet.mar.marandroidcliente.Code;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.mateonet.mar.marandroidcliente.AboutActivity;

/**
 * Created by Domingo on 2/8/2016.
 */
public class MarWebViewClient extends WebViewClient {
       // WebView mPrintWebView;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            /*if (url.toLowerCase().contains("/printing/")) {
                mPrintWebView =new WebView(view.getContext());
                mPrintWebView.setVisibility(View.INVISIBLE);
                mPrintWebView.setWebViewClient(new PrintWebViewClient());
                mPrintWebView.loadUrl(url);
                return true;
            }*/
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            MarSession.startAnimation();
            MarSession.setValue(MarSession.BROWSER_LOADING_URL, url);
            MarSession.setValue(MarSession.BROWSER_LAST_ERROR, null);
            view.setVisibility(View.INVISIBLE);
            MarSession.showToast("Espere... contactando central.");
        }

        @Override
        public  void  onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handleNavigationError(view, null, error.toString());
        }

/*        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse error) {
            handleNavigationError(view,request,error.toString());
        }*/

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            handleNavigationError(view, request, error.toString());
        }

        private void handleNavigationError(WebView view, WebResourceRequest request, String error) {
            view.stopLoading();
            if (error==null) {
                error="Error desconocido";
            }
            MarSession.setValue(MarSession.BROWSER_LAST_ERROR, error);
            //if (!MarSession.getServerValid()) {
            //    launchAboutActivity(view, "Error validando servidor: " + error);
            //} else {
                MarSession.showToast( "Error de comunicacion: " + error);
            //}
        }

        private  void launchAboutActivity(final WebView pView, String mensaje){
            if (MarSession.isAboutOpen()) {
                return;
            }
            MarSession.isAboutOpen(true);
            Intent intent = new Intent(pView.getContext(), AboutActivity.class);
            intent.putExtra("msg",mensaje);
            pView.getContext().startActivity(intent);
        }

        //private int validServerPending=0;
        private  boolean mSignatureFirstAttempt =false;
        private android.os.Handler mSignatureHandler;
        private Runnable mSignatureRunnable;
        @Override
        public void onPageFinished(final WebView view, final String url) {
            if (mSignatureHandler!=null && mSignatureRunnable!=null) {
                // Cancel previous callbacks
                mSignatureHandler.removeCallbacks(mSignatureRunnable);
            }
            mSignatureFirstAttempt =false;

            String lastError=MarSession.getValue(MarSession.BROWSER_LAST_ERROR);
            if (lastError!=null){
                launchAboutActivity(view, lastError);
                MarSession.cancelToast();
            }

            if (MarSession.getServerValid()) {
                view.setVisibility(View.VISIBLE);
                MarSession.cancelToast();
            } else {
                final MarWebViewClient that=this;
                mSignatureRunnable=new Runnable() {
                    WebView pView = view;
                    public void run() {
                        if (MarSession.getServerValid()) {
                            pView.setVisibility(View.VISIBLE);
                            MarSession.stopAnimation();
                            MarSession.cancelToast();
                        } else {
                            if (mSignatureFirstAttempt && mSignatureRunnable!=null) {
                                mSignatureFirstAttempt=false;
                                MarSession.showToast("Esperando identidad del servidor ...");
                                mSignatureHandler.postDelayed(mSignatureRunnable,5000);
                            } else {
                                if (pView.getVisibility() != View.VISIBLE
                                        && !MarSession.isAboutOpen()) {
                                    //MarWebInterface.signDevice hasn't been called after 5 seconds of last load
                                    launchAboutActivity(pView, "No se identifico el servidor.");
                                    MarSession.cancelToast();
                                } else {
                                    MarSession.showToast("SERVIDOR DESCONOCIDO - DEBE SALIR", Toast.LENGTH_LONG);
                                }
                                MarSession.stopAnimation();
                            }
                        }
                        mSignatureHandler=null;
                        mSignatureRunnable=null;
                    }
                };
                mSignatureHandler=new android.os.Handler();
                mSignatureFirstAttempt = true;
                mSignatureHandler.postDelayed(mSignatureRunnable,5000);
            }
        }
    }

