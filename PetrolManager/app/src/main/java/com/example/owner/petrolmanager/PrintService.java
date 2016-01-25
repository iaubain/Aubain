package com.example.owner.petrolmanager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.DBHelper;
import models.PrintData;
import models.PrintingHandler;
import models.TQueue;
import models.TransactionManager;

public class PrintService extends Service {
    DBHelper db;
    List qList;
    Context context=this;
    int sum=0;
    String traId="";
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        db=new DBHelper(this);
        qList=new ArrayList<TQueue>();

       // Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"Service Destroy", Toast.LENGTH_LONG).show();
    }

    public void stopService(){
        this.stopSelf();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Toast.makeText(this,"Service LowMemory",Toast.LENGTH_LONG).show();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //select queue from database
        qList=db.getAllQueue();

        //Check the Queue content
        if(!qList.isEmpty()){
            //Operation
            Iterator i=qList.iterator();
            while (i.hasNext()){
                TQueue queue=new TQueue();
                queue=(TQueue) i.next();
                String tId=queue.gettId();
                traId=tId;
                sum=Integer.parseInt(queue.getSum());
                //check it on line
                CheckTrans trans=new CheckTrans();
                trans.execute(tId);
            }

        }else{
            System.out.print("empty DB");

            //cancel periodic call when no data in db
            Intent i = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg","cancelPeriodic");
            sendBroadcast(i);

            //Kill The Service
            stopService();

            //Cancel alarm
            PersonalSale pers=new PersonalSale();
            Intent alarmIntent =new Intent(this, PrintService.class);
            PendingIntent pi= PendingIntent.getService(this, 0, alarmIntent, 0);
            AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
            try {
                pers.killAlarm(pi,am);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        //Toast.makeText(this,"Task Accomplished:",Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    //___________________Check Transaction__________________________\\
    private class CheckTrans extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String transId= params[0];

            //creating a json file to be posted
            JSONObject obj = new JSONObject();

            try {
                obj.put("tId",transId);
                String json=obj.toString();
                String url="http://41.74.172.132:8080/PetroStationManager/androiddata/getTraDataById";

                //_____________Opening connection and post data____________//
                URL oURL = new URL(url);
                HttpURLConnection con = (HttpURLConnection) oURL.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "Application/json; charset=UTF-8");


                con.setDoOutput(true);
                con.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());

                wr.writeBytes(json);
                wr.flush();
                wr.close();
                System.out.println("Data to post :"+json);
                BufferedReader in1= new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in1.readLine()) != null) {
                    response.append(inputLine);
                }
                in1.close();
                con.disconnect();
                return response.toString();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            try {
                System.out.println("Data from server: " + result);
                //__________Continue with data deleting from DB by transaction Id as long as print is finished____________\\
                JSONObject jo = new JSONObject(result);

                //Checking if Paymode is Cash And successful
                if((jo.get("validation").toString().equalsIgnoreCase("valid"))&&(!jo.get("pStatus").toString().equalsIgnoreCase("failure"))) {

                    //when a transaction was by Cash
                    if (jo.get("pStatus").toString().equalsIgnoreCase("success")) {
                        PrintData print = new PrintData();
                        print.setbName(jo.get("bName").toString());
                        print.setpName(jo.get("pName").toString());
                        print.setAmount(jo.get("amount").toString());
                        print.setQuantity(jo.get("quantity").toString());
                        print.setpMode(jo.get("pMode").toString());
                        print.setpStatus(jo.get("pStatus").toString());
                        print.setTel(jo.get("custTel").toString());
                        print.setImei(jo.get("imei").toString());
                        print.settId(jo.get("traId").toString());
                        print.setTime(jo.get("pTime").toString());

                        //handling printing
                        PrintingHandler ph = new PrintingHandler();
                        String printing = ph.dataToPrint(print, context);

                        //clear transaction id into transaction manager
                        db.deletequeue(Long.parseLong(jo.get("traId").toString()));
                        Intent intent = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg",printing+" "+"Local Cache Cleared");
                        sendBroadcast(intent);

                    }else{

                        if(jo.get("pStatus").toString().equalsIgnoreCase("failure")) {
                            db.deletequeue(Long.parseLong(jo.get("traId").toString()));
                        }

                        int sumIncrement=sum+1;
                        if(sumIncrement<=500){//remove 500 and turn it 200
                            TQueue queue=new TQueue();
                            queue.settId(jo.get("traId").toString());
                            queue.setpMode(jo.get("pMode").toString());
                            queue.setRequest("proceed");
                            queue.setSum(String.valueOf(sumIncrement));

                            db.updateQueue(queue);
                            Intent intent = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg",jo.get("pStatus").toString()+" "+jo.get("traId").toString());
                            sendBroadcast(intent);
                        }else{

                            //Delete on time out
                            db.deletequeue(Long.parseLong(jo.get("traId").toString()));
                            Intent intent = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg","Time Out "+jo.get("traId").toString());
                            sendBroadcast(intent);
                        }
                    }

                }else if(jo.get("validation").toString().equalsIgnoreCase("invalid")||(jo.get("pStatus").toString().equalsIgnoreCase("failure"))) {
                    db.deletequeue(Long.parseLong(traId));
                    db.deletequeue(Long.parseLong(jo.get("traId").toString()));
                    //db.truncateQueue();
                    //stopSelf();
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }catch (JSONException e) {
                    e.printStackTrace();
                }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}
