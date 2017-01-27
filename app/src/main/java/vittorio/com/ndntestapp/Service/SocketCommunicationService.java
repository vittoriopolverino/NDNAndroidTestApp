package vittorio.com.ndntestapp.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import vittorio.com.ndntestapp.SocketHandler;

public class SocketCommunicationService extends Service {

    private static final String TAG = SocketCommunicationService.class.getSimpleName();
    private static final String TEST_CASE_LAUNCHED_FROM = "testLaunchedFrom";
    private static final String PORT_NUMBER = "portNumber";
    private static final String TEST_CASE_NAME = "testCaseName";
    private static final String TEST_CASE_DESCRIPTION = "testCaseDescription";
    private static final String ROLE = "role";
    private static final String NFD_ADDRESS = "nfdAddress";;


    private ServerSocket serverSocket;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service started running");
        Toast.makeText(this, "Socket service is started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        Toast.makeText(this, "Service Killed", Toast.LENGTH_LONG).show();

        /*if (serverSocket != null) {
            try {
               // serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 4444;
        int count = 0;

        @Override
        public void run() {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                System.out.println("I'm waiting here: " + serverSocket.getLocalPort());
                //sendMessageToActivity("I'm waiting here: " + serverSocket.getLocalPort());

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    String messageFromClient = "";

                    //If no message sent from client, this code will block the program
                    messageFromClient = dataInputStream.readUTF();
                    System.out.println(messageFromClient);
                    String[] splitStr = messageFromClient.split("\\|");
                    String testCaseLaunchedFrom = splitStr[0];
                    String portNumber = splitStr[1];
                    String testName = splitStr[2];
                    String testDescription = splitStr[3];
                    String role = splitStr[4];
                    String nfdAddress = splitStr[5];

                    count++;
                    System.out.println(count + " " + messageFromClient);

                    // send reference address to main activity
                    new SocketHandler().setSocket(socket);

                    //socket.getInetAddress().toString(), socket.getPort()
                    sendMessageToActivity(testCaseLaunchedFrom, portNumber, testName, testDescription, role, nfdAddress);

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessageToActivity(String pTestCaseLaunchedFrom, String pPortNumber, String pTestName, String pTestDescription, String pRole, String pNfdAddress) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ServiceToActivityAction");
        broadcastIntent.putExtra(TEST_CASE_LAUNCHED_FROM, pTestCaseLaunchedFrom);
        broadcastIntent.putExtra(PORT_NUMBER, pPortNumber);
        broadcastIntent.putExtra(TEST_CASE_NAME, pTestName);
        broadcastIntent.putExtra(TEST_CASE_DESCRIPTION, pTestDescription);
        broadcastIntent.putExtra(ROLE, pRole);
        broadcastIntent.putExtra(NFD_ADDRESS, pNfdAddress);
        sendBroadcast(broadcastIntent);
    }

}