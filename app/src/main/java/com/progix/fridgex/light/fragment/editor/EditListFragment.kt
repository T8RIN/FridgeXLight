package com.progix.fridgex.light.fragment.editor

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.activity.ThirdActivity
import com.progix.fridgex.light.activity.ThirdActivity.Companion.editorInterface
import com.progix.fridgex.light.adapter.editor.EditListAdapter
import com.progix.fridgex.light.functions.Functions
import com.progix.fridgex.light.helper.interfaces.EditListChangesInterface
import com.progix.fridgex.light.model.RecyclerSortItem
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import com.skydoves.transformationlayout.onTransformationStartContainer
import kotlinx.coroutines.*


class EditListFragment : Fragment(R.layout.fragment_edit_list), EditListChangesInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onTransformationStartContainer()
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

    private var job: Job? = null

    private var recipeList: ArrayList<RecyclerSortItem> = ArrayList()

    private lateinit var recycler: RecyclerView
    private lateinit var loading: CircularProgressIndicator
    private lateinit var annotationCard: MaterialCardView

    private suspend fun startCoroutine() =
        withContext(Dispatchers.IO) {
            val pairList: ArrayList<RecyclerSortItem> = ArrayList()
            val allRecipes: Cursor =
                MainActivity.mDb.rawQuery(
                    "SELECT * FROM recipes WHERE source = ?",
                    listOf("Авторский").toTypedArray()
                )
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

            delay(400)
        }

    private val recipeClicker = EditListAdapter.OnClickListener { image, id ->
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

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        val transformationLayout: TransformationLayout =
            v.findViewById(R.id.transformationLayout)
        transformationLayout.transitionName = "myTransitionName"

        val fab: FloatingActionButton = v.findViewById(R.id.fab)

        fab.setOnClickListener {
            val intent = Intent(requireContext(), ThirdActivity::class.java)
            intent.putExtra("orient", resources.configuration.orientation)
            intent.putExtra("toEdit", -1)
            TransformationCompat.startActivity(transformationLayout, intent)
            editorInterface = this@EditListFragment
        }

        recycler = v.findViewById(R.id.editListRecycler)
        loading = v.findViewById(R.id.loading)
        annotationCard = v.findViewById(R.id.annotationCard)

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            startCoroutine()
            loading.visibility = View.GONE
            if (recipeList.isNotEmpty()) {
                recycler.adapter = EditListAdapter(
                    requireContext(),
                    recipeList,
                    recipeClicker,
                    this@EditListFragment
                )
                recycler.visibility = View.VISIBLE
                annotationCard.visibility = View.GONE
            } else {
                annotationCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.item_animation_fall_down
                    )
                )
                recycler.visibility = View.GONE
                annotationCard.visibility = View.VISIBLE
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.factory_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onNeedsToBeRecreated() {
        job = CoroutineScope(Dispatchers.Main).launch {

            startCoroutine()
            loading.visibility = View.GONE
            if (recipeList.isNotEmpty()) {
                recycler.adapter = EditListAdapter(
                    requireContext(),
                    recipeList,
                    recipeClicker,
                    this@EditListFragment
                )
                recycler.visibility = View.VISIBLE
                annotationCard.visibility = View.GONE
            } else {
                annotationCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.item_animation_fall_down
                    )
                )
                recycler.visibility = View.GONE
                annotationCard.visibility = View.VISIBLE
            }
        }
    }


}