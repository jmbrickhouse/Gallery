package com.example.gallery;

import android.annotation.SuppressLint;

import java.nio.file.attribute.FileTime;

public final class fileData {
    private final String path;
    private final String date;
    private final long time;

    @SuppressLint("NewApi")
    public fileData(String path, long time) {
        this.path = path;
        this.time = time;
        date = FileTime.fromMillis(time)
                .toString().split("T")[0];
    }

    public String getPath() {
        return path;
    }

    public String getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

}
