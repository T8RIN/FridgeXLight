package com.progix.fridgex.light.fragment.dialog


import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadFont
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveFont

class DialogFontFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    private var slider: Slider? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.modeAlert)
                .setTitle(R.string.fontSize)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    if (slider?.value != loadFont(requireContext())) {
                        slider?.value?.let { saveFont(requireContext(), it) }
                        requireActivity().recreate()
                    }
                }

        val view = requireActivity().layoutInflater.inflate(R.layout.font_dialog, null)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text: TextView = view.findViewById(R.id.text)
        slider = view.findViewById(R.id.slider)

        slider?.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            when (value) {
                0.5f -> text.text = getString(R.string.smaller)
                0.75f -> text.text = getString(R.string.small)
                1f -> text.text = getString(R.string.normal)
                1.25f -> text.text = getString(R.string.big)
                1.5f -> text.text = getString(R.string.bigger)
            }
        })

        setTextSizeAndText(
            slider!!,
            text,
            loadFont(requireContext())
        )
    }

    private fun setTextSizeAndText(
        slider: Slider,
        text: TextView,
        float: Float
    ) {
        val charSequence = when (float) {
            0.5f -> getString(R.string.smaller)
            0.75f -> getString(R.string.small)
            1.25f -> getString(R.string.big)
            1.5f -> getString(R.string.bigger)
            else -> getString(R.string.normal)
        }
        slider.value = float
        text.text = charSequence
    }

}