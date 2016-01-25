package com.example.owner.petrolmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import models.DBHelper;
import models.SessionData;
import models.SessionManager;

public class MainActivity extends ActionBarActivity {

    public EditText pw;
    public TextView monitor;
    public Button login;

    HomeWatcher mHomeWatcher;

    Context context;

    InputMethodManager imm;

    DBHelper db;
    PrintService print;
    PersonalSale pers;

    String TAG = "PetrolLog";

    ClipData myClip;
    ClipboardManager myClipboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        mHomeWatcher= new HomeWatcher(getApplicationContext());
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                // home key is pressed...
                Toast.makeText(getApplicationContext(), "Not Suitable Button", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }

            @Override
            public void onHomeLongPressed() {
                // home key is Long pressed...
                Toast.makeText(getApplicationContext(), "Not Suitable Button", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                //make activity sleep a while and start again
                //SystemClock.sleep(2000);//3senconds
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                finish();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        mHomeWatcher.startWatch();

        //pin box purify
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        pw=(EditText) findViewById(R.id.pw);
        pw.setTypeface(Typeface.DEFAULT);
        pw.setTransformationMethod(new PasswordTransformationMethod());

        //disable softkeyboard
        imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pw.getWindowToken(), 0);

        //Text copy and paste Solution for future use
        pw.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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
                        int max = pw.getText().length();
                        if (pw.isFocused()) {
                            final int selStart = pw.getSelectionStart();
                            final int selEnd = pw.getSelectionEnd();

                            min = Math.max(0, Math.min(selStart, selEnd));
                            max = Math.max(0, Math.max(selStart, selEnd));
                        }
                        // Perform your definition lookup with the selected text
                        final CharSequence selectedText = pw.getText()
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
        });

        //_______________________________________//

        //initialize views
        db=new DBHelper(getApplicationContext());
        print=new PrintService();
        pers=new PersonalSale();

        try{
            db.truncateQueue();
            db.truncateRec();
            print.stopService();
            PersonalSale personal=new PersonalSale();
            Intent alarmIntent =new Intent(this, PrintService.class);
            PendingIntent pi= PendingIntent.getService(this, 0, alarmIntent, 0);
            AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
            pers.killAlarm(pi,am);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        monitor= (TextView) findViewById(R.id.monitor);

        login=(Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Starting login processing
                if(! pw.getText().toString().isEmpty()) {

                    LoginAsync login = new LoginAsync();
                    String pin = pw.getText().toString();
                    login.execute(pin);

//                loginPro(pw.getText().toString());
                }
                else
                    monitor.setText("Empty Pin");
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            Log.e(TAG, "action:" +"Menu Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(TAG, "action:" +"Back Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //__________________Asyncronous Task of main activity_________________________\\
    private class LoginAsync extends AsyncTask<String, String, String> {

        private SessionData sesData;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Verifying..."); // Calls onProgressUpdate()
            String pin = params[0];

            //creating a json file to be posted
            JSONObject obj = new JSONObject();

            try {
                obj.put("pin",pin);
                // JSONArray ja=new JSONArray();
                // ja.put(obj);
                //JSONObject  rootObj = new JSONObject();
                //rootObj.put("login",ja);

                // String json=rootObj.toString();
                String json=obj.toString();
                String url="http://41.74.172.132:8080/PetroStationManager/androiddata/login";

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
//            try{
//                if((result.isEmpty()||(result==null))){
//                    pw.setText("");
//                    AlertDialogManager alter=new AlertDialogManager();
//                    alter.showAlertDialog(getApplicationContext(),"Something Is Wrong","Check your Internet Connection and Try Again After Some Time",null);
//                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
//                    startActivity(intent);
//                }
//
//            }catch (NullPointerException e){
//                e.printStackTrace();
//            }
//


            try {

                System.out.println("Data from server: "+result);
                JSONObject jsonObject = new JSONObject(result);

                //Get the instance of JSONArray that contains JSONObjects
                //JSONArray jsonArray = jsonRootObject.optJSONArray("login");
                SessionData sessData = new SessionData();

                //Iterate the jsonArray and print the info of JSONObjects
                // for(int i=0; i < jsonArray.length(); i++){
                //  JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Setting session data
                sessData.setU_id(jsonObject.get("uId").toString());
                sessData.setU_name(jsonObject.get("uName").toString());
                sessData.setU_type(jsonObject.get("uType").toString());
                sessData.setU_status(jsonObject.get("uStatus").toString());
                sessData.setValidation(jsonObject.get("validation").toString());
                sessData.setB_id(jsonObject.get("bId").toString());
                sessData.setB_name(jsonObject.get("bName").toString());

                //reserved code
//                sessData.setU_id(jsonObject.optString("u_Id").toString());
//                sessData.setU_name(jsonObject.optString("u_Name").toString());
//                sessData.setU_type(jsonObject.optString("u_Tytpe").toString());
//                sessData.setU_status(jsonObject.optString("u_Status").toString());
//                sessData.setValidation(jsonObject.optString("validation").toString());
//                sessData.setB_id(jsonObject.optString("b_Id").toString());
//                sessData.setB_name(jsonObject.optString("b_Name").toString());

                //  }
                // monitor.setText("Done");
                if ((sessData.getValidation().equalsIgnoreCase("valid")) && (sessData.getU_status().equalsIgnoreCase("active")) && (sessData.getU_type().equalsIgnoreCase("pumpist"))) {
                    //Credential accepted, creating session
                    SessionManager sesManager = new SessionManager(getApplicationContext());
                    sesManager.createLoginSession(sessData);
                    monitor.setText("Fill your Credential");
                    pw.setText("");

                    // After logout redirect user to Loing Activity
                    Intent intent = new Intent(getApplicationContext(), Seller.class);
                    // Stating Seller Activity
                    startActivity(intent);
                    //finish current activity
                    finish();
                } else
                    monitor.setText("Invalid User");

            } catch (JSONException e) {
                e.printStackTrace();
                monitor.setText("System Faillure");
                pw.setText("");
            }catch (NullPointerException e){
                e.printStackTrace();
                dialog("Check Internet");
                pw.setText("");
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
            //Task to be done while fetching info from url
            monitor.setText(text[0]);
        }
    }

    public void dialog(String message){
        pw.setText("");
        monitor.setText(message);
    }

}
