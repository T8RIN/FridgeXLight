package com.progix.fridgex.light.adapter.banned

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.data.DataArrays.productCategoriesImages
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment.Companion.prodAnno
import com.progix.fridgex.light.fragment.banned.BannedProductsFragment.Companion.prodRecycler
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface


class BannedProductsAdapter(
    var context: Context,
    private var bannedProductsList: ArrayList<Pair<String, String>>
) :
    RecyclerView.Adapter<BannedProductsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = bannedProductsList[position].first
        holder.star.visibility = View.GONE

        holder.name.text = name.replaceFirstChar(Char::uppercase)
        val cursor2: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(bannedProductsList[position].second).toTypedArray()
        )
        cursor2.moveToFirst()
        holder.image.setImageResource(productCategoriesImages[cursor2.getInt(0) - 1])

        holder.bind(position)
        cursor2.close()
        setAnimation(holder.itemView, position)

        (holder.itemView as MaterialCardView).isChecked = selectedIds.contains(name)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun popupMenus(
        view: View,
        position: Int
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear -> {
                    val tempValue = bannedProductsList[position]
                    mDb.execSQL(
                        "UPDATE products SET banned = 0 WHERE product = ?",
                        listOf(tempValue.first).toTypedArray()
                    )
                    bannedProductsList.remove(tempValue)
                    if (bannedProductsList.isEmpty()) {
                        prodRecycler?.visibility = View.GONE
                        prodAnno?.visibility = View.VISIBLE
                    } else notifyItemRemoved(position)
                    Handler(Looper.getMainLooper()).postDelayed({
                        notifyDataSetChanged()
                    }, 500)
                    CustomSnackbar(context)
                        .create(
                            55,
                            (context as MainActivity).findViewById(R.id.main_root),
                            context.getString(R.string.deletedFromBanned)
                        )
                        .setAction(context.getString(R.string.undo)) {
                            mDb.execSQL(
                                "UPDATE products SET banned = 1 WHERE product = ?",
                                listOf(tempValue.first).toTypedArray()
                            )
                            prodRecycler?.visibility = View.VISIBLE
                            prodAnno?.visibility = View.GONE
                            if (bannedProductsList.isNotEmpty()) notifyItemInserted(position)
                            bannedProductsList.add(position, tempValue)
                        }
                        .show()
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


    override fun getItemCount(): Int {
        return bannedProductsList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val star: ImageView = view.findViewById(R.id.star)
        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            position: Int
        ) {
            itemView.setOnLongClickListener {
                if (!isMultiSelectOn) {
                    isMultiSelectOn = true
                    addIDIntoSelectedIds(position)
                }
                true
            }
            itemView.setOnClickListener {
                if (isMultiSelectOn) addIDIntoSelectedIds(position)
                else popupMenus(it, position)
            }
        }

    }

    private var actionModeInterface: ActionModeInterface? = null

    fun attachInterface(actionModeInterface: ActionModeInterface) {
        this.actionModeInterface = actionModeInterface
    }

    fun addIDIntoSelectedIds(position: Int) {
        val id = bannedProductsList[position].first
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
            selectedPositions.remove(position)
        } else {
            selectedIds.add(id)
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        if (selectedIds.size < 1) isMultiSelectOn = false
        actionModeInterface?.onSelectedItemsCountChanged(selectedIds.size)
    }

    val selectedIds: ArrayList<String> = ArrayList()

    var tempList: ArrayList<String>? = null

    var tempPositions: ArrayList<Int>? = null

    val selectedPositions: ArrayList<Int> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun doSomeAction(modifier: String) {
        if (selectedIds.size < 1) return
        when (modifier) {
            "delete" -> {
                val delList: ArrayList<Pair<String, String>> = ArrayList()
                val indexes: ArrayList<Int> = ArrayList()
                for (i in tempPositions!!) {
                    delList.add(bannedProductsList[i])
                }
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET banned = 0 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    val tempPos = bannedProductsList.indexOf(delList[i])
                    indexes.add(tempPos)
                    bannedProductsList.remove(delList[i])
                    if (bannedProductsList.isEmpty()) {
                        prodRecycler?.visibility = View.GONE
                        prodAnno?.visibility = View.VISIBLE
                    } else notifyItemRemoved(tempPos)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    notifyDataSetChanged()
                }, 500)
                CustomSnackbar(context)
                    .create(
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.deletedFromBanned)
                    )
                    .setAction(context.getString(R.string.undo)) {
                        prodRecycler?.visibility = View.VISIBLE
                        prodAnno?.visibility = View.GONE
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET banned = 1 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            if (indexes[i] < bannedProductsList.size) bannedProductsList.add(
                                indexes[i],
                                delList[i]
                            )
                            else bannedProductsList.add(delList[i])
                        }
                        notifyDataSetChanged()
                    }
                    .show()
            }
        }
        isMultiSelectOn = false
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation: Animation =
            loadAnimation(context, R.anim.item_animation_fall_down)
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