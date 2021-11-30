package com.progix.fridgex.light.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.color.ColorPickerAdapter
import com.progix.fridgex.light.data.DataArrays.colorList
import com.progix.fridgex.light.helper.interfaces.ColorPickerInterface

class DialogColor : DialogFragment(R.layout.theme_picker) {

    var colorInterface: ColorPickerInterface? = null

    fun init(colorInterface: ColorPickerInterface){
        this.colorInterface = colorInterface
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.colorRecycler)
        recycler.adapter = ColorPickerAdapter(requireContext(), this, colorList, colorInterface)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

}