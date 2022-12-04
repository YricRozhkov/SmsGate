package tech.bogomolov.incomingsmsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver{

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.e("StartDeviceReceiver", "BROADCAST received " + intent.getAction());
    Intent myIntent = new Intent(context, MainActivity.class);
    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    myIntent.putExtra("key", "BC"); //Optional parameters
    context.startActivity(myIntent);
  }
}