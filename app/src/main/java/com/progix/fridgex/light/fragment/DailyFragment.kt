package com.progix.fridgex.light.fragment

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.MainActivity.Companion.tempContext
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.adapter.daily.DailyAdapter
import com.progix.fridgex.light.data.DataArrays.recipeImages
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadDailyRecipe
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadDate
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveDailyRecipe
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveDate
import com.progix.fridgex.light.model.RecipeItem
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DailyFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var job: Job? = null

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            (requireActivity() as MainActivity).bottomSlideUp()
        }, 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private lateinit var loading: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_daily, container, false)
        val dailyRecycler: RecyclerView = v.findViewById(R.id.dailyRecycler)
        loading = v.findViewById(R.id.loading)
        val swipeRefresh: SwipeRefreshLayout = v.findViewById(R.id.swipeRefresh)

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {

            val recipeList: ArrayList<RecipeItem> = startCoroutine()
            loading.visibility = View.GONE
            dailyRecycler.adapter = DailyAdapter(tempContext!!, recipeList, recipeClicker)

        }
        swipeRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                tempContext!!,
                R.color.manualBackground
            )
        )
        swipeRefresh.setColorSchemeResources(R.color.checked, R.color.red, R.color.yellow)
        swipeRefresh.setOnRefreshListener {

            job = CoroutineScope(Dispatchers.Main).launch {

                saveDate(tempContext!!, 0)
                val recipeList: ArrayList<RecipeItem> = startCoroutine()
                dailyRecycler.adapter = DailyAdapter(requireContext(), recipeList, recipeClicker)
                swipeRefresh.isRefreshing = false

            }
        }

        return v
    }

    private suspend fun startCoroutine(): ArrayList<RecipeItem> = withContext(Dispatchers.IO) {
        val recipeList: ArrayList<RecipeItem> = ArrayList()

        val dateOld = loadDate(tempContext!!)
        val currentDate = Date()
        val data: ArrayList<Int> = ArrayList()
        while (true) {
            val thumbnail = (Math.random() * recipeImages.size).toInt()
            if (!data.contains(thumbnail) && thumbnail != 0) data.add(thumbnail)
            if (data.size == 6) break
        }
        data.shuffle()
        val dateFormat: DateFormat = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
        val dateText: String = dateFormat.format(currentDate)
        val dateParts = dateText.split(":").toTypedArray()
        val dateNew = dateParts[0].toInt()
        if (dateNew != dateOld) {
            saveDate(tempContext!!, dateNew)
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
                saveDailyRecipe(tempContext!!, "rc$i", (data[i]).toString())
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
                val id = loadDailyRecipe(tempContext!!, "rc$i")
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
        @Suppress("BlockingMethodInNonBlockingContext")
        Thread.sleep(420)
        return@withContext recipeList
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DailyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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