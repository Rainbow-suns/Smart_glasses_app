package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        Button voiceCallButton = findViewById(R.id.voice_call);
        Button videoCallButton = findViewById(R.id.video_call);

        voiceCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to VoiceCallActivity
                Intent intent = new Intent(NextActivity.this, VoiceCallActivity.class);
                startActivity(intent);
            }
        });

        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to VideoCallActivity
                Intent intent = new Intent(NextActivity.this, VideoCallActivity.class);
                startActivity(intent);
            }
        });
    }
}
