package com.progix.fridgex.light.fragment.recipe

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.badgeBool
import com.progix.fridgex.light.activity.MainActivity.Companion.badgeCnt
import com.progix.fridgex.light.activity.MainActivity.Companion.badgeNames
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.activity.SecondActivity.Companion.needToControlFab
import com.progix.fridgex.light.adapter.recipe.IngredientsAdapter
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.extensions.Extensions.isNumeric
import java.text.DecimalFormat

class IngredsFragment : Fragment(R.layout.fragment_ingreds) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        if (savedInstanceState == null) currentStep = 0

        val slider: Slider = v.findViewById(R.id.slider)
        val id = SecondActivity.id
        val recycler = v.findViewById<RecyclerView>(R.id.ingredsRecycler)

        val cursor: Cursor =
            mDb.rawQuery("SELECT * FROM recipes WHERE id = ?", listOf(id.toString()).toTypedArray())
        cursor.moveToFirst()
        val products = cursor.getString(4).trim().split(" ")
        val amount = cursor.getString(5).split(" ")
        cursor.close()

        prodList = ArrayList()
        missList = ArrayList()
        val range = products.size - 1
        for (i in 0..range) {
            val cursor2: Cursor = mDb.rawQuery(
                "SELECT * FROM products WHERE id = ?",
                listOf(products[i]).toTypedArray()
            )
            cursor2.moveToFirst()
            missList?.add(cursor2.getInt(3) == 0)
            if (amount[i] != "-1") {
                val tempCursor: Cursor = mDb.rawQuery(
                    "SELECT * FROM categories WHERE category = ?",
                    listOf(cursor2.getString(1)).toTypedArray()
                )
                tempCursor.moveToFirst()
                prodList!!.add(
                    Pair(
                        cursor2.getString(2),
                        amount[i] + " " + tempCursor.getString(2)
                    )
                )
                tempCursor.close()
            } else
                prodList!!.add(Pair(cursor2.getString(2), getString(R.string.taste)))
            cursor2.close()
        }
        slider.addOnChangeListener(Slider.OnChangeListener { _, valuer, _ ->
            portions = valuer.toInt()
            prodList!!.clear()
            missList?.clear()
            val rangeLocal = products.size - 1
            val df = DecimalFormat("#.#")
            v.findViewById<TextView>(R.id.text).text =
                getString(R.string.portions) + " " + portions

            for (i in 0..rangeLocal) {
                val cursor2: Cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE id = ?",
                    listOf(products[i]).toTypedArray()
                )
                cursor2.moveToFirst()
                missList?.add(cursor2.getInt(3) == 0)

                if (amount[i] != "-1" && amount[i].isNumeric()) {
                    val new = amount[i].split(",")
                    val modifier: Double = if (new.size > 1) {
                        (new[0] + "." + new[1]).toDouble()
                    } else {
                        amount[i].toDouble()
                    }
                    val tempCursor: Cursor = mDb.rawQuery(
                        "SELECT * FROM categories WHERE category = ?",
                        listOf(cursor2.getString(1)).toTypedArray()
                    )
                    tempCursor.moveToFirst()
                    prodList!!.add(
                        Pair(
                            cursor2.getString(2),
                            df.format(modifier * valuer.toInt())
                                .toString() + " " + tempCursor.getString(
                                2
                            )
                        )
                    )
                    tempCursor.close()
                } else
                    prodList!!.add(Pair(cursor2.getString(2), getString(R.string.taste)))
                cursor2.close()
            }
            recycler.adapter = IngredientsAdapter(requireContext(), prodList!!, missList!!)
        })


        recycler.adapter = IngredientsAdapter(requireContext(), prodList!!, missList!!)

        attachList(prodList!!)

        fabOnClick((requireActivity() as SecondActivity).findViewById(R.id.fab))
    }

    @Suppress("UNCHECKED_CAST")
    private fun fabOnClick(fab: FloatingActionButton) {
        val tempMissList: ArrayList<Boolean> = missList!!.clone() as ArrayList<Boolean>
        val delList = missList!!.clone() as ArrayList<Boolean>
        delList.removeAll { !it }

        badgeCnt = 0
        if (delList.isNotEmpty()) {
            needToControlFab = true
            var tempCnt = 0
            for (i in tempMissList) {
                if (i) tempCnt++
            }
            fab.visibility = View.VISIBLE
            fab.setOnClickListener {
                val names = arrayOfNulls<String>(tempCnt)
                val bool = BooleanArray(tempCnt)
                var tcnt = 0
                for (i in tempMissList.indices) {
                    if (tempMissList[i]) {
                        names[tcnt] = prodList!![i].first.replaceFirstChar { it.titlecase() }
                        bool[tcnt] = true
                        tcnt++
                    }
                }
                MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                    .setTitle(getString(R.string.chooseProd))
                    .setMultiChoiceItems(
                        names, bool
                    ) { _, which: Int, isChecked: Boolean ->
                        bool[which] = isChecked
                    }
                    .setPositiveButton(
                        getString(R.string.ok)
                    ) { _, _ ->
                        badgeCnt = 0
                        for (i in bool.indices) {
                            if (bool[i]) {
                                mDb.execSQL(
                                    "UPDATE products SET is_in_cart = 1 WHERE product = ?",
                                    listOf(names[i]!!.lowercase()).toTypedArray()
                                )
                                badgeCnt++
                                badgeNames = names
                                badgeBool = bool
                            }
                        }
                        CustomSnackbar(requireContext()).create(
                            365,
                            (context as SecondActivity).findViewById(R.id.main_root),
                            getString(R.string.addedToCart),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                for (i in badgeBool!!.indices) {
                                    if (badgeBool!![i]) {
                                        mDb.execSQL(
                                            "UPDATE products SET is_in_cart = 0 WHERE product = ?",
                                            listOf(badgeNames!![i]!!.lowercase()).toTypedArray()
                                        )
                                        badgeCnt--
                                    }
                                }
                            }
                            .show()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setCancelable(false)
                    .show()
            }
        }
    }

    companion object {
        var portions = 1
        var list: ArrayList<Pair<String, String>>? = null
        var missList: ArrayList<Boolean>? = null
        var prodList: ArrayList<Pair<String, String>>? = null
        var currentStep = 0
    }

    private fun attachList(products: ArrayList<Pair<String, String>>) {
        list = products
    }
}