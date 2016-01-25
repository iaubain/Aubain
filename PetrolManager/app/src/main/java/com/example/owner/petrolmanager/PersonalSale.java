package com.example.owner.petrolmanager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import models.AfterComitSale;
import models.ComitSale;
import models.DBHelper;
import models.PrintData;
import models.PrintingHandler;
import models.Product;
import models.Reconciliation;
import models.SessionData;
import models.SessionManager;
import models.TQueue;
import models.TransactionManager;

public class PersonalSale extends ActionBarActivity {

    SessionManager sesManager;
    SessionData sesData;
    TransactionManager transMan;

    IntentFilter filter;

    BroadcastReceiver br;

    ClipData myClip;
    ClipboardManager myClipboard;


    TextView monitor,lbltel, rcount;
    ListView rlist;
    EditText tel,amnt,qty;
    List products;
    LinearLayout prodLayout;
    RadioGroup productsGroup,pmg;
    RadioButton productRadio;
    Button confirmTel,cancelTel,sale, recDone;

    Context context=this;
    Dialog dialog;

    InputMethodManager imm;

    String TAG = "PetrolLog";

    AfterComitSale afterComitSale;

    TextWatcher watchAmount, watchQuantity;

    double unityPrice=0;
    String prodId,payMode="cash",telephone,serviceMessage="";

    Calendar cal;
    PendingIntent pintent;
    AlarmManager alarm;
    Intent alarmIntent;

    DBHelper db;

    //Confirm transaction widgets
    TextView confProd,confAmnt,confqty,conftel,confpay,conftime;
    Button confComit, confCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // go full screen
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        // go non-full screen
//        WindowManager.LayoutParams attrs = activity_personal_sale.getWindow().getAttributes();
//        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        activity_personal_sale.getWindow().setAttributes(attrs);

        setContentView(R.layout.activity_personal_sale);

//        //registering Local broadcast receiver
//        filter = new IntentFilter("com.example.owner.petrolmanager.PRINT_TASK");
//        receiver = new LocalReceiver();
//        registerReceiver(receiver, filter);

        //___________________Verifying login data___________________//
        sesManager=new SessionManager(getApplicationContext());
        //check either he was logged in or not
        if(!sesManager.isLoggedIn()) {
            // Shared Preferences
            SharedPreferences pref;

            // Editor for Shared preferences
            SharedPreferences.Editor editor;

            // Shared pref mode
            int PRIVATE_MODE = 0;

            // Sharedpref file name
            String PREF_NAME = "loginData";
            pref = getApplicationContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = pref.edit();
            editor.clear();
            editor.commit();

            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            this.finish();
            startActivity(intent);
            // sesManager.checkLogin();
        }
        //getting user Details
        sesData = sesManager.getUserDetails();

        //______________Rest of Activity______________\\

        //transaction manager
        transMan=new TransactionManager(getApplicationContext());

        db=new DBHelper(getApplicationContext());

        monitor=(TextView) findViewById(R.id.monitor);
        products=new ArrayList<Product>();
        productsGroup=(RadioGroup) findViewById(R.id.productsradio);
        pmg=(RadioGroup) findViewById(R.id.pmodegroup);
        prodLayout=(LinearLayout) findViewById(R.id.prodlayout);
        amnt=(EditText) findViewById(R.id.amnt);
        qty=(EditText) findViewById(R.id.qty);
        sale=(Button) findViewById(R.id.salebtn);

