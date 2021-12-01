package com.progix.fridgex.light.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.color.ColorPickerAdapter
import com.progix.fridgex.light.data.DataArrays.colorList
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveTheme
import com.progix.fridgex.light.helper.interfaces.SettingsInterface

class DialogColor : DialogFragment(), SettingsInterface {

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
        super.onViewCreated(view, savedInstanceState)
        val recycler: RecyclerView = view.findViewById(R.id.colorRecycler)
        recycler.adapter = ColorPickerAdapter(requireContext(), this, colorList, this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder: AlertDialog.Builder =
            AlertDialog.Builder(requireActivity(), R.style.modeAlert)
                .setTitle(R.string.themeChooser)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().recreate()
                    saveTheme(requireContext(), currentColor)
                }

        val view = requireActivity().layoutInflater.inflate(R.layout.theme_picker, null)
        onViewCreated(view, null)
        dialogBuilder.setView(view)
        return dialogBuilder.create()
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

    private var currentColor: String = "def"

    override fun onPickColor(color: String) {
        currentColor = color
    }

}