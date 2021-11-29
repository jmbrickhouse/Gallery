package com.example.gallery;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

//manages the image thumbnail interface, overrides the view holder to hold views associated with
//image thumbnails as well as text fields for dates
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>
{
    private final MainActivity mainActivity;
    private final List<fileData> data;

    public GalleryAdapter(MainActivity mainActivity, List<fileData> data)
    {
        this.mainActivity = mainActivity;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        int pos = holder.getAdapterPosition() == RecyclerView.NO_POSITION ? position : holder.getAdapterPosition();
        String date = data.get(pos).getDate();
        int truncateIndex = pos - (pos % MainActivity.grid) - 1;

        //display the date text if a new day is encountered on the image
        if(pos == 0 || date.compareTo(data.get(pos-1).getDate()) != 0)
        {
            holder.title.setText(date);
            holder.title.setVisibility(View.VISIBLE);
        }
        //display the rest of the text views as blank for the row that displays the date text
        else if(pos < MainActivity.grid ||
                ((pos % MainActivity.grid != 0 && date.compareTo(data.get(truncateIndex).getDate()) != 0)))
        {
            holder.title.clearComposingText();
            holder.title.setVisibility(View.VISIBLE);
        }

        String path = data.get(pos).getPath();
        holder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //if actual image path, not blank placeholder
        if(path.compareTo("") != 0)
        {
            Drawable image = Drawable.createFromPath(path);
            holder.img.setImageDrawable(image);

            //open full screen when image is clicked
            holder.img.setOnClickListener(v -> {

                //hide action bar, status bar, and recycler view
                Objects.requireNonNull(mainActivity.getSupportActionBar()).hide();
                mainActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                mainActivity.findViewById(R.id.recyclerView).setVisibility(View.GONE);

                //set the fullscreen image view to the source and make it visible
                mainActivity.findViewById(R.id.fullscreen).setVisibility(View.VISIBLE);
                ImageView fullscreen = mainActivity.findViewById(R.id.fullscreenImage);
                fullscreen.setImageDrawable(image);

                //display or hide text and status bar when screen is clicked while in fullscreen view
                fullscreen.setOnClickListener(v1 -> {

                    TextView text = mainActivity.findViewById(R.id.fullscreenText);
                    text.setText(path);

                    if(text.getVisibility() != View.VISIBLE)
                    {
                        text.setVisibility(View.VISIBLE);
                        mainActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                    else
                    {
                        mainActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                        text.setVisibility(View.GONE);
                    }
                });
            });
        }
        else
            //set a blank place holder image for trailing grid space
            holder.img.setImageDrawable(holder.itemView.getBackground());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    //view holder contains an image view and a textview
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView title;
        private final ImageView img;

        public ViewHolder(View view)
        {
            super(view);
            title = view.findViewById(R.id.title);
            img = view.findViewById(R.id.img);
        }
    }
}
