package com.example.owner.petrolmanager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;

public class CompanySale extends ActionBarActivity {

    String TAG = "PetrolLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_sale);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            Log.e(TAG, "action" + "Menu Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(TAG, "action" +"Back Key Pressed" + ",reason:" + "Not allowed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
