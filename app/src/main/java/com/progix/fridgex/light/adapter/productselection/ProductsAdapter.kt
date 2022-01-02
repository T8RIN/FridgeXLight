package com.progix.fridgex.light.adapter.productselection

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.custom.CustomSnackbar


class ProductsAdapter(
    var context: Context,
    private var fridgeList: ArrayList<String>,
    var id: Int
) :
    RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_checkable, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = fridgeList[position].replaceFirstChar(Char::uppercase)
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM products WHERE product = ?",
            listOf(fridgeList[position]).toTypedArray()
        )
        cursor.moveToFirst()

        val starred = cursor.getInt(6) == 1
        val banned = cursor.getInt(7) == 1

        if (starred) holder.star.visibility = View.VISIBLE
        else holder.star.visibility = View.GONE
        holder.bind(cursor.getInt(0), position, starred, banned)

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
        cursor.close()
        setAnimation(holder.itemView, position)

    }

    private fun popupMenus(
        view: View,
        id: Int,
        position: Int,
        starred: Boolean,
        banned: Boolean
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus, starred, banned)
        popupMenus.setOnMenuItemClickListener {
            val bNav =
                (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
            when (it.itemId) {
                R.id.star_recipe -> {
                    mDb.execSQL("UPDATE products SET is_starred = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.addedToStarred),
                        id,
                        position,
                        "is_starred",
                        0
                    )
                    notifyItemChanged(position)
                    true
                }
                R.id.ban_recipe -> {
                    mDb.execSQL("UPDATE products SET banned = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.addedToBanList),
                        id,
                        position,
                        "banned",
                        0
                    )
                    notifyItemChanged(position)
                    true
                }
                R.id.de_star_recipe -> {
                    mDb.execSQL("UPDATE products SET is_starred = 0 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.delStarProd),
                        id,
                        position,
                        "is_starred",
                        1
                    )
                    notifyItemChanged(position)
                    true
                }
                R.id.de_ban_recipe -> {
                    mDb.execSQL("UPDATE products SET banned = 0 WHERE id = $id")
                    showSnackBar(context.getString(R.string.delBanProd), id, position, "banned", 1)
                    notifyItemChanged(position)
                    true
                }
                R.id.nav_fridge -> {
                    bNav.getOrCreateBadge(R.id.nav_fridge).number += 1
                    mDb.execSQL("UPDATE products SET is_in_fridge = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.delFridgeProduct),
                        id,
                        position,
                        "is_in_fridge",
                        0
                    )
                    notifyItemChanged(position)
                    true
                }
                R.id.nav_cart -> {
                    bNav.getOrCreateBadge(R.id.nav_cart).number += 1
                    mDb.execSQL("UPDATE products SET is_in_cart = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.delFridgeProduct),
                        id,
                        position,
                        "is_in_cart",
                        0
                    )
                    notifyItemChanged(position)
                    true
                }
                else -> true
            }

        }
        popupMenus.setForceShowIcon(true)
        popupMenus.show()
    }

    private fun inflatePopup(
        popupMenus: PopupMenu,
        starred: Boolean,
        banned: Boolean
    ) {
        if (!starred && !banned) popupMenus.inflate(R.menu.popup_menu_empty)
        else if (!starred && banned) popupMenus.inflate(R.menu.popup_menu_banned)
        else if (starred && !banned) popupMenus.inflate(R.menu.popup_menu_starred)
        else popupMenus.inflate(R.menu.popup_menu_both)

    }

    private fun showSnackBar(text: String, id: Int, position: Int, modifier: String, value: Int) {
        CustomSnackbar(context)
            .create(
                (context as MainActivity).findViewById(R.id.main_root),
                text,
                Snackbar.LENGTH_SHORT
            )
            .setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE products SET $modifier = $value WHERE id = $id")
                notifyItemChanged(position)
                if (modifier == "is_in_fridge") {
                    val badge =
                        (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
                            .getOrCreateBadge(R.id.nav_fridge)
                    badge.number -= 1
                    if (badge.number == 0) (context as MainActivity).findViewById<BottomNavigationView>(
                        R.id.bottom_navigation
                    ).removeBadge(R.id.nav_fridge)
                }
            }
            .show()
    }


    override fun getItemCount(): Int {
        return fridgeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
        val star: ImageView = view.findViewById(R.id.star)
        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            id: Int,
            position: Int,
            starred: Boolean,
            banned: Boolean
        ) {
            itemView.setOnLongClickListener {
                popupMenus(it, id, position, starred, banned)
                true
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