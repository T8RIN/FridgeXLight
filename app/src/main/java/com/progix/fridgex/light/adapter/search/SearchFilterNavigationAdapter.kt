package com.progix.fridgex.light.adapter.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.application.FridgeXLightApplication
import com.progix.fridgex.light.extensions.Extensions.getAttrColor
import com.progix.fridgex.light.model.NavItem


class SearchFilterNavigationAdapter(
    var context: Context,
    var navList: ArrayList<NavItem>,
    var onClickListener: OnClickListener
) :
    RecyclerView.Adapter<SearchFilterNavigationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context is FridgeXLightApplication) context =
            (context as FridgeXLightApplication).getCurrentContext()!!
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_navigation, parent, false)
        )
    }

    var mSelectedItem = 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.radioButton.isChecked = position == mSelectedItem
        if (holder.radioButton.isChecked) {
            holder.name.setTextColor(context.getAttrColor(R.attr.checked))
            holder.image.setColorFilter(context.getAttrColor(R.attr.checked))
        } else {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.unchecked))
            holder.image.setColorFilter(ContextCompat.getColor(context, R.color.unchecked))
        }
        holder.name.text = navList[position].name
        holder.image.setImageResource(navList[position].image)
        holder.bind(onClickListener)
    }

    override fun getItemCount(): Int {
        return navList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val radioButton: RadioButton = view.findViewById(R.id.radioButton)
        fun bind(clicker: OnClickListener) {
            itemView.setOnClickListener {
                mSelectedItem = bindingAdapterPosition
                notifyItemRangeChanged(0, navList.size)
                clicker.onClick(mSelectedItem)
            }
            radioButton.setOnClickListener {
                mSelectedItem = bindingAdapterPosition
                notifyItemRangeChanged(0, navList.size)
                clicker.onClick(mSelectedItem)
            }
        }
    }

    class OnClickListener(val clickListener: (Int) -> Unit) {
        fun onClick(
            id: Int
        ) = clickListener(id)
    }

}