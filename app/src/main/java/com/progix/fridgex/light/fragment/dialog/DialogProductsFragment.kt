package com.progix.fridgex.light.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
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
import com.progix.fridgex.light.activity.MainActivity.Companion.allHints
import com.progix.fridgex.light.activity.MainActivity.Companion.allProducts
import com.progix.fridgex.light.activity.ThirdActivity.Companion.thirdContext
import com.progix.fridgex.light.adapter.dialog.DialogListProductsAdapter
import com.progix.fridgex.light.adapter.dialog.DialogSearchProductsAdapter
import com.progix.fridgex.light.helper.interfaces.DialogAdapterInterface
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DialogProductsFragment : DialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

    private var adapterSearch: DialogSearchProductsAdapter? = null
    private var adapterList: DialogListProductsAdapter? = null
    var recycler: RecyclerView? = null
    var annotationCard: MaterialCardView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_products, container, false)

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

        return v
    }

    private fun search(s: String): DialogSearchProductsAdapter {
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
            list.add(item.second.replaceFirstChar { it.titlecase() })
        }
        return DialogSearchProductsAdapter(thirdContext!!, list)
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DialogProductsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        val adapterListValues: ArrayList<Pair<String, String>> = ArrayList()
        val adapterListNames: ArrayList<String> = ArrayList()

        var dialogAdapterInterface: DialogAdapterInterface? = null
        fun initAdapterInterface(dialogAdapterInterface: DialogAdapterInterface) {
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
        for (item in adapterListNames) {
            hintList.add(allHints[allProducts.indexOf(item.lowercase())])
        }
        adapterList = DialogListProductsAdapter(thirdContext!!, adapterListNames, hintList)

        if (adapterListNames.isEmpty()) {
            recycler?.visibility = GONE
            annotationCard?.visibility = VISIBLE
        } else {
            recycler?.visibility = VISIBLE
            annotationCard?.visibility = GONE
        }

    }

}