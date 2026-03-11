package com.example.Hangman;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.HashSet;

public class GameState implements Serializable {

    private String username;
    private String fullWord;
    private char[] displayChars;
    private int wrongGuesses;
    private int score;
    private HashSet<String> usedWords = new HashSet<>();

    public GameState(String username, String fullWord) {
        this.username = username;
        this.fullWord = fullWord;
        this.displayChars = initMask(fullWord);
        this.wrongGuesses = 0;
        this.score = 0;
    }

    /** Restore existing game */
    public GameState(String username, String fullWord, char[] displayChars, int wrongGuesses, int score) {
        this.username = username;
        this.fullWord = fullWord;
        this.displayChars = initMask(fullWord); // ensure correct length
        this.wrongGuesses = wrongGuesses;
        this.score = score;
    }


    private char[] initMask(String word) {
        char[] arr = new char[word.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (word.charAt(i) == ' ') ? ' ' : '_';
        }
        return arr;
    }

    public HashSet<String> getUsedWords() {
        return usedWords;
    }

    public void setUsedWords(HashSet<String> usedWords) {
        this.usedWords = usedWords;
    }

    public void resetForNewWord(String newWord) {
        this.fullWord = newWord;
        this.displayChars = initMask(newWord);
        this.wrongGuesses = 0;
    }

    public boolean applyGuess(char letter) {
        boolean found = false;
        for (int i = 0; i < fullWord.length(); i++) {
            if (fullWord.charAt(i) == letter) {
                displayChars[i] = letter;
                found = true;
            }
        }
        if (found) score += 10;
        else wrongGuesses++;

        return found;
    }

    public boolean isWon() {
        for (char c : displayChars) if (c == '_') return false;
        return true;
    }

    public boolean isLost() {
        return wrongGuesses >= 6;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("fullWord", fullWord);
        obj.put("wrongGuesses", wrongGuesses);
        obj.put("score", score);

        JSONArray arr = new JSONArray();
        for (char c : displayChars) arr.put(String.valueOf(c));
        obj.put("displayChars", arr);

        JSONArray usedArr = new JSONArray();
        for (String w : usedWords) usedArr.put(w);
        obj.put("usedWords", usedArr);

        return obj;
    }

    public static GameState fromJSON(JSONObject obj) throws JSONException {
        String username = obj.getString("username");
        String fullWord = obj.getString("fullWord");
        int wrongGuesses = obj.getInt("wrongGuesses");
        int score = obj.getInt("score");

        JSONArray arr = obj.getJSONArray("displayChars");
        char[] displayChars = new char[arr.length()];
        for (int i = 0; i < arr.length(); i++)
            displayChars[i] = arr.getString(i).charAt(0);

        JSONArray usedArr = obj.optJSONArray("usedWords");
        HashSet<String> used = new HashSet<>();
        if (usedArr != null) {
            for (int i = 0; i < usedArr.length(); i++)
                used.add(usedArr.getString(i));
        }
        GameState state = new GameState(username, fullWord, displayChars, wrongGuesses, score);
        state.setUsedWords(used);
        return state;
    }


    public char[] getDisplayChars() { return displayChars; }
    public int getWrongGuesses() { return wrongGuesses; }
    public int getScore() { return score; }

    public String getFullWord() { return fullWord; }
    public String getUsername() { return username; }

    public void setScore(int s) { this.score = s; }
}
