package com.poli.usp.erichain.ui.chat;

import java.util.List;

import com.poli.usp.erichain.data.remote.models.MessageResponse;
import com.poli.usp.erichain.ui.base.BasePresenter;
import rx.Subscription;

/**
 * Created by Bruno on 11-Aug-17.
 */

public class ChatPresenter extends BasePresenter<ChatMvpView> {

    private ChatMvpView mChatMvpView;
    private List<MessageResponse> mCachedMessages;
    private Subscription mSubscription;

    public ChatPresenter(){

    }


    public void loadMessages(String user_id, String sender_id){
//        mChatMvpView.showMessages(messages);
        mChatMvpView.loadContactMessages(user_id, sender_id);
    }

    public void newMessage(MessageResponse messageResponse){
        mChatMvpView.addMessage(messageResponse);
        mChatMvpView.saveMessage(messageResponse);
//        long id = mChatMvpView.saveMessage(messageResponse);
    }

    @Override
    public void attachView(ChatMvpView mvpView) {
        super.attachView(mvpView);
        mChatMvpView = mvpView;
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mSubscription != null) mSubscription.unsubscribe();
    }
}
