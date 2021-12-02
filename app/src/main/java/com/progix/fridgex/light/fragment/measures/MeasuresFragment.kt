package com.progix.fridgex.light.fragment.measures

import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.measures.MeasureAdapter
import com.progix.fridgex.light.model.MeasureItem
import kotlinx.coroutines.*

class MeasuresFragment : Fragment(R.layout.fragment_measures) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

    var job: Job? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val recycler: RecyclerView = v.findViewById(R.id.measuresRecycler)

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            val measuresList: ArrayList<MeasureItem> = ArrayList()
            v.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.item_animation_fall_down
                )
            )
            loadMeasuresListTo(measuresList)
            recycler.adapter = MeasureAdapter(requireContext(), measuresList)
        }
    }

    private suspend fun loadMeasuresListTo(measuresList: ArrayList<MeasureItem>) =
        withContext(Dispatchers.IO) {
            val table: Cursor = mDb.rawQuery("SELECT * FROM measures", null)
            table.moveToFirst()
            while (!table.isAfterLast) {
                measuresList.add(
                    MeasureItem(
                        table.getString(1),
                        table.getString(2),
                        table.getString(3),
                        table.getString(4),
                        table.getString(5),
                        table.getString(6),
                    )
                )
                table.moveToNext()
            }
            table.close()
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.measures_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}