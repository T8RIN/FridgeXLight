package com.progix.fridgex.light.fragment.banned

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.adapter.viewpager.BannedViewPagerAdapter
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment.Companion.prodAnno
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment.Companion.prodRecycler
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment.Companion.productsList
import com.progix.fridgex.light.fragment.banned.BannedRecipesFragment.Companion.recAnno
import com.progix.fridgex.light.fragment.banned.BannedRecipesFragment.Companion.recRecycler
import com.progix.fridgex.light.fragment.banned.BannedRecipesFragment.Companion.recipeList

class BannedFragment : Fragment() {


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_ban_list, container, false)

        setUpViewPager(v)

        val swipeRefresh: SwipeRefreshLayout = v.findViewById(R.id.swipeRefresh)

        swipeRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.manualBackground
            )
        )

        swipeRefresh.setColorSchemeResources(R.color.checked, R.color.red, R.color.yellow)

        swipeRefresh.setOnRefreshListener {
            setUpViewPager(v)
            swipeRefresh.isRefreshing = false
        }



        return v
    }

    private fun setUpViewPager(v: View) {
        val viewPager: ViewPager2 = v.findViewById(R.id.ban_view_pager)
        viewPager.adapter = BannedViewPagerAdapter(requireActivity())
        val titles = arrayOf(
            getString(R.string.recipes),
            getString(R.string.products)
        )
        val tabLayout = v.findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
            MainActivity.actionMode?.finish()
        }.attach()
        tabLayout.selectTab(tabLayout.getTabAt(1))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                MainActivity.actionMode?.finish()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.banned_menu, menu)
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
                                recRecycler?.visibility = View.GONE
                                recAnno?.visibility = View.VISIBLE
                                for (i in recipeList) {
                                    val t = i.recipeItem.recipeName
                                    MainActivity.mDb.execSQL(
                                        "UPDATE recipes SET banned = 0 WHERE recipe_name = ?",
                                        listOf(t).toTypedArray()
                                    )
                                }
                                CustomSnackbar(requireContext()).create(
                                    55,
                                    (requireContext() as MainActivity).findViewById(R.id.main_root),
                                    getString(R.string.clearSuccessBanned)
                                )
                                    .setAction(getString(R.string.undo)) {
                                        for (i in recipeList) {
                                            val t = i.recipeItem.recipeName
                                            MainActivity.mDb.execSQL(
                                                "UPDATE recipes SET banned = 1 WHERE recipe_name = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (recipeList.isNotEmpty()) {
                                            recRecycler?.visibility =
                                                View.VISIBLE
                                            recAnno?.visibility = View.GONE
                                        }
                                    }
                                    .show()
                            }
                            if (multiArrayBoolean[1] && !multiArrayBoolean[0]) {
                                prodRecycler?.visibility = View.GONE
                                prodAnno?.visibility = View.VISIBLE
                                for (i in productsList) {
                                    val t = i.first
                                    MainActivity.mDb.execSQL(
                                        "UPDATE products SET banned = 0 WHERE product = ?",
                                        listOf(t).toTypedArray()
                                    )
                                }
                                CustomSnackbar(requireContext()).create(
                                    55,
                                    (requireContext() as MainActivity).findViewById(R.id.main_root),
                                    getString(R.string.clearSuccessBannedProd)
                                )
                                    .setAction(getString(R.string.undo)) {
                                        for (i in productsList) {
                                            val t = i.first
                                            MainActivity.mDb.execSQL(
                                                "UPDATE products SET banned = 1 WHERE product = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (productsList.isNotEmpty()) {
                                            prodRecycler?.visibility =
                                                View.VISIBLE
                                            prodAnno?.visibility = View.GONE
                                        }
                                    }
                                    .show()
                            } else if (multiArrayBoolean[1] && multiArrayBoolean[0]) {
                                prodRecycler?.visibility = View.GONE
                                prodAnno?.visibility = View.VISIBLE
                                for (i in productsList) {
                                    val t = i.first
                                    MainActivity.mDb.execSQL(
                                        "UPDATE products SET banned = 0 WHERE product = ?",
                                        listOf(t).toTypedArray()
                                    )
                                }
                                recRecycler?.visibility = View.GONE
                                recAnno?.visibility = View.VISIBLE
                                for (i in recipeList) {
                                    val t = i.recipeItem.recipeName
                                    MainActivity.mDb.execSQL(
                                        "UPDATE recipes SET banned = 0 WHERE recipe_name = ?",
                                        listOf(t).toTypedArray()
                                    )
                                }
                                CustomSnackbar(requireContext()).create(
                                    55,
                                    (requireContext() as MainActivity).findViewById(R.id.main_root),
                                    getString(R.string.clearSuccessBannedAll)
                                )
                                    .setAction(getString(R.string.undo)) {
                                        for (i in recipeList) {
                                            val t = i.recipeItem.recipeName
                                            MainActivity.mDb.execSQL(
                                                "UPDATE recipes SET banned = 1 WHERE recipe_name = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (recipeList.isNotEmpty()) {
                                            recRecycler?.visibility =
                                                View.VISIBLE
                                            recAnno?.visibility = View.GONE
                                        }
                                        for (i in productsList) {
                                            val t = i.first
                                            MainActivity.mDb.execSQL(
                                                "UPDATE products SET banned = 1 WHERE product = ?",
                                                listOf(t).toTypedArray()
                                            )
                                        }
                                        if (productsList.isNotEmpty()) {
                                            prodRecycler?.visibility =
                                                View.VISIBLE
                                            prodAnno?.visibility = View.GONE
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
                        getString(R.string.banClearError),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}