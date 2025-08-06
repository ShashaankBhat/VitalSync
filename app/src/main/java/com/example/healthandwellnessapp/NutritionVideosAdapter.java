package com.example.healthandwellnessapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class NutritionVideosAdapter extends RecyclerView.Adapter<NutritionVideosAdapter.VideoViewHolder> {

    private final List<Video> videoList;
    private final OnVideoClickListener listener;

    public NutritionVideosAdapter(List<Video> videoList, OnVideoClickListener listener) {
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videoList.get(position);

        // Set the title, ensuring it's not null
        holder.titleTextView.setText(video.getTitle() != null ? video.getTitle() : "No Title");

        // Clear previous tags
        holder.tagsContainer.removeAllViews();

        // Add tags dynamically
        if (video.getTags() != null) {
            for (String tag : video.getTags()) {
                TextView tagView = new TextView(holder.itemView.getContext());
                tagView.setText(tag);
                tagView.setTextSize(14);
                tagView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
                tagView.setBackgroundResource(R.drawable.tag_background); // Reference your background drawable
                tagView.setPadding(25, 20, 25, 20);
                tagView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                // Optionally add margins
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tagView.getLayoutParams();
                params.setMargins(0, 2, 7, 2);
                tagView.setLayoutParams(params);

                Typeface customFont = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.fira_bold); // replace with your font file name
                tagView.setTypeface(customFont);


                holder.tagsContainer.addView(tagView);
            }
        }

        // Load the image using Glide
        Glide.with(holder.itemView.getContext())
                .load(video.getImage())
                .placeholder(R.drawable.placeholder) // Optional: Set a placeholder image
                .into(holder.imageView);

        // Set the click listener directly in onBindViewHolder
        holder.itemView.setOnClickListener(v -> {
            if (video.getYoutubeUrl() != null) {
                listener.onVideoClick(video.getYoutubeUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        LinearLayout tagsContainer; // Declare the LinearLayout for tags

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.video_image);
            titleTextView = itemView.findViewById(R.id.video_title);
            tagsContainer = itemView.findViewById(R.id.video_tags_container); // Reference the LinearLayout by ID
        }
    }

    public interface OnVideoClickListener {
        void onVideoClick(String youtubeUrl);
    }
}
