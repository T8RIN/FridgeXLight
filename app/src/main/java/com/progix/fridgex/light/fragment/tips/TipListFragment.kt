package com.progix.fridgex.light.fragment.tips

import android.database.Cursor
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.adapter.tips.TipListAdapter


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TipListFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tip_list, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = v.findViewById(R.id.tipRecycler)
        val tipList: ArrayList<Pair<Int, String>> = ArrayList()
        val cursor: Cursor = MainActivity.mDb.rawQuery("SELECT * FROM advices", null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            tipList.add(Pair(cursor.getString(0).toInt(), cursor.getString(1)))
            cursor.moveToNext()
        }
        cursor.close()
        val adapter = TipListAdapter(requireContext(), tipList, findNavController())
        recyclerView.apply {
            this.adapter = adapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }


    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TipListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tips_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}