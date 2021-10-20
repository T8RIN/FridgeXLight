package com.progix.fridgex.light.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R


class ProductsAdapter(var context: Context, var fridgeList: ArrayList<String>, var id: Int) :
    RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_checkable, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = fridgeList[position].replaceFirstChar(Char::uppercase)
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM products WHERE product = ?",
            listOf(fridgeList[position]).toTypedArray()
        )
        cursor.moveToFirst()
        val isChecked = when (id) {
            R.id.nav_cart -> {
                cursor.getString(4) == "1"
            }
            R.id.nav_fridge -> {
                cursor.getString(3) == "1"
            }
            R.id.nav_banned -> {
                cursor.getString(7) == "1"
            }
            else -> null
        }

        holder.checkbox.isChecked = isChecked!!
        holder.itemView.setOnClickListener {
            when (holder.checkbox.isChecked) {
                true -> {
                    when (id) {
                        R.id.nav_cart -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_cart = 0 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_fridge -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_banned -> {
                            mDb.execSQL(
                                "UPDATE products SET banned = 0 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                    }

                    holder.checkbox.isChecked = false
                }
                false -> {
                    when (id) {
                        R.id.nav_cart -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_cart = 1 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_fridge -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_banned -> {
                            mDb.execSQL(
                                "UPDATE products SET banned = 1 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                    }
                    holder.checkbox.isChecked = true
                }
            }
        }
        holder.checkbox.setOnClickListener {
            when (holder.checkbox.isChecked) {
                true -> {
                    when (id) {
                        R.id.nav_cart -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_cart = 1 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_fridge -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_banned -> {
                            mDb.execSQL(
                                "UPDATE products SET banned = 1 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                    }
                }
                false -> {
                    when (id) {
                        R.id.nav_cart -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_cart = 0 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_fridge -> {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                        R.id.nav_banned -> {
                            mDb.execSQL(
                                "UPDATE products SET banned = 0 WHERE product = ?",
                                listOf(fridgeList[position]).toTypedArray()
                            )
                        }
                    }
                }
            }
        }
        setAnimation(holder.itemView, position)

    }

    override fun getItemCount(): Int {
        return fridgeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
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