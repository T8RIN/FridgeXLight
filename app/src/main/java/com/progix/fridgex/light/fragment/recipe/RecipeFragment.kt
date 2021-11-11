package com.progix.fridgex.light.fragment.recipe

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.recipe.RecipeAdapter

class RecipeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recipe, container, false)

        val id = SecondActivity.id
        val recycler = v.findViewById<RecyclerView>(R.id.recipeRecycler)

        val cursor: Cursor = MainActivity.mDb.rawQuery(
            "SELECT * FROM recipes WHERE id = ?",
            listOf(id.toString()).toTypedArray()
        )
        cursor.moveToFirst()

        val list = cursor.getString(8).split("\n")
        cursor.close()
        recycler.adapter = RecipeAdapter(requireContext(), list)


        return v
    }

}