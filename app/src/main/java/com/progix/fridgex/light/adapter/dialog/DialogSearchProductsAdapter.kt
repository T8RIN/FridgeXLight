package com.progix.fridgex.light.adapter.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListNames
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListValues


class DialogSearchProductsAdapter(var context: Context, private var prodList: ArrayList<String>) :
    RecyclerView.Adapter<DialogSearchProductsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_checkable, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.star.visibility = GONE
        val temValue = prodList[position]
        holder.prodName.text = temValue.replaceFirstChar { it.titlecase() }

        holder.checkBox.isChecked = adapterListNames.contains(prodList[position])

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            if (holder.checkBox.isChecked && !adapterListNames.contains(prodList[position])) {
                adapterListNames.add(prodList[position])
                adapterListValues.add(Pair(prodList[position], "0"))
            } else if (adapterListNames.contains(prodList[position])) {
                adapterListNames.remove(prodList[position])
                adapterListValues.remove(Pair(prodList[position], "0"))
            }
            var tempString = ""
            for (i in adapterListValues) tempString += "${i.first} ... ${i.second}\n"
            DialogProductsFragment.adapterInterface?.onTextChange(tempString)
        }
        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked && !adapterListNames.contains(prodList[position])) {
                adapterListNames.add(prodList[position])
                adapterListValues.add(Pair(prodList[position], "0"))
            } else if (adapterListNames.contains(prodList[position])) {
                adapterListNames.remove(prodList[position])
                adapterListValues.remove(Pair(prodList[position], "0"))
            }
            var tempString = ""
            for (i in adapterListValues) tempString += "${i.first} ... ${i.second}\n"
            DialogProductsFragment.adapterInterface?.onTextChange(tempString)
        }

    }

    override fun getItemCount(): Int {
        return prodList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var prodName: TextView = view.findViewById(R.id.name)
        var checkBox: CheckBox = view.findViewById(R.id.checkbox)
        var star: ImageView = view.findViewById(R.id.star)
    }

}