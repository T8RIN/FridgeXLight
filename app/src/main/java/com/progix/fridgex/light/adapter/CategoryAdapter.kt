package com.progix.fridgex.light.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.data.DataArrays.productCategoriesImages


class CategoryAdapter(
    var context: Context,
    var fridgeList: ArrayList<String>,
    var onClickListener: OnClickListener
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = fridgeList[position]
        holder.star.visibility = GONE
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(fridgeList[position]).toTypedArray()
        )
        cursor.moveToFirst()
        holder.image.setImageResource(productCategoriesImages[cursor.getInt(0) - 1])
        cursor.close()

        holder.bind(onClickListener, fridgeList[position])
        setAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int {
        return fridgeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val star: ImageView = view.findViewById(R.id.star)
        fun bind(
            onClickListener: OnClickListener,
            text: String
        ) {
            itemView.setOnClickListener {
                onClickListener.onClick(name, text)
            }
        }

        fun clearAnimation() {
            itemView.clearAnimation()
        }

    }

    class OnClickListener(val clickListener: (TextView, String) -> Unit) {
        fun onClick(
            name: TextView,
            text: String
        ) = clickListener(name, text)
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