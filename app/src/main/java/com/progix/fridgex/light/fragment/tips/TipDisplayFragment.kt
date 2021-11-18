package com.progix.fridgex.light.fragment.tips

import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.data.DataArrays.adviceImages

class TipDisplayFragment : Fragment(R.layout.fragment_tip_display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        val transform =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        transform.duration = 400
        sharedElementEnterTransition = transform

        sharedElementReturnTransition = transform
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val id: Int = arguments?.get("advice") as Int

        val image = v.findViewById<ImageView>(R.id.image)
        image.setImageResource(adviceImages[id])
        image.transitionName = "advice$id"

        val cursor: Cursor = MainActivity.mDb.rawQuery(
            "SELECT * FROM advices WHERE id = ?",
            listOf((id + 1).toString()).toTypedArray()
        )
        cursor.moveToFirst()

        requireActivity().findViewById<Toolbar>(R.id.toolbar).title = cursor.getString(1)

        val text: TextView = v.findViewById(R.id.advice)
        text.text = cursor.getString(2)

        cursor.close()
    }

}