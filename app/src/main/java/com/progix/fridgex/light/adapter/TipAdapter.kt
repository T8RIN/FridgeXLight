package com.progix.fridgex.light.adapter

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
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progix.fridgex.light.MainActivity.Companion.advices
import com.progix.fridgex.light.R


class TipAdapter(
    var context: Context,
    var folderList: ArrayList<Pair<Int, String>>,
    //var onClickListener: OnClickListener,
    var navController: NavController
) : RecyclerView.Adapter<TipAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(advices[folderList[position].first - 1]).into(holder.image)
        holder.category.text = folderList[position].second
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("advice", position)
            holder.image.transitionName = "advice$position"
            val extras = FragmentNavigatorExtras(
                holder.image to holder.image.transitionName
            )
            navController.navigate(R.id.nav_tip_list, bundle, null, extras)
        }
        //holder.bind(onClickListener, position)
        setAnimation(holder.itemView, position)

    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
        val category: TextView = view.findViewById(R.id.category)

        fun clearAnimation() {
            itemView.clearAnimation()
        }

//        fun bind(
//            onClickListener: OnClickListener,
//            id: Int
//        ) {
//            itemView.setOnClickListener {
//                image.transitionName = "advice$id"
//                onClickListener.onClick(image, id)
//            }
//        }
    }

//    class OnClickListener(val clickListener: (ImageView, Int) -> Unit) {
//        fun onClick(
//            image: ImageView,
//            id: Int
//        ) = clickListener(image, id)
//    }

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