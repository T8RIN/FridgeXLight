package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.*
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.actionMode
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.fridge.FridgeAdapter
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.helper.interfaces.ActionInterface
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FridgeFragment : Fragment(), ActionInterface {
    private var param1: String? = null
    private var param2: String? = null
    private val fridgeList: ArrayList<Pair<String, String>> = ArrayList()

    private lateinit var loading: CircularProgressIndicator

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            (requireActivity() as MainActivity).bottomSlideUp()
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

    private var dispose: Disposable? = null

    private var adapter: FridgeAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_fridge, container, false)

        recycler = v.findViewById(R.id.fridgeRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)
        loading = v.findViewById(R.id.loading)

        dispose?.dispose()
        dispose = rxJava()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isNotEmpty()) {
                    adapter = FridgeAdapter(requireContext(), it)
                    adapter!!.init(this)
                    recycler.adapter = adapter
                } else {
                    annotationCard.visibility = VISIBLE
                    recycler.visibility = INVISIBLE
                }
                loading.visibility = INVISIBLE
            }, {

            })

        return v
    }


    private fun rxJava(): Observable<ArrayList<Pair<String, String>>> {
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
            cursor.close()
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

        lateinit var recycler: RecyclerView
        lateinit var annotationCard: MaterialCardView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fridge_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                if (recycler.adapter != null) {
                    MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                        .setTitle(getString(R.string.clearFridge))
                        .setPositiveButton(
                            getString(R.string.cont)
                        ) { _, _ ->
                            val bottomNavigationView: BottomNavigationView =
                                requireActivity().findViewById(R.id.bottom_navigation)
                            val layoutParams =
                                bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
                            val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
                            behavior.slideUp(bottomNavigationView)

                            CoroutineScope(Dispatchers.Main).launch {
                                annotationCard.visibility = VISIBLE
                                recycler.visibility = INVISIBLE
                                recycler.adapter = null
                                undoOrDoActionCoroutine("do")
                            }

                            CustomSnackbar(requireContext())
                                .create(
                                    (context as MainActivity).findViewById(R.id.main_root),
                                    getString(R.string.clearSuccessFridge),
                                    LENGTH_LONG
                                )
                                .setAction(getString(R.string.undo)) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        loading.visibility = VISIBLE
                                        annotationCard.visibility = INVISIBLE
                                        undoOrDoActionCoroutine("undo")
                                        recycler.visibility = VISIBLE
                                        adapter = FridgeAdapter(requireContext(), fridgeList)
                                        adapter!!.init(tHis())
                                        recycler.adapter = adapter

                                        behavior.slideUp(bottomNavigationView)

                                        loading.visibility = GONE
                                    }
                                }
                                .show()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.clearErrorFridge),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun tHis(): FridgeFragment {
        return this
    }

    private suspend fun undoOrDoActionCoroutine(param: String) = withContext(Dispatchers.IO) {
        when (param) {
            "do" -> {
                val cursor: Cursor =
                    mDb.rawQuery("SELECT * FROM products WHERE is_in_fridge = 1", null)
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    mDb.execSQL("UPDATE products SET is_in_fridge = 0 WHERE is_in_fridge = 1")
                    cursor.moveToNext()
                }
                cursor.close()
            }
            "undo" -> {
                for (i in fridgeList) {
                    mDb.execSQL(
                        "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                        listOf(i.first).toTypedArray()
                    )
                }
            }
        }
    }

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.init(adapter!!, R.id.nav_fridge)
        if (actionMode == null) actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) actionMode?.title = "$count"
        else actionMode?.finish()
    }

}