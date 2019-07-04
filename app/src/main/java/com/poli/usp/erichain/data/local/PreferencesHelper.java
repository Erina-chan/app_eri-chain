package com.poli.usp.erichain.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.poli.usp.erichain.Constants;

/**
 * Created by mobile2you on 11/08/16.
 * Modified by aerina 23/08/18.
 */
public class PreferencesHelper {

    public static final String SHARED_PREFERENCES_NAME = Constants.PACKAGE_NAME + ".SHARED_PREFERENCES";

    public static final String PREF_USER_ID = SHARED_PREFERENCES_NAME + ".PREF_USER_ID";
    public static final String PREF_USER_PASSWORD = SHARED_PREFERENCES_NAME + ".PREF_USER_PASSWORD";

    private SharedPreferences mSharedPreferences;

    private PreferencesHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static PreferencesHelper sInstance;

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesHelper(context);
        }
    }

    public static synchronized PreferencesHelper getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferencesHelper.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    public void putUserId(String id){
        mSharedPreferences.edit().putString(PREF_USER_ID, id).apply();
    }

    public String getUserId(){
        return mSharedPreferences.getString(PREF_USER_ID, "");
    }


    public void putUserPassword(String password){
        mSharedPreferences.edit().putString(PREF_USER_PASSWORD, password).apply();
    }

    public String getUserPassword(){
        return mSharedPreferences.getString(PREF_USER_PASSWORD, "");
    }

}