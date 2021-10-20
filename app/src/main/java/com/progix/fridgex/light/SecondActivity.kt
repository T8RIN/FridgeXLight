package com.progix.fridgex.light

import android.content.*
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.progix.fridgex.light.MainActivity.Companion.images
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.ViewPagerAdapter
import com.progix.fridgex.light.fragment.IngredsFragment.Companion.list
import com.progix.fridgex.light.fragment.IngredsFragment.Companion.portions
import java.util.*


class SecondActivity : AppCompatActivity() {

    private var mainRoot: CoordinatorLayout? = null
    private var expandButton: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.FridgeXLight)
        overridePendingTransition(R.anim.enter_fade_through, R.anim.exit_fade_through)
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

        image.setImageResource(images[id - 1])
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
        supportActionBar?.title = cursor.getString(3)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mainRoot = findViewById(R.id.main_root)
        expandButton = findViewById(R.id.expand_button)

        viewPager.adapter = ViewPagerAdapter(this)
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
            R.id.share ->{
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

        when(s){
            "copy" -> {
                Toast.makeText(this, getString(R.string.copiedRecipe), Toast.LENGTH_SHORT).show()
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", sharing + getString(R.string.copiedFrom))
                clipboard.setPrimaryClip(clip)
            }
            "share" -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, sharing + "\n\n" + getString(R.string.copiedFrom))
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share)))
            }
        }

    }

    private fun popupMenus(context: Context, mainRoot: CoordinatorLayout, view: View, id: Int, starred: Boolean, banned: Boolean) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus, starred, banned)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 1 WHERE id = $id")
                    showSnackBar(context, mainRoot, getString(R.string.addedToStarred), id, "is_starred", 0)
                    true
                }
                R.id.ban_recipe -> {
                    mDb.execSQL("UPDATE recipes SET banned = 1 WHERE id = $id")
                    showSnackBar(context, mainRoot, getString(R.string.addedToBanList), id, "banned", 0)
                    true
                }
                R.id.de_star_recipe -> {
                    mDb.execSQL("UPDATE recipes SET is_starred = 0 WHERE id = $id")
                    showSnackBar(context, mainRoot, getString(R.string.delStar), id, "is_starred", 1)
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
                R.id.share ->{
                    copyOrShare("share")
                    true
                }
                else -> true
            }

        }
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menu, true)
    }

    private fun inflatePopup(popupMenus: PopupMenu, starred: Boolean, banned: Boolean) {
        if(!starred && !banned) popupMenus.inflate(R.menu.popup_menu_empty_second)
        else if(!starred && banned) popupMenus.inflate(R.menu.popup_menu_banned_second)
        else if(starred && !banned) popupMenus.inflate(R.menu.popup_menu_starred_second)
        else popupMenus.inflate(R.menu.popup_menu_both_second)

    }

    private fun showSnackBar(context: Context, mainRoot: CoordinatorLayout, text: String, id: Int, modifier: String, value: Int) {
        val snackBar = Snackbar.make(mainRoot, text, Snackbar.LENGTH_SHORT)
            .setAction(context.getString(R.string.undo)) {
                mDb.execSQL("UPDATE recipes SET $modifier = $value WHERE id = $id")
            }
            .setActionTextColor(ContextCompat.getColor(context, R.color.checked))
            .setBackgroundTint(ContextCompat.getColor(context, R.color.manualBackground))
            .setTextColor(ContextCompat.getColor(context, R.color.manualText))
        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams

        params.anchorId = R.id.tabs
        params.anchorGravity = Gravity.TOP
        params.gravity = Gravity.TOP

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt() - 364
            val pixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpWidth.toFloat(),
                context.resources.displayMetrics
            )
            params.width = pixels.toInt()
            params.anchorId = R.id.tabs
            params.anchorGravity = Gravity.END
            params.gravity = Gravity.END
        }

        snackBar.view.layoutParams = params

        snackBar.show()
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.ingred_menu, menu)
        return true
    }

    companion object {
        var id = 1
    }
}