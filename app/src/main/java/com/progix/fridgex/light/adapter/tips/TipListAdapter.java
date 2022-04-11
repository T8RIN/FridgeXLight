package com.progix.fridgex.light.adapter.tips;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.progix.fridgex.light.data.DataArrays.adviceImages;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.progix.fridgex.light.R;

import java.util.ArrayList;

public class TipListAdapter extends RecyclerView.Adapter<TipListAdapter.ViewHolder> {

    Context context;
    ArrayList<Pair<Integer, String>> tipList;
    NavController navController;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(adviceImages.get(tipList.get(position).first - 1)).into(holder.image);
        holder.category.setText(tipList.get(position).second);
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("advice", position);
            navController.navigate(R.id.nav_tip_list, bundle, null);
        });
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        //MaterialCardView  ;
        ImageView image;
        TextView category;
        //View  ;

        public ViewHolder(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.image);
            category = view.findViewById(R.id.category);
        }

        public void clearAnimation() {
            itemView.clearAnimation();
        }

    }

    private int lastPosition = -1;

    private void setAnimation(View viewToAnimate, Integer position) {
        Animation animation = loadAnimation(context, R.anim.item_animation_fall_down);
        viewToAnimate.startAnimation(animation);
        lastPosition = position;
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return true;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.clearAnimation();
    }

}
