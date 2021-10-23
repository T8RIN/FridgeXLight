package com.progix.fridgex.light

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.progix.fridgex.light.R.drawable.*
import com.progix.fridgex.light.helper.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), ActionMode.Callback {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var toolbar: Toolbar
    private var currentFragment: Int = 0
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private var hide: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        when (loadNightMode()) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        setTheme(R.style.FridgeXLight)

        super.onCreate(savedInstanceState)

        initDataBase()

        overridePendingTransition(R.anim.enter_fade_through, R.anim.exit_fade_through)
        setContentView(R.layout.activity_main)


        navigationView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        anchor = bottomNavigationView
        drawer = drawerLayout
        setSupportActionBar(toolbar)
        currentFragment = R.id.nav_home
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(ic_baseline_menu_24)
        navigationView.setCheckedItem(R.id.nav_home)
        setupNavController()
        setupDrawerNavigation()
        setupBottomNavigation()
        visibilityNavElements(navController)
        if (guide) Toast.makeText(this, "Guide", Toast.LENGTH_SHORT).show()
        if (restart) {
            navigateTo(R.id.nav_settings, null)
            bottomNavigationView.visibility = GONE
            restart = false
        } else {
            bottomNavigationView.visibility = VISIBLE
        }
        setUpBadges()
//        val actionMode = startSupportActionMode(this)
//
//        actionMode!!.setTitle("dddsf")

        listAssign()

        CoroutineScope(Dispatchers.Main).launch {
            reloadEvery60Seconds()
        }
    }

    private fun setUpBadges() {
        val fridgeBadge = loadString("R.id.nav_fridge")!!
        val cartBadge = loadString("R.id.nav_cart")!!

        if (fridgeBadge != "0") {
            bottomNavigationView.getOrCreateBadge(R.id.nav_fridge).number =
                Integer.valueOf(fridgeBadge)
        }
        if (cartBadge != "0") {
            bottomNavigationView.getOrCreateBadge(R.id.nav_cart).number =
                Integer.valueOf(cartBadge)
        }
    }

    private fun saveBadgeState() {
        val fridgeBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_fridge).number.toString()
        val cartBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_cart).number.toString()


        saveString("R.id.nav_fridge", fridgeBadge)
        saveString("R.id.nav_cart", cartBadge)
    }

    private fun listAssign() {
        CoroutineScope(Dispatchers.Main).launch {
            suspend()
        }
    }

    private suspend fun suspend() = withContext(Dispatchers.IO) {
        val cursor: Cursor = mDb.rawQuery("SELECT * FROM products", null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            products.add(cursor.getString(2))
            cursor.moveToNext()
        }
        cursor.close()
    }

    private suspend fun reloadEvery60Seconds() = withContext(Dispatchers.IO) {
        for (i in 0..950) {
            mDb.execSQL(
                "UPDATE products SET is_in_fridge = 1 WHERE id = ?",
                listOf(i.toString()).toTypedArray()
            )
        }
        for (i in 0..30) {
            mDb.execSQL(
                "UPDATE recipes SET banned = 1 WHERE id = ?",
                listOf(i.toString()).toTypedArray()
            )
        }
        for (i in 20..60) {
            mDb.execSQL(
                "UPDATE recipes SET is_starred = 1 WHERE id = ?",
                listOf(i.toString()).toTypedArray()
            )
        }
    }

    private fun initDataBase() {
        if (languages.contains(Locale.getDefault().displayLanguage)) {
            DatabaseHelper.DB_NAME = "FridgeXX.db"
        } else {
            DatabaseHelper.DB_NAME = "FridgeXX_en.db"
        }
        val mDBHelper = DatabaseHelper(this)
        mDb = mDBHelper.writableDatabase
        if (loadBoolean("triedOnce") && !loadBoolean("upgraded") || DatabaseHelper.mNeedUpdate) {
            saveBoolean("triedOnce", true)
            saveBoolean("upgraded", false)
            DatabaseHelper.mNeedUpdate = true
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.updatedRecently))
                .setMessage(getString(R.string.updateMessage))
                .setPositiveButton(getString(R.string.update)) { _: DialogInterface?, _: Int ->
                    try {
                        mDBHelper.updateDataBase()
                        saveBoolean("upgraded", true)
                        Toast.makeText(this, getString(R.string.bdSuccess), Toast.LENGTH_SHORT)
                            .show()
                    } catch (mIOException: IOException) {
                        throw Error("UnableToUpdateDatabase")
                    }
                    mDb = mDBHelper.writableDatabase
                }
                .setCancelable(false)
                .show()
            saveBoolean("upgraded", false)
        } else {
            try {
                mDBHelper.updateDataBase()
            } catch (mIOException: IOException) {
                throw Error("UnableToUpdateDatabase")
            }
            mDb = mDBHelper.writableDatabase
        }
    }

    private var des = 0
    private fun visibilityNavElements(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            des = destination.id
            when (destination.id) {
                R.id.nav_home -> showBothNavigation()
                R.id.nav_search -> showBothNavigation()
                R.id.nav_fridge -> showBothNavigation()
                R.id.nav_cart -> showBothNavigation()
                R.id.nav_cat -> hideBothNavigation(true)
                R.id.nav_products -> hideBothNavigation(true)
                else -> hideBothNavigation(false)
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    currentFragment = item.itemId
                    navigateTo(R.id.nav_home, null)
                    true
                }
                R.id.nav_search -> {
                    currentFragment = item.itemId
                    navigateTo(R.id.nav_search, null)
                    true
                }
                R.id.nav_fridge -> {
                    currentFragment = item.itemId
                    navigateTo(R.id.nav_fridge, null)
                    true
                }
                R.id.nav_cart -> {
                    currentFragment = item.itemId
                    navigateTo(R.id.nav_cart, null)
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.setOnItemReselectedListener { }
    }

    private fun setupDrawerNavigation() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_search, R.id.nav_fridge, R.id.nav_cart,
                R.id.nav_star, R.id.nav_banned, R.id.nav_folder, R.id.nav_edit,
                R.id.nav_measures, R.id.nav_tip, R.id.nav_settings
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    if (!it.isChecked) {
                        bottomNavigationView.selectedItemId = R.id.nav_home
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_home, null)
                    }
                }
                R.id.nav_search -> {
                    if (!it.isChecked) {
                        bottomNavigationView.selectedItemId = R.id.nav_search
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_search, null)
                    }
                }
                R.id.nav_fridge -> {
                    if (!it.isChecked) {
                        bottomNavigationView.selectedItemId = R.id.nav_fridge
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_fridge, null)
                    }
                }
                R.id.nav_cart -> {
                    if (!it.isChecked) {
                        bottomNavigationView.selectedItemId = R.id.nav_cart
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_cart, null)
                    }
                }
                R.id.nav_star -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_star, null)
                    }
                }
                R.id.nav_banned -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_banned, null)
                    }
                }
                R.id.nav_folder -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_folder, null)
                    }
                }
                R.id.nav_edit -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_edit, null)
                    }
                }
                R.id.nav_measures -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_measures, null)
                    }
                }
                R.id.nav_tip -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_tip, null)
                    }
                }
                R.id.nav_settings -> {
                    if (!it.isChecked) {
                        currentFragment = it.itemId
                        navigateTo(R.id.nav_settings, null)
                    }
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupNavController() {
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                when (navController.currentDestination?.id) {
                    R.id.nav_cat -> onBackPressed()
                    R.id.nav_products -> onBackPressed()
                    R.id.nav_pod_folder -> onBackPressed()
                    R.id.nav_pod_pod_folder -> onBackPressed()
                    R.id.nav_tip_list -> onBackPressed()
                    else -> drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            R.id.add -> {
                val bundle = Bundle()
                val current = navController.currentDestination?.id
                current.let { bundle.putInt("prodCat", it!!) }
                navigateTo(R.id.nav_cat, bundle)
                currentFragment = R.id.nav_cat
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (navController.currentDestination?.id == R.id.nav_home
            || navController.currentDestination?.id == R.id.nav_search
            || navController.currentDestination?.id == R.id.nav_fridge
            || navController.currentDestination?.id == R.id.nav_cart
        ) {
            if (doubleBackToExitPressedOnce) {
                finishAffinity()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, getString(R.string.exitConfirm), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed(
                { doubleBackToExitPressedOnce = false },
                2000
            )
        } else if (navController.currentDestination?.id != R.id.nav_cat &&
            navController.currentDestination?.id != R.id.nav_products &&
            navController.currentDestination?.id != R.id.nav_pod_folder &&
            navController.currentDestination?.id != R.id.nav_pod_pod_folder &&
            navController.currentDestination?.id != R.id.nav_tip_list
        ) {
            drawerLayout.openDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun navigateTo(resId: Int, args: Bundle?) {
        navController.navigate(resId, args)
        bottomNavigationView.removeBadge(resId)
        saveString(resId.toString(), "0")
    }

    private fun showBothNavigation() {
        bottomNavigationView.visibility = VISIBLE
        navigationView.visibility = VISIBLE
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        bottomSlideUp()
        hide = false
    }

    private fun bottomSlideUp() {
        val layoutParams = bottomNavigationView.layoutParams
        if (layoutParams is CoordinatorLayout.LayoutParams) {
            val behavior = layoutParams.behavior
            if (behavior is HideBottomViewOnScrollBehavior<*>) {
                val hideShowBehavior =
                    behavior as HideBottomViewOnScrollBehavior<BottomNavigationView>
                hideShowBehavior.slideUp(bottomNavigationView)
            }
        }
    }

    private fun bottomSlideDown() {
        val layoutParams = bottomNavigationView.layoutParams
        if (layoutParams is CoordinatorLayout.LayoutParams) {
            val behavior = layoutParams.behavior
            if (behavior is HideBottomViewOnScrollBehavior<*>) {
                val hideShowBehavior =
                    behavior as HideBottomViewOnScrollBehavior<BottomNavigationView>
                hideShowBehavior.slideDown(bottomNavigationView)
            }
        }
    }

    private fun hideBothNavigation(lock: Boolean) {
        if (des == R.id.nav_banned) {
            bottomNavigationView.visibility = INVISIBLE
            navigationView.visibility = INVISIBLE
        }
        if (lock) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSlideDown()
        }, 1)
        hide = true
    }

    private fun loadNightMode(): Int? {
        val sharedPreferences = getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences?.getInt("mode", 2)
    }

    companion object {
        var guide = false
        var restart = false

        @SuppressLint("StaticFieldLeak")
        var languages = ArrayList(
            listOf(
                "ru",
                "uk",
                "be",
                "ba",
                "kk",
                "русский",
                "беларуская",
                "українська",
                "Қазақ тілі"
            )
        )
        lateinit var mDb: SQLiteDatabase
        val images: ArrayList<Int> = ArrayList(
            listOf(
                recipe_1,
                recipe_2,
                recipe_3,
                recipe_4,
                recipe_5,
                recipe_6,
                recipe_7,
                recipe_8,
                recipe_9,
                recipe_10,
                recipe_11,
                recipe_12,
                recipe_13,
                recipe_14,
                recipe_15,
                recipe_16,
                recipe_17,
                recipe_18,
                recipe_19,
                recipe_20,
                recipe_21,
                recipe_22,
                recipe_23,
                recipe_24,
                recipe_25,
                recipe_26,
                recipe_27,
                recipe_28,
                recipe_29,
                recipe_30,
                recipe_31,
                recipe_32,
                recipe_33,
                recipe_34,
                recipe_35,
                recipe_36,
                recipe_37,
                recipe_38,
                recipe_39,
                recipe_40,
                recipe_41,
                recipe_42,
                recipe_43,
                recipe_44,
                recipe_45,
                recipe_46,
                recipe_47,
                recipe_48,
                recipe_49,
                recipe_50,
                recipe_51,
                recipe_52,
                recipe_53,
                recipe_54,
                recipe_55,
                recipe_56,
                recipe_57,
                recipe_58,
                recipe_59,
                recipe_60,
                recipe_61,
                recipe_62,
                recipe_63,
                recipe_64,
                recipe_65,
                recipe_66,
                recipe_67,
                recipe_68,
                recipe_69,
                recipe_70,
                recipe_71,
                recipe_72,
                recipe_73,
                recipe_74,
                recipe_75,
                recipe_76,
                recipe_77,
                recipe_78,
                recipe_79,
                recipe_80,
                recipe_81,
                recipe_82,
                recipe_83,
                recipe_84,
                recipe_85,
                recipe_86,
                recipe_87,
                recipe_88,
                recipe_89,
                recipe_90,
                recipe_91,
                recipe_92,
                recipe_93,
                recipe_94,
                recipe_95,
                recipe_96,
                recipe_97,
                recipe_98,
                recipe_99,
                recipe_100,
                recipe_101,
                recipe_102,
                recipe_103,
                recipe_104,
                recipe_105,
                recipe_106,
                recipe_107,
                recipe_108,
                recipe_109,
                recipe_110,
                recipe_111,
                recipe_112,
                recipe_113,
                recipe_114,
                recipe_115,
                recipe_116,
                recipe_117,
                recipe_118,
                recipe_119,
                recipe_120,
                recipe_121,
                recipe_122,
                recipe_123,
                recipe_124,
                recipe_125,
                recipe_126,
                recipe_127,
                recipe_128,
                recipe_129,
                recipe_130,
                recipe_131,
                recipe_132,
                recipe_133,
                recipe_134,
                recipe_135,
                recipe_136,
                recipe_137,
                recipe_138,
                recipe_139,
                recipe_140,
                recipe_141,
                recipe_142,
                recipe_143,
                recipe_144,
                recipe_145
            )
        )
        val catG: ArrayList<Int> = ArrayList(
            listOf(folder1, folder2, folder3, folder4, folder5, folder6, folder7, folder8)
        )

        val advices: ArrayList<Int> = ArrayList(
            listOf(advice1, advice2, advice3, advice4, advice5, advice6, advice7, advice8)
        )

        val podFolder: ArrayList<Int> = ArrayList(
            listOf(
                podcateg_1, podcateg_2, podcateg_3, podcateg_4, podcateg_5,
                podcateg_6, podcateg_7, podcateg_8, podcateg_9, podcateg_10, podcateg_11,
                podcateg_12, podcateg_13, podcateg_14, podcateg_15, podcateg_16,
                podcateg_17, podcateg_18, podcateg_19, podcateg_20, podcateg_21, podcateg_22,
                podcateg_23, podcateg_24, podcateg_25, podcateg_26, podcateg_27, podcateg_28
            )
        )

        val imagesCat: ArrayList<Int> = ArrayList(
            listOf(
                ic_1,
                ic_2,
                ic_3,
                ic_4,
                ic_5,
                ic_6,
                ic_7,
                ic_8,
                ic_9,
                ic_10,
                ic_11,
                ic_12,
                ic_13,
                ic_14,
                ic_15,
                ic_16,
                ic_17,
                ic_18,
                ic_19,
                ic_20,
                ic_21,
            )
        )

        val products: ArrayList<String> = ArrayList()
        lateinit var anchor: BottomNavigationView
        lateinit var drawer: DrawerLayout

        fun slideUp() {
            val layoutParams = anchor.layoutParams
            if (layoutParams is CoordinatorLayout.LayoutParams) {
                val behavior = layoutParams.behavior
                if (behavior is HideBottomViewOnScrollBehavior<*>) {
                    val hideShowBehavior =
                        behavior as HideBottomViewOnScrollBehavior<BottomNavigationView>
                    hideShowBehavior.slideUp(anchor)
                }
            }
        }

        fun slideDown() {
            val layoutParams = anchor.layoutParams
            if (layoutParams is CoordinatorLayout.LayoutParams) {
                val behavior = layoutParams.behavior
                if (behavior is HideBottomViewOnScrollBehavior<*>) {
                    val hideShowBehavior =
                        behavior as HideBottomViewOnScrollBehavior<BottomNavigationView>
                    hideShowBehavior.slideDown(anchor)
                }
            }
        }

    }

    private fun saveBoolean(key: String?, value: Boolean) {
        val sharedPreferences = getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun loadBoolean(key: String?): Boolean {
        val sharedPreferences = getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, true)
    }

    private fun saveString(key: String, value: String) {
        val sharedPreferences = getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun loadString(key: String): String? {
        val sharedPreferences = getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "0")
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
    }

    override fun onDestroy() {
        saveBadgeState()
        super.onDestroy()
    }

    override fun onPause() {
        saveBadgeState()
        super.onPause()
    }

}
