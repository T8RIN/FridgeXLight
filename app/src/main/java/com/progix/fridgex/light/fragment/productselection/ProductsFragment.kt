package com.progix.fridgex.light.fragment.productselection

import android.database.Cursor
import android.os.Bundle
import android.util.Pair
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.productselection.ProductsAdapter
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
    private var idd: Int = 0

    private var job: Job? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        idd = requireArguments().getInt("prodCat")
        recycler = v.findViewById(R.id.productsRecycler)
        val name = arguments?.getString("category")
        requireActivity().findViewById<Toolbar>(R.id.toolbar).title = name

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            prodList = coroutine()
            pD = ProductsAdapter(requireContext(), prodList, idd)
            recycler.adapter = pD
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

    private var pD: ProductsAdapter? = null


    private fun search(s: String?, int: Int?) {
        if (s!!.isNotEmpty()) {
            val pairArrayList = ArrayList<Pair<Int, String>>()
            val list = ArrayList<String>()
            for (item in MainActivity.allProducts!!) {
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
            if (recycler.adapter != pD) {
                recycler.adapter = ProductsAdapter(requireContext(), prodList, idd)
            }
        }
    }

    private fun searchString(chtoIshem: String, gdeIshem: String): Int {
        val d = 256
        val q = 101
        var h = 1
        var i: Int
        var j: Int
        var p = 0
        var t = 0
        val m = chtoIshem.length
        val n = gdeIshem.length
        if (m <= n) {
            i = 0
            while (i < m - 1) {
                h = h * d % q
                ++i
            }
            i = 0
            while (i < m) {
                p = (d * p + chtoIshem[i].code) % q
                t = (d * t + gdeIshem[i].code) % q
                ++i
            }
            i = 0
            while (i <= n - m) {
                if (p == t) {
                    j = 0
                    while (j < m) {
                        if (gdeIshem[i + j] != chtoIshem[j]) break
                        ++j
                    }
                    if (j == m) return i
                }
                if (i < n - m) {
                    t = (d * (t - gdeIshem[i].code * h) + gdeIshem[i + m].code) % q
                    if (t < 0) t += q
                }
                ++i
            }
        } else return q
        return q
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