package com.poli.usp.erichain.data.remote.models;

import com.poli.usp.erichain.data.local.Contact;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by mayerlevy on 10/30/17.
 * Modified by aerina on 23/08/18.
 */

public class BaseResponse implements Serializable {

    protected Contact sender;
    protected Contact receiver;
    protected Timestamp sentAt;

    public BaseResponse(Contact sender, Contact receiver) {
        super();
        this.sender = sender;
        this.receiver = receiver;

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        this.sentAt = new Timestamp(now.getTime());
    }

    public Contact getSender() {
        return sender;
    }

    public Contact getReceiver() {
        return receiver;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

}
