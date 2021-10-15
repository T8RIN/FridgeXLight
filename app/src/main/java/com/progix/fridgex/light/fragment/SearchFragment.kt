package com.progix.fridgex.light.fragment

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.CheckBox
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.MainActivity.Companion.images
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.MainActivity.Companion.slideDown
import com.progix.fridgex.light.MainActivity.Companion.slideUp
import com.progix.fridgex.light.R
import com.progix.fridgex.light.R.integer
import com.progix.fridgex.light.SecondActivity
import com.progix.fridgex.light.adapter.NavigationAdapter
import com.progix.fridgex.light.adapter.SearchAdapter
import com.progix.fridgex.light.model.NavItem
import com.progix.fridgex.light.model.RecipeItem
import com.progix.fridgex.light.model.RecyclerSortItem
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var recipeList: ArrayList<RecyclerSortItem>? = null
    private lateinit var navigationView: NavigationView
    private lateinit var loading: CircularProgressIndicator
    var checked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_search, container, false)
        searchRecycler = v.findViewById(R.id.searchRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)
        loading = v.findViewById(R.id.loading)

        searchRecycler.setHasFixedSize(true)

        navigationView = v.findViewById(R.id.nav2)
        navigationView.setCheckedItem(R.id.amount_f)
        val navRecycler: RecyclerView = navigationView.findViewById(R.id.navRecycler)
        val list: ArrayList<NavItem> = arrayListOf(
            NavItem(getString(R.string.amount), R.drawable.ic_baseline_kitchen_24),
            NavItem(getString(R.string.byTime), R.drawable.ic_baseline_av_timer_24),
            NavItem(getString(R.string.byCal), R.drawable.ic_calories_24),
            NavItem(getString(R.string.byProt), R.drawable.ic_proteins_24),
            NavItem(getString(R.string.byFats), R.drawable.ic_fats_24),
            NavItem(getString(R.string.byCbh), R.drawable.ic_carbohydrates_24),
        )

        navRecycler.adapter = NavigationAdapter(requireContext(), list, navClicker)

        val checkBox: CheckBox = v.findViewById(R.id.checkbox)
        checkBox.setOnClickListener{
            checked = checkBox.isChecked
            recipeList!!.reverse()
            searchRecycler.adapter = SearchAdapter(requireContext(), recipeList!!, recipeClicker)
        }


        initSort(annotationCard, searchRecycler, loading)


//        val radioGroup: RadioGroup = navigationView.findViewById(R.id.radio)
//        radioGroup.setOnCheckedChangeListener { _, checkedId ->
//
//        }

        return v
    }

    private fun initSort(
        //modifier: Int,
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
                    SearchAdapter(requireContext(), recipeList!!, recipeClicker)
            } else {
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
                val id = allRecipes.getInt(0) - 1
                val name = allRecipes.getString(3)
                val time = allRecipes.getInt(6)
                val cal = allRecipes.getInt(10).toDouble()
                val prot = allRecipes.getDouble(11)
                val fats = allRecipes.getDouble(12)
                val carboh = allRecipes.getDouble(13)
                var having = 0
                val products: Cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE is_in_fridge = 1",
                    null
                )
                products.moveToFirst()
                val needed: ArrayList<String> =
                    ArrayList(allRecipes.getString(4).trim().split(" "))
                while (!products.isAfterLast) {
                    if (needed.contains(products.getString(0))) having++
                    products.moveToNext()
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
                    pairList.add(
                        RecyclerSortItem(
                            percentage, time, cal, prot, fats, carboh,
                            RecipeItem(
                                images[id],
                                indicator,
                                name,
                                time.toString(),
                                xOfY
                            )
                        )
                    )
                products.close()
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
            if(checked){
                pairList.reverse()
            }
            Thread.sleep(200)
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

    private val recipeClicker = SearchAdapter.OnClickListener { image, id ->
        val intent = Intent(context, SecondActivity::class.java)
        intent.putExtra("rec", id)
        val options = activity?.let { makeSceneTransitionAnimation(it, image, "recipe") }
        startActivity(intent, options!!.toBundle())
    }

    private val navClicker = NavigationAdapter.OnClickListener { id ->

        val drawerLayout: DrawerLayout? = view?.findViewById(R.id.drawer_layout)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {

            startCoroutine(id)

            if (recipeList!!.isNotEmpty()) {
                searchRecycler.adapter =
                    SearchAdapter(requireContext(), recipeList!!, recipeClicker)
            } else {
                annotationCard.visibility = View.VISIBLE
                searchRecycler.visibility = View.INVISIBLE
            }

            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout?.closeDrawer(GravityCompat.END)
            }, 600)

        }
    }


}