package com.poli.usp.erichain.data.local;

import android.app.Activity;
import android.app.ProgressDialog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mayerlevy on 10/5/17.
 * Modified by aerina on 23/08/18.
 */

public class ProgressDialogHelper {

    private ProgressDialog mProgressDialog;
    private Activity mActivity;
    private Timer mTimeoutTimer;

    public ProgressDialogHelper(Activity activity) {
        mActivity = activity;
        mTimeoutTimer = new Timer();
        initialize();
    }

    public void initialize() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    public void show(final String text) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(text);
                mProgressDialog.show();
            }
        });
    }

    public void hide() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        });
        mTimeoutTimer.cancel();
    }

}
