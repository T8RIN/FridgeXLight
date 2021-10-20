package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.SecondActivity
import com.progix.fridgex.light.adapter.IngredsAdapter
import java.text.DecimalFormat


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IngredsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_ingreds, container, false)

        val slider: Slider = v.findViewById(R.id.slider)
        val id = SecondActivity.id
        val recycler = v.findViewById<RecyclerView>(R.id.ingredsRecycler)

        val cursor: Cursor =
            mDb.rawQuery("SELECT * FROM recipes WHERE id = ?", listOf(id.toString()).toTypedArray())
        cursor.moveToFirst()
        val products = cursor.getString(4).trim().split(" ")
        val amount = cursor.getString(5).split(" ")
        val productss: ArrayList<Pair<String, String>> = ArrayList()
        val range = products.size - 1
        for (i in 0..range) {
            val cursor2: Cursor = mDb.rawQuery(
                "SELECT * FROM products WHERE id = ?",
                listOf(products[i]).toTypedArray()
            )
            cursor2.moveToFirst()
            if (amount[i] != "-1") {
                val cursorr: Cursor = mDb.rawQuery(
                    "SELECT * FROM categories WHERE category = ?",
                    listOf(cursor2.getString(1)).toTypedArray()
                )
                cursorr.moveToFirst()
                productss.add(Pair(cursor2.getString(2), amount[i] + " " + cursorr.getString(2)))
                cursorr.close()
            } else
                productss.add(Pair(cursor2.getString(2), getString(R.string.taste)))
            cursor2.close()
        }
        slider.addOnChangeListener(Slider.OnChangeListener { _, valuer, _ ->
            portions = valuer.toInt()
            productss.clear()
            val rangeous = products.size - 1
            val df = DecimalFormat("#.#")
            v.findViewById<TextView>(R.id.text).text =
                getString(R.string.portions) + " " + portions

            for (i in 0..rangeous) {
                val cursor2: Cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE id = ?",
                    listOf(products[i]).toTypedArray()
                )
                cursor2.moveToFirst()
                if (amount[i] != "-1") {
                    val new = amount[i].split(",")
                    val integr: Double = if (new.size > 1) {
                        (new[0] + "." + new[1]).toDouble()
                    } else {
                        amount[i].toDouble()
                    }
                    val cursorr: Cursor = mDb.rawQuery(
                        "SELECT * FROM categories WHERE category = ?",
                        listOf(cursor2.getString(1)).toTypedArray()
                    )
                    cursorr.moveToFirst()
                    productss.add(
                        Pair(
                            cursor2.getString(2),
                            df.format(integr * valuer.toInt()).toString() + " " + cursorr.getString(
                                2
                            )
                        )
                    )
                } else
                    productss.add(Pair(cursor2.getString(2), getString(R.string.taste)))
                cursor2.close()
            }
            recycler.adapter = IngredsAdapter(requireContext(), productss)
        })


        recycler.adapter = IngredsAdapter(requireContext(), productss)

        init(productss)

        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IngredsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        var portions = 1
        var list: ArrayList<Pair<String, String>>? = null
    }

    private fun init(products: ArrayList<Pair<String, String>>){
        list = products
    }
}