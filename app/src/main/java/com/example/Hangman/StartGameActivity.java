package com.example.Hangman;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class StartGameActivity extends AppCompatActivity {

    Button btnBack, btnGuess;
    HashSet<Character> guessedLetters = new HashSet<>();

    TextView tvTimer, tvScore, tvWord, tvGameOver;
    EditText editLetter;
    ImageView imageHangman;

    GameState game;
    CountDownTimer timer;
    ArrayList<String> wordList;
    int currentWordIndex = 0;
    HashSet<String> usedWords = new HashSet<>();

    // GAME END
    private void endGame(String message) {
        if (timer != null) timer.cancel();

        // Reveal the word when the game is lost or time is up
        if (game.isLost() || message.contains("TIME UP")) {
            Toast.makeText(
                    this,
                    "The word was: " + game.getFullWord(),
                    Toast.LENGTH_LONG
            ).show();
        }

        tvGameOver.setText(message);
        tvGameOver.setVisibility(View.VISIBLE);

        saveScoreToLeaderboard();

        GameStorage.clear(this);
        GameStorage.clearCurrentWordIndex(this);

        btnGuess.setEnabled(false);
        editLetter.setEnabled(false);
    }

    // TIMER
    private void startTimer() {
        timer = new CountDownTimer(60000, 1000) {
            public void onTick(long ms) {
                tvTimer.setText("Time: " + (ms / 1000) + "s");
            }
            public void onFinish() {
                endGame("TIME UP!");
            }
        };
        timer.start();
    }

    private void startTimerWithBonus(long bonusMillis) {
        long currentMillis = 0;
        try {
            String t = tvTimer.getText().toString()
                    .replace("Time: ", "").replace("s", "");
            currentMillis = Long.parseLong(t) * 1000;
        } catch (Exception ignored) {}

        long total = currentMillis + bonusMillis;

        timer = new CountDownTimer(total, 1000) {
            public void onTick(long ms) {
                tvTimer.setText("Time: " + (ms / 1000) + "s");
            }
            public void onFinish() {
                endGame("TIME UP!");
            }
        };
        timer.start();
    }

    // WORD GENERATION
    private String getNextWord() {
        if (usedWords.size() >= wordList.size()) {
            // --- Calculate time left ---
            long timeLeftSeconds = 0;
            try {
                String t = tvTimer.getText().toString()
                        .replace("Time: ", "")
                        .replace("s", "");
                timeLeftSeconds = Long.parseLong(t);
            } catch (Exception ignored) {}

            // --- Add bonus score ---
            int bonusScore = (int) (timeLeftSeconds * 100);
            game.setScore(game.getScore() + bonusScore);

            // Save leaderboard
            saveScoreToLeaderboard();

            // End game message including bonus
            endGame("YOU WIN! All words completed!\nBonus: +" + bonusScore);

            return null;
        }

        String nextWord;
        Random r = new Random();

        do {
            nextWord = wordList.get(r.nextInt(wordList.size()));
        } while (usedWords.contains(nextWord));

        usedWords.add(nextWord);
        game.setUsedWords(usedWords);

        return nextWord;
    }

    // ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        btnBack = findViewById(R.id.btnBackHome);
        btnGuess = findViewById(R.id.btnGuess);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvWord = findViewById(R.id.tvWord);
        editLetter = findViewById(R.id.editLetter);
        imageHangman = findViewById(R.id.imageHangman);
        tvGameOver = findViewById(R.id.tvGameOver);

        // LOAD WORDS
        wordList = WordsListActivity.loadWordsArray(this);
        if (wordList.size() < 5) {
            wordList.add("ANDROID");
            wordList.add("STUDIO");
            wordList.add("WIRELESS");
            wordList.add("MOBILE");
            wordList.add("TECHNOLOGY");
        }

        // LOAD GAME OR CREATE NEW
        game = GameStorage.load(this);

        if (game != null) {
            usedWords = game.getUsedWords();
            currentWordIndex = GameStorage.loadCurrentWordIndex(this);
        } else {
            currentWordIndex = new Random().nextInt(wordList.size());
            String randomWord = wordList.get(currentWordIndex);

            game = new GameState(
                    getIntent().getStringExtra("username"),
                    randomWord
            );
            usedWords = game.getUsedWords();

            GameStorage.save(this, game);
            GameStorage.saveCurrentWordIndex(this, currentWordIndex);
        }

        updateUI();
        updateHangmanImage();
        startTimer();

        btnBack.setOnClickListener(v -> finish());
        btnGuess.setOnClickListener(v -> handleGuess());
    }

    // HANDLE GUESS
    private void handleGuess() {
        String input = editLetter.getText().toString().toUpperCase();

        if (input.length() != 1) {
            Toast.makeText(this, "Enter one letter", Toast.LENGTH_SHORT).show();
            return;
        }

        char guess = input.charAt(0);

        // Reject numbers and non-letters
        if (!Character.isLetter(guess)) {
            Toast.makeText(this, "Only letters are allowed!", Toast.LENGTH_SHORT).show();
            editLetter.setText("");
            return;
        }

        if (guessedLetters.contains(guess)) {
            Toast.makeText(this, "Letter already played!", Toast.LENGTH_SHORT).show();
            editLetter.setText("");
            return;
        }

        guessedLetters.add(guess);

        boolean correct = game.applyGuess(guess);

        if (!correct) {
            updateHangmanImage();
        }

        updateUI();
        GameStorage.save(this, game);
        GameStorage.saveCurrentWordIndex(this, currentWordIndex);

        // WORD COMPLETE
        if (game.isWon()) {

            // Calculate bonus based on the solved word
            long bonus = getBonusForWord(game.getFullWord());

            String nextWord = getNextWord();

            if (nextWord != null) {

                game.resetForNewWord(nextWord);
                guessedLetters.clear();

                updateUI();
                updateHangmanImage();

                if (timer != null) timer.cancel();
                startTimerWithBonus(bonus);

                Toast.makeText(this, "Next word! +" + (bonus/1000) + "s", Toast.LENGTH_SHORT).show();
                GameStorage.save(this, game);
            }
        }

        if (game.isLost()) endGame("GAME OVER");

        editLetter.setText("");
    }

    // UI UPDATE
    private void updateUI() {
        tvScore.setText("Score: " + game.getScore());

        StringBuilder sb = new StringBuilder();
        for (char c : game.getDisplayChars()) {
            sb.append(c).append(" ");
        }
        tvWord.setText(sb.toString());
    }

    private void updateHangmanImage() {
        int wrong = game.getWrongGuesses();
        int resId = getResources().getIdentifier("hangman_" + wrong,
                "drawable", getPackageName());

        if (resId != 0) imageHangman.setImageResource(resId);
    }

    private void saveScoreToLeaderboard() {
        LeaderboardStorage.saveScore(
                this,
                game.getUsername(),
                game.getScore()
        );
    }

    //TIME BONUS
    private long getBonusForWord(String word) {
        int len = word.length();

        if (len >= 12) return 30000;   // long word = +30 sec
        if (len >= 8)  return 15000;   // medium word = +15 sec
        return 10000;                  // short word = +10 sec
    }
}
