package com.progix.fridgex.light.functions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.progix.fridgex.light.data.DataArrays
import com.progix.fridgex.light.model.RecipeItem
import com.progix.fridgex.light.model.RecyclerSortItem
import java.io.*


object Functions {

    private const val d = 256

    fun strToInt(txt: String): Int {
        var ida = 0
        var cnt = 0
        for (element in txt) {
            ida += Character.codePointAt(charArrayOf(element), 0)
            cnt++
        }
        if (ida < 200) {
            ida += (d * 2.8).toInt()
        } else if (ida < 1000) {
            ida += 1000
        }
        return ida * cnt
    }

    fun saveToInternalStorage(
        applicationContext: Context,
        bitmapImage: Bitmap,
        name: String
    ): String? {
        val cw = ContextWrapper(applicationContext)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val path = File(directory, name)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(path)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 85, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    fun loadImageFromStorage(context: Context, name: String): Bitmap? {
        val cw = ContextWrapper(context)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        try {
            val f = File(directory, name)
            return BitmapFactory.decodeStream(FileInputStream(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun addItemToList(
        id: Int,
        pairList: ArrayList<RecyclerSortItem>,
        percentage: Double,
        time: Int,
        cal: Double,
        prot: Double,
        fats: Double,
        carboh: Double,
        indicator: Int,
        name: String,
        xOfY: String
    ) {
        when (id + 1 <= DataArrays.recipeImages.size) {
            true -> {
                pairList.add(
                    RecyclerSortItem(
                        percentage, time, cal, prot, fats, carboh,
                        RecipeItem(
                            DataArrays.recipeImages[id],
                            indicator,
                            name,
                            time.toString(),
                            xOfY
                        )
                    )
                )
            }
            else -> {
                pairList.add(
                    RecyclerSortItem(
                        percentage, time, cal, prot, fats, carboh,
                        RecipeItem(
                            -1,
                            indicator,
                            name,
                            time.toString(),
                            xOfY
                        )
                    )
                )
            }
        }
    }

}