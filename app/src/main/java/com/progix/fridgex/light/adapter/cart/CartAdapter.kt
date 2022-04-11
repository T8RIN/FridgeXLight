package com.progix.fridgex.light.adapter.cart

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
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.application.FridgeXLightApplication
import com.progix.fridgex.light.data.DataArrays.productCategoriesImages
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadCartMode


class CartAdapter(var context: Context, var cartList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context is FridgeXLightApplication) context =
            (context as FridgeXLightApplication).getCurrentContext()!!
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.star.visibility = GONE
        holder.name.text = cartList[position].first.replaceFirstChar(Char::uppercase)
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(cartList[position].second).toTypedArray()
        )
        cursor.moveToFirst()
        holder.image.setImageResource(productCategoriesImages[cursor.getInt(0) - 1])
        cursor.close()
        val cc: Cursor = mDb.rawQuery(
            "SELECT * FROM products WHERE product = ?",
            listOf(cartList[position].first).toTypedArray()
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

        cc.close()
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

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
            itemView.setOnClickListener {
                val products: Cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE product = ?",
                    listOf(cartList[position].first).toTypedArray()
                )
                products.moveToFirst()
                val crossed = products.getString(5) == "1"
                mDb.execSQL(
                    "UPDATE products SET amount = ? WHERE product = ?",
                    listOf(!crossed, cartList[position].first).toTypedArray()
                )

                val addToFridge = loadCartMode(context) == 1
                val bNav =
                    (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

                if (crossed) {
                    if (addToFridge) {
                        mDb.execSQL(
                            "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                            listOf(cartList[position].first).toTypedArray()
                        )

                        val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                        badge.number -= 1
                        if (badge.number == 0) bNav.removeBadge(R.id.nav_fridge)

                    }
                    itemView.alpha = 1f
                    name.paintFlags =
                        name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                } else {
                    if (addToFridge) {
                        mDb.execSQL(
                            "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                            listOf(cartList[position].first).toTypedArray()
                        )

                        val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                        badge.number += 1
                    }

                    itemView.alpha = 0.5f
                    name.paintFlags = name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                products.close()
            }
        }
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation: Animation =
            loadAnimation(context, R.anim.item_animation_fall_down)
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