package com.example.healthandwellnessapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GuidelinesAdapter extends RecyclerView.Adapter<GuidelinesAdapter.GuidelineViewHolder> {

    private List<String> guidelines;

    public GuidelinesAdapter(List<String> guidelines) {
        this.guidelines = guidelines;
    }

    @NonNull
    @Override
    public GuidelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guideline_item, parent, false);
        return new GuidelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuidelineViewHolder holder, int position) {
        String guideline = guidelines.get(position);
        holder.guidelineText.setText(guideline);
    }

    @Override
    public int getItemCount() {
        return guidelines.size();
    }

    static class GuidelineViewHolder extends RecyclerView.ViewHolder {
        TextView guidelineText;

        public GuidelineViewHolder(@NonNull View itemView) {
            super(itemView);
            guidelineText = itemView.findViewById(R.id.guideline_text);
        }
    }
}
