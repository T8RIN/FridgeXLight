package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.model.RecyclerSortItem


class FolderRecipesAdapter(
    var context: Context,
    var recipeList: ArrayList<RecyclerSortItem>,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<FolderRecipesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(recipeList[position].recipeItem.image).into(holder.image)
        Glide.with(context).load(recipeList[position].recipeItem.indicator).into(holder.indicator)
        holder.recipeName.text = recipeList[position].recipeItem.recipeName
        holder.time.text = recipeList[position].recipeItem.time
        holder.xOfY.text = recipeList[position].recipeItem.xOfY
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ?",
            listOf(recipeList[position].recipeItem.recipeName).toTypedArray()
        )
        cursor.moveToFirst()
        val banned = cursor.getInt(14) == 1
        val starred = cursor.getInt(7) == 1
        if(banned && starred) holder.image.setTint(R.color.bothT)
        else if(banned && !starred) holder.image.setTint(R.color.redT)
        else if(!banned && starred) holder.image.setTint(R.color.yellowT)
        else holder.image.setTint(null)
        holder.bind(onClickListener, cursor.getInt(0))
        setAnimation(holder.itemView, position)
    }

    private fun ImageView.setTint(@ColorRes colorRes: Int?) {
        if(colorRes != null) {
            ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.SRC_ATOP);
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
            )
        }
        else ImageViewCompat.setImageTintList(this, null)
    }


    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
        var recipeName: TextView = view.findViewById(R.id.recipeName)
        var indicator: ImageView = view.findViewById(R.id.indicator)
        var xOfY: TextView = view.findViewById(R.id.x_y)
        var time: TextView = view.findViewById(R.id.time)

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            onClickListener: OnClickListener,
            id: Int
        ) {
            itemView.setOnClickListener {
                onClickListener.onClick(image, id)
            }
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