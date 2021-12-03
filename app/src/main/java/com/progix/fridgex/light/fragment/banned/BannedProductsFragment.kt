package com.progix.fridgex.light.fragment.banned

import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.adapter.banned.BannedProductsAdapter
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface
import kotlinx.coroutines.*


class BannedProductsFragment : Fragment(R.layout.fragment_banned_products), ActionModeInterface {

    private var job: Job? = null
    var adapter: BannedProductsAdapter? = null
    var loading: CircularProgressIndicator? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        prodRecycler = v.findViewById(R.id.bannedProductsRecycler)
        prodAnno = v.findViewById(R.id.annotationCard)
        loading = v.findViewById(R.id.loading)
        recreateList()
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

            delay(300)
        }

    companion object {
        var prodRecycler: RecyclerView? = null
        var prodAnno: MaterialCardView? = null
        var productsList: ArrayList<Pair<String, String>>? = null
    }

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.attachAdapter(adapter!!, 7)
        if (MainActivity.actionMode == null) MainActivity.actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) MainActivity.actionMode?.title = "$count"
        else MainActivity.actionMode?.finish()
    }

    fun recreateList() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (productsList == null) startCoroutine()

            loading?.visibility = View.GONE
            prodAnno = requireView().findViewById(R.id.annotationCard)
            prodRecycler = requireView().findViewById(R.id.bannedProductsRecycler)
            if (productsList!!.isNotEmpty()) {
                prodAnno?.visibility = View.GONE
                prodRecycler?.visibility = View.VISIBLE
                adapter = BannedProductsAdapter(requireContext(), productsList!!)
                adapter!!.attachInterface(this@BannedProductsFragment)
                prodRecycler?.adapter = adapter
            } else {
                prodAnno?.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.item_animation_fall_down
                    )
                )
                prodAnno?.visibility = View.VISIBLE
                prodRecycler?.visibility = View.GONE
            }
        }
    }
}