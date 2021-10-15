package com.progix.fridgex.light.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R


class RecipeAdapter(var context: Context, var recipeList: ArrayList<String>) :
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_error, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.prodName.text = recipeList[position]
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var prodName = view.findViewById<TextView>(R.id.name)
    }

}