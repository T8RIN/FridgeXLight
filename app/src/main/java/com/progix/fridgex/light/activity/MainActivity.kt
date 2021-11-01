package com.progix.fridgex.light.activity

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.progix.fridgex.light.R
import com.progix.fridgex.light.R.drawable.*
import com.progix.fridgex.light.data.DataArrays.languages
import com.progix.fridgex.light.helper.DatabaseHelper
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController


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
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(ic_baseline_menu_24)
        navigationView.setCheckedItem(R.id.nav_home)
        setupNavController()
        setupDrawerNavigation()
        setupBottomNavigation()
        visibilityNavElements(navController)
        if (guide) {
            Toast.makeText(this, "Guide", Toast.LENGTH_SHORT).show()
            guide = false
        }
        if (restart) {
            navigateTo(R.id.nav_settings, null)
            bottomNavigationView.visibility = GONE
            restart = false
        } else {
            bottomNavigationView.visibility = VISIBLE
        }
        setUpBadges()

        listAssign()

        CoroutineScope(Dispatchers.Main).launch {
            testDeleteAfterFinish()
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

        if (!bottomNavigationView.getOrCreateBadge(R.id.nav_cart)
                .hasNumber()
        ) bottomNavigationView.removeBadge(R.id.nav_cart)
        if (!bottomNavigationView.getOrCreateBadge(R.id.nav_fridge)
                .hasNumber()
        ) bottomNavigationView.removeBadge(R.id.nav_fridge)

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
            allProducts.add(cursor.getString(2))
            cursor.moveToNext()
        }
        cursor.close()
    }

    private suspend fun testDeleteAfterFinish() = withContext(Dispatchers.IO) {
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
            navigateTo(item.itemId, null)
            true
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
            navigateTo(it.itemId, null)
            bottomNavigationView.selectedItemId = it.itemId
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
        actionMode?.finish()
    }

    private fun showBothNavigation() {
        bottomNavigationView.visibility = VISIBLE
        navigationView.visibility = VISIBLE
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        bottomSlideUp()
    }

    private fun bottomSlideUp() {
        val layoutParams = bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
        behavior.slideUp(bottomNavigationView)
    }

    private fun bottomSlideDown() {
        val layoutParams = bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
        behavior.slideDown(bottomNavigationView)
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
    }

    private fun loadNightMode(): Int? {
        val sharedPreferences = getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences?.getInt("mode", 2)
    }

    companion object {
        var guide = false
        var restart = false

        lateinit var mDb: SQLiteDatabase

        val allProducts: ArrayList<String> = ArrayList()

        var isMultiSelectOn = false
        var actionMode: ActionMode? = null
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

    override fun onDestroy() {
        saveBadgeState()
        super.onDestroy()
    }

    override fun onPause() {
        waiting = CoroutineScope(Dispatchers.Main).launch {
            checkForActivity()
            finishAffinity()
        }
        saveBadgeState()
        super.onPause()
    }

    override fun onResume() {
        waiting?.cancel()
        super.onResume()
    }

    private var waiting: Job? = null

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun checkForActivity() = withContext(Dispatchers.IO) {
        Thread.sleep(600000)
    }

}
