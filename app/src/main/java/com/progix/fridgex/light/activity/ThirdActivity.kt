package com.progix.fridgex.light.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.dialog.DialogListProductsAdapter
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.allHints
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.allProducts
import com.progix.fridgex.light.application.FridgeXLightApplication.Companion.heightPixels
import com.progix.fridgex.light.data.SharedPreferencesAccess
import com.progix.fridgex.light.extensions.Extensions.initDataBase
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListNames
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.adapterListValues
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.attachInterface
import com.progix.fridgex.light.fragment.dialog.DialogProductsFragment.Companion.dialogAdapterInterface
import com.progix.fridgex.light.functions.Functions.loadImageFromStorage
import com.progix.fridgex.light.functions.Functions.saveToInternalStorage
import com.progix.fridgex.light.functions.Functions.strToInt
import com.progix.fridgex.light.helper.DatabaseHelper
import com.progix.fridgex.light.helper.interfaces.DialogAdapterInterface
import com.progix.fridgex.light.helper.interfaces.EditListChangesInterface
import com.skydoves.transformationlayout.TransformationAppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.Integer.max

class ThirdActivity : TransformationAppCompatActivity(), DialogAdapterInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var thirdContext: Context? = null
        var editorInterface: EditListChangesInterface? = null
    }

    private lateinit var recipeNameTextField: TextInputLayout
    private lateinit var timeTextField: TextInputLayout
    private lateinit var caloriesTextField: TextInputLayout
    private lateinit var proteinsTextField: TextInputLayout
    private lateinit var fatsTextField: TextInputLayout
    private lateinit var carbohydratesTextField: TextInputLayout
    private lateinit var categoryTextField: TextInputLayout
    private lateinit var recipeTextField: TextInputLayout
    private lateinit var productsTextField: TextInputLayout

    private var editionMode = false
    private var saved = false
    private var allEmpty = true
    private var noChanges = false
    private var someFieldsAreFilled = false
    private var fieldsCount = 0

    private var bitmapImage: Bitmap? = null

    private lateinit var fab: FloatingActionButton
    private lateinit var imageOverlay: ImageView
    private lateinit var imageRecipe: ImageView

    val fragment = DialogProductsFragment()

    private var idEditingNow = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

        overridePendingTransition(R.anim.enter_fade_through, R.anim.exit_fade_through)

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
        setContentView(R.layout.activity_third)

        fragment.isCancelable = true
        thirdContext = this

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

        idEditingNow = intent.extras?.get("toEdit") as Int
        editionMode = idEditingNow != -1

        Handler(Looper.getMainLooper()).postDelayed({
            initTextFields()
            setupErrors()
            addWatchersToTextFields()
            initProductPicker()
            attachInterface(this@ThirdActivity)
            initFabOnClick()
            initImageOnClick()
            initEditMode(idEditingNow)
            initCategoriesDropDown(categoryTextField)
        }, 550)

    }

    private fun initEditMode(idToEdit: Int) {
        if (editionMode) {
            val about =
                mDb.rawQuery("SELECT * FROM recipes WHERE id = $idToEdit", null)
            about.moveToFirst()

            val recipeName = about.getString(3)
            val products = about.getString(4)
            val values = about.getString(5)
            val time = about.getString(6)
            val calories = about.getString(10)
            val category = about.getString(2)
            val recipeActions = about.getString(8)
            val proteins = about.getString(11)
            val fats = about.getString(12)
            val carbohydrates = about.getString(13)

            about.close()

            val y = strToInt(recipeName)
            bitmapImage = loadImageFromStorage(this, "recipe_$y.png")

            Glide.with(this).load(bitmapImage).into(imageRecipe)

            recipeNameTextField.editText?.setText(recipeName)
            timeTextField.editText?.setText(time)
            caloriesTextField.editText?.setText(calories)
            categoryTextField.editText?.setText(category)
            recipeTextField.editText?.setText(recipeActions)
            proteinsTextField.editText?.setText(proteins)
            fatsTextField.editText?.setText(fats)
            carbohydratesTextField.editText?.setText(carbohydrates)

            val prodArr: ArrayList<String> = ArrayList(products.split(" "))
            val valArr: ArrayList<String> = ArrayList(values.split(" "))

            if (adapterListNames == null) {
                adapterListNames = ArrayList()
                adapterListValues = ArrayList()
            }

            for (i in 0 until prodArr.size) {
                val cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE id = ?",
                    listOf(prodArr[i]).toTypedArray()
                )
                cursor.moveToFirst()
                val prodName = cursor.getString(2).replaceFirstChar { it.titlecase() }
                adapterListNames!!.add(prodName)
                adapterListValues!!.add(Pair(prodName, valArr[i]))
                cursor.close()
            }
            val hintList = ArrayList<String>()
            for (item in adapterListNames!!) {
                hintList.add(allHints!![allProducts!!.indexOf(item.lowercase())])
            }
            var tempString = ""
            for (i in adapterListValues!!) tempString += "${i.first} ... ${i.second} ${
                hintList[adapterListValues!!.indexOf(
                    i
                )]
            }\n"
            onTextChange(tempString)

            startEditingRecipeState = getTextStatus()
        }
    }

    private var startEditingRecipeState: ArrayList<String>? = null

    private fun initImageOnClick() {

        imageOverlay = findViewById(R.id.imageOverlay)
        imageRecipe = findViewById(R.id.image)

        val image = AppCompatResources.getDrawable(this, R.drawable.ic_baseline_image_400)
        imageRecipe.setImageBitmap(image?.toBitmap())

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
                val ampl = max(selectedImage.height, selectedImage.width)
                if (ampl < heightPixels * 1.5) {
                    imageRecipe.setImageBitmap(selectedImage)
                } else {
                    Toast.makeText(this, getString(R.string.tooLargeImage), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    private fun initFabOnClick() {
        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (someFieldsAreFilled || allEmpty) {
                Toast.makeText(this@ThirdActivity, getString(R.string.saveError), Toast.LENGTH_LONG)
                    .show()
            } else if (noChanges && !editionMode) {
                Toast.makeText(
                    this@ThirdActivity,
                    getString(R.string.thereIsNoChangesToSave),
                    Toast.LENGTH_LONG
                ).show()
            } else if (editionMode) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.recipeIsAlmostReady))
                    .setMessage(getString(R.string.recipeIsAlmostReadyMessageEditingFab))
                    .setPositiveButton(getString(R.string.saveY)) { _, _ ->
                        requestUpdate(idEditingNow)
                    }
                    .setNegativeButton(getString(R.string.editor), null)
                    .show()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.recipeIsAlmostReady))
                    .setMessage(getString(R.string.recipeIsAlmostReadyMessageEditingFab))
                    .setPositiveButton(getString(R.string.saveY)) { _, _ ->
                        requestSaving()
                    }
                    .setNegativeButton(getString(R.string.editor), null)
                    .show()
            }
        }
    }

    private fun initProductPicker() {
        productsTextField.setEndIconOnClickListener {
            if (!fragment.isAdded) fragment.show(supportFragmentManager, "custom")
        }
        findViewById<View>(R.id.prodAction).setOnClickListener {
            if (!fragment.isAdded) fragment.show(supportFragmentManager, "custom")
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
        if (startEditingRecipeState == getTextStatus()) noChanges = true
        if (saved || noChanges || allEmpty) {
            super.onBackPressed()
        } else if (someFieldsAreFilled && !editionMode) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.someFilledTitleAlert))
                .setMessage(getString(R.string.someFilledMessageAlert))
                .setPositiveButton(getString(R.string.cont), null)
                .setNegativeButton(getString(R.string.otmenyt)) { _, _ ->
                    super.onBackPressed()
                }
                .show()
        } else if (editionMode) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.recipeIsAlmostReady))
                .setMessage(getString(R.string.recipeIsAlmostReadyMessageEditing))
                .setPositiveButton(getString(R.string.saveY)) { _, _ ->
                    requestUpdate(idEditingNow)
                }
                .setNegativeButton(getString(R.string.discard)) { _, _ ->
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
                .show()
        }
    }

    private fun requestSaving() {
        CoroutineScope(Dispatchers.Main).launch {
            when (asyncSaving()) {
                false -> {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.saveErrorName),
                        Toast.LENGTH_SHORT
                    ).show()
                    saved = false
                }
                true -> {
                    super.onBackPressed()
                    editorInterface?.onNeedsToBeRecreated()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    saved = true
                }
            }

        }
    }

    private fun requestUpdate(idEditingNow: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            when (asyncUpdating(idEditingNow)) {
                false -> {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.saveErrorName),
                        Toast.LENGTH_SHORT
                    ).show()
                    saved = false
                }
                true -> {
                    super.onBackPressed()
                    editorInterface?.onNeedsToBeRecreated()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    saved = true
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
            var cursor = mDb.rawQuery("SELECT * FROM recipes", null)
            cursor.moveToLast()
            val id = cursor.getInt(0) + 1
            cursor.close()

            cursor = mDb.rawQuery(
                "SELECT * FROM recipe_category_local WHERE category_local = ?",
                listOf(resultList[6]).toTypedArray()
            )
            cursor.moveToFirst()
            val catGlb = cursor.getString(1)
            cursor.close()

            val products = resultList[8].split("\n")

            var productsResult = ""
            var productsValues = ""

            for (i in products) {
                val product = i.split(" ... ")[0]
                val prodCount = i.split(" ... ")[1].split(" ")[0]

                productsValues += "$prodCount "
                cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE product = ?",
                    listOf(product.lowercase()).toTypedArray()
                )
                cursor.moveToFirst()
                productsResult += "${cursor.getString(0)} "
                cursor.close()
            }

            val newValues = ContentValues()
            newValues.put("id", id)
            newValues.put("category_global", catGlb)
            newValues.put("category_local", resultList[6])
            newValues.put("recipe_name", resultList[0])
            newValues.put("recipe", productsResult.trim())
            newValues.put("recipe_value", productsValues.trim())
            newValues.put("time", resultList[1])
            newValues.put("is_starred", "0")
            newValues.put("actions", resultList[7])
            newValues.put("source", "Авторский")
            newValues.put("calories", resultList[2])
            newValues.put("proteins", resultList[3])
            newValues.put("fats", resultList[4])
            newValues.put("carboh", resultList[5])
            newValues.put("banned", "0")

            mDb.insert("recipes", null, newValues)

            val imageId = strToInt(resultList[0])

            val ll: BitmapDrawable = imageRecipe.drawable as BitmapDrawable
            bitmapImage = ll.bitmap

            saveToInternalStorage(applicationContext, bitmapImage!!, "recipe_$imageId.png")

            return@withContext true
        }
    }

    private suspend fun asyncUpdating(idEditingNow: Int) = withContext(Dispatchers.IO) {
        val resultList = getTextStatus()
        val test = mDb.rawQuery(
            "SELECT * FROM recipes WHERE recipe_name = ? AND id NOT LIKE ?",
            listOf(resultList[0], idEditingNow.toString()).toTypedArray()
        )
        test.moveToFirst()
        val count = test.count
        test.close()
        if (count != 0) return@withContext false
        else {
            var cursor = mDb.rawQuery("SELECT * FROM recipes", null)
            cursor.moveToLast()
            val id = idEditingNow
            cursor.close()

            cursor = mDb.rawQuery(
                "SELECT * FROM recipe_category_local WHERE category_local = ?",
                listOf(resultList[6]).toTypedArray()
            )
            cursor.moveToFirst()
            val catGlb = cursor.getString(1)
            cursor.close()

            val products = resultList[8].split("\n")

            var productsResult = ""
            var productsValues = ""

            for (i in products) {
                val product = i.split(" ... ")[0]
                val prodCount = i.split(" ... ")[1].split(" ")[0]

                productsValues += "$prodCount "
                cursor = mDb.rawQuery(
                    "SELECT * FROM products WHERE product = ?",
                    listOf(product.lowercase()).toTypedArray()
                )
                cursor.moveToFirst()
                productsResult += "${cursor.getString(0)} "
                cursor.close()
            }

            val newValues = ContentValues()
            newValues.put("id", id)
            newValues.put("category_global", catGlb)
            newValues.put("category_local", resultList[6])
            newValues.put("recipe_name", resultList[0])
            newValues.put("recipe", productsResult.trim())
            newValues.put("recipe_value", productsValues.trim())
            newValues.put("time", resultList[1])
            newValues.put("is_starred", "0")
            newValues.put("actions", resultList[7])
            newValues.put("source", "Авторский")
            newValues.put("calories", resultList[2])
            newValues.put("proteins", resultList[3])
            newValues.put("fats", resultList[4])
            newValues.put("carboh", resultList[5])
            newValues.put("banned", "0")

            mDb.update("recipes", newValues, "id = $id", null)

            val z: Int = strToInt(startEditingRecipeState!![0])
            val cw = ContextWrapper(this@ThirdActivity)
            val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
            val file = File(
                directory,
                "recipe_$z.png"
            )
            file.delete()
            if (file.exists()) {
                try {
                    file.canonicalFile.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (file.exists()) {
                    deleteFile(file.name)
                }
            }

            val imageId = strToInt(resultList[0])

            val ll: BitmapDrawable = imageRecipe.drawable as BitmapDrawable
            bitmapImage = ll.bitmap

            saveToInternalStorage(applicationContext, bitmapImage!!, "recipe_$imageId.png")

            return@withContext true
        }
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
        adapterListValues = null
        adapterListNames = null
        dialogAdapterInterface = null
        thirdContext = null
        editorInterface = null
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

    override fun onNeedToNotifyDataSet() {
        val hintList = ArrayList<String>()
        for (item in adapterListNames!!) {
            hintList.add(allHints!![allProducts!!.indexOf(item.lowercase())])
        }
        fragment.adapterList = DialogListProductsAdapter(this, adapterListNames!!, hintList)
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.enter_fade_through, R.anim.exit_fade_through)
    }

    override fun onStart() {
        if (mDb != DatabaseHelper(this).writableDatabase) mDb =
            DatabaseHelper(this).writableDatabase
        super.onStart()
    }
}