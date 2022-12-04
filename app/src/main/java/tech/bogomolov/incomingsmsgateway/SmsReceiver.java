package tech.bogomolov.incomingsmsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecated")
public class SmsReceiver extends BroadcastReceiver {

    private Context context;
    @SuppressWarnings("deprecated")
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;//super
        }
        int slot = bundle.getInt("slot", -1);
        Log.d("SMSPDU:","Found slot " + String.valueOf(slot));

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0) {
            return;
        }
        Log.i("SMS:","PDU Found");

        ArrayList<ForwardingConfig> configs = ForwardingConfig.getAll(context);
        String asterisk = context.getString(R.string.asterisk);

        for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String sender = message.getOriginatingAddress();

            for (ForwardingConfig config : configs) {
                if (sender.equals(config.getSender()) || config.getSender().equals(asterisk)) {
                    try {
                        JSONObject messageJson = this.prepareMessage(sender, message.getMessageBody(), config.getVars());
                        messageJson.put("slot", slot);
                        this.callWebHook(config.getUrl(), messageJson.toString());
                    }catch(JSONException e){ Log.e("SendMessage","JSONError");}
                    break;
                }
            }
        }
    }

    protected void callWebHook(String url, String message) {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data data = new Data.Builder()
                .putString(WebHookWorkRequest.DATA_URL, url)
                .putString(WebHookWorkRequest.DATA_TEXT, message)
                .build();

        WorkRequest webhookWorkRequest =
                new OneTimeWorkRequest.Builder(WebHookWorkRequest.class)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.EXPONENTIAL,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS
                        )
                        .setInputData(data)
                        .build();

        WorkManager
                .getInstance(this.context)
                .enqueue(webhookWorkRequest);

    }

    @NonNull
    private JSONObject prepareMessage(String sender, String message, String Vars) {
        JSONObject messageData = new JSONObject();
        try {
            messageData.put("from", sender);
            messageData.put("text", message);
            messageData.put("extra",Vars);
        } catch (Exception e) {
            Log.e("SmsGateway", "Exception prepareMessage" + e);
        }

        return messageData;
    }
}
