package com.progix.fridgex.light.adapter.recipe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.progix.fridgex.light.R;

import java.util.ArrayList;


public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    Context context;
    ArrayList<Pair<String, String>> ingredientsList;
    ArrayList<Boolean> missing;

    public IngredientsAdapter(Context context, ArrayList<Pair<String, String>> ingredientsList, ArrayList<Boolean> missing) {
        this.context = context;
        this.ingredientsList = ingredientsList;
        this.missing = missing;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingreds, parent, false)
        );
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String str = ingredientsList.get(position).first;
        holder.prodName.setText(str.substring(0, 1).toUpperCase() + str.substring(1));
        holder.amount.setText(ingredientsList.get(position).second);
        int color;
        if (!missing.get(position)) {
            color = ContextCompat.getColor(context, R.color.manualGreen);
        } else {
            color = ContextCompat.getColor(context, R.color.manualText3);
        }
        holder.prodName.setTextColor(color);
        holder.amount.setTextColor(color);
        holder.dots.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return ingredientsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView prodName;
        TextView amount;
        TextView dots;

        public ViewHolder(@NonNull View view) {
            super(view);
            prodName = view.findViewById(R.id.name);
            amount = view.findViewById(R.id.amount);
            dots = view.findViewById(R.id.dots);
        }
    }


}