package vittorio.com.ndntestapp.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import vittorio.com.ndntestapp.Service.SocketCommunicationService;

public class RestartService extends BroadcastReceiver {

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        context.startService(new Intent(context.getApplicationContext(), SocketCommunicationService.class));
    }
}