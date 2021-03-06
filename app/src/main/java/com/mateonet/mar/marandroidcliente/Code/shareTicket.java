package com.mateonet.mar.marandroidcliente.Code;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.StringUtils;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;
import com.mateonet.mar.marandroidcliente.BuildConfig;
import com.mateonet.mar.marandroidcliente.MainActivity;
import com.mateonet.mar.marandroidcliente.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class shareTicket {
    private void ticketGenerator(Context context, ArrayList<PrintContentLine> theContent, String theQRCode) {
        String textOption = "", textBase = "";
        Integer heightIncremental = 0;
        Integer titleBaseY = 50, titleBaseX = 150, bodyBaseDX = 0, bodyBaseDY = 70;

        //Document and page configuration
        PdfDocument pdfDocument = new PdfDocument();

        try {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "font/cour.ttf");
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(500, theContent.size()*100, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(pageInfo);

        //Creation of canvas or "lienzo"
        Canvas canvas = myPage.getCanvas();

        // Drawing logo on canvas
             try{
                File dir = new File(Environment.getExternalStorageDirectory(), "tickets/logo.png");
                if(dir.exists()){
                    Bitmap logoBitmap = BitmapFactory.decodeFile(dir.getAbsolutePath());
                    Bitmap scaleLogo = Bitmap.createScaledBitmap(logoBitmap, 150, 150, false);
                    canvas.drawBitmap(scaleLogo, 170, bodyBaseDY-50, paint);
                    bodyBaseDY = bodyBaseDY + 135;
                }

            }catch (Exception e){
                e.printStackTrace();
                }

        //paint text settings
        paint.setTextSize(20);
        paint.setColor(ContextCompat.getColor(context, android.R.color.black));
        //canvas.drawText("Ticket de impresion", titleBaseX, titleBaseY, paint);

        paint.setColor(ContextCompat.getColor(context, R.color.black));
        paint.setTypeface(typeface);

        for(Integer index = 0; index < theContent.size(); index++) {
            bodyBaseDX = 75;

            textOption = theContent.get(index).mSize;
            textBase = theContent.get(index).mContent;

            if (textOption.equals("2")) {
                typeface = Typeface.createFromAsset(context.getAssets(), "font/courbd.ttf"); // Bold font
                heightIncremental = 23;
                paint.setTextSize(18);
                paint.setTypeface(typeface);
                //paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }if (textOption.equals("1")) {
                typeface = Typeface.createFromAsset(context.getAssets(), "font/cour.ttf"); // Normal font
                heightIncremental = 20;
                paint.setTextSize(16);
                paint.setTypeface(typeface);
                //paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            canvas.drawText(textBase, bodyBaseDX, bodyBaseDY, paint);
            bodyBaseDY = bodyBaseDY+heightIncremental;
        }

        // Creation of QRCODE
        if (!theQRCode.isEmpty()){
            Bitmap scaleQR = Bitmap.createScaledBitmap(encodeAsBitmap(theQRCode), 135, 135, false);
            canvas.drawBitmap(scaleQR, titleBaseX+19, bodyBaseDY-20, paint);
        }

        //Canvas save and restore
        canvas.save();
        canvas.restore();

        //Writing finished
        pdfDocument.finishPage(myPage);

        //File creation
        File file = new File(Environment.getExternalStorageDirectory(), "tickets/ticketMAR.pdf");

        //End processor
        pdfDocument.writeTo(new FileOutputStream(file));
        Toast.makeText(context, "Compartiendo ticket.", Toast.LENGTH_SHORT).show();
        } catch (IOException | WriterException e) {
            e.printStackTrace();
            Toast.makeText(context, "No he podido imprimir el ticket.", Toast.LENGTH_SHORT).show();
        }
        //Close pdf thread
        pdfDocument.close();
    }

    public void shareTicketGenerated(Context context, ArrayList<PrintContentLine> theContent, String theQRCode) {
        try{
            int androidVersion = Build.VERSION.SDK_INT;
            ticketGenerator(context, theContent, theQRCode);

            switch(androidVersion){
                case 24:
                case 25:
                    // Android version 7.1 - 7.1.1
                    shareWithAndroid7(context);
                    break;
                case 26:
                    // Android version 8
                    shareWithAndroid7(context);
                    break;
                case 27:
                    // Android version 8.1
                    shareWithAndroid7(context);
                    break;
                case 28:
                    // Android 9
                    shareWithAndroid9(context);
                    break;
                case 29:
                case 30:
                    // Android 10 and 11
                    shareWithAndroid11And10(context);
                    break;
                default:
                    Toast.makeText(context, "No disponible en la versi??n "+androidVersion+" comuniquese con nuestro servicio al cliente.", Toast.LENGTH_SHORT).show();
                    break;
            }
      } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void shareWithAndroid7(Context context){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");

        File pdfFile = new File(Environment.getExternalStorageDirectory(), "tickets/ticketMAR.pdf");
        Uri uri = Uri.fromFile(pdfFile);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Compartiendo ticket a imprimir.").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void shareWithAndroid8(Context context){

    }

    private void shareWithAndroid9(Context context){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");

        File pdfFile = new File(Environment.getExternalStorageDirectory(), "tickets/ticketMAR.pdf");
        Uri uri = Uri.fromFile(pdfFile);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Compartiendo ticket a imprimir.").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void shareWithAndroid11And10(Context context){
        File file = new File(Environment.getExternalStorageDirectory(), "tickets/ticketMAR.pdf");

        Uri uri = FileProvider.getUriForFile(context, "com.mateonet.mar.marandroidcliente", file);

        Intent intent = new Intent();

        intent.setDataAndType(uri, context.getContentResolver().getType(uri));
        int uid = Binder.getCallingUid();
        String callingPackage = context.getPackageManager().getNameForUid(uid);

        context.getApplicationContext().grantUriPermission(callingPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/pdf");

        intent.putExtra(Intent.EXTRA_STREAM, uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //QR code Encoder
    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 100, 100, null);
        } catch (IllegalArgumentException iae) {
            return null;
        }
        Integer WHITE = 0xFFFFFFFF;
        Integer BLACK = 0xFF000000;
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 100, 0, 0, w, h);
        return bitmap;
    }
}
