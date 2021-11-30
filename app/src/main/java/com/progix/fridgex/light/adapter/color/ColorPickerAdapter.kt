package com.progix.fridgex.light.adapter.color

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.data.DataArrays.colorNames
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadTheme
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveTheme
import com.progix.fridgex.light.helper.interfaces.ColorPickerInterface


class ColorPickerAdapter(
    var context: Context,
    val fragment: DialogFragment,
    private var colorList: List<Pair<Int, Int>>,
    private val colorInterface: ColorPickerInterface?
) :
    RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.color_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setStrokeColorResource(colorList[position].first)
        holder.image.setImageResource(colorList[position].second)
        holder.text.text = colorNames[position]
        val amplifier = when (position) {
            0 -> "red"
            1 -> "pnk"
            2 -> "vlt"
            3 -> "ble"
            4 -> "mnt"
            5 -> "grn"
            6 -> "yel"
            else -> "def"
        }
        (holder.itemView as MaterialCardView).isChecked = amplifier == loadTheme(context)
        holder.image.setOnClickListener {
            if (amplifier != loadTheme(context)) {
                (context as MainActivity).recreate()
                saveTheme(context, amplifier)
                colorInterface?.onPick()
            }
            fragment.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ShapeableImageView = view.findViewById(R.id.shapeImage)
        var text: TextView = view.findViewById(R.id.text)
    }

}