package com.gsnathan.pdfviewer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SavedLocation.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE = null;
    private static String DATABASE_NAME = "app-db.db";

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        String location = context.getCacheDir().getAbsolutePath() + "/" + DATABASE_NAME;
        INSTANCE = Room.databaseBuilder(context, AppDatabase.class, location).build();
        return INSTANCE;
    }

    public abstract SavedLocationDao savedLocationDao();
}
