package com.progix.fridgex.light.fragment.banned

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.banned.BannedRecipesAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.appContext
import com.progix.fridgex.light.functions.Functions.addItemToList
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionModeInterface
import com.progix.fridgex.light.model.RecyclerSortItem
import kotlinx.coroutines.*

class BannedRecipesFragment : Fragment(R.layout.fragment_banned_recipes), ActionModeInterface {

    lateinit var loading: CircularProgressIndicator
    lateinit var recycler: RecyclerView
    lateinit var annotationCard: MaterialCardView

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        recycler = v.findViewById(R.id.bannedRecipesRecycler)
        recRecycler = recycler
        annotationCard = v.findViewById(R.id.annotationCard)
        recAnno = annotationCard

        loading = v.findViewById(R.id.loading)
        recreateList()
    }

    fun recreateList() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (recipeList == null) startCoroutine()

            loading.visibility = View.GONE
            if (recipeList!!.isNotEmpty()) {
                adapter = BannedRecipesAdapter(
                    appContext,
                    recipeList!!, recipeClicker
                )
                adapter!!.attachInterface(this@BannedRecipesFragment)
                recycler.adapter = adapter
            } else {
                annotationCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        appContext,
                        R.anim.item_animation_fall_down
                    )
                )
                annotationCard.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            }
        }
    }

    private val recipeClicker = BannedRecipesAdapter.OnClickListener { image, id ->
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
                MainActivity.mDb.rawQuery("SELECT * FROM recipes WHERE banned = 1", null)
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
                val products: Cursor = MainActivity.mDb.rawQuery(
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
                addItemToList(
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

            delay(300)
        }

    private var job: Job? = null
    var adapter: BannedRecipesAdapter? = null

    companion object {
        var recRecycler: RecyclerView? = null
        var recAnno: MaterialCardView? = null
        var recipeList: ArrayList<RecyclerSortItem>? = null
    }

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.attachAdapter(adapter!!, 6)
        if (MainActivity.actionMode == null) MainActivity.actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) MainActivity.actionMode?.title = "$count"
        else MainActivity.actionMode?.finish()
    }
}