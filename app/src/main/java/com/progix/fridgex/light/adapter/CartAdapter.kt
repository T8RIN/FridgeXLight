package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.imagesCat
import com.progix.fridgex.light.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.helper.ActionInterface


class CartAdapter(var context: Context, var fridgeList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.star.visibility = GONE
        holder.name.text = fridgeList[position].first.replaceFirstChar(Char::uppercase)
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(fridgeList[position].second).toTypedArray()
        )
        cursor.moveToFirst()
        holder.image.setImageResource(imagesCat[cursor.getInt(0) - 1])
        cursor.close()
        val cc: Cursor = mDb.rawQuery(
            "SELECT * FROM products WHERE product = ?",
            listOf(fridgeList[position].first).toTypedArray()
        )
        cc.moveToFirst()
        val crossed = cc.getString(5) == "1"
        if (crossed) {
            holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.5f
        } else {
            holder.itemView.alpha = 1f
            holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        setAnimation(holder.itemView, position)
        holder.bind(position)
        (holder.itemView as MaterialCardView).isChecked =
            selectedIds.contains(fridgeList[position].first)
        cc.close()
    }

    override fun getItemCount(): Int {
        return fridgeList.size
    }

    var tempList: ArrayList<String>? = null

    var tempPositions: ArrayList<Int>? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val star: ImageView = view.findViewById(R.id.star)

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            position: Int
        ) {
            itemView.setOnLongClickListener {
                if (!isMultiSelectOn) {
                    isMultiSelectOn = true
                    addIDIntoSelectedIds(position)
                }
                true
            }
            itemView.setOnClickListener {
                if (isMultiSelectOn) addIDIntoSelectedIds(position)
                else {
                    val cc: Cursor = mDb.rawQuery(
                        "SELECT * FROM products WHERE product = ?",
                        listOf(fridgeList[position].first).toTypedArray()
                    )
                    cc.moveToFirst()
                    val crossed = cc.getString(5) == "1"
                    mDb.execSQL(
                        "UPDATE products SET amount = ? WHERE product = ?",
                        listOf(!crossed, fridgeList[position].first).toTypedArray()
                    )

                    val addToFridge = loadCartMode() == 1
                    val bNav =
                        (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

                    if (crossed) {
                        if(addToFridge){
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                                listOf(fridgeList[position].first).toTypedArray()
                            )

                            val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                            badge.number -= 1
                            if (badge.number == 0) bNav.removeBadge(R.id.nav_fridge)

                        }
                        itemView.alpha = 1f
                        name.paintFlags =
                            name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    } else {
                        if(addToFridge){
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                                listOf(fridgeList[position].first).toTypedArray()
                            )

                            val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                            badge.number += 1
                        }

                        itemView.alpha = 0.5f
                        name.paintFlags = name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    cc.close()
                }
            }
        }
    }

    var actionInterface: ActionInterface? = null

    fun init(actionInterface: ActionInterface) {
        this.actionInterface = actionInterface
    }

    fun addIDIntoSelectedIds(position: Int) {
        val id = fridgeList[position].first
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
            selectedPositions.remove(position)
        } else {
            selectedIds.add(id)
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        if (selectedIds.size < 1) isMultiSelectOn = false
        actionInterface?.actionInterface(selectedIds.size)
    }

    val selectedIds: ArrayList<String> = ArrayList()

    val selectedPositions: ArrayList<Int> = ArrayList()

    fun doSomeAction(modifier: String) {
        if (selectedIds.size < 1) return
        when (modifier) {
            "share" -> {
                var sharing = ""

                sharing += context.getString(R.string.shopping)
                sharing += "\n\n"
                sharing += context.getString(R.string.buy)
                sharing += "\n"
                for (i in tempList!!) {
                    sharing += i.replaceFirstChar { it.titlecase() }
                    sharing += "\n"
                }

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    sharing + "\n" + context.getString(R.string.copiedFrom)
                )

                sendIntent.type = "text/plain"
                context.startActivity(
                    Intent.createChooser(
                        sendIntent,
                        context.getString(R.string.share)
                    )
                )
            }
            "star" -> {
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET is_starred = 1 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    notifyItemChanged(tempPositions!![i])
                }
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToStarred),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_starred = 0 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            notifyItemChanged(tempPositions!![i])
                        }
                    }
                    .show()
            }
            "ban" -> {
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET banned = 1 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    notifyItemChanged(tempPositions!![i])
                }
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToBanList),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET banned = 0 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            notifyItemChanged(tempPositions!![i])
                        }
                    }
                    .show()
            }
            "delete" -> {
                val delList: ArrayList<Pair<String, String>> = ArrayList()
                val indexes: ArrayList<Int> = ArrayList()
                for (i in tempPositions!!) {
                    delList.add(fridgeList[i])
                }
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    val tempPos = fridgeList.indexOf(delList[i])
                    indexes.add(tempPos)
                    fridgeList.remove(delList[i])
                    notifyItemRemoved(tempPos)
                }
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToBanList),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            if (indexes[i] < fridgeList.size) fridgeList.add(indexes[i], delList[i])
                            else fridgeList.add(delList[i])
                        }
                        notifyDataSetChanged()
                    }
                    .show()
            }
        }
        isMultiSelectOn = false
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation: Animation =
            loadAnimation(context, R.anim.enter_fade_through)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return true
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    private fun loadCartMode(): Int {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("cartMode", 1)
    }

}