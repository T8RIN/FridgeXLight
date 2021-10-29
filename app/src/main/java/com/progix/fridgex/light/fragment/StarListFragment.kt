package com.progix.fridgex.light.fragment

import android.os.Bundle
import android.view.*
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
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.actionMode
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.fragment.StarProductsFragment.Companion.prodAnno
import com.progix.fridgex.light.fragment.StarProductsFragment.Companion.prodRecycler
import com.progix.fridgex.light.fragment.StarProductsFragment.Companion.productsList
import com.progix.fridgex.light.fragment.StarRecipesFragment.Companion.recAnno
import com.progix.fridgex.light.fragment.StarRecipesFragment.Companion.recRecycler
import com.progix.fridgex.light.fragment.StarRecipesFragment.Companion.recipeList
import com.progix.fridgex.light.viewpager.StarredViewPagerAdapter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StarFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_star_list, container, false)

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
        tabLayout.selectTab(tabLayout.getTabAt(1))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                actionMode?.finish()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.starred_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                if (productsList.isNotEmpty() || recipeList.isNotEmpty()) {
                    val multiArray = arrayOf(
                        getString(R.string.recipes),
                        getString(R.string.products)
                    )
                    val multiArrayBoolean = booleanArrayOf(
                        true,
                        true
                    )
                    MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                        .setTitle(getString(R.string.clear))
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->
                            if (multiArrayBoolean[0] && !multiArrayBoolean[1]) {
                                recRecycler?.visibility = GONE
                                recAnno?.visibility = VISIBLE
                                for (i in recipeList) {
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
                                        for (i in recipeList) {
                                            val t = i.recipeItem.recipeName
                                            mDb.execSQL(
                                                "UPDATE recipes SET is_starred = 1 WHERE recipe_name = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (recipeList.isNotEmpty()) {
                                            recRecycler?.visibility = VISIBLE
                                            recAnno?.visibility = GONE
                                        }
                                    }
                                    .show()
                            }
                            if (multiArrayBoolean[1] && !multiArrayBoolean[0]) {
                                prodRecycler?.visibility = GONE
                                prodAnno?.visibility = VISIBLE
                                for (i in productsList) {
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
                                        for (i in productsList) {
                                            val t = i.first
                                            mDb.execSQL(
                                                "UPDATE products SET is_starred = 1 WHERE product = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (productsList.isNotEmpty()) {
                                            prodRecycler?.visibility = VISIBLE
                                            prodAnno?.visibility = GONE
                                        }
                                    }
                                    .show()
                            } else if (multiArrayBoolean[1] && multiArrayBoolean[0]) {
                                prodRecycler?.visibility = GONE
                                prodAnno?.visibility = VISIBLE
                                for (i in productsList) {
                                    val t = i.first
                                    mDb.execSQL(
                                        "UPDATE products SET is_starred = 0 WHERE product = ?",
                                        listOf(t).toTypedArray()
                                    )
                                }
                                recRecycler?.visibility = GONE
                                recAnno?.visibility = VISIBLE
                                for (i in recipeList) {
                                    val t = i.recipeItem.recipeName
                                    mDb.execSQL(
                                        "UPDATE recipes SET is_starred = 0 WHERE recipe_name = ?",
                                        listOf(t).toTypedArray()
                                    )
                                }
                                CustomSnackbar(requireContext()).create(
                                    55,
                                    (requireContext() as MainActivity).findViewById(R.id.main_root),
                                    getString(R.string.clearSuccessStarredAll)
                                )
                                    .setAction(getString(R.string.undo)) {
                                        for (i in recipeList) {
                                            val t = i.recipeItem.recipeName
                                            mDb.execSQL(
                                                "UPDATE recipes SET is_starred = 1 WHERE recipe_name = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (recipeList.isNotEmpty()) {
                                            recRecycler?.visibility = VISIBLE
                                            recAnno?.visibility = GONE
                                        }
                                        for (i in productsList) {
                                            val t = i.first
                                            mDb.execSQL(
                                                "UPDATE products SET is_starred = 1 WHERE product = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (productsList.isNotEmpty()) {
                                            prodRecycler?.visibility = VISIBLE
                                            prodAnno?.visibility = GONE
                                        }
                                    }
                                    .show()
                            }
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setMultiChoiceItems(multiArray, multiArrayBoolean) { _, which, isChecked ->
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

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}