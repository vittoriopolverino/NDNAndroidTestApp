package vittorio.com.ndntestapp.BroadcastReceiver;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import vittorio.com.ndntestapp.Service.SocketCommunicationService;

public class OnBootComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, SocketCommunicationService.class);
            context.startService(serviceIntent);
        }
    }
}