package com.progix.fridgex.light.fragment.folder

import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.folder.FolderCategoriesAdapter
import com.progix.fridgex.light.adapter.folder.FolderRecipesAdapter
import com.progix.fridgex.light.data.DataArrays.secondaryFolderCategoriesImages
import com.progix.fridgex.light.functions.Functions
import com.progix.fridgex.light.functions.Functions.delayedAction
import com.progix.fridgex.light.functions.Functions.searchString
import com.progix.fridgex.light.model.RecyclerSortItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class FolderCategoriesFragment : Fragment(R.layout.fragment_folder_categories) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }

        setHasOptionsMenu(true)
    }


    private lateinit var recycler: RecyclerView

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val id: Int = arguments?.get("catF") as Int + 1
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipe_categories_global WHERE id = ?",
            listOf(id.toString()).toTypedArray()
        )
        cursor.moveToFirst()
        val name = cursor.getString(1)
        cursor.close()
        val cursor2: Cursor = mDb.rawQuery(
            "SELECT * FROM recipe_category_local WHERE category_global = ?",
            listOf(name.toString()).toTypedArray()
        )
        cursor2.moveToFirst()
        val listFolder: ArrayList<Pair<Int, String>> = ArrayList()
        val list: ArrayList<Int> = ArrayList()
        while (!cursor2.isAfterLast) {
            listFolder.add(
                Pair(
                    secondaryFolderCategoriesImages[cursor2.getInt(0) - 1],
                    cursor2.getString(2)
                )
            )
            list.add(cursor2.getInt(0) - 1)
            cursor2.moveToNext()
        }
        cursor2.close()

        delayedAction(10) {
            requireActivity().findViewById<Toolbar>(R.id.toolbar).title = name
        }

        recycler = v.findViewById(R.id.podFolderRecycler)
        adapter = FolderCategoriesAdapter(requireContext(), listFolder, folderClicker, list)
        recycler.adapter = adapter

        if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            (recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 1
        } else {
            (recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 2
        }
    }


    private val folderClicker = FolderCategoriesAdapter.OnClickListener { _, id ->
        val bundle = Bundle()
        bundle.putInt("catB", id)
        findNavController().navigate(R.id.nav_folder_recipes, bundle)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_menu, menu)
        val myActionMenuItem = menu.findItem(R.id.search_search)
        val searchView = myActionMenuItem.actionView as SearchView
        searchView.queryTextChangeEvents()
            .debounce(350, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                search(it.queryText.toString())
            }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun search(s: String?) {
        if (s!!.isNotEmpty()) {
            val pairArrayList = ArrayList<Pair<Int, RecyclerSortItem>>()
            val list = ArrayList<RecyclerSortItem>()

            val pairList: ArrayList<RecyclerSortItem> = ArrayList()
            val allRecipes: Cursor =
                mDb.rawQuery("SELECT * FROM recipes", null)
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
                Functions.addItemToList(
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
                products.close()
                allRecipes.moveToNext()
            }
            allRecipes.close()

            for (item in pairList) {
                val temp: Int =
                    searchString(s.lowercase(), item.recipeItem.recipeName.lowercase())
                if (temp != 101) {
                    pairArrayList.add(Pair(temp, item))
                }
            }
            pairArrayList.sortBy { it.first }
            for (item in pairArrayList) {
                list.add(item.second)
            }
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                (recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 1
            } else {
                (recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 2
            }
            recycler.adapter = FolderRecipesAdapter(requireContext(), list, recipeClicker)
        } else {
            if (recycler.adapter != adapter) {
                if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    (recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 1
                } else {
                    (recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 2
                }
                recycler.adapter = adapter
            }
        }
    }

    var adapter: FolderCategoriesAdapter? = null

    private val recipeClicker = FolderRecipesAdapter.OnClickListener { image, id ->
        val intent = Intent(context, SecondActivity::class.java)
        intent.putExtra("rec", id)
        val options = activity?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                it,
                image,
                "recipe"
            )
        }
        startActivity(intent, options!!.toBundle())
    }

}