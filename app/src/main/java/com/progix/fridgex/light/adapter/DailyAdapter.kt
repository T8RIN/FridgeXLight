package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
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
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
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
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_daily, parent, false)

        val displayMetrics = context.resources.displayMetrics
        val dpHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        val k = (dpHeight + 50) / 3 - 271
        val k2 = (dpHeight - 68) / 2 - 271

        rand =
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                ArrayList(listOf(254 + k, 280 + k, 266 + k, 273 + k, 295 + k, 262 + k))
            } else {
                ArrayList(listOf(240 + k2, 220 + k2, 240 + k2, 240 + k2, 220 + k2, 220 + k2))
            }



        return ViewHolder(view)
    }

    private var rand: ArrayList<Int> = ArrayList()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
        setAnimation(holder.itemView, position)
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
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menu, true)
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

//    private fun ImageView.setTint(@ColorRes colorRes: Int?) {
//        if(colorRes != null) {
//            ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.SRC_ATOP);
//            ImageViewCompat.setImageTintList(
//                this,
//                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
//            )
//        }
//        else ImageViewCompat.setImageTintList(this, null)
//    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
        var recipeName: TextView = view.findViewById(R.id.recipeName)
        var indicator: ImageView = view.findViewById(R.id.indicator)
        var xOfY: TextView = view.findViewById(R.id.x_y)
        var time: TextView = view.findViewById(R.id.time)
        var star: ImageView = view.findViewById(R.id.star)

        fun bind(
            onClickListener: OnClickListener,
            id: Int,
            position: Int,
            starred: Boolean,
            banned: Boolean
        ) {
            itemView.setOnClickListener {
                onClickListener.onClick(image, id)
            }
            itemView.setOnLongClickListener {
                popupMenus(it, id, position, starred, banned)
                true
            }
        }

        fun clearAnimation() {
            itemView.clearAnimation()
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
            AnimationUtils.loadAnimation(context, R.anim.enter_fade_through)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

}