package com.progix.fridgex.light.fragment.search

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.CheckBox
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.R.integer
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.search.SearchAdapter
import com.progix.fridgex.light.adapter.search.SearchFilterNavigationAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.appContext
import com.progix.fridgex.light.functions.Functions.addItemToList
import com.progix.fridgex.light.model.NavItem
import com.progix.fridgex.light.model.RecyclerSortItem
import kotlinx.coroutines.*

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var job: Job? = null

    private lateinit var searchRecycler: RecyclerView
    private lateinit var annotationCard: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(integer.anim_duration).toLong()
        }
    }

    private var recipeList: ArrayList<RecyclerSortItem>? = null
    private lateinit var navigationView: NavigationView
    private lateinit var loading: CircularProgressIndicator
    var checked = false

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        searchRecycler = v.findViewById(R.id.searchRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)
        loading = v.findViewById(R.id.loading)

        searchRecycler.setHasFixedSize(true)

        navigationView = v.findViewById(R.id.nav2)

        val navRecycler: RecyclerView = navigationView.findViewById(R.id.navRecycler)
        val list: ArrayList<NavItem> = arrayListOf(
            NavItem(getString(R.string.amount), R.drawable.ic_baseline_kitchen_24),
            NavItem(getString(R.string.byTime), R.drawable.ic_baseline_av_timer_24),
            NavItem(getString(R.string.byCal), R.drawable.ic_calories_24),
            NavItem(getString(R.string.byProt), R.drawable.ic_proteins_24),
            NavItem(getString(R.string.byFats), R.drawable.ic_fats_24),
            NavItem(getString(R.string.byCbh), R.drawable.ic_carbohydrates_24),
        )

        navRecycler.adapter = SearchFilterNavigationAdapter(appContext, list, navClicker)

        val checkBox: CheckBox = v.findViewById(R.id.checkbox)
        checkBox.setOnClickListener {
            checked = checkBox.isChecked
            recipeList!!.reverse()
            searchRecycler.adapter =
                SearchAdapter(appContext, recipeList!!, recipeClicker)
        }

        initSort(annotationCard, searchRecycler, loading)
    }

    private fun initSort(
        annotationCard: MaterialCardView,
        searchRecycler: RecyclerView,
        loading: CircularProgressIndicator
    ) {

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {

            startCoroutine(0)
            loading.visibility = View.GONE
            if (recipeList!!.isNotEmpty()) {
                searchRecycler.adapter =
                    SearchAdapter(
                        appContext,
                        recipeList!!,
                        recipeClicker
                    )
            } else {
                annotationCard.startAnimation(
                    loadAnimation(
                        appContext,
                        R.anim.item_animation_fall_down
                    )
                )
                annotationCard.visibility = View.VISIBLE
                searchRecycler.visibility = View.INVISIBLE
            }
        }
    }

    private suspend fun startCoroutine(modifier: Int) =
        withContext(Dispatchers.IO) {
            val pairList: ArrayList<RecyclerSortItem> = ArrayList()
            val allRecipes: Cursor =
                mDb.rawQuery("SELECT * FROM recipes WHERE banned NOT LIKE 1", null)
            allRecipes.moveToFirst()

            while (!allRecipes.isAfterLast) {
                val cc: Cursor = mDb.rawQuery("SELECT * FROM products WHERE banned = 1", null)
                cc.moveToFirst()
                val products = allRecipes.getString(4).split(" ")
                var found = false
                while (!cc.isAfterLast) {
                    val temp = "" + cc.getString(0)
                    if (products.contains(temp)) found = true
                    cc.moveToNext()
                }
                cc.close()
                if (!found) {
                    val id = allRecipes.getInt(0) - 1
                    val name = allRecipes.getString(3)
                    val time = allRecipes.getInt(6)
                    val cal = allRecipes.getInt(10).toDouble()
                    val prot = allRecipes.getDouble(11)
                    val fats = allRecipes.getDouble(12)
                    val carboh = allRecipes.getDouble(13)
                    var having = 0
                    val prodCursor: Cursor = mDb.rawQuery(
                        "SELECT * FROM products WHERE is_in_fridge = 1",
                        null
                    )
                    prodCursor.moveToFirst()
                    val needed: ArrayList<String> =
                        ArrayList(allRecipes.getString(4).trim().split(" "))
                    while (!prodCursor.isAfterLast) {
                        if (needed.contains(prodCursor.getString(0))) having++
                        prodCursor.moveToNext()
                    }
                    var indicator = 0
                    when {
                        having <= 0.49 * needed.size -> indicator = R.drawable.indicator_2
                        having <= 0.74 * needed.size -> indicator = R.drawable.indicator_1
                        having <= needed.size -> indicator = R.drawable.indicator_0
                    }
                    val xOfY = having.toString() + "/" + needed.size.toString()
                    val percentage = having.toDouble() / needed.size
                    if (percentage >= 0.35)
                        addItemToList(
                            id,
                            pairList,
                            percentage,
                            time,
                            cal,
                            prot,
                            fats,
                            carboh,
                            indicator,
                            name,
                            xOfY
                        )
                    prodCursor.close()
                }
                allRecipes.moveToNext()
            }
            allRecipes.close()
            when (modifier) {
                0 -> {
                    pairList.sortByDescending { it.amount }
                }
                1 -> {
                    pairList.sortByDescending { it.time }
                }
                2 -> {
                    pairList.sortByDescending { it.cal }
                }
                3 -> {
                    pairList.sortByDescending { it.prot }
                }
                4 -> {
                    pairList.sortByDescending { it.fats }
                }
                5 -> {
                    pairList.sortByDescending { it.carboh }
                }
            }
            recipeList = pairList
            if (checked) {
                pairList.reverse()
            }
            delay(300)
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter -> {
                if (recipeList != null) {
                    val drawerLayout: DrawerLayout? = view?.findViewById(R.id.drawer_layout)
                    drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                        drawerLayout.openDrawer(GravityCompat.END)
                        drawerLayout.addDrawerListener(object :
                            DrawerListener {
                            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                            }

                            override fun onDrawerOpened(drawerView: View) {
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                            }

                            override fun onDrawerClosed(drawerView: View) {
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                                slideUp()
                            }

                            override fun onDrawerStateChanged(newState: Int) {
                            }
                        })
                        slideDown()
                    } else {
                        drawerLayout.closeDrawer(GravityCompat.END)
                        slideUp()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun slideDown() {
        val bNav =
            (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val layoutParams = bNav.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
        behavior.slideDown(bNav)
    }

    private fun slideUp() {
        val bNav =
            (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val layoutParams = bNav.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
        behavior.slideUp(bNav)
    }

    private val recipeClicker = SearchAdapter.OnClickListener { image, id ->
        val intent = Intent(context, SecondActivity::class.java)
        intent.putExtra("rec", id)
        val options = activity?.let { makeSceneTransitionAnimation(it, image, "recipe") }
        startActivity(intent, options!!.toBundle())
    }

    private val navClicker = SearchFilterNavigationAdapter.OnClickListener { id ->

        val drawerLayout: DrawerLayout? = view?.findViewById(R.id.drawer_layout)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {

            startCoroutine(id)

            if (recipeList!!.isNotEmpty()) {
                searchRecycler.adapter =
                    SearchAdapter(
                        appContext,
                        recipeList!!,
                        recipeClicker
                    )
            } else {
                annotationCard.startAnimation(
                    loadAnimation(
                        appContext,
                        R.anim.item_animation_fall_down
                    )
                )
                annotationCard.visibility = View.VISIBLE
                searchRecycler.visibility = View.INVISIBLE
            }

            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout?.closeDrawer(GravityCompat.END)
            }, 600)

        }
    }

}