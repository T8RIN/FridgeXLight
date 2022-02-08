package com.progix.fridgex.light.fragment.fridge

import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.animation.AnimationUtils
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
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.appContext
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface
import kotlinx.coroutines.*

class FridgeFragment : Fragment(R.layout.fragment_fridge), ActionModeInterface {
    private val fridgeList: ArrayList<Pair<String, String>> = ArrayList()

    private lateinit var loading: CircularProgressIndicator


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

    private var job: Job? = null

    private var adapter: FridgeAdapter? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val privateRecycler: RecyclerView = v.findViewById(R.id.fridgeRecycler)
        recycler = privateRecycler
        val privateAnno: MaterialCardView = v.findViewById(R.id.annotationCard)
        annotationCard = privateAnno
        loading = v.findViewById(R.id.loading)

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            getList()
            if (fridgeList.isNotEmpty()) {
                adapter = FridgeAdapter(appContext, fridgeList)
                adapter!!.attachInterface(this@FridgeFragment)
                privateRecycler.adapter = adapter
            } else {
                privateAnno.startAnimation(
                    AnimationUtils.loadAnimation(
                        appContext,
                        R.anim.item_animation_fall_down
                    )
                )
                privateAnno.visibility = VISIBLE
                privateRecycler.visibility = GONE
            }
            loading.visibility = GONE
        }
    }

    private suspend fun getList() = withContext(Dispatchers.IO) {
        fridgeList.clear()
        val cursor: Cursor = mDb.rawQuery("SELECT * FROM products WHERE is_in_fridge = 1", null)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {
            fridgeList.add(Pair(cursor.getString(2), cursor.getString(1)))
            cursor.moveToNext()
        }
        fridgeList.sortBy { it.first }
        cursor.close()
        delay(400)
    }


    companion object {
        var recycler: RecyclerView? = null
        var annotationCard: MaterialCardView? = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fridge_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                if (recycler?.adapter != null) {
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
                                annotationCard!!.startAnimation(
                                    AnimationUtils.loadAnimation(
                                        requireContext(),
                                        R.anim.item_animation_fall_down
                                    )
                                )
                                annotationCard!!.visibility = VISIBLE
                                recycler!!.visibility = INVISIBLE
                                recycler!!.adapter = null
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
                                        annotationCard!!.visibility = INVISIBLE
                                        undoOrDoActionCoroutine("undo")
                                        recycler!!.visibility = VISIBLE
                                        adapter = FridgeAdapter(requireContext(), fridgeList)
                                        adapter!!.attachInterface(this@FridgeFragment)
                                        recycler!!.adapter = adapter

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
        callback.attachAdapter(adapter!!, R.id.nav_fridge)
        if (actionMode == null) actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) actionMode?.title = "$count"
        else actionMode?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler = null
        annotationCard = null
    }

}