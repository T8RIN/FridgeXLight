package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.util.Pair
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.CategoryAdapter
import com.progix.fridgex.light.adapter.ProductsAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductsFragment : Fragment() {
    // TODO: Rename and change types of parameters
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

    lateinit var recycler: RecyclerView
    private var prodList: ArrayList<String> = ArrayList()
    var idd: Int = 0

    var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_products, container, false)


        idd = requireArguments().getInt("prodCat")
        recycler = v.findViewById(R.id.productsRecycler)
        val name = arguments?.getString("category")
        (requireActivity() as MainActivity).toolbar.title = name

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch{
            prodList = coroutine()
            pD = ProductsAdapter(requireContext(), prodList, idd)
            recycler.adapter = pD
        }



        
        return v
    }

    private suspend fun coroutine(): ArrayList<String> = withContext(Dispatchers.IO){
        val name = arguments?.getString("category")
        val array = ArrayList<String>()
        val cursor: Cursor = mDb.rawQuery("SELECT * FROM products WHERE category = ?", listOf(name).toTypedArray())
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            array.add(cursor.getString(2))
            cursor.moveToNext()
        }
        array.sortBy{it}
        cursor.close()
        return@withContext array
    }

    var pD: ProductsAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun search(s: String?, int: Int?) {
        if (s!!.isNotEmpty()) {
            val pairArrayList = ArrayList<Pair<Int, String>>()
            val list = ArrayList<String>()
            for (item in MainActivity.products) {
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
        }else {
            if(recycler.adapter != pD){
                recycler.adapter = ProductsAdapter(requireContext(), prodList, idd)}
        }
    }

    fun searchString(chtoIshem: String, gdeIshem: String): Int {
        val d = 256
        val q = 101
        var h = 1
        var i: Int
        var j: Int
        var p = 0
        var t = 0
        val M = chtoIshem.length
        val N = gdeIshem.length
        if (M <= N) {
            i = 0
            while (i < M - 1) {
                h = h * d % q
                ++i
            }
            i = 0
            while (i < M) {
                p = (d * p + chtoIshem[i].code) % q
                t = (d * t + gdeIshem[i].code) % q
                ++i
            }
            i = 0
            while (i <= N - M) {
                if (p == t) {
                    j = 0
                    while (j < M) {
                        if (gdeIshem[i + j] != chtoIshem[j]) break
                        ++j
                    }
                    if (j == M) return i
                }
                if (i < N - M) {
                    t = (d * (t - gdeIshem[i].code * h) + gdeIshem[i + M].code) % q
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
            .subscribe{
                search(it.queryText.toString(), arguments?.getInt("prodCat"))
            }

        super.onCreateOptionsMenu(menu, inflater)
    }


}