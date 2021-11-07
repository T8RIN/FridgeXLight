package com.progix.fridgex.light.adapter.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.progix.fridgex.light.R
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterInterface
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListNames
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListValues


class DialogListProductsAdapter(
    var context: Context,
    private var prodList: ArrayList<String>,
    private var hintList: ArrayList<String>
) :
    RecyclerView.Adapter<DialogListProductsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_writable, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            holder.textField.requestFocus()
        }
        holder.itemView.setOnLongClickListener {
            popupMenus(it)
            true
        }
        holder.prodName.text = prodList[position]
        holder.textField.hint = hintList[position]
        holder.textField.editText?.addTextChangedListener {
            if (it?.isEmpty() == true) {
                holder.textField.error = context.getString(R.string.theFieldCantBeEmpty)
            } else {
                holder.textField.error = null
                val index = adapterListNames.indexOf(prodList[position])
                adapterListValues[index] = Pair(prodList[position], it.toString())
            }
            var tempString = ""
            for (i in adapterListValues) tempString += "${i.first} ... ${i.second} ${
                hintList[adapterListValues.indexOf(
                    i
                )]
            }\n"
            adapterInterface?.onTextChange(tempString)
        }
        val tempVal = adapterListValues[adapterListNames.indexOf(prodList[position])].second
        if (tempVal != "0") {
            holder.textField.editText?.setText(tempVal)
            holder.textField.error = null
        } else {
            holder.textField.error = context.getString(R.string.theFieldCantBeEmpty)
        }

    }

    override fun getItemCount(): Int {
        return prodList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var prodName: TextView = view.findViewById(R.id.name)
        var textField: TextInputLayout = view.findViewById(R.id.textField)
    }

    private fun popupMenus(
        view: View
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear -> {
                    val item = view.findViewById<TextView>(R.id.name).text
                    adapterListValues.removeAt(adapterListNames.indexOf(item))
                    adapterListNames.remove(item)
                    notifyItemRemoved(prodList.indexOf(item) - 1)
                    var tempString = ""
                    for (i in adapterListValues) tempString += "${i.first} ... ${i.second} ${
                        hintList[adapterListValues.indexOf(
                            i
                        )]
                    }\n"
                    adapterInterface?.onTextChange(tempString)
                    true
                }
                else -> true
            }

        }
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menu, true)
    }

    private fun inflatePopup(
        popupMenus: PopupMenu
    ) {
        popupMenus.menu.add(0, R.id.clear, 0, context.getString(R.string.deleteWord))
            ?.setIcon(R.drawable.ic_baseline_delete_24)
    }

}