package com.example.Hangman;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class LeaderboardStorage {

    private static final String PREF = "leaderboard_prefs";
    private static final String KEY = "leaderboard_data";
    private static final int MAX_ENTRIES = 20; // optional top N scores

    // Class to hold player score
    public static class PlayerScore {
        public String name;
        public int score;

        public PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", name);
                obj.put("score", score);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        public static PlayerScore fromJSON(JSONObject obj) {
            try {
                return new PlayerScore(obj.getString("name"), obj.getInt("score"));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /** Save a new score */
    public static void saveScore(Context context, String playerName, int score) {
        ArrayList<PlayerScore> list = loadScores(context);

        // Use a map to keep only highest score per player
        HashMap<String, PlayerScore> map = new HashMap<>();
        for (PlayerScore ps : list) {
            map.put(ps.name, ps);
        }

        if (!map.containsKey(playerName) || score > map.get(playerName).score) {
            map.put(playerName, new PlayerScore(playerName, score));
        }

        // Convert back to list
        list = new ArrayList<>(map.values());

        // Sort descending
        Collections.sort(list, (a, b) -> Integer.compare(b.score, a.score));

        // Optional: keep top N
        if (list.size() > MAX_ENTRIES) list = new ArrayList<>(list.subList(0, MAX_ENTRIES));

        saveScores(context, list);
    }

    /** Load leaderboard as list of PlayerScore objects */
    public static ArrayList<PlayerScore> loadScores(Context context) {
        ArrayList<PlayerScore> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String data = prefs.getString(KEY, "");
        if (!data.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(data);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    PlayerScore ps = PlayerScore.fromJSON(obj);
                    if (ps != null) list.add(ps);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /** Save the entire list to SharedPreferences */
    private static void saveScores(Context context, ArrayList<PlayerScore> list) {
        JSONArray arr = new JSONArray();
        for (PlayerScore ps : list) arr.put(ps.toJSON());

        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY, arr.toString()).apply();
    }

    /** Clear leaderboard */
    public static void clearLeaderboard(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY).apply();
    }
}
