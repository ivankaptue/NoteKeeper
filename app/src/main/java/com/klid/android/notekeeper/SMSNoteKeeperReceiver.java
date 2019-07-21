package com.klid.android.notekeeper;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class SMSNoteKeeperReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            if (sms == null) return;

            String from = "";
            String message = "";
            for (Object sm : sms) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sm);

                from = smsMessage.getOriginatingAddress();
                message = smsMessage.getMessageBody();
            }
            SMSReceiveNotification.notify(context, from, message);
            //Toast.makeText(context, smsMessageStr.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
