package com.progix.fridgex.light.adapter.tips

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progix.fridgex.light.R
import com.progix.fridgex.light.data.DataArrays.adviceImages


class TipListAdapter(
    var context: Context,
    private var tipList: ArrayList<Pair<Int, String>>,
    private var navController: NavController
) : RecyclerView.Adapter<TipListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(adviceImages[tipList[position].first - 1]).into(holder.image)
        holder.category.text = tipList[position].second
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("advice", position)
            holder.image.transitionName = "advice$position"
            val extras = FragmentNavigatorExtras(
                holder.image to holder.image.transitionName
            )
            navController.navigate(R.id.nav_tip_list, bundle, null, extras)
        }
        setAnimation(holder.itemView, position)

    }

    override fun getItemCount(): Int {
        return tipList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val category: TextView = view.findViewById(R.id.category)

        fun clearAnimation() {
            itemView.clearAnimation()
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