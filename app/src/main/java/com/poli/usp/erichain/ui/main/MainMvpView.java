package com.poli.usp.erichain.ui.main;

import com.poli.usp.erichain.data.local.Contact;
import com.poli.usp.erichain.ui.base.MvpView;

import java.util.List;

/**
 * Created by mobile2you on 28/11/16
 * Modified by aerina on 21/08/18.
 */

public interface MainMvpView extends MvpView {

    void showContacts(List<Contact> contacts);
}

