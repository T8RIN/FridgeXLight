package com.progix.fridgex.light.helper.tess

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import com.progix.fridgex.light.data.DataArrays
import com.progix.fridgex.light.functions.Functions.doInBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

object TessHelper {

    const val TESS_URL_RUS =
        "https://github.com/tesseract-ocr/tessdata/blob/main/rus.traineddata?raw=true"
    const val TESS_URL_ENG =
        "https://github.com/tesseract-ocr/tessdata/blob/main/eng.traineddata?raw=true"
    const val RUS_FILE = "rus.traineddata"
    const val ENG_FILE = "eng.traineddata"
    const val TESS_DIRECTORY = "tessdata"

    fun createTessFile(context: Context) {
        doInBackground {
            val path =
                "${context.getDir(TESS_DIRECTORY, Context.MODE_PRIVATE)}/$TESS_DIRECTORY/$ENG_FILE"

            File("${context.getDir(TESS_DIRECTORY, Context.MODE_PRIVATE)}/$TESS_DIRECTORY").mkdirs()

            val urlConnection = URL(TESS_URL_ENG).openConnection()
            val inputStream = BufferedInputStream(urlConnection.getInputStream(), 1024 * 30)
            val outStream = FileOutputStream(File(path))
            val buff = ByteArray(30 * 1024)
            var len: Int
            while (inputStream.read(buff).also { len = it } != -1) {
                outStream.write(buff, 0, len)
            }

            inputStream.close()
            outStream.flush()
            outStream.close()


            val pathRus =
                "${context.getDir(TESS_DIRECTORY, Context.MODE_PRIVATE)}/$TESS_DIRECTORY/$RUS_FILE"

            File("${context.getDir(TESS_DIRECTORY, Context.MODE_PRIVATE)}/$TESS_DIRECTORY").mkdirs()

            val urlConnectionRus = URL(TESS_URL_RUS).openConnection()
            val inputStreamRus = BufferedInputStream(urlConnectionRus.getInputStream(), 1024 * 30)
            val outStreamRus = FileOutputStream(File(pathRus))
            val buffRus = ByteArray(30 * 1024)
            var lenRus: Int
            while (inputStreamRus.read(buffRus).also { lenRus = it } != -1) {
                outStreamRus.write(buffRus, 0, lenRus)
            }

            inputStreamRus.close()
            outStreamRus.flush()
            outStreamRus.close()
        }
    }

    suspend fun recognize(context: Context, bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        val tess = TessBaseAPI()

        var lang = "eng"
        if (DataArrays.languages.contains(Locale.getDefault().displayLanguage)) lang = "rus"

        val path = "${context.getDir(TESS_DIRECTORY, Context.MODE_PRIVATE)}"

        tess.init(path, lang)
        tess.setImage(bitmap)
        val text = tess.utF8Text
        tess.recycle()

        return@withContext text
    }

}