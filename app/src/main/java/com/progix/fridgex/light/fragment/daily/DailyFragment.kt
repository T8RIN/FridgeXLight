package com.progix.fridgex.light.fragment.daily

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.daily.DailyAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.appContext
import com.progix.fridgex.light.data.DataArrays.recipeImages
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadDailyRecipe
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadDate
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveDailyRecipe
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveDate
import com.progix.fridgex.light.extensions.Extensions.getAttrColor
import com.progix.fridgex.light.model.RecipeItem
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DailyFragment : Fragment(R.layout.fragment_daily) {
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

    private lateinit var loading: CircularProgressIndicator

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val dailyRecycler: RecyclerView = v.findViewById(R.id.dailyRecycler)
        loading = v.findViewById(R.id.loading)
        val swipeRefresh: SwipeRefreshLayout = v.findViewById(R.id.swipeRefresh)

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            val recipeList: ArrayList<RecipeItem> = startCoroutine()
            loading.visibility = View.GONE
            dailyRecycler.adapter =
                DailyAdapter(appContext, recipeList, recipeClicker)
            swipeRefresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    appContext,
                    R.color.manualBackground
                )
            )
        }

        swipeRefresh.setColorSchemeColors(
            requireContext().getAttrColor(R.attr.checked),
            requireContext().getAttrColor(R.attr.checkedl)
        )
        swipeRefresh.setOnRefreshListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.updateDailyRecipes))
                .setMessage(getString(R.string.thisWillUpdateDailyRecipes))
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    swipeRefresh.isRefreshing = false
                }
                .setPositiveButton(getString(R.string.update)) { _, _ ->
                    job = CoroutineScope(Dispatchers.Main).launch {
                        try {
                            saveDate(requireActivity(), 0)
                            val recipeList: ArrayList<RecipeItem> = startCoroutine()
                            dailyRecycler.adapter =
                                DailyAdapter(requireActivity(), recipeList, recipeClicker)
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }

                        swipeRefresh.isRefreshing = false
                    }
                }
                .setOnDismissListener {
                    swipeRefresh.isRefreshing = false
                }
                .show()
        }
    }

    private suspend fun startCoroutine(): ArrayList<RecipeItem> = withContext(Dispatchers.IO) {

        val recipeList: ArrayList<RecipeItem> = ArrayList()

        val dateOld = loadDate(appContext)
        val currentDate = Date()

        val dateFormat: DateFormat = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
        val dateText: String = dateFormat.format(currentDate)
        val dateParts = dateText.split(":").toTypedArray()
        val dateNew = dateParts[0].toInt()
        if (dateNew != dateOld) {
            saveDate(appContext, dateNew)

            val data: ArrayList<Int> = ArrayList()

            val sortList: ArrayList<Int> = ArrayList()
            val allRecipes: Cursor =
                mDb.rawQuery("SELECT * FROM recipes WHERE banned NOT LIKE 1", null)
            allRecipes.moveToFirst()

            while (!allRecipes.isAfterLast) {
                val tempCursor: Cursor =
                    mDb.rawQuery("SELECT * FROM products WHERE banned = 1", null)
                tempCursor.moveToFirst()
                val products = allRecipes.getString(4).split(" ")
                var found = false
                while (!tempCursor.isAfterLast) {
                    val temp = "" + tempCursor.getString(0)
                    if (products.contains(temp)) found = true
                    tempCursor.moveToNext()
                }
                tempCursor.close()
                if (!found) {
                    var fnd = false
                    val tempId = allRecipes.getInt(0)
                    for (i in 0..5) {
                        if (tempId.toString() == loadDailyRecipe(appContext, "rc$i")) {
                            fnd = true
                            break
                        }
                    }
                    if (!fnd && allRecipes.getInt(0) <= recipeImages.size) sortList.add(tempId)
                }
                allRecipes.moveToNext()
            }
            allRecipes.close()

            if (sortList.size < 6) {
                while (true) {
                    val thumbnail = (Math.random() * recipeImages.size).toInt()
                    if (!data.contains(thumbnail) && thumbnail != 0) data.add(thumbnail)
                    if (data.size == 6) break
                }
            } else {
                while (true) {
                    val thumbnail = (Math.random() * sortList.size).toInt()
                    val item = sortList[thumbnail]
                    if (!data.contains(item)) data.add(item)
                    if (data.size == 6) break
                }
            }

            data.shuffle()

            val products: Cursor = mDb.rawQuery(
                "SELECT * FROM products WHERE is_in_fridge = 1",
                null
            )
            products.moveToFirst()
            val fridge: ArrayList<String> = ArrayList()
            while (!products.isAfterLast) {
                fridge.add(products.getString(0))
                products.moveToNext()
            }
            for (i in 0..5) {
                var having = 0
                val cursor: Cursor = mDb.rawQuery(
                    "SELECT * FROM recipes WHERE id = ?",
                    arrayOf(data[i].toString()),
                    null
                )
                cursor.moveToFirst()
                val needed: ArrayList<String> = ArrayList(cursor.getString(4).trim().split(" "))
                for (s: String in fridge) {
                    if (needed.contains(s)) having++
                }
                var indicator = 0
                when {
                    having <= 0.49 * needed.size -> indicator = R.drawable.indicator_2
                    having <= 0.74 * needed.size -> indicator = R.drawable.indicator_1
                    having <= needed.size -> indicator = R.drawable.indicator_0
                }
                val xOfY = having.toString() + "/" + needed.size.toString()
                recipeList.add(
                    RecipeItem(
                        recipeImages[data[i] - 1],
                        indicator,
                        cursor.getString(3),
                        cursor.getString(6),
                        xOfY
                    )
                )
                products.close()
                saveDailyRecipe(appContext, "rc$i", (data[i]).toString())
                cursor.close()
            }

        } else {
            val products: Cursor = mDb.rawQuery(
                "SELECT * FROM products WHERE is_in_fridge = 1",
                null
            )
            products.moveToFirst()
            val fridge: ArrayList<String> = ArrayList()
            while (!products.isAfterLast) {
                fridge.add(products.getString(0))
                products.moveToNext()
            }
            for (i in 0..5) {
                val id = loadDailyRecipe(appContext, "rc$i")
                var having = 0
                val cursor: Cursor = mDb.rawQuery(
                    "SELECT * FROM recipes WHERE id = ?",
                    arrayOf(id),
                    null
                )
                cursor.moveToFirst()
                val needed: ArrayList<String> = ArrayList(cursor.getString(4).trim().split(" "))
                for (s: String in fridge) {
                    if (needed.contains(s)) having++
                }
                var indicator = 0
                when {
                    having <= 0.49 * needed.size -> indicator = R.drawable.indicator_2
                    having <= 0.74 * needed.size -> indicator = R.drawable.indicator_1
                    having <= needed.size -> indicator = R.drawable.indicator_0
                }
                val xOfY = having.toString() + "/" + needed.size.toString()
                recipeList.add(
                    RecipeItem(
                        recipeImages[id!!.toInt() - 1],
                        indicator,
                        cursor.getString(3),
                        cursor.getString(6),
                        xOfY
                    )
                )
                products.close()
                cursor.close()
            }
        }

        return@withContext recipeList
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.daily_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private val recipeClicker = DailyAdapter.OnClickListener { image, id ->
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
}