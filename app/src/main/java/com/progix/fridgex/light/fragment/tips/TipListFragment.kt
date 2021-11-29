package com.progix.fridgex.light.fragment.tips

import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.adapter.tips.TipListAdapter

class TipListFragment : Fragment(R.layout.fragment_tip_list) {

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
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tips_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}