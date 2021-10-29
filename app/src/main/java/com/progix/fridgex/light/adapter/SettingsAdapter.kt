package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private var checkedItemCart = 1;
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
                        .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
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
                        .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                            checkedItem = which
                        }
                        .setOnDismissListener { checkedItem = loadNightMode() }
                        .show()
                }
            }
            1 -> {
                holder.icon.setImageResource(R.drawable.ic_baseline_shopping_cart_24)
                holder.text.text = settingsList[position]
                holder.onOff.text = when (loadCartMode()) {
                    1 -> context.getString(R.string.addingModeText)
                    else -> context.getString(R.string.ignoreText)
                }
                checkedItemCart = loadCartMode()
                holder.card.setOnClickListener {
                    val listItems = arrayOf(
                        context.getString(R.string.ignoreText),
                        context.getString(R.string.addingModeText)
                    )
                    MaterialAlertDialogBuilder(context, R.style.modeAlert)
                        .setTitle(context.getString(R.string.cartSettings))
                        .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                            when (checkedItem) {
                                1 -> {
                                    holder.onOff.text = context.getString(R.string.addingModeText)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.addingModeMessage),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    saveCartMode(1)
                                }
                                0 -> {
                                    holder.onOff.text = context.getString(R.string.ignoreText)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.ignoreMessage),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    saveCartMode(0)
                                }
                            }
                        }
                        .setSingleChoiceItems(listItems, checkedItemCart) { _, which ->
                            checkedItemCart = which
                            when (checkedItemCart) {
                                1 -> {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.addingModeMessage),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                0 -> {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.ignoreMessage),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        .setOnDismissListener { checkedItemCart = loadCartMode() }
                        .show()
                }
            }
            2 -> {
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

    private fun saveCartMode(value: Int) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("cartMode", value)
        editor?.apply()
    }

    private fun loadCartMode(): Int {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("cartMode", 1)
    }

}