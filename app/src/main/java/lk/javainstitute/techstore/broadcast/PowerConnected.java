package lk.javainstitute.techstore.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class PowerConnected extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context.getApplicationContext(), "App performance has been increased.", Toast.LENGTH_LONG).show();
    }

}
