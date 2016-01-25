package com.example.owner.petrolmanager;

import android.app.ActivityGroup;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;

import models.DBHelper;
import models.SessionData;
import models.SessionManager;

public class Seller extends ActivityGroup {

    SessionManager sesManager;
    SessionData sesData;

    TextView unm;
    TabHost tabHost;

    String TAG = "PetrolLog";

    DBHelper db;
    PersonalSale pers;
    PrintService print;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //go no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_seller);

        //___________________Verifying login data___________________//
        sesManager=new SessionManager(getApplicationContext());
        //check either he was logged in or not
        if(!sesManager.isLoggedIn()) {
            // Shared Preferences
            SharedPreferences pref;

            // Editor for Shared preferences
            SharedPreferences.Editor editor;

            // Context
            Context _context;

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

        //___________________The Rest of the Application___________________//
        db=new DBHelper(getApplicationContext());
        pers=new PersonalSale();
        print=new PrintService();

        unm=(TextView) findViewById(R.id.unm);
        unm.setText(sesData.getU_name());

        tabHost = (TabHost) findViewById(R.id.pumptab);
        tabHost.setup(this.getLocalActivityManager());

        // Tab of Sale
        TabHost.TabSpec personal = tabHost.newTabSpec("Personal");
        // setting Title and Icon for the Tab
        personal.setIndicator("Personal", getResources().getDrawable(R.drawable.icon_trans_tab));
        Intent transIntent = new Intent(getApplicationContext(), PersonalSale.class);
        personal.setContent(transIntent);

        // Tab of Employee Statements
        TabHost.TabSpec company = tabHost.newTabSpec("Company");
        // setting Title and Icon for the Tab
        company.setIndicator("Company", getResources().getDrawable(R.drawable.icon_stat_tab));
        Intent statIntent = new Intent(getApplicationContext(), CompanySale.class);
        company.setContent(statIntent);

        // Adding all TabSpec to TabHost
        tabHost.addTab(personal); // Adding Sale tab
        tabHost.addTab(company); // Adding Statement tab
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            Log.e(TAG, "action:" + "Menu Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(TAG, "action:" +"Back Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //logout activity
    public void logout(View v){
        // Shared Preferences
        SharedPreferences pref;

        // Editor for Shared preferences
        SharedPreferences.Editor editor;

        // Context
        Context _context;

        // Shared pref mode
        int PRIVATE_MODE = 0;

        // Sharedpref file name
        String PREF_NAME = "loginData";
        pref = getApplicationContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.clear();
        editor.commit();

        try{
            db.truncateQueue();
            db.truncateRec();
            Intent alarmIntent =new Intent(this, PrintService.class);
            PendingIntent pi= PendingIntent.getService(this, 0, alarmIntent, 0);
            AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
            pers.killAlarm(pi, am);
            print.stopService();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
        // sesManager=new SessionManager(getApplicationContext());
        //sesManager.logoutUser();
    }
}
