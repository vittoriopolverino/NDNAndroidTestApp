package vittorio.com.ndntestapp.NdnManager;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.util.Blob;

import java.io.IOException;

public class Echo implements OnInterestCallback, OnRegisterFailed {

    private KeyChain keyChain_;
    private Name certificateName_;
    int mResponseCount = 0;
    private String mFeedback;

    public Echo(KeyChain keyChain, Name certificateName) {
        keyChain_ = keyChain;
        certificateName_ = certificateName;
    }

    public Echo(KeyChain keyChain) {
        this.keyChain_ = keyChain;
    }

    ;

    public void
    onInterest(Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {

        ++mResponseCount;

        // Make and sign a Data packet.
        Data data = new Data(interest.getName());
        String content = "Echo " + interest.getName().toUri();
        data.setContent(new Blob(content));

        try {
            keyChain_.sign(data, certificateName_);
        } catch (SecurityException pE) {
            pE.printStackTrace();
        }
        mFeedback = "Sent content " + content;
        System.out.println("Sent content " + content);
        try {
            face.putData(data);
        } catch (IOException ex) {
            System.out.println("Echo: IOException in sending data " + ex.getMessage());
            mFeedback = "Echo: IOException in sending data " + ex.getMessage();
        }
    }

    public void
    onRegisterFailed(Name prefix) {
        ++mResponseCount;
        System.out.println("Register failed for prefix " + prefix.toUri());
        mFeedback = "Register failed for prefix " + prefix.toUri();
    }

    public String getFeedback() {
        return this.mFeedback;
    }

    public int getResponseCount() {
        return this.mResponseCount;
    }

}