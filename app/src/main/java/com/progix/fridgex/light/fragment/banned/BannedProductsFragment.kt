package com.progix.fridgex.light.fragment.banned

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.adapter.banned.BannedProductsAdapter
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionInterface
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BannedProductsFragment : Fragment(), ActionInterface {
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
    var adapter: BannedProductsAdapter? = null
    var loading: CircularProgressIndicator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_banned_products, container, false)

        val recycler: RecyclerView = v.findViewById(R.id.bannedProductsRecycler)
        prodRecycler = recycler
        val annotationCard: MaterialCardView = v.findViewById(R.id.annotationCard)
        prodAnno = annotationCard
        val swipeRefresh: SwipeRefreshLayout = v.findViewById(R.id.swipeRefresh)

        loading = v.findViewById(R.id.loading)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            startCoroutine()
            loading?.visibility = View.GONE
            if (productsList.isNotEmpty()) {
                adapter = BannedProductsAdapter(requireContext(), productsList)
                adapter!!.init(tHis())
                recycler.adapter = adapter
            } else {
                annotationCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.item_animation_fall_down
                    )
                )
                annotationCard.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            }
        }

        swipeRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.manualBackground
            )
        )
        swipeRefresh.setColorSchemeResources(R.color.checked, R.color.red, R.color.yellow)
        swipeRefresh.setOnRefreshListener {

            job = CoroutineScope(Dispatchers.Main).launch {
                startCoroutine()
                loading?.visibility = View.GONE
                if (productsList.isNotEmpty()) {
                    adapter = BannedProductsAdapter(requireContext(), productsList)
                    adapter!!.init(tHis())
                    recycler.adapter = adapter
                } else {
                    annotationCard.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.item_animation_fall_down
                        )
                    )
                    annotationCard.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                }
                swipeRefresh.isRefreshing = false
            }
        }

        return v
    }

    private fun tHis(): BannedProductsFragment {
        return this
    }

    private suspend fun startCoroutine() =
        withContext(Dispatchers.IO) {
            val pairList: ArrayList<Pair<String, String>> = ArrayList()

            val cursor: Cursor =
                MainActivity.mDb.rawQuery("SELECT * FROM products WHERE banned = 1", null)
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
            BannedProductsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        var prodRecycler: RecyclerView? = null
        var prodAnno: MaterialCardView? = null
        var productsList: ArrayList<Pair<String, String>> = ArrayList()
    }

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.init(adapter!!, 7)
        if (MainActivity.actionMode == null) MainActivity.actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) MainActivity.actionMode?.title = "$count"
        else MainActivity.actionMode?.finish()
    }


}