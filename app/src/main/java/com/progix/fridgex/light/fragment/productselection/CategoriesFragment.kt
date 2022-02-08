package com.progix.fridgex.light.fragment.productselection

import android.database.Cursor
import android.os.Bundle
import android.util.Pair
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.productselection.CategoryAdapter
import com.progix.fridgex.light.adapter.productselection.ProductsAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.allProducts
import com.progix.fridgex.light.functions.Functions.searchString
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

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

    private val catList: ArrayList<String> = ArrayList()
    private lateinit var recycler: RecyclerView

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        recycler = v.findViewById(R.id.categoriesRecycler)
        val cursor: Cursor = mDb.rawQuery("SELECT * FROM categories", null)
        cursor.moveToFirst()
        catList.clear()
        while (!cursor.isAfterLast) {
            catList.add(cursor.getString(1))
            cursor.moveToNext()
        }
        categoryAdapter = CategoryAdapter(requireContext(), catList, productClicker)
        recycler.adapter = categoryAdapter
        cursor.close()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_menu, menu)
        val myActionMenuItem = menu.findItem(R.id.search_search)
        val searchView = myActionMenuItem.actionView as SearchView
        searchView.queryTextChangeEvents()
            .debounce(350, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                search(it.queryText.toString(), arguments?.getInt("prodCat"))
            }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun search(s: String?, int: Int?) {
        if (s!!.isNotEmpty()) {
            val pairArrayList = ArrayList<Pair<Int, String>>()
            val list = ArrayList<String>()
            for (item in allProducts!!) {
                val temp: Int =
                    searchString(s.lowercase(), item)
                if (temp != 101) {
                    pairArrayList.add(Pair(temp, item))
                }
            }
            pairArrayList.sortBy { it.first }
            for (item in pairArrayList) {
                list.add(item.second)
            }
            recycler.adapter = ProductsAdapter(requireContext(), list, int!!)
        } else {
            if (recycler.adapter != categoryAdapter) {
                recycler.adapter = CategoryAdapter(requireContext(), catList, productClicker)
            }
        }
    }

    private var categoryAdapter: CategoryAdapter? = null

    private val productClicker = CategoryAdapter.OnClickListener { name, text ->
        val extras = FragmentNavigatorExtras(
            name to "name"
        )
        val bundle = Bundle()
        bundle.putString("category", text)
        arguments?.getInt("prodCat")?.let { bundle.putInt("prodCat", it) }
        findNavController().navigate(R.id.nav_products, bundle, null, extras)

    }

}