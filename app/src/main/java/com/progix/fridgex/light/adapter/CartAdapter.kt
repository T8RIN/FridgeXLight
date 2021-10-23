package com.progix.fridgex.light.adapter

import android.content.Context
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
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.imagesCat
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R


class CartAdapter(var context: Context, var fridgeList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    var cnt = 0

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

        holder.itemView.setOnClickListener {
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

            val bNav =
                (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

            if (crossed) {
                mDb.execSQL(
                    "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                    listOf(fridgeList[position].first).toTypedArray()
                )

                val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                badge.number -= 1
                if (badge.number == 0) bNav.removeBadge(R.id.nav_fridge)

                holder.itemView.alpha = 1f
                holder.name.paintFlags =
                    holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            } else {
                mDb.execSQL(
                    "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                    listOf(fridgeList[position].first).toTypedArray()
                )

                val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                badge.number += 1

                holder.itemView.alpha = 0.5f
                holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            cc.close()
        }
        setAnimation(holder.itemView, position)
        cc.close()
    }

    override fun getItemCount(): Int {
        return fridgeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val star: ImageView = view.findViewById(R.id.star)

        fun clearAnimation() {
            itemView.clearAnimation()
        }
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

}