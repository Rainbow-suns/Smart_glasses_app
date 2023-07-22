package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttException;

public class VideoCallActivity extends AppCompatActivity {

    private WebView webView;
    private AudioPlayer audioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        Button buttonMicrophone = findViewById(R.id.buttonMicrophone1);
        buttonMicrophone.setText("Turn on the microphone");
        buttonMicrophone.setVisibility(View.INVISIBLE);
        Button buttonListener = findViewById(R.id.buttonListener1);


        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webView.loadUrl("http://172.20.10.5");

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
    protected void onPause() {
        super.onPause();

        webView.stopLoading();
        webView.clearHistory();
        webView.clearCache(true);
    }
}