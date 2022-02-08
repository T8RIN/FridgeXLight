package com.progix.fridgex.light.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.ThirdActivity.Companion.thirdContext
import com.progix.fridgex.light.adapter.dialog.DialogListProductsAdapter
import com.progix.fridgex.light.adapter.dialog.DialogSearchProductsAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.allHints
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.allProducts
import com.progix.fridgex.light.functions.Functions.searchString
import com.progix.fridgex.light.helper.interfaces.DialogAdapterInterface
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class DialogProductsFragment : DialogFragment(R.layout.fragment_dialog_products) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        if (adapterListNames == null) {
            adapterListNames = ArrayList()
            adapterListValues = ArrayList()
        }
    }

    private var adapterSearch: DialogSearchProductsAdapter? = null
    var adapterList: DialogListProductsAdapter? = null
    var recycler: RecyclerView? = null
    var annotationCard: MaterialCardView? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        recycler = v.findViewById(R.id.dialogProductsRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)


        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.folder_menu)
        val myActionMenuItem = toolbar.menu.findItem(R.id.search_search)
        val searchView = myActionMenuItem.actionView as SearchView
        searchView.queryTextChangeEvents()
            .debounce(350, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onAdapterNeedsToBeInit()
                if (it.queryText.toString().isEmpty()) {
                    recycler!!.adapter = adapterList
                } else {
                    recycler?.visibility = VISIBLE
                    annotationCard?.visibility = GONE
                    adapterSearch = search(it.queryText.toString())
                    recycler!!.adapter = adapterSearch
                }
            }
    }

    private fun search(s: String): DialogSearchProductsAdapter {
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
            list.add(item.second.replaceFirstChar { it.titlecase() })
        }
        return DialogSearchProductsAdapter(thirdContext!!, list)
    }

    companion object {
        var adapterListValues: ArrayList<Pair<String, String>>? = null
        var adapterListNames: ArrayList<String>? = null

        var dialogAdapterInterface: DialogAdapterInterface? = null
        fun attachInterface(dialogAdapterInterface: DialogAdapterInterface) {
            this.dialogAdapterInterface = dialogAdapterInterface
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    private fun onAdapterNeedsToBeInit() {
        val hintList = ArrayList<String>()
        for (item in adapterListNames!!) {
            hintList.add(allHints!![allProducts!!.indexOf(item.lowercase())])
        }
        adapterList = DialogListProductsAdapter(thirdContext!!, adapterListNames!!, hintList)

        if (adapterListNames!!.isEmpty()) {
            recycler?.visibility = GONE
            annotationCard?.visibility = VISIBLE
        } else {
            recycler?.visibility = VISIBLE
            annotationCard?.visibility = GONE
        }

    }

}