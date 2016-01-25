package com.example.owner.petrolmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Owner on 12/19/2015.
 */
public class AppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "POS Started.", Toast.LENGTH_LONG).show();

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
        //Starting my Engen Activity
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appIntent);}

    }
}
