package com.example.Hangman;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameStorage {
    private static final String FILE_INDEX = "wordindex.txt";

    public static void saveCurrentWordIndex(Context ctx, int index) {
        try {
            FileOutputStream fos = ctx.openFileOutput(FILE_INDEX, Context.MODE_PRIVATE);
            fos.write(String.valueOf(index).getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int loadCurrentWordIndex(Context ctx) {
        try {
            FileInputStream fis = ctx.openFileInput(FILE_INDEX);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            int index = Integer.parseInt(reader.readLine());
            reader.close();
            return index;
        } catch (Exception e) {
            return 0; // default index if file not found
        }
    }

    public static void clearCurrentWordIndex(Context ctx) {
        ctx.deleteFile(FILE_INDEX);
    }


    private static final String FILE_NAME = "gamestate.json";

    // Save GameState as object
    public static void save(Context ctx, GameState state) {
        try (FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load GameState as object
    public static GameState load(Context ctx) {
        try (FileInputStream fis = ctx.openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (GameState) ois.readObject();
        } catch (Exception e) {
            return null; // no save file yet
        }
    }

    /** Delete save file */
    public static void clear(Context ctx) {
        ctx.deleteFile(FILE_NAME);
    }
}
