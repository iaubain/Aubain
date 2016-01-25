package models;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.owner.petrolmanager.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import justtide.ThermalPrinter;

/**
 * Created by Owner on 12/17/2015.
 */
public class PrintingHandler {

    ThermalPrinter thermalPrinter;
    SessionData sesData;
    SessionManager sesMan;
    Context context;
    byte[] lineBuffer;


    public String dataToPrint(PrintData pd, Context context){
        thermalPrinter=ThermalPrinter.getInstance();

        sesMan=new SessionManager(context);
        sesData=new SessionData();
        sesData=sesMan.getUserDetails();
        this.context=context;

        int i,state;
        int ret = -1;
        boolean blnRet = false;

        ret = thermalPrinter.getState();
        Log.v("PRINT", "getState:"+ret);

        ret = thermalPrinter.getTemperature();
        Log.v("PRINT", "getTemperature:"+ret);

        blnRet = thermalPrinter.isOverTemperature();
        Log.v("PRINT", "isOverTemperature:"+blnRet);

        blnRet = thermalPrinter.isPaperOut();
        Log.v("PRINT", "isPaperOut:" + blnRet);

        if ((!thermalPrinter.isOverTemperature())&&(!thermalPrinter.isPaperOut())&&(thermalPrinter.getState() > -1)) {
            thermalPrinter.initBuffer();
            thermalPrinter.setGray(3);
            thermalPrinter.setHeightAndLeft(20, 10);
            thermalPrinter.setLineSpacing(3);
            //thermalPrinter.setDispMode(ThermalPrinter.CMODE);
            thermalPrinter.setDispMode(ThermalPrinter.UMODE);

            //Setting bill logo
            Resources res= context.getResources();
            Bitmap bitmap =  BitmapFactory.decodeResource(res, R.drawable.logo);
            thermalPrinter.printLogo(0, 10, bitmap);

            //Setting header of the bill
            thermalPrinter.setStep(10);
            thermalPrinter.setFont(ThermalPrinter.ASC16X24B, ThermalPrinter.HZK12);
            //thermalPrinter.shiftRight(50);
//            thermalPrinter.print("___________________\n\n");
            line();
            thermalPrinter.shiftRight(75);
            thermalPrinter.print(pd.getbName() + "\n");
            //thermalPrinter.shiftRight(50);
            line();
            thermalPrinter.setStep(10);
            //thermalPrinter.print("___________________\n\n");

            //thermalPrinter.setHeightAndLeft(20, 10);
            thermalPrinter.setFont(ThermalPrinter.ASC12X24Y, ThermalPrinter.ASC12X24);
            thermalPrinter.print("Product: " + pd.getpName() + "\n");
            thermalPrinter.print("Amount: " + pd.getAmount() + " RWF\n");
            thermalPrinter.print("Quantity: " + pd.getQuantity() + " Liters\n\n");
            //thermalPrinter.print("___________________________\n");

            //transaction
            line();
            thermalPrinter.shiftRight(60);
            thermalPrinter.print("TRANSACTION DETAILS\n");
            line();
            thermalPrinter.setStep(4);
            thermalPrinter.print("Transaction No: " + pd.gettId() + "\n");
            thermalPrinter.print("Payment Method: " + pd.getpMode() + "\n");
            thermalPrinter.print("Payment Status: " + pd.getpStatus() + "\n");

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            String dateString=pd.getTime();
            try {

                Date date = formatter.parse(dateString);
                thermalPrinter.print("Time:" + formatter.format(date) + "\n");

            } catch (ParseException e) {
                e.printStackTrace();
                thermalPrinter.print("Time:" + pd.getTime() + "\n");
            }

            //transaction
            thermalPrinter.print("Device: " + pd.getImei() + "\n");

            //customer
            thermalPrinter.print("Customer:" + pd.getTel() + "\n");
            thermalPrinter.print("Served by:" + sesData.getU_name() + "\n\n");
            //thermalPrinter.print("___________________________\n\n");
            line();

            //Company Details
            thermalPrinter.shiftRight(60);
            thermalPrinter.print("COMPANY DETAILS \n");
            //thermalPrinter.shiftRight(50);
            //thermalPrinter.print("___________________\n");
            line();
            thermalPrinter.setStep(10);
            thermalPrinter.setFont(ThermalPrinter.HZK24F, ThermalPrinter.ASC12X24);

            thermalPrinter.print("ENGEN Ltd\n");
            thermalPrinter.print("Kacyiru, Gasabo, Kigali/Rwanda\n");
            thermalPrinter.print("TeL: (250) 788 175 111/(250) 788 304 747\n");
            thermalPrinter.print("BP. 1342, Kigali, Rwanda\n");
            thermalPrinter.print("Registration Number(TIN): 101835101\n\n");

            thermalPrinter.setFont(ThermalPrinter.ASC12X24YB, ThermalPrinter.HZK24F);

            thermalPrinter.shiftRight(120);
            thermalPrinter.print("Thank You!\n\n");

            thermalPrinter.setFont(ThermalPrinter.ASC8X16B, ThermalPrinter.HZK24F);
            thermalPrinter.shiftRight(90);
            thermalPrinter.print("_________________________\n");
            thermalPrinter.shiftRight(100);

            thermalPrinter.print("Powered By Oltranz.com \n");

            thermalPrinter.shiftRight(90);
            thermalPrinter.print("_________________________\n");

            thermalPrinter.setStep(200);

            thermalPrinter.printStart();
            state = thermalPrinter.waitForPrintFinish();

            Log.v("PRINT", "return" + state);
            Intent intent = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg","resetui");
            context.sendBroadcast(intent);
            return "Success: "+state;
        }else{
            //when something is wrong with printer
            Intent intent = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg","resetui");
            context.sendBroadcast(intent);
            return "Printer Failure: "+thermalPrinter.getState();
        }
    }

//    public double makeDouble(String number){
//        try{
//
//        }catch (NumberFormatException e){
//            e.printStackTrace();
//        }
//        return 0;
//    }
    public void line(){
        lineBuffer = new byte[96];
        for(int index=0;index<54;index++) {
            lineBuffer[index]= (byte) 0xfc;
        }
        thermalPrinter.printLine(lineBuffer);
        thermalPrinter.setStep(5);
    }
}
