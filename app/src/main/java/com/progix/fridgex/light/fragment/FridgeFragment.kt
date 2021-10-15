package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.View.INVISIBLE
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.MainActivity.Companion.anchor
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.FridgeAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FridgeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var wuv: View
    private val fridgeList: ArrayList<Pair<String, String>> = ArrayList()
    private lateinit var recycler: RecyclerView
    private lateinit var annotationCard: MaterialCardView

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

    var dispose: Disposable? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_fridge, container, false)

        recycler = v.findViewById(R.id.fridgeRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)
        val loading: CircularProgressIndicator = v.findViewById(R.id.loading)

        dispose?.dispose()
        dispose = rxJava()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isNotEmpty()) {
                    recycler.adapter = FridgeAdapter(requireContext(), it)
                } else {
                    annotationCard.visibility = View.VISIBLE
                    recycler.visibility = INVISIBLE
                }
                loading.visibility = INVISIBLE
            }, {

            })

        return v
    }


    private fun rxJava() : Observable<ArrayList<Pair<String, String>>>{
        return Observable.create { item ->
            fridgeList.clear()
            val cursor: Cursor = mDb.rawQuery("SELECT * FROM products WHERE is_in_fridge = 1", null)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                fridgeList.add(Pair(cursor.getString(2), cursor.getString(1)))
                cursor.moveToNext()
            }
            fridgeList.sortBy { it.first }
            item.onNext(fridgeList)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FridgeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fridge_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                    .setTitle(getString(R.string.clearFridge))
                    .setPositiveButton(
                        getString(R.string.cont)
                    ) { _, _ ->
                        val layoutParams = anchor.layoutParams
                        if (layoutParams is CoordinatorLayout.LayoutParams) {
                            val behavior = layoutParams.behavior
                            if (behavior is HideBottomViewOnScrollBehavior<*>) {
                                val hideShowBehavior =
                                    behavior as HideBottomViewOnScrollBehavior<BottomNavigationView>
                                hideShowBehavior.slideUp(anchor)
                            }
                        }
                        annotationCard.visibility = View.VISIBLE
                        recycler.visibility = View.INVISIBLE
                        val cursor: Cursor = mDb.rawQuery("SELECT * FROM products WHERE is_in_fridge = 1", null)
                        cursor.moveToFirst()
                        while(!cursor.isAfterLast){
                            mDb.execSQL("UPDATE products SET is_in_fridge = 0 WHERE is_in_fridge = 1")
                            cursor.moveToNext()
                        }
                        Snackbar.make(
                            requireContext(),
                            requireView(),
                            getString(R.string.clearSuccessFridge),
                            LENGTH_LONG
                        )
                            .setActionTextColor(getColor(requireContext(), R.color.checked))
                            .setAction(getString(R.string.cancel)) {
                                //TODO: clearing fridge and undo and error if it empty already
                                for (i in fridgeList){
                                    mDb.execSQL("UPDATE products SET is_in_fridge = 1 WHERE product = ?", listOf(i.first).toTypedArray())
                                }
                                recycler.adapter = FridgeAdapter(requireContext(), fridgeList)
                                annotationCard.visibility = View.INVISIBLE
                                recycler.visibility = View.VISIBLE
                                val layoutParams = anchor.layoutParams
                                if (layoutParams is CoordinatorLayout.LayoutParams) {
                                    val behavior = layoutParams.behavior
                                    if (behavior is HideBottomViewOnScrollBehavior<*>) {
                                        val hideShowBehavior =
                                            behavior as HideBottomViewOnScrollBehavior<BottomNavigationView>
                                        hideShowBehavior.slideUp(anchor)
                                    }
                                }
                            }
                            .setAnchorView(anchor)
                            .show()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}