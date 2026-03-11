package com.example.Hangman;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Context;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.TreeSet;

public class WordsListActivity extends AppCompatActivity {

    EditText editWords;
    Button btnClear, btnSave, btnShow, btnBack, btnClearList;
    ListView lvWords;

    private static final String FILE_NAME = "words.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        editWords = findViewById(R.id.editWords);
        btnClear = findViewById(R.id.btnClearWords);
        btnSave = findViewById(R.id.btnSaveWords);
        btnShow = findViewById(R.id.btnShowList);
        btnBack = findViewById(R.id.btnBackWords);
        lvWords = findViewById(R.id.lvWords);
        btnClearList = findViewById(R.id.btnClearList);

        btnClear.setOnClickListener(v -> editWords.setText(""));

        btnSave.setOnClickListener(v -> {
            String text = editWords.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(this, "Enter words to save", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> words = loadWordsArray(this);
            String[] newWords = text.split(",");

            int added = 0;
            for (String w : newWords) {
                w = w.trim().toUpperCase(); // <-- convert to uppercase

                // Reject words containing digits
                if (!w.matches("[A-Z]+")) {
                    Toast.makeText(this, "Invalid word: \"" + w + "\" (letters only)", Toast.LENGTH_SHORT).show();
                    continue;
                }

                if (!w.isEmpty() && !words.contains(w)) {
                    words.add(w);
                    added++;
                }
            }

            saveWordsArray(this, words);
            Toast.makeText(this, added + " new words saved.", Toast.LENGTH_SHORT).show();
            editWords.setText("");
        });

        btnShow.setOnClickListener(v -> {
            ArrayList<String> words = loadWordsArray(this);

            // Remove duplicates ignoring case and sort alphabetically
            TreeSet<String> uniqueWords = new TreeSet<>();
            for (String w : words) uniqueWords.add(w.toLowerCase());

            ArrayList<String> displayList = new ArrayList<>(uniqueWords);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    R.layout.list_item_black, // your custom layout
                    displayList
            );

            lvWords.setAdapter(adapter);

        });

        btnClearList.setOnClickListener(v -> {
            saveWordsArray(this, new ArrayList<>());
            lvWords.setAdapter(null);
            Toast.makeText(this, "All words cleared.", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    public static void saveWordsArray(Context ctx, ArrayList<String> list) {
        try {
            FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (String w : list) {
                bw.write(w.toUpperCase());
                bw.newLine();
            }

            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> loadWordsArray(Context ctx) {
        ArrayList<String> list = new ArrayList<>();

        try {
            FileInputStream fis = ctx.openFileInput(FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (!list.contains(line))
                    list.add(line);
            }

            br.close();
        } catch (Exception ignored) {}

        return list;
    }
}
