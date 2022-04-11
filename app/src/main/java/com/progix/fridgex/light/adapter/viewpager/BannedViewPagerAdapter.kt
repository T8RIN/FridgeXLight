package com.progix.fridgex.light.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.progix.fridgex.light.activity.MainActivity.Companion.productsFragment
import com.progix.fridgex.light.activity.MainActivity.Companion.recipesFragment
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment
import com.progix.fridgex.light.fragment.banned.BannedRecipesFragment


class BannedViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                recipesFragment = BannedRecipesFragment()
                recipesFragment!!
            }
            else -> {
                productsFragment = BannedProductsFragment()
                productsFragment!!
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}