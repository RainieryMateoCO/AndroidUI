package com.mateonet.mar.marandroidcliente;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.mateonet.mar.marandroidcliente.Code.MarSession;
import com.mateonet.mar.marandroidcliente.Code.MarSettings;
import com.mateonet.mar.marandroidcliente.utils.BluetoothUtil;
import com.mateonet.mar.marandroidcliente.utils.ESCUtil;
import com.mateonet.mar.marandroidcliente.utils.SunmiPrintHelper;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.datatype.Duration;

public class AboutActivity extends AppCompatActivity {
    static android.content.res.Resources res;
    RequestQueue request;
    OutputStream outputStream;
    EditText textoUrl;
    TextView enlaceLogo;
    public boolean ImprimioLogo;
    long DescargaID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textoUrl =(EditText) findViewById(R.id.editText);
        textoUrl.setText(MarSettings.getServerURL());
        setSupportActionBar(toolbar);
        request = Volley.newRequestQueue(getApplicationContext());
        enlaceLogo =(TextView) findViewById(R.id.enlaceLogo);
        enlaceLogo.setPaintFlags(enlaceLogo.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        PermisosAlmacenamiento();

        /**Comprobando si el dispositivo contiene el logo*/
        File filepath = Environment.getExternalStorageDirectory();
        File dir = new File(filepath.getAbsolutePath(),"/Pictures/logo.png");
        if(dir.exists()){
            enlaceLogo.setText("Actualizar Logo");

        }else{
            enlaceLogo.setText("Descargar Logo");

        }

        final Button btnGrabar = (Button) findViewById(R.id.btnGrabar);
        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText server = (EditText) findViewById(R.id.editText);
                if (server.getVisibility()==View.VISIBLE) {
                    String serverStr = server.getText().toString().trim().toLowerCase();
                    if (!serverStr.startsWith("http://")) serverStr = "http://" + serverStr;
                    Context ctx = getApplicationContext();
                    MarSession.setServerInvalid();
                    MarSession.setValue(MarSession.BROWSER_LAST_ERROR,null);
                    MarSettings.setServerURL(serverStr);
                    server.setText(MarSettings.getServerURL());
                }
                finish();
                VeficarExistenciaLogo();
            }
        });

        final Button btnCambiarCentral  = (Button) findViewById(R.id.btnCambiarCentral);
        btnCambiarCentral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context ctx=getApplicationContext();
                EditText txtServer = (EditText) findViewById(R.id.editText);
                txtServer.setText(MarSettings.getServerURL());
                txtServer.setVisibility(View.VISIBLE);

                TextView lblCentral = (TextView) findViewById(R.id.textView);
                lblCentral.setVisibility(View.VISIBLE);

                btnGrabar.setText("Cambiar Central");
                btnGrabar.setTextColor(Color.parseColor("#cc4600"));

                btnCambiarCentral.setVisibility(View.INVISIBLE);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        res = getResources();
    }

   

    public void DescargarVersion(View view){
        PermisosAlmacenamiento();
        try {

            String urlLogo = textoUrl.getText().toString() + "/marventaslogo.apk";
            final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlLogo));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle("Mar Ventas");
            request.setDescription("Punto De Ventas Mar");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,  "Ventas_Mar.apk");
            final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DescargaID = manager.enqueue(request);

            IntentFilter filtro = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long idLongitud = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
                    if(idLongitud == DescargaID){
                        if(ObteneStatusDescargar()==DownloadManager.STATUS_SUCCESSFUL){
                            Intent intente =new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                            intente.setData(Uri.parse("package:com.mateonet.mar.marandroidcliente"));
                            startActivity(intente);
                        }
                    }
                }
            },filtro);


        }catch (Exception e){
            Snackbar toast = Snackbar.make(view,e.getMessage().toString(),Snackbar.LENGTH_LONG);
            toast.show();
        }
    }

    public int ObteneStatusDescargar(){
        DownloadManager.Query requestQuery = new DownloadManager.Query();
        requestQuery.setFilterById(DescargaID);

        DownloadManager manangerQuery = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        Cursor cursor = manangerQuery.query(requestQuery);

        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            return status;
        }

        return  DownloadManager.ERROR_UNKNOWN;
    }
    public boolean VeficarExistenciaLogo(){
        boolean LogoExistente=false;

        File filepath = Environment.getExternalStorageDirectory();
        try {
            File dir = new File(filepath.getAbsolutePath(), "/Pictures/logo.png");
            if (dir.exists()) {
                LogoExistente = true;
            } else {
                LogoExistente = false;
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return  LogoExistente;
    };
    @Override
    public void onResume() {
        super.onResume();
        MarSession.isAboutOpen(true);

        Intent intent = getIntent();
        String message = intent.getStringExtra("msg");
        TextView txtErr=(TextView) findViewById(R.id.textError);
        txtErr.setText(message);
        if (message!=null && message.length()>0) {
            MarSession.showToast(message, Toast.LENGTH_LONG);
        }


    }
    private void init(){
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
    }
    public void ImprimirLogo(View view){
        try{
            BluetoothUtil bluethoo = new BluetoothUtil();
            ESCUtil escuUtil = new ESCUtil();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTargetDensity = 200;
            options.inDensity = 200;
            File filepath = Environment.getExternalStorageDirectory();
            File dir = new File(filepath.getAbsolutePath(),"/Pictures/logo.png");
            Bitmap bitmap = BitmapFactory.decodeFile(dir.getAbsolutePath());

            if(bitmap != null){
                bitmap = ImageScale(bitmap,200);
                if (!bluethoo.isBlueToothPrinter) {
                    SunmiPrintHelper.getInstance().printBitmap(bitmap, OrientationHelper.VERTICAL);
                    SunmiPrintHelper.getInstance().print3Line();

                    ImprimioLogo=true;
                } else {
                    bluethoo.sendData(escuUtil.printBitmap(bitmap, 3));
                }
            }else{
              /*En caso de que no funcione*/
            }
        }catch (Exception e){
            Snackbar bar = Snackbar.make(this.getCurrentFocus().getRootView(),e.getMessage(),Snackbar.LENGTH_LONG);
            bar.show();
        }
    }
    public void ObtenerLogo(final View view){
        String UrlLogo=textoUrl.getText().toString()+"/logo.png";
        PermisosAlmacenamiento();
        ImageRequest SolicitudImage = new ImageRequest(UrlLogo, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                if(response != null){
                    File filepath = Environment.getExternalStorageDirectory();
                    File dir = new File(filepath.getAbsolutePath(),"/Pictures/");
                    dir.mkdir();
                    dir.exists();
                    File ImageSave = new File(dir,"logo.png");
                    try {
                        outputStream = new FileOutputStream(ImageSave);
                        response.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                        enlaceLogo.setText("Actualizar Logo");
                        Toast toats = Toast.makeText(getApplicationContext(),"Logo Guardado",Toast.LENGTH_LONG);
                        toats.show();
                    }catch (Exception e){
                        Toast toats = Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG);
                        toats.show();
                    }
                   /** response = ImageScale(response,400);
                    if (!BluetoothUtil.isBlueToothPrinter) {
                        //SunmiPrintHelper.getInstance().printText("Edison El Mejor",12,true,false);
                        SunmiPrintHelper.getInstance().printBitmap(response, OrientationHelper.VERTICAL);
                        SunmiPrintHelper.getInstance().feedPaper();
                        //BluetoothUtil.sendData(ESCUtil.printBitmap(bitmap, 3));
                    } else {
                        BluetoothUtil.sendData(ESCUtil.printBitmap(response, 3));
                    }
                }else{
                    /*En caso de que no funcione*/
                }
            }
        }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(),"No se pudo descargar el logo",Toast.LENGTH_LONG);
                toast.show();
            }
        });

        request.add(SolicitudImage);
    }


    //Imprimir QRCode
    public void ImprimirQRCode(String codeQR){
        SunmiPrintHelper.getInstance().setAlign(1);
        SunmiPrintHelper.getInstance().print3Line();
        SunmiPrintHelper.getInstance().printQr(codeQR,5,1);
        SunmiPrintHelper.getInstance().feedPaper();
    }

    public void PermisosAlmacenamiento(){
        int permisoAlmacenamiento = ActivityCompat.checkSelfPermission(AboutActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permisoAlmacenamiento != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},111);
            }
        }
    }

    //Darle tamano a un bitmap
    public Bitmap ImageScale(Bitmap mBitmap, float newWidth){
        //Redimensionamos
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth,scaleWidth);
        // recreate the new Bitmap
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
    }
    private Bitmap scaleImage(Bitmap bitmap1) {
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();
        // 设置想要的大小
        int newWidth = (width/8+1)*8;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, 1);
        // 得到新的图片
        return Bitmap.createBitmap(bitmap1, 0, 0, width, height, matrix, true);
    }

    @Override
    public  void onDestroy() {
        MarSession.isAboutOpen(false);
        super.onDestroy();
    }

}
