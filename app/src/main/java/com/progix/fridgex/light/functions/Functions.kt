package com.progix.fridgex.light.functions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
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

    fun searchString(subString: String, string: String): Int {
        val q = 101
        var h = 1
        var i: Int
        var j: Int
        var p = 0
        var t = 0
        val m = subString.length
        val n = string.length
        if (m <= n) {
            i = 0
            while (i < m - 1) {
                h = h * d % q
                ++i
            }
            i = 0
            while (i < m) {
                p = (d * p + subString[i].code) % q
                t = (d * t + string[i].code) % q
                ++i
            }
            i = 0
            while (i <= n - m) {
                if (p == t) {
                    j = 0
                    while (j < m) {
                        if (string[i + j] != subString[j]) break
                        ++j
                    }
                    if (j == m) return i
                }
                if (i < n - m) {
                    t = (d * (t - string[i].code * h) + string[i + m].code) % q
                    if (t < 0) t += q
                }
                ++i
            }
        } else return q
        return q
    }

    fun delayedAction(time: Long, func: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            func()
        }, time)
    }

}