package com.poli.usp.erichain.ui.chat;

import java.util.List;

import com.poli.usp.erichain.data.remote.models.MessageResponse;
import com.poli.usp.erichain.ui.base.MvpView;

/**
 * Created by Bruno on 11-Aug-17.
 * Modified by aerina on 23/08/18.
 */

public interface ChatMvpView extends MvpView{
    void addMessage(MessageResponse message);

    long saveMessage(MessageResponse message);

    void showMessages(List<MessageResponse> messages);

    void loadContactMessages(String user_id, String sender_id);
}
