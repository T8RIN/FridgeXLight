package com.progix.fridgex.light.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.recipe.InfoAdapter
import com.progix.fridgex.light.adapter.viewpager.RecipeViewPagerAdapter
import com.progix.fridgex.light.custom.ApplicationBindedActivity
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.data.DataArrays.recipeImages
import com.progix.fridgex.light.data.SharedPreferencesAccess
import com.progix.fridgex.light.extensions.Extensions.initDataBase
import com.progix.fridgex.light.fragment.recipe.IngredsFragment.Companion.list
import com.progix.fridgex.light.fragment.recipe.IngredsFragment.Companion.missList
import com.progix.fridgex.light.fragment.recipe.IngredsFragment.Companion.portions
import com.progix.fridgex.light.fragment.recipe.IngredsFragment.Companion.prodList
import com.progix.fridgex.light.functions.Functions
import com.progix.fridgex.light.helper.DatabaseHelper
import kotlin.math.abs


class SecondActivity : ApplicationBindedActivity() {

    var mainRoot: CoordinatorLayout? = null
    private var expandButton: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        overridePendingTransition(R.anim.no_anim, R.anim.no_anim)

        when (SharedPreferencesAccess.loadTheme(this)) {
            "def" -> setTheme(R.style.FridgeXLight)
            "red" -> setTheme(R.style.FridgeXLight_Red)
            "pnk" -> setTheme(R.style.FridgeXLight_Pink)
            "grn" -> setTheme(R.style.FridgeXLight_Green)
            "vlt" -> setTheme(R.style.FridgeXLight_Violet)
            "yel" -> setTheme(R.style.FridgeXLight_Yellow)
            "mnt" -> setTheme(R.style.FridgeXLight_Mint)
            "ble" -> setTheme(R.style.FridgeXLight_Blue)
        }

