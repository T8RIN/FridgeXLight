package com.progix.fridgex.light.adapter.fridge

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.application.FridgeXLightApplication
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.data.DataArrays.productCategoriesImages
import com.progix.fridgex.light.fragment.fridge.FridgeFragment
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface


class FridgeAdapter(var context: Context, private var fridgeList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<FridgeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context is FridgeXLightApplication) context =
            (context as FridgeXLightApplication).getCurrentContext()!!
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = fridgeList[position].first
        holder.name.text = name.replaceFirstChar(Char::uppercase)
        val cursor2: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(fridgeList[position].second).toTypedArray()
        )
        cursor2.moveToFirst()
        holder.image.setImageResource(productCategoriesImages[cursor2.getInt(0) - 1])

        val cursor: Cursor =
            mDb.rawQuery("SELECT * FROM products WHERE product = ?", listOf(name).toTypedArray())
        cursor.moveToFirst()
        val starred = cursor.getInt(6) == 1
        val banned = cursor.getInt(7) == 1
        val inCart = cursor.getInt(4) == 1

        if (starred) holder.star.visibility = VISIBLE
        else holder.star.visibility = GONE
        holder.bind(cursor.getInt(0), position, starred, banned, inCart)
        cursor.close()
        cursor2.close()
        setAnimation(holder.itemView, position)

        (holder.itemView as MaterialCardView).isChecked = selectedIds.contains(name)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun popupMenus(
        view: View,
        id: Int,
        position: Int,
        starred: Boolean,
        banned: Boolean,
        inCart: Boolean
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus, starred, banned, inCart)
        popupMenus.setOnMenuItemClickListener {
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
                R.id.clear -> {
                    Handler(getMainLooper()).postDelayed({
                        notifyDataSetChanged()
                    }, 500)
                    val tempValue = fridgeList[position]
                    mDb.execSQL(
                        "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                        listOf(tempValue.first).toTypedArray()
                    )
                    fridgeList.remove(tempValue)
                    if (fridgeList.isEmpty()) {
                        FridgeFragment.recycler!!.visibility = GONE
                        FridgeFragment.annotationCard!!.visibility = VISIBLE
                    } else notifyItemRemoved(position)
                    CustomSnackbar(context)
                        .create(
                            (context as MainActivity).findViewById(R.id.main_root),
                            context.getString(R.string.delFridgeProduct),
                            Snackbar.LENGTH_SHORT
                        )
                        .setAction(context.getString(R.string.undo)) {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                                listOf(tempValue.first).toTypedArray()
                            )
                            FridgeFragment.recycler!!.visibility = VISIBLE
                            FridgeFragment.annotationCard!!.visibility = GONE
                            if (fridgeList.isNotEmpty()) notifyItemInserted(position)
                            fridgeList.add(position, tempValue)
                        }
                        .show()
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
        banned: Boolean,
        inCart: Boolean
    ) {
        if (!inCart) popupMenus.menu.add(0, R.id.nav_cart, 0, context.getString(R.string.toCart))
            ?.setIcon(R.drawable.ic_baseline_shopping_cart_24)
        if (!starred && !banned) popupMenus.inflate(R.menu.popup_menu_empty)
        else if (!starred && banned) popupMenus.inflate(R.menu.popup_menu_banned)
        else if (starred && !banned) popupMenus.inflate(R.menu.popup_menu_starred)
        else popupMenus.inflate(R.menu.popup_menu_both)
        popupMenus.menu.add(0, R.id.clear, 0, context.getString(R.string.deleteWord))
            ?.setIcon(R.drawable.ic_baseline_delete_24)

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
        return fridgeList.size
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
            starred: Boolean,
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
                else popupMenus(it, id, position, starred, banned, inCart)
            }
        }

    }

    private var actionModeInterface: ActionModeInterface? = null

    fun attachInterface(actionModeInterface: ActionModeInterface) {
        this.actionModeInterface = actionModeInterface
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
        actionModeInterface?.onSelectedItemsCountChanged(selectedIds.size)
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
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addToCart),
                        Snackbar.LENGTH_SHORT
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
                val layoutParams =
                    bottomNav.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
                behavior.slideUp(bottomNav)

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
                    if (fridgeList.isEmpty()) {
                        FridgeFragment.recycler!!.visibility = GONE
                        FridgeFragment.annotationCard!!.visibility = VISIBLE
                    } else notifyItemRemoved(tempPos)
                }
                Handler(getMainLooper()).postDelayed({
                    notifyDataSetChanged()
                }, 500)
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.deleteFromFridge),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        behavior.slideUp(bottomNav)
                        FridgeFragment.recycler!!.visibility = VISIBLE
                        FridgeFragment.annotationCard!!.visibility = GONE
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