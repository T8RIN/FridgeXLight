package com.progix.fridgex.light.adapter.recipe

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.AlarmClock.*
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.model.InfoItem


class InfoAdapter(var context: Context, private var infoList: ArrayList<InfoItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)
                )
            }
            else -> {
                ViewHolder2(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_info_7, parent, false)
                )
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (position) {
            7 -> 2
            else -> 1
        }
    }

    override fun onBindViewHolder(holderMain: RecyclerView.ViewHolder, position: Int) {
        when (holderMain.itemViewType) {
            1 -> {
                val holder = holderMain as ViewHolder
                holder.itemView.isClickable = false

                if (position == 0) {
                    holder.itemView.setOnClickListener {
                        MaterialAlertDialogBuilder(context, R.style.modeAlert)
                            .setTitle(context.getString(R.string.timer))
                            .setMessage(context.getString(R.string.setTimer) + SecondActivity.name)
                            .setPositiveButton(
                                context.getString(R.string.cont)
                            ) { _, _ ->
                                val intent = Intent(ACTION_SET_TIMER)
                                intent.putExtra(
                                    EXTRA_LENGTH,
                                    infoList[position].value.split(" ")[0].toInt() * 60
                                )
                                intent.putExtra(EXTRA_SKIP_UI, false)
                                intent.putExtra(EXTRA_MESSAGE, SecondActivity.name)
                                context.startActivity(intent)
                            }
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .show()
                    }
                }

                if (infoList[position].value == "0") holder.itemView.visibility = GONE
                holder.name.text = infoList[position].name
                holder.value.text = infoList[position].value
                holder.image.setImageResource(infoList[position].image)
                if (infoList[position].name == context.getString(R.string.source) && infoList[position].value != "Авторский") {
                    holder.itemView.setOnClickListener {
                        MaterialAlertDialogBuilder(context, R.style.modeAlert)
                            .setTitle(context.getString(R.string.redirect))
                            .setMessage(context.getString(R.string.redirectMessage))
                            .setPositiveButton(
                                context.getString(R.string.cont)
                            ) { _, _ ->
                                val url = "http://" + infoList[position].value
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(browserIntent)
                            }
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .show()
                    }
                } else if (infoList[position].value == "Авторский") {
                    holder.image.setImageResource(R.drawable.ic_round_edit_24)
                }
            }
            2 -> {
                val holder = holderMain as ViewHolder2

                holder.itemView.isClickable = false

                if (position == 0) {
                    holder.itemView.setOnClickListener {
                        MaterialAlertDialogBuilder(context, R.style.modeAlert)
                            .setTitle(context.getString(R.string.timer))
                            .setMessage(context.getString(R.string.setTimer) + SecondActivity.name)
                            .setPositiveButton(
                                context.getString(R.string.cont)
                            ) { _, _ ->
                                val intent = Intent(ACTION_SET_TIMER)
                                intent.putExtra(
                                    EXTRA_LENGTH,
                                    infoList[position].value.split(" ")[0].toInt() * 60
                                )
                                intent.putExtra(EXTRA_SKIP_UI, false)
                                intent.putExtra(EXTRA_MESSAGE, SecondActivity.name)
                                context.startActivity(intent)
                            }
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .show()
                    }
                }

                holder.name.text = infoList[position].name
                holder.value.text = infoList[position].value
                holder.image.setImageResource(infoList[position].image)
                if (infoList[position].name == context.getString(R.string.source) && infoList[position].value != "Авторский") {
                    holder.itemView.setOnClickListener {
                        MaterialAlertDialogBuilder(context, R.style.modeAlert)
                            .setTitle(context.getString(R.string.redirect))
                            .setMessage(context.getString(R.string.redirectMessage))
                            .setPositiveButton(
                                context.getString(R.string.cont)
                            ) { _, _ ->
                                val url = "http://" + infoList[position].value
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(browserIntent)
                            }
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .show()
                    }
                } else if (infoList[position].value == "Авторский") {
                    holder.image.setImageResource(R.drawable.ic_round_edit_24)
                }
                if (position == 7) {
                    val cursor: Cursor = mDb.rawQuery(
                        "SELECT * FROM recipes WHERE id = ?",
                        listOf(infoList[position].value).toTypedArray()
                    )
                    cursor.moveToFirst()
                    val starred = cursor.getInt(7) == 1
                    val banned = cursor.getInt(14) == 1
                    if (starred && banned) {
                        holder.banLayout.visibility = VISIBLE
                        holder.starLayout.visibility = VISIBLE
                        holder.itemView.visibility = VISIBLE
                        holder.value.text = context.getString(R.string.inStarred)
                        holder.value2.text = context.getString(R.string.inBanned)
                    } else if (starred && !banned) {
                        holder.value.text = context.getString(R.string.inStarred)
                        holder.banLayout.visibility = GONE
                        holder.starLayout.visibility = VISIBLE
                        holder.itemView.visibility = VISIBLE
                    } else if (!starred && banned) {
                        holder.starLayout.visibility = GONE
                        holder.banLayout.visibility = VISIBLE
                        holder.itemView.visibility = VISIBLE
                        holder.value2.text = context.getString(R.string.inBanned)
                    } else if (!starred && !banned) holder.itemView.visibility = GONE
                    cursor.close()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val value: TextView = view.findViewById(R.id.value)
        val image: ImageView = view.findViewById(R.id.image)
    }

    inner class ViewHolder2(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val value: TextView = view.findViewById(R.id.value)
        val image: ImageView = view.findViewById(R.id.star)
        val value2: TextView = view.findViewById(R.id.value2)
        val starLayout: LinearLayout = view.findViewById(R.id.star_layout)
        val banLayout: LinearLayout = view.findViewById(R.id.ban_layout)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.setOnClickListener {}
    }

}