package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttException;

public class VoiceCallActivity extends AppCompatActivity {

    private AudioPlayer audioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        Button buttonMicrophone = findViewById(R.id.buttonMicrophone);
        buttonMicrophone.setText("Turn on the microphone");
        buttonMicrophone.setVisibility(View.INVISIBLE);
        Button buttonListener = findViewById(R.id.buttonListener);

        buttonListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (audioPlayer == null) {
                        audioPlayer = new AudioPlayer(getApplicationContext());
                        buttonMicrophone.setVisibility(View.VISIBLE);
                        buttonListener.setText("Stop");
                    } else {
                        audioPlayer.stop();
                        audioPlayer = null;
                        buttonMicrophone.setVisibility(View.INVISIBLE);
                        buttonListener.setText("Start");
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

//        button.setOnClickListener(v -> {
//            try {
//                if (audioPlayer == null) {
//                    audioPlayer = new AudioPlayer(getApplicationContext());
//                    button.setText("Stop");
//                } else {
//                    audioPlayer.stop();
//                    audioPlayer = null;
//                    button.setText("Start");
//                }
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//        });

        buttonMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonMicrophone.getText().equals("Turn on the microphone")) {
                    audioPlayer.startMicrophone();
                    buttonMicrophone.setText("Turn off the microphone");
                } else {
                    audioPlayer.stopMicrophone();
                    buttonMicrophone.setText("Turn on the microphone");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.stopMicrophone();
            audioPlayer = null;
        }
    }
}
