package com.progix.fridgex.light.adapter.viewpager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.progix.fridgex.light.fragment.recipe.InfoFragment;
import com.progix.fridgex.light.fragment.recipe.IngredsFragment;
import com.progix.fridgex.light.fragment.recipe.RecipeFragment;

import org.jetbrains.annotations.NotNull;


public final class RecipeViewPagerAdapter extends FragmentStateAdapter {

    @Override
    @NotNull
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new IngredsFragment();
                break;
            case 1:
                fragment = new RecipeFragment();
                break;
            default:
                fragment = new InfoFragment();
        }
        return fragment;
    }

    public int getItemCount() {
        return 3;
    }

    public RecipeViewPagerAdapter(@NotNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
}
