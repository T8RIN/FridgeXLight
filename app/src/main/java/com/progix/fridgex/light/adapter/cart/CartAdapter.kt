package com.progix.fridgex.light.adapter.cart

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.application.FridgeXLightApplication
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.data.DataArrays.productCategoriesImages
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadCartMode
import com.progix.fridgex.light.fragment.cart.CartFragment
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface


class CartAdapter(var context: Context, var cartList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context is FridgeXLightApplication) context =
            (context as FridgeXLightApplication).getCurrentContext()!!
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.star.visibility = GONE
        holder.name.text = cartList[position].first.replaceFirstChar(Char::uppercase)
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(cartList[position].second).toTypedArray()
        )
        cursor.moveToFirst()
        holder.image.setImageResource(productCategoriesImages[cursor.getInt(0) - 1])
        cursor.close()
        val cc: Cursor = mDb.rawQuery(
            "SELECT * FROM products WHERE product = ?",
            listOf(cartList[position].first).toTypedArray()
        )
        cc.moveToFirst()
        val crossed = cc.getString(5) == "1"
        if (crossed) {
            holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.5f
        } else {
            holder.itemView.alpha = 1f
            holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        setAnimation(holder.itemView, position)
        holder.bind(position)
        (holder.itemView as MaterialCardView).isChecked =
            selectedIds.contains(cartList[position].first)
        cc.close()
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    var tempList: ArrayList<String>? = null

    private var crossList: ArrayList<String> = ArrayList()

    var tempPositions: ArrayList<Int>? = null

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
                else {
                    val products: Cursor = mDb.rawQuery(
                        "SELECT * FROM products WHERE product = ?",
                        listOf(cartList[position].first).toTypedArray()
                    )
                    products.moveToFirst()
                    val crossed = products.getString(5) == "1"
                    mDb.execSQL(
                        "UPDATE products SET amount = ? WHERE product = ?",
                        listOf(!crossed, cartList[position].first).toTypedArray()
                    )

                    val addToFridge = loadCartMode(context) == 1
                    val bNav =
                        (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

                    if (crossed) {
                        if (addToFridge) {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 0 WHERE product = ?",
                                listOf(cartList[position].first).toTypedArray()
                            )

                            val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                            badge.number -= 1
                            if (badge.number == 0) bNav.removeBadge(R.id.nav_fridge)

                        }
                        itemView.alpha = 1f
                        name.paintFlags =
                            name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    } else {
                        if (addToFridge) {
                            mDb.execSQL(
                                "UPDATE products SET is_in_fridge = 1 WHERE product = ?",
                                listOf(cartList[position].first).toTypedArray()
                            )

                            val badge = bNav.getOrCreateBadge(R.id.nav_fridge)
                            badge.number += 1
                        }

                        itemView.alpha = 0.5f
                        name.paintFlags = name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    products.close()
                }
            }
        }
    }

    var actionModeInterface: ActionModeInterface? = null

    fun attachInterface(actionModeInterface: ActionModeInterface) {
        this.actionModeInterface = actionModeInterface
    }

    fun addIDIntoSelectedIds(position: Int) {
        val id = cartList[position].first
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

    val selectedPositions: ArrayList<Int> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun doSomeAction(modifier: String) {
        if (selectedIds.size < 1) return
        when (modifier) {
            "share" -> {
                var sharing = ""

                sharing += context.getString(R.string.shopping)
                sharing += "\n\n"
                sharing += context.getString(R.string.buy)
                sharing += "\n"
                for (i in tempList!!) {
                    sharing += i.replaceFirstChar { it.titlecase() }
                    sharing += "\n"
                }

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    sharing + "\n" + context.getString(R.string.copiedFrom)
                )

                sendIntent.type = "text/plain"
                context.startActivity(
                    Intent.createChooser(
                        sendIntent,
                        context.getString(R.string.share)
                    )
                )
            }
            "star" -> {
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET is_starred = 1 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    notifyItemChanged(tempPositions!![i])
                }
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToStarred),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_starred = 0 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            notifyItemChanged(tempPositions!![i])
                        }
                    }
                    .show()
            }
            "ban" -> {
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET banned = 1 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    notifyItemChanged(tempPositions!![i])
                }
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.addedToBanList),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET banned = 0 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            notifyItemChanged(tempPositions!![i])
                        }
                    }
                    .show()
            }
            "delete" -> {
                val bottomNav =
                    (context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
                val layoutParams =
                    bottomNav.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
                behavior.slideUp(bottomNav)

                val delList: ArrayList<Pair<String, String>> = ArrayList()
                val indexes: ArrayList<Int> = ArrayList()
                for (i in tempPositions!!) {
                    delList.add(cartList[i])
                }
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    val cursor: Cursor = mDb.rawQuery(
                        "SELECT * FROM products WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    cursor.moveToFirst()
                    if (cursor.getInt(5) == 1) crossList.add(temp)
                    mDb.execSQL(
                        "UPDATE products SET amount = 0 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    mDb.execSQL(
                        "UPDATE products SET is_in_cart = 0 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    cursor.close()

                    val tempPos = cartList.indexOf(delList[i])
                    indexes.add(tempPos)
                    cartList.remove(delList[i])
                    if (cartList.isEmpty()) {
                        CartFragment.recycler?.visibility = GONE
                        CartFragment.annotationCard?.visibility = VISIBLE
                    } else notifyItemRemoved(tempPos)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    notifyDataSetChanged()
                }, 500)
                CustomSnackbar(context)
                    .create(
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.deleteFromCart),
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction(context.getString(R.string.undo)) {
                        behavior.slideUp(bottomNav)
                        CartFragment.recycler?.visibility = VISIBLE
                        CartFragment.annotationCard?.visibility = GONE
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET is_in_cart = 1 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            for (item in crossList) {
                                mDb.execSQL(
                                    "UPDATE products SET amount = 1 WHERE product = ?",
                                    listOf(item).toTypedArray()
                                )
                            }
                            crossList.clear()
                            if (indexes[i] < cartList.size) cartList.add(indexes[i], delList[i])
                            else cartList.add(delList[i])
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