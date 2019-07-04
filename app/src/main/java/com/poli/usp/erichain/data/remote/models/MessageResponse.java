package com.poli.usp.erichain.data.remote.models;

import com.poli.usp.erichain.data.local.Contact;
import com.poli.usp.erichain.data.local.HashManager;
import com.poli.usp.erichain.data.local.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Bruno on 12-Aug-17.
 * Modified by aerina on 07-nov-2018.
 */

public class MessageResponse extends BaseResponse implements Serializable {

    private String message;
    protected byte[] prevHash;

    public MessageResponse(Contact sender, Contact receiver) {
        super(sender, receiver);
    }

    public MessageResponse(Contact sender, Contact receiver, String message, Timestamp sentAt, byte[] prevHash) {
        super(sender, receiver);
        this.message = message;
        this.sentAt = sentAt;
        this.prevHash = prevHash;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(byte[] prevHash) {
        this.prevHash = prevHash;
    }

    /**
     * This function verify the prevHash
     * @param newMessage
     * @return correct
     */
    public static boolean verifyPrevHash(MessageResponse lastMessage, MessageResponse newMessage) {
        if (newMessage == null || newMessage.getPrevHash() == null)
            return false;
        try {
            boolean correct = true;
            byte[] calculatedHash = MessageResponse.messageGenerateHash(lastMessage);
            for (int i = 0; i < calculatedHash.length; i++) {
                if (calculatedHash[i] != newMessage.prevHash[i]) {
                    correct = false;
                }
            }
            return correct;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This function return hash of MessageResponse object. The sha3 is used to generate hash.
     * @param message
     * @return hash
     */
    public static byte[] messageGenerateHash(MessageResponse message) {
        try {
            byte[] serializedMessage = Utils.serialize(message);
            String serializedMessageString = new String(serializedMessage);
            return HashManager.generateSha3(serializedMessageString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
