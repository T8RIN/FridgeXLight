package com.progix.fridgex.light.fragment.starred

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.actionMode
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.viewpager.StarredViewPagerAdapter
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.fragment.starred.StarProductsFragment.Companion.prodAnno
import com.progix.fridgex.light.fragment.starred.StarProductsFragment.Companion.prodRecycler
import com.progix.fridgex.light.fragment.starred.StarProductsFragment.Companion.productsList
import com.progix.fridgex.light.fragment.starred.StarRecipesFragment.Companion.recAnno
import com.progix.fridgex.light.fragment.starred.StarRecipesFragment.Companion.recRecycler
import com.progix.fridgex.light.fragment.starred.StarRecipesFragment.Companion.recipeList

class StarListFragment : Fragment(R.layout.fragment_star_list) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

    private var position = 0

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val viewPager: ViewPager2 = v.findViewById(R.id.star_view_pager)
        viewPager.adapter = StarredViewPagerAdapter(requireActivity())
        val titles = arrayOf(
            getString(R.string.recipes),
            getString(R.string.products)
        )
        val tabLayout = v.findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
            actionMode?.finish()
        }.attach()

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                actionMode?.finish()
                position = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_with_only_delete_option, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                if (productsList != null || recipeList != null) {
                    when (position) {
                        0 -> {
                            if (recipeList?.isNotEmpty() == true) {
                                val multiArray = arrayOf(
                                    getString(R.string.recipes)
                                )
                                val multiArrayBoolean = booleanArrayOf(
                                    true
                                )
                                MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                                    .setTitle(getString(R.string.clear))
                                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                                        if (multiArrayBoolean[0]) {
                                            recRecycler?.visibility = GONE
                                            recAnno?.visibility = VISIBLE
                                            for (i in recipeList!!) {
                                                val t = i.recipeItem.recipeName
                                                mDb.execSQL(
                                                    "UPDATE recipes SET is_starred = 0 WHERE recipe_name = ?",
                                                    listOf(t).toTypedArray()
                                                )
                                            }
                                            CustomSnackbar(requireContext()).create(
                                                55,
                                                (requireContext() as MainActivity).findViewById(R.id.main_root),
                                                getString(R.string.clearSuccessStarred)
                                            )
                                                .setAction(getString(R.string.undo)) {
                                                    for (i in recipeList!!) {
                                                        val t = i.recipeItem.recipeName
                                                        mDb.execSQL(
                                                            "UPDATE recipes SET is_starred = 1 WHERE recipe_name = ?",
                                                            listOf(t).toTypedArray()
                                                        )
                                                    }
                                                    if (recipeList!!.isNotEmpty()) {
                                                        recRecycler?.visibility = VISIBLE
                                                        recAnno?.visibility = GONE
                                                    }
                                                }
                                                .show()
                                        }
                                    }
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .setMultiChoiceItems(
                                        multiArray,
                                        multiArrayBoolean
                                    ) { _, which, isChecked ->
                                        multiArrayBoolean[which] = isChecked
                                    }
                                    .show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.starClearError),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        1 -> {
                            if (productsList!!.isNotEmpty()) {
                                val multiArray = arrayOf(
                                    getString(R.string.products)
                                )
                                val multiArrayBoolean = booleanArrayOf(
                                    true
                                )
                                MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                                    .setTitle(getString(R.string.clear))
                                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                                        if (multiArrayBoolean[0]) {
                                            prodRecycler?.visibility = GONE
                                            prodAnno?.visibility = VISIBLE
                                            for (i in productsList!!) {
                                                val t = i.first
                                                mDb.execSQL(
                                                    "UPDATE products SET is_starred = 0 WHERE product = ?",
                                                    listOf(t).toTypedArray()
                                                )
                                            }
                                            CustomSnackbar(requireContext()).create(
                                                55,
                                                (requireContext() as MainActivity).findViewById(R.id.main_root),
                                                getString(R.string.clearSuccessStarredProd)
                                            )
                                                .setAction(getString(R.string.undo)) {
                                                    for (i in productsList!!) {
                                                        val t = i.first
                                                        mDb.execSQL(
                                                            "UPDATE products SET is_starred = 1 WHERE product = ?",
                                                            listOf(t).toTypedArray()
                                                        )
                                                    }
                                                    if (productsList!!.isNotEmpty()) {
                                                        prodRecycler?.visibility = VISIBLE
                                                        prodAnno?.visibility = GONE
                                                    }
                                                }
                                                .show()
                                        }
                                    }
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .setMultiChoiceItems(
                                        multiArray,
                                        multiArrayBoolean
                                    ) { _, which, isChecked ->
                                        multiArrayBoolean[which] = isChecked
                                    }
                                    .show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.starClearError),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prodRecycler = null
        productsList = null
        prodAnno = null
        recAnno = null
        recRecycler = null
        recipeList = null
    }

}