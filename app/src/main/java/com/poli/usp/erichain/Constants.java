package com.poli.usp.erichain;

/**
 * Created by mobile2you on 18/08/16.
 * Modified by aerina on 23/08/18.
 * Modified by aerina on 12/08/21.
 */
public class Constants {
    public static final String PACKAGE_NAME = "com.poli.usp.erichain";

    public static final String EXTRA_MYSELF = PACKAGE_NAME + "EXTRA_MYSELF";
    public static final String EXTRA_CONTACT = PACKAGE_NAME + "EXTRA_CONTACT";
    public static final String EXTRA_DIRECT_CONNECTION = PACKAGE_NAME + "EXTRA_DIRECT_CONNECTION";

    //    DATABSE CONSTANTS: CONTACTS
    public static final String DB_CONTACTS_TABLE = "contacts";
    public static final String DB_CONTACT_FIELD_ID = "_id";
    public static final String DB_CONTACT_FIELD_NAME = "name";
    public static final String DB_CONTACT_FIELD_IP = "ip";
    public static final String DB_CONTACT_FIELD_PORT = "port";
    public static final String DB_CONTACT_FIELD_SIGN_ENCODED_KEY = "sign_encoded_key";  // DSA key
    public static final String DB_CONTACT_FIELD_CHAT_ENCODED_KEY = "chat_encoded_key";  // PGP key
    public static final String DB_CONTACT_FIELD_SEED_ENCODED_KEY = "seed_encoded_key";  // DH key

    //    DATABSE CONSTANTS: MESSAGES
    public static final String DB_MESSAGES_TABLE = "messages";
    public static final String DB_MESSAGES_FIELD_MESSAGE_ID = "message_id";
    public static final String DB_MESSAGES_FIELD_SENDER_ID = "_id";
    public static final String DB_MESSAGES_FIELD_RECEIVER_ID = "receiver_id";
    public static final String DB_MESSAGES_FIELD_SENDER_NAME = "sender_name";
    public static final String DB_MESSAGES_FIELD_RECEIVER_NAME = "receiver_name";
    public static final String DB_MESSAGES_FIELD_TEXT = "text";
    public static final String DB_MESSAGES_FIELD_SENT_AT = "sent_at";
    public static final String DB_MESSAGES_FIELD_PREV_HASH = "prev_hash";
    public static final String DB_MESSAGES_FIELD_MESSAGE_COUNTER = "message_counter";

    public static final String FILTER_CHAT_RECEIVER = "FILTER_CHAT_RECEIVER";
    public static final String FILTER_DHT_CONNECTION = "FILTER_DHT_CONNECTION";
    public static final String FILTER_SIGNATURE_UPDATE = "FILTER_SIGNATURE_UPDATE";
    public static final String FILTER_CERTIFICATE_SIGN = "FILTER_CERTIFICATE_SIGN";

    public static final String NSD_SERVICE_NAME = "whatsp2p";
    public static final String NSD_SERVICE_TYPE = "_whatsp2p._tcp";

    public static final String PGP_KEY_ALIAS = "whatsp2p";

}