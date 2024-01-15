package com.example.maintrojan;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;


public class NoteActivity extends AppCompatActivity {
    private static final String TAG = "myTag";
    private boolean speechRecog = false;
    SpeechRecognizer speechRecognizer;
    ImageButton voice;
    int textPos = 0;
    int lastAddedPos = 0;
    int notePos = -1;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        voice = (ImageButton) findViewById(R.id.voice);
        Button done = (Button) findViewById(R.id.done);
        TextView heading = (TextView) findViewById(R.id.heading);
        EditText note = (EditText) findViewById(R.id.note);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if(type.compareTo("view") == 0){
            notePos = intent.getIntExtra("pos",-1);
            String fileName = intent.getStringExtra("fileName");
            File file = new File(getApplicationContext().getFilesDir(),fileName+".txt");

            byte[] bytes = new byte[(int)file.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(bytes);
                fis.close();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            String txt = new String(bytes);
            note.setText(txt);
            heading.setText(fileName);
        }


        // speech recognitions
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d(TAG, "onReadyForSpeech: ");
            }

            @Override
            public void onBeginningOfSpeech() {
                note.setText(note.getText()+" ");
                textPos = note.length();
                lastAddedPos = textPos;
                note.setSelection(textPos);
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {


            }

            @Override
            public void onEndOfSpeech() {
                speechRecog = false;
                cngIcon();
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                int dLen = data.get(0).length();
//                Log.d(TAG, String.valueOf(textPos));
                note.getText().replace(Math.min(textPos,lastAddedPos),Math.max(textPos,lastAddedPos),data.get(0),0,dLen);
            }


            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                int dLen = data.get(0).length();
//                Log.d(TAG, String.valueOf(textPos));
                note.getText().replace(Math.min(textPos,lastAddedPos),Math.max(textPos,lastAddedPos),data.get(0),0,dLen);
                lastAddedPos = textPos+dLen;
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(speechRecog){
                    speechRecog = false;
                    speechRecognizer.stopListening();
                }else{
                    speechRecog = true;
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                cngIcon();
            }
        });



        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String headTxt = heading.getText().toString();
                String noteTxt = note.getText().toString();
                if(noteTxt.isEmpty() || headTxt.isEmpty()){
                    Toast.makeText(NoteActivity.this, "Enter both Heading and Text", Toast.LENGTH_SHORT).show();
                }else{
                    // saving the note Data
                    File file = new File(getApplicationContext().getFilesDir(),headTxt+".txt");
                    try {
                        FileOutputStream stream = new FileOutputStream(file);
                        stream.write(noteTxt.getBytes(StandardCharsets.UTF_8));
                        stream.close();

                        // saving data to shared preferences
                        SharedPreferences sp = getSharedPreferences("shared",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();

                        String date = "";
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
                            LocalDateTime now = LocalDateTime.now();
                            date = dtf.format(now);
                            Log.d(TAG, date);
                        }

                        try {
                            String data = sp.getString("data",null);
                            JSONArray jArr;
                            if(data != null){
                                jArr = new JSONArray(data);
                            }else{
                                jArr = new JSONArray();
                            }
                            if(notePos != -1){
                                jArr.remove(notePos);
                            }
                            jArr.put(headTxt+"<=>"+date);
                            editor.putString("data",jArr.toString(0));
                            editor.putBoolean("updated",true);
                            editor.putString("nextVal",headTxt+"<=>"+date);
                            editor.apply();

                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());;
                        }
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    finish();
                }
            }
        });




    }
    public void cngIcon(){
        if(speechRecog){
            voice.setImageResource(R.drawable.green_mic);
        }else{
            voice.setImageResource(R.drawable.red_mic);
        }
    }
}