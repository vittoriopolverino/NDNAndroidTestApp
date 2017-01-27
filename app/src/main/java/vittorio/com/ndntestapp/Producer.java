package vittorio.com.ndntestapp;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterest;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.util.Blob;

import java.io.IOException;

/**
 * Created by Vittorio on 12/01/2017.
 */

public class Producer implements OnInterest, OnRegisterFailed {

    private final static String PRODUCER_TAG = "[Producer]";
    public String ProducerMessage;

    private KeyChain mKeyChain;
    private Face mFace;
    private Name mName;

    private ProducerThread mProducerThread;

    public Producer() {
        try {
            mFace = new Face("localhost");

            mKeyChain = new KeyChain();
//			mKeyChain.getDefaultCertificateName();

//			mFace.setCommandCertificateName(mKeyChain.getDefaultCertificateName());
            mFace.setCommandSigningInfo(mKeyChain,
                    mKeyChain.getDefaultCertificateName());

            mName = new Name("/ndn/test");

            mProducerThread = new ProducerThread();
            mProducerThread.start();

        } catch (SecurityException e) {
            e.printStackTrace();
            G.Log(PRODUCER_TAG, "Producer() error security.");
        }

        G.Log(PRODUCER_TAG, "Producer() Completed.");
    }

    @Override
    public void onInterest(Name prefix, Interest interest, Transport transport,
                           long registeredPrefixId) {

        G.Log(PRODUCER_TAG, "onInterest(): Prefix received: " + prefix.toString());

        // Create data
        Name name = new Name("/producer/reply");
        Data data = new Data(name);
        data.setContent(new Blob("Hello from producer".getBytes()));

        // Encode signed data
        try {
            mKeyChain.sign(data, mName);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Send data
        try {
            transport.send(data.getContent().buf());
            ProducerMessage = "SEND DATA: " + data.getContent().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProdMessage() {
        return this.ProducerMessage;
    }

    @Override
    public void onRegisterFailed(Name name) {
        G.Log("[Producer] Registration failed: " + name.toString());
        ProducerMessage = "[Producer] Registration failed: " + name.toString();
    }

    private class ProducerThread extends Thread {
        public void run() {
            G.Log(PRODUCER_TAG, "ProducerThread started.");

            try {
                mFace.registerPrefix(mName, Producer.this, Producer.this);

                while (true) {
                    mFace.processEvents();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (EncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            G.Log(PRODUCER_TAG, "ProducerThread::run() completed!");
        }
    }
}