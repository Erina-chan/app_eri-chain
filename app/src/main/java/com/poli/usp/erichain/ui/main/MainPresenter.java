package com.poli.usp.erichain.ui.main;

import android.content.Context;
import java.util.List;
import com.poli.usp.erichain.data.local.Contact;
import com.poli.usp.erichain.data.local.ContactDatabaseHelper;
import com.poli.usp.erichain.data.local.MessageDatabaseHelper;
import com.poli.usp.erichain.data.local.PreferencesHelper;
import com.poli.usp.erichain.ui.base.BasePresenter;

import rx.Subscription;

/**
 * Created by mobile2you on 28/11/16.
 * Modified by aerina on 01/08/19.
 * Modified by aerina on 12/7/2021.
 * Modified by aerina on 10-may-2022.
 * Modified by aerina on 29-jun-2022.
 */

public class MainPresenter extends BasePresenter<MainMvpView> {

    private MainMvpView mMainMvpView;
    private Subscription mSubscription;
    private List<Contact> mCachedContacts;

    public MainPresenter() {
    }

    @Override
    public void attachView(MainMvpView mvpView) {
        super.attachView(mvpView);
        mMainMvpView = mvpView;
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public void loadContacts(Context context){
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        mCachedContacts = dbHelper.getContacts();
        mMainMvpView.showContacts(mCachedContacts);
    }

    public void addContact(Context context, Contact contact) {
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        dbHelper.add(contact);
        mCachedContacts.add(contact);
        mMainMvpView.showContacts(mCachedContacts);
    }

    public void addContact(Context context, String username, String ip, int port, byte[] signPublicKeyEncoded, byte[] chatPublicKeyRingEncoded, byte[] seedPublicKeyEncoded){
        Contact contact = new Contact(username);
        contact.setIp(ip);
        contact.setPort(port);
        contact.setSignPublicKeyEncoded(signPublicKeyEncoded);
        contact.setChatPublicKeyRingEncoded(chatPublicKeyRingEncoded);
        contact.setSeedPublicKeyEncoded(seedPublicKeyEncoded);
        addContact(context, contact);

    }

    public void deleteContact(Context context, Contact contact){
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        dbHelper.delete(contact.getId());
        mCachedContacts.remove(contact);
        mMainMvpView.showContacts(mCachedContacts);
    }

    public void updateContact(Context context, Contact contact){
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        dbHelper.update(contact);
        loadContacts(context);
    }

    public void deleteConversation(Context context, Contact contact){
        MessageDatabaseHelper dbHelper = new MessageDatabaseHelper(context);
        String user_id = PreferencesHelper.getInstance().getUserId();
        dbHelper.deleteConversation(user_id, contact.getId());
    }

}
