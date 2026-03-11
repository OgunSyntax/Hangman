package com.example.Hangman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editUsername;
    Button btnStart, btnLeaderboard, btnWordsList, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editUsername = findViewById(R.id.editUsername);
        btnStart = findViewById(R.id.btnStartGame);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnWordsList = findViewById(R.id.btnWordsList);
        btnExit = findViewById(R.id.btnExit);

        btnStart.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a username before starting the game.", Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(MainActivity.this, StartGameActivity.class);
                i.putExtra("username", username);
                startActivity(i);
            }
        });

        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class))
        );

        btnWordsList.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WordsListActivity.class))
        );

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
