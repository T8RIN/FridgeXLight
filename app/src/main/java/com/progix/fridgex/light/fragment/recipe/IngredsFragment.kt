package com.progix.fridgex.light.fragment.recipe

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.recipe.IngredientsAdapter
import java.text.DecimalFormat

class IngredsFragment : Fragment(R.layout.fragment_ingreds) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
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
                if (amount[i] != "-1") {
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

        init(prodList!!)
    }

    companion object {
        var portions = 1
        var list: ArrayList<Pair<String, String>>? = null
        var missList: ArrayList<Boolean>? = null
        var prodList: ArrayList<Pair<String, String>>? = null
    }

    private fun init(products: ArrayList<Pair<String, String>>) {
        list = products
    }
}