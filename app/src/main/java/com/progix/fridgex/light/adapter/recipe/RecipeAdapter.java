package com.progix.fridgex.light.adapter.recipe;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.progix.fridgex.light.fragment.recipe.IngredsFragment.currentStep;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.activity.SecondActivity;
import com.progix.fridgex.light.custom.CustomSnackbar;
import com.progix.fridgex.light.fragment.recipe.RecipeFragment;

import java.util.List;
import java.util.Objects;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    RecipeFragment fragment;
    List<String> recipeList;

    public RecipeAdapter(RecipeFragment fragment, List<String> recipeList) {
        this.fragment = fragment;
        this.recipeList = recipeList;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setOnClickListener(RecipeAdapter.ViewHolder holder, Integer position) {
        holder.nextButton.setOnClickListener(v -> {
            if (currentStep + 1 >= recipeList.size()) {
                new CustomSnackbar(fragment.requireContext()).create(
                                364,
                                ((SecondActivity) fragment.requireActivity()).mainRoot,
                                fragment.requireContext().getString(R.string.bonAppetit),
                                Snackbar.LENGTH_LONG
                        )
                        .setAction(fragment.requireContext().getString(R.string.repeat), c -> {
                            currentStep = 0;
                            fragment.recycler.animate().alpha(0f).setDuration(200L).withEndAction(() -> {
                                notifyDataSetChanged();
                                fragment.recycler.scrollToPosition(0);
                                fragment.recycler.animate().alpha(1f).setDuration(200L).start();
                            }).start();
                        })
                        .show();
            }

            holder.nextButton.setOnClickListener(null);
            new Handler(Looper.getMainLooper()).postDelayed(() -> setOnClickListener(holder, position), 500);

            currentStep++;
            notifyItemChanged(position);
            notifyItemChanged(position + 1);

            ((LinearLayoutManager) Objects.requireNonNull(fragment.recycler.getLayoutManager())).scrollToPositionWithOffset(currentStep, 0);
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_actions, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.prodName.setText(recipeList.get(position));

        holder.container.setVisibility(GONE);
        if (currentStep == position) {
            holder.container.setVisibility(VISIBLE);
            setOnClickListener(holder, position);
            holder.itemView.setAlpha(1f);
        } else if (currentStep > position) {
            holder.itemView.setAlpha(1f);
        } else {
            holder.itemView.setAlpha(0.2f);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView prodName;
        View container;
        MaterialCardView nextButton;

        public ViewHolder(@NonNull View view) {
            super(view);
            prodName = view.findViewById(R.id.name);
            container = view.findViewById(R.id.buttonContainer);
            nextButton = view.findViewById(R.id.nextButton);
        }
    }

}