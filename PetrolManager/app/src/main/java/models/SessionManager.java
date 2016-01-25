package models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.owner.petrolmanager.MainActivity;

/**
 * Created by Owner on 12/7/2015.
 */
public class SessionManager {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared transPreference mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "loginData";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String keyid="u_id";
    public static final String keyName ="u_name";
    public static final String keyType ="u_type";
    public static final String keyStatus ="u_status";
    public static final String keyValidation ="validation";
    public static final String keyBId="b_id";
    public static final String keyBName="b_name";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public boolean createLoginSession(SessionData sesData){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing data in transPreference
        editor.putString(keyid, sesData.getU_id());
        editor.putString(keyName, sesData.getU_name());
        editor.putString(keyType, sesData.getU_type());
        editor.putString(keyStatus, sesData.getU_status());
        editor.putString(keyValidation, sesData.getValidation());
        editor.putString(keyBId, sesData.getB_id());
        editor.putString(keyBName,sesData.getB_name());

        // commit changes
        editor.commit();

        return true;
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, MainActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }



    /**
     * Get stored session data
     * */
    public SessionData getUserDetails(){
        SessionData sesData = new SessionData();
        // user id
        sesData.setU_id(pref.getString(keyid, null));
        sesData.setU_name(pref.getString(keyName, null));
        sesData.setU_type(pref.getString(keyType, null));
        sesData.setU_status(pref.getString(keyStatus, null));
        sesData.setValidation(pref.getString(keyValidation, null));
        sesData.setB_id(pref.getString(keyBId, null));
        sesData.setB_name(pref.getString(keyBName, null));
        //user.put(KEY_NAME, transPreference.getString(KEY_NAME, null));


        // return user
        return sesData;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, MainActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
