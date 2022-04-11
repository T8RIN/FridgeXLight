package com.progix.fridgex.light.adapter.viewpager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.progix.fridgex.light.activity.MainActivity;
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment;
import com.progix.fridgex.light.fragment.banned.BannedRecipesFragment;

import org.jetbrains.annotations.NotNull;

public final class BannedViewPagerAdapter extends FragmentStateAdapter {
    @NotNull
    public Fragment createFragment(int position) {
        Fragment var2;
        if (position == 0) {
            MainActivity.setRecipesFragment(new BannedRecipesFragment());
            var2 = MainActivity.getRecipesFragment();
        } else {
            MainActivity.setProductsFragment(new BannedProductsFragment());
            var2 = MainActivity.getProductsFragment();
        }
        return var2;
    }

    public int getItemCount() {
        return 2;
    }

    public BannedViewPagerAdapter(@NotNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
}
