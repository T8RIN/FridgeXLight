package com.progix.fridgex.light.adapter.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.progix.fridgex.light.R
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListNames
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListValues
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.dialogAdapterInterface


class DialogListProductsAdapter(
    var context: Context,
    private var prodList: ArrayList<String>,
    private var hintList: ArrayList<String>
) :
    RecyclerView.Adapter<DialogListProductsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_writable, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setAnimation(holder.itemView, position)
        holder.itemView.setOnClickListener {
            holder.textField.requestFocus()
        }
        holder.itemView.setOnLongClickListener {
            popupMenus(it)
            true
        }
        holder.prodName.text = prodList[position].replaceFirstChar { it.titlecase() }
        holder.textField.hint = hintList[position]

        holder.textField.editText?.addTextChangedListener {
            if (it?.isEmpty() == true) {
                holder.textField.error = context.getString(R.string.theFieldCantBeEmpty)
            } else {
                holder.textField.error = null
                val index = adapterListNames!!.indexOf(holder.prodName.text.toString())
                adapterListValues!![index] = Pair(holder.prodName.text.toString(), it.toString())
            }
            var tempString = ""
            for (i in adapterListValues!!) tempString += "${i.first} ... ${i.second} ${
                hintList[adapterListValues!!.indexOf(
                    i
                )]
            }\n"
            dialogAdapterInterface?.onTextChange(tempString)
        }

        val tempVal = adapterListValues!![adapterListNames!!.indexOf(prodList[position])].second
        if (tempVal != "0") {
            holder.textField.editText?.setText(tempVal)
            holder.textField.error = null
        } else {
            holder.textField.error = context.getString(R.string.theFieldCantBeEmpty)
            holder.textField.editText?.setText("")
        }

    }

    override fun getItemCount(): Int {
        return prodList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val prodName: TextView = view.findViewById(R.id.name)
        val textField: TextInputLayout = view.findViewById(R.id.textField)
        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return true
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun popupMenus(
        view: View
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear -> {
                    val item = view.findViewById<TextView>(R.id.name).text
                    adapterListValues!!.removeAt(adapterListNames!!.indexOf(item))
                    adapterListNames!!.remove(item)
                    notifyDataSetChanged()
                    var tempString = ""
                    for (i in adapterListValues!!) tempString += "${i.first} ... ${i.second} ${
                        hintList[adapterListValues!!.indexOf(
                            i
                        )]
                    }\n"
                    dialogAdapterInterface?.onTextChange(tempString)
                    true
                }
                else -> true
            }

        }
        popupMenus.setForceShowIcon(true)
        popupMenus.show()
    }

    private fun inflatePopup(
        popupMenus: PopupMenu
    ) {
        popupMenus.menu.add(0, R.id.clear, 0, context.getString(R.string.deleteWord))
            ?.setIcon(R.drawable.ic_baseline_delete_24)
    }

}