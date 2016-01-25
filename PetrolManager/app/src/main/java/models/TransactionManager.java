package models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.owner.petrolmanager.MainActivity;

/**
 * Created by Owner on 12/17/2015.
 */
public class TransactionManager {
    // Shared Preferences
    SharedPreferences transPreference;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared transPreference mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "transactionData";

    // All Shared Preferences Keys
    private static final String IS_COMITED = "IsThereData";

    // Transaction (make variable public to access from outside)
    public static final String keytid ="tId";


    // Constructor
    public TransactionManager(Context context){
        this._context = context;
        transPreference = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = transPreference.edit();
    }


    public boolean createTransactionSession(String tId){
        // Storing transaction value as TRUE
        editor.putBoolean(IS_COMITED, true);

        // Storing data in transPreference
        editor.putString(keytid, tId);

        // commit changes
        editor.commit();

        return true;
    }


    public boolean checkTransaction(){
        // Check login status
        if(!this.isTransComited()){
            // There is no transaction made
            return false;
        }
        return true;
    }

    public String getTransId(){
        String transId = "";
        // trans id
        transId=transPreference.getString(keytid, null);

        // return trans Id
        return transId;
    }

    public boolean destroyTransaction(){

        transPreference = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = transPreference.edit();
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

       return true;
    }

    /**
     * Quick check for transaction
     * **/
    // Get transaction State State
    public boolean isTransComited(){
        return transPreference.getBoolean(IS_COMITED, false);
    }

}
