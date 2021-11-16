package com.mateonet.mar.marandroidcliente;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mateonet.mar.marandroidcliente.Code.MarSession;
import com.mateonet.mar.marandroidcliente.Code.PrintContentLine;
import com.mateonet.mar.marandroidcliente.Code.PrintOption;
import com.telpo.tps550.api.printer.ThermalPrinter;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;

import java.util.ArrayList;

public class TelpoPrinterEngine {
	private static String printVersion;
	private final int NOPAPER = 3;
	private final int LOWBATTERY = 4;
	private final int PRINTVERSION = 5;
	private final int PRINTBARCODE = 6;
	private final int PRINTQRCODE = 7;
	private final int PRINTPAPERWALK = 8;
	private final int PRINTCONTENT = 9;
	private final int CANCELPROMPT = 10;
	private final int PRINTERR = 11;
	private final int OVERHEAT = 12;
	private final int MAKER = 13;
	private final int PRINTPICTURE = 14;
	private final int EXECUTECOMMAND = 15;
	private final int NOPRINTER = 16;

	private LinearLayout print_text, print_pic, print_comm;
	private TextView text_index, pic_index, comm_index, textPrintVersion;
	MyHandler handler;
	private EditText editTextLeftDistance, editTextLineDistance, editTextWordFont, editTextPrintGray,
			editTextBarcode, editTextQrcode, editTextPaperWalk, editTextContent,
			edittext_maker_search_distance, edittext_maker_walk_distance, edittext_input_command;
	private Button buttonBarcodePrint, buttonPaperWalkPrint, buttonContentPrint, buttonQrcodePrint,
			buttonGetExampleText, buttonGetZhExampleText, buttonClearText, button_maker,
			button_papercut, button_print_picture, button_execute_command;
	private String Result;
	private Boolean nopaper = false;
	private boolean LowBattery = false;

	public static String barcodeStr;
	public static String qrcodeStr;
	public static int paperWalk;
	public static String printContent;
	private int leftDistance = 0;
	private int lineDistance;
	private int wordFont;
	private int printGray;
	private ProgressDialog progressDialog;
	private final static int MAX_LEFT_DISTANCE = 255;
	ProgressDialog dialog;
	private String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/111.bmp";
	private Activity mActivity;
	private  ArrayList<PrintContentLine> mContentLines;
	private  ArrayList<PrintOption> mPrintOptions;

