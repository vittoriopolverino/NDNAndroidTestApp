package vittorio.com.ndntestapp;

/**
 * Created by Vittorio on 12/01/2017.
 */

import java.util.logging.Logger;
import java.io.IOException;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.util.Blob;

public class TestRemotePrefixRegistration extends Thread{

    @Override
    public void run() {
        Face face = new Face("10.1.1.6");
        KeyChain keyChain = null;
        try {
            keyChain = buildTestKeyChain();
        } catch (SecurityException pE) {
            pE.printStackTrace();
        }
        try {
            face.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
        } catch (SecurityException pE) {
            pE.printStackTrace();
        }

        // test connection
        Interest interest = new Interest(new Name("/ndn/test"));
        interest.setInterestLifetimeMilliseconds(1000);
        try {
            face.expressInterest(interest, new OnData() {
                public void onData(Interest interest, Data data) {
                    logger.info("Data received (bytes): " + data.getContent().size());
                }
            }, new OnTimeout() {
                public void onTimeout(Interest interest) {
                    logger.severe("Failed to retrieve localhop data from NFD: " + interest.toUri());
                    //System.exit(1);
                }
            });
        } catch (IOException pE) {
            pE.printStackTrace();
        }

        // check if face is local
        try {
            logger.info("Face is local: " + face.isLocal());
        } catch (IOException pE) {
            pE.printStackTrace();
        }

        // register remotely
        try {
            face.registerPrefix(new Name("/ndn/test"), new OnInterestCallback() {
                public void onInterest
                        (Name prefix, Interest interest, Face face, long interestFilterId,
                         InterestFilter filter) {
                    Data data = new Data(interest.getName());
                    data.setContent(new Blob("..."));
                    try {
                        face.putData(data);
                    } catch (IOException e) {
                        logger.severe("Failed to send data: " + e.getMessage());
                        //System.exit(1);
                    }
                }
            }, new OnRegisterFailed() {
                public void onRegisterFailed(Name prefix) {
                    logger.severe("Failed to register the external forwarder: " + prefix.toUri());
                    //System.exit(1);
                }
            });
        } catch (IOException pE) {
            pE.printStackTrace();
        } catch (SecurityException pE) {
            pE.printStackTrace();
        }

        // process events until process is killed
        while (true) {
            try {
                face.processEvents();
            } catch (IOException pE) {
                pE.printStackTrace();
            } catch (EncodingException pE) {
                pE.printStackTrace();
            }
        }
    }

    /**
     * Setup an in-memory KeyChain with a default identity.
     *
     * @return
     * @throws net.named_data.jndn.security.SecurityException
     */
    public static KeyChain buildTestKeyChain() throws net.named_data.jndn.security.SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        KeyChain keyChain = new KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (net.named_data.jndn.security.SecurityException e) {
            keyChain.createIdentity(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
        }
        return keyChain;
    }

    private static final Logger logger = Logger.getLogger(TestRemotePrefixRegistration.class.getName());
}