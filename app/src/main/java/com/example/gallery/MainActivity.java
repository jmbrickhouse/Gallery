package com.example.gallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static final int REQ_PERMISSIONS  = 66;

    private static final String[] PERMISSIONS =
    {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

   @SuppressLint("NewApi")
   private boolean arePermissionsDenied()
   {
       for (String permission : PERMISSIONS)
           if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
               return true;

       return false;
   }

   @SuppressLint("NewApi")
   @Override
   public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                          @NonNull final int[] grantResults)
   {
        //if permissions denied clearing app user data will allow to continuously prompt permissions
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_PERMISSIONS && grantResults.length > 0)
        {
            if(arePermissionsDenied())
            {
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE)))
                        .clearApplicationUserData();
                recreate();
            }
            else
                onResume();
        }
   }

    //add all supported file types in a directory to our image file list
    private void addImages(final File[] files, List<fileData> fileList)
    {
        for(File file : Objects.requireNonNull(files))
        {
            String path = file.getAbsolutePath();
            if(path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".jfif")
                    || path.endsWith(".pjpeg") || path.endsWith(".pjp") || path.endsWith(".svg")
                    || path.endsWith(".png") || path.endsWith(".webp"))
            {
                //subtract 5 hours for GMT-5 (EST)
                fileList.add(new fileData(path,
                        file.lastModified() - 5*60*60*1000));
            }
        }
    }

    //used to initialize the app in onResume only once when its called,
    //otherwise we always check permissions in onResume before initializing
    private boolean initialized;
    //grid row size for gallery grid layout
    public static final int grid = 3;

    @SuppressLint("NewApi")
    @Override
    protected void onResume()
    {
        super.onResume();
        //check if permissions needed
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied())
        {
            requestPermissions(PERMISSIONS, REQ_PERMISSIONS);
            return;
        }
        //initialize app
        if(!initialized)
        {
            List<fileData> fileList = new ArrayList<>();
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File[] rootFiles = new File(rootDir).listFiles();

            Queue<File[]> queue = new LinkedList<>();

            //BFS the file system for image files
            if(rootFiles != null)
            {
                addImages(rootFiles, fileList);
                queue.add(rootFiles);
            }
            while(!queue.isEmpty())
            {
                for(File file : Objects.requireNonNull(queue.poll()))
                {
                    if(file.isDirectory())
                    {
                        File[] files = new File(file.getAbsolutePath()).listFiles();
                        if(files == null) continue;
                        addImages(files, fileList);
                        queue.add(files);
                    }
                }
            }

            //sort fileList by most recently modified
            Collections.sort(fileList, (o1, o2) -> -Long.compare(o1.getTime(), o2.getTime()));

            for (int i = 1; i < fileList.size(); i++)
            {
                //if previous date doesn't match the current
                if(fileList.get(i).getDate().compareTo(fileList.get(i-1).getDate()) != 0)
                {
                    long prevTime = fileList.get(i-1).getTime();
                    //add empty placeholder for white space in image gallery if at a trailing
                    //grid position
                    while(i % grid != 0)
                    {
                        fileList.add(i, new fileData("", prevTime));
                        ++i;
                    }
                }
            }

            final RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            //use a grid layout for the thumbnail view, will have grid number of columns
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), grid);
            recyclerView.setLayoutManager(layoutManager);

            GalleryAdapter galleryAdapter = new GalleryAdapter(this, fileList);
            recyclerView.setAdapter(galleryAdapter);

            initialized = true;
        }
    }

    @Override
    public void onBackPressed()
    {
        //return to the gallery menu if in image fullscreen
        RelativeLayout fullscreen = findViewById(R.id.fullscreen);
        if(fullscreen.getVisibility() == View.VISIBLE)
        {
            fullscreen.setVisibility(View.GONE);
            findViewById(R.id.fullscreenText).setVisibility(View.GONE);
            findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            Objects.requireNonNull(this.getSupportActionBar()).show();
        }
        else
            super.onBackPressed();
    }

}

