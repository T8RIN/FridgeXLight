package com.progix.fridgex.light.fragment.starred

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.actionMode
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.starred.StarRecipesAdapter
import com.progix.fridgex.light.functions.Functions
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface
import com.progix.fridgex.light.model.RecyclerSortItem
import kotlinx.coroutines.*


class StarRecipesFragment : Fragment(R.layout.fragment_star_recipes), ActionModeInterface {

    private var job: Job? = null
    var adapter: StarRecipesAdapter? = null

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val recycler: RecyclerView = v.findViewById(R.id.starRecipeRecycler)
        recRecycler = recycler
        val annotationCard: MaterialCardView = v.findViewById(R.id.annotationCard)
        recAnno = annotationCard

        val loading: CircularProgressIndicator = v.findViewById(R.id.loading)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (recipeList == null) startCoroutine()

            loading.visibility = GONE
            if (recipeList!!.isNotEmpty()) {
                adapter = StarRecipesAdapter(requireContext(), recipeList!!, recipeClicker)
                adapter!!.attachInterface(this@StarRecipesFragment)
                recycler.adapter = adapter
            } else {
                annotationCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.item_animation_fall_down
                    )
                )
                annotationCard.visibility = VISIBLE
                recycler.visibility = GONE
            }

        }

    }

    private val recipeClicker = StarRecipesAdapter.OnClickListener { image, id ->
        val intent = Intent(context, SecondActivity::class.java)
        intent.putExtra("rec", id)
        val options = activity?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                it,
                image,
                "recipe"
            )
        }
        startActivity(intent, options!!.toBundle())
    }

    private suspend fun startCoroutine() =
        withContext(Dispatchers.IO) {
            val pairList: ArrayList<RecyclerSortItem> = ArrayList()
            val allRecipes: Cursor =
                mDb.rawQuery("SELECT * FROM recipes WHERE is_starred = 1", null)
            allRecipes.moveToFirst()
            while (!allRecipes.isAfterLast) {
                val id = allRecipes.getInt(0) - 1
                val name = allRecipes.getString(3)
                val time = allRecipes.getInt(6)
                val cal = allRecipes.getInt(10).toDouble()
                val prot = allRecipes.getDouble(11)
                val fats = allRecipes.getDouble(12)
                val carboh = allRecipes.getDouble(13)
                var having = 0
                val products: Cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE is_in_fridge = 1",
                    null
                )
                products.moveToFirst()
                val needed: ArrayList<String> =
                    ArrayList(allRecipes.getString(4).trim().split(" "))
                while (!products.isAfterLast) {
                    if (needed.contains(products.getString(0))) having++
                    products.moveToNext()
                }
                var indicator = 0
                when {
                    having <= 0.49 * needed.size -> indicator = R.drawable.indicator_2
                    having <= 0.74 * needed.size -> indicator = R.drawable.indicator_1
                    having <= needed.size -> indicator = R.drawable.indicator_0
                }
                val xOfY = having.toString() + "/" + needed.size.toString()
                val percentage = having.toDouble() / needed.size
                Functions.addItemToList(
                    id,
                    pairList,
                    percentage,
                    time,
                    cal,
                    prot,
                    fats,
                    carboh,
                    indicator,
                    name,
                    xOfY
                )
                products.close()
                allRecipes.moveToNext()
            }
            allRecipes.close()
            pairList.sortBy { it.recipeItem.recipeName }

            recipeList = pairList

            delay(200)
        }

    companion object {
        var recRecycler: RecyclerView? = null
        var recAnno: MaterialCardView? = null
        var recipeList: ArrayList<RecyclerSortItem>? = null
    }

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.attachAdapter(adapter!!, 5)
        if (actionMode == null) actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) actionMode?.title = "$count"
        else actionMode?.finish()
    }
}