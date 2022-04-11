package com.progix.fridgex.light.adapter.viewpager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.progix.fridgex.light.fragment.starred.StarProductsFragment;
import com.progix.fridgex.light.fragment.starred.StarRecipesFragment;

import org.jetbrains.annotations.NotNull;


public final class StarredViewPagerAdapter extends FragmentStateAdapter {
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new StarRecipesFragment();
        } else {
            return new StarProductsFragment();
        }
    }

    public int getItemCount() {
        return 2;
    }

    public StarredViewPagerAdapter(@NotNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
}
