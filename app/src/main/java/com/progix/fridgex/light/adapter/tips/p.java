package com.progix.fridgex.light.adapter.tips;

import static android.view.animation.AnimationUtils.loadAnimation;

import static com.progix.fridgex.light.data.SharedPreferencesAccess.loadTheme;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.data.DataArrays;
import com.progix.fridgex.light.helper.interfaces.SettingsInterface;

import java.util.ArrayList;
import java.util.List;

public class p extends RecyclerView.Adapter<p.ViewHolder> {

    Context context;
    DialogFragment fragment;
    List<Pair<Integer, Integer>> colorList;
    SettingsInterface colorInterface;
    

    public p(Context context, DialogFragment fragment, List<Pair<Integer, Integer>> colorList, SettingsInterface colorInterface){

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        //MaterialCardView  ;
        ImageView image;
        TextView text;
        //View  ;

        public ViewHolder(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.shapeImage);
            text = view.findViewById(R.id.text);
        }

        public void clearAnimation(){
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
    public boolean onFailedToRecycleView(@NonNull p.ViewHolder holder){
        return true;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull p.ViewHolder holder) {
        holder.clearAnimation();
    }
}
