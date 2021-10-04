package com.gsnathan.pdfviewer;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class SavedLocation {
    public SavedLocation(@NotNull String hash, int pageNumber) {
        this.hash = hash;
        this.pageNumber = pageNumber;
    }

    @PrimaryKey
    @NonNull
    public String hash;

    @ColumnInfo(name = "pageNumber")
    public int pageNumber;
}
