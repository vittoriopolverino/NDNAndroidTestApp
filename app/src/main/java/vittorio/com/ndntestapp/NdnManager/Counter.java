package vittorio.com.ndntestapp.NdnManager;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;


/**
 * Created by Vittorio on 13/12/2016.
 */

public class Counter implements OnData, OnTimeout {

    private StringBuilder mFeedback;
    private int mCallbackCount;

    public Counter() {
        mFeedback = new StringBuilder(100);
        mCallbackCount = 0;
    }

    public void onData(Interest interest, Data data) {
        ++mCallbackCount;
        mFeedback.append("Got data packet with name " + " "  + data.getName().toUri())
                .append('\n')
                .append(data.getContent().toString())
                .append('\n');
    }

    public void onTimeout(Interest interest) {
        ++mCallbackCount;
        mFeedback.append("Time out for interest " + " " + interest.getName().toUri());
    }

    public int getmCallbackCount() {
        return this.mCallbackCount;
    }

    public String getFeedback() {
        return this.mFeedback.toString();
    }
}
