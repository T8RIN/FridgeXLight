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
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.helper.ActionInterface
import com.progix.fridgex.light.model.RecyclerSortItem


class BannedRecipesAdapter(
    var context: Context,
    var recipeList: ArrayList<RecyclerSortItem>,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<BannedRecipesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.star.visibility = GONE
        Glide.with(context).load(recipeList[position].recipeItem.image).into(holder.image)
        Glide.with(context).load(recipeList[position].recipeItem.indicator).into(holder.indicator)
        holder.recipeName.text = recipeList[position].recipeItem.recipeName
        holder.time.text = recipeList[position].recipeItem.time
        holder.xOfY.text = recipeList[position].recipeItem.xOfY
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ?",
            listOf(recipeList[position].recipeItem.recipeName).toTypedArray()
        )
        cursor.moveToFirst()

        holder.bind(onClickListener, cursor.getInt(0), position)
        cursor.close()
        setAnimation(holder.itemView, position)
        (holder.itemView as MaterialCardView).isChecked =
            selectedIds.contains(recipeList[position].recipeItem.recipeName)
    }


    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
        var recipeName: TextView = view.findViewById(R.id.recipeName)
        var indicator: ImageView = view.findViewById(R.id.indicator)
        var xOfY: TextView = view.findViewById(R.id.x_y)
        var time: TextView = view.findViewById(R.id.time)
        var star: ImageView = view.findViewById(R.id.star)

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            onClickListener: OnClickListener,
            id: Int,
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
                else onClickListener.onClick(image, id)
            }
        }
    }

    private var actionInterface: ActionInterface? = null

    fun init(actionInterface: ActionInterface) {
        this.actionInterface = actionInterface
    }

    fun addIDIntoSelectedIds(position: Int) {
        val id = recipeList[position].recipeItem.recipeName
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
            selectedPositions.remove(position)
        } else {
            selectedIds.add(id)
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        if (selectedIds.size < 1) isMultiSelectOn = false
        actionInterface?.actionInterface(selectedIds.size)
    }

    val selectedIds: ArrayList<String> = ArrayList()

    var tempList: ArrayList<String>? = null

    var tempPositions: ArrayList<Int>? = null

    val selectedPositions: ArrayList<Int> = ArrayList()

    fun doSomeAction(modifier: String) {
        if (selectedIds.size < 1) return
        when (modifier) {
            "delete" -> {
                val delList: ArrayList<RecyclerSortItem> = ArrayList()
                val indexes: ArrayList<Int> = ArrayList()
                for (i in tempPositions!!) {
                    delList.add(recipeList[i])
                }
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE recipes SET banned = 0 WHERE recipe_name = ?",
                        listOf(temp).toTypedArray()
                    )
                    val tempPos = recipeList.indexOf(delList[i])
                    indexes.add(tempPos)
                    recipeList.remove(delList[i])
                    notifyItemRemoved(tempPos)
                }
                CustomSnackbar(context)
                    .create(
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.deletedFromBanned)
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE recipes SET banned = 1 WHERE recipe_name = ?",
                                listOf(temp).toTypedArray()
                            )
                            if (indexes[i] < recipeList.size) recipeList.add(indexes[i], delList[i])
                            else recipeList.add(delList[i])
                        }
                        notifyDataSetChanged()
                    }
                    .show()
            }
        }
        isMultiSelectOn = false
    }


    class OnClickListener(val clickListener: (ImageView, Int) -> Unit) {
        fun onClick(
            image: ImageView,
            id: Int
        ) = clickListener(image, id)
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