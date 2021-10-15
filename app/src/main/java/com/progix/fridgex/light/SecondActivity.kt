package com.progix.fridgex.light

import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.progix.fridgex.light.MainActivity.Companion.images
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.ViewPagerAdapter


class SecondActivity : AppCompatActivity() {
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
                Toast.makeText(this, "Copy", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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