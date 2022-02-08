package com.progix.fridgex.light.adapter.search

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.application.FridgeXLightApplication
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.functions.Functions.delayedAction
import com.progix.fridgex.light.functions.Functions.loadImageFromStorage
import com.progix.fridgex.light.functions.Functions.strToInt
import com.progix.fridgex.light.model.RecyclerSortItem


class SearchAdapter(
    var context: Context,
    var recipeList: ArrayList<RecyclerSortItem>,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context is FridgeXLightApplication) context =
            (context as FridgeXLightApplication).getCurrentContext()!!
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, no: Int) {
        val position = holder.layoutPosition
        if (recipeList[position].recipeItem.image != -1) {
            Glide.with(context).load(recipeList[position].recipeItem.image).into(holder.image)
        } else {
            val id: Int = strToInt(recipeList[position].recipeItem.recipeName)
            Glide.with(context).load(loadImageFromStorage(context, "recipe_$id.png"))
                .into(holder.image)
        }
        Glide.with(context).load(recipeList[position].recipeItem.indicator).into(holder.indicator)
        holder.recipeName.text = recipeList[position].recipeItem.recipeName
        holder.time.text = recipeList[position].recipeItem.time
        holder.xOfY.text = recipeList[position].recipeItem.xOfY
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ?",
            listOf(recipeList[position].recipeItem.recipeName).toTypedArray()
        )
        cursor.moveToFirst()
        val starred = cursor.getInt(7) == 1
        if (starred) holder.star.visibility = View.VISIBLE
        else holder.star.visibility = View.GONE
        holder.bind(onClickListener, cursor.getInt(0), position, starred, false)
        cursor.close()
        setAnimation(holder.itemView, position)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun popupMenus(view: View, id: Int, position: Int, starred: Boolean, banned: Boolean) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus, starred, banned)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 1 WHERE id = $id")
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
                    mDb.execSQL("UPDATE recipes SET banned = 1 WHERE id = $id")
                    showSnackBar(
                        context.getString(R.string.addedToBanList),
                        id,
                        position,
                        "banned",
                        0
                    )
                    recipeList.removeAt(position)
                    notifyItemRemoved(position)
                    delayedAction(500) { notifyDataSetChanged() }
                    if (recipeList.isEmpty()) {
                        (context as MainActivity).findNavController(R.id.nav_host_fragment)
                            .navigate(R.id.nav_search)
                    }
                    true
                }
                R.id.de_star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 0 WHERE id = $id")
                    showSnackBar(context.getString(R.string.delStar), id, position, "is_starred", 1)
                    notifyItemChanged(position)
                    true
                }
                else -> true
            }
        }
        popupMenus.setForceShowIcon(true)
        popupMenus.show()
    }

    private fun inflatePopup(popupMenus: PopupMenu, starred: Boolean, banned: Boolean) {
        if (!starred && !banned) popupMenus.inflate(R.menu.popup_menu_empty)
        else if (!starred && banned) popupMenus.inflate(R.menu.popup_menu_banned)
        else if (starred && !banned) popupMenus.inflate(R.menu.popup_menu_starred)
        else popupMenus.inflate(R.menu.popup_menu_both)

    }

    private fun showSnackBar(text: String, id: Int, position: Int, modifier: String, value: Int) {
        val snackBar = CustomSnackbar(context)
            .create(
                (context as MainActivity).findViewById(R.id.main_root),
                text,
                Snackbar.LENGTH_SHORT
            )
        if (modifier == "is_starred") {
            snackBar.setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE recipes SET $modifier = $value WHERE id = $id")
                notifyItemChanged(position)
            }
        } else {
            snackBar.setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE recipes SET $modifier = $value WHERE id = $id")
                (context as MainActivity).findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.nav_search)
            }
        }
        snackBar.show()
    }


    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val recipeName: TextView = view.findViewById(R.id.recipeName)
        val indicator: ImageView = view.findViewById(R.id.indicator)
        val xOfY: TextView = view.findViewById(R.id.x_y)
        val time: TextView = view.findViewById(R.id.time)
        val star: ImageView = view.findViewById(R.id.star)

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            onClickListener: OnClickListener,
            id: Int,
            position: Int,
            starred: Boolean,
            banned: Boolean
        ) {
            recursiveOnClick(onClickListener, itemView, image, id)
            itemView.setOnLongClickListener {
                popupMenus(it, id, position, starred, banned)
                true
            }
        }
    }

    private fun recursiveOnClick(
        onClickListener: OnClickListener,
        itemView: View,
        image: ImageView,
        id: Int
    ) {
        itemView.setOnClickListener {
            itemView.setOnClickListener {}
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                recursiveOnClick(onClickListener, itemView, image, id)
            }, 1000)
            onClickListener.onClick(image, id)
        }
    }

    class OnClickListener(val clickListener: (ImageView, Int) -> Unit) {
        fun onClick(
            image: ImageView,
            id: Int
        ) = clickListener(image, id)
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