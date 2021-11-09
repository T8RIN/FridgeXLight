package com.progix.fridgex.light.fragment.measures

import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.measures.MeasureAdapter
import com.progix.fridgex.light.model.MeasureItem
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MeasuresFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_measures, container, false)

        val recycler: RecyclerView = v.findViewById(R.id.measuresRecycler)
        val loading: CircularProgressIndicator = v.findViewById(R.id.loading)

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {

            val measuresList: ArrayList<MeasureItem> = ArrayList()

            v.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.item_animation_fall_down
                )
            )

            suspendFun(measuresList)

            loading.visibility = GONE

            recycler.adapter = MeasureAdapter(requireContext(), measuresList)

        }

        return v
    }

    private suspend fun suspendFun(measuresList: ArrayList<MeasureItem>) =
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MeasuresFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.measures_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}