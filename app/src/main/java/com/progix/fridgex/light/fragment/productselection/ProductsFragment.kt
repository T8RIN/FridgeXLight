package com.progix.fridgex.light.fragment.productselection

import android.database.Cursor
import android.os.Bundle
import android.util.Pair
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.productselection.ProductsAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.allProducts
import com.progix.fridgex.light.functions.Functions.delayedAction
import com.progix.fridgex.light.functions.Functions.searchString
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class ProductsFragment : Fragment(R.layout.fragment_products) {


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

    lateinit var recycler: RecyclerView
    private var prodList: ArrayList<String> = ArrayList()
    private var prodCategory: Int = 0

    private var job: Job? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        prodCategory = requireArguments().getInt("prodCat")
        recycler = v.findViewById(R.id.productsRecycler)
        val name = arguments?.getString("category")

        delayedAction(10) {
            requireActivity().findViewById<Toolbar>(R.id.toolbar).title = name
        }

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            prodList = coroutine()
            productsAdapter = ProductsAdapter(requireContext(), prodList, prodCategory)
            recycler.adapter = productsAdapter
        }
    }


    private suspend fun coroutine(): ArrayList<String> = withContext(Dispatchers.IO) {
        val name = arguments?.getString("category")
        val array = ArrayList<String>()
        val cursor: Cursor =
            mDb.rawQuery("SELECT * FROM products WHERE category = ?", listOf(name).toTypedArray())
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            array.add(cursor.getString(2))
            cursor.moveToNext()
        }
        array.sortBy { it }
        cursor.close()
        return@withContext array
    }

    private var productsAdapter: ProductsAdapter? = null


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
            if (recycler.adapter != productsAdapter) {
                recycler.adapter = ProductsAdapter(requireContext(), prodList, prodCategory)
            }
        }
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

}