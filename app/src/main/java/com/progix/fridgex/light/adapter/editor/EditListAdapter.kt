package com.progix.fridgex.light.adapter.editor

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.ThirdActivity
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.functions.Functions
import com.progix.fridgex.light.functions.Functions.strToInt
import com.progix.fridgex.light.helper.interfaces.EditListChangesInterface
import com.progix.fridgex.light.model.RecyclerSortItem
import com.skydoves.transformationlayout.TransformationCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException


class EditListAdapter(
    var context: Context,
    var recipeList: ArrayList<RecyclerSortItem>,
    var onClickListener: OnClickListener,
    private var editorInterface: EditListChangesInterface
) : RecyclerView.Adapter<EditListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.star.visibility = GONE
        if (recipeList[position].recipeItem.image != -1) {
            Glide.with(context).load(recipeList[position].recipeItem.image).into(holder.image)
        } else {
            val id: Int = strToInt(recipeList[position].recipeItem.recipeName)
            Glide.with(context).load(Functions.loadImageFromStorage(context, "recipe_$id.png"))
                .into(holder.image)
        }
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
    }

    private suspend fun waitFor5Sec() = withContext(Dispatchers.IO) {
        delay(5000)
    }

    private fun popupMenus(view: View, id: Int, position: Int) {
        val popupMenus = PopupMenu(context, view)
        popupMenus.inflate(R.menu.edit_menu)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear -> {
                    deleteJob = CoroutineScope(Dispatchers.Main).launch {
                        waitFor5Sec()
                        currentSnackbar?.dismiss()
                        currentSnackbar = null
                        initDeletion(id)
                        editorInterface.onNeedsToBeRecreated()
                        Toast.makeText(
                            context,
                            context.getString(R.string.deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showSnackBar(context.getString(R.string.recipeDeleted), position)
                    true
                }
                R.id.edit -> {
                    val intent = Intent(context, ThirdActivity::class.java)
                    intent.putExtra("orient", context.resources.configuration.orientation)
                    intent.putExtra("toEdit", id)
                    TransformationCompat.startActivity(
                        (context as MainActivity).findViewById(R.id.transformationLayout),
                        intent
                    )
                    ThirdActivity.editorInterface = editorInterface
                    true
                }
                else -> true
            }

        }
        popupMenus.setForceShowIcon(true)
        popupMenus.show()
    }

    private suspend fun initDeletion(id: Int) = withContext(Dispatchers.IO) {
        val cursor =
            mDb.rawQuery("SELECT * FROM recipes WHERE id = ?", listOf(id.toString()).toTypedArray())
        cursor.moveToFirst()
        val z: Int = strToInt(cursor.getString(3))
        val cw = ContextWrapper(context)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(
            directory,
            "recipe_$z.png"
        )
        file.delete()
        if (file.exists()) {
            try {
                file.canonicalFile.delete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (file.exists()) {
                context.deleteFile(file.name)
            }
        }
        cursor.close()
        mDb.delete("recipes", "id = $id", null)
    }

    private var tempvall: RecyclerSortItem? = null

    var currentSnackbar: Snackbar? = null

    private fun showSnackBar(text: String, position: Int) {
        tempvall = recipeList[position]
        recipeList.removeAt(position)
        notifyItemRemoved(position)

        currentSnackbar = CustomSnackbar(context)
            .create(
                true,
                (context as MainActivity).findViewById(R.id.main_root),
                text
            )

        currentSnackbar?.setAction(context.getString(R.string.undo)) {
            deleteJob?.cancel()
            recipeList.add(position, tempvall!!)
            notifyItemInserted(position)
            currentSnackbar = null
        }
            ?.show()
    }

    private var deleteJob: Job? = null

    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val recipeName: TextView = view.findViewById(R.id.recipeName)
        val indicator: ImageView = view.findViewById(R.id.indicator)
        val xOfY: TextView = view.findViewById(R.id.x_y)
        val time: TextView = view.findViewById(R.id.time)
        val star: ImageView = view.findViewById(R.id.star)

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            onClickListener: OnClickListener,
            id: Int,
            position: Int
        ) {
            recursiveOnClick(onClickListener, itemView, image, id)
            itemView.setOnLongClickListener {
                if (currentSnackbar == null) {
                    popupMenus(it, id, position)
                } else {
                    Toast.makeText(context, context.getString(R.string.wait), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }
        }
    }

    private fun recursiveOnClick(
        onClickListener: OnClickListener,
        itemView: View,
        image: ImageView,
        id: Int
    ) {
        itemView.setOnClickListener {
            itemView.setOnClickListener {}
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                recursiveOnClick(onClickListener, itemView, image, id)
            }, 1000)
            onClickListener.onClick(image, id)
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