package com.poli.usp.erichain.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.poli.usp.erichain.Constants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.poli.usp.erichain.data.remote.models.MessageResponse;

import static com.poli.usp.erichain.data.local.Utils.hexStringToByteArray;

/**
 * Created by Bruno on 13-Aug-17.
 * Modified by aerina on 23/08/18.
 */

public class MessageDatabaseHelper {
    private SQLiteOpenHelper _openHelper;

    public MessageDatabaseHelper(Context context) {
        _openHelper = new BaseSQLiteOpenHelper(context);
    }

    public List<MessageResponse> getMessagesFromContact(String user_id, String sender_id) {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        Cursor cursor =  db.rawQuery("select " +
                Constants.DB_MESSAGES_FIELD_SENDER_ID + ", " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_ID + ", " +
                Constants.DB_MESSAGES_FIELD_SENDER_NAME + ", " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_NAME + ", " +
                Constants.DB_MESSAGES_FIELD_TEXT + ", " +
                Constants.DB_MESSAGES_FIELD_SENT_AT + ", " +
                Constants.DB_MESSAGES_FIELD_PREV_HASH + " from " +
                Constants.DB_MESSAGES_TABLE + " where (" +
                Constants.DB_MESSAGES_FIELD_SENDER_ID + " = ? AND " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_ID + " = ?) OR (" +
                Constants.DB_MESSAGES_FIELD_SENDER_ID + " = ? AND " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_ID + " = ?)",  new String[] {
                sender_id, user_id, user_id, sender_id });
        List<MessageResponse> messages = convertCursorToMessages(cursor);
        db.close();
        return messages;
    }



    public long add(MessageResponse message) {
        // sha-3 256 de "erina" = 538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2
        // This msg0 is used as genesis message in the chain
        MessageResponse  msg0 = new MessageResponse(message.getSender(), message.getReceiver(), "mensagem 0",
                null, hexStringToByteArray("538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2"));
        // To add the new message, we need to verify if the hash chain properties are satisfied.
        List<MessageResponse> messages = getMessagesFromContact(message.getSender().getId(), message.getReceiver().getId());
        if (messages == null && messages.isEmpty()) {
            // início da conversa = não tem mensagem anterior para cálculo do prevHash
            boolean correct = false;
            correct = MessageResponse.verifyPrevHash(msg0, message);
            if (!correct) {
                System.out.println("Hash is not correct.");
                return -1;
            }
        }
        else {
            MessageResponse lastMessage = msg0;
            for (MessageResponse msg: messages) {
                lastMessage = msg;
            }
            boolean correct = false;
            correct = MessageResponse.verifyPrevHash(lastMessage, message);
            if (!correct) {
                System.out.println("Hash is not correct.");
                return -1;
            }
        }
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return 0;
        }
        ContentValues row = convertMessageToContentValues(message);
        long id = db.insert(Constants.DB_MESSAGES_TABLE, null, row);
        db.close();
        return id;
    }

    public void deleteConversation(String user_id, String sender_id) {
        // TODO: In theory, I think that it can't be done.
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return;
        }
        db.delete(Constants.DB_MESSAGES_TABLE, "(" +
                Constants.DB_MESSAGES_FIELD_SENDER_ID + " = ? AND " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_ID + " = ?) OR (" +
                Constants.DB_MESSAGES_FIELD_SENDER_ID + " = ? AND " +
                Constants.DB_MESSAGES_FIELD_RECEIVER_ID + " = ?)",new String[] {
                sender_id, user_id, user_id, sender_id });
        db.close();
    }

    private ContentValues convertMessageToContentValues(MessageResponse message){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.DB_MESSAGES_FIELD_SENDER_ID, message.getSender().getId());
        contentValues.put(Constants.DB_MESSAGES_FIELD_RECEIVER_ID, message.getReceiver().getId());
        contentValues.put(Constants.DB_MESSAGES_FIELD_SENDER_NAME, message.getSender().getName());
        contentValues.put(Constants.DB_MESSAGES_FIELD_RECEIVER_NAME, message.getReceiver().getName());
        contentValues.put(Constants.DB_MESSAGES_FIELD_TEXT, message.getMessage());
        contentValues.put(Constants.DB_MESSAGES_FIELD_SENT_AT, message.getSentAt().getTime());
        contentValues.put(Constants.DB_MESSAGES_FIELD_PREV_HASH, message.getPrevHash());
        return contentValues;
    }

    private List<MessageResponse> convertCursorToMessages(Cursor cursor){
        // TODO ver onde essa função é usada
        List<MessageResponse> messages = new ArrayList<>();
        while (cursor.moveToNext()) {
            String senderId = cursor.getString(0);
            String receiverId = cursor.getString(1);
            String senderName = cursor.getString(2);
            String receiverName = cursor.getString(3);
            String text = cursor.getString(4);
            Timestamp sentAt = new Timestamp(cursor.getLong(5));
            byte[] prevHash = cursor.getBlob(6);
            //byte[] prevHash = new Timestamp(cursor.getLong(6)); TODO ver como colocar isso, talvez
            Contact sender = new Contact(senderId, senderName);
            Contact receiver = new Contact(receiverId, receiverName);
            messages.add(new MessageResponse(sender, receiver, text, sentAt, prevHash));
        }
        return messages;
    }
}
