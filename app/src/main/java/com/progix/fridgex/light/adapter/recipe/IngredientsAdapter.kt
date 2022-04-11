package com.progix.fridgex.light.adapter.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R


class IngredientsAdapter(
    var context: Context,
    private var ingredientsList: ArrayList<Pair<String, String>>,
    private var missing: ArrayList<Boolean>
) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ingreds, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.prodName.text = ingredientsList[position].first.replaceFirstChar(Char::uppercase)
        holder.amount.text = ingredientsList[position].second
        if (!missing[position]) {
            val color = ContextCompat.getColor(context, R.color.manualGreen)
            holder.prodName.setTextColor(color)
            holder.amount.setTextColor(color)
            holder.dots.setTextColor(color)
        } else {
            val color = ContextCompat.getColor(context, R.color.manualText3)
            holder.prodName.setTextColor(color)
            holder.amount.setTextColor(color)
            holder.dots.setTextColor(color)
        }
    }

    override fun getItemCount(): Int {
        return ingredientsList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val prodName: TextView = view.findViewById(R.id.name)
        val amount: TextView = view.findViewById(R.id.amount)
        val dots: TextView = view.findViewById(R.id.dots)
    }


}