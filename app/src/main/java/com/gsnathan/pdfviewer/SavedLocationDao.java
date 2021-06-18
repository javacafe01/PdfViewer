package com.gsnathan.pdfviewer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SavedLocationDao {
    @Query("SELECT pageNumber FROM SavedLocation WHERE location = :location")
    Integer findSavedPage(String location);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SavedLocation saveLocations);
}
