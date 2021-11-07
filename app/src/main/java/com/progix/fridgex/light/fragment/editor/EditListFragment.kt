package com.progix.fridgex.light.fragment.editor

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
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
import com.progix.fridgex.light.data.Functions
import com.progix.fridgex.light.helper.interfaces.EditListChangesInterface
import com.progix.fridgex.light.model.RecyclerSortItem
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import com.skydoves.transformationlayout.onTransformationStartContainer
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditListFragment : Fragment(), EditListChangesInterface {
    private var param1: String? = null
    private var param2: String? = null

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            (requireActivity() as MainActivity).bottomSlideDown()
        }, 1)
    }

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
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var job: Job? = null

    private var recipeList: ArrayList<RecyclerSortItem> = ArrayList()

    private lateinit var recycler: RecyclerView
    private lateinit var loading: CircularProgressIndicator
    private lateinit var annotationCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_edit_list, container, false)

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
                recycler.visibility = View.GONE
                annotationCard.visibility = View.VISIBLE
            }
        }
        return v
    }

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

            @Suppress("BlockingMethodInNonBlockingContext")
            Thread.sleep(200)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transformationLayout: TransformationLayout =
            view.findViewById(R.id.transformationLayout)
        transformationLayout.transitionName = "myTransitionName"

        val fab: FloatingActionButton = view.findViewById(R.id.fab)

        fab.setOnClickListener {
            val intent = Intent(requireContext(), ThirdActivity::class.java)
            intent.putExtra("orient", resources.configuration.orientation)
            TransformationCompat.startActivity(transformationLayout, intent)
            editorInterface = this@EditListFragment
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
                recycler.visibility = View.GONE
                annotationCard.visibility = View.VISIBLE
            }
        }
    }


}