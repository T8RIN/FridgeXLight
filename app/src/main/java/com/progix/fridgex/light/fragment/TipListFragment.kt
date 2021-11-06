package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.data.DataArrays.adviceImages

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TipListFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_tip_list, container, false)

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

        return v
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
}