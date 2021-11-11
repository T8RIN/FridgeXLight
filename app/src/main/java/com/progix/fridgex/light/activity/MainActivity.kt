package com.progix.fridgex.light.activity


import android.annotation.SuppressLint
import android.content.res.Configuration
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
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.R.drawable.ic_baseline_menu_24
import com.progix.fridgex.light.custom.CustomTapTarget
import com.progix.fridgex.light.data.DataArrays.languages
import com.progix.fridgex.light.data.DataArrays.mainFragmentIds
import com.progix.fridgex.light.data.DataArrays.notNeedToOpenDrawerFragmentIds
import com.progix.fridgex.light.data.Extensions.dipToPixels
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadBoolean
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadFirstStart
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadNightMode
import com.progix.fridgex.light.data.SharedPreferencesAccess.loadString
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveBoolean
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveFirstStart
import com.progix.fridgex.light.data.SharedPreferencesAccess.saveString
import com.progix.fridgex.light.fragment.dialog.DialogLoadingFragment
import com.progix.fridgex.light.helper.DatabaseHelper
import com.skydoves.transformationlayout.onTransformationStartContainer
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var toolbar: Toolbar
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        when (loadNightMode(this)) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        setTheme(R.style.FridgeXLight)

        onTransformationStartContainer()

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
            beginGuide()
            guide = false
        }
        if (restart) {
            navigateTo(R.id.nav_settings, null)
            bottomNavigationView.visibility = GONE
            restart = false
        } else {
            if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                bottomNavigationView.visibility = VISIBLE
            }
        }
        setUpBadges()

        listAssign()

        if (loadFirstStart(this)) {
            showGuideDialog()
        }
    }

    private fun showGuideDialog() {
        MaterialAlertDialogBuilder(this, R.style.modeAlert)
            .setTitle(getString(R.string.guide))
            .setMessage(getString(R.string.guider))
            .setPositiveButton(getString(R.string.pass)) { _, _ ->
                beginGuide()
            }
            .setNegativeButton(getString(R.string.skip)) { _, _ ->
                saveFirstStart(this@MainActivity, false)
                Toast.makeText(this@MainActivity, getString(R.string.guideAlert), Toast.LENGTH_LONG)
                    .show()
            }
            .setCancelable(false)
            .show()
    }

    private fun beginGuide() {
        bottomNavigationView.visibility = VISIBLE
        val params = bottomNavigationView.layoutParams
        params.height = dipToPixels(56f).toInt()
        bottomNavigationView.layoutParams = params
        val targetCreator = CustomTapTarget(this)
        TapTargetSequence(this)
            .targets(
                targetCreator.create(
                    bottomNavigationView.findViewById(R.id.nav_home),
                    getString(R.string.recipesOfTheDay),
                    getString(R.string.guideDaily),
                    R.id.nav_search,
                    60
                ),
                targetCreator.create(
                    bottomNavigationView.findViewById(R.id.nav_search),
                    getString(R.string.searchG),
                    getString(R.string.guideSearch),
                    R.id.nav_fridge,
                    60
                ),
                targetCreator.create(
                    bottomNavigationView.findViewById(R.id.nav_fridge),
                    getString(R.string.fridgeG),
                    getString(R.string.guideFridge),
                    R.id.nav_cart,
                    60
                ),
                targetCreator.create(
                    bottomNavigationView.findViewById(R.id.nav_cart),
                    getString(R.string.cartG),
                    getString(R.string.guideCart),
                    R.id.nav_home,
                    60
                ),
                targetCreator.create(
                    toolbar.getChildAt(1),
                    getString(R.string.otherOptions),
                    getString(R.string.guideOther),
                    -1,
                    45
                )
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    saveFirstStart(this@MainActivity, false)
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.guideAlert),
                        Toast.LENGTH_LONG
                    ).show()
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        bottomNavigationView.visibility = INVISIBLE
                        val params2 = bottomNavigationView.layoutParams
                        params2.height = dipToPixels(2f).toInt()
                        bottomNavigationView.layoutParams = params2
                    }
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                    val id = lastTarget.id()
                    if (id != -1) {
                        navigateTo(id, null)
                        bottomNavigationView.selectedItemId = lastTarget.id()
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                }

                override fun onSequenceCanceled(lastTarget: TapTarget) {
                }
            })
            .start()
    }

    private fun setUpBadges() {
        val fridgeBadge = loadString(this, "R.id.nav_fridge")!!
        val cartBadge = loadString(this, "R.id.nav_cart")!!

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

        saveString(this, "R.id.nav_fridge", fridgeBadge)
        saveString(this, "R.id.nav_cart", cartBadge)
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
            val tCurs = mDb.rawQuery(
                "SELECT * FROM categories WHERE category = ?",
                listOf(cursor.getString(1)).toTypedArray()
            )
            tCurs.moveToFirst()
            allHints.add(tCurs.getString(2))
            tCurs.close()
            cursor.moveToNext()
        }
        cursor.close()
    }

    private fun initDataBase() {
        if (languages.contains(Locale.getDefault().displayLanguage)) {
            DatabaseHelper.DB_NAME = "FridgeXX.db"
        } else {
            DatabaseHelper.DB_NAME = "FridgeXX_en.db"
        }
        val mDBHelper = DatabaseHelper(this)
        mDb = mDBHelper.writableDatabase
        if (loadBoolean(this, "triedOnce") && !loadBoolean(
                this,
                "upgraded"
            ) || DatabaseHelper.mNeedUpdate
        ) {
            saveBoolean(this, "triedOnce", true)
            saveBoolean(this, "upgraded", false)
            DatabaseHelper.mNeedUpdate = true
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.updatedRecently))
                .setMessage(getString(R.string.updateMessage))
                .setPositiveButton(getString(R.string.update)) { _, _ ->
                    job?.cancel()
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val loadingFragment = DialogLoadingFragment()
                        loadingFragment.isCancelable = false
                        if (!loadingFragment.isAdded) loadingFragment.show(
                            supportFragmentManager,
                            "custom"
                        )
                        asyncUpdatingDatabase(mDBHelper)
                        loadingFragment.dismiss()
                        saveBoolean(this@MainActivity, "upgraded", true)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.bdSuccess),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        mDb = mDBHelper.writableDatabase
                    }
                }
                .setCancelable(false)
                .show()
            saveBoolean(this, "upgraded", false)
        }
    }

    private suspend fun asyncUpdatingDatabase(mDBHelper: DatabaseHelper) =
        withContext(Dispatchers.IO) {
            mDBHelper.updateDataBase()
        }

    private var job: Job? = null

    private var des = ""
    private fun visibilityNavElements(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            des = destination.displayName.split("id/")[1]
            when (des) {
                "nav_home" -> showBothNavigation()
                "nav_search" -> showBothNavigation()
                "nav_fridge" -> showBothNavigation()
                "nav_cart" -> showBothNavigation()
                "nav_cat" -> hideBothNavigation(true)
                "nav_products" -> hideBothNavigation(true)
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
                    R.id.nav_folder_categories -> onBackPressed()
                    R.id.nav_folder_recipes -> onBackPressed()
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
        val currentId = navController.currentDestination?.id
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else if (mainFragmentIds.contains(currentId)) {
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
        }
        else if (!notNeedToOpenDrawerFragmentIds.contains(currentId)) {
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
        saveString(this, resId.toString(), "0")
        actionMode?.finish()
    }

    private fun showBothNavigation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView.visibility = VISIBLE
        }
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
        if (lock) {
            bottomNavigationView.visibility = INVISIBLE
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSlideDown()
        }, 1)
    }

    companion object {
        var guide = false
        var restart = false

        lateinit var mDb: SQLiteDatabase

        val allProducts: ArrayList<String> = ArrayList()
        val allHints: ArrayList<String> = ArrayList()

        var isMultiSelectOn = false
        var actionMode: ActionMode? = null

    }

    override fun onDestroy() {
        saveBadgeState()
        allProducts.clear()
        allHints.clear()
        actionMode = null
        isMultiSelectOn = false
        super.onDestroy()
    }

    override fun onPause() {
        saveBadgeState()
        super.onPause()
    }

    override fun onResume() {
        updateNavStatus()
        super.onResume()
    }

    override fun onStart() {
        actionMode?.finish()
        actionMode = null
        isMultiSelectOn = false
        updateNavStatus()
        super.onStart()
    }

    private fun updateNavStatus() {
        des = navController.currentDestination?.displayName!!.split("id/")[1]
        when (des) {
            "nav_home" -> showBothNavigation()
            "nav_search" -> showBothNavigation()
            "nav_fridge" -> showBothNavigation()
            "nav_cart" -> showBothNavigation()
            "nav_cat" -> hideBothNavigation(true)
            "nav_products" -> hideBothNavigation(true)
            else -> hideBothNavigation(false)
        }
    }
}
