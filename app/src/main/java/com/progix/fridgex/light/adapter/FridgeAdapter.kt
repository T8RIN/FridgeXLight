package com.progix.fridgex.light.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.MainActivity.Companion.imagesCat
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R


class FridgeAdapter(var context: Context, var fridgeList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<FridgeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = fridgeList[position].first.replaceFirstChar(Char::uppercase)
        val cursor: Cursor = mDb.rawQuery("SELECT * FROM categories WHERE category = ?", listOf(fridgeList[position].second).toTypedArray())
        cursor.moveToFirst()
        holder.image.setImageResource(imagesCat[cursor.getInt(0) - 1])
        cursor.close()
        setAnimation(holder.itemView, position)

    }

    override fun getItemCount(): Int {
        return fridgeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        fun clearAnimation() {
            itemView.clearAnimation()
        }
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