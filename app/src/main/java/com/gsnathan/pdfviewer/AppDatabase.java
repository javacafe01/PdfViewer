package com.gsnathan.pdfviewer;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SavedLocation.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SavedLocationDao savedLocationDao();
}
