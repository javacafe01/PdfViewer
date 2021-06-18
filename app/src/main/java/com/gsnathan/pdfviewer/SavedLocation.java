package com.gsnathan.pdfviewer;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class SavedLocation {
    public SavedLocation(@NotNull String location, int pageNumber) {
        this.location = location;
        this.pageNumber = pageNumber;
    }

    @PrimaryKey
    @NonNull
    public String location;

    @ColumnInfo(name = "pageNumber")
    public int pageNumber;
}
