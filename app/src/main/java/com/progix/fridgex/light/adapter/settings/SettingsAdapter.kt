package com.progix.fridgex.light.adapter.settings

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.guide
import com.progix.fridgex.light.activity.MainActivity.Companion.restart
import com.progix.fridgex.light.data.DataArrays.colorList
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadCartMode
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadFont
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadNightMode
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadTheme
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveCartMode
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveNightMode
import com.progix.fridgex.light.fragment.dialog.DialogColorFragment
import com.progix.fridgex.light.fragment.dialog.DialogFontFragment

class SettingsAdapter(
    var context: Context,
    private var settingsList: List<String>,
) :
    RecyclerView.Adapter<SettingsAdapter.SettingsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsHolder {
        val itemView: View = LayoutInflater.from(context)
            .inflate(R.layout.item_settings, parent, false)
        return SettingsHolder(itemView)
    }

    private var checkedItem = 2

    override fun onBindViewHolder(holder: SettingsHolder, position: Int) {
        holder.itemView.isClickable = true
        setAnimation(holder.itemView, position)
        holder.switcher.visibility = GONE
        holder.subText.visibility = GONE
        holder.shapeImage.visibility = GONE
        when (position) {
            0 -> {
                holder.text.text = settingsList[position]
                val temp = loadNightMode(context)
                holder.onOff.text = when (temp) {
                    0 -> context.getString(R.string.on)
                    1 -> context.getString(R.string.off)
                    else -> context.getString(R.string.auto)
                }
                holder.icon.setImageResource(
                    when (temp) {
                        0 -> R.drawable.ic_baseline_dark_mode_24
                        1 -> R.drawable.ic_light_mode_24
                        else -> R.drawable.ic_auto_24
                    }
                )
                checkedItem = temp
                holder.card.setOnClickListener {
                    val listItems = arrayOf(
                        context.getString(R.string.on),
                        context.getString(R.string.off),
                        context.getString(R.string.auto)
                    )
                    MaterialAlertDialogBuilder(context, R.style.modeAlert)
                        .setTitle(context.getString(R.string.nightMode))
                        .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                            if (checkedItem != loadNightMode(context)) {
                                saveNightMode(context, checkedItem)
                                restartActivity()
                            }
                        }
                        .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                            checkedItem = which
                        }
                        .setOnDismissListener { checkedItem = loadNightMode(context) }
                        .show()
                }
            }
            1 -> {
                holder.text.text = settingsList[position]
                holder.icon.setImageResource(R.drawable.ic_baseline_color_lens_24)
                holder.onOff.visibility = GONE
                holder.shapeImage.visibility = VISIBLE

                val pos = when (loadTheme(context)) {
                    "red" -> 0
                    "pnk" -> 1
                    "vlt" -> 2
                    "ble" -> 3
                    "mnt" -> 4
                    "grn" -> 5
                    "yel" -> 6
                    else -> 7
                }

                holder.shapeImage.setStrokeColorResource(colorList[pos].first)
                holder.shapeImage.setImageResource(colorList[pos].second)

                holder.itemView.setOnClickListener {
                    val fragment = DialogColorFragment()
                    if (!fragment.isAdded) fragment.show(
                        (context as MainActivity).supportFragmentManager,
                        "custom"
                    )
                }
            }
            2 -> {
                holder.text.text = settingsList[position]
                holder.icon.setImageResource(R.drawable.ic_baseline_text_fields_24)
                val charSequence = when (loadFont(context)) {
                    0.5f -> context.getString(R.string.smaller)
                    0.75f -> context.getString(R.string.small)
                    1.25f -> context.getString(R.string.big)
                    1.5f -> context.getString(R.string.bigger)
                    else -> context.getString(R.string.normal)
                }
                holder.onOff.text = charSequence

                holder.itemView.setOnClickListener {
                    val dialog = DialogFontFragment()
                    if (!dialog.isAdded) dialog.show(
                        (context as MainActivity).supportFragmentManager,
                        "custom"
                    )
                }
            }
            3 -> {
                holder.switcher.visibility = VISIBLE
                holder.subText.visibility = VISIBLE
                holder.onOff.visibility = GONE

                holder.itemView.isClickable = false

                holder.icon.setImageResource(R.drawable.ic_baseline_shopping_cart_24)
                holder.text.text = settingsList[position]

                val isCartSetting = loadCartMode(context) == 1

                holder.switcher.isChecked = when (isCartSetting) {
                    true -> true
                    else -> false
                }

                holder.subText.text = when (isCartSetting) {
                    true -> context.getString(R.string.addingModeMessage)
                    else -> context.getString(R.string.ignoreMessage)
                }

                holder.switcher.setOnCheckedChangeListener { _, isChecked ->
                    when (isChecked) {
                        true -> {
                            saveCartMode(context, 1)
                            holder.subText.text = context.getString(R.string.addingModeMessage)
                        }
                        else -> {
                            saveCartMode(context, 0)
                            holder.subText.text = context.getString(R.string.ignoreMessage)
                        }
                    }
                }
            }
            4 -> {
                holder.icon.setImageResource(R.drawable.ic_baseline_info_24)
                holder.text.text = settingsList[position]
                holder.onOff.visibility = GONE
                holder.card.setOnClickListener {
                    guide = true
                    (context as MainActivity).finishAffinity()
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            }
        }
    }

    private fun restartActivity() {
        restart = true
        (context as MainActivity).recreate()
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    class SettingsHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val onOff: TextView = itemView.findViewById(R.id.switcher)
        val card: View = itemView
        val subText: TextView = itemView.findViewById(R.id.subtext)
        val switcher: SwitchMaterial = itemView.findViewById(R.id.switcher_switch)
        val shapeImage: ShapeableImageView = itemView.findViewById(R.id.shapeImage)

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

    override fun onFailedToRecycleView(holder: SettingsHolder): Boolean {
        return true
    }

    override fun onViewDetachedFromWindow(holder: SettingsHolder) {
        holder.clearAnimation()
    }

}