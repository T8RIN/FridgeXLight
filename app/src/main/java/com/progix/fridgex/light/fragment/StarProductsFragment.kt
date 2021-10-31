package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.StarProductsAdapter
import com.progix.fridgex.light.helper.ActionInterface
import com.progix.fridgex.light.helper.ActionModeCallback
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StarProductsFragment : Fragment(), ActionInterface {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var job: Job? = null
    var adapter: StarProductsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_star_products, container, false)

        val recycler: RecyclerView = v.findViewById(R.id.starProductsRecycler)
        prodRecycler = recycler
        val annotationCard: MaterialCardView = v.findViewById(R.id.annotationCard)
        prodAnno = annotationCard

        val loading: CircularProgressIndicator = v.findViewById(R.id.loading)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            startCoroutine()
            loading.visibility = View.GONE
            if (productsList.isNotEmpty()) {
                adapter = StarProductsAdapter(requireContext(), productsList)
                adapter!!.init(tHis())
                recycler.adapter = adapter
            } else {
                annotationCard.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            }
        }

        return v
    }

    private fun tHis(): StarProductsFragment {
        return this
    }

    private suspend fun startCoroutine() =
        withContext(Dispatchers.IO) {
            val pairList: ArrayList<Pair<String, String>> = ArrayList()

            val cursor: Cursor =
                MainActivity.mDb.rawQuery("SELECT * FROM products WHERE is_starred = 1", null)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                pairList.add(Pair(cursor.getString(2), cursor.getString(1)))
                cursor.moveToNext()
            }
            pairList.sortBy { it.first }

            cursor.close()

            productsList = pairList

            @Suppress("BlockingMethodInNonBlockingContext")
            Thread.sleep(200)
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StarProductsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        var prodRecycler: RecyclerView? = null
        var prodAnno: MaterialCardView? = null
        var productsList: ArrayList<Pair<String, String>> = ArrayList()
    }

    override fun actionInterface(size: Int) {
        val callback = ActionModeCallback()
        callback.init(adapter!!, 4)
        if (MainActivity.actionMode == null) MainActivity.actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (size > 0) MainActivity.actionMode?.title = "$size"
        else MainActivity.actionMode?.finish()
    }

}