        //disable soft keyboard
        imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(amnt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

        //On touch handler for future use
        amnt.setCustomSelectionActionModeCallback(amntCP);
        qty.setCustomSelectionActionModeCallback(qtyCP);

        //______________Request products to sale_________________\\
        ProductRequest runner = new ProductRequest();
        runner.execute(sesData.getB_id());

        //_________________Broadcast Receiver_________________\\
        filter=new IntentFilter("com.example.owner.petrolmanager.PRINTING");

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");

                //canceling alarm
                if(msg.equalsIgnoreCase("cancelPeriodic"))
                    cancelAlarm();
                else if(msg.equalsIgnoreCase("resetui")){
                    amnt.setText("");
                    qty.setText("");
                    RadioButton cash=(RadioButton) findViewById(R.id.cash);
                    cash.setChecked(true);
                    payMode="cash";
                    telephone="726";
                }else
                monitor.setText(msg);
            }
        };
        registerReceiver(br, filter);

        //Setting Quantity when Amount is Changed
        watchAmount=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                qty.removeTextChangedListener(watchQuantity);
                qty.setText("");
                double amount=0;

                //check length of text box
                if(amnt.getText().toString().length()<=5)
                    amnt.setTextSize(30);
                else if(amnt.getText().toString().length()==5)
                    amnt.setTextSize(20);
                else if(amnt.getText().toString().length()==6)
                    amnt.setTextSize(18);
                else if(amnt.getText().toString().length()>=7)
                    amnt.setTextSize(15);

                try{


                    amount=Double.parseDouble(amnt.getText().toString());

                if ((unityPrice != 0) && (amount>0)) {
                    double quantity = Double.parseDouble(amnt.getText().toString()) / unityPrice;
                    NumberFormat numberFormat=NumberFormat.getInstance();
                    numberFormat.setMaximumFractionDigits(2);

                    //purifying double value
                    String doubleString=String.valueOf(numberFormat.format(quantity));
                    qty.setText(String.valueOf(doubleString.replaceAll(",", "")));

                } else if(unityPrice <= 0)
                    monitor.setText("Revise Products");
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                qty.addTextChangedListener(watchQuantity);
            }
        };
        amnt.addTextChangedListener(watchAmount);

        //Setting Amount when Quantity is Changed
        watchQuantity=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                amnt.removeTextChangedListener(watchAmount);
                amnt.setText("");
                double quantity=0;

                //check length of text box
                if(qty.getText().toString().length()<=5)
                    qty.setTextSize(30);
                else if(qty.getText().toString().length()==5)
                    qty.setTextSize(20);
                else if(qty.getText().toString().length()==6)
                    qty.setTextSize(18);
                else if(qty.getText().toString().length()>=7)
                    qty.setTextSize(15);

                try{

                    quantity=Double.parseDouble(qty.getText().toString());

                    if ((unityPrice != 0) && (quantity>0)) {//remove 0 and set >=1
                        double amount = Double.parseDouble(qty.getText().toString()) * unityPrice;
                        NumberFormat numberFormat=NumberFormat.getInstance();
                        numberFormat.setMaximumFractionDigits(2);


                        //purifying double value
                        String doubleString=String.valueOf(numberFormat.format(amount));
                        amnt.setText(String.valueOf(doubleString.replaceAll(",", "")));
                    } else if(unityPrice <= 0)
                        monitor.setText("Revise Products");
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                amnt.addTextChangedListener(watchAmount);
            }
        };
        qty.addTextChangedListener(watchQuantity);

        //paymode selection
        pmg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton pay=(RadioButton) findViewById(checkedId);
                switch (checkedId) {
                    case R.id.cash:
                        // TODO Something
                        payMode="cash";
                        Toast.makeText(getApplicationContext(), "Data: "+payMode, Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.mtn:
                        // TODO Something when MTN is checked
                        payMode="mtn";
                        dialog = new Dialog(context);
                        dialog.setContentView(R.layout.telpopup);
                        dialog.setTitle("Fill the Number...");
                        lbltel=(TextView) dialog.findViewById(R.id.lbltel);
                        confirmTel=(Button) dialog.findViewById(R.id.telconfirm);
                        cancelTel=(Button) dialog.findViewById(R.id.telcancel);
                        tel=(EditText) dialog.findViewById(R.id.tel);
                        tel.setText("078");
                        tel.setFocusable(true);

                        //setting confirm tel on click
                        confirmTel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tel.getText().toString().length()<10){
                                    lbltel.setText("Incompelete number");
                                }else if(tel.getText().toString().length()>13){
                                    lbltel.setText("Long number");
                                } else{
                                telephone="25"+tel.getText().toString();
                                dialog.dismiss();}
                            }
                        });

                        //setting cancel telephone on click
                        cancelTel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RadioButton cash=(RadioButton) findViewById(R.id.cash);
                                cash.setChecked(true);
                                payMode="cash";
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                        break;

                    case R.id.tigo:
                        // TODO Something when tigo is checked
                        payMode="tigo";
                        dialog = new Dialog(context);
                        dialog.setContentView(R.layout.telpopup);
                        dialog.setTitle("Fill the Number...");
                        lbltel=(TextView) dialog.findViewById(R.id.lbltel);
                        confirmTel=(Button) dialog.findViewById(R.id.telconfirm);
                        cancelTel=(Button) dialog.findViewById(R.id.telcancel);
                        tel=(EditText) dialog.findViewById(R.id.tel);
                        tel.setText("072");
                        tel.setFocusable(true);

                        //setting confirm tel on click
                        confirmTel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tel.getText().toString().length()<10){
                                    lbltel.setText("Incompelete number");
                                }else{
                                    telephone="25"+tel.getText().toString();
                                    dialog.dismiss();}
                            }
                        });

                        //setting cancel telephone on click
                        cancelTel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RadioButton cash=(RadioButton) findViewById(R.id.cash);
                                cash.setChecked(true);
                                payMode="cash";
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                        break;

                    case R.id.airtel:
                        // TODO Something when airtel is checked
                        payMode="airtel";
                        dialog = new Dialog(context);
                        dialog.setContentView(R.layout.telpopup);
                        dialog.setTitle("Fill the Number...");
                        lbltel=(TextView) dialog.findViewById(R.id.lbltel);
                        confirmTel=(Button) dialog.findViewById(R.id.telconfirm);
                        cancelTel=(Button) dialog.findViewById(R.id.telcancel);
                        tel=(EditText) dialog.findViewById(R.id.tel);
                        tel.setText("073");
                        tel.setFocusable(true);

                        //setting confirm tel on click
                        confirmTel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tel.getText().toString().length()<10){
                                    lbltel.setText("Incompelete number");
                                }else{
                                    telephone="25"+tel.getText().toString();
                                    dialog.dismiss();}
                            }
                        });

                        //setting cancel telephone on click
                        cancelTel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RadioButton cash=(RadioButton) findViewById(R.id.cash);
                                cash.setChecked(true);
                                payMode="cash";
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                        break;
                }
            }
        });

        //_______________Comit Sale___________________\\
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComitSale comitSale= new ComitSale();
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                JSONObject obj = new JSONObject();

                try {
                    if (payMode.equalsIgnoreCase("cash"))
                        telephone = "726";
                }catch (NullPointerException e){
                    e.printStackTrace();
                    monitor.setText("Some Fields Are Empty");
                }

                try{
                if(!(Double.parseDouble(amnt.getText().toString())>1)&&(!(Double.parseDouble(qty.getText().toString())>=0)))//remove zero and set 1
                    monitor.setText("Some value are invalid");

                //Preparing selling data to be uploaded
                else {
                    comitSale.setpId(prodId);
                    comitSale.setAmount(amnt.getText().toString());
                    comitSale.setQuantity(qty.getText().toString());
                    comitSale.setpMode(payMode);
                    comitSale.setTel(telephone);
                    comitSale.setbId(sesData.getB_id());
                    comitSale.setuId(sesData.getU_id());
                    comitSale.setImei(telephonyManager.getDeviceId());

                    //Preparing a json file to be posted
                    obj.put("pId", comitSale.getpId());
                    obj.put("amount", comitSale.getAmount());
                    obj.put("quantity", comitSale.getQuantity());
                    obj.put("pMode", comitSale.getpMode());
                    obj.put("custTel", comitSale.getTel());
                    obj.put("uId", comitSale.getuId());
                    obj.put("bId", comitSale.getbId());
                    obj.put("imei", comitSale.getImei());

                    String json = obj.toString();

                    //Comit selling and online sync
                    Selling selling = new Selling();
                    selling.execute(json);
                }
                }catch (NullPointerException e){
                    //possible null pointers
                    e.printStackTrace();
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Warning...");
                    alertDialog.setMessage("Some Fields are seen as empty or null. Try Again");

                    // Setting Icon to Dialog
                    //alertDialog.setIcon(R.drawable.myDenialIcon);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Empty Fields", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Warning...");
                    alertDialog.setMessage("Some Fields are remarked with invalid data.");

                    // Setting Icon to Dialog
                    //alertDialog.setIcon(R.drawable.myDenialIcon);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Invalid data", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Warning...");
                    alertDialog.setMessage("Some Fields are empty or has invalid Data.");

                    // Setting Icon to Dialog
                    //alertDialog.setIcon(R.drawable.myDenialIcon);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Empty Or Invalid data", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // Show reconciliation pop up
            recHandler();
            Log.e(TAG, "action:" + "Menu Key Pressed" + ",reason:" + "Reconciliation");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(TAG, "action:" +"Back Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(br, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }


    //______________________Online request______________________//
    private class ProductRequest extends AsyncTask<String, String, String> {

        private SessionData sesData;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Loading Products..."); // Calls onProgressUpdate()
            String bId = params[0];

            //creating a json file to be posted
            JSONObject obj = new JSONObject();

            try {
                obj.put("bId",bId);
                String json=obj.toString();
                String url="http://41.74.172.132:8080/PetroStationManager/androiddata/listProduct";

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
            // execution of result of Long time consuming operation
            try {
                //JSONObject jsonObject = new JSONObject(jsonProduct);
                JSONArray ja= new JSONArray(result);
                //Iterate the jsonArray and print the info of JSONObjects
                for(int i=0; i < ja.length(); i++) {
                    Product prod=new Product();
                    JSONObject jo = ja.getJSONObject(i);
                    prod.setPid(jo.getString("pId").toString());
                    prod.setPname(jo.getString("pName").toString());
                    prod.setPpunity(jo.getString("uPrice").toString());
                    prod.setUnity(jo.getString("unity").toString());

                    products.add(prod);

                    productRadio = new RadioButton(getApplicationContext());
                    productRadio.setId(Integer.parseInt(jo.getString("pId")));
                    productRadio.setText(jo.getString("pName").toString());

                    //Set first element to be checked
                    if(i==0){
                        productRadio.setChecked(true);
                        productRadio.setPadding(0, 0, 5, 0);
                        unityPrice=Double.parseDouble(jo.getString("uPrice").toString());
                        prodId=jo.getString("pId").toString();
                    }else
                    productRadio.setPadding(5,0,0,0);


                    //Decorate the radio button
                    productRadio.setTextColor(getResources().getColor(R.color.rdcolor));
                    productRadio.setWidth(160);
                    productRadio.setSingleLine(true);
                    productRadio.setButtonDrawable(new StateListDrawable());
                    productRadio.setTextSize(10);
                    productRadio.setHeight(70);
                    productRadio.setGravity(Gravity.CENTER);
                    productRadio.setBackgroundResource(R.drawable.myradio);
                    productRadio.setOnClickListener(mThisButtonListener);
                    productRadio.setLongClickable(false);


                    productsGroup.addView(productRadio);
                }
                //Clearing monitor text
                monitor.setText("");
                monitor.clearComposingText();

                //adding buttons on main UI

            } catch (JSONException e) {
                e.printStackTrace();
                monitor.setText("No Product");
            }catch (NullPointerException e){
                e.printStackTrace();
                monitor.setText("Check Internet");
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            monitor.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }

    public void onClick(View view) {
        try {
            String s = ((RadioButton) view).getText().toString();
            Toast.makeText(this, "This is: " + s,
                    Toast.LENGTH_LONG).show();
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private View.OnClickListener mThisButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            String s =((RadioButton) v).getText().toString();
            prodId=String.valueOf(((RadioButton) v).getId());
            Iterator i=products.iterator();
            while(i.hasNext()){
                Product product=new Product();
                product=(Product) i.next();
                if(product.getPid().equals(prodId))
                    unityPrice=Integer.parseInt(product.getPpunity());
            }
            //clearing amnt box and Quantity box
            amnt.clearComposingText();
            amnt.setText("");

            qty.clearComposingText();
            qty.setText("");

            monitor.clearComposingText();
            monitor.setText("");

            Toast.makeText(getApplicationContext(), "You selected " + s,
                    Toast.LENGTH_SHORT).show();
        }
    };


    //____________________Posting selling data_________________\\

    private class Selling extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Comit Selling..."); // Calls onProgressUpdate()
            String json = params[0];

            try {
                String url="http://41.74.172.132:8080/PetroStationManager/androiddata/getTraData";

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
            }catch (MalformedURLException e) {
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

            try{
                System.out.println("Data from server: "+ result);
                JSONObject jsonObject = new JSONObject(result);
                TransactionManager transMan=new TransactionManager(getApplicationContext());
                if(jsonObject.get("validation").toString().equalsIgnoreCase("valid")) {
                    transMan.createTransactionSession(jsonObject.get("traId").toString());
                    afterComitSale=new AfterComitSale();

                    afterComitSale.settId(jsonObject.get("traId").toString());
                    afterComitSale.setpStatus(jsonObject.get("pStatus").toString());
                    afterComitSale.setpName(jsonObject.get("pName").toString());
                    afterComitSale.setAmount(jsonObject.get("amount").toString());
                    afterComitSale.setQuantity(jsonObject.get("quantity").toString());
                    afterComitSale.setpMode(jsonObject.get("pMode").toString());
                    afterComitSale.setTel(jsonObject.get("custTel").toString());
                    afterComitSale.setImei(jsonObject.get("imei").toString());

                    //Setting a PopUp to confirm transaction
                    dialog = new Dialog(context);
                    dialog.setContentView(R.layout.popuplayout);
                    dialog.setTitle("CONFIRM PAYMENT");
                    confProd=(TextView) dialog.findViewById(R.id.productname);
                    confAmnt=(TextView) dialog.findViewById(R.id.amount);
                    confqty=(TextView) dialog.findViewById(R.id.quantity);
                    conftel=(TextView) dialog.findViewById(R.id.tel);
                    confpay=(TextView) dialog.findViewById(R.id.pmode);

                    confComit=(Button) dialog.findViewById(R.id.comit);
                    confCancel=(Button) dialog.findViewById(R.id.cancel);

                    confProd.setText(afterComitSale.getpName());
                    confAmnt.setText(afterComitSale.getAmount());
                    confqty.setText(afterComitSale.getQuantity());
                    confpay.setText(afterComitSale.getpMode());
                    conftel.setText(afterComitSale.getTel());
                    //setting confirm transaction on click
                    confComit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ((!confProd.getText().toString().isEmpty())&&(!confAmnt.getText().toString().isEmpty())) {
                                //request payment
                                if((Double.parseDouble(confAmnt.getText().toString())>1)&&(Double.parseDouble(confqty.getText().toString())>0)){

                                    JSONObject jo=new JSONObject();
                                    try {

                                        if(!afterComitSale.getpMode().equalsIgnoreCase("cash")){
                                            //Triger the periodic request and put on queue this transaction
                                            TQueue queue=new TQueue();
                                            queue.settId(afterComitSale.gettId());
                                            queue.setpMode(afterComitSale.getpMode());
                                            queue.setRequest("proceed");
                                            queue.setSum("1");
                                            long qId=db.createQueue(queue);

                                            //Record a transaction into reconciliation
                                            Reconciliation rec=new Reconciliation();
                                            rec.setDes("Transaction ID: " + afterComitSale.gettId()+" | "+afterComitSale.getpName() + " | " + afterComitSale.getAmount() + " Rwf | " + afterComitSale.getQuantity() + " Liters");
                                                    rec.setTra_Id(afterComitSale.gettId());
                                            db.createRec(rec);

                                            //waking up the service
                                            wakeAlarm();
                                            monitor.setText("On Queue: "+qId);
                                            dialog.dismiss();
                                        }

                                        //_________Proceed on payment________\\
                                            jo.put("tId", afterComitSale.gettId());
                                            jo.put("pMode", afterComitSale.getpMode());
                                            jo.put("request", "proceed");

                                            //request payment
                                            RequestPayment runner = new RequestPayment();
                                            runner.execute(jo.toString());
                                            dialog.dismiss();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        monitor.setText("Verfy Failed");
                                        dialog.dismiss();
                                    }catch (NullPointerException e){
                                        e.printStackTrace();
                                        monitor.setText("Verfy Failed");
                                        dialog.dismiss();
                                    }
                                }else{
                                    monitor.setText("Empty Fields");
                                    dialog.dismiss();
                                }
                            } else {
                                monitor.setText("Empty Fields");
                                dialog.dismiss();
                            }
                        }
                    });

                    //cancelling a transaction
                    confCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ((!confProd.getText().toString().isEmpty())&&(!confAmnt.getText().toString().isEmpty())) {
                                //request payment
                                if((Double.parseDouble(confAmnt.getText().toString())>1)&&(Double.parseDouble(confqty.getText().toString())>=0)){//remove 0 and set 1

                                    JSONObject jo=new JSONObject();
                                    try {
                                        jo.put("tId", afterComitSale.gettId());
                                        jo.put("pMode", afterComitSale.getpMode());
                                        jo.put("request", "cancel");
                                        dialog.dismiss();

                                        //request payment
                                        RequestPayment runner = new RequestPayment();
                                        runner.execute(jo.toString());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        monitor.setText("Verify Failed");
                                        dialog.dismiss();
                                    }

                                }else{
                                    monitor.setText("Empty Fields");
                                    dialog.dismiss();
                                }
                            } else {
                                monitor.setText("Empty Fields");
                                dialog.dismiss();
                            }
                        }
                    });

                    dialog.show();
                }
                monitor.setText("");
                monitor.clearComposingText();
            }catch (JSONException e) {
            e.printStackTrace();
                //when invalid response from server is detected
            monitor.setText("Fata error in transaction");
        }catch (NullPointerException e){
            e.printStackTrace();
            monitor.setText("Check Internet");
        }
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            monitor.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }

    //___________________Request Payment__________________________\\
    private class RequestPayment extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Requesting Payment..."); // Calls onProgressUpdate()
            String json = params[0];

            try {

                String url="http://41.74.172.132:8080/PetroStationManager/androiddata/getPaymentData";

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

            }  catch (MalformedURLException e) {
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

            //Getting printing Data from server
            try {
                System.out.println("Data from server: "+ result);
                JSONObject jo = new JSONObject(result);

                //Checking if Paymode is Cash And successful
                if((jo.get("validation").toString().equalsIgnoreCase("valid"))&&(jo.get("pMode").toString().equalsIgnoreCase("cash"))){

                   //when a transaction was by Cash
                    if(jo.get("pStatus").toString().equalsIgnoreCase("success")){
                    PrintData print=new PrintData();
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

                        //Record a transaction into reconciliation
                        Reconciliation rec=new Reconciliation();
                        rec.setDes("Transaction ID: "+jo.get("traId").toString()+" | "+jo.get("pName").toString() + " | " + jo.get("amount").toString() + " Rwf | " + jo.get("quantity").toString()+" Liters");
                                rec.setTra_Id(jo.get("traId").toString());
                        db.createRec(rec);

                        //handling printing
                        PrintingHandler ph=new PrintingHandler();
                        String printing=ph.dataToPrint(print, context);

                        //remove dialog and clear transaction id into transaction manager
                        Boolean transCheck=transMan.destroyTransaction();
                        if(!transCheck)
                            monitor.setText("Clearing local Transaction Failed");
                        else
                            monitor.setText(printing +" and "+" Local Cache cleared");

                        dialog.dismiss();

                    }else{
                        monitor.setText("Transaction Status: "+jo.get("pStatus").toString());
                        dialog.dismiss();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                monitor.setText("Printing Data missing");
            }catch (NullPointerException e){
                e.printStackTrace();
                monitor.setText("Check Internet");
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            monitor.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }

    //________________________Setting alarm manager__________________________\\
    public void wakeAlarm(){
        cal = Calendar.getInstance();

        alarmIntent = new Intent(getApplicationContext(), PrintService.class);

        pintent = PendingIntent.getService(getApplicationContext(), 0, alarmIntent, 0);

        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //clean alarm cache for previous pending intent
        alarm.cancel(pintent);

        // schedule for every 3 seconds
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 3 * 1000, pintent);
    }

// Cancel alarm
    public void cancelAlarm(){

        alarm.cancel(pintent);
    }
    //kill Alarm
    public void killAlarm(PendingIntent pi, AlarmManager am){
        am.cancel(pi);
    }

    //Reset UI after activity
    public  void appReload(){

    }

    //_________________custom copy and paste____________________________\\
    private ActionMode.Callback amntCP = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.copy:
                    int min = 0;
                    int max = amnt.getText().length();
                    if (amnt.isFocused()) {
                        final int selStart = amnt.getSelectionStart();
                        final int selEnd = amnt.getSelectionEnd();

                        min = Math.max(0, Math.min(selStart, selEnd));
                        max = Math.max(0, Math.max(selStart, selEnd));
                    }
                    // Perform your definition lookup with the selected text
                    final CharSequence selectedText = amnt.getText()
                            .subSequence(min, max);
                    String text = selectedText.toString();

                    myClip = ClipData.newPlainText("text", text);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getApplicationContext(), "Text Copied",
                            Toast.LENGTH_SHORT).show();
                    // Finish and close the ActionMode
                    mode.finish();
                    return true;
                case android.R.id.cut:
                    // custom code to get cut functionality according
                    // to customer requirement
                    return true;
                case android.R.id.paste:
                    // custom code to get paste functionality according
                    // to customer requirement
                    return true;

                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    };

    private ActionMode.Callback qtyCP = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.copy:
                    int min = 0;
                    int max = qty.getText().length();
                    if (qty.isFocused()) {
                        final int selStart = qty.getSelectionStart();
                        final int selEnd = qty.getSelectionEnd();

                        min = Math.max(0, Math.min(selStart, selEnd));
                        max = Math.max(0, Math.max(selStart, selEnd));
                    }
                    // Perform your definition lookup with the selected text
                    final CharSequence selectedText = qty.getText()
                            .subSequence(min, max);
                    String text = selectedText.toString();

                    myClip = ClipData.newPlainText("text", text);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getApplicationContext(), "Text Copied",
                            Toast.LENGTH_SHORT).show();
                    // Finish and close the ActionMode
                    mode.finish();
                    return true;
                case android.R.id.cut:
                    // custom code to get cut functionality according
                    // to customer requirement
                    return true;
                case android.R.id.paste:
                    // custom code to get paste functionality according
                    // to customer requirement
                    return true;

                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    };

    //____________________Reconciliation Room function________________________\\
    public void recHandler(){
        dialog=new Dialog(context);
        dialog.setContentView(R.layout.reconciliation);
        dialog.setTitle("Reconciliation Room");
        rcount=(TextView) dialog.findViewById(R.id.rcount);
        rlist=(ListView) dialog.findViewById(R.id.reclist);
        recDone=(Button) dialog.findViewById(R.id.done);

        rcount.setText(String.valueOf((int) recCount()));

        List<Reconciliation> reconsList=new ArrayList<Reconciliation>();
        List<String> showList=new ArrayList<String>();
        final List<String> traIdList=new ArrayList<String>();
        reconsList=getRList();
        Iterator itr=reconsList.iterator();
        while(itr.hasNext()){
            Reconciliation rec=new Reconciliation();
            rec=(Reconciliation) itr.next();
            showList.add(rec.getDes());
            traIdList.add(rec.getTra_Id());
        }

        ArrayAdapter recAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, showList);
        rlist.setAdapter(recAdapter);
        rlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub

                //validate if it is not on the queue
                TQueue tq=new TQueue();
                int tra_id=arg2;
                try{
                    tq=db.getSingleQueue(Long.parseLong(traIdList.get(arg2)));
                    TQueue tQueue=new TQueue();
                    tQueue=tq;
                    if(!tQueue.gettId().equals(null)) {
                        monitor.setText("Wait until the process is completed");
                        dialog.dismiss();
                    }
                    else{
                        TQueue tQ=new TQueue();
                        tQ.settId(traIdList.get(tra_id));
                        db.createQueue(tQ);

                        wakeAlarm();
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                    TQueue tQ=new TQueue();
                    tQ.settId(traIdList.get(tra_id));
                    db.createQueue(tQ);

                    wakeAlarm();
                }


                Toast.makeText(context,"Reconciliate: "+traIdList.get(arg2),Toast.LENGTH_SHORT).show();

            }
        });

        //setting Done on click
        recDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //show Dialog
        dialog.show();
    }
    //rec count
    public double recCount(){
        try{
            return db.getRecCount();
        }catch (NullPointerException e){e.printStackTrace();
            return 0;
        }
    }
    //rec list
    public List<Reconciliation> getRList(){
        try{
        List<Reconciliation> rList=db.getAllRec();
        return rList;
    }catch (NullPointerException e){
        e.printStackTrace();
        return null;
    }
    }
}
