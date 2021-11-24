package com.progix.fridgex.light.adapter.measures

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.model.MeasureItem


class MeasureAdapter(var context: Context, var measuresList: ArrayList<MeasureItem>) :
    RecyclerView.Adapter<MeasureAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.measure_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.product.text = measuresList[position].product
        holder.cup250.text = measuresList[position].cup250
        holder.cup200.text = measuresList[position].cup200
        holder.tbsp.text = measuresList[position].tbsp
        holder.tsp.text = measuresList[position].tsp
        holder.onepcs.text = measuresList[position].onepcs
    }

    override fun getItemCount(): Int {
        return measuresList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val product: TextView = view.findViewById(R.id.product)
        val cup250: TextView = view.findViewById(R.id.cup250)
        val cup200: TextView = view.findViewById(R.id.cup200)
        val tbsp: TextView = view.findViewById(R.id.tbsp)
        val tsp: TextView = view.findViewById(R.id.tsp)
        val onepcs: TextView = view.findViewById(R.id.onepcs)
    }

}