package com.progix.fridgex.light.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.data.DataArrays.productCategoriesImages
import com.progix.fridgex.light.helper.ActionInterface


class StarProductsAdapter(
    var context: Context,
    var starProducstList: ArrayList<Pair<String, String>>
) :
    RecyclerView.Adapter<StarProductsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = starProducstList[position].first
        holder.star.visibility = View.GONE

        holder.name.text = name.replaceFirstChar(Char::uppercase)
        val cursor2: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(starProducstList[position].second).toTypedArray()
        )
        cursor2.moveToFirst()
        holder.image.setImageResource(productCategoriesImages[cursor2.getInt(0) - 1])

        val cursor: Cursor =
            mDb.rawQuery("SELECT * FROM products WHERE product = ?", listOf(name).toTypedArray())
        cursor.moveToFirst()
        val banned = cursor.getInt(7) == 1
        val inCart = cursor.getInt(4) == 1
        val inFridge = cursor.getInt(3) == 1

        holder.bind(cursor.getInt(0), position, inFridge, banned, inCart)
        cursor.close()
        cursor2.close()
        setAnimation(holder.itemView, position)

        (holder.itemView as MaterialCardView).isChecked = selectedIds.contains(name)
    }


    private fun popupMenus(
        view: View,
        id: Int,
        position: Int,
        inFridge: Boolean,
        banned: Boolean,
        inCart: Boolean
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus, inFridge, banned, inCart)
        popupMenus.setOnMenuItemClickListener {
            val bNav =
                (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

            when (it.itemId) {
                R.id.nav_fridge -> {
                    bNav.getOrCreateBadge(R.id.nav_fridge).number += 1
                    mDb.execSQL("UPDATE products SET is_in_fridge = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.addedToFridge),
                        id,
                        position,
                        "is_in_fridge",
                        0
                    )
                    notifyItemChanged(position)
                    true
                }

                1 -> {
                    val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                    badge.number -= 1
                    if (badge.number == 0) bNav.removeBadge(R.id.nav_fridge)
                    mDb.execSQL("UPDATE products SET is_in_fridge = 0 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.deleteFromFridge),
                        id,
                        position,
                        "is_in_fridge",
                        1
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

                R.id.de_ban_recipe -> {
                    mDb.execSQL("UPDATE products SET banned = 0 WHERE id = $id")
                    showSnackBar(context.getString(R.string.delBanProd), id, position, "banned", 1)
                    notifyItemChanged(position)
                    true
                }

                R.id.nav_cart -> {
                    (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
                        .getOrCreateBadge(R.id.nav_cart).number += 1
                    mDb.execSQL("UPDATE products SET is_in_cart = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.addedToCart),
                        id,
                        position,
                        "is_in_cart",
                        0
                    )
                    notifyItemChanged(position)
                    true
                }

                2 -> {
                    val badge = bNav.getOrCreateBadge(R.id.nav_cart)
                    badge.number -= 1
                    if (badge.number == 0) bNav.removeBadge(R.id.nav_cart)
                    mDb.execSQL("UPDATE products SET is_in_cart = 0 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.deleteFromCart),
                        id,
                        position,
                        "is_in_cart",
                        1
                    )
                    notifyItemChanged(position)
                    true
                }

                R.id.clear -> {
                    val tempValue = starProducstList[position]
                    mDb.execSQL(
                        "UPDATE products SET is_starred = 0 WHERE product = ?",
                        listOf(tempValue.first).toTypedArray()
                    )
                    starProducstList.remove(tempValue)
                    notifyItemRemoved(position)
                    CustomSnackbar(context)
                        .create(
                            55,
                            (context as MainActivity).findViewById(R.id.main_root),
                            context.getString(R.string.deletedFromStarred)
                        )
                        .setAction(context.getString(R.string.undo)) {
                            mDb.execSQL(
                                "UPDATE products SET is_starred = 1 WHERE product = ?",
                                listOf(tempValue.first).toTypedArray()
                            )
                            starProducstList.add(position, tempValue)
                            notifyItemInserted(position)
                        }
                        .show()
                    true
                }

                else -> true
            }

        }
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menu, true)
    }

    private fun inflatePopup(
        popupMenus: PopupMenu,
        inFridge: Boolean,
        banned: Boolean,
        inCart: Boolean
    ) {
        if (!inFridge) {
            popupMenus.menu.add(0, R.id.nav_fridge, 0, context.getString(R.string.toFridge))
                ?.setIcon(R.drawable.ic_baseline_kitchen_24)
        } else {
            popupMenus.menu.add(0, 1, 0, context.getString(R.string.deFridge))
                ?.setIcon(R.drawable.ic_baseline_kitchen_24)
        }
        if (!inCart) {
            popupMenus.menu.add(0, R.id.nav_cart, 0, context.getString(R.string.toCart))
                ?.setIcon(R.drawable.ic_baseline_shopping_cart_24)
        } else {
            popupMenus.menu.add(0, 2, 0, context.getString(R.string.deCart))
                ?.setIcon(R.drawable.ic_baseline_shopping_cart_24)
        }
        if (!banned) {
            popupMenus.menu.add(0, R.id.ban_recipe, 0, context.getString(R.string.ban))
                ?.setIcon(R.drawable.ic_baseline_block_24)
        } else {
            popupMenus.menu.add(0, R.id.de_ban_recipe, 0, context.getString(R.string.deBan))
                ?.setIcon(R.drawable.ic_baseline_block_24)
        }

        popupMenus.menu.add(0, R.id.clear, 0, context.getString(R.string.deleteWord))
            ?.setIcon(R.drawable.ic_baseline_delete_24)
    }

    private fun showSnackBar(text: String, id: Int, position: Int, modifier: String, value: Int) {
        CustomSnackbar(context)
            .create(
                55,
                (context as MainActivity).findViewById(R.id.main_root),
                text
            )
            .setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE products SET $modifier = $value WHERE id = $id")
                notifyItemChanged(position)
                if (modifier == "is_in_cart") {
                    val badge =
                        (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
                            .getOrCreateBadge(R.id.nav_cart)
                    badge.number -= 1
                    if (badge.number == 0) (context as MainActivity).findViewById<BottomNavigationView>(
                        R.id.bottom_navigation
                    ).removeBadge(R.id.nav_cart)
                }
            }
            .show()
    }


    override fun getItemCount(): Int {
        return starProducstList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val star: ImageView = view.findViewById(R.id.star)
        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            id: Int,
            position: Int,
            inFridge: Boolean,
            banned: Boolean,
            inCart: Boolean
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
                else popupMenus(it, id, position, inFridge, banned, inCart)
            }
        }

    }

    private var actionInterface: ActionInterface? = null

    fun init(actionInterface: ActionInterface) {
        this.actionInterface = actionInterface
    }

    fun addIDIntoSelectedIds(position: Int) {
        val id = starProducstList[position].first
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

    var tempList: ArrayList<String>? = null

    var tempPositions: ArrayList<Int>? = null

    val selectedPositions: ArrayList<Int> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun doSomeAction(modifier: String) {
        val bottomNav =
            (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (selectedIds.size < 1) return
        when (modifier) {
            "fridge" -> {
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    notifyItemChanged(tempPositions!![i])
                }
                bottomNav.getOrCreateBadge(R.id.nav_fridge).number += tempList!!.size
                CustomSnackbar(context)
                    .create(
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToFridge)
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            notifyItemChanged(tempPositions!![i])
                        }
                        val badge = bottomNav.getOrCreateBadge(R.id.nav_fridge)
                        badge.number -= tempList!!.size
                        if (badge.number == 0) bottomNav.removeBadge(R.id.nav_fridge)
                    }
                    .show()
            }
            "cart" -> {
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET is_in_cart = 1 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    notifyItemChanged(tempPositions!![i])
                }
                bottomNav.getOrCreateBadge(R.id.nav_cart).number += tempList!!.size
                CustomSnackbar(context)
                    .create(
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToCart)
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_in_cart = 0 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            notifyItemChanged(tempPositions!![i])
                        }
                        val badge = bottomNav.getOrCreateBadge(R.id.nav_cart)
                        badge.number -= tempList!!.size
                        if (badge.number == 0) bottomNav.removeBadge(R.id.nav_cart)
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
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToBanList)
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
                    delList.add(starProducstList[i])
                }
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET is_starred = 0 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    val tempPos = starProducstList.indexOf(delList[i])
                    indexes.add(tempPos)
                    starProducstList.remove(delList[i])
                    notifyItemRemoved(tempPos)
                }
                CustomSnackbar(context)
                    .create(
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.deletedFromStarred)
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_starred = 1 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            if (indexes[i] < starProducstList.size) starProducstList.add(
                                indexes[i],
                                delList[i]
                            )
                            else starProducstList.add(delList[i])
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


}