	public TelpoPrinterEngine(Activity pActivity) {
		mActivity = pActivity;
        initialize();
	}
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case NOPAPER:
					noPaperDlg();
					break;
				case LOWBATTERY:
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
					alertDialog.setTitle("MAR - Telpo Printer");
					alertDialog.setMessage("Las baterias del equipo estan descargadas.");
					alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
						}
					});
					alertDialog.show();
					break;
				case PRINTVERSION:
					dialog.dismiss();
					if (msg.obj.equals("1")) {
						textPrintVersion.setText(printVersion);
					} else {
						MarSession.showToast("Print version failed", Toast.LENGTH_LONG);
					}
					break;
				/*case PRINTBARCODE:
					new barcodePrintThread().start();
					break;
				case PRINTQRCODE:
					new qrcodePrintThread().start();
					break;
				case PRINTPAPERWALK:
					new paperWalkPrintThread().start();
					break;*/
				case PRINTCONTENT:
					new contentPrintThread().start();
					break;
				/*case MAKER:
					new MakerThread().start();
					break;
				case PRINTPICTURE:
					new printPicture().start();
					break;
				case EXECUTECOMMAND:
					new executeCommand().start();
					break;*/
                case CANCELPROMPT:
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					break;
				case OVERHEAT:
					AlertDialog.Builder overHeatDialog = new AlertDialog.Builder(mActivity);
                    overHeatDialog.setTitle("MAR - Telpo Printer");
					overHeatDialog.setMessage("Impresora sobrecalentada");
					overHeatDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
						}
					});
					overHeatDialog.show();
					break;
				case NOPRINTER:
					MarSession.showToast("No se encontro la impresora!", Toast.LENGTH_LONG);
					break;
				default:
					MarSession.showToast("Error indefinido de impresion!", Toast.LENGTH_LONG);
					break;
			}
		}
	}

 	private void initialize() {
        handler = new MyHandler();
        IntentFilter pIntentFilter = new IntentFilter();
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT");
        mActivity.registerReceiver(printReceive, pIntentFilter);
    }

    public void Imprime(ArrayList<PrintContentLine> pContentLines, ArrayList<PrintOption> pPrintOptions) {
        mContentLines=pContentLines;
        mPrintOptions=pPrintOptions;
        if (LowBattery == true) {
            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
        } else {
            if (!nopaper) {
                progressDialog = ProgressDialog.show(mActivity, "MAR - Imprimiendo", "Imprimiendo, por favor espere");
                handler.sendMessage(handler.obtainMessage(PRINTCONTENT, 1, 0, null));
            } else {
                noPaperDlg();
            }
        }
    }

	private final BroadcastReceiver printReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING);
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
				//TPS390 can not print,while in low battery,whether is charging or not charging
				if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal()) {
					if (level * 5 <= scale) {
						LowBattery = true;
					} else {
						LowBattery = false;
					}
				} else {
					if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
						if (level * 5 <= scale) {
							LowBattery = true;
						} else {
							LowBattery = false;
						}
					} else {
						LowBattery = false;
					}
				}
			}
			//Only use for TPS550MTK devices
			else if (action.equals("android.intent.action.BATTERY_CAPACITY_EVENT")) {
				int status = intent.getIntExtra("action", 0);
				int level = intent.getIntExtra("level", 0);
				if (status == 0) {
					if (level < 1) {
						LowBattery = true;
					} else {
						LowBattery = false;
					}
				} else {
					LowBattery = false;
				}
			}
		}
	};

	private void noPaperDlg() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(mActivity);
		dlg.setTitle("No hay papel");
		dlg.setMessage("Cambie el papel de la impresora.");
		dlg.setCancelable(false);
		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
                nopaper=false;
				ThermalPrinter.stop(mActivity);
			}
		});
		dlg.show();
	}

	private class contentPrintThread extends Thread {
		@Override
		public void run() {
			super.run();
            Boolean libStarted=false;
			try {
                ThermalPrinter.start(mActivity);
                libStarted=true;
                ThermalPrinter.reset();
                ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_LEFT);
                ThermalPrinter.setLeftIndent(0);
                ThermalPrinter.setLineSpace(0);
                ThermalPrinter.setFontSize(2);
                ThermalPrinter.setGray(12);

				String mLogoBase64 = "";
				Integer mLogoPos = 0;
				Bitmap mLogo = null;

                Integer mCodeWidth = 256;
				Integer mCodeHeight = 256;
				String mCodeText = "";
				Integer mCodePos = mContentLines.size();
				Bitmap mCode = null;
				BarcodeFormat mCodeType = BarcodeFormat.QR_CODE;

				if (mPrintOptions!=null) {
					for (int j = 0; j < mPrintOptions.size(); j++) {
						PrintOption theOption = mPrintOptions.get(j);
						if (theOption.Key().toLowerCase() == "logobase64") {
							mLogoBase64 = theOption.Value();
						}
						if (theOption.Key().toLowerCase() == "logopos") {
							mLogoPos = Integer.parseInt(theOption.Value());
						}

						if (theOption.Key().toLowerCase() == "codetype") {
							mCodeType = theOption.Value().toUpperCase() == "BAR" ? BarcodeFormat.CODE_128 : BarcodeFormat.QR_CODE;
						}
						if (theOption.Key().toLowerCase() == "codetext") {
							mCodeText = theOption.Value();
						}
						if (theOption.Key().toLowerCase() == "codewidth") {
							mCodeWidth = Integer.parseInt(theOption.Value());
						}
						if (theOption.Key().toLowerCase() == "codeheight") {
							mCodeHeight = Integer.parseInt(theOption.Value());
						}
						if (theOption.Key().toLowerCase() == "codepos") {
							mCodePos = Integer.parseInt(theOption.Value());
						}
					}
				}

				if (mCodeText.length()>0) {
					mCode = CreateCode(mCodeText, mCodeType, mCodeWidth, mCodeHeight);
				}
				if (mLogoBase64.length()>0) {
					byte[] logoBytes = Base64.decode(mLogoBase64, Base64.DEFAULT);
					mLogo = BitmapFactory.decodeByteArray(logoBytes, 0, logoBytes.length);
				}


				String theLastSize="";
                String thePrintText="";

                for (int i=0;i<mContentLines.size();i++) {
                	if (mCodePos==i && mCode != null) {
                		ThermalPrinter.printLogo(mCode);
					}
					if (mLogoPos==i && mLogo != null) {
						ThermalPrinter.printLogo(mLogo);
					}

                    PrintContentLine theLine=mContentLines.get(i);
                    if (!theLastSize.equals(theLine.Size())) {
                        if (thePrintText.length()>0) {
                            ThermalPrinter.addString(thePrintText);
                            ThermalPrinter.printString();
                            thePrintText="";
                        }
                        theLastSize=theLine.Size();
                        if (theLastSize.equals("1")) {
                            ThermalPrinter.enlargeFontSize(1, 1);
                        } else {
                            ThermalPrinter.enlargeFontSize(1, 2);
                        }
                    }
                    thePrintText=thePrintText+"\n"+theLine.Content();
                }
                if (thePrintText.length()>0) {
                    ThermalPrinter.addString(thePrintText);
                    ThermalPrinter.printString();
                }
				if (mCodePos>=mContentLines.size() && mCode != null) {
					ThermalPrinter.printLogo(mCode);
				}
				if (mLogoPos>=mContentLines.size() && mLogo != null) {
					ThermalPrinter.printLogo(mLogo);
				}
				ThermalPrinter.walkPaper(10);
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else if (libStarted) {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(NOPRINTER, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper) {
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(mActivity);
			}
		}
	}

	public Bitmap CreateCode(String str, com.google.zxing.BarcodeFormat type, int bmpWidth, int bmpHeight) throws WriterException {
		// 生成二维矩阵,编码时要指定大小,不要生成了图片以后再进行缩放,以防模糊导致识别失败
		BitMatrix matrix = new MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		// 二维矩阵转为一维像素数组（一直横着排）
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				} else {
					pixels[y * width + x] = 0xffffffff;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		// 通过像素数组生成bitmap,具体参考api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}
