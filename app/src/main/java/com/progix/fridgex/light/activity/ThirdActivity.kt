package com.progix.fridgex.light.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterInterface
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListNames
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListValues
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.initAdapterInterface
import com.progix.fridgex.light.helper.interfaces.AdapterInterface
import com.skydoves.transformationlayout.TransformationAppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ThirdActivity : TransformationAppCompatActivity(), AdapterInterface {

    private lateinit var recipeNameTextField: TextInputLayout
    private lateinit var timeTextField: TextInputLayout
    private lateinit var caloriesTextField: TextInputLayout
    private lateinit var proteinsTextField: TextInputLayout
    private lateinit var fatsTextField: TextInputLayout
    private lateinit var carbohydratesTextField: TextInputLayout
    private lateinit var categoryTextField: TextInputLayout
    private lateinit var recipeTextField: TextInputLayout
    private lateinit var productsTextField: TextInputLayout

    private var saved = false
    private var allEmpty = true
    private var noChanges = false
    private var someFieldsAreFilled = false
    private var fieldsCount = 0

    private var bitmapImage: Bitmap? = null

    private lateinit var fab: FloatingActionButton
    private lateinit var imageOverlay: ImageView

    val fragment = DialogProductsFragment()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)

        when (intent.extras?.get("orient")) {
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
            initProductPicker()
            initAdapterInterface(this@ThirdActivity)
            initFabOnClick()
            initImageOnClick()
        }, 550)

    }

    private fun initImageOnClick() {
        imageOverlay = findViewById(R.id.imageOverlay)
        imageOverlay.setOnClickListener {
            toggleImageSelector()
        }
    }

    private fun toggleImageSelector() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data: Intent = result.data!!
                val imageUri = data.data
                val imageStream = contentResolver.openInputStream(imageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                bitmapImage = selectedImage
                imageOverlay.setImageBitmap(selectedImage)
            }
        }

    private fun initFabOnClick() {
        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (someFieldsAreFilled || allEmpty) {
                Toast.makeText(this@ThirdActivity, getString(R.string.saveError), Toast.LENGTH_LONG)
                    .show()
            } else if (noChanges) {
                Toast.makeText(
                    this@ThirdActivity,
                    getString(R.string.thereIsNoChangesToSave),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.recipeIsAlmostReady))
                    .setMessage(getString(R.string.recipeIsAlmostReadyMessage2))
                    .setPositiveButton(getString(R.string.saveY)) { _, _ ->
                        requestSaving()
                    }
                    .setNegativeButton(getString(R.string.continueToEdit), null)
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun initProductPicker() {
        productsTextField.setEndIconOnClickListener {
            fragment.show(supportFragmentManager, "custom")
        }
        findViewById<View>(R.id.prodAction).setOnClickListener {
            fragment.show(supportFragmentManager, "custom")
        }
    }

    private fun addWatchersToTextFields() {
        recipeNameTextField.editText?.addTextChangedListener(textChangedListener(recipeNameTextField))
        timeTextField.editText?.addTextChangedListener(textChangedListener(timeTextField))
        caloriesTextField.editText?.addTextChangedListener(textChangedListener(caloriesTextField))
        proteinsTextField.editText?.addTextChangedListener(textChangedListener(proteinsTextField))
        fatsTextField.editText?.addTextChangedListener(textChangedListener(fatsTextField))
        carbohydratesTextField.editText?.addTextChangedListener(
            textChangedListener(
                carbohydratesTextField
            )
        )
        categoryTextField.editText?.addTextChangedListener(textChangedListener(categoryTextField))
        recipeTextField.editText?.addTextChangedListener(textChangedListener(recipeTextField))
        productsTextField.editText?.addTextChangedListener(textChangedListener(productsTextField))
    }

    private fun textChangedListener(textInputLayout: TextInputLayout): TextWatcher {
        var error: String = getString(R.string.theFieldCantBeEmpty)
        fieldsCount++
        if (textInputLayout == categoryTextField) error = getString(R.string.chooseCategory)
        if (textInputLayout == productsTextField) error = getString(R.string.addProducts)
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) textInputLayout.error = null
                else textInputLayout.error = error
                val temp = getTextStatus()
                when {
                    temp.isEmpty() -> allEmpty = true
                    temp.size < fieldsCount -> {
                        someFieldsAreFilled = true
                        allEmpty = false
                    }
                    else -> {
                        someFieldsAreFilled = false
                        allEmpty = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }
    }

    private fun getTextStatus(): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        result.add(recipeNameTextField.editText?.text.toString())
        result.add(timeTextField.editText?.text.toString())
        result.add(caloriesTextField.editText?.text.toString())
        result.add(proteinsTextField.editText?.text.toString())
        result.add(fatsTextField.editText?.text.toString())
        result.add(carbohydratesTextField.editText?.text.toString())
        result.add(categoryTextField.editText?.text.toString())
        result.add(recipeTextField.editText?.text.toString())
        result.add(productsTextField.editText?.text.toString())
        result.removeAll { it == "" }
        return result
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
        productsTextField.error = getString(R.string.addProducts)
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
        productsTextField = findViewById(R.id.productsTextField)
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

    private fun showExitDialog() {
        if (saved || noChanges || allEmpty) {
            super.onBackPressed()
        } else if (someFieldsAreFilled) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.someFilledTitleAlert))
                .setMessage(getString(R.string.someFilledMessageAlert))
                .setPositiveButton(getString(R.string.cont), null)
                .setNegativeButton(getString(R.string.otmenyt)) { _, _ ->
                    super.onBackPressed()
                }
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.recipeIsAlmostReady))
                .setMessage(getString(R.string.recipeIsAlmostReadyMessage))
                .setPositiveButton(getString(R.string.saveY)) { _, _ ->
                    requestSaving()
                }
                .setNegativeButton(getString(R.string.otmenyt)) { _, _ ->
                    super.onBackPressed()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun requestSaving() {
        CoroutineScope(Dispatchers.Main).launch {
            when (asyncSaving()) {
                false -> Toast.makeText(
                    applicationContext,
                    getString(R.string.saveErrorName),
                    Toast.LENGTH_SHORT
                ).show()
                true -> {
                    super.onBackPressed()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private suspend fun asyncSaving() = withContext(Dispatchers.IO) {
        val resultList = getTextStatus()
        val test = mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ?",
            listOf(resultList[0]).toTypedArray()
        )
        test.moveToFirst()
        val count = test.count
        test.close()
        if (count != 0) return@withContext false
        else {

            val newValues = ContentValues()
//            newValues.put("id", id)
//            newValues.put("category_global", )
//            newValues.put("category_local", resultList[6])
//            newValues.put("recipe_name", resultList[0])
//            newValues.put("recipe", )
//            newValues.put("recipe_value", )
//            newValues.put("time", resultList[1])
//            newValues.put("is_starred", "0")
//            newValues.put("actions", resultList[7])
//            newValues.put("source", "Авторский")
//            newValues.put("calories", resultList[2])
//            newValues.put("proteins", resultList[3])
//            newValues.put("fats", resultList[4])
//            newValues.put("carboh", resultList[5])
//            newValues.put("banned", "0")

            return@withContext true
        }
        //TODO: saving to bd
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapterListValues.clear()
        adapterListNames.clear()
        adapterInterface = null
    }

    override fun onTextChange(tempString: String) {
        productsTextField.editText?.setText(tempString.dropLast(1))
        if (tempString == "") {
            fragment.recycler?.visibility = View.GONE
            fragment.annotationCard?.visibility = View.VISIBLE
        } else {
            fragment.recycler?.visibility = View.VISIBLE
            fragment.annotationCard?.visibility = View.GONE
        }
    }
}