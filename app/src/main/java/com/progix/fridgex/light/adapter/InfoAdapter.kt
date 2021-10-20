package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progix.fridgex.light.R
import com.progix.fridgex.light.model.InfoItem


class InfoAdapter(var context: Context, var infoList: ArrayList<InfoItem>) :
    RecyclerView.Adapter<InfoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = infoList[position].name
        holder.value.text = infoList[position].value
        holder.image.setImageResource(infoList[position].image)
        if (infoList[position].name == context.getString(R.string.source)) {
            holder.itemView.setOnClickListener {
                MaterialAlertDialogBuilder(context, R.style.modeAlert)
                    .setTitle(context.getString(R.string.redirect))
                    .setMessage(context.getString(R.string.redirectMessage))
                    .setPositiveButton(
                        context.getString(R.string.cont)
                    ) { _, _ ->
                        val url = "http://" + infoList[position].value
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(browserIntent)
                    }
                    .setNegativeButton(context.getString(R.string.cancel), null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val value: TextView = view.findViewById(R.id.value)
        val image: ImageView = view.findViewById(R.id.image)
    }

}