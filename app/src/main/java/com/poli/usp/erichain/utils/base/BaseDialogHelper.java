package com.poli.usp.erichain.utils.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import poli.com.mobile2you.whatsp2p.R;

/**
 * Created by mobile2you on 11/08/16.
 * Modified by aerina on 23/08/18.
 */
public class BaseDialogHelper {

    /**
     * Shows progress dialog with standard message.
     */
    public static ProgressDialog createProgressDialog(Context context) {
        return createProgressDialog(context, context.getString(R.string.progress_dialog_standard_msg));
    }

    public static ProgressDialog createProgressDialog(Context context, String msg) {
        ProgressDialog dialog = buildProgressDialog(context);
        dialog.setMessage(msg);
        return dialog;
    }

    private static ProgressDialog buildProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static AlertDialog createDisclaimerDialog(Context context, String title, String msg, String positiveText, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context).setMessage(msg).setTitle(title).setPositiveButton(positiveText, listener).setCancelable(false);
        return alertDialogBuilder.create();
    }

}
