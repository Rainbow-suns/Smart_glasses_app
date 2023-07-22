package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        db = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button registerButton = findViewById(R.id.register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (db.insertData(username, password)) {
                    Toast.makeText(RegistrationActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegistrationActivity.this, "User Registration Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
