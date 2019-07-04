package com.poli.usp.erichain.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.poli.usp.erichain.Constants;

/**
 * Created by Bruno on 15-Aug-17.
 * Modified by aerina 14-11-2018.
 */

public class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
    BaseSQLiteOpenHelper(Context context) {
        super(context, "main.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Constants.DB_CONTACTS_TABLE + " (" +
                Constants.DB_CONTACT_FIELD_ID + " text primary key, " +
                Constants.DB_CONTACT_FIELD_NAME + " text, " +
                Constants.DB_CONTACT_FIELD_IP + " text, "+
                Constants.DB_CONTACT_FIELD_PORT + " text, "+
                Constants.DB_CONTACT_FIELD_SIGN_ENCODED_KEY + " blob, "+
                Constants.DB_CONTACT_FIELD_CHAT_ENCODED_KEY + " blob)"
        );
        db.execSQL("create table " + Constants.DB_MESSAGES_TABLE + " (" +
                Constants.DB_MESSAGES_FIELD_MESSAGE_ID + " integer primary key autoincrement, " +
                Constants.DB_MESSAGES_FIELD_SENDER_ID + " text key, " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_ID + " text key, " +
                Constants.DB_MESSAGES_FIELD_SENDER_NAME + " text, " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_NAME + " text, " +
                Constants.DB_MESSAGES_FIELD_TEXT + " text, " +
                Constants.DB_MESSAGES_FIELD_SENT_AT + " DATETIME, " +
                Constants.DB_MESSAGES_FIELD_PREV_HASH + " blob)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
