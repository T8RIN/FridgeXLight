package com.progix.fridgex.light.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Insets.add
import android.graphics.PorterDuff
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.R
import com.progix.fridgex.light.model.RecipeItem
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets.add
import androidx.core.widget.ImageViewCompat


class DailyAdapter(
    var context: Context,
    var recipeList: ArrayList<RecipeItem>,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return true
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_daily, parent, false)

        val displayMetrics = context.resources.displayMetrics
        val dpHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        val k = (dpHeight + 50) / 3 - 271
        val k2 = (dpHeight - 68) / 2 - 271

        rand =
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                ArrayList(listOf(254 + k, 280 + k, 266 + k, 273 + k, 295 + k, 262 + k))
            } else {
                ArrayList(listOf(240 + k2, 220 + k2, 240 + k2, 240 + k2, 220 + k2, 220 + k2))
            }



        return ViewHolder(view)
    }

    private var rand: ArrayList<Int> = ArrayList()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(recipeList[position].image).into(holder.image)
        Glide.with(context).load(recipeList[position].indicator).into(holder.indicator)
        holder.recipeName.text = recipeList[position].recipeName
        holder.time.text = recipeList[position].time
        holder.xOfY.text = recipeList[position].xOfY

        val params = holder.itemView.layoutParams
        val random = rand[position]
        val pixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            random.toFloat(),
            context.resources.displayMetrics
        )
        params.height = pixels.toInt()
        holder.itemView.layoutParams = params
        val cursor: Cursor = MainActivity.mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ?",
            listOf(recipeList[position].recipeName).toTypedArray()
        )
        cursor.moveToFirst()
        val banned = cursor.getInt(14) == 1
        val starred = cursor.getInt(7) == 1
        if(banned && starred) {
            holder.star.visibility = VISIBLE
            holder.ban.visibility = VISIBLE
            holder.star.setTint(R.color.yellow)
            holder.star.setImageResource(R.drawable.ic_round_star_24)
            holder.ban.setTint(R.color.red)
            holder.ban.setImageResource(R.drawable.ic_baseline_block_24)
        }
        else if(banned && !starred) {
            holder.star.setTint(R.color.red)
            holder.star.setImageResource(R.drawable.ic_baseline_block_24)
            holder.ban.visibility = GONE
        }
        else if(!banned && starred) {
            holder.star.setTint(R.color.yellow)
            holder.star.setImageResource(R.drawable.ic_round_star_24)
            holder.ban.visibility = GONE
        }
        else {
            holder.star.visibility = GONE
            holder.ban.visibility = GONE
        }
        holder.bind(onClickListener, cursor.getInt(0))

        setAnimation(holder.itemView, position)
    }

    private fun ImageView.setTint(@ColorRes colorRes: Int?) {
        if(colorRes != null) {
            ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.SRC_ATOP);
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
            )
        }
        else ImageViewCompat.setImageTintList(this, null)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        var image: ImageView = view.findViewById(R.id.image)
        var recipeName: TextView = view.findViewById(R.id.recipeName)
        var indicator: ImageView = view.findViewById(R.id.indicator)
        var xOfY: TextView = view.findViewById(R.id.x_y)
        var time: TextView = view.findViewById(R.id.time)
        var star: ImageView = view.findViewById(R.id.star)
        val ban: ImageView = view.findViewById(R.id.ban)

        fun bind(
            onClickListener: OnClickListener,
            id: Int
        ) {
            itemView.setOnClickListener {
                onClickListener.onClick(image, id)
            }
        }

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            p1: View?,
            p2: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.add(R.string.saveRecipe);
        }
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
            AnimationUtils.loadAnimation(context, R.anim.enter_fade_through)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

}