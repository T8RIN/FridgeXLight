package com.progix.fridgex.light.fragment.recipe

import android.database.Cursor
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.recipe.RecipeAdapter

class RecipeFragment : Fragment(R.layout.fragment_recipe) {

    lateinit var recycler: RecyclerView

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val id = SecondActivity.id
        recycler = v.findViewById(R.id.recipeRecycler)

        val cursor: Cursor = MainActivity.mDb.rawQuery(
            "SELECT * FROM recipes WHERE id = ?",
            listOf(id.toString()).toTypedArray()
        )
        cursor.moveToFirst()

        val list = cursor.getString(8).split("\n")
        cursor.close()
        recycler.adapter = RecipeAdapter(this, list)

    }

}