package com.progix.fridgex.light.adapter.daily

import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.application.FridgeXLightApplication
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.model.RecipeItem


class DailyAdapter(
    var context: Context,
    var recipeList: ArrayList<RecipeItem>,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return true
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context is FridgeXLightApplication) context =
            (context as FridgeXLightApplication).getCurrentContext()!!

        val displayMetrics = context.resources.displayMetrics
        val dpHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        val k = (dpHeight + 50) / 3 - 271
        val k2 = (dpHeight - 4) / 2 - 271

        rand =
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                ArrayList(listOf(254 + k, 280 + k, 266 + k, 273 + k, 295 + k, 262 + k))
            } else {
                ArrayList(listOf(240 + k2, 220 + k2, 240 + k2, 240 + k2, 220 + k2, 220 + k2))
            }

        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_daily, parent, false)
        )
    }

    private var rand: ArrayList<Int> = ArrayList()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setAnimation(holder.itemView, position)
        Glide.with(context).load(recipeList[position].image).into(holder.image)
        Glide.with(context).load(recipeList[position].indicator).into(holder.indicator)
        holder.recipeName.text = recipeList[position].recipeName
        holder.time.text = recipeList[position].time
        holder.xOfY.text = recipeList[position].xOfY

        val params = holder.itemView.layoutParams
        val random = rand[position]
        val pixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            random.toFloat(),
            context.resources.displayMetrics
        )
        params.height = pixels.toInt()
        holder.itemView.layoutParams = params
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ?",
            listOf(recipeList[position].recipeName).toTypedArray()
        )
        cursor.moveToFirst()
        val starred = cursor.getInt(7) == 1
        val banned = cursor.getInt(14) == 1

        if (starred) holder.star.visibility = VISIBLE
        else holder.star.visibility = GONE
        holder.bind(onClickListener, cursor.getInt(0), position, starred, banned)
        cursor.close()
    }

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
                    notifyItemChanged(position)
                    true
                }
                R.id.de_star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 0 WHERE id = $id")
                    showSnackBar(context.getString(R.string.delStar), id, position, "is_starred", 1)
                    notifyItemChanged(position)
                    true
                }
                R.id.de_ban_recipe -> {
                    mDb.execSQL("UPDATE recipes SET banned = 0 WHERE id = $id")
                    showSnackBar(context.getString(R.string.delBan), id, position, "banned", 1)
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

        CustomSnackbar(context)
            .create(
                (context as MainActivity).findViewById(R.id.main_root),
                text,
                Snackbar.LENGTH_LONG
            )
            .setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE recipes SET $modifier = $value WHERE id = $id")
                notifyItemChanged(position)
            }
            .show()
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

        fun clearAnimation() {
            itemView.clearAnimation()
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
            AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

}