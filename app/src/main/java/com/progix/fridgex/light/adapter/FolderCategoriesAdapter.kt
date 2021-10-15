package com.progix.fridgex.light.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R


class FolderCategoriesAdapter(
    var context: Context,
    var recipeList: ArrayList<Pair<Int, String>>,
    var onClickListener: OnClickListener,
    var list: ArrayList<Int>
) : RecyclerView.Adapter<FolderCategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageResource(recipeList[position].first)
        holder.category.text = recipeList[position].second

        holder.bind(onClickListener, list[position])

        setAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
        var category: TextView = view.findViewById(R.id.category)

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