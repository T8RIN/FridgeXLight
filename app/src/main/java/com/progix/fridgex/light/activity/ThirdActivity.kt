package com.progix.fridgex.light.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.skydoves.transformationlayout.TransformationAppCompatActivity


class ThirdActivity : TransformationAppCompatActivity() {

    private lateinit var recipeNameTextField: TextInputLayout
    private lateinit var timeTextField: TextInputLayout
    private lateinit var caloriesTextField: TextInputLayout
    private lateinit var proteinsTextField: TextInputLayout
    private lateinit var fatsTextField: TextInputLayout
    private lateinit var carbohydratesTextField: TextInputLayout
    private lateinit var categoryTextField: TextInputLayout
    private lateinit var recipeTextField: TextInputLayout

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)

        when(resources.configuration.orientation){
            Configuration.ORIENTATION_LANDSCAPE -> {
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

       Handler(Looper.getMainLooper()).postDelayed({
           initTextFields()
           setupErrors()
           addWatchersToTextFields()
           initCategoriesDropDown(categoryTextField)
       }, 300)

        val imageOverlay: ImageView = findViewById(R.id.imageOverlay)
        imageOverlay.setOnClickListener {
            Toast.makeText(this@ThirdActivity, "clicked", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addWatchersToTextFields() {
        recipeNameTextField.editText?.addTextChangedListener(textChangedListener(recipeNameTextField))
        timeTextField.editText?.addTextChangedListener(textChangedListener(timeTextField))
        caloriesTextField.editText?.addTextChangedListener(textChangedListener(caloriesTextField))
        proteinsTextField.editText?.addTextChangedListener(textChangedListener(proteinsTextField))
        fatsTextField.editText?.addTextChangedListener(textChangedListener(fatsTextField))
        carbohydratesTextField.editText?.addTextChangedListener(textChangedListener(carbohydratesTextField))
        categoryTextField.editText?.addTextChangedListener(textChangedListener(categoryTextField))
        recipeTextField.editText?.addTextChangedListener(textChangedListener(recipeTextField))
    }

    private fun textChangedListener(textInputLayout: TextInputLayout): TextWatcher {
        var error: String = getString(R.string.theFieldCantBeEmpty)
        if(textInputLayout == categoryTextField) error = getString(R.string.chooseCategory)
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if(s.isNotEmpty()) textInputLayout.error = null
                else textInputLayout.error = error
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }
    }

    private fun setupErrors() {
        recipeNameTextField.error = getString(R.string.theFieldCantBeEmpty)
        timeTextField.error = getString(R.string.theFieldCantBeEmpty)
        caloriesTextField.error = getString(R.string.theFieldCantBeEmpty)
        proteinsTextField.error = getString(R.string.theFieldCantBeEmpty)
        fatsTextField.error = getString(R.string.theFieldCantBeEmpty)
        carbohydratesTextField.error = getString(R.string.theFieldCantBeEmpty)
        categoryTextField.error = getString(R.string.chooseCategory)
        recipeTextField.error = getString(R.string.theFieldCantBeEmpty)
    }

    private fun initTextFields() {
        recipeNameTextField = findViewById(R.id.recipeNameTextField)
        timeTextField = findViewById(R.id.timeTextField)
        caloriesTextField = findViewById(R.id.caloriesTextField)
        proteinsTextField = findViewById(R.id.proteinsTextField)
        fatsTextField = findViewById(R.id.fatsTextField)
        carbohydratesTextField = findViewById(R.id.carbohydratesTextField)
        categoryTextField = findViewById(R.id.categoryTextField)
        recipeTextField = findViewById(R.id.recipeTextField)
    }

    private fun initCategoriesDropDown(categoryTextField: TextInputLayout) {
        val categories: ArrayList<String> = ArrayList()

        val cursor: Cursor = mDb.rawQuery("SELECT * FROM recipe_category_local", null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            categories.add(cursor.getString(2))
            cursor.moveToNext()
        }
        cursor.close()
        categories.sortBy { it }

        val categoryAdapter = ArrayAdapter(this, R.layout.list_item, categories)
        (categoryTextField.editText as? AutoCompleteTextView)?.setAdapter(categoryAdapter)
    }
}