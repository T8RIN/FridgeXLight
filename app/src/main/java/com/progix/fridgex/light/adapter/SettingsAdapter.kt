package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.guide
import com.progix.fridgex.light.MainActivity.Companion.restart
import com.progix.fridgex.light.R


class SettingsAdapter(var context: Context, var settingsList: List<String>) :
    RecyclerView.Adapter<SettingsAdapter.SettingsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsHolder {
        val itemView: View = LayoutInflater.from(context)
            .inflate(R.layout.item_settings, parent, false)
        return SettingsHolder(itemView)
    }

    private var checkedItem = 3;
    override fun onBindViewHolder(holder: SettingsHolder, position: Int) {
        when (position) {
            0 -> {
                holder.icon.setImageResource(R.drawable.ic_baseline_dark_mode_24)
                holder.text.text = settingsList[position]
                val temp = loadNightMode()
                holder.onOff.text = when (temp) {
                    0 -> context.getString(R.string.on)
                    1 -> context.getString(R.string.off)
                    else -> context.getString(R.string.auto)
                }
                checkedItem = temp
                holder.card.setOnClickListener {
                    val listItems = arrayOf(
                        context.getString(R.string.on),
                        context.getString(R.string.off),
                        context.getString(R.string.auto)
                    )
                    MaterialAlertDialogBuilder(context, R.style.modeAlert)
                        .setTitle(context.getString(R.string.nightMode))
                        .setPositiveButton(context.getString(R.string.ok), null)
                        .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                            checkedItem = which
                            when (checkedItem) {
                                0 -> {
                                    holder.onOff.text = context.getString(R.string.on)
                                    saveNightMode(0)
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                    restart = true
                                }
                                1 -> {
                                    holder.onOff.text = context.getString(R.string.off)
                                    saveNightMode(1)
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                    restart = true
                                }
                                2 -> {
                                    holder.onOff.text = context.getString(R.string.auto)
                                    saveNightMode(2)
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                    restart = true
                                }
                            }
                        }
                        .show()
                }
            }
            1 -> {
                holder.icon.setImageResource(R.drawable.ic_baseline_info_24)
                holder.text.text = settingsList[position]
                holder.onOff.visibility = View.GONE
                holder.card.setOnClickListener {
                    guide = true
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    class SettingsHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.text)
        var icon: ImageView = itemView.findViewById(R.id.icon)
        var onOff: TextView = itemView.findViewById(R.id.switcher)
        var card: View = itemView
    }

    private fun saveNightMode(value: Int) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("mode", value)
        editor?.apply()
    }

    private fun loadNightMode(): Int {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("mode", 2)
    }


}