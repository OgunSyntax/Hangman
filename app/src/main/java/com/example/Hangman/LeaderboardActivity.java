package com.example.Hangman;

import android.Manifest;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {

    TableLayout tableLeaderboard;
    Button btnBackLB, btnClearLB, btnSendSMS;
    EditText editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        tableLeaderboard = findViewById(R.id.tableLeaderboard);
        btnBackLB = findViewById(R.id.btnBackLB);
        btnClearLB = findViewById(R.id.btnClearLB);
        btnSendSMS = findViewById(R.id.btnSendSMS);
        editPhone = findViewById(R.id.editPhone);

        // Rq SMS permission
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.SEND_SMS}, 1
        );

        // Table header
        addHeaderRow();

        // Load data from file
        ArrayList<LeaderboardStorage.PlayerScore> data = LeaderboardStorage.loadScores(this);

        int limit = Math.min(data.size(), 10);

        for (int i = 0; i < limit; i++) {
            LeaderboardStorage.PlayerScore ps = data.get(i);

            TableRow row = new TableRow(this);

            TextView tvRank = new TextView(this);
            tvRank.setText(String.valueOf(i + 1));
            tvRank.setPadding(8,8,8,8);

            TextView tvPlayer = new TextView(this);
            tvPlayer.setText(ps.name);
            tvPlayer.setPadding(8,8,8,8);

            TextView tvScore = new TextView(this);
            tvScore.setText(String.valueOf(ps.score));
            tvScore.setPadding(8,8,8,8);

            row.addView(tvRank);
            row.addView(tvPlayer);
            row.addView(tvScore);

            tableLeaderboard.addView(row);
        }

        btnSendSMS.setOnClickListener(v -> sendLeaderboardSMS());
        btnBackLB.setOnClickListener(v -> finish());

        btnClearLB.setOnClickListener(v -> {
            LeaderboardStorage.clearLeaderboard(this);
            tableLeaderboard.removeAllViews();
            addHeaderRow();
            Toast.makeText(this, "Leaderboard cleared.", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendLeaderboardSMS() {

        String phone = editPhone.getText().toString().trim();
        phone = phone.replaceAll("[^+0-9]", ""); //ignore non-digits

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build leaderboard message
        StringBuilder sb = new StringBuilder();
        sb.append("Hangman Leaderboard: \n");

        int rows = tableLeaderboard.getChildCount();
        for (int i = 1; i < rows; i++) {
            TableRow tr = (TableRow) tableLeaderboard.getChildAt(i);

            TextView rank = (TextView) tr.getChildAt(0);
            TextView player = (TextView) tr.getChildAt(1);
            TextView score = (TextView) tr.getChildAt(2);
            // Message formatting
            sb.append(rank.getText())
                    .append(". ")
                    .append(player.getText())
                    .append(" - ")
                    .append(score.getText())
                    .append("\n");

        }

        String message = sb.toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);

            Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "SMS failed" + message, Toast.LENGTH_LONG).show();
        }
    }

    private void addHeaderRow() {
        TableRow header = new TableRow(this);

        TextView tvRank = new TextView(this);
        tvRank.setText("Rank");
        tvRank.setPadding(8,8,8,8);

        TextView tvPlayer = new TextView(this);
        tvPlayer.setText("Player");
        tvPlayer.setPadding(8,8,8,8);

        TextView tvScore = new TextView(this);
        tvScore.setText("Score");
        tvScore.setPadding(8,8,8,8);

        header.addView(tvRank);
        header.addView(tvPlayer);
        header.addView(tvScore);

        tableLeaderboard.addView(header);
    }
}