        initDataBase()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val image: ImageView = findViewById(R.id.image)
        val intent = intent
        id = intent.getIntExtra("rec", 0)
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE id = ?",
            listOf(id.toString()).toTypedArray()
        )
        cursor.moveToFirst()
        name = cursor.getString(3)

        if (id - 1 < recipeImages.size) {
            Glide.with(this).load(recipeImages[id - 1]).into(image)
        } else {
            val id: Int = Functions.strToInt(cursor.getString(3))
            Glide.with(this).load(Functions.loadImageFromStorage(this, "recipe_$id.png"))
                .into(image)
        }

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
        supportActionBar?.title = cursor.getString(3)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mainRoot = findViewById(R.id.main_root)
        expandButton = findViewById(R.id.expand_button)

        viewPager.adapter = RecipeViewPagerAdapter(this)
        val titles = arrayOf(
            getString(R.string.products),
            getString(R.string.recipe),
            getString(R.string.infoTab)
        )
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()

        cursor.close()

        val appBarLayout: AppBarLayout = findViewById(R.id.appbar)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        Handler(mainLooper).postDelayed({
            appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { v, verticalOffset ->
                if (needToControlFab) {
                    when (abs(verticalOffset) - v.totalScrollRange) {
                        0 -> fab.hide()
                        else -> fab.show()
                    }
                }
            })
        }, 500)
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                true
            }
            R.id.copy -> {
                copyOrShare("copy")
                true
            }
            R.id.share -> {
                copyOrShare("share")
                true
            }
            R.id.expand -> {
                val cursor: Cursor = mDb.rawQuery(
                    "SELECT * FROM recipes WHERE id = ?",
                    listOf(id.toString()).toTypedArray()
                )
                cursor.moveToFirst()
                val starred = cursor.getInt(7) == 1
                val banned = cursor.getInt(14) == 1
                cursor.close()
                popupMenus(this, mainRoot!!, expandButton!!, id, starred, banned)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun copyOrShare(s: String) {
        val cursor: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE id = ?",
            listOf(id.toString()).toTypedArray()
        )
        cursor.moveToFirst()
        val name = cursor.getString(3)
        val time = cursor.getString(6)
        val calories = cursor.getString(10)
        val proteins = cursor.getString(11)
        val fats = cursor.getString(12)
        val carboh = cursor.getString(13)
        val temp = cursor.getString(8)
        val category = cursor.getString(2)
        val tr = temp.split("\n")
        var recipe = ""
        for (i in tr) recipe += (i + "\n\n")
        var ingreds = ""
        for (t in list!!) ingreds += (t.first.replaceFirstChar(Char::titlecase) + "............." + t.second + "\n")

        val sharing: String =
            name + "\n\n\n" + getString(R.string.timeCook) + "............." + time + getString(R.string.minutes) +
                    "\n" + getString(R.string.cal) + "............." + calories + getString(R.string.calories) + "\n" +
                    getString(R.string.category) + "............." + category + "\n" +
                    getString(R.string.proteins) + "............." + proteins + getString(R.string.gram) + "\n" +
                    getString(R.string.fats) + "............." + fats + getString(R.string.gram) + "\n" +
                    getString(R.string.carbohydrates) + "............." + carboh + getString(R.string.gram) + "\n" +
                    getString(R.string.portions) + " " + portions + "\n\n\n" + ingreds + "\n\n" + recipe
        cursor.close()
        when (s) {
            "copy" -> {
                Toast.makeText(this, getString(R.string.copiedRecipe), Toast.LENGTH_SHORT).show()
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", sharing + getString(R.string.copiedFrom))
                clipboard.setPrimaryClip(clip)
            }
            "share" -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    sharing + "\n\n" + getString(R.string.copiedFrom)
                )
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share)))
            }
        }

    }

    private fun popupMenus(
        context: Context,
        mainRoot: CoordinatorLayout,
        view: View,
        id: Int,
        starred: Boolean,
        banned: Boolean
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus, starred, banned)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 1 WHERE id = $id")
                    showSnackBar(
                        context,
                        mainRoot,
                        getString(R.string.addedToStarred),
                        id,
                        "is_starred",
                        0
                    )
                    true
                }
                R.id.ban_recipe -> {
                    mDb.execSQL("UPDATE recipes SET banned = 1 WHERE id = $id")
                    showSnackBar(
                        context,
                        mainRoot,
                        getString(R.string.addedToBanList),
                        id,
                        "banned",
                        0
                    )
                    true
                }
                R.id.de_star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 0 WHERE id = $id")
                    showSnackBar(
                        context,
                        mainRoot,
                        getString(R.string.delStar),
                        id,
                        "is_starred",
                        1
                    )
                    true
                }
                R.id.de_ban_recipe -> {
                    mDb.execSQL("UPDATE recipes SET banned = 0 WHERE id = $id")
                    showSnackBar(context, mainRoot, getString(R.string.delBan), id, "banned", 1)
                    true
                }
                R.id.copy -> {
                    copyOrShare("copy")
                    true
                }
                R.id.share -> {
                    copyOrShare("share")
                    true
                }
                else -> true
            }
        }
        popupMenus.setForceShowIcon(true)
        popupMenus.show()
    }

    private fun inflatePopup(popupMenus: PopupMenu, starred: Boolean, banned: Boolean) {
        if (!starred && !banned) popupMenus.inflate(R.menu.popup_menu_empty_second)
        else if (!starred && banned) popupMenus.inflate(R.menu.popup_menu_banned_second)
        else if (starred && !banned) popupMenus.inflate(R.menu.popup_menu_starred_second)
        else popupMenus.inflate(R.menu.popup_menu_both_second)
    }

    private fun showSnackBar(
        context: Context,
        mainRoot: CoordinatorLayout,
        text: String,
        id: Int,
        modifier: String,
        value: Int
    ) {
        CustomSnackbar(this).create(364, mainRoot, text, Snackbar.LENGTH_SHORT)
            .setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE recipes SET $modifier = $value WHERE id = $id")
                adapter?.notifyItemChanged(7)
            }
            .show()
        adapter?.notifyItemChanged(7)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ingred_menu, menu)
        return true
    }

    companion object {
        var id = 1

        var name = ""

        @SuppressLint("StaticFieldLeak")
        var adapter: InfoAdapter? = null

        var needToControlFab = false
    }

    override fun onDestroy() {
        adapter = null
        list = null
        prodList = null
        missList = null
        needToControlFab = false
        id = 1
        name = ""
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim)
    }

    override fun onStart() {
        if (mDb != DatabaseHelper(this).writableDatabase) mDb =
            DatabaseHelper(this).writableDatabase
        super.onStart()
    }
}