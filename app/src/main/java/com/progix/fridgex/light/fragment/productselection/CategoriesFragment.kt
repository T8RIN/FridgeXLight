package com.progix.fridgex.light.fragment.productselection

import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Pair
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.allProducts
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.productselection.CategoryAdapter
import com.progix.fridgex.light.adapter.productselection.ProductsAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CategoriesFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            (requireActivity() as MainActivity).bottomSlideDown()
        }, 1)
    }

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

    private val catList: ArrayList<String> = ArrayList()
    private lateinit var recycler: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_categories, container, false)


        recycler = v.findViewById(R.id.categoriesRecycler)
        val cursor: Cursor = mDb.rawQuery("SELECT * FROM categories", null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            catList.add(cursor.getString(1))
            cursor.moveToNext()
        }
        pD = CategoryAdapter(requireContext(), catList, productClicker)
        recycler.adapter = pD
        cursor.close()


        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoriesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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

    private fun search(s: String?, int: Int?) {
        if (s!!.isNotEmpty()) {
            val pairArrayList = ArrayList<Pair<Int, String>>()
            val list = ArrayList<String>()
            for (item in allProducts) {
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
                recycler.adapter = CategoryAdapter(requireContext(), catList, productClicker)
            }
        }
    }

    private var pD: CategoryAdapter? = null

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

    private val productClicker = CategoryAdapter.OnClickListener { name, text ->
        val extras = FragmentNavigatorExtras(
            name to "name"
        )
        val bundle = Bundle()
        bundle.putString("category", text)
        arguments?.getInt("prodCat")?.let { bundle.putInt("prodCat", it) }
        findNavController().navigate(R.id.nav_products, bundle, null, extras)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }

}